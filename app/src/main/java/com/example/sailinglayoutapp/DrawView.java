package com.example.sailinglayoutapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class DrawView extends View {

    View startView, endView;
    Paint paint = new Paint();

    public DrawView(Context context, View startView, View endView) {
        super(context);
        this.startView = startView;
        this.endView = endView;
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(8);
    }

    public void onDraw(Canvas canvas) {
        canvas.drawLine(startView.getX() + 58, startView.getY() + 58, endView.getX() + 58, endView.getY() + 58, paint);
    }
}
