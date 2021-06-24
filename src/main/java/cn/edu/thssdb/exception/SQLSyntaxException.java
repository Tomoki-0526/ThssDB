package cn.edu.thssdb.exception;

public class SQLSyntaxException extends DBException{
    /**
     * 1. string指定长度为浮点数
     * 2. 指定的主键列名不存在
     * @param err
     */
    public SQLSyntaxException(String err){
        super(err);
    }
}
