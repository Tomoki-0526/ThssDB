package cn.edu.thssdb.statement.row;

import cn.edu.thssdb.exception.DBException;
import cn.edu.thssdb.exception.NotExistException;
import cn.edu.thssdb.query.QueryTable;
import cn.edu.thssdb.schema.*;
import cn.edu.thssdb.statement.Condition;
import cn.edu.thssdb.statement.ExecuteResult;
import cn.edu.thssdb.statement.ExecuteStatement;
import cn.edu.thssdb.transaction.Session;
import cn.edu.thssdb.utils.Global;

/**
 * @描述 删除数据语句
 */
public class DeleteStmt extends ExecuteStatement {
    private String tableName;
    private Condition condition;
    private Table table;

    public DeleteStmt(String rawStmt, String tableName, Condition condition) {
        super.needRedo = true;
        super.needLog = true;
        super.rawStatement = rawStmt;
        this.condition = condition;
        this.tableName = tableName;
    }

    @Override
    public ExecuteResult execute(Session session) throws Exception {
        Database database = session.getDatabase();
        if (database == null) throw new DBException("Haven't specified a database!");

        table = database.getTableByTableName(tableName);
        if (table == null) throw new NotExistException("table", tableName);

        table.acquireTBWriteLock();

        Column[] columns = table.getColumns().toArray(new Column[table.getColumns().size()]);
        QueryTable query = new QueryTable(table);
        int count = 0;
        while (query.hasNext()) {
            Row row = query.next();
            if (condition == null || condition.calculateResult(columns, row.getEntries().toArray(new Entry[row.getEntries().size()]))) {
                table.delete(row);
                count += 1;
            }
        }
        return new ExecuteResult(Global.SUCCESS_CODE, "Successfully delete " + count + " rows.");
    }
}
