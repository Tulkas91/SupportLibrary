package it.mm.supportlibrary.ui.component;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.Shape;

public class TextDrawable extends ShapeDrawable {

    private final Paint textPaint;
    private final String text;
    private final int color;

    public TextDrawable(String text, int color) {
        super(new OvalShape());
        this.text = text;
        this.color = color;
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Align.CENTER);
        textPaint.setTextSize(48f);
        getPaint().setColor(color);
    }

    @Override
    protected void onDraw(Shape shape, Canvas canvas, Paint paint) {
        super.onDraw(shape, canvas, paint);

        Rect bounds = getBounds();
        int x = bounds.centerX();
        int y = (int) (bounds.centerY() - ((textPaint.descent() + textPaint.ascent()) / 2));

        canvas.drawText(text, x, y, textPaint);
    }
}
