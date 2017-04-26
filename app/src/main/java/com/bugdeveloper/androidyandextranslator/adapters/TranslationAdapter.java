package com.bugdeveloper.androidyandextranslator.adapters;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.bugdeveloper.androidyandextranslator.R;
import com.bugdeveloper.androidyandextranslator.fragments.Translation;

import java.util.List;

public class TranslationAdapter extends BaseAdapter implements ListAdapter {

    private Activity activity;
    private List<Translation> translations;

    public TranslationAdapter(Activity activity, List<Translation> translations) {
        this.activity = activity;
        this.translations = translations;
    }

    @Override
    public int getCount() {
        return translations.size();
    }

    @Override
    public Translation getItem(int position) {
        return translations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = activity.getLayoutInflater().inflate(R.layout.history_row, parent, false);

            convertView.setEnabled(false);
            convertView.setOnClickListener(null);

            holder = new ViewHolder();
            holder.setWord((TextView) convertView.findViewById(R.id.wordField));
            holder.setFavourite((ImageView) convertView.findViewById(R.id.addToFavourite));
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.getWord().setText(translations.get(position).getWordTranslation());
        ImageView image = holder.getFavourite();

        if (translations.get(position).getFavourite()) {
            image.setColorFilter(ContextCompat.getColor(activity, R.color.primary));
        } else {
            image.setColorFilter(ContextCompat.getColor(activity, R.color.button));
        }

        return convertView;
    }

    private static class ViewHolder {
        private TextView word;
        private ImageView favourite;

        ImageView getFavourite() {
            return favourite;
        }

        void setFavourite(ImageView favourite) {
            this.favourite = favourite;
        }

        TextView getWord() {
            return word;
        }

        void setWord(TextView speechPart) {
            this.word = speechPart;
        }
    }
}
