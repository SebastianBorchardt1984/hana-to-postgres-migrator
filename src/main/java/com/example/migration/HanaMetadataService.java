package com.example.migration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.agroal.api.AgroalDataSource;
import io.quarkus.agroal.DataSource;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.example.migration.model.ColumnMeta;
import com.example.migration.model.ForeignKeyMeta;
import com.example.migration.model.TableMeta;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class HanaMetadataService {
    @Inject
    @DataSource("hana")
    AgroalDataSource hanaDataSource;

    @ConfigProperty(name = "hana.allowed-namespaces")
    String allowedNamespacesConfig;

    @ConfigProperty(name = "hana.schema")
    String hanaSchema;

    private List<String> allowedNamespaces;
    private boolean allowAllTables = false;

    @jakarta.annotation.PostConstruct
    void init() {
        if (allowedNamespacesConfig == null || allowedNamespacesConfig.isBlank()) {
            throw new RuntimeException("hana.allowed-namespaces must be configured");
        }
        if (hanaSchema == null || hanaSchema.isBlank()) {
            throw new RuntimeException("hana.schema must be configured");
        }

        if (allowedNamespacesConfig.trim().equals("*")) {
            allowAllTables = true;
        } else {
            allowedNamespaces = Arrays.stream(allowedNamespacesConfig.split(","))
                                       .map(String::trim)
                                       .toList();
        }
    }

    public List<TableMeta> fetchTables() throws SQLException {
        List<TableMeta> tables = new ArrayList<>();
        try (Connection conn = hanaDataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT TABLE_NAME FROM TABLES WHERE SCHEMA_NAME = '" + hanaSchema + "'")) {
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");

                if (shouldExcludeTable(tableName)) {
                    continue;
                }

                TableMeta table = new TableMeta();
                table.setName(tableName);
                table.setSchema(hanaSchema);
                tables.add(table);
            }
        }
        return tables;
    }

    private boolean shouldExcludeTable(String tableName) {
        if (allowAllTables) {
            return false;
        }
        if (tableName.startsWith("/")) {
            return allowedNamespaces.stream().noneMatch(tableName::startsWith);
        }
        return false;
    }

    public void fetchColumns(TableMeta table) throws SQLException {
        try (Connection conn = hanaDataSource.getConnection();
             ResultSet cols = conn.getMetaData().getColumns(null, table.getSchema(), table.getName(), "%")) {
            while (cols.next()) {
                ColumnMeta col = new ColumnMeta();
                col.setName(cols.getString("COLUMN_NAME"));
                col.setTypeName(cols.getString("TYPE_NAME"));
                col.setSize(cols.getInt("COLUMN_SIZE"));
                col.setDecimalDigits(cols.getInt("DECIMAL_DIGITS"));
                col.setNullable(cols.getInt("NULLABLE") != DatabaseMetaData.columnNoNulls);
                table.getColumns().add(col);
            }
        }
    }

    public void fetchPrimaryKey(TableMeta table) throws SQLException {
        try (Connection conn = hanaDataSource.getConnection();
             ResultSet pk = conn.getMetaData().getPrimaryKeys(null, table.getSchema(), table.getName())) {
            while (pk.next()) {
                table.getPrimaryKeys().add(pk.getString("COLUMN_NAME"));
            }
        }
    }

    public void fetchForeignKeys(TableMeta table) throws SQLException {
        try (Connection conn = hanaDataSource.getConnection();
             ResultSet fk = conn.getMetaData().getImportedKeys(null, table.getSchema(), table.getName())) {
            while (fk.next()) {
                ForeignKeyMeta fkm = new ForeignKeyMeta();
                fkm.setFkColumn(fk.getString("FKCOLUMN_NAME"));
                fkm.setPkTable(fk.getString("PKTABLE_NAME"));
                fkm.setPkColumn(fk.getString("PKCOLUMN_NAME"));
                fkm.setFkName(fk.getString("FK_NAME"));
                table.getForeignKeys().add(fkm);
            }
        }
    }
}
