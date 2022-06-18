package com.suszkolabs.entity;

public class Review {

    private int id;
    private String courseName;
    private double givenRating;
    private String title;
    private String review;
    private String reviewer;
    private String postDate;
    private int teacherId;

    public Review() {

    }

    public Review(int id, String courseName, double givenRating, String title, String review, String reviewer, String postDate, int teacherId) {
        this.id = id;
        this.courseName = courseName;
        this.givenRating = givenRating;
        this.title = title;
        this.review = review;
        this.reviewer = reviewer;
        this.postDate = postDate;
        this.teacherId = teacherId;
    }

    public Review(String courseName, double givenRating, String title, String review, String reviewer, String postDate, int teacherId) {
        this.courseName = courseName;
        this.givenRating = givenRating;
        this.title = title;
        this.review = review;
        this.reviewer = reviewer;
        this.postDate = postDate;
        this.teacherId = teacherId;
    }

    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", courseName='" + courseName + '\'' +
                ", givenRating=" + givenRating +
                ", title='" + title + '\'' +
                ", review='" + review + '\'' +
                ", reviewer='" + reviewer + '\'' +
                ", postDate='" + postDate + '\'' +
                ", teacherId=" + teacherId +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public double getGivenRating() {
        return givenRating;
    }

    public void setGivenRating(double givenRating) {
        this.givenRating = givenRating;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getReviewer() {
        return reviewer;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }

    public String getPostDate() {
        return postDate;
    }

    public void setPostDate(String postDate) {
        this.postDate = postDate;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }
}
