package com.android.kemilingcom;

public class product {
    private int id;
    private String title;
    private int price;
    private String location;
    private float rating;
    private String imageUrl;
    private int weekdayTicket;
    private int weekendTicket;
    private String category; // Tambahkan kategori produk


    // Constructor
    public product(int id, String title, int price, int weekdayTicket, int weekendTicket, String category, String location, float rating, String imageUrl) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.weekdayTicket = weekdayTicket;
        this.weekendTicket = weekendTicket;
        this.category = category;
        this.location = location;
        this.rating = rating;
        this.imageUrl = imageUrl;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getPrice() {
        return price;
    }
    public int getWeekdayTicket() {
        return weekdayTicket;
    }

    public int getWeekendTicket() {
        return weekendTicket;
    }

    public String getCategory() {
        return category;
    }

    public String getLocation() {
        return location;
    }

    public float getRating() {
        return rating;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
