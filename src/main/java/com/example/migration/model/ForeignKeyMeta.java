package com.example.migration.model;

public class ForeignKeyMeta {
    private String fkColumn;
    private String pkTable;
    private String pkColumn;
    private String fkName;

    // Getter und Setter

    public String getFkColumn() { return fkColumn; }
    public void setFkColumn(String fkColumn) { this.fkColumn = fkColumn; }

    public String getPkTable() { return pkTable; }
    public void setPkTable(String pkTable) { this.pkTable = pkTable; }

    public String getPkColumn() { return pkColumn; }
    public void setPkColumn(String pkColumn) { this.pkColumn = pkColumn; }

    public String getFkName() { return fkName; }
    public void setFkName(String fkName) { this.fkName = fkName; }
}
