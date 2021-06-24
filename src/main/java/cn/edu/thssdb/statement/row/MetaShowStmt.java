package cn.edu.thssdb.statement.row;

import cn.edu.thssdb.exception.DBException;
import cn.edu.thssdb.exception.NotExistException;
import cn.edu.thssdb.schema.Database;
import cn.edu.thssdb.schema.Table;
import cn.edu.thssdb.statement.ExecuteResult;
import cn.edu.thssdb.statement.ExecuteStatement;
import cn.edu.thssdb.transaction.Session;

public class MetaShowStmt extends ExecuteStatement {
    private String tableName;

    public MetaShowStmt(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public ExecuteResult execute(Session session) throws Exception {
        Database database = session.getDatabase();
        if (database == null) throw new DBException("Haven't specified a database!");
        Table table = database.getTableByTableName(this.tableName);
        if (table == null) throw new NotExistException("table", tableName);

        table.acquireTBReadLock();
        ExecuteResult result = new ExecuteResult(table.getColumns(), true);
        table.releaseTBReadLock();

        return result;
    }
}
