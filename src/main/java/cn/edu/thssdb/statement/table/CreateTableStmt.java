package cn.edu.thssdb.statement.table;

import cn.edu.thssdb.schema.Column;
import cn.edu.thssdb.schema.Database;
import cn.edu.thssdb.schema.Manager;
import cn.edu.thssdb.statement.ExecuteResult;
import cn.edu.thssdb.statement.ExecuteStatement;
import cn.edu.thssdb.transaction.Session;
import cn.edu.thssdb.utils.Global;

import java.util.ArrayList;

/**
 * @描述 创建表语句
 */
public class CreateTableStmt extends ExecuteStatement {
    private Database database;
    private String tableName;
    private Column[] columns;

    public CreateTableStmt(String tableName, ArrayList<Column> columns, String rawStatement) {
        super.rawStatement = rawStatement;
        super.needLog = true;
        this.tableName = tableName;
        this.columns = new Column[columns.size()];
        columns.toArray(this.columns);
    }

    @Override
    public ExecuteResult execute(Session session) throws Exception {
        /* 获取当前用户正在使用的数据库 */
        database = session.getDatabase();
        if (database == null) {
            throw new Exception(Global.NO_ASSIGNED_DATABASE);
        }

        boolean flag = Manager.getInstance().managerReadLock();
        database.databaseWriteLock();
        if (flag)
            Manager.getInstance().managerReadUnlock();
        database.create(tableName, columns);
        return new ExecuteResult(Global.SUCCESS_CODE, Global.CREATE_TABLE_OK);
    }

    @Override
    public void releaseLock() {
        if (database != null) {
            database.databaseWriteUnlock();
            database = null;
        }
    }
}
