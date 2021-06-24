package cn.edu.thssdb.statement.database;

import cn.edu.thssdb.schema.Manager;
import cn.edu.thssdb.statement.ExecuteResult;
import cn.edu.thssdb.statement.ExecuteStatement;
import cn.edu.thssdb.transaction.Session;
import cn.edu.thssdb.utils.Global;

/**
 * @描述 创建数据库语句
 */
public class CreateDatabaseStmt extends ExecuteStatement {
    private String databaseName;

    public CreateDatabaseStmt(String rawStatement, String databaseName)
    {
        super.needLog = true;
        super.rawStatement = rawStatement;
        this.databaseName = databaseName;
    }

    @Override
    public ExecuteResult execute(Session session) throws Exception
    {
        Manager.getInstance().managerWriteLock();
        Manager.getInstance().createDatabaseIfNotExists(this.databaseName);
        return new ExecuteResult(Global.SUCCESS_CODE, Global.CREATE_DB_OK);
    }

    @Override
    public void releaseLock() {
        Manager.getInstance().managerWriteUnlock();
    }
}
