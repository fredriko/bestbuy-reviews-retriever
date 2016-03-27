package se.fredrikolsson.bestbuy;

import com.google.common.collect.Lists;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
class BestBuyClient {

    private static Logger logger = LoggerFactory.getLogger(BestBuyClient.class);

    private final String apiKey;
    private static final String REVIEWS_API_ENDPOINT = "http://api.bestbuy.com/v1/reviews";
    private static final String PRODUCTS_API_ENDPOINT = "http://api.bestbuy.com/v1/products";


    BestBuyClient(String apiKey) {
        this.apiKey = apiKey;
    }

    List<JSONObject> getReviews(String skuCode) throws Exception {
        int currentPage = 1;
        JSONObject response = getReviews(skuCode, currentPage);

        List<JSONObject> intermediateResults = Lists.newArrayList(response);

        int totalPages = response.getInt("totalPages");
        while (currentPage < totalPages) {
            response = getReviews(skuCode, ++currentPage);
            intermediateResults.add(response);
        }
        List<JSONObject> result = new ArrayList<>();
        for (JSONObject intermediateResult : intermediateResults) {
            JSONArray reviews = intermediateResult.getJSONArray("reviews");
            for (int i = 0; i < reviews.length(); i++) {
                result.add(reviews.getJSONObject(i));
            }
        }
        return result;
    }


    private JSONObject getReviews(String skuCode, int currentPage) throws Exception {
        HttpResponse<JsonNode> response = Unirest.get(getReviewsApiEndpoint()
                + "(sku={sku})?pageSize=100&page={currentPage}&format=json&apiKey={apiKey}")
                .routeParam("sku", skuCode)
                .routeParam("currentPage", Integer.toString(currentPage))
                .routeParam("apiKey", getApiKey())
                .asJson();

        if (response.getStatus() != 200) {
            logger.error("Got HTTP status {}: response body: {}", response.getStatus(), response.getBody().toString());
            throw new Exception("Got HTTP status "
                    + response.getStatus() + ": " + response.getStatusText());
        }
        return response.getBody().getObject();
    }

    String getProductName(String skuCode) throws Exception {
        HttpResponse<JsonNode> response = Unirest.get(getProductsApiEndpoint()
                + "(sku={sku})?show=name&format=json&apiKey={apiKey}")
                .routeParam("sku", skuCode)
                .routeParam("apiKey", getApiKey())
                .asJson();

        if (response.getStatus() != 200) {
            logger.error("Got HTTP status {}: response body: {}", response.getStatus(), response.getBody().toString());
            throw new Exception("Got HTTP status "
                    + response.getStatus() + ": " + response.getStatusText());
        }
        JSONArray products = response.getBody().getArray();
        String result = "NO NAME FOUND";
        if (products != null && products.length() > 0) {
            result = products.getJSONObject(0).getJSONArray("products").getJSONObject(0).getString("name");
        }
        return result;
    }


    private String getApiKey() {
        return apiKey;
    }

    private static String getReviewsApiEndpoint() {
        return REVIEWS_API_ENDPOINT;
    }

    private static String getProductsApiEndpoint() {
        return PRODUCTS_API_ENDPOINT;
    }
}
