package cn.edu.thssdb.io;

import cn.edu.thssdb.index.BPlusTree;
import cn.edu.thssdb.index.BPlusTreeIterator;
import cn.edu.thssdb.schema.Column;
import cn.edu.thssdb.schema.Entry;
import cn.edu.thssdb.schema.Row;
import cn.edu.thssdb.schema.Table;
import cn.edu.thssdb.type.ColumnType;
import cn.edu.thssdb.utils.Global;

import java.io.*;
import java.util.ArrayList;

/**
 * @描述 IO工具类
 */
public class IOUtils {
    // 可以访问文件的任何一个位置
    private RandomAccessFile randomAccessFile;
    // 页面大小
    private static final int pageSize = 4096;
    // 前四页是信息页
    private static final int infoPageNum = 4;
    // 数据页页头长度
    private static final int headerLength = Global.SIZE_INT;
    // 索引数量
    private int indexNum;
    // 最大页数
    private int maxPageNum;
    // 待存储的row
    private Row row;
    // row的长度
    private int rowLength;

    private static int getPosition(int page, int off) {
        return page * pageSize + off;
    }

    /**
     * 为长度为len的记录分配空闲页面
     * @param len
     * @param accessFile
     * @return 返回新插入的数据的位置
     * @throws IOException
     */
    private static int allocatePage(int len, RandomAccessFile accessFile) throws IOException {
        int page = 0;
        /* 在已有页面中查找 */
        for (page = infoPageNum; page * pageSize < accessFile.length(); ++page) {
            // 找到一个页面
            accessFile.seek(page * pageSize);
            // 判断是否被占用
            int inUse = IOBytes.readInteger(accessFile, false);
            if (inUse == -1) {
                // 已被占用
                int usedLength = accessFile.readInt();
                page += (usedLength - 1);
            }
            else {
                // 判断该页面是否有足够的空闲区域
                if (len + inUse <= pageSize) {
                    accessFile.seek(page * pageSize);
                    IOBytes.writeInteger(accessFile, inUse + len, false);
                    return getPosition(page, inUse);
                }
            }
        }

        /* 在已有页面中没找到合适的位置 */
        if (len + headerLength > pageSize) {
            // 数据长度大于页面的最大可使用空间
            accessFile.seek(page * pageSize);
            accessFile.write(-1);
            int rest = len - (pageSize - headerLength);
            int num = rest / pageSize;
            if (rest % pageSize == 0)
                num += 1;
            else
                num += 2;
            accessFile.write(num);
            return getPosition(page, headerLength);
        }
        else {
            // 数据能被一个空页面装下，创建一个页面
            accessFile.seek(page * pageSize);
            IOBytes.writeInteger(accessFile, len + headerLength, false);
            return getPosition(page, headerLength);
        }
    }

    public static IOUtils getInstance() {
        return new IOUtils();
    }

    public IOUtils() {}

    public IOUtils(String path) throws IOException {
        this.indexNum = 0;
        this.rowLength = 0;
        this.maxPageNum = 4;
        this.randomAccessFile = new RandomAccessFile(path + ".data", "rw");
    }

    /**
     * 获取一行的长度
     * @param columnList
     * @return
     */
    public static int getLength(ArrayList<Column> columnList) {
        int len = 0;
        for(Column column: columnList) {
            ColumnType type = column.getType();
            if (type == ColumnType.INT)
                len += Global.SIZE_INT;
            else if (type == ColumnType.LONG)
                len += Global.SIZE_LONG;
            else if (type == ColumnType.FLOAT)
                len += Global.SIZE_FLOAT;
            else if (type == ColumnType.DOUBLE)
                len += Global.SIZE_DOUBLE;
            else if (type == ColumnType.STRING)
                len += Global.SIZE_STRING;
        }
        return len;
    }

    /**
     * 读取一行
     * @param pos
     * @param len length of row
     * @param file
     * @param columnList
     * @return
     * @throws IOException
     */
    public static Row readRow(int pos, int len, RandomAccessFile file, ArrayList<Column> columnList) throws IOException {
        file.seek(pos);

        ArrayList<Entry> entryArrayList = new ArrayList<>();
        /* 判断是否小于页面的最大可使用空间 */
        if (len + headerLength <= pageSize) {
            for (Column column: columnList) {
                ColumnType type = column.getType();
                if (type == ColumnType.INT)
                    entryArrayList.add(new Entry(IOBytes.readInteger(file, false)));
                else if (type == ColumnType.LONG)
                    entryArrayList.add(new Entry(IOBytes.readLong(file, false)));
                else if (type == ColumnType.FLOAT)
                    entryArrayList.add(new Entry(IOBytes.readFloat(file, false)));
                else if (type == ColumnType.DOUBLE)
                    entryArrayList.add(new Entry(IOBytes.readDouble(file, false)));
                else if (type == ColumnType.STRING)
                    entryArrayList.add(new Entry(IOBytes.readString(file, Global.SIZE_STRING, false)));
            }
        }

        Entry[] entries = new Entry[entryArrayList.size()];
        entryArrayList.toArray(entries);
        return new Row(entries);
    }

    /**
     * 写一行
     * @param row
     * @param accessFile
     * @param columnList
     * @throws IOException
     */
    public static void writeRow(Row row, RandomAccessFile accessFile, ArrayList<Column> columnList) throws IOException {
        ArrayList<Entry> entries = row.getEntries();
        for (int i = 0; i < entries.size(); ++i) {
            Column column = columnList.get(i);
            Entry entry = entries.get(i);
            ColumnType type = column.getType();
            if (type == ColumnType.INT) {
                int val = Integer.parseInt(entry.value.toString());
                IOBytes.writeInteger(accessFile, val, false);
            }
            else if (type == ColumnType.LONG) {
                long val = Long.parseLong(entry.value.toString());
                IOBytes.writeLong(accessFile, val, false);
            }
            else if(type == ColumnType.FLOAT) {
                float val = Float.parseFloat(entry.value.toString());
                IOBytes.writeFloat(accessFile, val, false);
            }
            else if(type == ColumnType.DOUBLE) {
                double val = Double.parseDouble(entry.value.toString());
                IOBytes.writeDouble(accessFile, val, false);
            }
            else if(type == ColumnType.STRING) {
                String str = entry.value.toString();
                IOBytes.writeString(accessFile, str, Global.SIZE_STRING, false);
            }
        }
    }

    /**
     * 插入一行
     * @param path
     * @param insertRow
     * @param columnList
     * @param table
     * @param indexTree
     * @throws IOException
     */
    public static void insertRow(String path, ArrayList<Entry> insertRow, ArrayList<Column> columnList, Table table, BPlusTree<Entry, Row> indexTree) throws IOException {
        RandomAccessFile accessFile = new RandomAccessFile(path, "rw");
        int rowNum = IOBytes.readInteger(accessFile, false);
        if (rowNum < 0)
            rowNum = 0;
        rowNum = rowNum + insertRow.size();
        int pageNum = IOBytes.readInteger(accessFile, false);

        ArrayList<Row> rowList = new ArrayList<>();
        for (Entry entry: insertRow)
            rowList.add(indexTree.get(entry));

        int rowLen = getLength(columnList);
        for (Row row: rowList) {
            int pos = allocatePage(rowLen, accessFile);
            accessFile.seek(pos);
            writeRow(row, accessFile, columnList);
        }

        int newPageNum = (int)accessFile.length() / pageSize + 1;
        accessFile.seek(0);
        IOBytes.writeInteger(accessFile, rowNum, false);
        IOBytes.writeInteger(accessFile, newPageNum, false);
        table.setPageNum(newPageNum);
    }

    /**
     * 删除一行
     * @param path
     * @param deleteRow 待删除的主键
     * @param columnList
     * @param filePageNum
     * @param priIndex 主键在row的第几列
     * @throws IOException
     */
    public static void deleteRow(String path, ArrayList<Entry> deleteRow, ArrayList<Column> columnList, int filePageNum, int priIndex) throws IOException {
        RandomAccessFile accessFile = new RandomAccessFile(path, "rw");
        long fileLen = accessFile.length();
        filePageNum = (int)(fileLen / pageSize);
        if (filePageNum * pageSize < fileLen)
            filePageNum += 1;
        int dataPageNum = filePageNum - 4;

        int rowNum = IOBytes.readInteger(accessFile, false);
        int pageIndex = 0;
        int rowLen = getLength(columnList);
        ArrayList<Row> rowList = new ArrayList<>();

        /* 遍历所有页 */
        while (deleteRow.size() != 0 && (pageIndex = nextPage(columnList, path, dataPageNum, rowList, pageIndex)) != 0) {
            int deleteIndex = 0;
            /* 查找这一页有没有需要删除的数据 */
            for (int i = 0; i < rowList.size(); ++i) {
                Entry entry = rowList.get(i).getEntries().get(priIndex);
                for (int j = 0; j < deleteRow.size(); ++j) {
                    if (equal(deleteRow.get(j), entry)) {
                        rowList.remove(i);
                        i -= 1;
                        deleteRow.remove(j);
                        j -= 1;
                        if (deleteIndex == 0)
                            deleteIndex = i + 1;
                        rowNum--;
                    }
                }
            }

            /* 删除完成后，将这一页的内容写回文件 */
            accessFile.seek((pageIndex + 3) * pageSize);
            IOBytes.writeInteger(accessFile, rowLen * rowList.size() + headerLength, false);
            accessFile.seek((pageIndex + 3) * pageSize + deleteIndex * rowLen + headerLength);
            for (int k = deleteIndex; k < rowList.size(); ++k) {
                writeRow(rowList.get(k), accessFile, columnList);
            }
            accessFile.seek(0);
            IOBytes.writeInteger(accessFile, rowNum, false);
        }
    }

    /**
     * 读取一页
     * @param pageIndex
     * @param rowLength
     * @param file
     * @param columnList
     * @return
     * @throws IOException
     */
    public static ArrayList<Row> readPage(int pageIndex, int rowLength, RandomAccessFile file, ArrayList<Column> columnList) throws IOException {
        ArrayList<Row> rowList = new ArrayList<>();
        int pos = pageIndex * pageSize;
        file.seek(pos);
        int inUse = IOBytes.readInteger(file, false);
        if (inUse != -1) {
            pos += headerLength;
            while (pos < inUse + pageIndex * pageSize) {
                rowList.add(readRow(pos, rowLength, file, columnList));
                pos += rowLength;
            }
        }
        else {
            pos += headerLength;
            rowList.add(readRow(pos, rowLength, file, columnList));
        }
        return rowList;
    }

    /**
     * 返回下一页的页号，如果没有下一页返回0
     * @param columnList
     * @param path
     * @param pageNum
     * @param rowList
     * @param pageIndex
     * @return
     * @throws IOException
     */
    public static int nextPage(ArrayList<Column> columnList, String path, int pageNum, ArrayList<Row> rowList, int pageIndex) throws IOException {
        // 没有下一页
        if (pageNum <= pageIndex)
            return 0;
        RandomAccessFile file = new RandomAccessFile(path, "r");
        int len = getLength(columnList);
        rowList.clear();
        rowList.addAll(readPage(pageIndex + 4, len, file, columnList));
        return pageIndex + 1;
    }

    public static void saveMeta(String path, ArrayList<String> names) throws IOException {
        File file = new File(path);
        /* 判断文件有无，如果有，删除 */
        if (file.exists())
            file.delete();
        file.createNewFile();
        RandomAccessFile accessFile = new RandomAccessFile(file, "rw");
        accessFile.seek(0);
        IOBytes.writeInteger(accessFile, names.size(), true);
        for (String name: names) {
            IOBytes.writeString(accessFile, name, Global.SIZE_STRING, true);
        }
        accessFile.close();
    }

    public static ArrayList<String> loadMeta(String path) throws IOException {
        File file = new File(path);
        /* 判断文件存不存在 */
        if (!file.exists()) {
            // 不存在，创建新文件
            file.createNewFile();
            RandomAccessFile accessFile = new RandomAccessFile(file, "rw");
            IOBytes.writeInteger(accessFile, 0, true);
            return null;
        }

        RandomAccessFile accessFile = new RandomAccessFile(file, "rw");
        int tmp = IOBytes.readInteger(accessFile, true);
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < tmp; ++i) {
            String name = IOBytes.readString(accessFile, Global.SIZE_STRING, true);
            names.add(name);
        }
        accessFile.close();
        return names;
    }

    public static void saveTableMeta(ArrayList<Column> columnList, String path) throws IOException{
        File file = new File(path);
        if (!file.exists())
            throw new FileNotFoundException(String.format(Global.NO_META));

        ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file));
        for (Column column: columnList) {
            os.writeObject(column);
        }
        os.close();
    }

    public static ArrayList<Column> loadTableMeta(String path) throws IOException {
        File file = new File(path);
        if (!file.exists())
            throw new FileNotFoundException(String.format(Global.NO_META));
        FileInputStream metaInputStream = new FileInputStream(file);
        ObjectInputStream columnInputStream = new ObjectInputStream(metaInputStream);
        ArrayList<Column> columnList = new ArrayList<>();
        while (true) {
            try {
                Column col = (Column) columnInputStream.readObject();
                columnList.add(col);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (EOFException e) {
                break;
            }
        }
        columnInputStream.close();
        metaInputStream.close();
        return columnList;
    }

    /**
     * 将一个表中的数据存储到文件
     * @param bPlusTree
     * @param columnList
     * @param path
     * @throws IOException
     */
    public static void saveTableData(BPlusTree<Entry, Row> bPlusTree, ArrayList<Column> columnList, String path) throws IOException {
        File file = new File(path);
        if(!file.exists())
            throw new FileNotFoundException(Global.NO_FILE);

        int rowNum = bPlusTree.size();
        int length = 0;
        int pageNum = 0;
        BPlusTreeIterator<Entry, Row> iter = bPlusTree.iterator();
        RandomAccessFile accessFile = new RandomAccessFile(file, "rw");
        IOBytes.writeInteger(accessFile, rowNum, false);

        while (iter.hasNext()) {
            Row row = iter.next().right;
            length = getLength(columnList);
            int pos = allocatePage(length, accessFile);
            accessFile.seek(pos);
            writeRow(row, accessFile, columnList);
        }

        pageNum = (int)accessFile.length() / pageSize + 1;
        accessFile.seek(Global.SIZE_INT);
        IOBytes.writeInteger(accessFile, pageNum, false);
        accessFile.seek(2 * Global.SIZE_INT);
        IOBytes.writeInteger(accessFile, 0, false);
        accessFile.close();
    }

    /**
     * 从文件里读一个表的数据
     * @param columnList
     * @param path
     * @return
     * @throws IOException
     */
    public static ArrayList<ArrayList> loadTableData(ArrayList<Column> columnList, String path) throws IOException {
        File file = new File(path);
        if (!file.exists())
            throw new FileNotFoundException(Global.NO_FILE);

        RandomAccessFile accessFile = new RandomAccessFile(file, "r");
        int count = 0;
        boolean flag = true;
        int len = getLength(columnList);
        int rowNum = IOBytes.readInteger(accessFile, false);
        int pageNum = IOBytes.readInteger(accessFile, false);
        ArrayList<Row> rowList = new ArrayList<>();
        ArrayList<Long> pointers = new ArrayList<>();
        ArrayList<Integer> pageNumList = new ArrayList<>();
        pageNumList.add(pageNum);
        int pageIndex = 4;

        while(count < rowNum) {
            rowList.addAll(readPage(pageIndex, len, accessFile, columnList));
            count = rowList.size();
            pageIndex++;
        }

        accessFile.close();
        ArrayList<ArrayList> list = new ArrayList<>();
        list.add(rowList);
        list.add(pointers);
        list.add(pageNumList);
        return list;
    }

    private static boolean equal(Entry e1, Entry e2) {
        if(e1.getType() == ColumnType.INT)
        {
            int val1 = Integer.parseInt(e1.value.toString());
            int val2 = Integer.parseInt(e2.value.toString());
            return val1 == val2;
        }
        else if(e1.getType() == ColumnType.LONG)
        {
            long val1 = Long.parseLong(e1.value.toString());
            long val2 = Long.parseLong(e2.value.toString());
            return val1 == val2;
        }
        else if(e1.getType() == ColumnType.FLOAT)
        {
            float val1 = Float.parseFloat(e1.value.toString());
            float val2 = Float.parseFloat(e2.value.toString());
            return val1 == val2;
        }
        else if(e1.getType() == ColumnType.DOUBLE)
        {
            double val1 = Double.parseDouble(e1.value.toString());
            double val2 = Double.parseDouble(e2.value.toString());
            return val1 == val2;
        }
        else if(e1.getType() == ColumnType.STRING)
            return e1.value.toString().equals(e2.value.toString());
        return true;
    }

}
