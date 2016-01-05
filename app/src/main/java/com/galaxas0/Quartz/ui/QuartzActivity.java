package com.galaxas0.Quartz.ui;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;

import com.galaxas0.Quartz.utils.ThemeUtils;

import java.util.ArrayList;
import java.util.HashMap;

import java8.util.function.Consumer;
import java8.util.stream.StreamSupport;

public class QuartzActivity extends AppCompatActivity implements OnSharedPreferenceChangeListener {

    private HashMap<String, Consumer<SharedPreferences>> _consumers = new HashMap<>();
    private ArrayList<Runnable> _visibility = new ArrayList<>();
    private boolean isFront = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeUtils.preferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    // SharedPreferences Support
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(_consumers.containsKey(key))
            _consumers.get(key).accept(sharedPreferences);
    }

    protected final void addPreferenceListener(String key, Consumer<SharedPreferences> value) {
        _consumers.put(key, value);
    }

    protected final void removePreferenceListener(String key) {
        _consumers.remove(key);
    }

    // isFront Support
    @Override
    protected void onResume() {
        super.onResume();
        isFront = true;

        StreamSupport.stream(_visibility)
                .forEach(Runnable::run);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isFront = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isFront = false;
    }

    public boolean isActivityVisible() {
        return isFront;
    }

    public void runWhenVisible(Runnable runnable) {
        if(isFront)
            runnable.run();
        else _visibility.add(runnable);
    }

    public void runOnUiThreadDelayed(Runnable action, long delay) {
        new Handler(getMainLooper()).postDelayed(action, delay);
    }
}
