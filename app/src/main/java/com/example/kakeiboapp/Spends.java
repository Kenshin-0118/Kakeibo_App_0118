package com.example.kakeiboapp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Spends {
    private String description;
    private String amount;
    private String category;
    private Date created;

    public Spends(String description, String amount, String category, Date created) {
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.created = created;
    }

    public String getDescription() {
        return description;
    }

    public String getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public Date getCreated() {
        return created;
    }

    // Convert the Date object to a string before returning it
    public String getCreatedAsString() {
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.US);
        return formatter.format(created);
    }
}