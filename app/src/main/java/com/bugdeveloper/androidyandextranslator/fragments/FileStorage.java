package com.bugdeveloper.androidyandextranslator.fragments;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public final class FileStorage {

    private FileStorage() {
    }

    public static final String LANGUAGES = "LANGUAGES.DAT";
    public static final String TRANSLATIONS = "TRANSLATIONS.DAT";

    public static String CACHE_PATH;

    public static void CreateDir(Context context) {
        context.getDir("cache", Context.MODE_PRIVATE);
        CACHE_PATH = context.getFilesDir().getPath() + "/" + "cache";
    }

    public static void Save(String fileName, Object obj) {

        File directory = new File(CACHE_PATH);

        if (!directory.exists()) {
            directory.mkdirs();
        }

        File file = new File(directory, fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file, false);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(obj);
            os.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Object Load(String fileName) throws IOException {

        String path = CACHE_PATH + "/" + fileName;

        Object obj = null;
            FileInputStream fis = new FileInputStream(path);
            ObjectInputStream is = new ObjectInputStream(fis);
            try {
                obj = is.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            is.close();
            fis.close();
        return obj;
    }
}