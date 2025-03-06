package com.example.firebase;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.AdapterView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ManagerActivity extends AppCompatActivity {
    private EditText serviceNameInput, serviceCategoryInput, servicePriceInput, searchServiceInput;
    private Spinner serviceCategoryFilter;
    private FirebaseFirestore db;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> servicesList;
    private ArrayList<String> filteredServicesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        db = FirebaseFirestore.getInstance();

        serviceNameInput = findViewById(R.id.service_name_input);
        serviceCategoryInput = findViewById(R.id.service_category_input);
        servicePriceInput = findViewById(R.id.service_price_input);
        searchServiceInput = findViewById(R.id.search_service_input);
        serviceCategoryFilter = findViewById(R.id.service_category_filter);
        Button addServiceButton = findViewById(R.id.add_service_button);
        Button updateServiceButton = findViewById(R.id.update_service_button);
        Button deleteServiceButton = findViewById(R.id.delete_service_button);
        ListView servicesListView = findViewById(R.id.services_list_view);

        servicesList = new ArrayList<>();
        filteredServicesList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filteredServicesList);
        servicesListView.setAdapter(adapter);

        addServiceButton.setOnClickListener(v -> addService());
        updateServiceButton.setOnClickListener(v -> updateService());
        deleteServiceButton.setOnClickListener(v -> deleteService());

        searchServiceInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                filterServices();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        serviceCategoryFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                filterServices();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        loadCategories();
        loadServices();

        servicesListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedService = filteredServicesList.get(position);

            String[] parts = selectedService.split(" - ");
            String serviceName = parts[0];
            String serviceCategory = parts.length > 1 ? parts[1] : "";

            serviceNameInput.setText(serviceName);
            serviceCategoryInput.setText(serviceCategory);

            db.collection("services").whereEqualTo("name", serviceName).get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            QueryDocumentSnapshot document = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                            String servicePrice = document.getString("price");
                            servicePriceInput.setText(servicePrice);
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Ошибка загрузки цены: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    private void loadCategories() {
        db.collection("services").get().addOnSuccessListener(queryDocumentSnapshots -> {
            ArrayList<String> categories = new ArrayList<>();
            categories.add("Все категории");

            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                String category = document.getString("category");
                if (category != null && !categories.contains(category)) {
                    categories.add(category);
                }
            }

            ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            serviceCategoryFilter.setAdapter(categoryAdapter);
        }).addOnFailureListener(e -> Toast.makeText(this, "Ошибка загрузки категорий: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadServices() {
        db.collection("services").get().addOnSuccessListener(queryDocumentSnapshots -> {
            servicesList.clear();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                String serviceName = document.getString("name");
                String serviceCategory = document.getString("category");
                servicesList.add(serviceName + " - " + serviceCategory);
            }
            filterServices();
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> Toast.makeText(this, "Ошибка загрузки: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void filterServices() {
        String searchQuery = searchServiceInput.getText().toString().trim().toLowerCase();

        String selectedCategory = serviceCategoryFilter.getSelectedItem() != null ?
                serviceCategoryFilter.getSelectedItem().toString().trim().toLowerCase() : "";

        filteredServicesList.clear();

        for (String serviceName : servicesList) {
            String[] parts = serviceName.split(" - ");
            String serviceCategory = parts.length > 1 ? parts[1].toLowerCase() : "";

            if ("все категории".equals(selectedCategory) ||
                    (serviceCategory.contains(selectedCategory) && serviceName.toLowerCase().contains(searchQuery))) {
                filteredServicesList.add(serviceName);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void addService() {
        String name = serviceNameInput.getText().toString().trim();
        String category = serviceCategoryInput.getText().toString().trim();
        String price = servicePriceInput.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(category) || TextUtils.isEmpty(price)) {
            Toast.makeText(this, "Все поля должны быть заполнены", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> service = new HashMap<>();
        service.put("name", name);
        service.put("category", category);
        service.put("price", price);

        db.collection("services").add(service)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Услуга добавлена", Toast.LENGTH_SHORT).show();
                    loadServices();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateService() {
        String name = serviceNameInput.getText().toString().trim();
        String category = serviceCategoryInput.getText().toString().trim();
        String price = servicePriceInput.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Введите название услуги для обновления", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        if (!TextUtils.isEmpty(category)) updates.put("category", category);
        if (!TextUtils.isEmpty(price)) updates.put("price", price);

        db.collection("services").whereEqualTo("name", name).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentReference docRef = queryDocumentSnapshots.getDocuments().get(0).getReference();
                        docRef.update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Услуга обновлена", Toast.LENGTH_SHORT).show();
                                    loadServices();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "Услуга не найдена", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void deleteService() {
        String name = serviceNameInput.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Введите название услуги для удаления", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("services").whereEqualTo("name", name).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentReference docRef = queryDocumentSnapshots.getDocuments().get(0).getReference();
                        docRef.delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Услуга удалена", Toast.LENGTH_SHORT).show();
                                    loadServices();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "Услуга не найдена", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
