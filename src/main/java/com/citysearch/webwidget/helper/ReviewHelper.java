package com.citysearch.webwidget.helper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

import com.citysearch.webwidget.bean.Profile;
import com.citysearch.webwidget.bean.ProfileRequest;
import com.citysearch.webwidget.bean.Review;
import com.citysearch.webwidget.bean.ReviewRequest;
import com.citysearch.webwidget.bean.SearchRequest;
import com.citysearch.webwidget.exception.CitysearchException;
import com.citysearch.webwidget.exception.InvalidHttpResponseException;
import com.citysearch.webwidget.exception.InvalidRequestParametersException;
import com.citysearch.webwidget.util.APIFieldNameConstants;
import com.citysearch.webwidget.util.CommonConstants;
import com.citysearch.webwidget.util.HelperUtil;
import com.citysearch.webwidget.util.PropertiesLoader;

/**
 * This Helper class performs all the functionality related to Reviews.
 * Validates the Review request, calls the API, aprses the response, then calls
 * the Profile API and returns the final response back
 * 
 * @author Aspert Benjamin
 * 
 */
public class ReviewHelper {

	public final static String PROPERTY_REVIEW_URL = "reviews.url";

	private static final int MINIMUM_RATING = 6;

	private static final String ELEMENT_REVIEW_URL = "review_url";
	private static final String BUSINESS_NAME = "business_name";
	private static final String LISTING_ID = "listing_id";
	private static final String REVIEW_ID = "review_id";
	private static final String REVIEW_TITLE = "review_title";
	private static final String REVIEW_TEXT = "review_text";
	private static final String PROS = "pros";
	private static final String CONS = "cons";
	private static final String REVIEW_RATING = "review_rating";
	private static final String REVIEW_DATE = "review_date";
	private static final String REVIEW_AUTHOR = "review_author";
	private static final String DATE_FORMAT = "reviewdate.format";
	private static final String REVIEW_ELEMENT = "review";

	private Logger log = Logger.getLogger(getClass());
	private String rootPath;

	public ReviewHelper(String rootPath) {
		this.rootPath = rootPath;
	}

	/**
	 * Constructs the Reviews API query string with all the supplied parameters
	 * 
	 * @return String
	 * @throws CitysearchException
	 */
	private String getQueryString(ReviewRequest request)
			throws CitysearchException {
		StringBuilder strBuilder = new StringBuilder(request.getQueryString());
		if (!StringUtils.isEmpty(request.getPublisher())) {
			strBuilder.append(CommonConstants.SYMBOL_AMPERSAND);
			strBuilder.append(HelperUtil.constructQueryParam(
					APIFieldNameConstants.PUBLISHER, request.getPublisher()));
		}
		if (request.isCustomerOnly()) {
			strBuilder.append(CommonConstants.SYMBOL_AMPERSAND);
			strBuilder.append(HelperUtil.constructQueryParam(
					APIFieldNameConstants.CUSTOMER_ONLY, String.valueOf(request
							.isCustomerOnly())));
		}
		if (!StringUtils.isEmpty(request.getRating())) {
			strBuilder.append(CommonConstants.SYMBOL_AMPERSAND);
			strBuilder.append(HelperUtil.constructQueryParam(
					APIFieldNameConstants.RATING, request.getRating()));
		}
		if (!StringUtils.isEmpty(request.getDays())) {
			strBuilder.append(CommonConstants.SYMBOL_AMPERSAND);
			strBuilder.append(HelperUtil.constructQueryParam(
					APIFieldNameConstants.DAYS, request.getDays()));
		}
		if (!StringUtils.isEmpty(request.getMax())) {
			strBuilder.append(CommonConstants.SYMBOL_AMPERSAND);
			strBuilder.append(HelperUtil.constructQueryParam(
					APIFieldNameConstants.MAX, request.getMax()));
		}
		if (!StringUtils.isEmpty(request.getPlacement())) {
			strBuilder.append(CommonConstants.SYMBOL_AMPERSAND);
			strBuilder.append(HelperUtil.constructQueryParam(
					APIFieldNameConstants.PLACEMENT, request.getPlacement()));
		}
		return strBuilder.toString();
	}

	/**
	 * Gets the review with the latest timestamp from Review API Then calls the
	 * profile API and gets the details not available from review API like
	 * Address,Phone,SendToFriendURL and ImageURL
	 * 
	 * @param request
	 * @return Review
	 * @throws InvalidRequestParametersException
	 * @throws CitysearchException
	 */
	public Review getLatestReview(ReviewRequest request)
			throws InvalidRequestParametersException, CitysearchException {
		log.info("ReviewHelper.getLatestReview:: before validate");
		request.validate();
		log.info("ReviewHelper.getLatestReview:: after validate");
		// If Lat and Lon is set find the nearest postal code using search API
		// Reviews API does not support lat & lon directly
		if (!StringUtils.isBlank(request.getLatitude())
				&& !StringUtils.isBlank(request.getLongitude())) {
			log
					.info("ReviewHelper.getLatestReview:: Lat and Lon received. Find zip");
			SearchRequest searchReq = new SearchRequest(request);
			SearchHelper shelper = new SearchHelper(this.rootPath, request
					.getDisplaySize());
			String where = shelper.getClosestLocationPostalCode(searchReq);
			request.setWhere(where);
			log.info("ReviewHelper.getLatestReview:: After finding zip");
		}

		Properties properties = PropertiesLoader.getAPIProperties();
		String urlString = properties.getProperty(PROPERTY_REVIEW_URL)
				+ getQueryString(request);
		log.info("ReviewHelper.getLatestReview:: Request URL " + urlString);
		Document responseDocument = null;
		try {
			responseDocument = HelperUtil.getAPIResponse(urlString, null);
			log
					.info("ReviewHelper.getLatestReview:: Successfull response received.");
		} catch (InvalidHttpResponseException ihe) {
			throw new CitysearchException(this.getClass().getName(),
					"getLatestReview", ihe);
		}
		Review reviewObj = parseXML(request, responseDocument);
		return reviewObj;
	}

	/**
	 * Parses the Reviews xml. Returns Review object with values from api
	 * 
	 * @param doc
	 * @return Review
	 * @throws CitysearchException
	 */
	private Review parseXML(ReviewRequest request, Document doc)
			throws CitysearchException {
		Review review = null;
		if (doc != null && doc.hasRootElement()) {
			Element rootElement = doc.getRootElement();
			List<Element> reviewsList = rootElement.getChildren(REVIEW_ELEMENT);
			SimpleDateFormat formatter = new SimpleDateFormat(PropertiesLoader
					.getAPIProperties().getProperty(DATE_FORMAT));
			SortedMap<Date, Element> reviewMap = new TreeMap<Date, Element>();
			for (int i = 0; i < reviewsList.size(); i++) {
				Element reviewElem = reviewsList.get(i);
				String rating = reviewElem.getChildText(REVIEW_RATING);
				if (NumberUtils.toInt(rating) >= MINIMUM_RATING) {
					String dateStr = reviewElem.getChildText(REVIEW_DATE);
					Date date = HelperUtil.parseDate(dateStr, formatter);
					if (date != null) {
						reviewMap.put(date, reviewElem);
					}
				}
			}
			Element reviewElm = reviewMap.get(reviewMap.lastKey());
			review = getReviewInstance(request, reviewElm, this.rootPath,
					request.getAdUnitIdentifier());
		}
		return review;
	}

	/**
	 * Parses the review element and set the required values in the Review bean
	 * 
	 * @param review
	 * @param reviewsElem
	 * @return Review
	 * @throws CitysearchException
	 */
	public static Review getReviewInstance(ReviewRequest request,
			Element reviewElem, String path, String adUnitIdentifier)
			throws CitysearchException {
		Review review = new Review();

		String businessName = reviewElem.getChildText(BUSINESS_NAME);
		review.setBusinessName(businessName);
		StringBuilder nameLengthProp = new StringBuilder(adUnitIdentifier);
		nameLengthProp.append(".");
		nameLengthProp.append(CommonConstants.NAME_LENGTH);
		businessName = HelperUtil.getAbbreviatedString(businessName,
				nameLengthProp.toString());
		review.setShortBusinessName(businessName);

		String reviewTitle = reviewElem.getChildText(REVIEW_TITLE);
		review.setReviewTitle(reviewTitle);
		StringBuilder titleLengthProp = new StringBuilder(adUnitIdentifier);
		titleLengthProp.append(".");
		titleLengthProp.append(CommonConstants.REVIEW_TITLE_LENGTH);
		reviewTitle = HelperUtil.getAbbreviatedString(reviewTitle,
				titleLengthProp.toString());
		review.setShortTitle(reviewTitle);

		String reviewText = reviewElem.getChildText(REVIEW_TEXT);
		review.setReviewText(reviewText);
		StringBuilder textLengthProp = new StringBuilder(adUnitIdentifier);
		textLengthProp.append(".");
		textLengthProp.append(CommonConstants.REVIEW_TEXT_LENGTH);
		reviewText = HelperUtil.getAbbreviatedString(reviewText, textLengthProp
				.toString());
		review.setShortReviewText(reviewText);

		textLengthProp = new StringBuilder(adUnitIdentifier);
		textLengthProp.append(".");
		textLengthProp.append(CommonConstants.REVIEW_TEXT_SMALL_LENGTH);
		reviewText = HelperUtil.getAbbreviatedString(reviewText, textLengthProp
				.toString());
		review.setSmallReviewText(reviewText);

		String pros = reviewElem.getChildText(PROS);
		review.setPros(pros);
		StringBuilder prosLengthProp = new StringBuilder(adUnitIdentifier);
		prosLengthProp.append(".");
		prosLengthProp.append(CommonConstants.REVIEW_PROS_LENGTH);
		pros = HelperUtil.getAbbreviatedString(pros, prosLengthProp.toString());
		review.setShortPros(pros);

		String cons = reviewElem.getChildText(CONS);
		review.setCons(cons);
		StringBuilder consLengthProp = new StringBuilder(adUnitIdentifier);
		consLengthProp.append(".");
		consLengthProp.append(CommonConstants.REVIEW_CONS_LENGTH);
		cons = HelperUtil.getAbbreviatedString(cons, consLengthProp.toString());
		review.setShortCons(cons);
		
		review.setListingId(reviewElem.getChildText(LISTING_ID));
		review.setReviewAuthor(reviewElem.getChildText(REVIEW_AUTHOR));
		String ratingVal = reviewElem.getChildText(REVIEW_RATING);
		double rating = NumberUtils.toDouble(ratingVal) / 2;
		review.setRating(HelperUtil.getRatingsList(ratingVal));
		review.setReviewRating(String.valueOf(rating));
		review.setReviewId(reviewElem.getChildText(REVIEW_ID));
		review.setReviewUrl(reviewElem.getChildText(ELEMENT_REVIEW_URL));

		String rDateStr = reviewElem.getChildText(REVIEW_DATE);
		SimpleDateFormat formatter = new SimpleDateFormat(PropertiesLoader
				.getAPIProperties().getProperty(DATE_FORMAT));
		Date date = HelperUtil.parseDate(rDateStr, formatter);
		long now = Calendar.getInstance().getTimeInMillis();
		review.setTimeSinceReviewString(DurationFormatUtils
				.formatDurationWords(now - date.getTime(), true, true));

		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		review.setReviewDate(df.format(date));

		// request will be null if called by profile
		if (request != null) {
			review.setCallBackFunction(request.getCallBackFunction());
			review.setCallBackUrl(request.getCallBackUrl());

			ProfileRequest profileRequest = new ProfileRequest(request);
			profileRequest.setClientIP(request.getClientIP());
			profileRequest.setListingId(review.getListingId());

			ProfileHelper profHelper = new ProfileHelper(path);
			Profile profile = profHelper.getProfile(profileRequest);

			review.setAddress(profile.getAddress());
			review.setPhone(profile.getPhone());
			review.setProfileUrl(profile.getProfileUrl());
			review.setSendToFriendUrl(profile.getSendToFriendUrl());
			review.setImageUrl(profile.getImageUrl());

			String sendToFriendTrackingUrl = HelperUtil.getTrackingUrl(profile
					.getSendToFriendUrl(), null, request.getCallBackUrl(),
					request.getDartClickTrackUrl(), review.getListingId(),
					profile.getPhone(), request.getPublisher(), request
							.getAdUnitName(), request.getAdUnitSize());
			review.setSendToFriendTrackingUrl(sendToFriendTrackingUrl);

			String profileTrackingUrl = HelperUtil.getTrackingUrl(profile
					.getProfileUrl(), null, request.getCallBackUrl(), request
					.getDartClickTrackUrl(), review.getListingId(), profile
					.getPhone(), request.getPublisher(), request
					.getAdUnitName(), request.getAdUnitSize());
			review.setProfileTrackingUrl(profileTrackingUrl);

			String adDisplayTrackingUrl = HelperUtil.getTrackingUrl(review
					.getReviewUrl(), null, request.getCallBackUrl(), request
					.getDartClickTrackUrl(), review.getListingId(), profile
					.getPhone(), request.getPublisher(), request
					.getAdUnitName(), request.getAdUnitSize());
			review.setReviewTrackingUrl(adDisplayTrackingUrl);

			String callBackFn = HelperUtil.getCallBackFunctionString(request
					.getCallBackFunction(), review.getListingId(), profile
					.getPhone());
			review.setCallBackFunction(callBackFn);
		}
		return review;
	}

}
