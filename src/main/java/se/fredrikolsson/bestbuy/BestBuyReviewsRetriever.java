package se.fredrikolsson.bestbuy;

import com.google.common.collect.Lists;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class for retrieving product reviews from BestBuy and write them to a CSV file.
 */
public class BestBuyReviewsRetriever {

    private static Logger logger = LoggerFactory.getLogger(BestBuyReviewsRetriever.class);
    private final String apiKey;

    public static void main(String[] args) throws Exception {

        if (args.length < 3) {
            System.out.println("Usage: <yourBestBuyApiKey> <outputDirectory> <skuCode_1> ... <skuCode_N");
            System.exit(1);
        }

        String apiKey = args[0];
        String outputCsvDirectory = args[1];
        List<String> skuCodes = new ArrayList<String>(Arrays.asList(Arrays.copyOfRange(args, 2, args.length)));

        BestBuyReviewsRetriever populator = new BestBuyReviewsRetriever(apiKey);
        File outputFile = populator.createOutputFile(skuCodes, outputCsvDirectory);

        logger.info("Retrieving reviews for {} sku {} and write CSV output to {}",
                skuCodes.size(),
                skuCodes.size() == 1 ? "code" : "codes",
                outputFile.toString());

        List<JSONObject> reviews = populator.retrieveReviews(skuCodes);
        logger.info("Writing {} reviews to CSV file {}", reviews.size(), outputFile);
        populator.createReviewCsv(reviews, outputFile);
        logger.info("Done.");
    }


    private BestBuyReviewsRetriever(String apiKey) {
        this.apiKey = apiKey;
    }


    private List<JSONObject> retrieveReviews(List<String> skuCodes) throws Exception {
        BestBuyClient client = new BestBuyClient(getApiKey());
        List<JSONObject> result = new ArrayList<JSONObject>();
        for (String skuCode : skuCodes) {
            List<JSONObject> reviews = client.getReviews(skuCode);
            logger.info("Retrieved {} reviews for sku code {}", reviews.size(), skuCode);
            result.addAll(reviews);
        }
        return result;
    }

    private File createOutputFile(List<String> skuCodes, String outputDirectory) throws IOException {
        if (!new File(outputDirectory).canWrite()) {
            throw new IOException("Cannot write to directory: " + outputDirectory + ". Aborting!");
        }
        StringBuilder fileName = new StringBuilder("sku-");
        for (String skuCode : skuCodes) {
            fileName.append(skuCode).append("-");
        }
        fileName.append("reviews.csv");
        return new File(outputDirectory + File.separator + fileName.toString());
    }

    private void createReviewCsv(List<JSONObject> reviews, File outputFile) throws Exception {
        CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(outputFile), CSVFormat.DEFAULT);
        csvPrinter.printRecord(
                Lists.newArrayList(
                        "sku",
                        "reviewId",
                        "submissionTime",
                        "reviewerName",
                        "rating",
                        "review"));

        for (JSONObject review : reviews) {
            ReviewWrapper w = new ReviewWrapper(review);
            csvPrinter.printRecord(
                    Lists.newArrayList(
                            w.getSku(),
                            w.getId(),
                            w.getSubmissionTime(),
                            w.getReviewerName(),
                            w.getRating(),
                            combineTitleAndComment(w)
                    ));
        }
        csvPrinter.close();
    }


    private String combineTitleAndComment(ReviewWrapper wrapper) throws JSONException {
        String title = wrapper.getTitle();
        if (!title.endsWith(".") || !title.endsWith("?") || !title.endsWith("!")) {
            title += ".";
        }
        return (title + " " + wrapper.getComment()).replaceAll("\\n", " ");
    }

    private String getApiKey() {
        return apiKey;
    }

}
