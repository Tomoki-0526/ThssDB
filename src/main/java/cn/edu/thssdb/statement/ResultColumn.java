package cn.edu.thssdb.statement;

import cn.edu.thssdb.exception.DBException;
import cn.edu.thssdb.exception.NotExistException;
import cn.edu.thssdb.schema.Column;
import cn.edu.thssdb.schema.Database;
import cn.edu.thssdb.schema.Table;

import java.io.IOException;
import java.util.ArrayList;

public class ResultColumn {
    private String tableName;
    private ArrayList<String> columnNames = new ArrayList<>();
    private boolean mul;
    public ResultColumn(String tableName, String columnName)  throws DBException {
        if(tableName == null) {
            throw new NotExistException("table", "tableName");
        }
        this.columnNames.add(tableName+"."+columnName);
        this.mul = false;
    }
    public ResultColumn(String tableName, boolean mul){
        this.tableName = tableName;
        this.mul = mul;// 这个时候传入的mul为true
    }
    public ResultColumn(boolean mul) {
        this.mul = mul;
        this.tableName = null;
        this.columnNames = null;
    }

    public ArrayList<String> getColumnNames() {
        return columnNames;
    }

    public boolean isMul() {
        return mul;
    }

    public String getTableName() {
        return tableName;
    }

    public void executeResultColumn(Database database) {
        if(!mul || tableName == null) return;
        Table table = database.getTableByTableName(tableName);
        ArrayList<Column> columns = table.getColumns();
        for(Column column : columns){
            this.columnNames.add(tableName+"."+column.getName());
        }
    }
}
