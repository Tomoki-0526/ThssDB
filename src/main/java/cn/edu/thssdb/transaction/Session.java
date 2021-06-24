package cn.edu.thssdb.transaction;

import java.util.ArrayList;

import cn.edu.thssdb.schema.Database;
import cn.edu.thssdb.statement.ExecuteStatement;

/**
 * @描述 保持一个用户的会话状态
 */
public class Session {
    // sessionId
    private long sessionId;
    // 是否在事务中
    private boolean inTransaction = false;
    // 当前用户的database
    private Database database = null;
    // 操作序列
    private ArrayList<String> operations = new ArrayList<>();
    // 是否有写操作
    private boolean hasWrite = false;
    // 锁语句
    private ArrayList<ExecuteStatement> lockStatements = new ArrayList<>();

    public Session(long sessionId)
    {
        this.sessionId = sessionId;
    }

    public Session(long sessionId, Database database)
    {
        this.sessionId = sessionId;
        this.database = database;
    }

    public long getSessionId()
    {
        return sessionId;
    }

    public void addOperation(ExecuteStatement statement) {
        // TODO
    }

    public void emptyOperations()
    {
        operations.clear();
        hasWrite = false;
    }

    public ArrayList<String> getOperations() {
        return this.operations;
    }

    public Database getDatabase()
    {
        return database;
    }

    public void setDatabase(Database database)
    {
        this.database = database;
    }

    public boolean isInTransaction()
    {
        return inTransaction;
    }

    public void setInTransaction(boolean inTransaction)
    {
        this.inTransaction = inTransaction;
    }

    public void addLockStmt(ExecuteStatement statement)
    {
        lockStatements.add(statement);
    }

    public void releaseLockStmt()
    {
        if (database != null) {
            database.degradeToReadLock();
        }
        for (ExecuteStatement statement: lockStatements) {
            statement.releaseLock();
        }
        lockStatements.clear();
    }

    public void releaseDatabase()
    {
        if (database != null) {
            while (database.databaseWriteUnlock());
            while (database.databaseReadUnlock());
            database = null;
        }
    }
}
