package com.example.demo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

public class HiveTableGenerator {

    private static final String DRIVER_NAME = "org.apache.hive.jdbc.HiveDriver";
    private static final String CONNECTION_URL = "jdbc:hive2://192.168.80.183:10000/default";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    private static final String TABLE_NAME = "test_table";
    private static final String[] COLUMN_NAMES = {"id", "boolean_col", "tinyint_col", "smallint_col", "int_col", "bigint_col", "float_col", "double_col", "decimal_col", "string_col", "varchar_col", "char_col", "date_col", "timestamp_col", "array_col", "map_col", "struct_col", "union_col"};

    public static void main(String[] args) {
        try {
            Class.forName(DRIVER_NAME);
            Connection con = DriverManager.getConnection(CONNECTION_URL, USERNAME, PASSWORD);
            Statement stmt = con.createStatement();

            // Create table
            StringBuilder createTableQuery = new StringBuilder("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (");
            for (int i = 0; i < COLUMN_NAMES.length; i++) {
                String columnName = COLUMN_NAMES[i];
                String columnType = getColumnType(i);
                createTableQuery.append(columnName).append(" ").append(columnType);
                if (i < COLUMN_NAMES.length - 1) {
                    createTableQuery.append(", ");
                }
            }
            createTableQuery.append(")");
            stmt.execute(createTableQuery.toString());

            // Generate and insert data
            Random random = new Random();
            StringBuilder insertQuery = new StringBuilder("INSERT INTO " + TABLE_NAME + " VALUES ");
            for (int i = 0; i < 10000000; i++) {
                insertQuery.append("(").append(i).append(", ");
                insertQuery.append(random.nextBoolean()).append(", ");
                insertQuery.append(random.nextInt(128)).append(", ");
                insertQuery.append(random.nextInt(32768)).append(", ");
                insertQuery.append(random.nextInt()).append(", ");
                insertQuery.append(random.nextLong()).append(", ");
                insertQuery.append(random.nextFloat()).append(", ");
                insertQuery.append(random.nextDouble()).append(", ");
                insertQuery.append("CAST(").append(random.nextInt(100000)).append(" AS DECIMAL(18,2))").append(", ");
                insertQuery.append("'string_value_" + i + "'").append(", ");
                insertQuery.append("'varchar_value_" + i + "'").append(", ");
                insertQuery.append("'char_value_" + i + "'").append(", ");
                insertQuery.append("'2022-01-01'").append(", ");
                insertQuery.append("'2022-01-01 00:00:00'").append(", ");
                insertQuery.append("[1,2,3]").append(", ");
                insertQuery.append("MAP('key1', 'value1', 'key2', 'value2')").append(", ");
                insertQuery.append("STRUCT('field1', 1, 'field2', 'value2')").append(", ");
                insertQuery.append("UNIONTYPE<int, string>('string_value_" + i + "')");
                insertQuery.append(")");
                if (i < 99999999) {
                    insertQuery.append(", ");
                }

            }
            stmt.execute(insertQuery.toString());

            stmt.close();
            con.close();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private static String getColumnType(int index) {
        switch (index) {
            case 0:
                return "INT";
            case 1:
                return "BOOLEAN";
            case 2:
                return "TINYINT";
            case 3:
                return "SMALLINT";
            case 4:
                return "INT";
            case 5:
                return "BIGINT";
            case 6:
                return "FLOAT";
            case 7:
                return "DOUBLE";
            case 8:
                return "DECIMAL(18,2)";
            case 9:
                return "STRING";
            case 10:
                return "VARCHAR(20)";
            case 11:
                return "CHAR(10)";
            case 12:
                return "DATE";
            case 13:
                return "TIMESTAMP";
            case 14:
                return "ARRAY<INT>";
            case 15:
                return "MAP<STRING, STRING>";
            case 16:
                return "STRUCT<field1:INT, field2:STRING>";
            case 17:
                return "UNIONTYPE<INT, STRING>";
            default:
                return "STRING";
        }
    }
}
