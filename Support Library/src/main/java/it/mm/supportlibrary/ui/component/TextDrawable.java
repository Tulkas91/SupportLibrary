package it.mm.supportlibrary.ui.component;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import java.util.Random;

public class TextDrawable extends Drawable {

    private final Paint textPaint;
    private final Paint backgroundPaint;
    private final String text;
    private final Rect textBounds = new Rect();

    public TextDrawable(String text) {
        this.text = text;

        // Imposta il Paint per il testo
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(48f);

        // Imposta il Paint per il cerchio di sfondo
        backgroundPaint = new Paint();
        backgroundPaint.setColor(getRandomColor());
        backgroundPaint.setAntiAlias(true);
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        int centerX = bounds.centerX();
        int centerY = bounds.centerY();
        int radius = Math.min(bounds.width(), bounds.height()) / 2;

        // Disegna il cerchio di sfondo
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint);

        // Calcola la posizione del testo
        textPaint.getTextBounds(text, 0, text.length(), textBounds);
        float textHeight = textBounds.height();
        float textOffsetY = textHeight / 2;

        // Disegna il testo al centro
        canvas.drawText(text, centerX, centerY + textOffsetY, textPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        textPaint.setAlpha(alpha);
        backgroundPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        textPaint.setColorFilter(colorFilter);
        backgroundPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public static int getRandomColor() {
        Random random = new Random();
        int red = random.nextInt(256);   // Valore tra 0 e 255
        int green = random.nextInt(256); // Valore tra 0 e 255
        int blue = random.nextInt(256);  // Valore tra 0 e 255
        return Color.rgb(red, green, blue);
    }
}
