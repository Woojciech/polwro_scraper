package com.suszkolabs.entity;

public class Teacher {

    private int id;
    private String category;
    private String fullName;
    private String academicTitle;
    private double averageRating;
    private String detailsLink;

    public Teacher(int id, String category, String firstName, String academicTitle, double averageRating, String detailsLink) {
        this.id = id;
        this.category = category;
        this.fullName = firstName;
        this.academicTitle = academicTitle;
        this.averageRating = averageRating;
        this.detailsLink = detailsLink;
    }

    public Teacher(String category, String firstName, String academicTitle, double averageRating, String detailsLink) {
        this.category = category;
        this.fullName = firstName;
        this.academicTitle = academicTitle;
        this.averageRating = averageRating;
        this.detailsLink = detailsLink;
    }

    public Teacher() {

    }

    @Override
    public String toString() {
        return "Teacher{" +
                "category='" + category + '\'' +
                ", fullName='" + fullName + '\'' +
                ", academicTitle='" + academicTitle + '\'' +
                ", averageRating=" + averageRating +
                ", detailsLink='" + detailsLink + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAcademicTitle() {
        return academicTitle;
    }

    public void setAcademicTitle(String academicTitle) {
        this.academicTitle = academicTitle;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public String getDetailsLink() {
        return detailsLink;
    }

    public void setDetailsLink(String detailsLink) {
        this.detailsLink = detailsLink;
    }
}
