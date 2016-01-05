package com.galaxas0.Quartz.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;

import com.galaxas0.Quartz.R;

public class LineColorPickerPreference extends DialogPreference {

    private int[] colors = null;

    public LineColorPickerPreference(Context context, AttributeSet attr) {
        super(context, attr);
        setDialogLayoutResource(R.layout.color_preference);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder.setTitle(""));
    }

    @Override
    public void onBindDialogView(View view) {
        LineColorPicker colorPicker = (LineColorPicker) view.findViewById(R.id.color_picker);
        if(colors != null)
            colorPicker.setColors(colors);

        colorPicker.setSelectedColor(getSharedPreferences().getInt(getKey(), 0));
        colorPicker.setOnColorChangedListener(color -> getEditor().putInt(getKey(), color).apply());
        super.onBindDialogView(view);
    }

    public void setColors(int[] colors) {
        if(getDialog() != null) {
            LineColorPicker colorPicker = (LineColorPicker) getDialog().findViewById(R.id.color_picker);
            colorPicker.setColors((this.colors = colors));
        } else {
            this.colors = colors;
        }
    }

    public void setColors(TypedArray array) {
        if(array == null)
            return;
        int[] colors = new int[array.length()];
        for(int i = 0; i < array.length(); i++)
            colors[i] = array.getColor(i, 0);
        setColors(colors);
    }
}
