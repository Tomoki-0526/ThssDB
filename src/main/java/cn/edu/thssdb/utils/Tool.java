package cn.edu.thssdb.utils;

import cn.edu.thssdb.exception.DBException;
import cn.edu.thssdb.exception.MultipleKeyException;
import cn.edu.thssdb.exception.NotExistException;
import cn.edu.thssdb.schema.Column;
import cn.edu.thssdb.type.ArithmeticOperation;
import cn.edu.thssdb.type.ColumnType;
import cn.edu.thssdb.type.CompareOperation;
import cn.edu.thssdb.type.LogicOperation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Tool {
    public static boolean isValidColumn(Column[] columns) {
        return true;
    }

    public static String columnTypeToString(ColumnType type) {
        switch (type) {
            case INT:
                return "int";
            case LONG:
                return "long";
            case FLOAT:
                return "float";
            case DOUBLE:
                return "double";
            default:
                return "string";
        }
    }

    public static ColumnType stringToColumnType(String type) {
        switch (type) {
            case "int":
                return ColumnType.INT;
            case "long":
                return ColumnType.LONG;
            case "float":
                return ColumnType.FLOAT;
            case "double":
                return ColumnType.DOUBLE;
            default:
                return ColumnType.STRING;
        }
    }


    public static CompareOperation stringToCompareOP(String operation) {
        switch (operation) {
            case Global.EQ_STR:
                return CompareOperation.EQ;
            case Global.NE_STR:
                return CompareOperation.NE;
            case Global.GE_STR:
                return CompareOperation.GE;
            case Global.LE_STR:
                return CompareOperation.LE;
            case Global.GT_STR:
                return CompareOperation.GT;
            case Global.LT_STR:
                return CompareOperation.LT;
            default:
                return null;
        }
    }

    public static ArithmeticOperation stringToArithmeticOP(String operation) {
        switch (operation) {
            case Global.ADD_STR:
                return ArithmeticOperation.ADD;
            case Global.MINUS_STR:
                return ArithmeticOperation.MINUS;
            case Global.MULTIPLY_STR:
                return ArithmeticOperation.MULTIPLY;
            case Global.DIVIDE_STR:
                return ArithmeticOperation.DIVIDE;
            default:
                return null;
        }
    }

    public static String compareOPToString(CompareOperation operation) {
        switch (operation) {
            case EQ:
                return Global.EQ_STR;
            case NE:
                return Global.NE_STR;
            case GE:
                return Global.GE_STR;
            case LE:
                return Global.LE_STR;
            case GT:
                return Global.GT_STR;
            case LT:
                return Global.LT_STR;
            default:
                return null;
        }

    }

    public static LogicOperation stringToLogicOP(String operation) {
        if ("&&".equals(operation)) {
            return LogicOperation.AND;
        } else if ("||".equals(operation)) {
            return LogicOperation.OR;
        } else {
            return null;
        }
    }


    // 获得 columns 中有索引的一列编号
    public static int getPrimaryIndex(Column[] columns) throws DBException {
        int size = columns.length;
        int index = -1;
        int index_count = 0;
        for (int i = 0; i < size; i++) {
            if (columns[i].isPrimary()) {
                index = i;
                index_count++;
            }
        }
        if(index_count <= 0) throw new NotExistException("primary key","");
        if (index_count > 1) throw new MultipleKeyException();
        else return index;
    }

    public static void showResult(List<String> columnList, List<List<String>> rowList) {
        // 记录每一列的长度
        ArrayList<Integer> integers = new ArrayList<>();
        int count = 0;
        // 获取长度
        for (String column : columnList) {
            integers.add(column.length());
            count += column.length();
        }
        for(List<String> row : rowList){
            for (int i = 0; i < row.size(); i++){
                if(row.get(i) != null && integers.get(i) < row.get(i).length()){
                    integers.set(i, row.get(i).length());
                }
            }
        }
//        print("+");
//        for(Integer inte : integers){
//            print(new String());
//        }
        print("|");

        for(int i = 0; i < columnList.size(); i++){
            print(columnList.get(i) + String.join("", Collections.nCopies(integers.get(i)-columnList.get(i).length()," "))+ "|");
        }
        print("\n");
        print(String.join("", Collections.nCopies(count, "-")));

        for (List<String> row : rowList) {
            print("\n|");
            int index = 0;
            for (String entry : row) {
                int cnt = integers.get(index);
                if(entry==null) entry="null";
                print(entry + String.join("", Collections.nCopies(cnt-entry.length()," ")) + "|");
                index ++;
            }
        }
        print("\n");
    }

    private static void print(String msg) {
        System.out.printf(msg);
    }
}
