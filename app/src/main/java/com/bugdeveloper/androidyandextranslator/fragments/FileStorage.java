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
    public static final String CACHE_DIR = "cache";

    public static void CreateDir(Context context) {
        context.getDir(CACHE_DIR, Context.MODE_PRIVATE);
    }

    public static boolean FileExists(String path, String fileName) {
        return new File(path + "/" + fileName).exists();
    }

    public static void Save(String path, String fileName, Object obj) {

        File directory = new File(path);

        if (!directory.exists()) {
            directory.mkdirs();
        }

        File file = new File(directory, fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file, true);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(obj);
            os.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Object Load(String directory, String fileName) {

        String path = directory + "/" + fileName;

        Object obj = null;
        try {
            FileInputStream fis = new FileInputStream(path);
            ObjectInputStream is = new ObjectInputStream(fis);
            try {
                obj = is.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            is.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return obj;
    }
}