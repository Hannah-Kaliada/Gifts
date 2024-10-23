package com.example.gifts;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class UserDetailActivity extends AppCompatActivity {

    private ListView listViewGifts;
    private GiftAdapter giftAdapter;
    private FirebaseFirestore db;
    private ArrayList<Gift> giftList;
    private ArrayList<Gift> filteredList;
    private ArrayList<String> giftIds;
    private boolean isLoading = false;
    private Timer searchDebounceTimer = new Timer();
    private final long SEARCH_DELAY = 500;
    private GestureDetectorCompat gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        db = FirebaseFirestore.getInstance();
        String username = getIntent().getStringExtra("USERNAME");

        TextView textViewUsername = findViewById(R.id.textViewUsername);
        if (username != null) {
            textViewUsername.setText("Падарункi\n"+ username);
        } else {
            textViewUsername.setText("");
        }

        listViewGifts = findViewById(R.id.listViewGifts);
        giftList = new ArrayList<>();
        filteredList = new ArrayList<>();
        giftIds = new ArrayList<>();
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
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        EditText editTextSearch = findViewById(R.id.editTextSearch);
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchDebounceTimer.cancel();
                searchDebounceTimer = new Timer();
                searchDebounceTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(() -> filterGifts(s.toString()));
                    }
                }, SEARCH_DELAY);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        gestureDetector = new GestureDetectorCompat(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                int position = listViewGifts.pointToPosition((int) e.getX(), (int) e.getY());
                if (position != ListView.INVALID_POSITION) {
                    String giftId = giftIds.get(position);
                    showGiftDetailsDialog(giftId);
                }
                return true;
            }
        });

        listViewGifts.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        if (username != null) {
            loadUserGifts(username, 0);
        } else {
            //Toast.makeText(this, "Не было перададзена імя карыстальніка", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserGifts(String username, int filterPosition) {
        if (isLoading) return;
        isLoading = true;

        Query query = db.collection("users").document(username).collection("gifts");

        if (filterPosition == 1) {
            query = query.whereEqualTo("isReserved", true);
        } else if (filterPosition == 2) {
            query = query.whereEqualTo("isReserved", false);
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    giftList.clear();
                    filteredList.clear();
                    giftIds.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Gift gift = document.toObject(Gift.class);
                        if (gift != null) {
                            giftList.add(gift);
                            giftIds.add(document.getId());
                        }
                    }
                    filteredList.addAll(giftList);
                    giftAdapter.notifyDataSetChanged();
                    isLoading = false;
                })
                .addOnFailureListener(e -> {
                    isLoading = false;
                });
    }

    private void filterGifts(String query) {
        if (isLoading) return;
        filteredList.clear();
        for (Gift gift : giftList) {
            if (gift.getName() != null && gift.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(gift);
            }
        }
        giftAdapter.notifyDataSetChanged();
    }

    private void showGiftDetailsDialog(String giftId) {
        db.collection("users").document(getIntent().getStringExtra("USERNAME")).collection("gifts").document(giftId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String link = documentSnapshot.getString("link");
                        String store = documentSnapshot.getString("store");

                        LinearLayout layout = new LinearLayout(this);
                        layout.setOrientation(LinearLayout.VERTICAL);
                        layout.setPadding(16, 16, 16, 16);

                        TextView textView = new TextView(this);
                        textView.setText(name);
                        textView.setTextSize(18);
                        layout.addView(textView);

                        if (store != null && !store.isEmpty()) {
                            TextView storeTextView = new TextView(this);
                            storeTextView.setText("Крама: " + store);
                            storeTextView.setTextSize(16);
                            layout.addView(storeTextView);
                        }

                        Button button = null;
                        if (link != null && !link.isEmpty()) {
                            button = new Button(this);
                            button.setText("Перайсці да падарунка");

                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                            params.gravity = Gravity.CENTER;
                            button.setLayoutParams(params);

                            button.setOnClickListener(v -> {

                                if (isValidUrl(link)) {
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                                    startActivity(browserIntent);
                                } else {
                                    Toast.makeText(UserDetailActivity.this, "Няма спасылкi", Toast.LENGTH_SHORT).show();
                                }
                            });

                            layout.addView(button);
                        }

                        AlertDialog dialog = new AlertDialog.Builder(this)
                                .setTitle(name)
                                .setView(layout)
                                .setCancelable(true)
                                .create();

                        dialog.show();
                    }
                });
    }

    private boolean isValidUrl(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
