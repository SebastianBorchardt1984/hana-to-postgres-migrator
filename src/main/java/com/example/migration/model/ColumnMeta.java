package com.example.migration.model;

public class ColumnMeta {
    private String name;
    private String typeName;
    private int size;
    private int decimalDigits;
    private boolean nullable;

    // Getter und Setter

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public int getDecimalDigits() { return decimalDigits; }
    public void setDecimalDigits(int decimalDigits) { this.decimalDigits = decimalDigits; }

    public boolean isNullable() { return nullable; }
    public void setNullable(boolean nullable) { this.nullable = nullable; }
}
