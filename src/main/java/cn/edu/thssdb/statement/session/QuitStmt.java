package cn.edu.thssdb.statement.session;

import cn.edu.thssdb.statement.ExecuteResult;
import cn.edu.thssdb.statement.ExecuteStatement;
import cn.edu.thssdb.transaction.Session;
import cn.edu.thssdb.utils.Global;

/**
 * @描述 退出数据库
 */
public class QuitStmt extends ExecuteStatement {
    public QuitStmt() {
        super.needLog = true;
    }

    @Override
    public ExecuteResult execute(Session session) {
        return new ExecuteResult(Global.SUCCESS_CODE, Global.EXECUTE_OK);
    }
}
