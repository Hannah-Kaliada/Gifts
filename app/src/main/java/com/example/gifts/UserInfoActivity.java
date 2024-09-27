package com.example.gifts;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import android.view.ScaleGestureDetector;

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
    private List<String> giftIds = new ArrayList<>();
    private ScaleGestureDetector scaleGestureDetector;

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

        listViewGifts.setOnItemLongClickListener((parent, view, position, id) -> {
            String selectedGiftId = giftIds.get(position);
            Map<String, Object> selectedGift = (Map<String, Object>) parent.getItemAtPosition(position);
            String selectedGiftName = (String) selectedGift.get("name");
            String selectedGiftLink = (String) selectedGift.get("link");
            String selectedGiftStore = (String) selectedGift.get("store");

            showEditDeleteDialog(selectedGiftId, selectedGiftName, selectedGiftLink, selectedGiftStore);
            return true;
        });

        loadGifts();

        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                if (detector.getScaleFactor() < 1.0) {
                    showAppDescriptionDialog();
                }
                return true;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
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
                        Toast.makeText(UserInfoActivity.this, "Памылка пры атрыманні карыстальнікаў", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(UserInfoActivity.this, "Назва падарунка абавязковая", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(UserInfoActivity.this, "Падарунак дададзены", Toast.LENGTH_SHORT).show();
                    loadGifts();
                })
                .addOnFailureListener(e -> Toast.makeText(UserInfoActivity.this, "Памылка пры дадаванні падарунка", Toast.LENGTH_SHORT).show());
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
                        List<Map<String, String>> giftsList = new ArrayList<>();
                        giftIds.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, String> gift = new HashMap<>();
                            gift.put("name", (String) document.get("name"));
                            gift.put("link", (String) document.get("link"));
                            gift.put("store", (String) document.get("store"));

                            giftIds.add(document.getId());
                            giftsList.add(gift);
                        }

                        String[] from = {"name", "link", "store"};
                        int[] to = {R.id.textViewGiftName, R.id.textViewGiftLink, R.id.textViewGiftStore};

                        SimpleAdapter adapter = new SimpleAdapter(this, giftsList, R.layout.gift_list_item, from, to);
                        ListView giftsListView = findViewById(R.id.listViewGifts);
                        giftsListView.setAdapter(adapter);
                    } else {
                        Log.e("UserInfoActivity", "Error getting documents: ", task.getException());
                    }
                });
    }

    private boolean isDescriptionDialogOpen = false;

    private void showAppDescriptionDialog() {
        if (isDescriptionDialogOpen) return;

        isDescriptionDialogOpen = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Апісанне функцыянальнасці прыкладання")
                .setMessage("Гэта прыкладанне распрацавана для дапамогі карыстальнікам з лёгкасцю адсочваць свае жаданні і спрыяння сябрам і сям'і ў пошуку ідэальных падарункаў. " +
                        "\n\nАсноўныя магчымасці:" +
                        "\n ⁃ Стварэнне спісаў жаданняў: Дадавайце падарункі з важнай інфармацыяй, такой як назва, крама і спасылка на тавар." +
                        "\n ⁃ Прагляд спісаў жаданняў іншых карыстальнікаў: Шукайце і даследуйце спісы жаданняў сваіх сяброў." +
                        "\n ⁃ Рэзерваванне падарункаў: Ананімна забраніруйце падарункі, каб яны не былі набыты іншымі." +
                        "\n ⁃ Рэдагаванне і выдаленне падарункаў: Лёгка змяняйце або выдаляйце падарункі з вашага спісу." +
                        "\n\nАўтар: Ганна Пацукевіч" +
                        "\nГод: 2024" +
                        "\n\nДля дадатковай інфармацыі або падтрымкі, звяртайцеся да нас у Telegram: @jewishmommy")
                .setPositiveButton("OK", (dialog, which) -> isDescriptionDialogOpen = false)
                .setOnDismissListener(dialog -> isDescriptionDialogOpen = false)
                .show();
    }

    private void showEditDeleteDialog(String giftId, String giftName, String giftLink, String giftStore) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Рэдагаваць або выдаліць падарунак?")
                .setMessage("Вы выбралі падарунак: " + giftName)
                .setPositiveButton("Рэдагаваць", (dialog, which) -> showEditGiftDialog(giftId, giftName, giftLink, giftStore))
                .setNegativeButton("Выдаліць", (dialog, which) -> deleteGift(giftId))
                .setNeutralButton("Скасаванне", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showEditGiftDialog(String giftId, String giftName, String giftLink, String giftStore) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_gift, null);
        builder.setView(dialogView);

        EditText editTextGiftName = dialogView.findViewById(R.id.editTextGiftName);
        EditText editTextGiftLink = dialogView.findViewById(R.id.editTextGiftLink);
        EditText editTextGiftStore = dialogView.findViewById(R.id.editTextGiftStore);
        Button buttonAddGift = dialogView.findViewById(R.id.buttonAddGift);

        editTextGiftName.setText(giftName);
        editTextGiftLink.setText(giftLink);
        editTextGiftStore.setText(giftStore);

        AlertDialog dialog = builder.create();

        buttonAddGift.setOnClickListener(v -> {
            String newGiftName = editTextGiftName.getText().toString();
            String newGiftLink = editTextGiftLink.getText().toString();
            String newGiftStore = editTextGiftStore.getText().toString();

            if (!newGiftName.isEmpty()) {
                editGiftInFirestore(giftId, newGiftName, newGiftLink, newGiftStore);
                dialog.dismiss();
            } else {
                Toast.makeText(UserInfoActivity.this, "Назва падарунка абавязковая", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void editGiftInFirestore(String giftId, String name, String link, String store) {
        Map<String, Object> updatedGift = new HashMap<>();
        updatedGift.put("name", name);
        updatedGift.put("link", link);
        updatedGift.put("store", store);

        db.collection("users").document(loggedInUsername).collection("gifts").document(giftId)
                .update(updatedGift)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UserInfoActivity.this, "Падарунак абноўлены", Toast.LENGTH_SHORT).show();
                    loadGifts();
                })
                .addOnFailureListener(e -> Toast.makeText(UserInfoActivity.this, "Памылка пры абнаўленні падарунка", Toast.LENGTH_SHORT).show());
    }

    private void deleteGift(String giftId) {
        db.collection("users").document(loggedInUsername).collection("gifts").document(giftId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UserInfoActivity.this, "Падарунак выдалены", Toast.LENGTH_SHORT).show();
                    loadGifts();
                })
                .addOnFailureListener(e -> Toast.makeText(UserInfoActivity.this, "Памылка пры выдаленні падарунка", Toast.LENGTH_SHORT).show());
    }
}

