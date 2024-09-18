package it.mm.support_library.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ListView;
import android.widget.Toast;

import com.github.pwittchen.prefser.library.Prefser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import it.sikuel.googlegeocoderapp.GoogleGeocoderApplication;
import it.sikuel.googlegeocoderapp.model.Task;

/**
 * Created by Marco Mezzasalma on 19/08/24.
 * Copyright (c) 2024 Dott. Marco Mezzasalma. All rights reserved.
 */
public class Utilities {

    public static volatile DispatchQueue importQueue = new DispatchQueue("importQueue");
    private static HashMap<Integer, File> uploadsFiles = new HashMap<Integer, File>();
    private static HashMap<Integer, String> uploadsKeys = new HashMap<Integer, String>();
    private static HashMap<Integer, String> uploadsBelfiori = new HashMap<Integer, String>();
    private static HashMap<Integer, String> uploadsHosts = new HashMap<Integer, String>();

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
//        if (KTaripApplication.getInstance().getPrefser().getPreferences().getBoolean("prefIntervalOnline", false)) {
//            Date date = new Date();
//            Calendar calendar = GregorianCalendar.getInstance();
//            calendar.setTime(date);
//            int hour = calendar.get(Calendar.HOUR_OF_DAY);
//
//            if (!(hour == 13 || hour == 14 || hour >= 18)) {
//                return false;
//            }
//        }

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
//            if (value1 == null || value2 == null) {
//                changedProperties.add(parent + "." + field.getName());
//            } else {
//                if (isBaseType(value1.getClass())) {
//                    if (!Objects.equals(value1, value2)) {
//                        changedProperties.add(parent + "." + field.getName());
//                    }
//                } else {
//                    difference(value1, value2, changedProperties, parent + "." + field.getName());
//                }
//            }
        }
    }

    private static final Set<Class> BASE_TYPES = new HashSet<>(Arrays.asList(
            String.class, Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Void.class));

    public static boolean isBaseType(Class clazz) {
        return BASE_TYPES.contains(clazz);
    }

//    public static void downloadAnydesk(String typeOf, String name) {
//        if (Utilities.checkInternetConnection(KTaripApplication.getAppContext())) {
//            Response.Listener mListener = new Response.Listener<JSONObject>() {
//                @Override
//                public void onResponse(JSONObject o) {
//                    try {
//                        if (o.getBoolean("app_found")) {
//                            String link = o.getString("link_attachment");
//                            AndroidUtilities.runOnUIThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Utilities.execTerminalCommand("rm /sdcard/Download/" + name + "\n");
//                                    downloadFileUpdate(KTaripApplication.getServerUrl() + link, name, true);
//                                }
//                            });
//                        } else {
//                            AndroidUtilities.runOnUIThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    AlertDialog materialAlertDialog = new MaterialAlertDialogBuilder(AndroidUtilities.getActivity(), R.style.ThemeOverlay_Catalog_MaterialAlertDialog_FilledButton)
//                                            .setTitle("Nessuna app trovata")
//                                            .setMessage("Impossibile scaricare il file. Contattare l'assistenza.")
//                                            .setCancelable(false).create();
//                                    materialAlertDialog.show();
//
//                                    final Handler handler = new Handler();
//                                    final Runnable runnable = new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            if (materialAlertDialog.isShowing()) {
//                                                materialAlertDialog.dismiss();
//                                            }
//                                        }
//                                    };
//
//                                    materialAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                                        @Override
//                                        public void onDismiss(DialogInterface dialog) {
//                                            handler.removeCallbacks(runnable);
//                                        }
//                                    });
//
//                                    handler.postDelayed(runnable, 2500);
//                                }
//                            });
//                        }
//                    } catch (JSONException ex) {
//                        FileLog.e(BuildVars.TAG, ex);
//                    }
//                }
//            };
//
//            Response.ErrorListener mErrorListener = new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError volleyError) {
//                    final NetworkResponse response = volleyError.networkResponse;
//                    AndroidUtilities.runOnUIThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (response != null && response.statusCode == 500) {
//                                AlertDialog materialAlertDialog = new MaterialAlertDialogBuilder(AndroidUtilities.getActivity(), R.style.ThemeOverlay_Catalog_MaterialAlertDialog_FilledButton)
//                                        .setTitle("Errore nel server")
//                                        .setMessage("Contattare l'assistenza.")
//                                        .setCancelable(false).create();
//                                materialAlertDialog.show();
//
//                                final Handler handler = new Handler();
//                                final Runnable runnable = new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        if (materialAlertDialog.isShowing()) {
//                                            materialAlertDialog.dismiss();
//                                        }
//                                    }
//                                };
//
//                                materialAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                                    @Override
//                                    public void onDismiss(DialogInterface dialog) {
//                                        handler.removeCallbacks(runnable);
//                                    }
//                                });
//
//                                handler.postDelayed(runnable, 2500);
//                                return;
//                            }
//                            try {
//                                String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
//                                if (jsonString.length() != 0) {
//                                    JSONObject jsonObj = new JSONObject(jsonString);
//                                    JSONArray errors = jsonObj.getJSONArray("errors");
//                                    jsonString = errors.getJSONObject(0).getString("detail");
//                                }
//                            } catch (JSONException ex) {
//                                FileLog.e("KWaterPro", ex);
//                            } catch (UnsupportedEncodingException ex) {
//                                FileLog.e("KWaterPro", ex);
//                            } catch (NullPointerException ex) {
//                                FileLog.e("KWaterPro", ex);
//                            }
//                        }
//                    });
//                }
//            };
//
//            RequestManager.queue().useBackgroundQueue().clearCache();
//            RequestManager.queue().useBackgroundQueue().addRequest(new DownloaAnydeskRequest(typeOf), mListener, mErrorListener);
//        }
//    }
//
//    public static void downloadFileUpdate(String url, String fileName, boolean installAPK) {
//        final ProgressDialog progressBarDialog = new ProgressDialog(AndroidUtilities.getActivity());
//        progressBarDialog.setCancelable(false);
//        progressBarDialog.setTitle("Scarico file in corso, attendere...");
//
//        progressBarDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//        progressBarDialog.setProgress(0);
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                boolean downloading = true;
//                DownloadManager.Request dmr = new DownloadManager.Request(Uri.parse(url));
//                dmr.setTitle(fileName);
//                dmr.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
//                dmr.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION);
//                dmr.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
//                DownloadManager manager = (DownloadManager) AndroidUtilities.getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
//                long downloadManagerId = manager.enqueue(dmr);
//                while (downloading) {
//                    DownloadManager.Query q = new DownloadManager.Query();
//                    q.setFilterById(downloadManagerId); //filter by id which you have receieved when reqesting download from download manager
//                    Cursor cursor = manager.query(q);
//                    cursor.moveToFirst();
//                    int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
//                    int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
//
//                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
//                        downloading = false;
//                        progressBarDialog.dismiss();
//
//                        if (installAPK)
//                            startInstallApk(fileName);
//                    }
//
//                    final int dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);
//
//                    AndroidUtilities.getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            progressBarDialog.setProgress((int) dl_progress);
//                        }
//                    });
//                    cursor.close();
//                }
//
//            }
//        }).start();
//
//        progressBarDialog.show();
//    }
//
//    private static void startInstallApk(String fileName) {
//        Uri apkURI = FileProvider.getUriForFile(AndroidUtilities.getActivity(), "it.sikuel.ktarippro.fileprovider", new File(getSdCardPath() + "/Download/" + fileName));
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setDataAndType(apkURI, "application/vnd.android.package-archive");
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
//        AndroidUtilities.getActivity().startActivity(intent);
//    }
//
//    public static void clearMemory() {
//        String targetPdf = "//data//" + KTaripApplication.getAppContext().getPackageName()
//                + "//files";
//        File dir = new File(Environment.getDataDirectory(), targetPdf);
//        ArrayList<File> files = new ArrayList<File>();
//
//        Stack<File> dirlist = new Stack<File>();
//        dirlist.clear();
//        dirlist.push(dir);
//
//        while (!dirlist.isEmpty()) {
//            File dirCurrent = dirlist.pop();
//
//            File[] fileList = dirCurrent.listFiles();
//            for (File aFileList : fileList) {
//                if (aFileList.isDirectory())
//                    dirlist.push(aFileList);
//                else {
//                    files.add(aFileList);
//
//                }
//            }
//        }
//
//        Calendar time = Calendar.getInstance();
//        time.add(Calendar.DAY_OF_YEAR, -7);
//        Double totMemoryCleared = 0.0;
//        for (File f : files) {
//            Date lastModified = new Date(f.lastModified());
//            if (lastModified.before(time.getTime())) {
//                //file is older than a week
//                totMemoryCleared += Double.parseDouble(String.valueOf(f.length() / 1024));
//                f.delete();
//            }
//        }
//        Toast.makeText(KTaripApplication.getAppContext(), String.format("Totale memoria liberata %.2f MB", totMemoryCleared / 1024), Toast.LENGTH_LONG).show();
//    }

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
        final Prefser prefser = GoogleGeocoderApplication.Companion.getInstance().getPrefser();

        return prefser.getPreferences().getString("files_path", null);
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

    public static int calculateTotalElementFromFile(String originalPath) {
        int totalElement = 0;
        try {
            File directory = new File(originalPath);
            if (directory.exists()) {
                try {
                    FileInputStream is = new FileInputStream(directory);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String line = reader.readLine();
                    line = reader.readLine();
                    int i = 0;
                    while (line != null) {
                        totalElement++;
                        line = reader.readLine();
                    }
                } catch (Exception e) {
                    FileLog.e(BuildVars.TAG, e);
                    AndroidUtilities.showNotification("Errore", "Calcolo elementi terminato con errore.");
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(GoogleGeocoderApplication.Companion.getAppContext(), "Calcolo elementi terminato con errore.", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        } catch (Exception e) {
            FileLog.e(BuildVars.TAG, e);
            AndroidUtilities.showNotification("Errore", "Calcolo elementi terminato con errore.");
            AndroidUtilities.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(GoogleGeocoderApplication.Companion.getAppContext(), "Calcolo elementi terminato con errore.", Toast.LENGTH_LONG).show();
                }
            });
        }
        return totalElement;
    }

    public static void geoCodeBuildingFromFile(Task task, String originalPath, String destinationPath) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) GoogleGeocoderApplication.Companion.getInstance().getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ktarip:ImportJobTag");
                wakeLock.acquire();
                try {
                    File directory = new File(originalPath);
                    if (directory.exists()) {
                        try {
                            new File(destinationPath).mkdirs();
                            FileWriter myWriter = new FileWriter(destinationPath + "/gps_immobili.csv");
                            FileInputStream is = new FileInputStream(directory);
                            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                            String line = reader.readLine();
                            line = reader.readLine();
                            int i = 0;
                            myWriter.write("id_portale;ubicazione;latitudine;longitudine;comune;provincia\n");
                            while (line != null) {
                                if (i >= task.totalProcessed) {
                                    String[] elements = line.split(",");
                                    String id = elements[0];
                                    String street = "";
                                    if (elements[1].contains("C.LE")) {
                                        street = elements[1].replace("C.LE", "CONSORTILE");
                                    } else if (elements[1].contains("PROV.LE")) {
                                        street = elements[1].replace("PROV.LE", "PROVINCIALE");
                                    } else if (elements[1].contains("CIRC.")) {
                                        street = elements[1].replace("CIRC.", "CIRCONVALLAZIONE");
                                    } else if (elements[1].contains("VAR.")) {
                                        street = elements[1].replace("VAR.", "VARIANTE");
                                    } else {
                                        street = elements[1];
                                    }
                                    String streetNumber = elements.length <= 2 ? "" : elements[2];
                                    String streetSub = elements.length <= 3 ? "" : elements[3];
                                    String comune = elements.length <= 4 ? "" : elements[4];
                                    String provincia = elements.length <= 5 ? "" : elements[5];
                                    Geocoder geocoder = new Geocoder(GoogleGeocoderApplication.Companion.getAppContext());
                                    List<Address> addresses;
                                    try {
                                        addresses = geocoder.getFromLocationName(street + ", " + streetNumber + " " + streetSub + " " + comune, 1);
                                        if (addresses.size() > 0) {
                                            double destinationLatitude = addresses.get(0).getLatitude();
                                            double destinationLongitude = addresses.get(0).getLongitude();
                                            myWriter.write(id + ";" + street + "," + streetNumber + " " + streetSub + ";" + destinationLatitude + ";" + destinationLongitude + ";" + comune + ";" + provincia + "\n");
                                        } else {
                                            myWriter.write(id + ";" + street + "," + streetNumber + " " + streetSub + ";" + ";" + ";" + comune + ";" + provincia + "\n");
                                        }
                                    } catch (IOException e) {

                                    }
                                }
                                line = reader.readLine();
                                i++;
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateMenuFragment, task, i);
                            }
                            myWriter.close();
                            task.endAt = new Date();
                            task.save();
                        } catch (Exception e) {
                            FileLog.e(BuildVars.TAG, e);
                            AndroidUtilities.showNotification("Aggiornamento remoto", "Aggiornamento dati terminato con errore.");
                            task.error = e.toString();
                            task.endAt = new Date();
                            task.save();
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(GoogleGeocoderApplication.Companion.getAppContext(), "Aggiornamento dati terminato con errore.", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    FileLog.e(BuildVars.TAG, e);
                    AndroidUtilities.showNotification("Aggiornamento remoto", "Aggiornamento dati terminato con errore.");
                    task.error = e.toString();
                    task.endAt = new Date();
                    task.save();
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(GoogleGeocoderApplication.Companion.getAppContext(), "Aggiornamento dati terminato con errore.", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        };
        thread.start();
    }

    public static void geoCodeStreetFromFile(Task task, String originalPath, String destinationPath) {
        final Prefser prefser = GoogleGeocoderApplication.Companion.getInstance().getPrefser();

        Utilities.importQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) GoogleGeocoderApplication.Companion.getInstance().getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ktarip:ImportJobTag");
                wakeLock.acquire();

                try {
                    File directory = new File(originalPath);
                    if (directory.exists()) {
                        try {
                            FileWriter myWriter = new FileWriter(destinationPath + "/geocoder_streets.csv");
                            FileInputStream is = new FileInputStream(directory);
                            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                            String line = reader.readLine();
                            line = reader.readLine();
                            int i = 0;
                            while (line != null) {
                                if (i >= 0) {
                                    String[] elements = line.split(";");
                                    String id = elements[0];
                                    String street = "";
                                    if (elements[1].contains("C.LE")) {
                                        street = elements[1].replace("C.LE", "CONSORTILE");
                                    } else if (elements[1].contains("PROV.LE")) {
                                        street = elements[1].replace("PROV.LE", "PROVINCIALE");
                                    } else if (elements[1].contains("CIRC.")) {
                                        street = elements[1].replace("CIRC.", "CIRCONVALLAZIONE");
                                    } else if (elements[1].contains("VAR.")) {
                                        street = elements[1].replace("VAR.", "VARIANTE");
                                    } else {
                                        street = elements[1];
                                    }
                                    String comune = elements.length <= 2 ? "" : elements[2];
                                    String billing_code = elements.length <= 3 ? "" : elements[3];
                                    Geocoder geocoder = new Geocoder(GoogleGeocoderApplication.Companion.getAppContext());
                                    List<Address> addresses;
                                    try {
                                        addresses = geocoder.getFromLocationName(street + " " + comune, 1);
                                        if (addresses.size() > 0) {
                                            double destinationLatitude = addresses.get(0).getLatitude();
                                            double destinationLongitude = addresses.get(0).getLongitude();

                                            myWriter.write(id + ";" + billing_code + ";" + street + ";" + comune + ";" + destinationLatitude + ";" + destinationLongitude + ";" + addresses.get(0).getAddressLine(0) + ";" + addresses.get(0).getThoroughfare() + "\n");
                                        } else {
                                            myWriter.write(id + ";" + billing_code + ";" + street + ";" + comune + ";" + ";" + "\n");
                                        }
                                    } catch (IOException e) {

                                    }
                                }
                                line = reader.readLine();
                                i++;
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.totalEvents++, i);
//                                if (i == 25000) {
//                                    myWriter.close();
//                                    break;
//                                }
                            }
                            myWriter.close();
                            task.endAt = new Date();
                            task.save();
                        } catch (Exception e) {
                            FileLog.e(BuildVars.TAG, e);
                            task.error = e.toString();
                            task.endAt = new Date();
                            task.save();
                            AndroidUtilities.showNotification("Aggiornamento remoto", "Aggiornamento dati terminato con errore.");
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(GoogleGeocoderApplication.Companion.getAppContext(), "Aggiornamento dati terminato con errore.", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    FileLog.e(BuildVars.TAG, e);
                    task.error = e.toString();
                    task.endAt = new Date();
                    task.save();
                    AndroidUtilities.showNotification("Aggiornamento remoto", "Aggiornamento dati terminato con errore.");
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(GoogleGeocoderApplication.Companion.getAppContext(), "Aggiornamento dati terminato con errore.", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
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