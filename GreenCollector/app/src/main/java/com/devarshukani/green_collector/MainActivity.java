package com.devarshukani.green_collector;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.TravelMode;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mapView;
    private GoogleMap googleMap;
    private FloatingActionButton fabProfile, fabQrScanner, fabReachedDestination, fabRecenter;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LatLng destination; // Destination location

    private Polyline routePolyline; // Polyline for the route
    private LatLng currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fabProfile = findViewById(R.id.fabProfile);
        fabQrScanner = findViewById(R.id.fabQrScanner);
        fabReachedDestination = findViewById(R.id.fabReachedDestination);
        fabRecenter = findViewById(R.id.fabRecenter);

        fabProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        fabRecenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // Set the destination location (replace with your desired destination coordinates)
        destination = new LatLng(22.3071364, 70.8151765);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    updateCurrentLocation(location);
                }
            }
        };
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        // Check location permissions
        if (checkLocationPermissions()) {
            // Permissions already granted, start location updates
            startLocationUpdates();
        } else {
            // Request location permissions
            requestLocationPermissions();
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(createLocationRequest(), locationCallback, null);
    }

    private LocationRequest createLocationRequest() {
        return LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000); // Update interval in milliseconds
    }

    private void updateCurrentLocation(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        currentLocation = new LatLng(latitude, longitude);

        // Add a marker to the map
        googleMap.addMarker(new MarkerOptions()
                .position(currentLocation));

        // Draw the route
        drawRoute();
    }

    private void drawRoute() {
        if (routePolyline != null) {
            routePolyline.remove();
        }

        // Create the GeoApiContext object with your API key
        GeoApiContext geoApiContext = new GeoApiContext.Builder()
                .apiKey("AIzaSyAcuHc_bMfcvjREIQMow4PGmdfTIIy0qgc")
                .build();

        // Create the Directions API request
        DirectionsApiRequest directionsRequest = DirectionsApi.newRequest(geoApiContext)
                .origin(currentLocation.latitude + "," + currentLocation.longitude)
                .destination(destination.latitude + "," + destination.longitude)
                .mode(TravelMode.DRIVING);

        // Asynchronously execute the Directions API request
        directionsRequest.setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                // Process the Directions API response
                if (result.routes != null && result.routes.length > 0) {
                    DirectionsRoute route = result.routes[0];

                    // Extract the overview polyline from the route
                    EncodedPolyline overviewPolyline = route.overviewPolyline;

                    // Decode the polyline into a list of LatLng points
                    List<com.google.maps.model.LatLng> routePoints = PolylineEncoding.decode(overviewPolyline.getEncodedPath());

                    // Convert the routePoints to LatLng objects used by Google Maps Android API
                    List<LatLng> androidRoutePoints = new ArrayList<>();
                    for (com.google.maps.model.LatLng point : routePoints) {
                        androidRoutePoints.add(new LatLng(point.lat, point.lng));
                    }

                    // Create the PolylineOptions and set its properties
                    PolylineOptions polylineOptions = new PolylineOptions()
                            .addAll(androidRoutePoints)
                            .width(5f)
                            .color(Color.BLUE);


                    // Add the polyline to the map
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            routePolyline = googleMap.addPolyline(polylineOptions);

                            // Move the camera to include both the current location and the destination
                            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder()
                                    .include(currentLocation)
                                    .include(destination);
                            LatLngBounds bounds = boundsBuilder.build();
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
                        }
                    });
                }
            }

            @Override
            public void onFailure(Throwable e) {
                // Handle any errors that occurred during the Directions API request
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Failed to get directions", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private boolean checkLocationPermissions() {
        int coarseLocationPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int fineLocationPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        return coarseLocationPermission == PackageManager.PERMISSION_GRANTED &&
                fineLocationPermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Location permissions required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
