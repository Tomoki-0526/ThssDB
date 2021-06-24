package cn.edu.thssdb.statement.row;

import cn.edu.thssdb.schema.*;
import cn.edu.thssdb.statement.ExecuteResult;
import cn.edu.thssdb.statement.ExecuteStatement;
import cn.edu.thssdb.transaction.Session;
import cn.edu.thssdb.utils.Global;

import java.util.ArrayList;

/**
 * @描述 增加数据语句
 */
public class InsertStmt extends ExecuteStatement {
    private String tableName;
    private ArrayList<String> columnNames;
    private ArrayList<ArrayList<Comparable>> values;
    private Table table;

    public InsertStmt(String tableName, ArrayList<String> columnNames, ArrayList<ArrayList<Comparable>> values, String rawStatement) {
        this.columnNames = columnNames;
        this.values = values;
        this.tableName = tableName;
        super.needLog = true;
        super.needRedo = true;
        super.rawStatement  = rawStatement;
    }

    @Override
    public ExecuteResult execute(Session session) throws Exception {
        /* 检查数据库 */
        Database database = session.getDatabase();
        if (database == null) {
            throw new Exception(Global.NO_ASSIGNED_DATABASE);
        }

        /* 检查表 */
        table = database.getTableByTableName(tableName);
        if (table == null) {
            throw new Exception(Global.NO_SUCH_TABLE);
        }
        table.acquireTBWriteLock(); // TODO: table上写锁

        /* 获取表的属性 */
        ArrayList<Column> columns = table.getColumns();
        if (columnNames == null || columns.size() == 0) {
            columnNames = new ArrayList<>();
            for (Column col: columns)
                columnNames.add(col.getName());
        }

        /* 添加数据 */
        for (ArrayList<Comparable> comparableArrayList: values) {
            ArrayList<Entry> entryArrayList = new ArrayList<>();
            for (int i = 0; i < columns.size(); ++i) {
                int index = columnNames.indexOf(columns.get(i).getName());
                if (index > 0)
                    entryArrayList.add(new Entry(comparableArrayList.get(index)));
                else
                    entryArrayList.add(new Entry(null));
            }
            Entry[] entries = new Entry[entryArrayList.size()];
            entryArrayList.toArray(entries);
            table.insert(new Row(entries));
        }

        return new ExecuteResult(Global.SUCCESS_CODE, Global.INSERT_OK);
    }

    @Override
    public void releaseLock() {
        if (table != null) {
            table.releaseTBWriteLock();
            table = null;
        }
    }

    @Override
    public void writeToDisk(Session systemSession) throws Exception {
        Database database = systemSession.getDatabase();
        if (database == null)
            throw new Exception(Global.NO_ASSIGNED_DATABASE);

        table = database.getTableByTableName(tableName);
        if (table == null)
            return;

        table.acquireTBWriteLock();

        if (table.needWrite()) {
            table.writeToDisk();
            table.setNeedWrite(false);
        }
    }
}