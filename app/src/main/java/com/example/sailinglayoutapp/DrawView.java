package com.example.sailinglayoutapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class DrawView extends View {

    View startView, endView;
    Paint paint = new Paint();
    int buffer;

    public DrawView(Context context, View startView, View endView, int width) {
        super(context);
        this.startView = startView;
        this.endView = endView;
        paint.setColor(getResources().getColor(R.color.colorLayoutLine));
        paint.setStrokeWidth(45);
        buffer = (int) (0.04027778 * width);
    }

    public void onDraw(Canvas canvas) {
        canvas.drawLine(startView.getX() + buffer, startView.getY() + buffer, endView.getX() + buffer, endView.getY() + buffer, paint);
    }
}
