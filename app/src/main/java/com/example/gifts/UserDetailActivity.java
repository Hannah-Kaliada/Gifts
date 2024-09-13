package com.example.gifts;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserDetailActivity extends AppCompatActivity {

    private TextView textViewUsername;
    private TextView textViewPassword;
    private RecyclerView recyclerViewGifts;
    private FirebaseFirestore db;
    private GiftAdapter giftAdapter;
    private List<Gift> giftList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        textViewUsername = findViewById(R.id.textViewUsername);
        textViewPassword = findViewById(R.id.textViewPassword);
        recyclerViewGifts = findViewById(R.id.recyclerViewGifts);
        db = FirebaseFirestore.getInstance();
        giftList = new ArrayList<>();

        recyclerViewGifts.setLayoutManager(new LinearLayoutManager(this));
        giftAdapter = new GiftAdapter(giftList);
        recyclerViewGifts.setAdapter(giftAdapter);

        // Получение имени пользователя
        Intent intent = getIntent();
        String username = intent.getStringExtra("USERNAME");

        if (username != null) {
            loadUserDetails(username);
            loadUserGifts(username);
        } else {
            Toast.makeText(this, "No username provided", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserDetails(String username) {
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            User user = document.toObject(User.class);
                            if (user != null) {
                                textViewUsername.setText("Username: " + user.getUsername());
                                textViewPassword.setText("Password: " + user.getPassword());
                            } else {
                                Toast.makeText(UserDetailActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(UserDetailActivity.this, "No user found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(UserDetailActivity.this, "Error getting user details", Toast.LENGTH_SHORT).show();
                    }
                });
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
                })
                .addOnFailureListener(e -> Toast.makeText(UserDetailActivity.this, "Error loading gifts", Toast.LENGTH_SHORT).show());
    }
}
