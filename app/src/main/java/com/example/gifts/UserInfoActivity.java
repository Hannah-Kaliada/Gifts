package com.example.gifts;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserInfoActivity extends AppCompatActivity {

    private ListView listViewGifts;
    private FirebaseFirestore db;
    private String loggedInUsername;
    private List<String> giftIds = new ArrayList<>();
    private GestureDetectorCompat gestureDetector; // Для обработки жестов
    private boolean isDescriptionDialogOpen = false; // Флаг для отслеживания состояния диалога
    private ScaleGestureDetector scaleGestureDetector;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        listViewGifts = findViewById(R.id.listViewGifts);
        db = FirebaseFirestore.getInstance();

        // Инициализация GestureDetector для обработки жестов
        gestureDetector = new GestureDetectorCompat(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                // Обработка одиночного нажатия
                int position = listViewGifts.pointToPosition((int) e.getX(), (int) e.getY());
                if (position != ListView.INVALID_POSITION) {
                    String giftId = giftIds.get(position);
                    showGiftDetailsDialog(giftId); // Метод для показа информации о подарке
                }
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                // Проверка на смахивание влево
                if (e1.getX() - e2.getX() > 100 && Math.abs(velocityX) > 100) {
                    // Смахивание влево
                    int position = listViewGifts.pointToPosition((int) e1.getX(), (int) e1.getY());
                    if (position != ListView.INVALID_POSITION) {
                        String giftId = giftIds.get(position);
                        showEditDeleteDialog(giftId);
                    }
                    return true;
                }
                return false;
            }
        });

        // Инициализация ScaleGestureDetector для обработки жеста сведения пальцев
        ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                // Показываем описание приложения при начале жеста сведения пальцев
                showAppDescriptionDialog();
                return true;
            }
        });

        // Устанавливаем обработку касаний на ListView для жестов
        listViewGifts.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            scaleGestureDetector.onTouchEvent(event); // Обрабатываем жест сведения пальцев
            return true;
        });

        Intent intent = getIntent();
        loggedInUsername = intent.getStringExtra("USERNAME");

        FloatingActionButton fabAddGift = findViewById(R.id.fabAddGift);
        FloatingActionButton fabSearchUsers = findViewById(R.id.fabSearchUsers);

        fabAddGift.setOnClickListener(v -> showAddGiftDialog());
        fabSearchUsers.setOnClickListener(v -> showSearchUsersDialog());

        loadGifts();
    }



    // Диалог для отображения описания приложения при флинге
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

    // Диалог для поиска пользователей
    private void showSearchUsersDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_search_users, null);
        builder.setView(dialogView);

        EditText editTextSearchUsername = dialogView.findViewById(R.id.editTextSearchUsername);
        ListView listViewSearchResults = dialogView.findViewById(R.id.listViewSearchResults);

        AlertDialog dialog = builder.create();

        editTextSearchUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString(), listViewSearchResults);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        listViewSearchResults.setOnItemClickListener((parent, view, position, id) -> {
            String selectedUsername = (String) parent.getItemAtPosition(position);
            Intent detailIntent = new Intent(UserInfoActivity.this, UserDetailActivity.class);
            detailIntent.putExtra("USERNAME", selectedUsername);
            startActivity(detailIntent);
            dialog.dismiss();
        });

        dialog.show();
    }

    // Поиск пользователей по имени
    private void searchUsers(String username, ListView listViewSearchResults) {
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> userNames = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            userNames.add(document.getString("username"));
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_list_item_1, userNames);
                        listViewSearchResults.setAdapter(adapter);
                    } else {
                        Toast.makeText(UserInfoActivity.this, "Памылка пры атрыманні карыстальнікаў", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Загрузка списка подарков (только названия)
    private void loadGifts() {
        db.collection("users").document(loggedInUsername).collection("gifts")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> giftNames = new ArrayList<>();
                        giftIds.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            giftNames.add(document.getString("name"));
                            giftIds.add(document.getId());
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_list_item_1, giftNames);
                        listViewGifts.setAdapter(adapter);

                        listViewGifts.setOnItemClickListener((parent, view, position, id) -> {
                            String giftId = giftIds.get(position);
                            showGiftDetailsDialog(giftId);
                        });
                    } else {
                        Toast.makeText(UserInfoActivity.this, "Памылка пры загрузцы падарункаў", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Диалог для отображения информации о подарке с возможностью открыть ссылку
    private void showGiftDetailsDialog(String giftId) {
        db.collection("users").document(loggedInUsername).collection("gifts").document(giftId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String link = documentSnapshot.getString("link");
                        String store = documentSnapshot.getString("store");

                        // Создание вертикального LinearLayout
                        LinearLayout layout = new LinearLayout(this);
                        layout.setOrientation(LinearLayout.VERTICAL);
                        layout.setPadding(16, 16, 16, 16);

                        // Создание TextView для отображения информации о подарке
                        TextView textView = new TextView(this);
                        textView.setText(name); // Отображаем только имя подарка
                        textView.setTextSize(18);
                        layout.addView(textView);

                        // Проверка наличия информации о магазине
                        if (store != null && !store.isEmpty()) {
                            TextView storeTextView = new TextView(this);
                            storeTextView.setText("Крама: " + store);
                            storeTextView.setTextSize(16);
                            layout.addView(storeTextView);
                        }

                        // Проверка наличия ссылки
                        Button button = null; // Инициализируем кнопку для ссылки
                        if (link != null && !link.isEmpty()) {
                            // Создание кнопки
                            button = new Button(this);
                            button.setText("Перайсці да падарунка");

                            // Центрирование кнопки
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                            params.gravity = Gravity.CENTER;
                            button.setLayoutParams(params);

                            // Установка обработчика нажатия на кнопку
                            button.setOnClickListener(v -> {
                                // Проверка на корректность ссылки перед открытием
                                if (isValidUrl(link)) {
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                                    startActivity(browserIntent);
                                } else {
                                    Toast.makeText(UserInfoActivity.this, "Няма спасылкi", Toast.LENGTH_SHORT).show();
                                }
                            });

                            // Добавление кнопки в layout
                            layout.addView(button);
                        }

                        // Создание диалога перед его закрытием
                        AlertDialog dialog = new AlertDialog.Builder(this)
                                .setTitle(name)
                                .setView(layout)
                                .setCancelable(true) // Позволяет закрыть диалог при нажатии вне его
                                .create();

                        // Показываем диалог
                        dialog.show();
                    }
                });
    }





    // Диалог для добавления подарка
    private void showAddGiftDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_gift, null);
        builder.setView(dialogView);

        EditText editTextGiftName = dialogView.findViewById(R.id.editTextGiftName);
        EditText editTextGiftLink = dialogView.findViewById(R.id.editTextGiftLink);
        EditText editTextGiftStore = dialogView.findViewById(R.id.editTextGiftStore);

        builder.setPositiveButton("Дадаць", (dialog, which) -> {
            String giftName = editTextGiftName.getText().toString().trim();
            String giftLink = editTextGiftLink.getText().toString().trim();
            String giftStore = editTextGiftStore.getText().toString().trim();
            boolean isReserved = false;

            if (!giftName.isEmpty() && !giftLink.isEmpty() && !giftStore.isEmpty()) {
                addGiftToDatabase(giftName, giftLink, giftStore, isReserved); // Pass isReserved
            } else {
                Toast.makeText(UserInfoActivity.this, "Калі ласка, запоўніце ўсе палі", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Адмяніць", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }


    // Добавление подарка в базу данных
    private void addGiftToDatabase(String name, String link, String store, boolean isReserved) {
        Map<String, Object> gift = new HashMap<>();
        gift.put("name", name);
        gift.put("link", link);
        gift.put("store", store);
        gift.put("isReserved", isReserved); // Add the isReserved field

        db.collection("users").document(loggedInUsername).collection("gifts")
                .add(gift)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(UserInfoActivity.this, "Падарунак дададзены", Toast.LENGTH_SHORT).show();
                    loadGifts(); // Обновить список подарков
                })
                .addOnFailureListener(e -> Toast.makeText(UserInfoActivity.this, "Памылка пры даданні падарунка", Toast.LENGTH_SHORT).show());
    }

    // Диалог для редактирования подарка
// Диалог для редактирования подарка
    private void showEditGiftDialog(String giftId) {
        // Сначала получаем данные подарка
        db.collection("users").document(loggedInUsername).collection("gifts").document(giftId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String giftName = documentSnapshot.getString("name");
                        String giftLink = documentSnapshot.getString("link");
                        String giftStore = documentSnapshot.getString("store");

                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        LayoutInflater inflater = getLayoutInflater();
                        View dialogView = inflater.inflate(R.layout.dialog_edit_gift, null);
                        builder.setView(dialogView);

                        EditText editTextGiftName = dialogView.findViewById(R.id.editTextGiftName);
                        EditText editTextGiftLink = dialogView.findViewById(R.id.editTextGiftLink);
                        EditText editTextGiftStore = dialogView.findViewById(R.id.editTextGiftStore);

                        editTextGiftName.setText(giftName);
                        editTextGiftLink.setText(giftLink);
                        editTextGiftStore.setText(giftStore);

                        builder.setPositiveButton("Абнавіць", (dialog, which) -> {
                            String newGiftName = editTextGiftName.getText().toString().trim();
                            String newGiftLink = editTextGiftLink.getText().toString().trim();
                            String newGiftStore = editTextGiftStore.getText().toString().trim();

                            if (!newGiftName.isEmpty() && !newGiftLink.isEmpty() && !newGiftStore.isEmpty()) {
                                editGiftInFirestore(giftId, newGiftName, newGiftLink, newGiftStore);
                            } else {
                                Toast.makeText(UserInfoActivity.this, "Калі ласка, запоўніце ўсе палі", Toast.LENGTH_SHORT).show();
                            }
                        });

                        builder.setNegativeButton("Адмяніць", (dialog, which) -> dialog.dismiss());

                        builder.create().show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(UserInfoActivity.this, "Памылка пры загрузцы падарунка", Toast.LENGTH_SHORT).show());
    }


    // Обновление подарка в Firestore
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

    // Метод для проверки корректности URL
    private boolean isValidUrl(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    // Диалог для выбора действия (редактировать/удалить)
    private void showEditDeleteDialog(String giftId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите действие")
                .setItems(new String[]{"Редактировать", "Удалить"}, (dialog, which) -> {
                    if (which == 0) {
                        // Редактирование подарка
                        showEditGiftDialog(giftId);
                    } else if (which == 1) {
                        // Удаление подарка
                        deleteGift(giftId); // Здесь вызываем метод с подтверждением
                    }
                })
                .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss())
                .show();
    }


    // Метод для удаления подарка
// Метод для удаления подарка с подтверждением
    private void deleteGift(String giftId) {
        new AlertDialog.Builder(this)
                .setTitle("Подтверждение удаления")
                .setMessage("Вы уверены, что хотите удалить этот подарок?")
                .setPositiveButton("Да", (dialog, which) -> {
                    // Удаление подарка
                    db.collection("users").document(loggedInUsername).collection("gifts").document(giftId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(UserInfoActivity.this, "Падарунак выдалены", Toast.LENGTH_SHORT).show();
                                loadGifts();
                            })
                            .addOnFailureListener(e -> Toast.makeText(UserInfoActivity.this, "Памылка пры выдаленні падарунка", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Нет", (dialog, which) -> dialog.dismiss())
                .show();
    }

}
