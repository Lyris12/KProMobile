package cn.garymb.ygomobile.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UnzipUtils {
    private static final int BUFFER_SIZE = 1024 * 1024;//1M Byte
    public static final int ZIP_READY = 600;
    public static final int ZIP_UNZIP_OK = 601;
    public static final int ZIP_UNZIP_EXCEPTION = 602;

    /**
     * 解压缩一个文件
     *
     * @param zipFile    压缩文件
     * @param folderPath 解压缩的目标目录
     * @return
     * @throws IOException 当解压缩过程出错时抛出
     */
    public static ArrayList<File> upZipFile(File zipFile, String folderPath) throws IOException {
        ArrayList<File> fileList = new ArrayList<File>();
        File desDir = new File(folderPath);
        if (!desDir.exists()) {
            desDir.mkdirs();
        }
        ZipFile zf = new ZipFile(zipFile);
        for (Enumeration<?> entries = zf.entries(); entries.hasMoreElements(); ) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            if (entry.isDirectory()) {
                continue;
            }
            InputStream is = zf.getInputStream(entry);
            String str = folderPath + File.separator + entry.getName();
            str = new String(str.getBytes("8859_1"), StandardCharsets.UTF_8);
            File desFile = new File(str);
            if (!desFile.exists()) {
                File fileParentDir = desFile.getParentFile();
                if (!fileParentDir.exists()) {
                    fileParentDir.mkdirs();
                }
                desFile.createNewFile();
            }
            OutputStream os = new FileOutputStream(desFile);
            byte[] buffer = new byte[BUFFER_SIZE];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
            os.close();
            is.close();
            fileList.add(desFile);
        }
        return fileList;
    }

    /**
     * 解压文件名包含传入文字的文件
     *
     * @param zipFile      压缩文件
     * @param folderPath   目标文件夹
     * @param nameContains 传入的文件匹配名
     * @return
     * @throws IOException 当解压缩过程出错时抛出
     */
    public static ArrayList<File> upZipSelectFile(File zipFile, String folderPath, String nameContains) throws IOException {
        ArrayList<File> fileList = new ArrayList<File>();
        File desDir = new File(folderPath);
        if (!desDir.exists()) {
            desDir.mkdirs();
        }
        ZipFile zf = new ZipFile(zipFile);
        for (Enumeration<?> entries = zf.entries(); entries.hasMoreElements(); ) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            if (entry.isDirectory()) {
                continue;
            }
            if (entry.getName().contains(nameContains)) {//如果zip中包含.ypk格式的文件，则将其放到/storage/emulated/0/Android/data/cn.garymb.ygomobile.CN/files/ygocore目录下
                InputStream is = zf.getInputStream(entry);
                String str = folderPath + File.separator + entry.getName();
                str = new String(str.getBytes("8859_1"), StandardCharsets.UTF_8);
                File desFile = new File(str);
                if (!desFile.exists()) {
                    File fileParentDir = desFile.getParentFile();
                    if (!fileParentDir.exists()) {
                        fileParentDir.mkdirs();
                    }
                    desFile.createNewFile();
                }
                OutputStream os = new FileOutputStream(desFile);
                byte[] buffer = new byte[BUFFER_SIZE];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                is.close();
                os.flush();
                os.close();
                fileList.add(desFile);
            }
        }
        return fileList;
    }
}
