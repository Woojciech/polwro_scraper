package com.suszkolabs.entity;

import com.beust.ah.A;

import java.util.ArrayList;
import java.util.Objects;

public class Teacher {

    private static int currentId = 1;

    private int id;
    private String category;
    private String fullName;
    private String academicTitle;
    private double averageRating;
    private String detailsLink;
    private ArrayList<Review> reviews;

    public Teacher(int id, String category, String firstName, String academicTitle, double averageRating, String detailsLink) {
        this.id = id;
        this.category = category;
        this.fullName = firstName;
        this.academicTitle = academicTitle;
        this.averageRating = averageRating;
        this.detailsLink = detailsLink;
        this.reviews = new ArrayList<>();
    }

    public Teacher(String category, String firstName, String academicTitle, double averageRating, String detailsLink) {
        this.category = category;
        this.fullName = firstName;
        this.academicTitle = academicTitle;
        this.averageRating = averageRating;
        this.detailsLink = detailsLink;
        this.reviews = new ArrayList<>();
    }

    public Teacher() {
        this.reviews = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Teacher teacher = (Teacher) o;
        return id == teacher.id && Double.compare(teacher.averageRating, averageRating) == 0 && Objects.equals(category, teacher.category) && Objects.equals(fullName, teacher.fullName) && Objects.equals(academicTitle, teacher.academicTitle) && Objects.equals(detailsLink, teacher.detailsLink) && Objects.equals(reviews, teacher.reviews);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, category, fullName, academicTitle, averageRating, detailsLink, reviews);
    }

    @Override
    public String toString() {
        return "Teacher{" +
                "id=" + id +
                ", category='" + category + '\'' +
                ", fullName='" + fullName + '\'' +
                ", academicTitle='" + academicTitle + '\'' +
                ", averageRating=" + averageRating +
                ", detailsLink='" + detailsLink + '\'' +
                ", reviews=" + reviews +
                '}';
    }

    public void addReview(Review review){
        reviews.add(review);
    }

    public static int getCurrentId() {
        return currentId;
    }

    public static void setCurrentId(int currentId) {
        Teacher.currentId = currentId;
    }

    public ArrayList<Review> getReviews() {
        return reviews;
    }

    public void setReviews(ArrayList<Review> reviews) {
        this.reviews = reviews;
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
