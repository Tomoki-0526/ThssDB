package cn.edu.thssdb.statement.table;

import cn.edu.thssdb.schema.Database;
import cn.edu.thssdb.schema.Manager;
import cn.edu.thssdb.statement.ExecuteResult;
import cn.edu.thssdb.statement.ExecuteStatement;
import cn.edu.thssdb.transaction.Session;
import cn.edu.thssdb.utils.Global;

/**
 * @描述 删除表语句
 */
public class DropTableStmt extends ExecuteStatement {
    private String tableName;
    private Database database;

    public DropTableStmt(String tableName, String rawStatement) {
        this.tableName = tableName;
        super.rawStatement = rawStatement;
        super.needLog = true;
    }

    @Override
    public ExecuteResult execute(Session session) throws Exception {
        /* 检查当前用户正在使用的数据库 */
        database = session.getDatabase();
        if (database == null)
            throw new Exception(Global.NO_ASSIGNED_DATABASE);
        boolean flag = Manager.getInstance().managerReadLock();
        database.databaseWriteLock();
        if (flag)
            Manager.getInstance().managerReadUnlock();
        database.drop(tableName);
        return new ExecuteResult(Global.SUCCESS_CODE, Global.DROP_TABLE_OK);
    }

    @Override
    public void releaseLock() {
        if (database != null) {
            database.databaseWriteUnlock();
            database = null;
        }
    }
}
