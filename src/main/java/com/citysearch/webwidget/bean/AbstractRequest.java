package com.citysearch.webwidget.bean;

/**
 * The abstract class that contains the common Request field across APIs
 * 
 * @author Aspert Benjamin
 * 
 */
public abstract class AbstractRequest {
    protected String publisher;
    protected boolean customerOnly;
    protected String format;
    protected String adUnitName;
    protected String adUnitSize;
    protected Integer displaySize;
    protected String clientIP;
    protected String dartClickTrackUrl;
    protected String callBackFunction;
    protected String callBackUrl;
    protected String where;
    protected String what;
    protected String latitude;
    protected String longitude;
    protected String radius;
    
    public String getCallBackFunction() {
        return callBackFunction;
    }

    public void setCallBackFunction(String callBackFunction) {
        this.callBackFunction = callBackFunction;
    }

    public String getCallBackUrl() {
        return callBackUrl;
    }

    public void setCallBackUrl(String callBackUrl) {
        this.callBackUrl = callBackUrl;
    }

    public String getDartClickTrackUrl() {
        return dartClickTrackUrl;
    }

    public void setDartClickTrackUrl(String dartClickTrackUrl) {
        this.dartClickTrackUrl = dartClickTrackUrl;
    }

    public String getClientIP() {
        return clientIP;
    }

    public void setClientIP(String clientIP) {
        this.clientIP = clientIP;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public boolean isCustomerOnly() {
        return customerOnly;
    }

    public void setCustomerOnly(boolean customerOnly) {
        this.customerOnly = customerOnly;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getAdUnitName() {
        return adUnitName;
    }

    public void setAdUnitName(String adUnitName) {
        this.adUnitName = adUnitName;
    }

    public String getAdUnitSize() {
        return adUnitSize;
    }

    public void setAdUnitSize(String adUnitSize) {
        this.adUnitSize = adUnitSize;
    }

    public Integer getDisplaySize() {
        return displaySize;
    }

    public void setDisplaySize(Integer displaySize) {
        this.displaySize = displaySize;
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public String getWhat() {
        return what;
    }

    public void setWhat(String what) {
        this.what = what;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getRadius() {
        return radius;
    }

    public void setRadius(String radius) {
        this.radius = radius;
    }
}
