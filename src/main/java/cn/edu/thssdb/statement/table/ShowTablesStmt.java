package cn.edu.thssdb.statement.table;

import cn.edu.thssdb.schema.Database;
import cn.edu.thssdb.statement.ExecuteResult;
import cn.edu.thssdb.statement.ExecuteStatement;
import cn.edu.thssdb.transaction.Session;
import cn.edu.thssdb.utils.Global;

import java.util.ArrayList;

/**
 * @描述 展示表语句
 */
public class ShowTablesStmt extends ExecuteStatement {
    private String databaseName;

    public ShowTablesStmt(String rawStatement, String databaseName) {
        super.rawStatement = rawStatement;
        this.databaseName = databaseName;
    }

    @Override
    public ExecuteResult execute(Session session) throws Exception{
        /* 检查用户正在使用的数据库 */
        Database database = session.getDatabase();
        if (database == null)
            throw new Exception(Global.NO_ASSIGNED_DATABASE);
        ArrayList<String> tableNames = database.getTableNames();
        return new ExecuteResult(Global.SUCCESS_CODE, Global.SHOW_TABLES_OK);
    }
}
