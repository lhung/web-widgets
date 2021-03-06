package com.citysearch.webwidget.helper;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

import com.citysearch.webwidget.bean.Offer;
import com.citysearch.webwidget.bean.OffersRequest;
import com.citysearch.webwidget.bean.Profile;
import com.citysearch.webwidget.bean.ProfileRequest;
import com.citysearch.webwidget.bean.SearchRequest;
import com.citysearch.webwidget.exception.CitysearchException;
import com.citysearch.webwidget.exception.InvalidHttpResponseException;
import com.citysearch.webwidget.exception.InvalidRequestParametersException;
import com.citysearch.webwidget.util.APIFieldNameConstants;
import com.citysearch.webwidget.util.CommonConstants;
import com.citysearch.webwidget.util.HelperUtil;
import com.citysearch.webwidget.util.PropertiesLoader;

public class OffersHelper {
    private final static String PROPERTY_OFFERS_URL = "offers.url";
    private final static String PROPERTY_CITYSEARCH_COUPON_URL = "citysearch.coupon.url";
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
    private static final String PUBLISHER_HEADER = "X-Publisher";

    private Integer displaySize;

    public OffersHelper(String rootPath, Integer displaySize) {
        this.rootPath = rootPath;
        this.displaySize = displaySize;
    }

    /**
     * Constructs the Offers API query string with all the supplied parameters
     *
     * @return String
     * @throws CitysearchException
     */
    private String getQueryString(OffersRequest request) throws CitysearchException {
        log.info("Start offersHelper getQueryString()");
        StringBuilder strBuilder = new StringBuilder();
        /*
         * strBuilder.append(HelperUtil.constructQueryParam(APIFieldNameConstants .PUBLISHER,
         * request.getPublisher().trim())); strBuilder.append(CommonConstants.SYMBOL_AMPERSAND);
         */
        strBuilder.append(HelperUtil.constructQueryParam(APIFieldNameConstants.RPP, RPP_OFFERS));
        strBuilder.append(CommonConstants.SYMBOL_AMPERSAND);

        strBuilder.append(HelperUtil.constructQueryParam(APIFieldNameConstants.CLIENT_IP,
                request.getClientIP()));
        strBuilder.append(CommonConstants.SYMBOL_AMPERSAND);

        if (!StringUtils.isBlank(request.getWhat())) {
            // Offers API throws internal error when reading sushi+restaurant
            String what = request.getWhat().trim();
            try {
                what = URLEncoder.encode(what, "UTF-8");
            } catch (UnsupportedEncodingException excep) {
                throw new CitysearchException("OffersHelper", "getQueryString", excep);
            }
            strBuilder.append(HelperUtil.constructQueryParam(APIFieldNameConstants.WHAT, what));
        }

        if (!StringUtils.isBlank(request.getLatitude())
                && !StringUtils.isBlank(request.getLongitude())) {
            strBuilder.append(CommonConstants.SYMBOL_AMPERSAND);
            strBuilder.append(HelperUtil.constructQueryParam(APIFieldNameConstants.LATITUDE,
                    request.getLatitude().trim()));

            strBuilder.append(CommonConstants.SYMBOL_AMPERSAND);
            strBuilder.append(HelperUtil.constructQueryParam(APIFieldNameConstants.LONGITUDE,
                    request.getLongitude().trim()));
        } else {
            strBuilder.append(CommonConstants.SYMBOL_AMPERSAND);
            strBuilder.append(HelperUtil.constructQueryParam(APIFieldNameConstants.WHERE,
                    request.getWhere().trim()));
        }
        if (!StringUtils.isBlank(request.getTag())) {
            strBuilder.append(CommonConstants.SYMBOL_AMPERSAND);
            strBuilder.append(HelperUtil.constructQueryParam(APIFieldNameConstants.TAG,
                    request.getTag().trim()));
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
            String radius = HelperUtil.parseRadius(request.getRadius());
            request.setRadius(radius);
            strBuilder.append(HelperUtil.constructQueryParam(APIFieldNameConstants.RADIUS,
                    request.getRadius()));
        }
        if (!StringUtils.isBlank(request.getCallBackFunction())) {
            strBuilder.append(CommonConstants.SYMBOL_AMPERSAND);
            strBuilder.append(HelperUtil.constructQueryParam(APIFieldNameConstants.CALLBACK,
                    request.getCallBackFunction().trim()));
        }
        log.info("Start offersHelper getQueryString() querystring is " + strBuilder);
        return strBuilder.toString();
    }

    private void loadLatitudeAndLongitudeFromSearchAPI(OffersRequest request)
            throws CitysearchException {
        SearchRequest sRequest = new SearchRequest(request);
        sRequest.setWhat(request.getWhat());
        sRequest.setTags(request.getTag());
        sRequest.setWhere(request.getWhere());
        sRequest.setPublisher(request.getPublisher());

        SearchHelper sHelper = new SearchHelper(this.rootPath, this.displaySize);
        String[] latLon = sHelper.getLatitudeLongitude(sRequest);
        if (latLon.length >= 2) {
            request.setLatitude(latLon[0]);
            request.setLongitude(latLon[1]);
        }
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
        log.info("Start offersHelper getOffers()");

        request.validate();

        // Always return offers from customer who have budget
        // TODO: cleanup!!
        request.setCustomerHasbudget("true");// ????

        Properties properties = PropertiesLoader.getAPIProperties();
        StringBuilder urlString = new StringBuilder(properties.getProperty(PROPERTY_OFFERS_URL));
        urlString.append(getQueryString(request));
        Document responseDocument = null;
        try {
            String publisherHdr = request.getPublisher().trim();
            HashMap<String, String> hdrMap = new HashMap<String, String>();
            hdrMap.put(PUBLISHER_HEADER, publisherHdr);
            responseDocument = HelperUtil.getAPIResponse(urlString.toString(), hdrMap);
        } catch (InvalidHttpResponseException ihe) {
            // throw new CitysearchException(this.getClass().getName(),
            // "getOffers", ihe);
            // Return null and let it go to backfill
            log.error(ihe.getMessage());
            return null;
        }

        List<Offer> offersList = parseXML(request, responseDocument);
        if (offersList != null && !offersList.isEmpty()) {
            for (Offer offer : offersList) {
                ProfileRequest profileRequest = new ProfileRequest(request);
                profileRequest.setClientIP(request.getClientIP());
                profileRequest.setListingId(offer.getListingId());

                ProfileHelper profHelper = new ProfileHelper(this.rootPath);
                Profile profile = profHelper.getProfile(profileRequest);
                if (profile != null) {
                    offer.setReviewCount(HelperUtil.toInteger(profile.getReviewCount()));
                    offer.setProfileUrl(profile.getProfileUrl());
                    offer.setPhone(HelperUtil.parsePhone(profile.getPhone()));

                    offer.setCallBackFunction(request.getCallBackFunction());
                    offer.setCallBackUrl(request.getCallBackUrl());

                    String profileTrackingUrl = HelperUtil.getTrackingUrl(profile.getProfileUrl(),
                            null, request.getCallBackUrl(), request.getDartClickTrackUrl(),
                            offer.getListingId(), profile.getPhone(), request.getPublisher(),
                            request.getAdUnitName(), request.getAdUnitSize());
                    offer.setProfileTrackingUrl(profileTrackingUrl);

                    String callBackFn = HelperUtil.getCallBackFunctionString(
                            request.getCallBackFunction(), offer.getListingId(), profile.getPhone());
                    offer.setCallBackFunction(callBackFn);

                    StringBuilder couponUrl = new StringBuilder(properties.getProperty(PROPERTY_CITYSEARCH_COUPON_URL));
                    couponUrl.append(HelperUtil.constructQueryParam(CommonConstants.LISTING_ID, offer.getListingId()));
                    couponUrl.append(CommonConstants.SYMBOL_AMPERSAND);
                    couponUrl.append(HelperUtil.constructQueryParam("offerId", offer.getOfferId()));

                    String couponTrackingUrl = HelperUtil.getTrackingUrl(couponUrl.toString(),
                            null, null, request.getDartClickTrackUrl(), offer.getListingId(),
                            profile.getPhone(), request.getPublisher(), request.getAdUnitName(),
                            request.getAdUnitSize());
                    offer.setCouponUrl(couponTrackingUrl);
                }
            }
        }

        if (offersList != null && !offersList.isEmpty()
                && offersList.size() < request.getDisplaySize()) {
            ProfileRequest profileRequest = new ProfileRequest(request);
            profileRequest.setClientIP(request.getClientIP());
            ProfileHelper phelper = new ProfileHelper(this.rootPath);
            for (Offer offer : offersList) {
                profileRequest.setListingId(offer.getListingId());
                Profile profile = phelper.getProfileAndHighestReview(profileRequest);
                offer.setProfile(profile);
            }
        }

        log.info("End offersHelper getOffers()");
        return offersList;
    }

    /**
     * Parses the offers xml. Returns List of offer objects
     *
     * @param doc
     * @return List of Offer objects
     * @throws CitysearchException
     */
    private List<Offer> parseXML(OffersRequest request, Document doc) throws CitysearchException {
        log.info("Start OffersHelper parseXML");
        List<Offer> offersList = null;
        if (doc != null && doc.hasRootElement()) {
            Element rootElement = doc.getRootElement();
            List<Element> offersElementList = rootElement.getChildren(OFFER);
            if (StringUtils.isBlank(request.getLatitude())
                    || StringUtils.isBlank(request.getLongitude())) {
                offersList = (List<Offer>) getOffersByRating(request, offersElementList);
            } else {
                offersList = (List<Offer>) getOffersByDistance(request, offersElementList);
            }
            addDefaultImages(offersList, this.rootPath);
        }
        log.info("End OffersHelper parseXML");
        return offersList;
    }

    private List<Offer> getOffersByDistance(OffersRequest request, List<Element> offerElemList)
            throws CitysearchException {
        log.info("Start OffersHelper getOffersByDistance");
        List<Offer> offersLst = new ArrayList<Offer>();
        if (!offerElemList.isEmpty()) {
            BigDecimal sourceLatitude = new BigDecimal(request.getLatitude());
            BigDecimal sourceLongitude = new BigDecimal(request.getLongitude());
            SortedMap<Double, List<Element>> elmsSortedByDistance = new TreeMap<Double, List<Element>>();
            for (Element elm : offerElemList) {
                BigDecimal businessLatitude = new BigDecimal(
                        elm.getChildText(CommonConstants.LATITUDE));
                BigDecimal businessLongitude = new BigDecimal(
                        elm.getChildText(CommonConstants.LONGITUDE));
                double distance = HelperUtil.getDistance(sourceLatitude, sourceLongitude,
                        businessLatitude, businessLongitude);
                if (elmsSortedByDistance.containsKey(distance)) {
                    elmsSortedByDistance.get(distance).add(elm);
                } else {
                    List<Element> elms = new ArrayList<Element>();
                    elms.add(elm);
                    elmsSortedByDistance.put(distance, elms);
                }
            }
            offersLst = getOffersList(request, elmsSortedByDistance);
        }
        log.info("End OffersHelper getOffersByDistance");
        return offersLst;
    }

    private List<Offer> getOffersByRating(OffersRequest request, List<Element> offerElemList)
            throws CitysearchException {
        log.info("Start OffersHelper getOffersByRating");
        List<Offer> offersLst = new ArrayList<Offer>();
        if (!offerElemList.isEmpty()) {
            SortedMap<Double, List<Element>> elmsSortedByRating = new TreeMap<Double, List<Element>>();
            for (Element elm : offerElemList) {
                String ratingVal = elm.getChildText(CS_RATING);
                if (!StringUtils.isBlank(ratingVal) && NumberUtils.isNumber(ratingVal)) {
                    double rating = NumberUtils.toDouble(ratingVal);
                    if (elmsSortedByRating.containsKey(rating)) {
                        elmsSortedByRating.get(rating).add(elm);
                    } else {
                        List<Element> elms = new ArrayList<Element>();
                        elms.add(elm);
                        elmsSortedByRating.put(rating, elms);
                    }
                }
            }
            offersLst = getOffersList(request, elmsSortedByRating);
        }
        log.info("End OffersHelper getOffersByRating");
        return offersLst;
    }

    private List<Offer> getOffersList(OffersRequest request,
            SortedMap<Double, List<Element>> sortedElms) throws CitysearchException {
        List<Offer> offersLst = new ArrayList<Offer>();
        if (!sortedElms.isEmpty()) {
            for (int j = 0; j < sortedElms.size(); j++) {
                if (offersLst.size() >= this.displaySize) {
                    break;
                }
                Double key = sortedElms.firstKey();
                List<Element> elms = sortedElms.remove(key);
                for (int idx = 0; idx < elms.size(); idx++) {
                    if (offersLst.size() == this.displaySize) {
                        break;
                    }
                    Offer offer = toOffer(request, elms.get(idx));
                    offersLst.add(offer);
                }
            }
        }
        return offersLst;
    }

    private Offer toOffer(OffersRequest request, Element offerElement) throws CitysearchException {
        Offer offer = new Offer();

        if (!StringUtils.isBlank(request.getLatitude())
                && !StringUtils.isBlank(request.getLongitude())) {
            BigDecimal sourceLatitude = new BigDecimal(request.getLatitude());
            BigDecimal sourceLongitude = new BigDecimal(request.getLongitude());
            BigDecimal businessLatitude = new BigDecimal(
                    offerElement.getChildText(CommonConstants.LATITUDE));
            BigDecimal businessLongitude = new BigDecimal(
                    offerElement.getChildText(CommonConstants.LONGITUDE));
            double distance = HelperUtil.getDistance(sourceLatitude, sourceLongitude,
                    businessLatitude, businessLongitude);
            offer.setDistance(String.valueOf(distance));
        } else {
            offer.setDistance(null);
        }
        offer.setCity(offerElement.getChildText(CITY));
        offer.setState(offerElement.getChildText(STATE));
        String location = HelperUtil.getLocationString(offer.getCity(), offer.getState());
        offer.setLocation(location);
        offer.setAttributionSrc(offerElement.getChildText(ATTRIBUTION_SOURCE));
        String ratingVal = offerElement.getChildText(CS_RATING);
        List<Integer> ratingList = HelperUtil.getRatingsList(ratingVal);
        offer.setListingRating(ratingList);
        offer.setReviewCount(HelperUtil.toInteger(offerElement.getChildText(REVIEW_COUNT)));
        offer.setImageUrl(offerElement.getChildText(IMAGE_URL));
        offer.setLatitude(offerElement.getChildText(LATITUDE));
        offer.setListingId(offerElement.getChildText(LISTING_ID));
        offer.setLongitude(offerElement.getChildText(LONGITUDE));
        offer.setOfferId(offerElement.getChildText(OFFER_ID));

        String adUnitIdentifier = request.getAdUnitIdentifier();

        String offerTitle = offerElement.getChildText(OFFER_TITLE);
        StringBuilder titleLengthProp = new StringBuilder(adUnitIdentifier);
        titleLengthProp.append(".");
        titleLengthProp.append(CommonConstants.TITLE_LENGTH);
        offerTitle = HelperUtil.getAbbreviatedString(offerTitle, titleLengthProp.toString());
        offer.setOfferTitle(offerTitle);
        offer.setOfferShortTitle(offerTitle);

        String offerdesc = offerElement.getChildText(OFFER_DESCRIPTION);
        StringBuilder descLengthProp = new StringBuilder(adUnitIdentifier);
        descLengthProp.append(".");
        descLengthProp.append(CommonConstants.DESCRIPTION_LENGTH);
        offerdesc = HelperUtil.getAbbreviatedString(offerdesc, descLengthProp.toString());
        offer.setOfferDescription(offerdesc);

        String name = offerElement.getChildText(LISTING_NAME);
        StringBuilder nameLengthProp = new StringBuilder(adUnitIdentifier);
        nameLengthProp.append(".");
        nameLengthProp.append(CommonConstants.NAME_LENGTH);
        name = HelperUtil.getAbbreviatedString(name, nameLengthProp.toString());
        offer.setListingName(name);

        offer.setReferenceId(offerElement.getChildText(REFERENCE_ID));
        offer.setStreet(offerElement.getChildText(STREET));
        offer.setZip(offerElement.getChildText(ZIP));
        return offer;
    }

    private List<Offer> addDefaultImages(List<Offer> offers, String path)
            throws CitysearchException {
        if (offers != null && !offers.isEmpty()) {
            List<String> imageList = HelperUtil.getImages(path);
            if (imageList != null && !imageList.isEmpty()) {
                ArrayList<Integer> indexList = new ArrayList<Integer>(3);
                Random randomizer = new Random();
                for (int i = 0; i < offers.size(); i++) {
                    Offer offer = offers.get(i);
                    String imageUrl = offer.getImageUrl();
                    if (StringUtils.isBlank(imageUrl)) {
                        int index = 0;
                        do {
                            index = randomizer.nextInt(imageList.size());
                        } while (indexList.contains(index));
                        indexList.add(index);
                        imageUrl = imageList.get(index);
                        offer.setImageUrl(imageUrl);
                    }
                    offers.set(i, offer);
                }
            }
        }
        return offers;
    }
}
