package com.citysearch.webwidget.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

import com.citysearch.webwidget.bean.Offer;
import com.citysearch.webwidget.bean.OffersRequest;
import com.citysearch.webwidget.bean.Profile;
import com.citysearch.webwidget.bean.ProfileRequest;
import com.citysearch.webwidget.exception.CitysearchException;
import com.citysearch.webwidget.exception.InvalidHttpResponseException;
import com.citysearch.webwidget.exception.InvalidRequestParametersException;
import com.citysearch.webwidget.util.APIFieldNameConstants;
import com.citysearch.webwidget.util.CommonConstants;
import com.citysearch.webwidget.util.HelperUtil;
import com.citysearch.webwidget.util.PropertiesLoader;

public class OffersHelper {

    private final static String PROPERTY_OFFERS_URL = "offers.url";
    private Logger log = Logger.getLogger(getClass());
    private String rootPath;

    private static final String OFFER = "offer";
    private static final String RPP_OFFERS = "2";
    private static final String CITY = "city";
    private static final String ATTRIBUTION_SOURCE = "attribution_source";
    private static final String CS_RATING = "cs_rating";
    private static final String REVIEW_COUNT = "review_count";
    private static final String IMAGE_URL = "image_url";
    private static final String LATITUDE = "latitude";
    private static final String LISTING_ID = "listing_id";
    private static final String LISTING_NAME = "listing_name";
    private static final String LONGITUDE = "longitude";
    private static final String OFFER_DESCRIPTION = "offer_description";
    private static final String OFFER_ID = "offer_id";
    private static final String OFFER_TITLE = "offer_title";
    private static final String REFERENCE_ID = "reference_id";
    private static final String STATE = "state";
    private static final String STREET = "street";
    private static final String ZIP = "zip";
    private static final String OFFER_API_PUBLISHER_HEADER = "X-Publisher";

    public OffersHelper(String rootPath) {
        this.rootPath = rootPath;
    }

    /**
     * Constructs the Offers API query string with all the supplied parameters
     * 
     * @return String
     * @throws CitysearchException
     */
    private String getQueryString(OffersRequest request) throws CitysearchException {
        log.info("=========Start offersHelper getQueryString()============================ >");
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(HelperUtil.constructQueryParam(APIFieldNameConstants.PUBLISHER,
                request.getPublisher().trim()));
        strBuilder.append(CommonConstants.SYMBOL_AMPERSAND);

        strBuilder.append(HelperUtil.constructQueryParam(APIFieldNameConstants.RPP, RPP_OFFERS));
        strBuilder.append(CommonConstants.SYMBOL_AMPERSAND);

        if (!StringUtils.isBlank(request.getWhat())) {
            strBuilder.append(HelperUtil.constructQueryParam(APIFieldNameConstants.WHAT,
                    request.getWhat()));
        }
        if (!StringUtils.isBlank(request.getWhere())) {
            strBuilder.append(CommonConstants.SYMBOL_AMPERSAND);
            strBuilder.append(HelperUtil.constructQueryParam(APIFieldNameConstants.WHERE,
                    request.getWhere()));
        }
        if (!StringUtils.isBlank(request.getTag())) {
            strBuilder.append(CommonConstants.SYMBOL_AMPERSAND);
            strBuilder.append(HelperUtil.constructQueryParam(APIFieldNameConstants.TAG,
                    request.getTag()));
        }
        if (!StringUtils.isBlank(request.getLatitude())) {
            strBuilder.append(CommonConstants.SYMBOL_AMPERSAND);
            strBuilder.append(HelperUtil.constructQueryParam(APIFieldNameConstants.LATITUDE,
                    request.getLatitude().trim()));
        }
        if (!StringUtils.isBlank(request.getLongitude())) {
            strBuilder.append(CommonConstants.SYMBOL_AMPERSAND);
            strBuilder.append(HelperUtil.constructQueryParam(APIFieldNameConstants.LONGITUDE,
                    request.getLongitude().trim()));
        }
        if (!StringUtils.isBlank(request.getExpiresBefore())) {
            strBuilder.append(CommonConstants.SYMBOL_AMPERSAND);
            strBuilder.append(HelperUtil.constructQueryParam(APIFieldNameConstants.EXPIRES_BEFORE,
                    request.getExpiresBefore().trim()));
        }
        if (!StringUtils.isBlank(request.getCustomerHasbudget())) {
            strBuilder.append(CommonConstants.SYMBOL_AMPERSAND);
            strBuilder.append(HelperUtil.constructQueryParam(
                    APIFieldNameConstants.CUSTOMER_HASBUDGET,
                    String.valueOf(request.getCustomerHasbudget().trim())));
        }
        if (!StringUtils.isBlank(request.getRadius())) {
            strBuilder.append(CommonConstants.SYMBOL_AMPERSAND);
            strBuilder.append(HelperUtil.constructQueryParam(APIFieldNameConstants.RADIUS,
                    request.getRadius().trim()));
        }
        if (!StringUtils.isBlank(request.getCallbackFunction())) {
            strBuilder.append(CommonConstants.SYMBOL_AMPERSAND);
            strBuilder.append(HelperUtil.constructQueryParam(APIFieldNameConstants.CALLBACK,
                    request.getCallbackFunction().trim()));
        }
        log.info("=========Start offersHelper getQueryString() querystring is ============================ >"
                + strBuilder);
        return strBuilder.toString();
    }

    /**
     * Validates the request. If any of the parameters are missing, throws Citysearch Exception
     * 
     * @throws CitysearchException
     */
    public void validateRequest(OffersRequest request) throws InvalidRequestParametersException,
            CitysearchException {
        log.info("=========Start offersHelper validateRequest()============================ >");
        List<String> errors = new ArrayList<String>();
        Properties errorProperties = PropertiesLoader.getErrorProperties();

        if (StringUtils.isBlank(request.getPublisher())) {
            errors.add(errorProperties.getProperty(CommonConstants.PUBLISHER_ERROR_CODE));
        }
        if ((StringUtils.isBlank(request.getLatitude()) && !StringUtils.isBlank(request.getLongitude()))
                || (StringUtils.isBlank(request.getLongitude()) && !StringUtils.isBlank(request.getLatitude()))) {
            errors.add(errorProperties.getProperty(CommonConstants.LAT_LONG_ERROR));
        }
        if (!StringUtils.isBlank(request.getRadius())
                && !StringUtils.isBlank(request.getLatitude())
                && !StringUtils.isBlank(request.getLongitude())
                && !StringUtils.isBlank(request.getWhere())) {
            errors.add(errorProperties.getProperty(CommonConstants.LOCATION_ERROR));
        }
        if (StringUtils.isBlank(request.getLatitude())
                && StringUtils.isBlank(request.getLongitude())
                && StringUtils.isBlank(request.getWhere())) {
            errors.add(errorProperties.getProperty(CommonConstants.WHERE_ERROR_CODE));
        }
        if (!StringUtils.isBlank(request.getLatitude())
                && StringUtils.isBlank(request.getLongitude())) {
            errors.add(errorProperties.getProperty(CommonConstants.LONGITUDE_ERROR));
        } else if (StringUtils.isBlank(request.getLatitude())
                && !StringUtils.isBlank(request.getLongitude())) {
            errors.add(errorProperties.getProperty(CommonConstants.LATITUDE_ERROR));
        }
        if (!StringUtils.isBlank(request.getLatitude())
                && !StringUtils.isBlank(request.getLongitude())
                && (StringUtils.isBlank(request.getRadius())
                        || (new Integer(request.getRadius()).intValue() > 25) || (new Integer(
                        request.getRadius()).intValue() < 1))) {
            errors.add(errorProperties.getProperty(CommonConstants.RADIUS_ERROR));
        }
        if (StringUtils.isBlank(request.getWhere())) {
            errors.add(errorProperties.getProperty(CommonConstants.ZIPCODE_ERROR));
        }
        if (StringUtils.isBlank(request.getClientIP())) {
            errors.add(errorProperties.getProperty(CommonConstants.CLIENT_IP_ERROR_CODE));
        }
        if (!errors.isEmpty()) {
            throw new InvalidRequestParametersException(this.getClass().getName(),
                    "validateRequest", "Invalid parameters.", errors);
        }
        log.info("=========End offersHelper validateRequest()============================ >");
    }

    /**
     * Get the offers from Offers API
     * 
     * @param request
     * @return List of Offers
     * @throws InvalidRequestParametersException
     * @throws CitysearchException
     */
    public List<Offer> getOffers(OffersRequest request) throws InvalidRequestParametersException,
            CitysearchException {
        log.info("=========Start offersHelper getOffers()============================ >");
        validateRequest(request);

        Properties properties = PropertiesLoader.getAPIProperties();
        String urlString = properties.getProperty(PROPERTY_OFFERS_URL) + getQueryString(request);
        Document responseDocument = null;
        try {
            String publisherHdr = request.getPublisher().trim();
            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put(OFFER_API_PUBLISHER_HEADER, publisherHdr);
            responseDocument = HelperUtil.getAPIResponse(urlString, headers);
        } catch (InvalidHttpResponseException ihe) {
            throw new CitysearchException(this.getClass().getName(), "getOffers", ihe);
        }
        List<Offer> offersList = parseXML(responseDocument);
        if (offersList == null) {
            log.info("OffersHelper.getOffers:: Null offers instance ");
            throw new CitysearchException(this.getClass().getName(), "getOffers", "No offer found.");
        }
        // call for Profile API to get review count, profile url and phone#
        ProfileHelper profHelper = new ProfileHelper(this.rootPath);
        ProfileRequest profileRequest = new ProfileRequest();
        profileRequest.setPublisher(request.getPublisher());
        profileRequest.setClientIP(request.getClientIP());
        for (Offer offer : offersList) {
            profileRequest.setListingId(offer.getListingId());
            Profile profile = profHelper.getProfile(profileRequest);
            if (profile != null) {
                offer.setReviewCount(HelperUtil.toInteger(profile.getReviewCount()));
                offer.setProfileUrl(profile.getProfileUrl());
                offer.setPhone(profile.getPhone());
            } else {
                offer.setReviewCount(0);
                offer.setProfileUrl("#");
                offer.setPhone("");
            }
        }
        log.info("=========End offersHelper getOffers()============================ >");
        return offersList;
    }

    /**
     * Parses the offers xml. Returns List of offer objects
     * 
     * @param doc
     * @return List of Offer objects
     * @throws CitysearchException
     */
    private List<Offer> parseXML(Document doc) throws CitysearchException {
        log.info("========================== Start OffersHelper parseXML=======================");
        List<Element> offersElementList = null;
        List<Offer> offersList = null;
        if (doc != null && doc.hasRootElement()) {
            Element rootElement = doc.getRootElement();
            offersElementList = rootElement.getChildren(OFFER);
            offersList = getOffersList(offersElementList);
        }
        log.info("========================== End OffersHelper parseXML=======================");
        return offersList;
    }

    /**
     * Parses the offers element list and create list of Offer objects
     * 
     * @param List
     *            of Offer Elements
     * @return List of Offer beans
     * @throws CitysearchException
     */
    private List<Offer> getOffersList(List<Element> offerElemList) throws CitysearchException {
        log.info("========================== Start OffersHelper getOffersList=======================");
        Offer offer = null;
        List<Offer> offersLst = new ArrayList<Offer>();
        if (offerElemList.size() != 0) {
            for (int c = 0; c < 2; c++) {
                Element offerElement = (Element) offerElemList.get(c);

                offer = new Offer();
                offer.setCity(offerElement.getChildText(CITY));
                offer.setAttributionSrc(offerElement.getChildText(ATTRIBUTION_SOURCE));

                String ratingVal = offerElement.getChildText(CS_RATING);
                double rating = NumberUtils.toDouble(ratingVal) / 2;
                List<Integer> ratingList = HelperUtil.getRatingsList(ratingVal);
                offer.setCsRating(ratingList);
                offer.setReviewCount(HelperUtil.toInteger(offerElement.getChildText(REVIEW_COUNT)));
                offer.setImgUrl(offerElement.getChildText(IMAGE_URL));
                offer.setLatitude(offerElement.getChildText(LATITUDE));
                offer.setListingId(offerElement.getChildText(LISTING_ID));
                offer.setListingName(offerElement.getChildText(LISTING_NAME));
                offer.setLatitude(offerElement.getChildText(LONGITUDE));
                offer.setOfferDesc(offerElement.getChildText(OFFER_DESCRIPTION));
                offer.setOfferId(offerElement.getChildText(OFFER_ID));
                offer.setOfferTtl(offerElement.getChildText(OFFER_TITLE));
                offer.setRefId(offerElement.getChildText(REFERENCE_ID));
                offer.setState(offerElement.getChildText(STATE));
                offer.setStreet(offerElement.getChildText(STREET));
                offer.setZip(offerElement.getChildText(ZIP));
                offersLst.add(offer);
            }
        }
        log.info("========================== End OffersHelper getOffersList=======================");
        return offersLst;
    }
}
