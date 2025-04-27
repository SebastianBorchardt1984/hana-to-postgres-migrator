package com.example.migration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.agroal.api.AgroalDataSource;

import java.sql.Connection;
import java.sql.Statement;

import com.example.migration.model.ColumnMeta;
import com.example.migration.model.ForeignKeyMeta;
import com.example.migration.model.TableMeta;

import java.io.IOException;
import java.nio.file.*;

@ApplicationScoped
public class PostgresSchemaService {

    @Inject
    AgroalDataSource pgDataSource;

    public void createTableInPostgres(TableMeta table) throws Exception {
        String ddl = buildDdlForTable(table);
        
        try (Connection conn = pgDataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(ddl);
        }

        appendToFlywayScript(ddl);
    }

    public void generateFlywayScriptOnly(TableMeta table) throws IOException {
        String ddl = buildDdlForTable(table);
        appendToFlywayScript(ddl);
    }

    private String buildDdlForTable(TableMeta table) {
        StringBuilder ddl = new StringBuilder();
ddl.append("CREATE TABLE ").append(table.getName()).append(" (\n");

        for (ColumnMeta col : table.getColumns()) {
            String pgType = HanaToPostgresTypeMapper.map(col.getTypeName(), col.getSize(), col.getDecimalDigits());
            ddl.append("  ").append(col.getName())
               .append(" ").append(pgType);
            if (!col.isNullable()) {
                ddl.append(" NOT NULL");
            }
ddl.append(",\n");
        }

        if (!table.getPrimaryKeys().isEmpty()) {
            ddl.append("  PRIMARY KEY (").append(String.join(", ", table.getPrimaryKeys())).append(")");
        } else {
            ddl.setLength(ddl.length() - 2);
            ddl.append("");
        }

ddl.append(");\n");

        for (ForeignKeyMeta fk : table.getForeignKeys()) {
            ddl.append("ALTER TABLE ").append(table.getName())
               .append(" ADD CONSTRAINT ").append(fk.getFkName())
               .append(" FOREIGN KEY (").append(fk.getFkColumn())
               .append(") REFERENCES ").append(fk.getPkTable())
               .append(" (").append(fk.getPkColumn()).append(");");
        }

        return ddl.toString();
    }

    private void appendToFlywayScript(String ddl) throws IOException {
        Path flywayDir = Paths.get("src/main/resources/db/migration");
        Files.createDirectories(flywayDir);
        Path file = flywayDir.resolve("V1__init.sql");
        Files.writeString(file, ddl + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}
