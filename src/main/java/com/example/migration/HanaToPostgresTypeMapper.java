package com.example.migration;

public class HanaToPostgresTypeMapper {

    public static String map(String hanaType, int size, int scale) {
        String type = hanaType.toUpperCase();
        switch (type) {
            case "NVARCHAR":
            case "VARCHAR":
            case "SHORTTEXT":
                return "VARCHAR(" + size + ")";
            case "ALPHANUM":
            case "TEXT":
            case "CLOB":
            case "NCLOB":
                return "TEXT";
            case "CHAR":
                return "CHAR(" + size + ")";
            case "DECIMAL":
            case "NUMERIC":
                return "NUMERIC(" + size + ", " + scale + ")";
            case "SMALLINT":
                return "SMALLINT";
            case "INTEGER":
            case "INT":
                return "INTEGER";
            case "BIGINT":
                return "BIGINT";
            case "REAL":
                return "REAL";
            case "DOUBLE":
            case "FLOAT":
                return "DOUBLE PRECISION";
            case "DATE":
                return "DATE";
            case "TIME":
                return "TIME";
            case "SECONDDATE":
            case "TIMESTAMP":
                return "TIMESTAMP";
            case "BINARY":
            case "VARBINARY":
            case "BLOB":
                return "BYTEA";
            case "BOOLEAN":
                return "BOOLEAN";
            default:
                return "TEXT";
        }
    }
}
