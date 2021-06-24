package cn.edu.thssdb.statement.database;

import cn.edu.thssdb.schema.Manager;
import cn.edu.thssdb.statement.ExecuteResult;
import cn.edu.thssdb.statement.ExecuteStatement;
import cn.edu.thssdb.transaction.Session;

import java.util.ArrayList;

/**
 * @描述 显示所有数据库语句
 */
public class ShowDatabasesStmt extends ExecuteStatement {
    public ShowDatabasesStmt(String rawStatement) {
        this.rawStatement = rawStatement;
    }

    @Override
    public ExecuteResult execute(Session session) {
        boolean flag = Manager.getInstance().managerReadLock();
        ArrayList<String> databaseNames = Manager.getInstance().getDatabaseNames();
        if (flag)
            Manager.getInstance().managerReadUnlock();
        return new ExecuteResult(databaseNames, "Database");
    }
}
