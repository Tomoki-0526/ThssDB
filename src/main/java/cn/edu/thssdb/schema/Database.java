package cn.edu.thssdb.schema;

import cn.edu.thssdb.io.IOUtils;
import cn.edu.thssdb.query.QueryResult;
import cn.edu.thssdb.query.QueryTable;
import cn.edu.thssdb.utils.Global;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Database {

  private String name;
  private String dirName;
  private String metaFilePath;
  private boolean needWrite;
  private HashMap<String, Table> tables;
  ReentrantReadWriteLock lock;


  public Database(String name) throws IOException {
    this.name = name;
    this.tables = new HashMap<>();
    this.lock = new ReentrantReadWriteLock();
    this.dirName = Global.ROOT_DIR + name + File.separator;
    this.needWrite = false;

    File file = new File(this.dirName);
    if (!file.exists()){
      file.mkdirs();
    }
    else{
      loadMeta();
    }
  }

  private void loadMeta() throws IOException {
    ArrayList<String> tableNames = IOUtils.loadMeta(metaFilePath);
    if(tableNames != null && tableNames.size() !=0){
      for (int i=0;i<tableNames.size();i++){
        tables.put(name,new Table(this.name,name));
      }
    }
  }

  private void persist() {
    // TODO
//    try {
//      lock.writeLock().lock();
//      if (this.tables == null){
//
//      }
////      File dir = new File(Global.DATABASE_DIR+ File.separator+name);
//      File dir = null;
//      if (!dir.exists() && !dir.mkdirs()){
//
//      }
//      ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dir.toString()+File.separator+"TABLES_NAME"));
//      for (String tableName: tables.keySet()){
//        oos.writeObject(tableName);
//        ObjectOutputStream oosSchema = new ObjectOutputStream(new FileOutputStream(dir.toString()+File.separator+tableName+"_SCHEMA"));
//        for (Column c: tables.get(tableName).columns) {
//          oosSchema.writeObject(c.toString());
//        }
//        oosSchema.close();
//        Table table = tables.get(tableName);
////        if (table == null) {
////          System.err.println("Table is null in index while trying to persist database.");
////        }
////        else {
////          if (!table.persist()) {
////          }
////        }
//        oos.close();
//      }
//    } catch (FileNotFoundException e) {
//      e.printStackTrace();
//    } catch (IOException e) {
//      e.printStackTrace();
//    } finally{
//      lock.writeLock().unlock();
//    }
  }

  public void create(String name, Column[] columns) {
    // TODO
    try {
      lock.writeLock().lock();
//      if (checkTableExist(tableName)) {
//        throw new TableAlreadyExistException();
//      }
      Table table = new Table(this.name, name, columns);
      tables.put(name, table);
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  public void drop(String name) {
    // TODO
    try {
      lock.writeLock().lock();
//      if (tables.get(name) == null) {
//        throw new TableNotExistException();
//      }
      tables.remove(name);
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  public String select(QueryTable[] queryTables) {
    // TODO
    QueryResult queryResult = new QueryResult(queryTables);
    return null;
  }

  private void recover() {
    // TODO
  }

  public void quit() {
    // TODO
  }

  /**
   * @描述 清除掉这个数据库的所有表、metadata和目录
   * 我暂时不知道和drop有啥区别
   */
  public void clear() {
    // TODO
  }

  /**
   * 申请读锁
   * @return
   */
  public boolean databaseReadLock() {
    /* 如果有锁，则返回 */
    if (lock.getWriteHoldCount() > 0 || lock.getReadHoldCount() > 0)
      return false;
    lock.readLock().lock();
    return true;
  }

  /**
   * 释放读锁
   * @return
   */
  public boolean databaseReadUnlock() {
    if (lock.getReadHoldCount() > 0) {
      lock.readLock().unlock();
      return true;
    }
    return false;
  }

  /**
   * 申请写锁
   * @return
   */
  public boolean databaseWriteLock() {
    /* 释放所有的读锁 */
    while (lock.getReadHoldCount() > 0)
      lock.readLock().unlock();
    lock.writeLock().lock();
    return true;
  }

  /**
   * 释放写锁
   * @return
   */
  public boolean databaseWriteUnlock() {
    if (lock.getWriteHoldCount() > 0) {
      lock.writeLock().unlock();
      return true;
    }
    return false;
  }

  /** 尝试获得数据库的写锁 */
  public boolean getWriteLock() {
    return lock.writeLock().tryLock();
  }

  public String getName() {
    return name;
  }

  public ArrayList<String> getTableNames() {
    return new ArrayList<>(this.tables.keySet());
  }

  public Table getTableByTableName(String tableName) {
    return tables.get(tableName);
  }

  public void releaseDBWriteLock() {
  }

  public boolean releaseDBReadLock() {
    return false;
  }

  public void degradeToReadLock() {
  }
}
