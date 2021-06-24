package cn.edu.thssdb.exception;

public class DBException extends RuntimeException {

    public DBException(){ }

    public DBException(String msg){
        super(msg);
    }

}
