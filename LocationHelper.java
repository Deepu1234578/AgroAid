package com.example.agroaid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class LocationHelper {

    public static void getLocation(
            Context context,
            LocationCallback callback) {

        FusedLocationProviderClient client =
                LocationServices.getFusedLocationProviderClient(context);

        @SuppressLint("MissingPermission")
        com.google.android.gms.tasks.Task<Location> task =
                client.getCurrentLocation(
                        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                        null
                );

        task.addOnSuccessListener(location -> {

            if (location != null) {

                callback.onLocationResult(location);

            }

        }).addOnFailureListener(e -> {

            e.printStackTrace();

        });
    }

    public interface LocationCallback {

        void onLocationResult(Location location);

    }
}