package com.galaxas0.Quartz.utils;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Property;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.widget.ImageView;

import com.galaxas0.Quartz.R;

import java.lang.reflect.Method;

public class ThemeUtils {

    public static SharedPreferences preferences(Fragment ctx) {
        return preferences(ctx.getActivity());
    }

    public static SharedPreferences preferences(Activity ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx.getApplicationContext());
    }

    public static int[] getThemeColors(Context ctx) {
        TypedValue typedValue = new TypedValue();
        ctx.getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        int colorPrimary = typedValue.data;
        ctx.getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        int colorPrimaryDark = typedValue.data;
        ctx.getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
        int colorAccent = typedValue.data;
        return new int [] { colorPrimary, colorPrimaryDark, colorAccent };
    }

    public static int[] getThemeColors(Activity ctx, int resId) {
        return getThemeColors(new ContextThemeWrapper(ctx, resId));
    }

    public static int getThemeResource(Activity ctx, int themeColor) {
        TypedArray val = ctx.getResources().obtainTypedArray(R.array.material_colors);
        //if(themeColor.equals(val[val.length - 1])
        //    themeColor = val[(int)(Math.random() * val.length)];

        int resId = R.style.AppTheme_Grey;
        if(themeColor == val.getColor(0, 0)) {
            resId = R.style.AppTheme_Red;
        } else if(themeColor == val.getColor(1, 0)) {
            resId = R.style.AppTheme_Pink;
        } else if(themeColor == val.getColor(2, 0)) {
            resId = R.style.AppTheme_Purple;
        } else if(themeColor == val.getColor(3, 0)) {
            resId = R.style.AppTheme_DeepPurple;
        } else if(themeColor == val.getColor(4, 0)) {
            resId = R.style.AppTheme_Indigo;
        } else if(themeColor == val.getColor(5, 0)) {
            resId = R.style.AppTheme_Blue;
        } else if(themeColor == val.getColor(6, 0)) {
            resId = R.style.AppTheme_LightBlue;
        } else if(themeColor == val.getColor(7, 0)) {
            resId = R.style.AppTheme_Cyan;
        } else if(themeColor == val.getColor(8, 0)) {
            resId = R.style.AppTheme_Teal;
        } else if(themeColor == val.getColor(9, 0)) {
            resId = R.style.AppTheme_Green;
        } else if(themeColor == val.getColor(10, 0)) {
            resId = R.style.AppTheme_LightGreen;
        } else if(themeColor == val.getColor(11, 0)) {
            resId = R.style.AppTheme_Lime;
        } else if(themeColor == val.getColor(12, 0)) {
            resId = R.style.AppTheme_Yellow;
        } else if(themeColor == val.getColor(13, 0)) {
            resId = R.style.AppTheme_Amber;
        } else if(themeColor == val.getColor(14, 0)) {
            resId = R.style.AppTheme_Orange;
        } else if(themeColor == val.getColor(15, 0)) {
            resId = R.style.AppTheme_DeepOrange;
        } else if(themeColor == val.getColor(16, 0)) {
            resId = R.style.AppTheme_Brown;
        } else if(themeColor == val.getColor(17, 0)) {
            resId = R.style.AppTheme_Grey;
        } else if(themeColor == val.getColor(18, 0)) {
            resId = R.style.AppTheme_BlueGrey;
        }
        return resId;
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static void enableTranslucent(Activity ctx) {
        if(ctx == null) return;
        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            ctx.getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            ctx.getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    public static TransitionDrawable getBitmapResourceDrawables(Context ctx, int... resIds) {
        Drawable[] layers = new Drawable[resIds.length];
        for(int i = 0; i < resIds.length; i++)
            layers[i] = new BitmapDrawable(ctx.getResources(), BitmapFactory.decodeResource(ctx.getResources(), resIds[i]));
        return new TransitionDrawable(layers);
    }

    public static void toggleTransitionDrawable(ImageView view, boolean transition) {
        TransitionDrawable t = (TransitionDrawable)view.getDrawable();
        if (transition) t.startTransition(125);
        else t.reverseTransition(125);
    }
}
