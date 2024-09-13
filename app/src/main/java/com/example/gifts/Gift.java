package com.example.gifts;

public class Gift {
    private String name;
    private String link; // Optional
    private String store; // Optional

    // Default constructor for Firestore
    public Gift() {
    }

    // Constructor with all fields
    public Gift(String name, String link, String store) {
        this.name = name;
        this.link = link;
        this.store = store;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }
}
