package it.mm.supportlibrary.reactiveandroid.internal.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexFile;

public final class MultiDexHelper {

    private static final String EXTRACTED_NAME_EXT = ".classes";
    private static final String EXTRACTED_SUFFIX = ".zip";
    private static final String INSTANT_RUN_DEX_DIR_PATH = "files/instant-run/dex/";
    private static final String SECONDARY_FOLDER_NAME = "code_cache" + File.separator + "secondary-dexes";
    private static final String PREFS_FILE = "multidex.version";
    private static final String KEY_DEX_NUMBER = "dex.number";

    //Prevent instantiation..
    private MultiDexHelper() { }

    /**
     * get all the dex path
     *
     * @return all the dex path, including the ones in the newly added instant-run folder
     * @throws PackageManager.NameNotFoundException
     * @throws IOException
     */
    @NonNull
    public static List<String> getSourcePaths(Context context) throws PackageManager.NameNotFoundException, IOException {
        ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
        File sourceApk = new File(applicationInfo.sourceDir);
        File dexDir = new File(applicationInfo.dataDir, SECONDARY_FOLDER_NAME);
        File instantRunDir = new File(applicationInfo.dataDir, INSTANT_RUN_DEX_DIR_PATH); //default instant-run dir

        List<String> sourcePaths = new ArrayList<>();
        sourcePaths.add(applicationInfo.sourceDir); //addColumn the default apk path

        if (instantRunDir.exists()) { //check if app using instant run
            for(final File dexFile : instantRunDir.listFiles()) { //addColumn all sources from instan-run
                sourcePaths.add(dexFile.getAbsolutePath());
            }
        }

        //the prefix of extracted file, ie: test.classes
        String extractedFilePrefix = sourceApk.getName() + EXTRACTED_NAME_EXT;
        //the total dex numbers
        int totalDexNumber = getMultiDexPreferences(context).getInt(KEY_DEX_NUMBER, 1);

        for (int secondaryNumber = 2; secondaryNumber <= totalDexNumber; secondaryNumber++) {
            //for each dex file, ie: test.classes2.zip, test.classes3.zip...
            String fileName = extractedFilePrefix + secondaryNumber + EXTRACTED_SUFFIX;
            File extractedFile = new File(dexDir, fileName);
            if (extractedFile.isFile()) {
                sourcePaths.add(extractedFile.getAbsolutePath());
                //we ignore the verify zip part
            }
        }

        return sourcePaths;
    }

    /**
     * get all the classes name in "classes.dex", "classes2.dex", ....
     *
     * @return all the classes name
     * @throws PackageManager.NameNotFoundException
     * @throws IOException
     */
    @NonNull
    public static List<String> getAllClasses(Context context) throws PackageManager.NameNotFoundException, IOException {
        List<String> classNames = new ArrayList<>();
        for (String path : getSourcePaths(context)) {
            try {
                DexFile dexfile;
                if (path.endsWith(EXTRACTED_SUFFIX)) {
                    //NOT use new DexFile(path), because it will throw "permission error in /data/dalvik-cache"
                    dexfile = DexFile.loadDex(path, path + ".tmp", 0);
                } else {
                    dexfile = new DexFile(path);
                }
                Enumeration<String> dexEntries = dexfile.entries();
                while (dexEntries.hasMoreElements()) {
                    classNames.add(dexEntries.nextElement());
                }
            } catch (IOException e) {
                throw new IOException("Error at loading dex file '" + path + "'");
            }
        }
        return classNames;
    }

    private static SharedPreferences getMultiDexPreferences(Context context) {
        return context.getSharedPreferences(PREFS_FILE, Context.MODE_MULTI_PROCESS);
    }

}