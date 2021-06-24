package cn.edu.thssdb.query;

import cn.edu.thssdb.index.BPlusTreeIterator;
import cn.edu.thssdb.schema.Entry;
import cn.edu.thssdb.schema.Row;
import cn.edu.thssdb.schema.Table;
import cn.edu.thssdb.utils.Pair;

import java.io.IOException;
import java.util.Iterator;

public class QueryTable implements Iterator<Row> {
  private BPlusTreeIterator<Entry, Row> iterator;
  private Table table;

  public QueryTable(Table table) throws IOException
  {
    this.table = table;
    this.iterator = table.getIndexTree().iterator();
  }

  @Override
  public boolean hasNext() {
    if(!iterator.hasNext()){
      try {
        table.getNextPage();
        iterator = table.getIndexTree().iterator();
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        return false;
      }
    }
    return iterator.hasNext();
  }

  @Override
  public Row next() {
    return iterator.next().right;
  }
}