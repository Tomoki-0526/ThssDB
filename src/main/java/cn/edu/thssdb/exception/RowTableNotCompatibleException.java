package cn.edu.thssdb.exception;

public class RowTableNotCompatibleException extends DBException{
    /**
     * 计算condition时，传入的columns和row的长度不匹配
     * @return
     */
    @Override
    public String getMessage(){return "Row and table not compatible!";}
}

