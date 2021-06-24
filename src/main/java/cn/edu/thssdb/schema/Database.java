package cn.edu.thssdb.schema;

import cn.edu.thssdb.query.QueryResult;
import cn.edu.thssdb.query.QueryTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Database {

  private String name;
  private HashMap<String, Table> tables;
  ReentrantReadWriteLock lock;

  public Database(String name) {
    this.name = name;
    this.tables = new HashMap<>();
    this.lock = new ReentrantReadWriteLock();
    recover();
  }

  private void persist() {
    // TODO
  }

  public void create(String name, Column[] columns) {
    // TODO
  }

  public void drop(String name) {
    // TODO
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
}
