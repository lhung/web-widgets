package com.citysearch.webwidget.action;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;

import com.citysearch.webwidget.exception.CitysearchException;

public class AbstractCitySearchAction implements ServletRequestAware, ServletResponseAware {
    private HttpServletRequest httpRequest;
    private HttpServletResponse httpResponse;

    public void setServletRequest(HttpServletRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    public void setServletResponse(HttpServletResponse httpResponse) {
        this.httpResponse = httpResponse;
    }

    public String getResourceRootPath() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(httpRequest.getScheme());
        strBuilder.append("://");
        strBuilder.append(httpRequest.getServerName());
        strBuilder.append(":");
        strBuilder.append(httpRequest.getServerPort());
        strBuilder.append(httpRequest.getContextPath());
        return strBuilder.toString();
    }

    public String getTrackingUrl(String adDisplayURL, String dartTrackingUrl, String listingId,
            String publisher, String adUnitName, String adUnitSize) throws CitysearchException {
        try {
            URL url = new URL(adDisplayURL);
            int prodDetId = 12; // Click outside Citysearch
            String host = url.getHost();
            if (host.indexOf("citysearch.com") != -1) {
                prodDetId = 16;
            }

            StringBuilder strBuilder = new StringBuilder("http://pfpc.citysearch.com/pfp/ad?");
            if (dartTrackingUrl != null) {
                StringBuilder dartUrl = new StringBuilder(dartTrackingUrl);
                dartUrl.append(adDisplayURL);
                strBuilder.append("directUrl=");
                strBuilder.append(URLEncoder.encode(dartUrl.toString(), "UTF-8"));
            }
            strBuilder.append("&listingId=");
            strBuilder.append(URLEncoder.encode(listingId, "UTF-8"));
            strBuilder.append("&publisher=");
            strBuilder.append(URLEncoder.encode(publisher, "UTF-8"));
            strBuilder.append("&prodDetId=");
            strBuilder.append(prodDetId);
            strBuilder.append("&placement=");
            strBuilder.append(URLEncoder.encode(publisher + "_" + adUnitName + "_" + adUnitSize,
                    "UTF-8"));
            return strBuilder.toString();
        } catch (MalformedURLException mue) {
            throw new CitysearchException("AbstractCitySearchAction", "getTrackingUrl", mue);
        } catch (UnsupportedEncodingException excep) {
            throw new CitysearchException("AbstractCitySearchAction", "getTrackingUrl", excep);
        }
    }
}
