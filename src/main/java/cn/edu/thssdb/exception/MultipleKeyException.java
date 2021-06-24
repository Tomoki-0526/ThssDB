package cn.edu.thssdb.exception;

public class MultipleKeyException extends DBException {
    /**
     * 仅在创建table时使用
     * @return
     */
    @Override
    public String getMessage(){
        return "Exception: only support one primary index!";
    }
}
