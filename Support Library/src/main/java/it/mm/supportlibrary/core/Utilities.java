package it.mm.supportlibrary.core;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.mikepenz.iconics.IconicsDrawable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import it.mm.supportlibrary.Application;

/**
 * Created by Marco Mezzasalma on 18/09/24.
 * Copyright (c) 2024 Dott. Marco Mezzasalma. All rights reserved.
 */
public class Utilities {

    public static volatile DispatchQueue globalQueue = new DispatchQueue("globalQueue");
    public static volatile DispatchQueue importQueue = new DispatchQueue("importQueue");
    private static HashMap<Integer, File> uploadsFiles = new HashMap<>();
    private static HashMap<Integer, String> uploadsKeys = new HashMap<>();
    private static HashMap<Integer, String> uploadsBelfiori = new HashMap<>();
    private static HashMap<Integer, String> uploadsHosts = new HashMap<>();

    public static void downloadFileUpdate(Handler mApplicationHandler, Context context, String url, String fileName, boolean installAPK, String authority) {
        // Usa ProgressBar o altri metodi più moderni se preferito, ma manteniamo ProgressDialog per semplicità
        final ProgressDialog progressBarDialog = new ProgressDialog(context);
        progressBarDialog.setCancelable(false);
        progressBarDialog.setTitle("Scarico file in corso, attendere...");
        progressBarDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressBarDialog.setProgress(0);
        progressBarDialog.show();

        // Gestisci il download in un ExecutorService o HandlerThread, migliore gestione dei thread
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.submit(() -> {
            DownloadManager.Request dmr = new DownloadManager.Request(Uri.parse(url));
            dmr.setTitle(fileName);
            dmr.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            dmr.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION);
            dmr.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);

            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            long downloadManagerId = manager.enqueue(dmr);

            boolean downloading = true;
            while (downloading) {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadManagerId);
                Cursor cursor = null;
                try {
                    cursor = manager.query(query);
                    if (cursor != null && cursor.moveToFirst()) {
                        int statusColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        int bytesDownloadedColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                        int bytesTotalColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);

                        // Verifica che gli indici delle colonne siano validi prima di accedere ai dati
                        if (statusColumnIndex != -1 && bytesDownloadedColumnIndex != -1 && bytesTotalColumnIndex != -1) {
                            int status = cursor.getInt(statusColumnIndex);
                            int bytesDownloaded = cursor.getInt(bytesDownloadedColumnIndex);
                            int bytesTotal = cursor.getInt(bytesTotalColumnIndex);

                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                downloading = false;

                                AndroidUtilities.runOnUIThread(mApplicationHandler, () -> {
                                    progressBarDialog.dismiss();
                                    if (installAPK) {
                                        startInstallApk(context, authority, fileName);
                                    }
                                });
                            }

                            if (bytesTotal > 0) {
                                final int progress = (int) ((bytesDownloaded * 100L) / bytesTotal);
                                AndroidUtilities.runOnUIThread(mApplicationHandler, () -> progressBarDialog.setProgress(progress));
                            }
                        } else {
                            // Gestisci il caso in cui una o più colonne non siano presenti
                            Toast.makeText(context, "Una delle colonne non è presente nel Cursor.", Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (cursor != null) {
                        cursor.close(); // Assicurati di chiudere il cursor
                    }
                }

                try {
                    Thread.sleep(500); // Piccola pausa per evitare il polling continuo
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        executorService.shutdown(); // Assicura che l'ExecutorService venga chiuso una volta completato il task
    }

    private static void startInstallApk(Context context, String authority, String fileName) {
        Uri apkURI = FileProvider.getUriForFile(context, authority, new File(Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName));
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apkURI, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
        ContextCompat.startActivity(context, intent, null);
    }

    public static IconicsDrawable setAndGetIconicsDrawable(Context context, String iconName, int color, int size, int padding) {
        IconicsDrawable icon = new IconicsDrawable(context, iconName);
        icon.setColorList(ColorStateList.valueOf(ContextCompat.getColor(context, color)));
        icon.setSizeXPx(AndroidUtilities.dpToPx(size));
        icon.setSizeYPx(AndroidUtilities.dpToPx(size));
        icon.setPaddingPx(AndroidUtilities.dpToPx(padding));
        return icon;
    }

    // Funzione per aprire l'app di supporto remoto
    public static void openRemoteSupportApp(Context context) {
        PackageManager packageManager = context.getPackageManager();

        // Definizione dei package delle app
        String quickSupportPackage = "com.teamviewer.quicksupport.market";
        String anyDeskPackage = "com.anydesk.anydeskandroid";

        // Verifica se TeamViewer QuickSupport è installato
        boolean isQuickSupportInstalled = isAppInstalled(packageManager, quickSupportPackage);

        // Verifica se AnyDesk è installato
        boolean isAnyDeskInstalled = isAppInstalled(packageManager, anyDeskPackage);

        if (isQuickSupportInstalled) {
            // Apri TeamViewer QuickSupport
            Intent intent = packageManager.getLaunchIntentForPackage(quickSupportPackage);
            if (intent != null) {
                context.startActivity(intent);
            } else {
                // Se non è possibile aprire QuickSupport, gestisci l'errore
                showError(context, "Non è possibile aprire QuickSupport.");
            }
        } else if (isAnyDeskInstalled) {
            // Apri AnyDesk
            Intent intent = packageManager.getLaunchIntentForPackage(anyDeskPackage);
            if (intent != null) {
                context.startActivity(intent);
            } else {
                // Se non è possibile aprire AnyDesk, gestisci l'errore
                showError(context, "Non è possibile aprire AnyDesk.");
            }
        } else {
            // Nessuna app installata, gestisci il caso
            showError(context, "Né QuickSupport né AnyDesk sono installati.");
        }
    }

    // Funzione per verificare se l'app è installata
    private static boolean isAppInstalled(PackageManager packageManager, String packageName) {
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return packageInfo != null;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    // Funzione per mostrare un messaggio di errore
    private static void showError(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void updateHeightofListView(ListView listView) {

        Adapter mAdapter = listView.getAdapter();

        int totalHeight = 0;

        for (int i = 0; i < mAdapter.getCount(); i++) {
            View mView = mAdapter.getView(i, null, listView);

            mView.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),

                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

            totalHeight += mView.getMeasuredHeight();
            //Log.w("HEIGHT" + i, String.valueOf(totalHeight));
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (mAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();

    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeFile(String file, int reqWidth, int reqHeight) {
        //try {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inDither = true;
        return BitmapFactory.decodeFile(file, options);
       /* } catch (OutOfMemoryError ex) {
            FileLog.e("yesapp21", ex);
        }
        return null;*/
    }

    public static boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {

            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean checkInternetConnection(Context context) {
        int[] networkTypes = {ConnectivityManager.TYPE_MOBILE,
                ConnectivityManager.TYPE_WIFI, ConnectivityManager.TYPE_ETHERNET};
        try {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            for (int networkType : networkTypes) {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetworkInfo != null &&
                        activeNetworkInfo.getType() == networkType) {
                    if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_ETHERNET)
                        return isOnline();
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public static String getStackTrace(Throwable aThrowable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        aThrowable.printStackTrace(printWriter);
        return result.toString();
    }

    public static String padLeftZeros(String str, int n) {
        return String.format("%1$" + n + "s", str).replace(' ', '0');
    }

    public static void difference(Object s1, Object s2, List<String> changedProperties, String parent) throws IllegalAccessException {
        for (Field field : s1.getClass().getDeclaredFields()) {
            if (parent == null) {
                parent = s1.getClass().getSimpleName();
            }
            field.setAccessible(true);
            Object value1 = field.get(s1);
            Object value2 = field.get(s2);
            if (value1 == null && value2 == null) {
                continue;
            }
            if (value1 != null && isBaseType(value1.getClass())) {
                if (value2 == null) {
                    changedProperties.add(parent + "." + field.getName());
                } else if (!Objects.equals(value1, value2)) {
                    changedProperties.add(parent + "." + field.getName());
                }
            }
        }
    }

    private static final Set<Class> BASE_TYPES = new HashSet<>(Arrays.asList(
            String.class, Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Void.class));

    public static boolean isBaseType(Class clazz) {
        return BASE_TYPES.contains(clazz);
    }

    public static synchronized Bitmap getImageBitmap(Context context, String name) {

        Bitmap b = null;

        try {
            FileInputStream fis = context.openFileInput(name);
            //b = Utilities.decodeFile(avatarFile.getAbsolutePath(), 2048, 2048);
            b = BitmapFactory.decodeStream(fis);
            fis.close();
        } catch (FileNotFoundException e) {
            FileLog.e(BuildVars.TAG, e);
        } catch (IOException e) {
            FileLog.e(BuildVars.TAG, e);
        }

        return b;
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void zipFile(String inputPath, String outZipPath) {
        try {
            File srcFile = new File(inputPath);

            File[] files = null;
            if (srcFile.isDirectory()) {
                files = srcFile.listFiles();
            } else {
                files = new File[1];
                files[0] = srcFile;
            }

            if (files == null || files.length == 0)
                return;

            FileOutputStream fos = new FileOutputStream(outZipPath);
            ZipOutputStream zos = new ZipOutputStream(fos);


            for (int i = 0; i < files.length; i++) {
                Log.d("", "Adding file: " + files[i].getName());
                byte[] buffer = new byte[1024];
                FileInputStream fis = new FileInputStream(files[i]);
                zos.putNextEntry(new ZipEntry(files[i].getName()));
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
                fis.close();
            }
            zos.close();
        } catch (IOException ioe) {
            Log.e("", ioe.getMessage());
        }
    }

    /**
     * @param thisDate
     * @param thatDate
     * @param maxDays  set to -1 to not set a max
     * @returns number of days covered between thisDate and thatDate, inclusive, i.e., counting both
     * thisDate and thatDate as an entire day. Will short out if the number of days exceeds
     * or meets maxDays
     */
    public static int daysCoveredByDates(Date thisDate, Date thatDate, int maxDays) {
        //Check inputs
        if (thisDate == null || thatDate == null) {
            return -1;
        }

        //Set calendar objects
        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();
        if (thisDate.before(thatDate)) {
            startCal.setTime(thisDate);
            endCal.setTime(thatDate);
        } else {
            startCal.setTime(thatDate);
            endCal.setTime(thisDate);
        }

        //Get years and dates of our times.
        int startYear = startCal.get(Calendar.YEAR);
        int endYear = endCal.get(Calendar.YEAR);
        int startDay = startCal.get(Calendar.DAY_OF_YEAR);
        int endDay = endCal.get(Calendar.DAY_OF_YEAR);

        //Calculate the number of days between dates.  Add up each year going by until we catch up to endDate.
        while (startYear < endYear && maxDays >= 0 && endDay - startDay + 1 < maxDays) {
            endDay += startCal.getActualMaximum(Calendar.DAY_OF_YEAR); //adds the number of days in the year startDate is currently in
            ++startYear;
            startCal.set(Calendar.YEAR, startYear); //reup the year
        }
        int days = endDay - startDay + 1;

        //Honor the maximum, if set
        if (maxDays >= 0) {
            days = Math.min(days, maxDays);
        }
        return days;
    }

    public static String getSdCardPath() {

        return Application.Companion.getPrefs().getString("files_path", "");
//        return Environment.getExternalStorageDirectory().getPath() + "/";
//        return getExternalSdCard().getAbsolutePath() + "/";
    }

    public static boolean isAvailable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    static class Sortbyname implements Comparator<File> {
        public int compare(File a, File b) {
            if (a.getName().contains("DIMMA") || b.getName().contains("DIMMA"))
                return 0;
            return Integer.parseInt(a.getName().split("_")[0]) - Integer.parseInt(b.getName().split("_")[0]);
        }
    }

    public static File getAbsolutePath() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        return new File("/storage/emulated/0/Documents");
//        else
//            return new File(Environment.getExternalStorageDirectory().toString());
    }

    public static boolean execTerminalCommand(String command) {
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);

            try {
                p.waitFor();
                return p.exitValue() != 255;
            } catch (Exception e) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static double bytesToHuman(Long size) {
        long Kb = 1 * 1024;
        long Mb = Kb * 1024;
        long Gb = Mb * 1024;
        long Tb = Gb * 1024;
        long Pb = Tb * 1024;
        long Eb = Pb * 1024;

        if (size < Kb) return Double.parseDouble(new DecimalFormat("#.##").format(size));
        if (size >= Kb && size < Mb)
            return Double.parseDouble(new DecimalFormat("#.##").format(((double) size / Kb)));
        if (size >= Mb && size < Gb)
            return Double.parseDouble(new DecimalFormat("#.##").format(((double) size / Mb)));
        if (size >= Gb && size < Tb)
            return Double.parseDouble(new DecimalFormat("#.##").format(((double) size / Gb)));
        if (size >= Tb && size < Pb)
            return Double.parseDouble(new DecimalFormat("#.##").format(((double) size / Tb)));
        if (size >= Pb && size < Eb)
            return Double.parseDouble(new DecimalFormat("#.##").format(((double) size / Pb)));
        if (size >= Eb)
            return Double.parseDouble(new DecimalFormat("#.##").format(((double) size / Eb)));

        return 0;
    }

    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return file.delete();
        } else {
            return false;
        }
    }

    public static void moveFile(String inputPath, String inputFile, String outputPath) {

        InputStream in = null;
        OutputStream out = null;
        try {

            //create output directory if it doesn't exist
            File dir = new File(outputPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }


            in = new FileInputStream(inputPath + inputFile);
            out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;

            // delete the original file
            new File(inputPath + inputFile).delete();


        } catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }

    public static void moveFile(String inputPath, String outputPath) {

        InputStream in = null;
        OutputStream out = null;
        try {

            //create output directory if it doesn't exist
            File dir = new File(outputPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }


            in = new FileInputStream(inputPath);
            out = new FileOutputStream(outputPath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;

            // delete the original file
//            new File(inputPath + inputFile).delete();


        } catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }
}