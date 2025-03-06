package com.example.firebase;

import com.google.firebase.firestore.FirebaseFirestore;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {
    private EditText email_input, password_input;
    private FirebaseAuth firebaseAuth;
    private String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();

        email_input = findViewById(R.id.email_input);
        password_input = findViewById(R.id.password_input);
        Button register_button = findViewById(R.id.register_button);
        TextView login_prompt = findViewById(R.id.login_prompt);

        register_button.setOnClickListener(v -> validData());

        login_prompt.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void validData() {
        email = email_input.getText().toString().trim();
        password = password_input.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            email_input.setError("Неправильный формат почты.");
        } else if (TextUtils.isEmpty(password)) {
            password_input.setError("Пароль не должен быть пустым.");
        } else if (password.length() < 6) {
            password_input.setError("Длина пароля должна быть не менее 6 символов.");
        } else if (!password.matches(".*[A-Za-z].*") || !password.matches(".*\\d.*")) {
            password_input.setError("Пароль должен содержать буквы и цифры.");
        } else {
            firebaseSignUp();
        }
    }

    private void firebaseSignUp() {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        String userId = user.getUid();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        User newUser = new User(userId, email, "user");

                        db.collection("users").document(userId).set(newUser)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(SignUpActivity.this, "Регистрация успешна", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(SignUpActivity.this, "Ошибка сохранения данных: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                );
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SignUpActivity.this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
