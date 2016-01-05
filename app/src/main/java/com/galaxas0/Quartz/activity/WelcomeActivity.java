package com.galaxas0.Quartz.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.res.TypedArray;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewAnimationUtils;

import com.galaxas0.Quartz.R;
import com.galaxas0.Quartz.databinding.ActivityWelcomeBinding;
import com.galaxas0.Quartz.ui.QuartzActivity;
import com.galaxas0.Quartz.utils.AnimationUtils;
import com.galaxas0.Quartz.ui.StaticPagerAdapter;
import com.galaxas0.Quartz.utils.ThemeUtils;

public class WelcomeActivity extends QuartzActivity {

    private ActivityWelcomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_welcome);
        ThemeUtils.enableTranslucent(this);
        StaticPagerAdapter.setPagerAdapter(binding.pager);

        runOnUiThreadDelayed(() -> {
            Animator a = AnimationUtils.animateBackgroundColor(binding.pager, Color.parseColor("#ff37474f"));
            Animator b = AnimationUtils.animateStatusBarColor(this, Color.parseColor("#ff37474f"));
            Animator c = AnimationUtils.animateNavigationBarColor(this, Color.parseColor("#ff37474f"));
            AnimationUtils.animateParallelly(a, b, c).start();
        }, 250L);

        binding.setupComplete.setOnClickListener(v -> {
            ThemeUtils.preferences(WelcomeActivity.this).edit().putBoolean("setup_complete", true).apply();
            runOnUiThreadDelayed(() -> {
                int[] location = new int[2];
                //noinspection ResourceType
                binding.setupComplete.getLocationOnScreen(location);
                int cx = (location[0] + (binding.setupComplete.getWidth() / 2));
                int cy = location[1] + (binding.setupComplete.getHeight() / 2);

                Animator a = AnimationUtils.animateBackgroundColor(binding.pager, Color.parseColor("#ff424242"));
                Animator b = AnimationUtils.animateStatusBarColor(WelcomeActivity.this, Color.parseColor("#ff424242"));
                Animator c = AnimationUtils.animateNavigationBarColor(WelcomeActivity.this, Color.parseColor("#ff424242"));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Animator d = ViewAnimationUtils.createCircularReveal(binding.pager, cx, cy, binding.pager.getHeight(), 0);
                    d.setDuration(250L);
                    d.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            binding.pager.setVisibility(View.INVISIBLE);
                            overridePendingTransition(0, 0);
                            finish();
                        }
                    });

                    AnimationUtils.animateParallelly(a, b, c, d).start();
                } else AnimationUtils.animateParallelly(a, b, c).start();
            }, 250L);
        });

        binding.colorPicker.setOnColorChangedListener(color -> {
            ThemeUtils.preferences(this).edit().putInt("themeColor", color).apply();

            Animator a = AnimationUtils.animateBackgroundColor(binding.pager, color);
            Animator b = AnimationUtils.animateStatusBarColor(WelcomeActivity.this, color);
            Animator c = AnimationUtils.animateNavigationBarColor(WelcomeActivity.this, color);
            AnimationUtils.animateParallelly(a, b, c).start();
        });

        binding.boxExpanded.setOnClickListener(v -> {
            ThemeUtils.preferences(this).edit().putBoolean("expandColumns", true).apply();
        });

        binding.boxNormal.setOnClickListener(v -> {
            ThemeUtils.preferences(this).edit().putBoolean("expandColumns", false).apply();
        });
    }
}
