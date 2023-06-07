package com.devarshukani.green_collector;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mapView;
    private GoogleMap googleMap;
    private FloatingActionButton fabProfile, fabQrScanner, fabReachedDestination, fabRecenter;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LatLng destination, source; // Destination location
    private Polyline routePolyline; // Polyline for the route
    private List<LatLng> routePoints; // List of points for the route
    private LatLng currentLocation;
    private Marker currentLocationMarker;
    private BitmapDescriptor customMarkerIcon;
    private boolean startlater = false;



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

        fabReachedDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(startlater == false){
                    showCustomDialogReachedDestination();
                }
                else{
                    startlater = false;
                    startRoute();

                }
            }
        });

        showCustomDialogStartGarbageCollection();

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        fabRecenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recenterMap();
            }
        });

    }

    private void showCustomDialogStartGarbageCollection() {
        // Create a dialog object
        Dialog dialog = new Dialog(this, R.style.CustomDialog);

        dialog.setContentView(R.layout.dialog_start_garbage_collection);
        dialog.setCanceledOnTouchOutside(false);

        Button startButton = dialog.findViewById(R.id.startButton);
        TextView startLaterLink = dialog.findViewById(R.id.startLaterLink);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRoute();
                dialog.dismiss();
            }
        });
        startLaterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startlater = true;
                dialog.dismiss();
            }
        });

        // Show the dialog
        dialog.show();
    }

    private void showCustomDialogReachedDestination() {
        // Create a dialog object
        Dialog dialog = new Dialog(this, R.style.CustomDialog);

        dialog.setContentView(R.layout.dialog_reached_destination);
        dialog.setCanceledOnTouchOutside(false);

        Button scanQRCodeButton = dialog.findViewById(R.id.scanQRCodeButton);
        TextView continueWithoutScanningLink = dialog.findViewById(R.id.continueWithoutScanningLink);
        scanQRCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        continueWithoutScanningLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomDialogStartNextDestination();
                dialog.dismiss();
            }
        });

        // Show the dialog
        dialog.show();
    }

    private void showCustomDialogStartNextDestination() {
        // Create a dialog object
        BottomSheetDialog dialog = new BottomSheetDialog(this);

        dialog.setContentView(R.layout.dialog_start_next_destination);
        dialog.setCanceledOnTouchOutside(false);

        Button continueCollectingButton = dialog.findViewById(R.id.continueCollectingButton);
        Button dumpGarbageButton = dialog.findViewById(R.id.dumpGarbageButton);
        TextView stopCollectingLink = dialog.findViewById(R.id.stopCollectingLink);
        continueCollectingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleMap.clear();
                startRoute();
                dialog.dismiss();
            }
        });

        dumpGarbageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        stopCollectingLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // Show the dialog
        dialog.show();
    }




    public void startRoute() {

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Request the user's current location
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        // Location found, retrieve latitude and longitude
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        // Do something with the retrieved location coordinates
                        Log.d("Location", "Latitude: " + latitude + " Longitude: " + longitude);
                        source = new LatLng(latitude, longitude);
                        destination = new LatLng(22.2770064, 70.7828704);

                        BitmapDescriptor customMarkerIcon;
//                        customMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.garbage_van_marker);
//                        MarkerOptions markerOptionsSource = new MarkerOptions()
//                                .position(source)
//                                .title("You")
//                                .icon(customMarkerIcon)
//                                .anchor(0.5f, 0.5f);
//                        sourceMarker = googleMap.addMarker(markerOptionsSource);


                        customMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.dustbin_marker);
                        MarkerOptions markerOptionsDestination = new MarkerOptions()
                                .position(destination)
                                .title("Collection Point")
                                .icon(customMarkerIcon)
                                .anchor(0.5f,0.5f);
                        googleMap.addMarker(markerOptionsDestination);

                        getDirections(source, destination);
                    } else {
                        // Location is null, handle the case when the location is not available
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "Failed to access location", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Request permission to access the user's location
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

//        source = new LatLng(22.2781274, 70.7743364);
//        destination = new LatLng(22.2770064, 70.7828704);
//
//        // Call Directions API to get the optimized route and draw the polyline
//        getDirections(source, destination);
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        // Check for location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            customMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.garbage_van_marker);
            googleMap.setMyLocationEnabled(false);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            setupLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

//        // Set the destination and source coordinates (dummy values for demonstration)
//        destination = new LatLng(37.7749, -122.4194); // San Francisco
//        source = new LatLng(37.7749, -122.4312); // Source location
//
//        // Call Directions API to get the optimized route and draw the polyline
//        getDirections(source, destination);
    }

    private void setupLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000); // Update location every 5 seconds

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        // Update current location on the map
                        updateCurrentLocationMarker(currentLocation);
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void getDirections(LatLng source, LatLng destination) {
        // Build the Directions API URL
        String directionsUrl = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + source.latitude + "," + source.longitude +
                "&destination=" + destination.latitude + "," + destination.longitude +
                "&key=AIzaSyAcuHc_bMfcvjREIQMow4PGmdfTIIy0qgc";

        // Create an HTTP client
        OkHttpClient client = new OkHttpClient();

        // Create the request
        Request request = new Request.Builder()
                .url(directionsUrl)
                .build();

        // Make the API call asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle API call failure
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String jsonData = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonData);

                        // Parse the response to retrieve the route points
                        routePoints = parseRoutePoints(jsonObject);

                        // Draw the polyline on the map
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                drawPolyline();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private List<LatLng> parseRoutePoints(JSONObject jsonObject) throws JSONException {
        List<LatLng> points = new ArrayList<>();

        JSONArray routesArray = jsonObject.getJSONArray("routes");
        if (routesArray.length() > 0) {
            JSONObject route = routesArray.getJSONObject(0);
            JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
            String encodedPolyline = overviewPolyline.getString("points");

            points = decodePolyline(encodedPolyline);
        }

        return points;
    }

    private List<LatLng> decodePolyline(String encodedPolyline) {
        List<LatLng> points = new ArrayList<>();
        int index = 0;
        int length = encodedPolyline.length();
        int latitude = 0, longitude = 0;

        while (index < length) {
            int b;
            int shift = 0;
            int result = 0;

            do {
                b = encodedPolyline.charAt(index++) - 63;
                result |= (b & 0x1F) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            latitude += dlat;

            shift = 0;
            result = 0;

            do {
                b = encodedPolyline.charAt(index++) - 63;
                result |= (b & 0x1F) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            longitude += dlng;

            points.add(new LatLng(latitude / 1E5, longitude / 1E5));
        }

        return points;
    }

    private void drawPolyline() {
        if (routePoints != null && routePoints.size() > 0) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(routePoints)
                    .width(12)
                    .color(Color.rgb(76,175,80));

            routePolyline = googleMap.addPolyline(polylineOptions);

            // Move the camera to the destination
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(source, 18f));
        }
    }



    private void updateCurrentLocationMarker(LatLng location) {
        // Update the marker for current location on the map
        // Remove previous marker if exists
        // Add new marker for current location
        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
        }
        // Add new marker for current location
        MarkerOptions markerOptions = new MarkerOptions()
                .position(location)
                .title("Current Location")
                .icon(customMarkerIcon)
                .anchor(0.5f, 0.5f);
        currentLocationMarker = googleMap.addMarker(markerOptions);
    }

    private void recenterMap() {
        // Recenter the map to the current location
        if (currentLocation != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18f));
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
        if (fusedLocationProviderClient != null && locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            googleMap.setMyLocationEnabled(false);
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            setupLocationUpdates();
        }
    }
}
