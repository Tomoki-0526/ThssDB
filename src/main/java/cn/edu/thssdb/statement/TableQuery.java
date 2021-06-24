package cn.edu.thssdb.statement;

import cn.edu.thssdb.exception.DBException;
import cn.edu.thssdb.exception.NotExistException;
import cn.edu.thssdb.index.BPlusTreeIterator;
import cn.edu.thssdb.query.QueryTable;
import cn.edu.thssdb.schema.*;

import java.io.IOException;
import java.util.ArrayList;

public class TableQuery {
    private String[] tableNames;
    private Condition condition;

    /**
     * 本质上tableQuery就是一个table，如果是join，就是把join后的新table返回作为结果
     * 但是考虑到join后难以确认主键，所以直接储存所有满足条件的row
     */
    private ArrayList<Column> columns;
    private ArrayList<Row> rows;
    // 存储参与join的tables，应当只是table中的索引，而不会重新创建table
    private ArrayList<Table> tables;

    public TableQuery(ArrayList<String> tableNames, Condition condition) {
        this.tableNames = tableNames.toArray(new String[tableNames.size()]);
        this.condition = condition;
        this.columns = new ArrayList<>();
        this.tables = new ArrayList<>();
        this.rows = new ArrayList<>();

    }

    public void executeTableQuery(Database database) throws IOException, DBException {
        for (int i = 0; i < tableNames.length; i++) {
            Table table = database.getTableByTableName(tableNames[i]);
            if(table == null)throw new NotExistException("table", tableNames[i]);
            tables.add(table);
        }
        /**
         * 进行两个table的join
         */
        // 进行两个table的columns的合并
        for (int i = 0; i < tables.size(); i++) {
            combineColumns(tables.get(i).getColumns(), i);
        }
        combineRow(new ArrayList<>(), 0);
    }

    private void combineColumns(ArrayList<Column> cols, int tableIndex) {
        for (int i = 0; i < cols.size(); i++) {
            Column col = cols.get(i);
            String columnName = tableNames[tableIndex] + "." + col.getName();
            Column column = new Column(col);
            column.setName(columnName);
            this.columns.add(column);
        }
    }

    private void combineRow(ArrayList<Row> rows, int tableIndex) throws IOException {
        if (tableIndex == 0) {
            rows = new ArrayList<>();
        }
        if (tableIndex >= tables.size()) {
            ArrayList<Entry> entries = new ArrayList<>();
            for (Row row : rows) {
                entries.addAll(row.getEntries());
            }
            Column[] columns1 = columns.toArray(new Column[columns.size()]);
            Entry[] entries1 = entries.toArray(new Entry[entries.size()]);
            if (condition == null || condition.calculateResult(columns1, entries1)) {
                Entry[] es = new Entry[entries.size()];
                entries.toArray(es);
                this.rows.add(new Row(es));
            }
            return;
        }
        Table table = tables.get(tableIndex);
        BPlusTreeIterator<Entry, Row> iterator = table.getIndexTree().iterator();
        QueryTable query = new QueryTable(table);
        while (query.hasNext()) {
            Row row = query.next();
            rows.add(row);
            combineRow(rows, tableIndex + 1);
            rows.remove(row);
        }
    }

    public ArrayList<Column> getColumns() {
        return columns;
    }

    public ArrayList<Row> getRows() {
        return rows;
    }

    public ArrayList<Table> getTables() {
        return tables;
    }
}
