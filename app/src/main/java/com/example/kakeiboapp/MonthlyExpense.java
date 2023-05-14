package com.example.kakeiboapp;

import java.util.Date;

public class MonthlyExpense {
    private String period;
    private double expenses;
    private double limit;

    public MonthlyExpense(String period, double expenses, double limit) {
        this.period = period;
        this.expenses = expenses;
        this.limit = limit;
    }

    public String getPeriod() {
        return period;
    }

    public double getExpense() {
        return expenses;
    }

    public double getSaved() {
        return limit - expenses;
    }

}
