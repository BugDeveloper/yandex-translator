package com.bugdeveloper.androidyandextranslator.fragments;

import java.io.Serializable;

public class Translation implements Serializable {

    private String[] translation;
    private boolean favourite;

    public boolean getFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

    public Translation(String[] translation, boolean favourite) {
        this.translation = translation;
        this.favourite = favourite;
    }

    public String getWordTranslation() {
        return translation[0];
    }

    public String getDictionaryTranslation() {
        return translation[1];
    }
}
