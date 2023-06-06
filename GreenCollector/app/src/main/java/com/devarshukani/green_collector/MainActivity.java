package com.devarshukani.green_collector;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mapView;
    private GoogleMap googleMap;
    private List<LatLng> startingPoints;
    private List<LatLng> destinations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Define your starting points and destinations here
        startingPoints = new ArrayList<>();
        startingPoints.add(new LatLng(18.5167261, 73.8562553)); // Starting Point 1
        startingPoints.add(new LatLng(18.5626915, 73.5524682)); // Starting Point 2
//        startingPoints.add(new LatLng(18.4832192, 73.8094734)); // Collection Location 4

        destinations = new ArrayList<>();
        destinations.add(new LatLng(18.5382469, 73.7984964)); // Collection Location 1
        destinations.add(new LatLng(18.5488615, 73.7524642)); // Collection Location 2
        destinations.add(new LatLng(18.5254469, 73.5584764)); // Collection Location 3


        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        for (LatLng startingPoint : startingPoints) {
            googleMap.addMarker(new MarkerOptions()
                    .position(startingPoint)
                    .title("Starting Point"));
        }

        for (LatLng destination : destinations) {
            googleMap.addMarker(new MarkerOptions()
                    .position(destination)
                    .title("Destination"));
        }

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(startingPoints.get(0))
                .zoom(15)
                .build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        // Draw routes for each starting point and destination
        for (int i = 0; i < startingPoints.size(); i++) {
            LatLng startingPoint = startingPoints.get(i);
            LatLng destination = destinations.get(i % destinations.size()); // Use modulo operator for cycling through destinations

            drawRoute(startingPoint, destination);
        }
    }

    private void drawRoute(LatLng startingPoint, LatLng destination) {
        GeoApiContext geoApiContext = new GeoApiContext.Builder()
                .apiKey("AIzaSyAcuHc_bMfcvjREIQMow4PGmdfTIIy0qgc") // Replace with your own Google Maps API key
                .build();

        com.google.maps.model.LatLng origin = new com.google.maps.model.LatLng(startingPoint.latitude, startingPoint.longitude);
        com.google.maps.model.LatLng dest = new com.google.maps.model.LatLng(destination.latitude, destination.longitude);

        com.google.maps.DirectionsApiRequest request = DirectionsApi.newRequest(geoApiContext)
                .mode(TravelMode.DRIVING)
                .origin(origin)
                .destination(dest);

        // Asynchronously calculate the directions
        request.setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                if (result.routes != null && result.routes.length > 0) {
                    DirectionsRoute route = result.routes[0]; // Get the first route

                    List<LatLng> polylinePoints = decodePolyline(route.overviewPolyline.getEncodedPath());

                    PolylineOptions polylineOptions = new PolylineOptions()
                            .color(Color.RED)
                            .width(5)
                            .addAll(polylinePoints);

                    runOnUiThread(() -> googleMap.addPolyline(polylineOptions));
                }
            }

            @Override
            public void onFailure(Throwable e) {
                // Handle failure
            }
        });
    }

    // Helper method to decode the polyline points
    private List<LatLng> decodePolyline(String encodedPath) {
        List<LatLng> polylinePoints = new ArrayList<>();
        int index = 0, len = encodedPath.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int shift = 0, result = 0;
            int b;

            do {
                b = encodedPath.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;

            do {
                b = encodedPath.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng point = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            polylinePoints.add(point);
        }

        return polylinePoints;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
