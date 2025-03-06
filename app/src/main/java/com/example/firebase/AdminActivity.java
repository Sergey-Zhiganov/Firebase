package com.example.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {
    private EditText userEmailInput, userRoleInput;
    private FirebaseFirestore db;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> usersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        db = FirebaseFirestore.getInstance();

        userEmailInput = findViewById(R.id.user_email_input);
        userRoleInput = findViewById(R.id.user_role_input);
        Button addUserButton = findViewById(R.id.add_user_button);
        Button updateUserButton = findViewById(R.id.update_user_button);
        Button deleteUserButton = findViewById(R.id.delete_user_button);
        Button goToManagerButton = findViewById(R.id.go_to_manager_button);
        ListView usersListView = findViewById(R.id.users_list_view);

        usersList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, usersList);
        usersListView.setAdapter(adapter);

        addUserButton.setOnClickListener(v -> addUser());
        updateUserButton.setOnClickListener(v -> updateUser());
        deleteUserButton.setOnClickListener(v -> deleteUser());
        goToManagerButton.setOnClickListener(v -> startActivity(new Intent(AdminActivity.this, ManagerActivity.class)));

        usersListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedEmail = usersList.get(position);
            loadUserDetails(selectedEmail);
        });

        loadUsers();
    }

    private void loadUsers() {
        db.collection("users").get().addOnSuccessListener(queryDocumentSnapshots -> {
            usersList.clear();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                String email = document.getString("email");
                usersList.add(email);
            }
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> Toast.makeText(this, "Ошибка загрузки: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadUserDetails(String email) {
        db.collection("users").whereEqualTo("email", email).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        String role = document.getString("role");

                        userEmailInput.setText(email);
                        userRoleInput.setText(role);
                    } else {
                        Toast.makeText(this, "Пользователь не найден", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка загрузки данных: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    private void addUser() {
        String email = userEmailInput.getText().toString().trim();
        String role = userRoleInput.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(role)) {
            Toast.makeText(this, "Все поля должны быть заполнены", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("role", role);

        db.collection("users").add(user)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Пользователь добавлен", Toast.LENGTH_SHORT).show();
                    loadUsers();
                    logAction("Пользователь добавлен: " + email);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUser() {
        String email = userEmailInput.getText().toString().trim();
        String role = userRoleInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Введите email пользователя для обновления", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        if (!TextUtils.isEmpty(role)) updates.put("role", role);

        db.collection("users").whereEqualTo("email", email).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentReference docRef = queryDocumentSnapshots.getDocuments().get(0).getReference();
                        docRef.update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Данные пользователя обновлены", Toast.LENGTH_SHORT).show();
                                    loadUsers();
                                    logAction("Данные пользователя обновлены: " + email);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Пользователь не найден", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteUser() {
        String email = userEmailInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Введите email пользователя для удаления", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").whereEqualTo("email", email).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentReference docRef = queryDocumentSnapshots.getDocuments().get(0).getReference();
                        docRef.delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Пользователь удален", Toast.LENGTH_SHORT).show();
                                    loadUsers();
                                    logAction("Пользователь удален: " + email);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Пользователь не найден", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void logAction(String action) {
        Map<String, Object> log = new HashMap<>();
        log.put("action", action);
        log.put("timestamp", System.currentTimeMillis());

        db.collection("logs").add(log)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка логирования: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}