package com.devarshukani.green_user.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.devarshukani.green_user.R;
import com.devarshukani.green_user.adapter.AwarenessAdapter;
import com.devarshukani.green_user.model.AwarenessModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AwarenessFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AwarenessFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";



    private String mParam1;
    private String mParam2;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private AwarenessAdapter awarenessAdapter;
    FirebaseFirestore db;


    public AwarenessFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AwarenessFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AwarenessFragment newInstance(String param1, String param2) {
        AwarenessFragment fragment = new AwarenessFragment();
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
        View view =  inflater.inflate(R.layout.fragment_awareness, container, false);


        progressBar = view.findViewById(R.id.progressBar);
        recyclerView = view.findViewById(R.id.rvNewsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        awarenessAdapter = new AwarenessAdapter();
        recyclerView.setAdapter(awarenessAdapter);

        progressBar.setVisibility(View.VISIBLE);

        db = FirebaseFirestore.getInstance();
        fetchAwarenessData();

        return view;
    }
    private void fetchAwarenessData() {
        db.collection("news").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<AwarenessModel> awarenessList = new ArrayList<>();
                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    String title = document.getString("title");
                    String description = document.getString("description");
                    AwarenessModel news = new AwarenessModel(title, description);
                    awarenessList.add(news);
                }
                awarenessAdapter.setData(awarenessList);
                progressBar.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle error
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}