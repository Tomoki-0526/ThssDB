package cn.edu.thssdb.utils;

import java.io.File;

public class Global {
  public static int fanout = 129;

  public static int SUCCESS_CODE = 0;
  public static int FAILURE_CODE = -1;

  public static String LOGIN_OK = "Login OK!";
  public static String NEED_USERNAME = "Need username.";
  public static String NEED_LOGIN = "Login timeout or not logged.";
  public static String LOGOUT_OK = "Logout...";
  public static String EXECUTE_OK = "Execute successful!";
  public static String SYNTAX_ERROR = "SQL syntax error!";
  public static String NEED_QUERY = "Need query.";
  public static String SINGLE_SUPPORT = "Only support A single query once.";
  public static String CREATE_DB_OK = "Create database successfully!";
  public static String DROP_DB_FAIL = "No such database.";
  public static String DROP_DB_OK = "Drop database successfully!";
  public static String SWITCH_DB_OK = "Switch database successfully!";
  public static String NO_ASSIGNED_DATABASE = "Haven't assigned a database.";
  public static String CREATE_TABLE_OK = "Create table successfully!";
  public static String DROP_TABLE_OK = "Drop table successfully!";
  public static String SHOW_TABLES_OK = "Show tables successfully!";
  public static String COMMIT_OK = "Commit successfully!";
  public static String BEGIN_TRANSACTION_OK = "Begin transaction successfully!";
  public static String NO_SUCH_TABLE = "No such table.";
  public static String INSERT_OK = "Insert a row successfully!";
  public static String NO_META = "Meta not found!";
  public static String NO_FILE = "File not found!";

  public static String DEFAULT_SERVER_HOST = "127.0.0.1";
  public static int DEFAULT_SERVER_PORT = 6667;

  public static String CLI_PREFIX = "ThssDB>";
  public static final String SHOW_TIME = "show time;";
  public static final String QUIT = "quit;";

  public static final String S_URL_INTERNAL = "jdbc:default:connection";

  /* 数据长度 */
  public static int SIZE_INT = 32;
  public static int SIZE_LONG = 64;
  public static int SIZE_FLOAT = 32;
  public static int SIZE_DOUBLE = 64;
  public static int SIZE_STRING = 128;

  /* 数据根目录 */
  public static final String ROOT_DIR = System.getProperty("user.dir") + File.separator + "root" + File.separator;

  /* 各种operator */
  public static final String EQ_STR = "=";
  public static final String NE_STR = "<>";
  public static final String LE_STR = "<=";
  public static final String GE_STR = ">=";
  public static final String LT_STR = "<";
  public static final String GT_STR = ">";

  public static final String AND_STR = "&&";
  public static final String OR_STR = "||";

  public static final String ADD_STR = "+";
  public static final String MINUS_STR = "-";
  public static final String MULTIPLY_STR = "*";
  public static final String DIVIDE_STR = "/";
}
