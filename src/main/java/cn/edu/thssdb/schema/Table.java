package cn.edu.thssdb.schema;

import cn.edu.thssdb.index.BPlusTree;
import cn.edu.thssdb.io.IOUtils;
import cn.edu.thssdb.utils.Global;
import cn.edu.thssdb.utils.Pair;
import cn.edu.thssdb.utils.Tool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Table implements Iterable<Row> {
  ReentrantReadWriteLock lock;
  private String databaseName;
  public String tableName;
  public ArrayList<Column> columns;
  public BPlusTree<Entry, Row> index;
  private int primaryIndex;
  private String fileName;
  public boolean needWrite;
  private ArrayList<Entry> deleteList;
  private ArrayList<Entry> updateList;
  private ArrayList<Entry> insertList;
  private int pageNumber;
  private ArrayList<Row> rows;
  private int pagePointer;


  public Table(String databaseName, String tableName, Column[] columns) throws IOException {
    // TODO
    this.databaseName = databaseName;
    this.tableName = tableName;
    this.fileName = Global.ROOT_DIR + this.databaseName + File.separator + this.tableName;
    this.index = new BPlusTree<>();
    needWrite = true;
    this.deleteList = new ArrayList<>();
    this.updateList = new ArrayList<>();
    this.insertList = new ArrayList<>();
    this.pageNumber = 0;
    this.pagePointer = -1;
    this.rows = new ArrayList<>();
    this.primaryIndex = Tool.getPrimaryIndex(columns);
    this.columns = new ArrayList<>(Arrays.asList(columns));
    File metaFile = new File(this.fileName + ".meta");
    File dataFile = new File(this.fileName + ".data");
    if (metaFile.exists()) metaFile.delete();
    if (dataFile.exists()) dataFile.delete();
    metaFile.createNewFile();
    dataFile.createNewFile();
    this.saveMeta();
  }

  public Table(String dbName, String tableName) throws IOException {
    this.databaseName = dbName;
    this.tableName = tableName;
    this.fileName = Global.ROOT_DIR + this.databaseName + File.separator + this.tableName;
    this.index = new BPlusTree<>();
    needWrite = true;
    this.deleteList = new ArrayList<>();
    this.updateList = new ArrayList<>();
    this.insertList = new ArrayList<>();
    this.pageNumber = 0;
    this.pagePointer = -1;
    this.rows = new ArrayList<>();
    File metaFile = new File(this.fileName + ".meta");
    File dataFile = new File(this.fileName + ".data");
    if(!metaFile.exists() || !dataFile.exists()){

    }
    this.loadMeta();

  }

  private void loadMeta() throws IOException {
    if(this.columns != null){
      this.columns.clear();
    }
    else{
      this.columns = new ArrayList<>();
    }
    ArrayList<Column> list = IOUtils.loadTableMeta(this.fileName + ".meta");
    columns.addAll(list);
    for(int i=0;i<columns.size();i++){
      if (columns.get(i).isPrimary()) {
        primaryIndex = i;
        break;
      }
    }
  }

  private void saveMeta() throws IOException {
    IOUtils.saveTableMeta(columns, this.fileName + ".meta");
  }

  private void recover() throws IOException {
    ArrayList<ArrayList> list = IOUtils.loadTableData(columns,this.fileName + ".data");
    ArrayList<Row> rows = list.get(0);
    ArrayList<Long> pointers = list.get(1);
    ArrayList<Integer> info = list.get(2);
    pageNumber = info.get(0) - 4;
    for(int i=0;i<rows.size();i++){
      this.index.put(rows.get(i).getEntries().get(primaryIndex),rows.get(i));
    }
  }

  public void insert(Row row) {
    // TODO
    Entry entry = row.getEntries().get(primaryIndex);
    try{
      index.put(entry,row);
    }catch(DuplicateException e){

    }
    insertList.add(entry);
    needWrite = true;
  }

  public void delete(Row row) {
    // TODO
    Entry entry = row.getEntries().get(primaryIndex);
    boolean found = false;
    for (int i = 0; i < insertList.size(); i++) {
      if (insertList.get(i).equals(entry)) {
        insertList.remove(i);
        found = true;
        break;
      }
    }
    index.remove(entry);
    if(!found)
    {
      deleteList.add(entry);
    }
    for (int i = 0; i < updateList.size(); i++) {
      if (updateList.get(i).equals(entry)) {
        updateList.remove(i);
        break;
      }
    }
    needWrite = true;
  }

  public void update(Row row) {
    // TODO
    boolean found = false;
    Entry entry = row.getEntries().get(primaryIndex);
    for (int i = 0; i < updateList.size(); i++) {
      if (updateList.get(i).equals(entry)) {
        found = true;
        break;
      }
    }
    for (int i = 0; i < insertList.size(); i++) {
      if (insertList.get(i).equals(entry)) {
        found = true;
        break;
      }
    }
    if (found) {
      updateList.add(entry);
    }
    index.update(entry, row);
    needWrite = true;
  }

  public void getNextPage() throws IOException{
    pagePointer = IOUtils.nextPage(columns, fileName + ".data", pageNumber, rows, pagePointer);
  }

  public void writeToDisk() throws IOException {
    IOUtils.deleteRow(fileName + ".data", updateList, columns, pageNum - 4, primaryIndex);
    IOUtils.insertRow(fileName + ".data", updateList, columns, this, indexTree);
    IOUtils.insertRow(fileName + ".data", insertList, columns, this, indexTree);
    IOUtils.deleteRow(fileName + ".data", deleteList, columns, pageNum - 4, primaryIndex);
    insertList.clear();
    deleteList.clear();
    updateList.clear();
  }

}
