package com.citysearch.webwidget.bean;

/**
 * Response bean class for Profile API
 * 
 * @author Aspert Benjamin
 * 
 */
public class Profile {
    private Address address;
    private String phone;
    private String profileUrl;
    private String sendToFriendUrl;
    private String imageUrl;
    private String reviewsUrl;
    private String websiteUrl;
    private String menuUrl;
    private String reservationUrl;
    private String mapUrl;
    private Review review;

    public String getReviewsUrl() {
        return reviewsUrl;
    }

    public void setReviewsUrl(String reviewsUrl) {
        this.reviewsUrl = reviewsUrl;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getMenuUrl() {
        return menuUrl;
    }

    public void setMenuUrl(String menuUrl) {
        this.menuUrl = menuUrl;
    }

    public String getReservationUrl() {
        return reservationUrl;
    }

    public void setReservationUrl(String reservationUrl) {
        this.reservationUrl = reservationUrl;
    }

    public String getMapUrl() {
        return mapUrl;
    }

    public void setMapUrl(String mapUrl) {
        this.mapUrl = mapUrl;
    }

    public Review getReview() {
        return review;
    }

    public void setReview(Review review) {
        this.review = review;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getSendToFriendUrl() {
        return sendToFriendUrl;
    }

    public void setSendToFriendUrl(String sendToFriendUrl) {
        this.sendToFriendUrl = sendToFriendUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}
