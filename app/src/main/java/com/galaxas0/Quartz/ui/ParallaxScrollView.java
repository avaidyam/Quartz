package com.galaxas0.Quartz.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.ScrollView;

import com.galaxas0.Quartz.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ParallaxScrollView extends ScrollView {

    private static final int DEFAULT_PARALLAX_VIEWS = 1;
    private static final float DEFAULT_INNER_PARALLAX_FACTOR = 1.9F;
    private static final float DEFAULT_PARALLAX_FACTOR = 1.9F;
    private static final float DEFAULT_ALPHA_FACTOR = -1F;
    
    private int numOfParallaxViews = DEFAULT_PARALLAX_VIEWS;
    private float innerParallaxFactor = DEFAULT_PARALLAX_FACTOR;
    private float parallaxFactor = DEFAULT_PARALLAX_FACTOR;

    public int getNumOfParallaxViews() {
        return numOfParallaxViews;
    }

    public void setNumOfParallaxViews(int numOfParallaxViews) {
        this.numOfParallaxViews = numOfParallaxViews;
    }

    public float getInnerParallaxFactor() {
        return innerParallaxFactor;
    }

    public void setInnerParallaxFactor(float innerParallaxFactor) {
        this.innerParallaxFactor = innerParallaxFactor;
    }

    public float getParallaxFactor() {
        return parallaxFactor;
    }

    public void setParallaxFactor(float parallaxFactor) {
        this.parallaxFactor = parallaxFactor;
    }

    public float getAlphaFactor() {
        return alphaFactor;
    }

    public void setAlphaFactor(float alphaFactor) {
        this.alphaFactor = alphaFactor;
    }

    private float alphaFactor = DEFAULT_ALPHA_FACTOR;

    private ArrayList<ParallaxedView> parallaxedViews = new ArrayList<ParallaxedView>();

    public ParallaxScrollView(Context context) {
        super(context);
    }

    public ParallaxScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        makeViewsParallax();
    }

    private void makeViewsParallax() {
        if (getChildCount() > 0 && getChildAt(0) instanceof ViewGroup) {
            ViewGroup viewsHolder = (ViewGroup) getChildAt(0);
            int numOfParallaxViews = Math.min(this.numOfParallaxViews, viewsHolder.getChildCount());
            for (int i = 0; i < numOfParallaxViews; i++) {
                ParallaxedView parallaxedView = new ParallaxedView(viewsHolder.getChildAt(i));
                parallaxedViews.add(parallaxedView);
            }
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        float parallax = parallaxFactor;
        float alpha = alphaFactor;

        for (ParallaxedView parallaxedView : parallaxedViews) {
            parallaxedView.setOffset((float)t / parallax);
            parallax *= innerParallaxFactor;

            if (alpha != DEFAULT_ALPHA_FACTOR) {
                parallaxedView.setAlpha(100 / ((float)t * alpha));
                alpha /= alphaFactor;
            }
            parallaxedView.animateNow();
        }
    }

    public class ParallaxedView {
        protected WeakReference<View> view;
        List<Animation> animations = new ArrayList<>();

        public ParallaxedView(View view) {
            this.view = new WeakReference<>(view);
        }

        public boolean is(View v) {
            return (v != null && view != null && view.get() != null && view.get().equals(v));
        }

        public void setOffset(float offset) {
            View view = this.view.get();
            if (view != null)
                view.setTranslationY(offset);
        }

        public void setAlpha(float alpha) {
            View view = this.view.get();
            if (view != null)
                view.setAlpha(alpha);
        }

        protected synchronized void addAnimation(Animation animation) {
            animations.add(animation);
        }

        protected synchronized void animateNow() {
            View view = this.view.get();
            if (view != null) {
                AnimationSet set = new AnimationSet(true);
                for (Animation animation : animations)
                    if (animation != null)
                        set.addAnimation(animation);
                set.setDuration(0);
                set.setFillAfter(true);
                view.setAnimation(set);
                set.start();
                animations.clear();
            }
        }

        public void setView(View view) {
            this.view = new WeakReference<>(view);
        }
    }
}