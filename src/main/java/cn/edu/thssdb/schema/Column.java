package cn.edu.thssdb.schema;

import java.io.Serializable;

import cn.edu.thssdb.type.ColumnType;
import cn.edu.thssdb.utils.Global;

public class Column implements Comparable<Column>, Serializable {

  private static final long serialVersionUID = -5809782578272943999L;

  private String name;
  private ColumnType type;
  private boolean primary;
  private boolean notNull;
  private int maxLength;

  public Column(String name, ColumnType type, boolean primary, boolean notNull, int maxLength) {
    this.name = name;
    this.type = type;
    this.primary = primary;
    this.notNull = notNull;

    if (type == ColumnType.INT)
      this.maxLength = Global.SIZE_INT;
    else if(type == ColumnType.LONG)
      this.maxLength = Global.SIZE_LONG;
    else if(type == ColumnType.FLOAT)
      this.maxLength = Global.SIZE_FLOAT;
    else if(type == ColumnType.DOUBLE)
      this.maxLength = Global.SIZE_DOUBLE;
    else
      this.maxLength = maxLength;
  }

  public Column(Column col)
  {
    this.name = col.getName();
    this.type = col.getType();
    this.primary = col.isPrimary();
    this.notNull = col.isNotNull();
    this.maxLength = col.getMaxLength();
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name) 
  {
    this.name = name;
  }

  public ColumnType getType()
  {
    return type;
  }

  public void setType(ColumnType type)
  {
    this.type = type;
  }

  public boolean isPrimary()
  {
    return primary;
  }

  public void setPrimary(boolean primary)
  {
    this.primary = primary;
  }

  public boolean isNotNull()
  {
    return notNull;
  }

  public void setNotNull(boolean notNull)
  {
    this.notNull = notNull;
  }

  public int getMaxLength() 
  {
    return this.maxLength;
  }

  public void setMaxLength(int maxLength)
  {
    this.maxLength = maxLength;
  }

  @Override
  public int compareTo(Column e) {
    return name.compareTo(e.name);
  }

  public String toString() {
    return name + ',' + type + ',' + primary + ',' + notNull + ',' + maxLength;
  }
}
