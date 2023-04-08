package de.dogneeds.backend.controller;

import de.dogneeds.backend.service.UserService;
import de.dogneeds.backend.user.user.User;
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
@RequestMapping("/account")
public class UserController {

    @Autowired
    UserService userService;

    /**
     * handles login-requests
     *
     * @param loginData in json format with attributes email & password
     * @return success/error with the session-id/error-code
     */
    @CrossOrigin
    @RequestMapping("/login")
    public String login(@RequestBody String loginData) {
        JSONObject jsonObject = new JSONObject(loginData);

        if (!jsonObject.has("email") || !jsonObject.has("password")) {
            System.out.println("Could not handle login: " + loginData);
            return "error;unknown";
        }

        return userService.login(jsonObject.getString("email"), jsonObject.getString("password"));
    }

    /**
     * handles register-requests
     *
     * @param registerInfo in json format containing all details of the form
     * @return status of request
     */
    @CrossOrigin
    @RequestMapping("/register")
    public String register(@RequestBody String registerInfo) {
        return userService.register(new JSONObject(registerInfo));
    }

    /**
     * handles logout-requests
     *
     * @param session session-id
     */
    @CrossOrigin
    @RequestMapping("/logout")
    public void logout(@RequestBody String session) {
        userService.logout(UUID.fromString(session));
    }

    /**
     * handles authentication-requests (if a session is still valid)
     *
     * @param session session-id
     * @return "true" if valid, "false" if not
     */
    @CrossOrigin
    @RequestMapping("/auth")
    public String auth(@RequestBody String session) {
        return userService.auth(UUID.fromString(session));
    }

    @CrossOrigin
    @RequestMapping("/admingroup")
    public String isAdmin(@RequestBody String session) {
        return String.valueOf(userService.isAdmin(UUID.fromString(session)));
    }

    /**
     * request data of the user
     *
     * @param requestString what data is requested, example: {session};firstName;lastName;...
     * @return the data
     */
    @CrossOrigin
    @RequestMapping("/request")
    public String request(@RequestBody String requestString) {
        String[] requestUnfiltered = requestString.split(";");

        if (requestUnfiltered[0].isEmpty()) {
            return "error;invalidsession";
        }
        UUID session = UUID.fromString(requestUnfiltered[0]);

        // remove session from request
        String[] requestFiltered = new String[requestUnfiltered.length - 1];
        System.arraycopy(requestUnfiltered, 1, requestFiltered, 0, requestUnfiltered.length - 1);

        return userService.request(session, requestFiltered);
    }

    /**
     * gets all existing users
     *
     * @return list of users in json format
     */
    @CrossOrigin
    @RequestMapping("/getAll")
    public String getUsers() {
        return new JSONArray(userService.getUsers()).toString();
    }

    @CrossOrigin
    @RequestMapping("/get")
    public String getUser(@RequestBody String details) {
        JSONObject jsonObject = new JSONObject(details);

        // the needed arguments are not given
        if (!jsonObject.has("session") || !jsonObject.has("userId")) {
            return "error;wrongargs";
        }
        if (jsonObject.getString("session").isEmpty()) {
            return "error;invalidsession";
        }
        long userId;
        try {
            userId = Long.parseLong(jsonObject.getString("userId"));
        } catch (NumberFormatException e) {
            return "error;invaliduserid";
        }

        User user = userService.getUser(UUID.fromString(jsonObject.getString("session")), userId);

        if (user == null) {
            return "error;nopermission";
        }
        return new JSONObject(user).toString();
    }

    /**
     * handles requests to edit the details of a user
     *
     * @param details session and the changed attributes
     * @return success/error depending on if the action was successful
     */
    @CrossOrigin
    @RequestMapping("/edit")
    public String editUser(@RequestBody String details) {
        JSONObject jsonObject = new JSONObject(details);

        // the needed arguments are not given
        if (!jsonObject.has("session") || !jsonObject.has("userId")) {
            return "error;wrongargs";
        }

        return userService.editUser(jsonObject);
    }

    /**
     * handles requests to delete a user
     *
     * @param details session of the requester and the id of the user to delete
     * @return success/error depending on if the action was successful
     */
    @CrossOrigin
    @RequestMapping("/delete")
    public String deleteUser(@RequestBody String details) {
        JSONObject jsonObject = new JSONObject(details);

        // the needed arguments are not given
        if (!jsonObject.has("session") || !jsonObject.has("userId")) {
            return "error;wrongargs";
        }

        return userService.deleteUser(jsonObject);
    }

    /**
     * handles requests to add a viewed product
     *
     * @param details contains the session and productId in JSON format
     * @return success/error
     */
    @CrossOrigin
    @RequestMapping("/addLastViewedProduct")
    public String addLastViewedProduct(@RequestBody String details) {
        JSONObject jsonObject = new JSONObject(details);

        // the needed arguments are not given
        if (!jsonObject.has("session") || !jsonObject.has("productId")) {
            return "error;wrongargs";
        }

        return userService.addViewedItem(UUID.fromString(jsonObject.getString("session")), jsonObject.getLong("productId"));
    }

    /**
     * handles requests to get the last viewed products
     *
     * @param session session of the user
     * @return JSON-Array of products
     */
    @CrossOrigin
    @RequestMapping("/getLastViewedProducts")
    public String getLastViewedProducts(@RequestBody String session) {
        return new JSONArray(userService.getLastViewedProducts(UUID.fromString(session))).toString();
    }

    /**
     * handles requests to get the user groups
     *
     * @return JSON-Array of group-names
     */
    @CrossOrigin
    @RequestMapping("/getUserGroups")
    public String getUserGroups() {
        return new JSONArray(userService.getUserGroups()).toString();
    }
}

     

