package com.teknisio.mobile.model.request;

public class CreateReviewRequest {
    public Integer rating;
    public String comment;

    public CreateReviewRequest(Integer rating, String comment) {
        this.rating = rating;
        this.comment = comment;
    }
}
