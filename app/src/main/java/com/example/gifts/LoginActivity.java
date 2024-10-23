package com.example.gifts;

import static androidx.fragment.app.FragmentManager.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonLogin, buttonSignUp;
    private ImageButton buttonShowPassword; // Добавляем переменную для кнопки "глаз"
    private boolean isPasswordVisible = false; // Переменная для отслеживания состояния видимости пароля
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = FirebaseFirestore.getInstance();

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        buttonShowPassword = findViewById(R.id.buttonShowPassword); // Найдем кнопку "глаз" в макете

        // Логика для показа/скрытия пароля
        buttonShowPassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                // Скрываем пароль
                editTextPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                buttonShowPassword.setImageResource(R.drawable.baseline_visibility_24); // Иконка "показать"
            } else {
                // Показываем пароль
                editTextPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                buttonShowPassword.setImageResource(R.drawable.baseline_visibility_off_24); // Иконка "скрыть"
            }
            editTextPassword.setSelection(editTextPassword.getText().length()); // Возвращаем курсор в конец
            isPasswordVisible = !isPasswordVisible; // Меняем состояние видимости
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUsername.getText().toString();
                String password = editTextPassword.getText().toString();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Калі ласка, увядзіце ўсе даныя", Toast.LENGTH_SHORT).show();
                } else {
                    String hashedPassword = Utils.hashPassword(password);
                    db.collection("users")
                            .whereEqualTo("username", username)
                            .whereEqualTo("password", hashedPassword)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    Intent intent = new Intent(LoginActivity.this, UserInfoActivity.class);
                                    intent.putExtra("USERNAME", username);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Няправільнае імя карыстальніка або пароль", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(LoginActivity.this, "Памылка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }
        });

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}
