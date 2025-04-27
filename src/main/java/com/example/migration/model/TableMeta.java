package com.example.migration.model;

import java.util.ArrayList;
import java.util.List;

public class TableMeta {
    private String name;
    private String schema;
    private List<ColumnMeta> columns = new ArrayList<>();
    private List<String> primaryKeys = new ArrayList<>();
    private List<ForeignKeyMeta> foreignKeys = new ArrayList<>();

    // Getter und Setter

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSchema() { return schema; }
    public void setSchema(String schema) { this.schema = schema; }

    public List<ColumnMeta> getColumns() { return columns; }
    public List<String> getPrimaryKeys() { return primaryKeys; }
    public List<ForeignKeyMeta> getForeignKeys() { return foreignKeys; }
}
