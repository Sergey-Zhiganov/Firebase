package com.example.firebase;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    Button logout_button;
//    FirebaseFirestore db;
//    private ListView servicesListView;
//    private List<String> servicesList;
//    private List<String> idServices;
//    private ArrayAdapter<String> adapter;
//    private Calendar calendar;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

//        db = FirebaseFirestore.getInstance();
//        servicesListView = findViewById(R.id.servicesListView);
//        Button bookServices = findViewById(R.id.bookServicesButton);
//        servicesList = new ArrayList<>();
//        idServices = new ArrayList<>();
//        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, servicesList);
//        servicesListView.setAdapter(adapter);
//
//        servicesListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
//        loadServices();
//
//        bookServices.setOnClickListener(v -> {
//            int selectedPosition = servicesListView.getCheckedItemPosition();
//            if (selectedPosition != ListView.INVALID_POSITION) {
//                String idServiced = idServices.get(selectedPosition);
//                showDateTimePickerDialog(idServiced);
//            } else {
//                Toast.makeText(MainActivity.this, "Выберите услугу", Toast.LENGTH_SHORT).show();
//            }
//        });

        firebaseAuth = FirebaseAuth.getInstance();

        logout_button = findViewById(R.id.logout_button);

        logout_button.setOnClickListener(v -> {
            firebaseAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });
    }

//    private void loadServices() {
//        db.collection("services").get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    servicesList.clear();
//                    idServices.clear();
//
//                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
//                        String serviceName = document.getString("serviceName");
//                        String id = document.getId();
//
//                        servicesList.add(serviceName);
//                        idServices.add(id);
//                    }
//
//                    adapter.notifyDataSetChanged();
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(MainActivity.this, "Ошибка загрузки услуг", Toast.LENGTH_SHORT).show();
//                });
//
//    }
//
//    private void showDateTimePickerDialog(String idServiced) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Выберите дату и время.");
//        View view = getLayoutInflater().inflate(R.layout.show_date_time_picker, null);
//        builder.setView(view);
//
//        TextView dateField = view.findViewById(R.id.dateField);
//        TextView timeField = view.findViewById(R.id.timeField);
//
//        dateField.setOnClickListener(v -> showDatePickerDialog(dateField));
//        timeField.setOnClickListener(v -> showTimePickerDialog(timeField));
//
//        builder.setPositiveButton("Записаться", (dialog,which) -> {
//            String date = dateField.getText().toString().trim();
//            String time = timeField.getText().toString().trim();
//
//            if (date.isEmpty() || time.isEmpty()) {
//                Toast.makeText(this, "Заполните дату и время.", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            bookService(idServiced, date, time);
//        });
//
//        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
//        builder.create().show();
//    }
//
//    private void showTimePickerDialog(TextView timeField) {
//        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
//                (view, hourOfDay, minute) -> {
//                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
//                calendar.set(Calendar.MINUTE, minute);
//        },
//                calendar.get(Calendar.HOUR_OF_DAY),
//                calendar.get(Calendar.MINUTE),
//                true
//        );
//        timePickerDialog.show();
//    }
//
//    private void showDatePickerDialog(TextView dateField) {
//        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
//                (view, year, month, dateOfMonth) -> {
//                    calendar.set(Calendar.YEAR, year);
//                    calendar.set(Calendar.MONTH, month);
//                    calendar.set(Calendar.DAY_OF_MONTH, dateOfMonth);
//                },
//                calendar.get(Calendar.YEAR),
//                calendar.get(Calendar.MONTH),
//                calendar.get(Calendar.DAY_OF_MONTH)
//        );
//        datePickerDialog.show();
//    }
//
//    private void bookService(String idServiced, String date, String time) {
//        FirebaseUser user =  firebaseAuth.getCurrentUser();
//
//        if (user != null) {
//            String clientName = user.getEmail();
//            db.collection("services").document(idServiced).get()
//                    .addOnSuccessListener(documentSnapshot -> {
//                        if (documentSnapshot.exists()) {
//                            String serviceName = documentSnapshot.getString("service");
//
//                            Map<String, Object> appointment = new HashMap<>();
//                            appointment.put("clientId", user.getUid());
//                            appointment.put("clientName", user.getEmail());
//                            appointment.put("idService", idServiced);
//                            appointment.put("serviceName", serviceName);
//                            appointment.put("date", date);
//                            appointment.put("time", time);
//
//                            db.collection("appointment").add(appointment)
//                                    .addOnSuccessListener(documentReference -> {
//                                        Toast.makeText(this, "Запись создана", Toast.LENGTH_SHORT).show();
//                                    })
//                                    .addOnFailureListener(e -> {
//                                        Toast.makeText(this, "Ошибка создания записи: " + e.getMessage(),
//                                                Toast.LENGTH_SHORT).show();
//                                    });
//                        } else {
//                            Toast.makeText(this, "Услуга не найдена", Toast.LENGTH_SHORT).show();
//                        }
//                    })
//                    .addOnFailureListener(e -> {
//                        Toast.makeText(this, "Ошибка загрузки данных услуги: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    });
//        } else {
//            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
//        }
//    }
}
