package com.devarshukani.green_user.fragment;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.devarshukani.green_user.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    FirebaseAuth auth;
    FirebaseFirestore db;
    TextView helloHomeTextView;
    CardView wastePickupClick;
    Button qrCodeButton;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_home, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        helloHomeTextView = view.findViewById(R.id.helloHomeTextView);
        wastePickupClick = view.findViewById(R.id.wastePickupClick);
        qrCodeButton = view.findViewById(R.id.qrCodeButton);

        db.collection("users").document(auth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String fname = documentSnapshot.getString("fname");

                            helloHomeTextView.setText("Hello "+fname+",");
                        } else {
                            Log.d("Error", "Document does not exist");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Error", "Error getting document", e);
                    }
                });

        wastePickupClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showCustomDialogWastePickupQuestions();
            }
        });

        qrCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomDialogQRCodeImage();
            }
        });



        return view;
    }

    private void showCustomDialogWastePickupQuestions() {
        // Create a dialog object
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());

        dialog.setContentView(R.layout.dialog_waste_pickup_questions);

        Button yesButton = dialog.findViewById(R.id.yesButton);
        Button partialButton = dialog.findViewById(R.id.partialButton);
        Button noButton = dialog.findViewById(R.id.noButton);
        TextView notSureLink = dialog.findViewById(R.id.notSureLink);

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserData("yes");
                dialog.dismiss();
            }
        });
        partialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserData("partial");
                dialog.dismiss();
            }
        });
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserData("no");
                dialog.dismiss();
            }
        });
        notSureLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserData("notsure");
                dialog.dismiss();
            }
        });

        // Show the dialog
        dialog.show();
    }

    private void updateUserData(String answer){
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                // Get the latitude and longitude
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();

                                Map<String, Object> userlocationdata = new HashMap<>();
                                userlocationdata.put("latitude", latitude);
                                userlocationdata.put("longitude", longitude);
                                userlocationdata.put("isrecyclable", answer);

                                db.collection("users").document(auth.getCurrentUser().getUid())
                                        .update(userlocationdata)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Location data stored successfully
                                                Toast.makeText(getContext(), "Your request has been saved", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Failed to store location data
                                                Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
                                                Log.d("ERROR",e.toString());
                                            }
                                        });
                            }
                        }
                    });

        } else {
            // Permission not granted, request it from the user
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void showCustomDialogQRCodeImage() {
        // Create a dialog object
        Dialog dialog = new Dialog(getContext());

        dialog.setContentView(R.layout.dialog_show_qr_code);

        ImageView qrCodeImageView = dialog.findViewById(R.id.qrCodeImageView);

        String data = auth.getCurrentUser().getUid();

        try {
            // Generate a QR code with the given data
            Bitmap qrCodeBitmap = generateQRCode(data, 700, 700);

            // Set the generated QR code bitmap to the ImageView
            qrCodeImageView.setImageBitmap(qrCodeBitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        // Show the dialog
        dialog.show();
    }

    private Bitmap generateQRCode(String data, int width, int height) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height);

        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }

        Bitmap qrCodeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        qrCodeBitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        return qrCodeBitmap;
    }
}