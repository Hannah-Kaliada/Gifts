package com.example.gifts;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class UserDetailActivity extends AppCompatActivity {

    private ListView listViewGifts;
    private GiftAdapter giftAdapter;
    private FirebaseFirestore db;
    private ArrayList<Gift> giftList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        db = FirebaseFirestore.getInstance();
        String username = getIntent().getStringExtra("USERNAME");

        listViewGifts = findViewById(R.id.listViewGifts);
        giftList = new ArrayList<>();
        giftAdapter = new GiftAdapter(this, giftList, username);
        listViewGifts.setAdapter(giftAdapter);

        if (username != null) {
            loadUserGifts(username);
        } else {
            Toast.makeText(this, "No username provided", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserGifts(String username) {
        db.collection("users").document(username).collection("gifts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    giftList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Gift gift = document.toObject(Gift.class);
                        giftList.add(gift);
                    }
                    giftAdapter.notifyDataSetChanged();

                    TextView textViewUsername = findViewById(R.id.textViewUsername);
                    textViewUsername.setText(username + "'s gifts");
                })
                .addOnFailureListener(e -> Toast.makeText(UserDetailActivity.this, "Error loading gifts", Toast.LENGTH_SHORT).show());
    }
}
