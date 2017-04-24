package com.bugdeveloper.androidyandextranslator.fragments;

import com.bugdeveloper.androidyandextranslator.DataStorage;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

public final class QuerySender {

    private QuerySender() {
    }

    public static String PostQuery(String api, Iterable<BasicNameValuePair> args) {

        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(api);

        List<NameValuePair> params = new ArrayList<>(4);

        for (BasicNameValuePair arg : args) {
            params.add(arg);
        }

        try {
            httppost.setEntity(new UrlEncodedFormEntity(params, DataStorage.STRING_FORMAT));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        HttpResponse response = null;
        try {
            response = httpclient.execute(httppost);
        } catch (IOException e) {
            e.printStackTrace();
        }

        HttpEntity entity = response.getEntity();

        String res = "";

        if (entity != null) {
            InputStream instream = null;
            try {
                instream = entity.getContent();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                res = IOUtils.toString(instream, DataStorage.STRING_FORMAT);
            } catch (IOException e) {
                e.printStackTrace();
            }

            IOUtils.closeQuietly(instream);
        }
        return res;
    }
}
