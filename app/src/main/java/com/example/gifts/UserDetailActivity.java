package com.example.gifts;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

public class UserDetailActivity extends AppCompatActivity {

    private TextView textViewUsername;
    private TextView textViewPassword;
    private TableLayout tableLayoutGifts;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        textViewUsername = findViewById(R.id.textViewUsername);
        textViewPassword = findViewById(R.id.textViewPassword);
        tableLayoutGifts = findViewById(R.id.tableLayoutGifts);
        db = FirebaseFirestore.getInstance();

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
                    tableLayoutGifts.removeAllViews();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Gift gift = document.toObject(Gift.class);
                        addGiftToTable(gift.getName(), gift.getLink(), gift.getStore());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(UserDetailActivity.this, "Error loading gifts", Toast.LENGTH_SHORT).show());
    }

    private void addGiftToTable(String name, String link, String store) {
        TableRow tableRow = new TableRow(this);
        tableRow.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));

        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                0,
                TableLayout.LayoutParams.WRAP_CONTENT, 1.0f);

        TextView nameTextView = new TextView(this);
        nameTextView.setText(name);
        nameTextView.setTextColor(getResources().getColor(R.color.purple_700));
        nameTextView.setPadding(4, 4, 4, 4);
        nameTextView.setLayoutParams(layoutParams);

        TextView linkTextView = new TextView(this);
        linkTextView.setText(link.isEmpty() ? "N/A" : link);
        linkTextView.setTextColor(getResources().getColor(R.color.purple_700));
        linkTextView.setPadding(4, 4, 4, 4);
        linkTextView.setLayoutParams(layoutParams);

        TextView storeTextView = new TextView(this);
        storeTextView.setText(store.isEmpty() ? "N/A" : store);
        storeTextView.setTextColor(getResources().getColor(R.color.purple_700));
        storeTextView.setPadding(4, 4, 4, 4);
        storeTextView.setLayoutParams(layoutParams);


        CheckBox checkBox = new CheckBox(this);
        checkBox.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        tableRow.addView(nameTextView);
        tableRow.addView(linkTextView);
        tableRow.addView(storeTextView);
        tableRow.addView(checkBox);

        tableLayoutGifts.addView(tableRow);
    }
}
