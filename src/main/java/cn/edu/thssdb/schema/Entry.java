package cn.edu.thssdb.schema;

import java.io.Serializable;

import cn.edu.thssdb.type.ColumnType;

public class Entry implements Comparable<Entry>, Serializable {
  private static final long serialVersionUID = -5809782578272943999L;
  public Comparable value;

  public Entry(Comparable value) {
    this.value = value;
  }

  @Override
  public int compareTo(Entry e) {
    return value.compareTo(e.value);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (this.getClass() != obj.getClass())
      return false;
    Entry e = (Entry) obj;
    return value.equals(e.value);
  }

  public String toString() {
    return value.toString();
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  /**
   * 返回该值对应的属性
   * @return
   */
  public ColumnType getType()
  {
    if (value instanceof Integer)
      return ColumnType.INT;
    else if (value instanceof Long)
      return ColumnType.LONG;
    else if (value instanceof Float)
      return ColumnType.FLOAT;
    else if (value instanceof Double)
      return ColumnType.DOUBLE;
    else
      return ColumnType.STRING;
  }

  /**
   * 判断一个entry和其所在的column是否匹配
   * @param col
   * @return
   */
  public boolean entryColumnMatching(Column col) throws Exception
  {
    /* 检查非空性 */
    if (value == null)
    {
      if (col.isNotNull())
        throw new Exception(col.getName() + " CANNOT be null.");
      return true;
    }

    /* 检查类型 */
    if (col.getType() != getType())
      throw new Exception("Type not match.");

    /* 检查长度 */
    if (((String)value).length() > col.getMaxLength())
    {
      throw new Exception(col.getName() + "CANNOT be over " + col.getMaxLength());
    }

    return true;
  }

  public Comparable getValue() {
    return this.value;
  }

  public void setValue(Comparable value) {
    this.value = value;
  }

}
