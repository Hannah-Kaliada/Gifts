package com.example.gifts;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class UserDetailActivity extends AppCompatActivity {

    private ListView listViewGifts;
    private GiftAdapter giftAdapter;
    private FirebaseFirestore db;
    private ArrayList<Gift> giftList;
    private ArrayList<Gift> filteredList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        db = FirebaseFirestore.getInstance();
        String username = getIntent().getStringExtra("USERNAME");

        listViewGifts = findViewById(R.id.listViewGifts);
        giftList = new ArrayList<>();
        filteredList = new ArrayList<>();
        giftAdapter = new GiftAdapter(this, filteredList, username);
        listViewGifts.setAdapter(giftAdapter);

        Spinner spinnerFilter = findViewById(R.id.spinnerFilter);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.filter_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadUserGifts(username, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        EditText editTextSearch = findViewById(R.id.editTextSearch);
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterGifts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        if (username != null) {
            loadUserGifts(username, 0);
        } else {
            Toast.makeText(this, "Не было перададзена імя карыстальніка", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserGifts(String username, int filterPosition) {
        Query query = db.collection("users").document(username).collection("gifts");

        if (filterPosition == 1) {
            query = query.whereEqualTo("isReserved", true);
        } else if (filterPosition == 2) {
            query = query.whereEqualTo("isReserved", false);
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    giftList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Gift gift = document.toObject(Gift.class);
                        if (gift != null) {
                            giftList.add(gift);
                        }
                    }
                    filteredList.clear();
                    filteredList.addAll(giftList);
                    giftAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UserDetailActivity.this, "Памылка загрузкі падарункаў", Toast.LENGTH_SHORT).show();
                });
    }

    private void filterGifts(String query) {
        filteredList.clear();
        for (Gift gift : giftList) {
            if (gift.getName() != null && gift.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(gift);
            }
        }
        giftAdapter.notifyDataSetChanged();
    }
}
