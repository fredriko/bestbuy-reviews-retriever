package se.fredrikolsson.bestbuy;

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
public class BestBuyClient {

    private static Logger logger = LoggerFactory.getLogger(BestBuyClient.class);

        private final String apiKey;
        // http://api.bestbuy.com/v1/reviews(sku=4447801)?pageSize=100&page=2&format=json&apiKey=p9na65xz3zb3jcqgvg4aqsjy
        private static final String REVIEWS_API_ENDPOINT = "http://api.bestbuy.com/v1/reviews";
        private static final String PRODUCTS_API_ENDPOINT = "http://api.bestbuy.com/v1/products";


        public static void main(String ... args) throws Exception {
            String apiKey = "p9na65xz3zb3jcqgvg4aqsjy";
            String skuCode = "4447801";

            BestBuyClient client = new BestBuyClient(apiKey);
            String name = client.getProductName(skuCode);
            System.out.println("Got name " + name);
            //List<JSONObject> reviews = client.getReviews(skuCode);
            //System.out.println("Got " + reviews.size() + " reviews for sku " + skuCode);
        }


        public BestBuyClient(String apiKey) {
            this.apiKey = apiKey;
        }

        public List<JSONObject> getReviews(String skuCode) throws Exception {
            int currentPage = 1;
            JSONObject response = getReviews(skuCode, currentPage);

            List<JSONObject> intermediateResults = new ArrayList<JSONObject>();
            intermediateResults.add(response);

            int totalPages = response.getInt("totalPages");
            while (currentPage < totalPages) {
                response = getReviews(skuCode, ++currentPage);
                intermediateResults.add(response);
            }
            List<JSONObject> result = new ArrayList<JSONObject>();
            for (JSONObject intermediateResult : intermediateResults) {
                JSONArray reviews = intermediateResult.getJSONArray("reviews");
                for (int i = 0; i < reviews.length(); i++) {
                    result.add(reviews.getJSONObject(i));
                }
            }
            return result;
        }



        protected JSONObject getReviews(String skuCode, int currentPage) throws Exception {
            logger.info("Getting reviews for sku: {}, page: {}", skuCode, currentPage);
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

        public String getProductName(String skuCode) throws Exception {
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
            logger.info("Got product name for sku {}: \"{}\"", skuCode, result);
            return result;
        }


        public String getApiKey() {
            return apiKey;
        }

        public static String getReviewsApiEndpoint() {
            return REVIEWS_API_ENDPOINT;
        }

        public static String getProductsApiEndpoint() {
            return PRODUCTS_API_ENDPOINT;
        }
}
