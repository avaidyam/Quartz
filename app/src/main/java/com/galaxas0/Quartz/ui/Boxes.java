package com.galaxas0.Quartz.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class Boxes extends View {

    private int rows, columns;
    private int boxInset = 2, boxColor = Color.BLACK;

    private Paint paint = new Paint();
    private Rect rect = new Rect();

    public Boxes(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(boxColor);

        int width = canvas.getWidth() / columns, height = canvas.getHeight() / rows;
        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < columns; j++) {
                rect.set(width * j, height * i, width * (j + 1), height * (i + 1));
                rect.inset(boxInset, boxInset);
                canvas.drawRect(rect, paint);
            }
        }
    }

    // ...

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getRows() {
        return rows;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public int getColumns() {
        return columns;
    }

    public void setBoxInset(int boxInset) {
        this.boxInset = boxInset;
    }

    public int getBoxInset() {
        return boxInset;
    }

    public void setBoxColor(int boxColor) {
        this.boxColor = boxColor;
    }

    public int getBoxColor() {
        return boxColor;
    }
}
