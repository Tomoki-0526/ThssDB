package cn.edu.thssdb.exception;

public class NotExistException extends RuntimeException{
        /**
         * 1. drop table
         * 2. delete db
         * 3. switch db
         * @param obj
         * @param name
         */
        public NotExistException(String obj, String name) {
            super(String.format("Exception: %s %s doesn't exist!", obj, name));
        }
}