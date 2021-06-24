package cn.edu.thssdb.statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import cn.edu.thssdb.schema.Column;
import cn.edu.thssdb.schema.Entry;
import cn.edu.thssdb.schema.Row;
import cn.edu.thssdb.utils.Global;

/**
 * @描述 执行结果
 */
public class ExecuteResult {
    private int code;
    private String msg;
    private boolean hasResult;

    public static ExecuteResult successResult = new ExecuteResult(Global.SUCCESS_CODE, Global.EXECUTE_OK);

    /* 需要展示的行和列数据 */
    private List<List<String>> rowList = null;
    private List<String> columnList = null;

    /* Metadata */
    public static String[] columnMetadata = {"name", "type", "isPrimary", "isNull", "maxLength"};

    /**
     * 普通构造函数
     * @param code
     * @param msg
     */
    public ExecuteResult(int code, String msg) 
    {
        this.code = code;
        this.msg = msg;
        this.hasResult = false;
    }

    /**
     * 基于show metadata的构造函数
     * columnList: 属性的属性
     * rowList: 每个row表示一个属性的详细信息
     * @param columns 属性列表
     */
    public ExecuteResult(ArrayList<Column> columns)
    {
        this.code = Global.SUCCESS_CODE;
        this.hasResult = true;
        this.msg = Global.EXECUTE_OK;
        this.columnList = Arrays.asList(columnMetadata);
        this.rowList = new ArrayList<>();

        /* 组织metadata */
        for (Column col: columns)
        {
            List<String> row = new LinkedList<>();
            row.add(col.getName());
            row.add(col.getType().toString());
            row.add(col.isPrimary() ? "primary key" : "");
            row.add(col.isNotNull() ? "not null" : "");
            row.add(Integer.toString(col.getMaxLength()));
            rowList.add(row);
        }
    }

    /**
     * 基于show table or database的构造函数
     * @param names 所有的表或数据库的名称
     * @param column 
     */
    public ExecuteResult(ArrayList<String> names, String column)
    {
        this.code = Global.SUCCESS_CODE;
        this.hasResult = true;
        this.msg = Global.EXECUTE_OK;
        this.columnList = new LinkedList<>();
        columnList.add(column);
        this.rowList = new LinkedList<>();

        for (String name: names)
        {
            ArrayList<String> row = new ArrayList<>();
            row.add(name);
            this.rowList.add(row);
        }
    }

    /**
     * 基于select的构造函数
     * @param columns 选出来的属性
     * @param add 属性是否要展示
     * @param rows 符合条件的所有元组
     */
    public ExecuteResult(ArrayList<Column> columns, ArrayList<Boolean> add,
    ArrayList<Row> rows)
    {
        this.code = Global.SUCCESS_CODE;
        this.hasResult = true;
        this.msg = Global.EXECUTE_OK;
        this.columnList = new LinkedList<>();
        for (int i = 0; i < columns.size(); ++i)
        {
            if (add.get(i))
            {
                this.columnList.add(columns.get(i).getName());
            }
        }
        this.rowList = new LinkedList<>();
        for (Row tuple: rows) {
            List<String> list = new LinkedList<>();
            for (Entry entry : tuple.getEntries()) {
                list.add(entry.toString());
            }
            this.rowList.add(list);
        }
    }

    public int getCode()
    {
        return code;
    }

    public void setCode(int code)
    {
        this.code = code;
    }

    public String getMsg() 
    {
        return msg;
    }

    public void setMsg(String msg)
    {
        this.msg = msg;
    }

    public boolean hasResult()
    {
        return hasResult;
    }

    public void setResult(boolean hasResult)
    {
        this.hasResult = hasResult;
    }

    public List<List<String>> getRows()
    {
        return rowList;
    }

    public List<String> getColumns()
    {
        return columnList;
    }
}