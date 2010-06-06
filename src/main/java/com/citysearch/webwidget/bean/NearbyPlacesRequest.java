package com.citysearch.webwidget.bean;

/**
 * Request object for PFP API
 * 
 * @author Aspert Benjamin
 * 
 */
public class NearbyPlacesRequest extends AbstractRequest {
    private String what;
    private String where;
    private String latitude;
    private String longitude;
    private String tags;
    private String radius;
    private String callBackFunction;
    private String callBackUrl;
    
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

    public String getWhat() {
        return what;
    }

    public void setWhat(String what) {
        this.what = what;
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
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

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getRadius() {
        return radius;
    }

    public void setRadius(String radius) {
        this.radius = radius;
    }

}
