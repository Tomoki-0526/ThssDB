package cn.edu.thssdb.statement.database;

import cn.edu.thssdb.schema.Manager;
import cn.edu.thssdb.statement.ExecuteResult;
import cn.edu.thssdb.statement.ExecuteStatement;
import cn.edu.thssdb.transaction.Session;
import cn.edu.thssdb.utils.Global;

/**
 * @描述 切换数据库语句
 */
public class UseDatabaseStmt extends ExecuteStatement {
    private String targetDatabaseName;

    public UseDatabaseStmt(String targetDatabaseName, String rawStatement) {
        this.targetDatabaseName = targetDatabaseName;
        super.needLog = true;
        super.rawStatement = rawStatement;
        super.needRedo = true;
    }

    @Override
    public ExecuteResult execute(Session session) {
        boolean flag = Manager.getInstance().managerReadLock();
        Manager.getInstance().switchDatabase(session, targetDatabaseName);
        if (flag)
            Manager.getInstance().managerReadUnlock();
        return new ExecuteResult(Global.SUCCESS_CODE, Global.SWITCH_DB_OK);
    }
}
