package com.galaxas0.Quartz.activity;

import android.animation.Animator;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;

import com.galaxas0.Quartz.BuildConfig;
import com.galaxas0.Quartz.R;
import com.galaxas0.Quartz.ui.LineColorPickerPreference;
import com.galaxas0.Quartz.ui.QuartzActivity;
import com.galaxas0.Quartz.utils.AnimationUtils;
import com.galaxas0.Quartz.utils.StringUtils;
import com.galaxas0.Quartz.utils.ThemeUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class SettingsActivity extends QuartzActivity {

    private static final DateFormat DATE_DISPLAY_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm a");

    public Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeUtils.getThemeResource(this, ThemeUtils.preferences(this).getInt("themeColor", Color.parseColor("#212121"))));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle("Settings");
        toolbar.setNavigationOnClickListener((View v) -> onBackPressed());

        getFragmentManager().beginTransaction().replace(R.id.content, new SettingsFragment()).commit();

        addPreferenceListener("themeColor", pref -> {
            int color = pref.getInt("themeColor", Color.parseColor("#212121"));
            int colors[] = ThemeUtils.getThemeColors(this, ThemeUtils.getThemeResource(this, color));
            Animator a = AnimationUtils.animateBackgroundColor(this.toolbar, colors[0]);
            Animator b = AnimationUtils.animateStatusBarColor(this, colors[1]);
            Animator c = AnimationUtils.animateNavigationBarColor(this, colors[1]);
            AnimationUtils.animateParallelly(a, b, c).start();
        });

        //addPreferenceListener("animationSpeed", pref -> {
        //    ThemeUtils.applyAnimationSpeed(pref.getBoolean("animationSpeed", false) ? 5 : 1);
        //});
    }

    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            LineColorPickerPreference p = (LineColorPickerPreference) getPreferenceManager().findPreference("themeColor");
            TypedArray array = getResources().obtainTypedArray(R.array.material_colors);
            p.setColors(array);
            array.recycle();

            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            String densityBucket = StringUtils.getDensityString(displayMetrics);

            findPreference("device").setSummary(StringUtils.truncateAt(Build.MANUFACTURER, 20) + " " + StringUtils.truncateAt(Build.MODEL, 20));
            findPreference("resolution").setSummary(displayMetrics.heightPixels + "x" + displayMetrics.widthPixels);
            findPreference("density").setSummary(displayMetrics.densityDpi + " dpi [" + densityBucket + "]");
            findPreference("api-release").setSummary("Android " + Build.VERSION.RELEASE + " " + String.valueOf(Build.VERSION.SDK_INT));
            findPreference("build-info").setSummary(BuildConfig.VERSION_NAME + " [" + String.valueOf(BuildConfig.VERSION_CODE) + "]");

            try {
                DateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
                inFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date buildTime = inFormat.parse(BuildConfig.BUILD_TIME);
                findPreference("date").setSummary(DATE_DISPLAY_FORMAT.format(buildTime));
            } catch (ParseException e) {
                throw new RuntimeException("Unable to decode build time: " + BuildConfig.BUILD_TIME, e);
            }
        }
    }
}
