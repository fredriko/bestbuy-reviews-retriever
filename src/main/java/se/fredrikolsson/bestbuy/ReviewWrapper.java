package se.fredrikolsson.bestbuy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 */
class ReviewWrapper {
    private static Logger logger = LoggerFactory.getLogger(ReviewWrapper.class);

    private JSONObject rawReviewJson;
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public ReviewWrapper(JSONObject rawReviewJson) {
        setRawReviewJson(rawReviewJson);
    }


    private JSONObject getRawReviewJson() {
        return rawReviewJson;
    }

    public String getComment() throws JSONException {
        String result = "";
        if (getRawReviewJson() != null) {
            result = getRawReviewJson().getString("comment");
        }
        return result;
    }

    public String getTitle() throws JSONException {
        String result = "";
        if (getRawReviewJson() != null) {
            result = getRawReviewJson().getString("title");
        }
        return result;
    }

    public int getId() throws JSONException {
        int result = -1;
        if (getRawReviewJson() != null) {
            result = getRawReviewJson().getInt("id");
        }
        return result;
    }

    public double getRating() throws JSONException {
        double result = -1;
        if (getRawReviewJson() != null) {
            result = getRawReviewJson().getDouble("rating");
        }
        return result;
    }

    public String getReviewerName() throws JSONException {
        String result = "";
        if (getRawReviewJson() != null) {

            Object o = getRawReviewJson().get("reviewer");
            if (o instanceof JSONObject) {
                result = getRawReviewJson().getJSONObject("reviewer").getString("name");
            } else if (o instanceof JSONArray) {
                result = getRawReviewJson().getJSONArray("reviewer").getJSONObject(0).getString("name");
            } else {
                logger.warn("Could not obtain reviewer.name from rawReviewJson: {}", toString());
            }
        }
        return result;
    }

    public long getSku() throws JSONException {
        long result = -1;
        if (getRawReviewJson() != null) {
            result = getRawReviewJson().getLong("sku");
        }
        return result;
    }

    public Date getSubmissionTime() throws ParseException, JSONException {
        Date result = null;
        if (getRawReviewJson() != null) {
            result = getDateFormat().parse(getRawReviewJson().getString("submissionTime"));
        }
        return result;
    }

    private SimpleDateFormat getDateFormat() {
        return df;
    }

    private void setRawReviewJson(JSONObject rawReviewJson) {
        this.rawReviewJson = rawReviewJson;
    }

    public String toString() {
        return getRawReviewJson().toString();
    }
}
