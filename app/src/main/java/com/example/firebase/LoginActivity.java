package com.example.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private EditText email_input, password_input;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        email_input = findViewById(R.id.email_input);
        password_input = findViewById(R.id.password_input);
        Button login_button = findViewById(R.id.login_button);
        TextView signup_prompt = findViewById(R.id.signup_prompt);

        login_button.setOnClickListener(v -> validData());

        signup_prompt.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            finish();
        });
    }

    private void validData() {
        String email = email_input.getText().toString().trim();
        String password = password_input.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            email_input.setError("Неправильный формат почты.");
            return;
        } else if (TextUtils.isEmpty(password)) {
            password_input.setError("Пароль не должен быть пустым.");
            return;
        } else if (password.length() < 6) {
            password_input.setError("Пароль должен содержать не менее 6 символов.");
            return;
        }

        firebaseLogin(email, password);
    }

    private void firebaseLogin(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(LoginActivity.this, "Успешный вход", Toast.LENGTH_SHORT).show();
                    login();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginActivity.this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void login() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String role = documentSnapshot.getString("role");
                            if (role != null) {
                                switch (role) {
                                    case "admin":
                                        startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                                        break;
                                    case "manager":
                                        startActivity(new Intent(LoginActivity.this, ManagerActivity.class));
                                        break;
                                    default:
                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                        break;
                                }
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Ошибка: роль не найдена", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Ошибка: пользователь не найден", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(LoginActivity.this, "Ошибка загрузки данных: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}