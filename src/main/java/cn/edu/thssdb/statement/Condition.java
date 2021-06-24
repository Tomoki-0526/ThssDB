package cn.edu.thssdb.statement;

import cn.edu.thssdb.exception.DBException;
import cn.edu.thssdb.exception.SQLSyntaxException;
import cn.edu.thssdb.schema.Column;
import cn.edu.thssdb.schema.Entry;
import cn.edu.thssdb.type.ColumnType;
import cn.edu.thssdb.type.CompareOperation;
import cn.edu.thssdb.type.LogicOperation;
import cn.edu.thssdb.utils.Tool;
import cn.edu.thssdb.utils.Pair;

public class Condition {

    private boolean isLeaf;
    private Object leftNode;
    private Object rightNode;
    private Object operation;


    // 传入的参数是 表达式子节点 和 操作字符串
    public Condition(Expression leftExpr, Expression rightExpr, String opStr) {
        this.isLeaf = true;
        this.leftNode = leftExpr;
        this.rightNode = rightExpr;
        this.operation = Tool.stringToCompareOP(opStr);
    }

    // 传入的参数是 condition 子节点 和 操作字符串
    public Condition(Condition leftNode, Condition rightNode, String opStr) {
        this.isLeaf = false;
        this.leftNode = leftNode;
        this.rightNode = rightNode;
        this.operation = Tool.stringToLogicOP(opStr);
    }

    // 计算 condition 的结果
    public boolean calculateResult(Column[] columns, Entry[] entries) throws DBException {
        if (this.isLeaf) {
            try {

                Pair<ColumnType, Comparable> leftResult = ((Expression) this.leftNode).calculateResult(columns, entries);
                Pair<ColumnType, Comparable> rightResult = ((Expression) this.rightNode).calculateResult(columns, entries);

                // 处理 null 的情况
                if (leftResult.right == null || rightResult.right == null) {
                    return false;
                }

                CompareOperation compareOP = (CompareOperation) this.operation;
                ColumnType leftType = leftResult.left;
                ColumnType rightType = rightResult.left;
                ColumnType targetType = Expression.getResultType(leftType, rightType);

                // 不允许 string 和 非string 进行比较
                if (targetType == ColumnType.STRING && (leftType != ColumnType.STRING || rightType != ColumnType.STRING)) {
                    throw new SQLSyntaxException(String.format("Exception: operation %s not allowed between %s and %s",
                            Tool.compareOPToString(compareOP), Tool.columnTypeToString(leftType), Tool.columnTypeToString(rightType)));
                }
                // 进行类型转换
                Comparable leftValue, rightValue;
                if (leftType != rightType) {
                    leftValue = Expression.convertToTargetType(leftResult.right, targetType);
                    rightValue = Expression.convertToTargetType(rightResult.right, targetType);
                } else {
                    leftValue = leftResult.right;
                    rightValue = rightResult.right;
                }
                // 计算结果
                return calculateCompareResult(leftValue, rightValue, targetType, compareOP);

            } catch (DBException e) {
                throw e;
            } catch (Exception e) {
                // 可能存在的类型转换异常
                throw new DBException(e.getMessage());
            }

        } else {
            boolean leftValue, rightValue;
            LogicOperation logicOP;
            try {
                leftValue = ((Condition) leftNode).calculateResult(columns, entries);
                rightValue = ((Condition) rightNode).calculateResult(columns, entries);
                logicOP = (LogicOperation) this.operation;
            } catch (DBException e) {
                throw e;
            } catch (Exception e) {
                throw new DBException(e.getMessage());
            }

            if (logicOP == LogicOperation.AND) {
                return leftValue && rightValue;
            } else {
                return leftValue || rightValue;
            }
        }
    }

    private static boolean calculateCompareResult(Comparable a, Comparable b, ColumnType targetType, CompareOperation op) {
        switch (op) {
            case EQ:
                return valueEQ(a, b, targetType);
            case NE:
                return !valueEQ(a, b, targetType);
            case GE:
                return !valueLT(a, b, targetType);
            case LE:
                return !valueGT(a, b, targetType);
            case GT:
                return valueGT(a, b, targetType);
            default:
                return valueLT(a, b, targetType);
        }
    }

    private static boolean valueEQ(Comparable a, Comparable b, ColumnType targetType) {
        switch (targetType) {
            case INT:
                return (int) a == (int) b;
            case LONG:
                return (long) a == (long) b;
            case FLOAT:
                return (float) a == (float) b;
            case DOUBLE:
                return (double) a == (double) b;
            default:
                return a.equals(b);
        }
    }

    private static boolean valueGT(Comparable a, Comparable b, ColumnType targetType) {
        switch (targetType) {
            case INT:
                return (int) a > (int) b;
            case LONG:
                return (long) a > (long) b;
            case FLOAT:
                return (float) a > (float) b;
            case DOUBLE:
                return (double) a > (double) b;
            default:
                return ((String) a).compareTo((String) b) > 0;
        }
    }

    private static boolean valueLT(Comparable a, Comparable b, ColumnType targetType) {
        switch (targetType) {
            case INT:
                return (int) a < (int) b;
            case LONG:
                return (long) a < (long) b;
            case FLOAT:
                return (float) a < (float) b;
            case DOUBLE:
                return (double) a < (double) b;
            default:
                return ((String) a).compareTo((String) b) < 0;
        }
    }

}
