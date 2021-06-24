package cn.edu.thssdb.statement.transaction;

import cn.edu.thssdb.statement.ExecuteResult;
import cn.edu.thssdb.statement.ExecuteStatement;
import cn.edu.thssdb.transaction.Session;
import cn.edu.thssdb.utils.Global;

/**
 * @描述 开始事务语句
 */
public class TransactionStmt extends ExecuteStatement {
    public TransactionStmt(String rawStatement) {
        super.needLog = true;
        super.rawStatement = rawStatement;
    }

    @Override
    public ExecuteResult execute(Session session) {
        session.setInTransaction(true);
        return new ExecuteResult(Global.SUCCESS_CODE, Global.BEGIN_TRANSACTION_OK);
    }
}
