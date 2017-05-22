package com.polsl.roadtracker.util;

import android.content.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
                return STORE_DIRECTORY + fileName;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
}
