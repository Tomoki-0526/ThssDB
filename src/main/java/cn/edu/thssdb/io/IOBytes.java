package cn.edu.thssdb.io;

import cn.edu.thssdb.utils.Global;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

/**
 * @描述 IO字节流操作类
 * 以字节数组的形式存储到文件
 */
public class IOBytes {
    public static void writeLine(RandomAccessFile outFile) throws IOException {
        outFile.write((char) 13);
    }

    public static void readLine(RandomAccessFile inFile) throws IOException {
        byte[] tmp = new byte[1];
        inFile.read(tmp, 0, 1);
    }

    public static int readInteger(RandomAccessFile inFile, boolean hasNext) throws IOException{
        byte[] data = new byte[Global.SIZE_INT];
        inFile.read(data, 0, Global.SIZE_INT);
        String tmp = new String(data).trim();
        if (tmp.length() == 0)
            return -1;
        int val = Integer.parseInt(tmp);
        if (hasNext)
            readLine(inFile);
        return val;
    }

    public static void writeInteger(RandomAccessFile outFile, int val, boolean hasNext) throws IOException {
        byte[] res = new byte[Global.SIZE_INT];
        byte[] tmp = Integer.toString(val).getBytes();
        Arrays.fill(res, (byte)0);
        System.arraycopy(tmp, 0, res, 0, tmp.length);
        outFile.write(res, 0, Global.SIZE_INT);
        if (hasNext)
            writeLine(outFile);
    }

    public static long readLong(RandomAccessFile inFile, boolean hasNext) throws IOException {
        byte[] data = new byte[Global.SIZE_LONG];
        inFile.read(data, 0, Global.SIZE_LONG);
        String tmp = new String(data).trim();
        if (tmp.length() == 0)
            return -1;
        long val = Long.parseLong(tmp);
        if (hasNext)
            readLine(inFile);
        return val;
    }

    public static void writeLong(RandomAccessFile outFile, long val, boolean hasNext) throws IOException {
        byte[] res = new byte[Global.SIZE_LONG];
        byte[] tmp = Long.toString(val).getBytes();
        Arrays.fill(res, (byte)0);
        System.arraycopy(tmp, 0, res, 0, tmp.length);
        outFile.write(res, 0, Global.SIZE_LONG);
        if (hasNext)
            writeLine(outFile);
    }

    public static float readFloat(RandomAccessFile inFile, boolean hasNext) throws IOException {
        byte[] data = new byte[Global.SIZE_FLOAT];
        inFile.read(data, 0, Global.SIZE_FLOAT);
        String tmp = new String(data).trim();
        if (tmp.length() == 0)
            return -1;
        float val = Float.parseFloat(tmp);
        if (hasNext)
            readLine(inFile);
        return val;
    }

    public static void writeFloat(RandomAccessFile outFile, float val, boolean hasNext) throws IOException {
        byte[] res = new byte[Global.SIZE_FLOAT];
        byte[] tmp = Float.toString(val).getBytes();
        Arrays.fill(res, (byte)0);
        System.arraycopy(tmp, 0, res, 0, tmp.length);
        outFile.write(res, 0, Global.SIZE_FLOAT);
        if (hasNext)
            writeLine(outFile);
    }

    public static double readDouble(RandomAccessFile inFile, boolean hasNext) throws IOException {
        byte[] data = new byte[Global.SIZE_DOUBLE];
        inFile.read(data, 0, Global.SIZE_DOUBLE);
        String tmp = new String(data).trim();
        if (tmp.length() == 0)
            return -1;
        double val = Double.parseDouble(tmp);
        if (hasNext)
            readLine(inFile);
        return val;
    }

    public static void writeDouble(RandomAccessFile outFile, double val, boolean hasNext) throws IOException {
        byte[] res = new byte[Global.SIZE_DOUBLE];
        byte[] tmp = Double.toString(val).getBytes();
        Arrays.fill(res, (byte)0);
        System.arraycopy(tmp, 0, res, 0, tmp.length);
        outFile.write(res, 0, Global.SIZE_DOUBLE);
        if (hasNext)
            writeLine(outFile);
    }

    public static String readString(RandomAccessFile inFile, int maxLength, boolean hasNext) throws IOException {
        byte[] data = new byte[maxLength];
        inFile.read(data, 0, maxLength);
        String tmp = new String(data).trim();
        if(hasNext)
            readLine(inFile);
        return tmp;
    }

    public static void writeString(RandomAccessFile outFile, String str, int maxLength, boolean hasNext) throws IOException {
        byte[] res = new byte[maxLength];
        byte[] tmp = str.getBytes();
        Arrays.fill(res, (byte) 0);
        System.arraycopy(tmp, 0, res, 0, tmp.length);
        outFile.write(res, 0, maxLength);
        if (hasNext)
            writeLine(outFile);
    }
}
