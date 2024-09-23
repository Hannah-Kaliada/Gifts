package com.example.gifts;

public class Gift {
    private String name;
    private String link;
    private String store;
    private boolean isReserved;

    public Gift() {
    }

    public Gift(String name, String link, String store, boolean isReserved) {
        this.name = name;
        this.link = link;
        this.store = store;
        this.isReserved = isReserved;
    }

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

    public boolean isReserved() {
        return isReserved;
    }

    public void setReserved(boolean reserved) {
        isReserved = reserved;
    }
}
