package com.galaxas0.Quartz.ui;

import android.animation.Animator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.galaxas0.Quartz.R;
import com.galaxas0.Quartz.utils.AnimationUtils;
import com.galaxas0.Quartz.utils.ThemeUtils;

import java.util.Objects;

import java8.util.function.Consumer;

public class LineColorPicker extends View {

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    private int mOrientation = HORIZONTAL;
    private float mOverdrawScale = 0.08f;
    private int[] colors = new int[] {   Color.parseColor("#b8c847"),
            Color.parseColor("#67bb43"), Color.parseColor("#41b691"),
            Color.parseColor("#4182b6"), Color.parseColor("#4149b6"),
            Color.parseColor("#7641b6"), Color.parseColor("#b741a7"),
            Color.parseColor("#c54657"), Color.parseColor("#d1694a"),
            Color.parseColor("#d1904a"), Color.parseColor("#d1c54a")};

    private Paint paint = new Paint();
    private Rect rect = new Rect();

    boolean isColorSelected = false;
    private int selectedColor = colors[0];
    private Consumer<Integer> onColorChanged;
    private int cellSize;

    private boolean isClick = false;
    private int screenW;
    private int screenH;

    public LineColorPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setStyle(Style.FILL);
        if (mOrientation == HORIZONTAL)
            drawHorizontalPicker(canvas);
        else if(mOrientation == VERTICAL)
            drawVerticalPicker(canvas);
    }

    private void drawVerticalPicker(Canvas canvas) {
        rect.left = 0;
        rect.top = 0;
        rect.right = canvas.getWidth();
        rect.bottom = 0;

        int margin = Math.round(canvas.getWidth() * mOverdrawScale);
        for (int color : colors) {
            paint.setColor(color);

            rect.top = rect.bottom;
            rect.bottom += cellSize;

            if (isColorSelected && color == selectedColor) {
                rect.left = 0;
                rect.right = canvas.getWidth();
            } else {
                rect.left = margin;
                rect.right = canvas.getWidth() - margin;
            }
            canvas.drawRect(rect, paint);
        }
    }

    private void drawHorizontalPicker(Canvas canvas) {
        rect.left = 0;
        rect.top = 0;
        rect.right = 0;
        rect.bottom = canvas.getHeight();

        int margin = Math.round(canvas.getHeight() * mOverdrawScale);
        for (int color : colors) {
            paint.setColor(color);

            rect.left = rect.right;
            rect.right += cellSize;

            if (isColorSelected && color == selectedColor) {
                rect.top = 0;
                rect.bottom = canvas.getHeight();
            } else {
                rect.top = margin;
                rect.bottom = canvas.getHeight() - margin;
            }
            canvas.drawRect(rect, paint);
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isClick = true;
                break;
            case MotionEvent.ACTION_UP:
                setSelectedColor(getColorAtXY(event.getX(), event.getY()));
                if (isClick) performClick();
                break;
            case MotionEvent.ACTION_MOVE:
                setSelectedColor(getColorAtXY(event.getX(), event.getY()));
                break;
            case MotionEvent.ACTION_CANCEL:
                isClick = false;
                break;
            case MotionEvent.ACTION_OUTSIDE:
                isClick = false;
                break;
            default:
                break;
        }

        return true;
    }

    private int getColorAtXY(float x, float y) {
        if (mOrientation == HORIZONTAL) {
            int left, right = 0;

            for (int color : colors) {
                left = right;
                right += cellSize;

                if (left <= x && right >= x)
                    return color;
            }
        } else if(mOrientation == VERTICAL) {
            int top,  bottom = 0;

            for (int color : colors) {
                top = bottom;
                bottom += cellSize;

                if (y >= top && y <= bottom)
                    return color;
            }
        }
        return selectedColor;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.selectedColor = this.selectedColor;
        ss.isColorSelected = this.isColorSelected;
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        this.selectedColor = ss.selectedColor;
        this.isColorSelected = ss.isColorSelected;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        screenW = w;
        screenH = h;

        recalcCellSize();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     * Return currently selected color.
     */
    public int getColor() {
        return selectedColor;
    }

    /**
     * Set selected color as color value from palette.
     */
    public void setSelectedColor(int color) {
        if (!containsColor(colors, color))
            return;

        if (!isColorSelected || selectedColor != color) {
            this.selectedColor = color;
            isColorSelected = true;

            invalidate();
            if (onColorChanged != null)
                onColorChanged.accept(color);
        }
    }

    /**
     * Set selected color as index from palete
     */
    public void setSelectedColorPosition(int position) {
        setSelectedColor(colors[position]);
    }

    /**
     * Set picker palette
     */
    public void setColors(int[] colors) {
        Objects.requireNonNull(colors);
        this.colors = colors;

        if (!containsColor(colors, selectedColor))
            selectedColor = colors[0];

        recalcCellSize();
        invalidate();
    }

    public void setColors(TypedArray array) {
        if(array == null)
            return;
        int[] colors = new int[array.length()];
        for(int i = 0; i < array.length(); i++)
            colors[i] = array.getColor(i, 0);
        setColors(colors);
    }

    private int recalcCellSize() {
        if (mOrientation == HORIZONTAL)
            cellSize = Math.round(screenW / (colors.length * 1f));
        else cellSize = Math.round(screenH / (colors.length * 1f));
        return cellSize;
    }

    /**
     * Return current picker palete
     */
    public int[] getColors() {
        return colors;
    }

    /**
     * Return true if palette contains this color
     */
    private boolean containsColor(int[] colors, int c) {
        for (int color : colors)
            if (color == c)
                return true;
        return false;
    }

    /**
     * Set onColorChanged listener
     *
     * @param l consumer
     */
    public void setOnColorChangedListener(Consumer<Integer> l) {
        this.onColorChanged = l;
    }

    private static class SavedState extends BaseSavedState {
        int selectedColor;
        boolean isColorSelected;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.selectedColor = in.readInt();
            this.isColorSelected = in.readInt() == 1;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.selectedColor);
            out.writeInt(this.isColorSelected ? 1 : 0);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}