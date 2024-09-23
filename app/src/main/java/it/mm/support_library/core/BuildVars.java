/**
 * Created by Giovanni Accetta on 04/08/15.
 * Copyright (c) 2017 Dott. Ing. Giovanni Accetta. All rights reserved.
 */

package it.mm.support_library.core;

public class BuildVars {
    public static boolean DEBUG_VERSION = true;

    public static String SEND_SUPPORT_EMAIL = "k-reader@sikuel.it";

    /**
     * A flag to switch if the app should be run with local dev server or
     * production (cloud).
     */
    public static boolean LOCAL_SERVER_RUN = false;

    /**
     * Endpoint root URL
     */
    public static final String SERVER_URL = "https://help.sikuel.it";

//    public static final String LOCAL_SERVER_URL = "http://192.168.40.67:3000";
    public static final String LOCAL_SERVER_URL = "http://192.168.40.55:3000";
//    public static final String LOCAL_SERVER_URL = "http://192.168.30.51:3000"; //Cesarina


    /**
     * Tag name for logging.
     */
    public static final String TAG = "support-library";

    /**
     * Timeout request.
     * Timeout request
     */
    public static final int timeout = 25000;

    public static final int long_timeout = 40000;

}
