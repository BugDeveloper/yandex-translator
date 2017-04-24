package com.bugdeveloper.androidyandextranslator;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonAdapter extends BaseAdapter implements ListAdapter {

    private final Activity activity;
    private final JSONArray jsonArray;

    public JsonAdapter(Activity activity, JSONArray jsonArray) {
        this.activity = activity;
        this.jsonArray = jsonArray;
    }

    @Override
    public int getCount() {
        return jsonArray.length();
    }

    @Override
    public JSONObject getItem(int position) {
        return jsonArray.optJSONObject(position);
    }

    @Override
    public long getItemId(int position) {
        JSONObject jsonObject = getItem(position);
        return jsonObject.optLong("id");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = activity.getLayoutInflater().inflate(R.layout.dictionary_row, parent, false);

            convertView.setEnabled(false);
            convertView.setOnClickListener(null);

            holder = new ViewHolder();
            holder.setSpeechPart((TextView) convertView.findViewById(R.id.speechPartTitle));
            holder.setTranslationLayout((LinearLayout) convertView.findViewById(R.id.TranslationLayout));
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        JSONObject root;

        try {
            root = jsonArray.getJSONObject(position);
            holder.getSpeechPart().setText(root.getString("pos"));
            JSONArray translations = root.getJSONArray("tr");

            holder.getTranslationLayout().removeAllViews();

            for (int i = 0; i < translations.length(); i++) {
                String translation = "";
                String mean = "";

                JSONObject jsonObject = translations.getJSONObject(i);
                translation += jsonObject.getString("text") + ", ";

                try {
                    JSONArray synArray = jsonObject.getJSONArray("syn");
                    for (int j = 0; j < synArray.length(); j++) {
                        translation += synArray.getJSONObject(j).getString("text") + ", ";
                    }
                } catch (JSONException ignored) {
                }

                try {
                    JSONArray meanArray = jsonObject.getJSONArray("mean");
                    for (int j = 0; j < meanArray.length(); j++) {
                        mean += meanArray.getJSONObject(j).getString("text") + ", ";
                    }
                } catch (JSONException ignored) {
                }

                View dicRow = activity.getLayoutInflater().inflate(R.layout.dictionary_subrow, holder.getTranslationLayout(), false);
                TextView dicTrans = (TextView) dicRow.findViewById(R.id.DicTranslation);
                TextView dicMean = (TextView) dicRow.findViewById(R.id.DicMean);

                if (translation.length() > 2)
                    translation = translation.substring(0, translation.length() - 2);
                if (mean.length() > 2)
                    mean = '(' + mean.substring(0, mean.length() - 2) + ')';

                dicTrans.setText(translation);
                dicMean.setText(mean);

                holder.getTranslationLayout().addView(dicRow);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return convertView;
    }

    private static class ViewHolder {
        private TextView speechPart;
        private LinearLayout translationLayout;

        TextView getSpeechPart() {
            return speechPart;
        }

        void setSpeechPart(TextView speechPart) {
            this.speechPart = speechPart;
        }

        LinearLayout getTranslationLayout() {
            return translationLayout;
        }

        void setTranslationLayout(LinearLayout translationLayout) {
            this.translationLayout = translationLayout;
        }
    }
}
