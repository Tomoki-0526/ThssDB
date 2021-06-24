package cn.edu.thssdb.schema;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringJoiner;

public class Row implements Serializable {
  private static final long serialVersionUID = -5809782578272943999L;
  protected ArrayList<Entry> entries;

  public Row() {
    this.entries = new ArrayList<>();
  }

  public Row(Entry[] entries) {
    this.entries = new ArrayList<>(Arrays.asList(entries));
  }

  public ArrayList<Entry> getEntries() {
    return entries;
  }

  public void appendEntries(ArrayList<Entry> entries) {
    this.entries.addAll(entries);
  }

  public String toString() {
    if (entries == null)
      return "EMPTY";
    StringJoiner sj = new StringJoiner(", ");
    for (Entry e : entries)
      sj.add(e.toString());
    return sj.toString();
  }

  /**
   * 检查元组和属性表是否匹配
   * @param columns
   * @return
   * @throws Exception
   */
  public boolean rowColumnsMatching(ArrayList<Column> columns) throws Exception
  {
    /* 检查元组和属性表长度 */
    if (entries.size() != columns.size())
      return false;
    
    for (int i = 0; i < entries.size(); ++i)
    {
      /* 对非空值依次进行检查 */
      if (entries.get(i) != null)
      {
        entries.get(i).entryColumnMatching(columns.get(i));
      }
    }

    return true;
  }
}
