package cn.edu.thssdb.statement;

import java.io.IOException;
import java.util.ArrayList;

import cn.edu.thssdb.parser.SQLBaseVisitor;
import cn.edu.thssdb.parser.SQLVisitor;
import cn.edu.thssdb.statement.transaction.CommitStmt;
import cn.edu.thssdb.transaction.LogManager;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import cn.edu.thssdb.parser.SQLLexer;
import cn.edu.thssdb.parser.SQLParser;
import cn.edu.thssdb.transaction.Session;
import cn.edu.thssdb.utils.Global;

/**
 * @描述 SQL语句执行器
 */
public class Executor
{
    /**
     * SQL语句解析
     * @param statement
     * @return
     * @throws Exception
     */
    public static ArrayList<ExecuteStatement> parseStatement(String statement) throws Exception
    {
        SQLLexer lexer = new SQLLexer(CharStreams.fromString(statement));
        SQLParser parser = new SQLParser(new CommonTokenStream(lexer));
        SQLParser.ParseContext parseTree = parser.parse();
        SQLVisitor visitor = new SQLBaseVisitor();
        return (ArrayList<ExecuteStatement>) visitor.visit(parseTree);
    }

    /**
     * SQL语句执行
     * @param statement SQL语句
     * @param session 用户session
     * @return
     */
    public static ExecuteResult execute(String statement, Session session) throws Exception {
        ExecuteResult res;
        /* 检查用户是否登录 */
        if (session == null)
        {
            return new ExecuteResult(Global.FAILURE_CODE, Global.NEED_LOGIN);
        }

        /* 解析 */
        ArrayList<ExecuteStatement> stmtList;
        try
        {
            stmtList = parseStatement(statement);
        }
        catch (Exception e) // 解析不成功，有语法错误
        {
            String msg = e.getMessage();
            if (msg == null || msg.isEmpty())
            {
                msg = Global.SYNTAX_ERROR;
            }
            return new ExecuteResult(Global.FAILURE_CODE, msg);
        }
        if (stmtList.size() == 0) // 没解析到东西
        {
            return new ExecuteResult(Global.FAILURE_CODE, Global.NEED_QUERY);
        }
        if (stmtList.size() != 1) // 解析到多条查询语句
        {
            return new ExecuteResult(Global.FAILURE_CODE, Global.SINGLE_SUPPORT);
        }

        /* 执行 */
        ExecuteStatement stmt = stmtList.get(0);
        if (stmt == null)
        {
            return new ExecuteResult(Global.FAILURE_CODE, Global.SYNTAX_ERROR);
        }
        try
        {
            res = stmt.execute(session);
        }
        catch (Exception e)
        {
            if (!session.isInTransaction())
            {
                stmt.releaseLock();
            }
            else
            {
                session.addLockStmt(stmt);
            }
            return new ExecuteResult(Global.FAILURE_CODE, e.getMessage());
        }

        /* 提交 */
        commit(stmt, session);

        return res;
    }
    
    /**
     * 提交
     * @param statement
     * @param session
     */
    private static void commit(ExecuteStatement statement, Session session) throws Exception {
        if (!(statement instanceof CommitStmt)) {
            if (statement.needLog()) {
                session.addOperation(statement);
                if (session.isInTransaction()) {
                    session.addLockStmt(statement);
                }
                else {
                    LogManager.getInstance().writeLog(session);
                    session.emptyOperations();
                    statement.releaseLock();
                    LogManager.getInstance().checkLog();
                }
            }
            else {
                if (session.isInTransaction()) {
                    session.addOperation(statement);
                    LogManager.getInstance().writeLog(session);
                    session.emptyOperations();
                    session.setInTransaction(false);
                    session.releaseLockStmt();
                    LogManager.getInstance().checkLog();
                }
            }
        }
    }

    /**
     * 恢复时执行log文件
     * @param statement
     * @param systemSession
     */
    public static void executeLog(String statement, Session systemSession) {
        ArrayList<ExecuteStatement> statementArrayList = new ArrayList<>();
        /* 解析语句 */
        try {
            statementArrayList = parseStatement(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for(ExecuteStatement stmt: statementArrayList) {
            if (!stmt.needRedo())
                continue;
            try {
                stmt.execute(systemSession);
                stmt.releaseLock();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        systemSession.releaseDatabase();
    }
}