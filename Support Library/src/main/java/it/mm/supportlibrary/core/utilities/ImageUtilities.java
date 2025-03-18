package it.mm.supportlibrary.core.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.util.Log;

import androidx.exifinterface.media.ExifInterface;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Locale;

import it.mm.supportlibrary.core.time.FastDateFormat;

/**
 * Created by Marco Mezzasalma on 18/03/25.
 * Copyright (c) 2025 Dott. Marco Mezzasalma. All rights reserved.
 */

public final class ImageUtilities {

    private static final String TAG = "ImageUtilities";

    // Costruttore privato per evitare istanziazioni
    private ImageUtilities() {
    }

    public static Bitmap processBitmaps(Context context, Uri uri, int reqWidth, int reqHeight, String imagePath, float scaleFactor, float x, float y, float textSize, int color) {
        Bitmap bitmap = decodeSampledBitmapFromUri(context, uri, reqWidth, reqHeight);
        if (bitmap != null) {
            if (imagePath != null) {
                bitmap = ImageUtilities.rotateBitmapIfNeeded(bitmap, imagePath);
            }
            int newWidth = (int) (bitmap.getWidth() * scaleFactor);
            int newHeight = (int) (bitmap.getHeight() * scaleFactor);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

            String watermarkText = FastDateFormat.getInstance("dd/MM/yyyy HH:mm", Locale.ITALY).format(new Date());
            scaledBitmap = ImageUtilities.addWatermark(scaledBitmap, watermarkText, x, y, textSize, color);
            return scaledBitmap;
        } else {
            return null;
        }
    }

    /**
     * Decodifica un'immagine da un Uri in maniera scalata, per evitare OutOfMemoryError
     *
     * @param context   contesto Android
     * @param uri       Uri dell'immagine (file, media store, ecc.)
     * @param reqWidth  larghezza desiderata (in px)
     * @param reqHeight altezza desiderata (in px)
     * @return una Bitmap ridimensionata in memoria, oppure null se non decodificabile
     */
    public static Bitmap decodeSampledBitmapFromUri(Context context, Uri uri, int reqWidth, int reqHeight) {
        if (uri == null) {
            return null;
        }

        InputStream is = null;
        try {
            // 1) Leggere solo i metadati (dimensioni) senza caricare la bitmap in memoria
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            is = context.getContentResolver().openInputStream(uri);
            BitmapFactory.decodeStream(is, null, options);

            if (is != null) {
                is.close();
            }

            // 2) Calcolare un inSampleSize appropriato rispetto ai requisiti
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // 3) Decodifica effettiva con la riduzione
            options.inJustDecodeBounds = false;
            is = context.getContentResolver().openInputStream(uri);

            return BitmapFactory.decodeStream(is, null, options);
        } catch (IOException e) {
            Log.e(TAG, "Errore nella decodifica dell'immagine: " + e.getMessage(), e);
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * Calcola il fattore di riduzione (inSampleSize) in base alle dimensioni richieste.
     *
     * @param options   oggetto BitmapFactory.Options con outWidth e outHeight letti
     * @param reqWidth  larghezza richiesta
     * @param reqHeight altezza richiesta
     * @return inSampleSize (1, 2, 4, 8...)
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Aumenta inSampleSize finché l'immagine "scaled" non rientra nei limiti
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * Ruota la bitmap in base ai metadati EXIF.
     * NOTA: Usa il percorso del file (imagePath) o un Uri che punti a un file leggibile da ExifInterface.
     *
     * @param bitmap     la bitmap originale
     * @param imagePath  percorso fisico del file (es. /storage/emulated/0/DCIM/Camera/xxx.jpg)
     * @return la bitmap ruotata se necessario, altrimenti la bitmap originale
     */
    public static Bitmap rotateBitmapIfNeeded(Bitmap bitmap, String imagePath) {
        if (bitmap == null || imagePath == null) {
            return bitmap;
        }
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            int rotate = switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90 -> 90;
                case ExifInterface.ORIENTATION_ROTATE_180 -> 180;
                case ExifInterface.ORIENTATION_ROTATE_270 -> 270;
                default -> 0;
            };

            if (rotate != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotate);
                Bitmap rotatedBitmap = Bitmap.createBitmap(
                        bitmap,
                        0,
                        0,
                        bitmap.getWidth(),
                        bitmap.getHeight(),
                        matrix,
                        true
                );
                // Se vuoi liberare la bitmap precedente
                bitmap.recycle();
                return rotatedBitmap;
            } else {
                return bitmap;
            }
        } catch (IOException e) {
            Log.e(TAG, "rotateBitmapIfNeeded: " + e.getMessage(), e);
            return bitmap;
        }
    }

    /**
     * Crea una nuova bitmap con una watermark di testo.
     *
     * @param src      bitmap sorgente
     * @param text     testo da disegnare
     * @param x        posizione X (in px)
     * @param y        posizione Y (in px)
     * @param textSize dimensione testo in px
     * @param color    colore del testo
     * @return nuova bitmap con testo sovrapposto
     */
    public static Bitmap addWatermark(Bitmap src, String text, float x, float y, float textSize, int color) {
        if (src == null) {
            return null;
        }

        // Creiamo una copia modificabile
        Bitmap result = src.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(result);

        // Paint
        Paint tPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tPaint.setTextSize(textSize);
        tPaint.setColor(color);
        tPaint.setStyle(Paint.Style.FILL);

        tPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        tPaint.setColor(Color.BLACK);
        tPaint.setStrokeWidth(2);
        canvas.drawText(text, x, y, tPaint);

        // Ripristina colore
        tPaint.setColor(color);
        tPaint.setStrokeWidth(0);

        // Disegniamo il testo
        canvas.drawText(text, x, y, tPaint);

        return result;
    }

    /**
     * Esempio di compressione e salvataggio su byte[] per eventuale upload o memorizzazione.
     *
     * @param bitmap  bitmap da comprimere
     * @param quality qualità JPEG (0-100)
     * @return array di byte contenente l'immagine in formato JPEG
     */
    public static byte[] compressBitmapToJpegBytes(Bitmap bitmap, int quality) {
        if (bitmap == null) {
            return null;
        }
        try {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
            return bos.toByteArray();
        } catch (Exception e) {
            Log.e(TAG, "Errore durante compressione JPEG: " + e.getMessage(), e);
            return null;
        }
    }
}
