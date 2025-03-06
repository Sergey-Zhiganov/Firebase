package com.example.firebase;

public class Service {
    private String id;
    private String name;
    private String category;
    private String description;
    private double price;

    public Service() {}

    public Service(String id, String name, String category, String description, double price) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
