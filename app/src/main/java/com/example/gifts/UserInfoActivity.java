package com.example.gifts;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserInfoActivity extends AppCompatActivity {

    private ListView listViewUsers;
    private ListView listViewGifts;
    private EditText editTextSearchUsername;
    private FirebaseFirestore db;
    private String loggedInUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        listViewUsers = findViewById(R.id.listViewUsers);
        listViewGifts = findViewById(R.id.listViewGifts);
        editTextSearchUsername = findViewById(R.id.editTextSearchUsername);
        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        loggedInUsername = intent.getStringExtra("USERNAME");

        FloatingActionButton fabAddGift = findViewById(R.id.fabAddGift);
        fabAddGift.setOnClickListener(v -> showAddGiftDialog());

        editTextSearchUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        listViewUsers.setOnItemClickListener((parent, view, position, id) -> {
            String selectedUsername = (String) parent.getItemAtPosition(position);
            Intent detailIntent = new Intent(UserInfoActivity.this, UserDetailActivity.class);
            detailIntent.putExtra("USERNAME", selectedUsername);
            startActivity(detailIntent);
        });

        loadGifts();
    }

    private void searchUsers(String username) {
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> userNames = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User user = document.toObject(User.class);
                            userNames.add(user.getUsername());
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_list_item_1, userNames);
                        listViewUsers.setAdapter(adapter);
                    } else {
                        Toast.makeText(UserInfoActivity.this, "Error getting users", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showAddGiftDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_gift, null);
        builder.setView(dialogView);

        EditText editTextGiftName = dialogView.findViewById(R.id.editTextGiftName);
        EditText editTextGiftLink = dialogView.findViewById(R.id.editTextGiftLink);
        EditText editTextGiftStore = dialogView.findViewById(R.id.editTextGiftStore);
        Button buttonAddGift = dialogView.findViewById(R.id.buttonAddGift);

        AlertDialog dialog = builder.create();

        buttonAddGift.setOnClickListener(v -> {
            String giftName = editTextGiftName.getText().toString();
            String giftLink = editTextGiftLink.getText().toString();
            String giftStore = editTextGiftStore.getText().toString();

            if (!giftName.isEmpty()) {
                addGiftToFirestore(giftName, giftLink, giftStore);
                dialog.dismiss();
            } else {
                Toast.makeText(UserInfoActivity.this, "Gift name is required", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void addGiftToFirestore(String name, String link, String store) {
        Map<String, Object> gift = new HashMap<>();
        gift.put("name", name);
        gift.put("link", link);
        gift.put("store", store);

        db.collection("users").document(loggedInUsername).collection("gifts")
                .add(gift)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(UserInfoActivity.this, "Gift added", Toast.LENGTH_SHORT).show();
                    loadGifts();
                })
                .addOnFailureListener(e -> Toast.makeText(UserInfoActivity.this, "Error adding gift", Toast.LENGTH_SHORT).show());
    }

    private void loadGifts() {
        Log.d("UserInfoActivity", "Loading gifts for user: " + loggedInUsername);

        if (loggedInUsername == null || loggedInUsername.isEmpty()) {
            Log.e("UserInfoActivity", "Logged in username is null or empty");
            return;
        }

        db.collection("users").document(loggedInUsername).collection("gifts")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> giftNames = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d("UserInfoActivity", "Document ID: " + document.getId());
                            Map<String, Object> gift = document.getData();
                            String giftName = (String) gift.get("name");

                            if (giftName != null) {
                                giftNames.add(giftName);
                            } else {
                                Log.w("UserInfoActivity", "Gift name is null for document: " + document.getId());
                            }
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_list_item_1, giftNames);
                        listViewGifts.setAdapter(adapter);

                        Log.d("UserInfoActivity", "Number of gifts: " + giftNames.size());
                    } else {
                        Log.e("UserInfoActivity", "Error loading gifts", task.getException());
                        Toast.makeText(UserInfoActivity.this, "Error loading gifts", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
