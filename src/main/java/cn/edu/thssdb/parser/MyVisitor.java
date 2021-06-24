package cn.edu.thssdb.parser;

import cn.edu.thssdb.exception.DBException;
import cn.edu.thssdb.exception.SQLSyntaxException;
import cn.edu.thssdb.schema.Column;
import cn.edu.thssdb.statement.*;
import cn.edu.thssdb.statement.database.CreateDatabaseStmt;
import cn.edu.thssdb.statement.database.DropDatabaseStmt;
import cn.edu.thssdb.statement.database.ShowDatabasesStmt;
import cn.edu.thssdb.statement.database.UseDatabaseStmt;
import cn.edu.thssdb.statement.row.DeleteStmt;
import cn.edu.thssdb.statement.row.InsertStmt;
import cn.edu.thssdb.statement.row.SelectStmt;
import cn.edu.thssdb.statement.row.UpdateStmt;
import cn.edu.thssdb.statement.session.QuitStmt;
import cn.edu.thssdb.statement.table.CreateTableStmt;
import cn.edu.thssdb.statement.table.ShowTablesStmt;
import cn.edu.thssdb.statement.transaction.CommitStmt;
import cn.edu.thssdb.statement.transaction.TransactionStmt;
import cn.edu.thssdb.type.ColumnType;
import cn.edu.thssdb.utils.Tool;
import cn.edu.thssdb.utils.Pair;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

public class MyVisitor extends SQLBaseVisitor<Object>{
    public MyVisitor() {
        super();
    }

    @Override
    public Object visitParse(SQLParser.ParseContext ctx) {
        return super.visit(ctx.sql_stmt_list());
    }

    //访问每一个 stmt
    @Override
    public Object visitSql_stmt_list(SQLParser.Sql_stmt_listContext ctx) {
        ArrayList<ExecuteStatement> res = new ArrayList<>();
        List<SQLParser.Sql_stmtContext> stmt_list = ctx.sql_stmt();
        for (SQLParser.Sql_stmtContext i : stmt_list) {
            res.add((ExecuteStatement) super.visit(i));
        }
        return res;
    }

    // 解析一个 stmt
    @Override
    public Object visitSql_stmt(SQLParser.Sql_stmtContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public Object visitCreate_db_stmt(SQLParser.Create_db_stmtContext ctx) {
        String databaseName = ctx.database_name().getText();
        return new CreateDatabaseStmt(getRawText(ctx), databaseName);
    }

    @Override
    public Object visitCreate_table_stmt(SQLParser.Create_table_stmtContext ctx) throws DBException {
        String tableName = ctx.table_name().getText();
        int columnCount = ctx.column_def().size();
        ArrayList<Column> columns = new ArrayList<>(columnCount);
        for (int i = 0; i < columnCount; ++i) {
            Column column = (Column) visit(ctx.column_def(i));
            columns.add(column);
        }

        if (ctx.table_constraint() != null) {
            for (SQLParser.Column_nameContext i : ctx.table_constraint().column_name()) {
                String columnName = i.getText();
                boolean findColumn = false;
                for (int j = 0; j < columnCount; j++) {
                    if (columns.get(j).getName().equals(columnName)) {
                        columns.get(j).setPrimary(true);
                        findColumn = true;
                        break;
                    }
                }
                if (!findColumn) {
                    throw new SQLSyntaxException("Exception: primary key :" + columnName + "doesn't exist!");
                }
            }
        }
        return new CreateTableStmt(tableName, columns,getRawText(ctx));
    }

    @Override
    public Object visitDrop_table_stmt(SQLParser.Drop_table_stmtContext ctx) {
        String tableName = ctx.table_name().getText();
        boolean askExists = notNullToken(ctx.K_EXISTS());
        return new DropDatabaseStmt(getRawText(ctx), tableName, askExists);
    }

    @Override
    public Object visitShow_table_stmt(SQLParser.Show_table_stmtContext ctx) {
        String dbName = ctx.database_name().getText();
        // TODO 参照mysql的方式，show table的调用方式是"show tableName",而非"show DBName"。
        return new ShowTablesStmt(getRawText(ctx), dbName);
    }

    @Override
    public Object visitUse_db_stmt(SQLParser.Use_db_stmtContext ctx) {
        String dbName = ctx.database_name().getText();
        return new UseDatabaseStmt(getRawText(ctx), dbName);
    }

    @Override
    public Object visitShow_db_stmt(SQLParser.Show_db_stmtContext ctx) {
        return new ShowDatabasesStmt(getRawText(ctx));
    }

    @Override
    public Object visitDelete_stmt(SQLParser.Delete_stmtContext ctx) {
        String tableName = ctx.table_name().getText();
        Condition condition = null;
        if (notNullToken(ctx.multiple_condition()))
            condition = (Condition) visit(ctx.multiple_condition());
        return new DeleteStmt(getRawText(ctx), tableName, condition);
    }

    @Override
    public Object visitQuit_stmt(SQLParser.Quit_stmtContext ctx) {
        return new QuitStmt();
    }

    @Override
    public Object visitShow_meta_stmt(SQLParser.Show_meta_stmtContext ctx) {
        String tableName = ctx.table_name().getText();
        return new ShowDatabasesStmt(tableName);
    }

    // 返回一个 insert 对象
    @Override
    public Object visitInsert_stmt(SQLParser.Insert_stmtContext ctx) {
        // 获得表名
        String tableName = ctx.table_name().getText();

        // 获得插入的 columns 名称
        int columnCount = ctx.column_name().size();
        ArrayList<String> columnNames = new ArrayList<>(columnCount);
        for (int i = 0; i < columnCount; ++i) {
            columnNames.add(ctx.column_name(i).getText());
        }

        // 获得插入的多条记录
        int rowCount = ctx.value_entry().size();
        ArrayList<ArrayList<Comparable>> values = new ArrayList<>(rowCount);
        for (int i = 0; i < rowCount; ++i) {
            ArrayList<Comparable> value = (ArrayList<Comparable>) visit(ctx.value_entry(i));
            values.add(value);
        }
        return new InsertStmt(tableName, columnNames, values, getRawText(ctx));
    }

//    @Override
//    public Object visitBegin_trans_stmt(SQLParser.Begin_trans_stmtContext ctx) {
//        return new TransactionStmt(getRawText(ctx));
//    }
//
//    @Override
//    public Object visitCommit_stmt(SQLParser.Commit_stmtContext ctx) {
//        return new CommitStmt(getRawText(ctx));
//    }

    // 返回一个 comparable list 表示的是插入的一行数据
    @Override
    public Object visitValue_entry(SQLParser.Value_entryContext ctx) {
        int valueCount = ctx.literal_value().size();
        ArrayList<Comparable> values = new ArrayList<>(valueCount);
        for (SQLParser.Literal_valueContext i : ctx.literal_value()) {
            Comparable value = ((Pair<ColumnType, Comparable>) visit(i)).right;
            values.add(value);
        }
        return values;
    }

    @Override
    public Object visitSelect_stmt(SQLParser.Select_stmtContext ctx) {
        boolean requireDistinct = ctx.K_DISTINCT() != null;
        boolean requireAll = ctx.K_ALL() != null;
        ArrayList<ResultColumn> resultColumns = new ArrayList<>();
        for (SQLParser.Result_columnContext i : ctx.result_column()) {
            ResultColumn resultColumn = (ResultColumn) visit(i);
            resultColumns.add(resultColumn);
        }
        ArrayList<TableQuery> tableQueries = new ArrayList<>();
        for (SQLParser.Table_queryContext i : ctx.table_query()) {
            TableQuery tableQuery = (TableQuery) visit(i);
            tableQueries.add(tableQuery);
        }
        // TODO table query 的解析，只有一个on
        Condition condition = null;
        if (notNullToken(ctx.multiple_condition())) {
            condition = (Condition) visit(ctx.multiple_condition());
        }
        return new SelectStmt(getRawText(ctx), resultColumns, tableQueries, condition, requireDistinct, requireAll);
    }

    @Override
    public Object visitUpdate_stmt(SQLParser.Update_stmtContext ctx) {
        String tableName = ctx.table_name().getText();
        String columnName = ctx.column_name().getText();
        Expression expression = (Expression) visit(ctx.expression());
        Condition condition = null;
        if (notNullToken(ctx.multiple_condition())) {
            condition = (Condition) visit(ctx.multiple_condition());
        }
        return new UpdateStmt(getRawText(ctx), tableName, columnName, expression, condition);
    }

    @Override
    public Column visitColumn_def(SQLParser.Column_defContext ctx) throws DBException {
        // Over
        String columnName = ctx.column_name().getText();
        ColumnType columnType = Tool.stringToColumnType(ctx.type_name().getText().toLowerCase());

        // string 需要指定长度
        int maxLength = 0;
        if (columnType == ColumnType.STRING) {
            String num = ctx.type_name().NUMERIC_LITERAL().getText();
            try {
                maxLength = Integer.parseInt(num);
            } catch (Exception e) {
                throw new SQLSyntaxException("Exception: string filed length should be an integer!");
            }
        }

        // 解析 约束
        boolean notNull = false;
        boolean primaryKey = false;

        if (ctx.column_constraint().size() != 0) {
            for (SQLParser.Column_constraintContext i : ctx.column_constraint()) {
                if (i.K_PRIMARY() != null) primaryKey = true;
                else if (i.K_NOT() != null && i.K_NULL() != null) notNull = true;
            }
        }

        return new Column(columnName, columnType, primaryKey, notNull, maxLength);
    }

    // 返回一个 condition，返回一个Condition的链表
    @Override
    public Object visitMultiple_condition(SQLParser.Multiple_conditionContext ctx) {
        List<SQLParser.Multiple_conditionContext> list = ctx.multiple_condition();
        TerminalNode node = ctx.AND();
        if (notNullToken(ctx.multiple_condition()) && ctx.multiple_condition().size() != 0) {
            Condition leftNode = (Condition) visit(ctx.multiple_condition(0));
            Condition rightNode = (Condition) visit(ctx.multiple_condition(1));
            String logicOp = ctx.getChild(1).getText();
            return new Condition(leftNode, rightNode, logicOp);
        } else {
            return visit(ctx.condition());
        }
    }

    // 返回一个 condition
    @Override
    public Object visitCondition(SQLParser.ConditionContext ctx) {
        Expression leftExpr = (Expression) visit(ctx.expression(0));
        Expression rightExpr = (Expression) visit(ctx.expression(1));
        String operation = ctx.getChild(1).getText();
        return new Condition(leftExpr, rightExpr, operation);
    }

    // 返回一个 叶子 expression
    @Override
    public Object visitComparer(SQLParser.ComparerContext ctx) {
        // Over
        if (notNullToken(ctx.column_full_name())) {
            // 有表名 和 列名
            if (notNullToken(ctx.column_full_name().table_name())) {
                String tableName = ctx.column_full_name().table_name().getText();
                String columnName = ctx.column_full_name().column_name().getText();
                return new Expression(tableName, columnName);
            } else {
                // 只有列名
                return new Expression(ctx.getText());
            }
        } else {
            // 常量值
            Pair<ColumnType, Comparable> valuePair = (Pair<ColumnType, Comparable>) visit(ctx.literal_value());
            return new Expression(valuePair);
        }
    }

    // 返回一个 expression
    @Override
    public Object visitExpression(SQLParser.ExpressionContext ctx) {
        // 是最终节点
        if (notNullToken(ctx.comparer())) {
            return visit(ctx.comparer());
        } else if (ctx.expression().size() == 1) {
            // 只有一个孩子
            return visit(ctx.expression(0));
        } else {
            // 有两个 expression 节点 和 一个操作符
            Expression leftNode = (Expression) visit(ctx.expression(0));
            Expression rightNode = (Expression) visit(ctx.expression(1));
            String operation = ctx.getChild(1).getText();
            return new Expression(leftNode, rightNode, operation);
        }
    }

    @Override
    public Object visitResult_column(SQLParser.Result_columnContext ctx) {
        // Over
        SQLParser.Table_nameContext table_nameContext = ctx.table_name();
        Object mul = ctx.MUL();
        SQLParser.Column_full_nameContext column_full_nameContext = ctx.column_full_name();
        if (notNullToken(column_full_nameContext)) {
            // tableName.columnName的模式
            String tableName = column_full_nameContext.table_name().getText();
            String columnName = column_full_nameContext.column_name().getText();
            return new ResultColumn(tableName, columnName);
        } else if (notNullToken(mul)) {
            if (notNullToken(table_nameContext)) {
                // tableName.*的模式
                String tableName = table_nameContext.getText();
                return new ResultColumn(tableName, true);
            } else {
                // * 的模式
                return new ResultColumn(true);
            }
        }
        return null;
    }

    @Override
    public Object visitTable_query(SQLParser.Table_queryContext ctx) {
        //Over
        // 一个table_query包含tableName外加可能的join tableName on condition
        // TODO 目前只支持使用join来使用两个table的连接，也就是说，有n个join，就有n+1个tableName
        ArrayList<String> tableNames = new ArrayList<>();
        for (SQLParser.Table_nameContext i : ctx.table_name()) {
            tableNames.add(i.getText());
        }
        // 考虑condition
        Condition condition = null;
        if (notNullToken(ctx.K_ON())) {
            condition = (Condition) visit(ctx.multiple_condition());
        }
        return new TableQuery(tableNames, condition);
    }

    // 得到 literal_value， 四种可能 null, string, long, double
    // 返回的是一个 Pair< ColumnType, comparable>
    @Override
    public Object visitLiteral_value(SQLParser.Literal_valueContext ctx) {

        Comparable value = null;
        ColumnType valueType = ColumnType.INT;
        if (ctx.STRING_LITERAL() != null) {
            String rawString = ctx.getText();
            value = rawString.substring(1, rawString.length() - 1);
            valueType = ColumnType.STRING;
        } else if (ctx.NUMERIC_LITERAL() != null) {
            String text = ctx.getText();
            if (text.contains(".")) {
                value = Double.valueOf(text);
                valueType = ColumnType.DOUBLE;
            } else {
                value = Long.valueOf(text);
                valueType = ColumnType.LONG;
            }
        }
        return new Pair<>(valueType, value);
    }


    private boolean notNullToken(Object object) {
        return object != null;
    }

    private String getRawText(ParserRuleContext ctx) {
        return ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
    }

}
