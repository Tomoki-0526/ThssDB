package cn.edu.thssdb.statement.row;

import cn.edu.thssdb.exception.DBException;
import cn.edu.thssdb.schema.*;
import cn.edu.thssdb.statement.*;
import cn.edu.thssdb.transaction.Session;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @描述 选择语句
 */
public class SelectStmt extends ExecuteStatement {
    private ArrayList<ResultColumn> resultColumns;
    //目前只支持两个table之间的join,所以这里table_query的size为2
    private ArrayList<TableQuery> tableQueries;
    private Condition condition;
    private boolean isDistinct;
    private boolean isAll;


    private ArrayList<Column> columns = new ArrayList<>();
    private ArrayList<Row> resultRows = new ArrayList<>();

    private ArrayList<Boolean> isAdds = null;

    private ArrayList<Table> tables = new ArrayList<>();

    public SelectStmt(String rawStatement, ArrayList<ResultColumn> resultCols, ArrayList<TableQuery> tableQueries,
                           Condition condition, boolean isDistinct, boolean isAll) {
        super.rawStatement  = rawStatement;
        this.resultColumns = resultCols;
        this.tableQueries = tableQueries;
        this.condition = condition;
        this.isDistinct = isDistinct;
        this.isAll = isAll;
    }

    @Override
    public ExecuteResult execute(Session session) throws Exception {
        Database currentDatabase = session.getDatabase();
        if (currentDatabase == null) throw new DBException("Haven't specified a database!");

        for (TableQuery tableQuery : tableQueries) {
            tableQuery.executeTableQuery(currentDatabase);
            this.columns.addAll(tableQuery.getColumns());
        }

        // -----------------------------------------------------------------
        // 获取所有读锁的过程
        tables = getAllTables();
        for (Table table : tables) {
            table.acquireTBReadLock();
        }

        for (ResultColumn resultColumn : resultColumns) {
            resultColumn.executeResultColumn(currentDatabase);
        }
        // ------------------------------------------------------------------

        try {

            // resultColumns的大小一定大于等于1，然后当为*号时，表示选择全部
            boolean isMul = resultColumns.size() == 1 && resultColumns.get(0).isMul();
            if (isMul) { // 全部复制为true,添加每一项
                if (resultColumns.get(0).getTableName() == null) {
                    // 只有*号出现，选择全部
                    isAdds = new ArrayList<>(Collections.nCopies(this.columns.size(), true));
                } else {
                    // table.*，选择属于该table的column
                    isAdds = new ArrayList<>(Collections.nCopies(resultColumns.get(0).getColumnNames().size(), true));
                }
            } else {
                isAdds = new ArrayList<>(Collections.nCopies(this.columns.size(), false));
                // 每次添加row都需要遍历循环，
                int index = 0;
                for (Column column : columns) {
                    for (ResultColumn resultColumn : resultColumns) {
                        ArrayList<String> names = resultColumn.getColumnNames();
                        for (String name : names) {
                            // 此处必须使用equals，否则会因为内存地址不相同永远返回不想等
                            if (column.getName().equals(name)) {
                                isAdds.set(index, true);
                                break;
                            }
                        }
                    }
                    index++;// column的下标
                }
            }
            combineRow(new ArrayList<>(), 0);

        } finally {

            for (Table table : tables) {
                table.releaseTBReadLock();
            }
            tables = null;
        }

        return new ExecuteResult(this.columns, this.isAdds, this.resultRows);
    }

    private void combineRow(ArrayList<Row> rows, int index) {
        if (index == tableQueries.size()) {
            ArrayList<Entry> entries = new ArrayList<>();
            for (Row row : rows) {
                entries.addAll(row.getEntries());
            }
            if (condition == null ||
                    condition.calculateResult(columns.toArray(new Column[columns.size()]), entries.toArray(new Entry[entries.size()]))) {
                addRow(entries);
            }
            return;
        }
        ArrayList<Row> rs = tableQueries.get(index).getRows();
        for (Row row : rs) {
            rows.add(row);
            combineRow(rows, index + 1);
            rows.remove(row);
        }
    }

    private void addRow(ArrayList<Entry> entries) {
        ArrayList<Entry> es = new ArrayList<>();
        // 遍历，符合resultcolumns对应的就添加entry
        for (int i = 0; i < isAdds.size(); i++) {
            if (isAdds.get(i)) es.add(entries.get(i));
        }
        Entry[] ans = new Entry[entries.size()];
        entries.toArray(ans);
        resultRows.add(new Row(ans));
    }

    private ArrayList<Table> getAllTables() {
        ArrayList<Table> tables = new ArrayList<>();
        for (TableQuery t : tableQueries) {
            tables.addAll(t.getTables());
        }
        return tables;
    }
}
