package com.example.gifts;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonRegister;
    private ImageButton buttonShowPassword;
    private FirebaseFirestore db;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = FirebaseFirestore.getInstance();

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonShowPassword = findViewById(R.id.buttonShowPassword);

        buttonRegister.setOnClickListener(v -> {
            String username = editTextUsername.getText().toString().trim();
            String password = editTextPassword.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Калі ласка, запоўніце ўсе палі", Toast.LENGTH_SHORT).show();
            } else {
                checkUsernameExists(username, password);
            }
        });

        buttonShowPassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                editTextPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                buttonShowPassword.setImageResource(R.drawable.baseline_visibility_24);
            } else {
                editTextPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                buttonShowPassword.setImageResource(R.drawable.baseline_visibility_off_24);
            }
            editTextPassword.setSelection(editTextPassword.getText().length());
            isPasswordVisible = !isPasswordVisible;
        });
    }

    private void checkUsernameExists(String username, String password) {
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        showUsernameExistsDialog();
                    } else {
                        if (!isPasswordComplex(password)) {
                            showPasswordComplexityDialog();
                        } else {
                            String hashedPassword = Utils.hashPassword(password);
                            User user = new User(username, hashedPassword);

                            db.collection("users")
                                    .add(user)
                                    .addOnSuccessListener(documentReference -> {
                                        Toast.makeText(RegisterActivity.this, "Карыстальнік зарэгістраваны паспяхова", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(RegisterActivity.this, "Памылка захавання карыстальніка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                });
    }

    private boolean isPasswordComplex(String password) {
        return password.length() >= 8 &&
                password.matches(".*\\d.*") &&
                password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
    }

    private void showPasswordComplexityDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Патрабаванні да складанасці пароля")
                .setMessage("Пароль павінен быць не менш за 8 сімвалаў, уключаючы хаця б адну лічбу і адзін спецыяльны сімвал.")
                .setPositiveButton("OK", (dialog, which) -> {})
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void showUsernameExistsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Імя карыстальніка ўжо існуе")
                .setMessage("Гэта імя карыстальніка ўжо занята. Калі ласка, выберыце іншае.")
                .setPositiveButton("OK", (dialog, which) -> {})
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
