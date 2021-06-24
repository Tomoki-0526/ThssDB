package cn.edu.thssdb.transaction;

import cn.edu.thssdb.schema.Manager;
import cn.edu.thssdb.statement.ExecuteStatement;
import cn.edu.thssdb.statement.Executor;
import cn.edu.thssdb.statement.database.UseDatabaseStmt;
import cn.edu.thssdb.utils.Global;

import java.io.*;
import java.util.ArrayList;

public class LogManager {
    private static final int MAX_COUNT = 100;
    private String path = Global.ROOT_DIR + "db.log";
    private int crtRowCount = 0;

    public static LogManager getInstance() {
        return logManagerHolder.INSTANCE;
    }

    private LogManager() throws IOException {
        File logFile = new File(path);
        if(!logFile.exists()){
            logFile.createNewFile();
            crtRowCount = 0;
        }
        else {
            FileReader fileReader = new FileReader(logFile);
            LineNumberReader lineNumberReader = new LineNumberReader(fileReader);
            lineNumberReader.skip(Long.MAX_VALUE);
            crtRowCount = lineNumberReader.getLineNumber() + 1;
            fileReader.close();
            lineNumberReader.close();
        }
    }

    private int getLogCount() throws IOException {
        File logFile = new File(path);
        if(!logFile.exists()){
            logFile.createNewFile();
            return 0;
        }
        FileReader fileReader = new FileReader(logFile);
        LineNumberReader lineNumberReader = new LineNumberReader(fileReader);
        lineNumberReader.skip(Long.MAX_VALUE);
        int lines = lineNumberReader.getLineNumber() + 1;
        fileReader.close();
        lineNumberReader.close();
        return lines;
    }

    public String getLog() throws IOException {
        File logFile = new File(path);
        if(!logFile.exists()){
            logFile.createNewFile();
            return "";
        }
        else {
            FileReader fileReader = new FileReader(logFile);
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader bufferReader = new BufferedReader(fileReader);
            String line = "";
            while ((line = bufferReader.readLine())!=null){
                stringBuilder.append(line).append(";");
            }
            bufferReader.close();
            return stringBuilder.toString();
        }
    }

    public synchronized void writeLog(Session session) throws IOException {
        File logFile = new File(path);
        if(!logFile.exists()){
            logFile.createNewFile();
        }
        else {
           FileWriter fileWriter = new FileWriter(logFile,true);
           ArrayList<String> rawStmt = session.getOperations();
           for(int i = 0;i<rawStmt.size();i++){
               fileWriter.write(rawStmt.get(i)+";\n");
               crtRowCount++;
           }
           fileWriter.flush();
           fileWriter.close();
        }
    }

    public void clearLog() throws IOException {
        File logFile = new File(path);
        if(!logFile.exists()){
            logFile.createNewFile();
        }
        else {
            FileWriter fileWriter = new FileWriter(logFile);
            fileWriter.write("");
            fileWriter.flush();
            fileWriter.close();
            crtRowCount = 0;
        }
    }

    public synchronized void checkLog() throws Exception {
        if (crtRowCount >= MAX_COUNT){
            ArrayList<ExecuteStatement> statements = Executor.parseStatement(getLog());
            Session session = Manager.getInstance().getConnectSession();
            session.releaseDatabase();
            for(int i = 0; i < statements.size(); i++){
                if (statements.get(i) instanceof UseDatabaseStmt){
                    statements.get(i).execute(session);
                }
                if(statements.get(i).needRedo()){
                    statements.get(i).writeToDisk(session);
                }
                session.addLockStmt(statements.get(i));
            }
            session.releaseLockStmt();
            session.releaseDatabase();
            clearLog();
        }
    }

    private static class logManagerHolder {
        private static LogManager INSTANCE = null;

        static {
            try {
                INSTANCE = new LogManager();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private logManagerHolder() {
        }
    }
}
