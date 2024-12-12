package it.mm.supportlibrary.core;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class GoogleApiGPS {

//    private Activity activity;
    private Context context;
    private Location finalLocation;
    public MutableLiveData<Location> mutableLocation;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final long UPDATE_INTERVAL = 1000, FASTEST_INTERVAL = 500;
    private float accuracy = 1000;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    // DecimalFormat dichiarato a livello di classe per evitare ricreazioni inutili
    private static final DecimalFormat df = new DecimalFormat("#.######");

    static {
        df.setRoundingMode(RoundingMode.CEILING);
    }

    public GoogleApiGPS(Context ctx) {
        context = ctx;
//        activity = act;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location.getAccuracy() < accuracy) {
                        accuracy = location.getAccuracy();
                        mutableLocation.setValue(location);
                    }
                }
            }
        };

        // Avvia gli aggiornamenti solo se Play Services sono disponibili
//        if (checkPlayServices()) {
//            startLocationUpdates();
//        }
    }

//    private boolean checkPlayServices() {
//        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
//        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
//
//        if (resultCode != ConnectionResult.SUCCESS) {
//            if (apiAvailability.isUserResolvableError(resultCode)) {
//                apiAvailability.getErrorDialog(activity, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
//            }
//            return false;
//        }
//        return true;
//    }

    public void startLocationUpdates() {
//        if (!hasLocationPermission()) {
//            requestLocationPermission();
//            return;
//        }

        try {
            LocationRequest locationRequest = new LocationRequest.Builder(LocationRequest.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL)
                    .setMinUpdateIntervalMillis(FASTEST_INTERVAL)
                    .build();

            fusedLocationClient.requestLocationUpdates(locationRequest, ContextCompat.getMainExecutor(context), locationCallback);
        } catch (SecurityException e) {
            // Gestisci l'eccezione nel caso in cui i permessi non siano stati concessi
            Toast.makeText(context, "Non sono stati concessi i permessi di geolocalizzazione.", Toast.LENGTH_SHORT).show();
        }
    }

    public void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

//    private void requestLocationPermission() {
//        Toast.makeText(context, "You need to enable permissions to display location!", Toast.LENGTH_SHORT).show();
//        ActivityCompat.requestPermissions(activity, new String[]{
//                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
//        }, 1);
//    }

    public Location getFinalLocation() {
//        if (!hasLocationPermission()) {
//            requestLocationPermission();
//            return null;
//        }

        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    finalLocation = location;
                }
            });
        } catch (SecurityException e) {
            // Gestisci l'eccezione nel caso in cui i permessi non siano stati concessi
            Toast.makeText(context, "Non sono stati concessi i permessi di geolocalizzazione", Toast.LENGTH_SHORT).show();
        }

        return finalLocation;
    }
}
