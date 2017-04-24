package com.bugdeveloper.androidyandextranslator;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import com.bugdeveloper.androidyandextranslator.fragments.Bookmarks;
import com.bugdeveloper.androidyandextranslator.fragments.Settings;
import com.bugdeveloper.androidyandextranslator.fragments.Translate;

public class MainActivity extends AppCompatActivity {

    private Fragment translate, bookmarks, settings;

    private int curId = R.id.navigation_translate;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            if (curId == item.getItemId()) return true;

            switch (item.getItemId()) {
                case R.id.navigation_translate:
                    curId = R.id.navigation_translate;
                    SwitchFragmentTo(translate);
                    return true;
                case R.id.navigation_bookmarks:
                    curId = R.id.navigation_bookmarks;
                    SwitchFragmentTo(bookmarks);
                    return true;
                case R.id.navigation_settings:
                    curId = R.id.navigation_settings;
                    SwitchFragmentTo(settings);
                    return true;
            }

            return false;
        }

    };

    private void SwitchFragmentTo(Fragment fragment) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.content, fragment);
        ft.commit();
    }

    private void InitializeContent() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.content, translate);
        ft.commit();
    }

    private void InitializeFragments() {
        translate = new Translate();
        bookmarks = new Bookmarks();
        settings = new Settings();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InitializeFragments();
        InitializeContent();
        SwitchFragmentTo(translate);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

}
