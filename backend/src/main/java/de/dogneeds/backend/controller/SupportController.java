package de.dogneeds.backend.controller;

import de.dogneeds.backend.service.SupportService;
import de.dogneeds.backend.support.Type;
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
@RequestMapping("/support")
public class SupportController {

    @Autowired
    private SupportService supportService;

    /**
     * handles requests to log a message
     *
     * @param message the message in json format
     */
    @CrossOrigin
    @RequestMapping("/log")
    public void logMessage(@RequestBody String message) {
        JSONObject jsonObject = new JSONObject(message);
        supportService.logMessage(jsonObject.getLong("roomId"), jsonObject.getLong("userId"), jsonObject.getString("text"), Type.valueOf(jsonObject.getString("type")));
    }

    /**
     * handles requests to check if a user has already a room in use
     *
     * @param session the session of the user
     * @return 0 if no room exists / is open; otherwise the roomId
     */
    @CrossOrigin
    @RequestMapping("/hasRoom")
    public Long hasRoom(@RequestBody String session) {
        return supportService.hasOpenRoom(UUID.fromString(session));
    }

    /**
     * handles requests to create a new room
     *
     * @param session the session of the user
     * @return the id of the new room
     */
    @CrossOrigin
    @RequestMapping("/createRoom")
    public Long createRoom(@RequestBody String session) {
        return supportService.createRoom(UUID.fromString(session));
    }

    /**
     * handles requests to get a list of unsupported rooms
     *
     * @param session the session who requests it
     * @return list of unsupported rooms in json array format
     */
    @CrossOrigin
    @RequestMapping("/getUnsupported")
    public String getUnsupportedRooms(@RequestBody String session) {
        return new JSONArray(supportService.getUnsupportedRooms(UUID.fromString(session))).toString();
    }

    /**
     * handles requests to get a list of a supporters rooms
     *
     * @param session the session of the supporter
     * @return list of his rooms in json array format
     */
    @CrossOrigin
    @RequestMapping("/getSupported")
    public String getSupportedRooms(@RequestBody String session) {
        return new JSONArray(supportService.getSupportedRooms(UUID.fromString(session))).toString();
    }

    /**
     * handles requests to claim a room
     *
     * @param details contains session and roomId
     * @return true if claimed successfully
     */
    @CrossOrigin
    @RequestMapping("/supportRoom")
    public boolean setSupporter(@RequestBody String details) {
        JSONObject jsonObject = new JSONObject(details);

        if (!jsonObject.has("session") || !jsonObject.has("roomId")) {
            return false;
        }

        return supportService.setSupporter(UUID.fromString(jsonObject.getString("session")), jsonObject.getLong("roomId"));
    }

    /**
     * handles requests to close a room
     *
     * @param details the session and the id of the room
     * @return true if successful
     */
    @CrossOrigin
    @RequestMapping("/closeRoom")
    public boolean closeRoom(@RequestBody String details) {
        JSONObject jsonObject = new JSONObject(details);

        if (!jsonObject.has("session") || !jsonObject.has("roomId")) {
            return false;
        }

        return supportService.closeRoom(UUID.fromString(jsonObject.getString("session")), jsonObject.getLong("roomId"));
    }

    /**
     * handles requests to free a room
     *
     * @param details the session and the id of the room
     * @return true if successful
     */
    @CrossOrigin
    @RequestMapping("/freeRoom")
    public boolean freeRoom(@RequestBody String details) {
        JSONObject jsonObject = new JSONObject(details);

        if (!jsonObject.has("session") || !jsonObject.has("roomId")) {
            return false;
        }

        return supportService.freeRoom(UUID.fromString(jsonObject.getString("session")), jsonObject.getLong("roomId"));
    }

    /**
     * handles requests to load previous messages of a room
     *
     * @param details the session and the id of the room
     * @return list of messages in json array format
     */
    @CrossOrigin
    @RequestMapping("/getMessages")
    public String getMessages(@RequestBody String details) {
        JSONObject jsonObject = new JSONObject(details);

        if (!jsonObject.has("session") || !jsonObject.has("roomId")) {
            return "error;wrongargs";
        }

        return new JSONArray(supportService.getMessages(UUID.fromString(jsonObject.getString("session")), jsonObject.getLong("roomId"))).toString();
    }
}