package com.polsl.roadtracker.util;

import android.content.Context;
import android.util.Base64;

import org.apache.commons.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by alachman on 20.05.2017.
 */

public class FileHelper {
    public static String saveRouteToFile(String content, Long id, Context context) {
        String STORE_DIRECTORY;
        FileWriter file = null;
        String fileName = "route" + id + ".json";

        try {
            File externalFilesDir = context.getExternalFilesDir(null);
            if (externalFilesDir != null) {
                STORE_DIRECTORY = externalFilesDir.getAbsolutePath() + "/routes/";
                File storeDirectory = new File(STORE_DIRECTORY);
                if (!storeDirectory.exists()) {
                    boolean success = storeDirectory.mkdirs();
                    if (!success) {
                        Timber.e("failed to create file storage directory.");
                        return null;
                    }
                }
                file = new FileWriter(STORE_DIRECTORY + fileName);
                file.write(content);
                file.flush();
                file.close();
                Timber.d("Finished writing file " + fileName);
                return "/routes/" + fileName;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public static String convertFileToString(String pathToFile) throws IOException {
        return new String(Base64.encode(FileUtils.readFileToByteArray(new File(pathToFile)), Base64.DEFAULT));
    }

    public static List<String> splitFile(String path) throws IOException {
        File f = new File(path);
        List<String> fileNames = new ArrayList<>();
        int partCounter = 1;

        int sizeOfFiles = 1024 * 1024 * 10;// 10MB
        byte[] buffer = new byte[sizeOfFiles];

        FileInputStream fileInputStream = new FileInputStream(f);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

        String name = f.getName();

        int tmp = 0;
        while ((tmp = bufferedInputStream.read(buffer)) > 0) {
            String partName = name + "." + String.format("%04d", partCounter++);
            fileNames.add(f.getParent() + "/" + partName);
            File newFile = new File(f.getParent(), partName);
            FileOutputStream out = new FileOutputStream(newFile);
            out.write(buffer, 0, tmp);
            out.close();
        }

        bufferedInputStream.close();
        fileInputStream.close();

        return fileNames;
    }
    public static void deleteFile(String fileName) {
        (new File(fileName)).delete();
    }

    public static void deleteFiles(List<String> fileNames) {
        for (String fileName : fileNames) {
            (new File(fileName)).delete();
        }
    }
    private static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromFile(String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }

    public static void deleteResultFiles(Context context) {
        File externalFilesDir = context.getExternalFilesDir(null);
        if (externalFilesDir != null) {
            String path = externalFilesDir.getAbsolutePath() + "/routes/";

            File file = new File(path);

            if (file.exists()) {
                String deleteCmd = "rm -r " + path;
                Runtime runtime = Runtime.getRuntime();
                try {
                    runtime.exec(deleteCmd);
                } catch (IOException e) {
                }
            }
        }
    }
}
