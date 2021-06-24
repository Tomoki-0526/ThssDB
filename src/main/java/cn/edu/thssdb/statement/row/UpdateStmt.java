package cn.edu.thssdb.statement.row;

import cn.edu.thssdb.exception.DBException;
import cn.edu.thssdb.exception.NotExistException;
import cn.edu.thssdb.query.QueryTable;
import cn.edu.thssdb.schema.*;
import cn.edu.thssdb.statement.Condition;
import cn.edu.thssdb.statement.ExecuteResult;
import cn.edu.thssdb.statement.ExecuteStatement;
import cn.edu.thssdb.statement.Expression;
import cn.edu.thssdb.transaction.Session;
import cn.edu.thssdb.type.ColumnType;
import cn.edu.thssdb.utils.Global;
import cn.edu.thssdb.utils.Pair;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @描述 更新数据语句
 */
public class UpdateStmt extends ExecuteStatement {
    private String tableName;
    private String columnName;
    private Expression expression;
    private Condition condition;
    private Table table;

    public UpdateStmt(String rawStmt, String tableName, String columnName, Expression expression, Condition condition) {
        super.needRedo = true;
        super.needLog = true;
        super.rawStatement = rawStmt;
        this.tableName = tableName;
        this.columnName = columnName;
        this.expression = expression;
        this.condition = condition;
    }

    @Override
    public ExecuteResult execute(Session session) throws Exception {
        Database database = session.getDatabase();
        if (database == null) throw new DBException("Haven't specified a database");
        table = database.getTableByTableName(this.tableName);
        if (table == null) throw new NotExistException("table", this.tableName);

        table.acquireTBWriteLock();

        ArrayList<Column> columns = table.getColumns();
        int columnIdx = getColumnIndex(columns);
        if (columnIdx == -1) {
            throw new NotExistException("column", this.columnName);
        }
        QueryTable query = new QueryTable(table);
        Column[] columns_ = columns.toArray(new Column[columns.size()]);
        Column targetColumn = columns_[columnIdx];
        ColumnType targetType = targetColumn.getType();
        boolean targetNotNull = targetColumn.isNotNull();

        boolean execSuccess = true;
        String errorMsg = "";
        ArrayList<Row> changedRows = new ArrayList<>();

        while (query.hasNext()) {
            Row row = query.next();
            Entry[] entries = row.getEntries().toArray(new Entry[row.getEntries().size()]);
            if (condition.calculateResult(columns_, entries)) {
                Pair<ColumnType, Comparable> value = this.expression.calculateResult(columns_, entries);
                // 计算结果为空，但是改列不允许为空
                if (value.right == null && targetColumn.isNotNull()) {
                    execSuccess = false;
                    errorMsg = "Exception : trying to assign null to not null column!";
                    break;
                }
                Comparable targetResult;
                // 类型转换失败
                try {
                    targetResult = Expression.convertToTargetType(value.right, targetType);
                } catch (Exception e) {
                    execSuccess = false;
                    errorMsg = "Exception: type cast failed!";
                    break;
                }
                row.getEntries().get(columnIdx).setValue(targetResult);
                changedRows.add(row);
            }
        }
        int count = 0;
        if (!execSuccess) {
            throw new DBException(errorMsg);
        } else {
            for (Row row : changedRows) {
                table.update(row);
                count += 1;
            }
        }
        return new ExecuteResult(Global.SUCCESS_CODE, "Successfully update " + count + " rows.");
    }

    private int getColumnIndex(ArrayList<Column> columns) {
        int size = columns.size();
        for (int i = 0; i < size; i++) {
            if (columns.get(i).getName().equals(this.columnName)) return i;
        }
        return -1;
    }


    @Override
    public void releaseLock() {
        if (table != null) {
            table.releaseTBWriteLock();
            table = null;
        }
    }

    public String getTableName() {
        return this.tableName;
    }

    @Override
    public void writeToDisk(Session sysSession) throws DBException, IOException {
        Database database = sysSession.getDatabase();
        if (database == null) throw new DBException("Haven't specified a database!");

        table = database.getTableByTableName(tableName);
        if (table == null) return;
        table.acquireTBWriteLock();

        if (table.isNeedWrite()) {
            try {
                table.writeToDisk();
            } catch (Exception e) {
                e.printStackTrace();
            }
            table.setNeedWrite(false);
        }
    }
}
