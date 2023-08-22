package com.semantyca.core.util;

import org.apache.commons.io.FileUtils;
import org.jboss.logging.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtil {

    private static final Logger LOGGER = Logger.getLogger(FileUtil.class);

    public static File getRndFile(String directory) {
        int maxIt = 10, it = 0;
        File dir = new File(directory);
        File[] files = dir.listFiles();
        File file = null;
        do {
            Random rand = new Random();
            file = files[rand.nextInt(files.length)];
            it++;
        } while (file.isDirectory() && it < maxIt);
        return file;
    }

    public static String getFileName(String fn, String tmpFolder) {
        int folderNum = 1;
        File dir = new File(tmpFolder + File.separator + Integer.toString(folderNum));
        while (dir.exists()) {
            folderNum++;
            dir = new File(tmpFolder + File.separator + Integer.toString(folderNum));
        }
        dir.mkdirs();
        fn = dir + File.separator + fn;
        return fn;
    }

    public static String getFileDateTime(String folder, String fn) {
        File file = new File(folder + File.separator + fn);
        while (file.exists() && (!file.isDirectory())) {
            Path filePath = file.toPath();
            try {
                BasicFileAttributes attr = Files.readAttributes(filePath, BasicFileAttributes.class);
                Date creationDate = new Date(attr.creationTime().to(TimeUnit.MILLISECONDS));
                return TimeUtil.dateTimeToStringSilently(creationDate);
            } catch (IOException e) {
                LOGGER.error(e);
                return "";
            }

        }
        return "";
    }




    public static String readFile(String file) {
        BufferedReader reader = null;
        try {
            // File f = new File(file);
            reader = new BufferedReader(new FileReader(file));
            String line = null;
            StringBuilder stringBuilder = new StringBuilder();
            String ls = System.getProperty("line.separator");

            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }

            return stringBuilder.toString();
        } catch (FileNotFoundException e) {
            LOGGER.error(e);
        } catch (IOException e) {
            LOGGER.error(e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                LOGGER.error(e);
            }
        }
        return "";
    }


    public static void zipFolder(String tmpBackupDirPath, String zipFileName, boolean deleteFolderAfterZip) {
        try {
            System.out.println("packing to \"" + zipFileName + "\"...");
            pack(tmpBackupDirPath, zipFileName);
            System.out.println("deleting temporary folder...");
            if (deleteFolderAfterZip) {
                FileUtils.deleteDirectory(new File(tmpBackupDirPath));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.info("\"" + tmpBackupDirPath + "\"backed up");
    }

    private static void pack(String sourceDirPath, String zipFilePath) throws IOException {
        Path p = Files.createFile(Paths.get(zipFilePath));

        try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p))) {
            Path pp = Paths.get(sourceDirPath);
            Files.walk(pp)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
                        try {
                            zs.putNextEntry(zipEntry);
                            Files.copy(path, zs);
                            zs.closeEntry();
                        } catch (IOException e) {
                            System.err.println(e);
                        }
                    });
        }
    }

}
