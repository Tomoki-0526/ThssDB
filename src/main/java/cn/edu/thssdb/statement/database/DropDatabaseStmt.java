package cn.edu.thssdb.statement.database;

import cn.edu.thssdb.schema.Manager;
import cn.edu.thssdb.statement.ExecuteResult;
import cn.edu.thssdb.statement.ExecuteStatement;
import cn.edu.thssdb.transaction.Session;
import cn.edu.thssdb.utils.Global;

/**
 * @描述 删除数据库语句
 */
public class DropDatabaseStmt extends ExecuteStatement {
    private String databaseName;
    private boolean askExists;

    public DropDatabaseStmt(String rawStatement, String databaseName, boolean askExists) {
        this.databaseName = databaseName;
        super.rawStatement = rawStatement;
        super.needLog = true;
        this.askExists = askExists;
    }

    @Override
    public ExecuteResult execute(Session session) {
        Manager manager = Manager.getInstance();
        manager.managerWriteLock();
        if(askExists) {
            /* 检查数据库是否存在 */
            if (!manager.hasDatabase(databaseName))
                return new ExecuteResult(Global.FAILURE_CODE, Global.DROP_DB_FAIL);
        }
        Manager.getInstance().deleteDatabase(session, databaseName);
        return new ExecuteResult(Global.SUCCESS_CODE, Global.DROP_DB_OK);

    }

    @Override
    public void releaseLock() {
        Manager.getInstance().managerWriteUnlock();
    }
}
