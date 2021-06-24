package cn.edu.thssdb.statement;


import cn.edu.thssdb.transaction.Session;

/**
 * @描述 执行语句
 */
public abstract class ExecuteStatement
{
    protected String rawStatement;
    protected boolean needLog = false;
    protected boolean needRedo = false;

    public abstract ExecuteResult execute(Session session) throws Exception;

    public void writeToDisk(Session sysSession) throws Exception {}

    public String getRawStatement()
    {
        return rawStatement;
    }

    public boolean needLog()
    {
        return needLog;
    }

    public boolean needRedo()
    {
        return needRedo;
    }

    public void releaseLock() {}
}