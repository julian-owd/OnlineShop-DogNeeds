package de.dogneeds.backend.controller;

import de.dogneeds.backend.service.FeedbackService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Controller
@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    /**
     * handles requests to check if a user is allowed to give feedback
     *
     * @param details contains session and productId
     * @return true if the user is allowed to give feedback
     */
    @CrossOrigin
    @RequestMapping("/check")
    public String checkAllowed(@RequestBody String details) {
        JSONObject jsonObject = new JSONObject(details);
        if (!jsonObject.has("session") || !jsonObject.has("productId")) {
            return "error;wrongargs";
        }

        boolean hasBought = feedbackService.hasBoughtProduct(UUID.fromString(jsonObject.getString("session")), jsonObject.getLong("productId"));
        boolean isAllowedToFeedback = feedbackService.isAllowedToGiveFeedback(UUID.fromString(jsonObject.getString("session")), jsonObject.getLong("productId"));

        return String.valueOf(hasBought && isAllowedToFeedback);
    }

    /**
     * handles requests to add a feedback
     *
     * @param details contains the session, productId, rating and description
     * @return true if the feedback was added, false if not
     */
    @CrossOrigin
    @RequestMapping("/add")
    public String addFeedback(@RequestBody String details) {
        JSONObject jsonObject = new JSONObject(details);
        if (!jsonObject.has("session") || !jsonObject.has("productId") || !jsonObject.has("userRating") || !jsonObject.has("userText")) {
            return "error;wrongargs";
        }
        if (!Boolean.parseBoolean(checkAllowed(details))) {
            return "error;notallowed";
        }

        return String.valueOf(feedbackService.addFeedback(UUID.fromString(jsonObject.getString("session")), jsonObject.getLong("productId"), jsonObject.getInt("userRating"), jsonObject.getString("userText")));
    }

    /**
     * handles requests to get the feedbacks of a product
     *
     * @param productId the id of the product
     * @return json array of feedbacks
     */
    @CrossOrigin
    @RequestMapping("/get")
    public String getFeedbackForProduct(@RequestBody String productId) {
        return new JSONArray(feedbackService.getFeedbacks(Long.parseLong(productId))).toString();
    }

    /**
     * handles requests to load all feedbacks
     * just the highest feedback per user
     *
     * @return json array of feedbacks
     */
    @CrossOrigin
    @RequestMapping("/getAll")
    public String getFeedbacks() {
        return new JSONArray(feedbackService.getAllFeedbacks()).toString();
    }

}
