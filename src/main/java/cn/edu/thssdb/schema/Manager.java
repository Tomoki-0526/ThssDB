package cn.edu.thssdb.schema;

import cn.edu.thssdb.transaction.Session;
import cn.edu.thssdb.utils.Global;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Manager {
	// metadata路径
	private String metadataPath = Global.ROOT_DIR + "metadata.db";

	// sessionId生成器
	private Random sessionIdGenerator = new Random();
	// sessionId和session的映射
	private Map<Long, Session> sessionMap = new ConcurrentHashMap<>();
	// 每个manager都有一个系统用户
	private Session sysSession;

	// 所有的数据库名称
	private ArrayList<String> databaseNames = new ArrayList<>();
	// 对databaseNames（上面这个列表）进行操作时的锁
	private static ReentrantReadWriteLock databasesLock = new ReentrantReadWriteLock();
	// 数据库名称到数据库的映射
	private HashMap<String, Database> databases;
	// manager锁
	private static ReentrantReadWriteLock managerLock = new ReentrantReadWriteLock();
	// 是否需要写
	private boolean needWrite = false;

  	public static Manager getInstance() {
    return Manager.ManagerHolder.INSTANCE;
  }

  	public Manager() {
		File rootDir = new File(Global.ROOT_DIR);
		if (!rootDir.exists())
			rootDir.mkdirs();
		loadMeta();
  	}

	/**
	 * 从metadata.db中读取数据
	 */
	private void loadMeta() {
  		// TODO
//		ArrayList<String> names = IOManager.loadMeta(metadataPath);
		ArrayList<String> names = null;
		if(names != null){
			databaseNames.addAll(names);
		}
	}

	/**
	 * 向metadata.db中写入数据
	 */
	private void saveMeta() {
		// TODO
		if(needWrite){

			needWrite = false;
		}
	}

  	public void createDatabaseIfNotExists(String databaseName) {
    	/* 检查数据库是否已存在 */
		if (databaseNames.contains(databaseName)) {
			try {
				throw new Exception(databaseName + "already exists.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Database database = new Database(databaseName);
		databaseNames.add(databaseName);
		databases.put(databaseName, database);
		needWrite = true;

		saveMeta();
	}

  	public void deleteDatabase(Session session, String databaseName) {
  		/* 检查数据库是否存在 */
		if (!databaseNames.contains(databaseName)) {
			try {
				throw new Exception(databaseName + "doesn't exists.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/* 获得待删除的数据库 */
		databasesLock.readLock().lock();
		Database database = databases.get(databaseName);
		databasesLock.readLock().unlock();
		if (database == null)	// 如果待删除的数据库不存在，则新建？？？难道不是应该直接结束吗
			database = new Database(databaseName);

		/* 获得database的写锁 */
		boolean writeLock = database.getWriteLock();
		if (!writeLock) {
			try {
				throw new Exception(databaseName + "is being used!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		database.clear();
		databaseNames.remove(databaseName);

		/* 检查待删除数据库是否是当前用户正在使用的数据库 */
		Database userDatabase = session.getDatabase();
		if (userDatabase != null && databaseName.equals(userDatabase.getName())) {
			session.setDatabase(null);
		}

		/* 删掉数据库 */
		databasesLock.writeLock().lock();
		databases.remove(databaseName);
		databasesLock.writeLock().unlock();

		database.releaseDBWriteLock();
		needWrite = true;
		saveMeta();
  	}

  	public void switchDatabase(Session session, String targetDatabaseName) {
		/* 检查数据库是否存在 */
		if (!databaseNames.contains(targetDatabaseName)) {
			try {
				throw new Exception(targetDatabaseName + "doesn't exists.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/* 获取当前用户正在使用的数据库 */
		Database curDatabase = session.getDatabase();
		if (curDatabase != null && curDatabase.getName().equals(targetDatabaseName))
			return;

		/* 获取目标数据库 */
		databasesLock.readLock().lock();
		Database targetDatabase = databases.get(targetDatabaseName);
		databasesLock.readLock().unlock();
		if (targetDatabase == null) {	// 如果目标数据库不存在，则新建
			targetDatabase = new Database(targetDatabaseName);
			databasesLock.writeLock().lock();
			databases.put(targetDatabaseName, targetDatabase);
			databasesLock.writeLock().unlock();
		}

//		/* 释放掉原数据库的读锁 */
//		if (curDatabase != null) {
//			while (curDatabase.releaseDBReadLock());
//		}
//
//		/* 对目标数据库上读锁 */
//		targetDatabase.acquireDBReadLock();
//		session.setDatabase(targetDatabase);
  	}

	/** 用户登录时获得session */
	public synchronized Session getConnectSession() 
	{
		while (true) 
		{
			long sessionId = sessionIdGenerator.nextLong();
			if (!sessionMap.containsKey(sessionId)) 
			{
				Session session = new Session(sessionId);
				sessionMap.put(sessionId, session);
				return session;
			}
		}
	}

	/** 判断用户当前是否已经登录 */
	public boolean isConnected(long sessionId)
	{
		return sessionMap.containsKey(sessionId);
	}

	/** 根据sessionId获取session */
	public Session getSession(long sessionId)
	{
		return sessionMap.get(sessionId);
	}

	/** 用户登出时清除session */
	public void removeSession(long sessionId)
	{
		Session session = sessionMap.get(sessionId);
		if (session != null) 
		{
			sessionMap.remove(sessionId);
			/* 如果在事务中，需要释放锁 */
			if (session.isInTransaction())
			{
				/* TODO: 写日志 */
				/* 清除操作序列 */
				session.emptyOperations();
				/* 释放锁 */
				session.releaseLockStmt();
			}
			/* 释放数据库 */
			session.releaseDatabase();
			session = null;
		}
	}

  	private static class ManagerHolder {
    	private static final Manager INSTANCE = new Manager();
    	private ManagerHolder() {

    	}
  	}

	/**
	 * 申请写锁
	 * @return
	 */
	public boolean managerWriteLock() {
		/* 释放读锁 */
		while (managerLock.getReadLockCount() > 0)
			managerLock.readLock().unlock();
		/* 上写锁 */
		managerLock.writeLock().lock();
		return true;
	}

	/**
	 * 释放写锁
	 * @return
	 */
	public boolean managerWriteUnlock() {
		if (managerLock.getWriteHoldCount() > 0) {
			managerLock.writeLock().unlock();
			return true;
		}
		return false;
	}

	/**
	 * 申请读锁
	 * @return
	 */
	public boolean managerReadLock() {
		/* 检查有没有上写锁或读锁 */
		if (managerLock.getWriteHoldCount() > 0 || managerLock.getReadLockCount() > 0)
			return false;
		managerLock.readLock().lock();
		return true;
	}

	/**
	 * 释放读锁
	 * @return
	 */
	public boolean managerReadUnlock() {
		if (managerLock.getReadLockCount() > 0) {
			managerLock.readLock().unlock();
			return true;
		}
		return false;
	}

	public boolean hasDatabase(String databaseName) {
		return databaseNames.contains(databaseName);
	}

	public ArrayList<String> getDatabaseNames() {
		return new ArrayList<>(databaseNames);
	}
}
