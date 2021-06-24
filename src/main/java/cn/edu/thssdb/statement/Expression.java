package cn.edu.thssdb.statement;

import cn.edu.thssdb.exception.DBException;
import cn.edu.thssdb.exception.RowTableNotCompatibleException;
import cn.edu.thssdb.exception.SQLSyntaxException;
import cn.edu.thssdb.schema.Column;
import cn.edu.thssdb.schema.Entry;
import cn.edu.thssdb.type.ColumnType;
import cn.edu.thssdb.utils.Global;
import cn.edu.thssdb.utils.Tool;
import javafx.util.Pair;


public class Expression {
    // leaf node 的一些属性
    private String columnName;
    private int leafType;
    private Pair<ColumnType, Comparable> value;

    // 0 const value
    // 1 full name column
    // 2 column name only

    // 非叶子节点的属性
    private Expression leftNode;
    private Expression rightNode;
    private String operation;

    //1.只有当expr出现expr的左右子节点时，才不是叶子节点，2.其余全为叶子节点，
    private boolean isLeaf;

    public Expression(Expression leftNode, Expression rightNode, String operation) {
        this.isLeaf = false;
        this.leftNode = leftNode;
        this.rightNode = rightNode;
        this.operation = operation;
    }

    // 常量值节点
    public Expression(Pair<ColumnType, Comparable> value) {
        this.value = value;
        this.isLeaf = true;
        this.leafType = 0;
    }

    public Expression(Comparable value, ColumnType type){
        this.value = new Pair<>(type, value);
        this.isLeaf = true;
        this.leafType = 0;
    }

    public Expression(String tableName, String columnName) {
        this.isLeaf = true;
        this.leafType = 1;
        this.columnName = tableName + "." + columnName;
    }

    // column 字段节点
    public Expression(String columnName) {
        this.isLeaf = true;// 这里应当也是叶子节点
        this.leafType = 2;
        this.columnName = columnName;
    }

    public int getLeafType() {
        return this.leafType;
    }


    // 构造好的 columns 和 entries
    public Pair<ColumnType, Comparable> calculateResult(Column[] columns, Entry[] entries) throws DBException {
        // 传入参数错误
        if (columns.length != entries.length) {
            throw new RowTableNotCompatibleException();
        }

        if (this.isLeaf) {
            if (this.leafType == 0) {
                return this.value;
            } else {
                int length = columns.length;
                for (int i = 0; i < length; ++i) {
                    if (columns[i].getName().equals(this.columnName)) {
                        return new Pair<>(entries[i].getType(), entries[i].getValue());
                    }
                }
            }
            // 没有查找到对应的 column
            throw new SQLSyntaxException("Exception : unknown column name : " + this.columnName);
        } else {

            // 递归调用左右孩子节点进行计算
            Pair<ColumnType, Comparable> leftResult = this.leftNode.calculateResult(columns, entries);
            Pair<ColumnType, Comparable> rightResult = this.rightNode.calculateResult(columns, entries);

            // 先处理 null，涉及到 null 的算数计算结果都是 null
            if (leftResult.getValue() == null || rightResult.getValue() == null) {
                return new Pair<ColumnType, Comparable>(ColumnType.INT, null);
            }

            ColumnType resultType = getResultType(leftResult.getKey(), rightResult.getKey());

            // 两种类型不能进行计算
            if (resultType == ColumnType.STRING && !"+".equals(this.operation)) {
                throw new SQLSyntaxException(String.format("Exception : operation %s not allowed between %s and %s",
                        this.operation, Tool.columnTypeToString(leftResult.getKey()), Tool.columnTypeToString(rightResult.getKey())));
            }

            // 转换到最终类型
            Comparable leftValue = convertToTargetType(leftResult.getValue(), resultType);
            Comparable rightValue = convertToTargetType(rightResult.getValue(), resultType);

            // 继续计算
            Comparable resultValue;
            switch (this.operation) {
                case Global.ADD_STR:
                    resultValue = add(leftValue, rightValue, resultType);
                    break;
                case Global.MINUS_STR:
                    resultValue = minus(leftValue, rightValue, resultType);
                    break;
                case Global.MULTIPLY_STR:
                    resultValue = multiply(leftValue, rightValue, resultType);
                    break;
                case Global.DIVIDE_STR:
                    resultValue = divide(leftValue, rightValue, resultType);
                    break;
                default:
                    throw new SQLSyntaxException("Exception: unknown arithmetic operation: " + this.operation);
            }
            return new Pair<>(resultType, resultValue);
        }
    }


    static ColumnType getResultType(ColumnType type1, ColumnType type2) {
        return type1.ordinal() > type2.ordinal() ? type1 : type2;
    }

    public static Comparable convertToTargetType(Comparable a, ColumnType targetType) {
        switch (targetType) {
            case INT:
                return Integer.parseInt(String.valueOf(a));
            case LONG:
                return Long.parseLong(String.valueOf(a));
            case FLOAT:
                return Float.parseFloat(String.valueOf(a));
            case DOUBLE:
                return Double.parseDouble(String.valueOf(a));
            default:
                return String.valueOf(a);
        }
    }

    public static Comparable add(Comparable a, Comparable b, ColumnType targetType) {
        switch (targetType) {
            case INT:
                return (int) a + (int) b;
            case LONG:
                return (long) a + (long) b;
            case FLOAT:
                return (float) a + (float) b;
            case DOUBLE:
                return (double) a + (double) b;
            default:
                return (String) a + (String) b;
        }
    }

    private static Comparable minus(Comparable a, Comparable b, ColumnType targetType) {
        switch (targetType) {
            case INT:
                return (int) a - (int) b;
            case LONG:
                return (long) a - (long) b;
            case FLOAT:
                return (float) a - (float) b;
            case DOUBLE:
                return (double) a - (double) b;
            default:
                return null;
        }
    }

    private static Comparable multiply(Comparable a, Comparable b, ColumnType targetType) {
        switch (targetType) {
            case INT:
                return (int) a * (int) b;
            case LONG:
                return (long) a * (long) b;
            case FLOAT:
                return (float) a * (float) b;
            case DOUBLE:
                return (double) a * (double) b;
            default:
                return null;
        }
    }

    private static Comparable divide(Comparable a, Comparable b, ColumnType targetType) {
        switch (targetType) {
            case INT:
                return (int) a / (int) b;
            case LONG:
                return (long) a / (long) b;
            case FLOAT:
                return (float) a / (float) b;
            case DOUBLE:
                return (double) a / (double) b;
            default:
                return null;
        }
    }
}
