package com.company.vzvod.view.usercard;

public class WorkResultsRow {
    private String category;
    private Integer month;
    private Integer year;
    private Integer total;

    public WorkResultsRow() {
    }

    public WorkResultsRow(String category, Integer month, Integer year, Integer total) {
        this.category = category;
        this.month = month;
        this.year = year;
        this.total = total;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}

