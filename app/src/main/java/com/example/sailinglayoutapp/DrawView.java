package com.example.sailinglayoutapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

public class DrawView extends View {

    View startView, endView;
    Paint paint = new Paint();
    float startX, startY, endX, endY;

    public DrawView(Context context, View startView, View endView, int width) {
        super(context);
        this.startView = startView;
        this.endView = endView;
        paint.setColor(getResources().getColor(R.color.colorLayoutLine));
        paint.setStrokeWidth(45);
    }

    public void onDraw(Canvas canvas) {
        setPosition();
        canvas.drawLine(startX, startY, endX, endY, paint);
    }

    private void setPosition() {
        startX = startView.getX() + (startView.getWidth()/2);
        startY = startView.getY() + (startView.getHeight()/2);
        endX = endView.getX() + (endView.getWidth()/2);
        endY = endView.getY() + (endView.getHeight()/2);
    }
}
