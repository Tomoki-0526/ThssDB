package cn.edu.thssdb.statement.transaction;

import cn.edu.thssdb.statement.ExecuteResult;
import cn.edu.thssdb.statement.ExecuteStatement;
import cn.edu.thssdb.transaction.Session;
import cn.edu.thssdb.utils.Global;

/**
 * @描述 提交事务语句
 */
public class CommitStmt extends ExecuteStatement {
    public CommitStmt(String rawStatement) {
        super.needLog = true;
        super.rawStatement = rawStatement;
    }

    @Override
    public ExecuteResult execute(Session session) {
        return new ExecuteResult(Global.SUCCESS_CODE, Global.COMMIT_OK);
    }
}
