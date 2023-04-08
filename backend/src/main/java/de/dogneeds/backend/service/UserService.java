package de.dogneeds.backend.service;

import de.dogneeds.backend.events.DeleteEvent;
import de.dogneeds.backend.product.product.Product;
import de.dogneeds.backend.user.address.Address;
import de.dogneeds.backend.user.address.AddressRepository;
import de.dogneeds.backend.user.lastviewed.LastViewed;
import de.dogneeds.backend.user.lastviewed.LastViewedRepository;
import de.dogneeds.backend.user.user.User;
import de.dogneeds.backend.user.user.UserRepository;
import de.dogneeds.backend.user.usergroup.UserGroup;
import de.dogneeds.backend.user.usergroup.UserGroupRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
public class UserService implements ApplicationListener<DeleteEvent> {

    private final HashMap<UUID, User> sessionMap = new HashMap<>(); // stores all the current active sessions

    @Autowired
    private EmailService emailService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private LastViewedRepository lastViewedRepository;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * called automatically when the service is initialized
     */
    @PostConstruct
    public void init() {
        if (userGroupRepository.getUserGroupNames().size() == 0) {
            System.out.println("Create user groups");
            userGroupRepository.save(new UserGroup("Nutzer"));
            userGroupRepository.save(new UserGroup("Admin"));
        }
    }

    /**
     * creates a session if provided with valid email & password combination
     *
     * @param email    email
     * @param password password
     * @return success/error with the session-id/error-code
     */
    public String login(String email, String password) {
        // search for User matching this email & password
        User repoUser = userRepository.findByEmailAndPassword(email, password);

        if (repoUser == null) {
            System.out.println("No account found for " + email + " with password " + password);
            return "error;noaccountfound";
        }

        // random session-id
        UUID uuid = UUID.randomUUID();
        sessionMap.put(uuid, repoUser);
        System.out.println("Create session " + uuid + " for " + repoUser.getEmail());

        return "success;" + uuid;
    }

    /**
     * deletes a session
     *
     * @param session session-id
     */
    public void logout(UUID session) {
        if (sessionMap.containsKey(session)) {
            String email = sessionMap.remove(session).getEmail();
            System.out.println("Removed session for " + email);
        }
    }

    /**
     * creates a new user
     *
     * @param jsonObject register-data in json format
     * @return success/error with session-id/error-code
     */
    public String register(JSONObject jsonObject) {
        String email = String.valueOf(jsonObject.get("email"));

        // check if email already exists
        if (userRepository.isEmailRegistered(email) != null) {
            System.out.println("Failed to register " + email + ": Email is already in use");
            return "error;emailalreadyinuse";
        }

        try {
            // save address
            Address address = new Address(String.valueOf(jsonObject.get("plz")), String.valueOf(jsonObject.get("city")), String.valueOf(jsonObject.get("street")), String.valueOf(jsonObject.get("number")));
            Address savedAddress = addressRepository.save(address);

            // save user
            User user = new User(email, String.valueOf(jsonObject.get("password")), String.valueOf(jsonObject.get("firstName")), String.valueOf(jsonObject.get("lastName")), savedAddress, userGroupRepository.findUserGroupByName("Nutzer"));
            User savedUser = userRepository.save(user);

            System.out.println("Successfully registered " + jsonObject.get("firstName") + " " + jsonObject.get("lastName") + " with email " + email);

            // random session-id
            UUID uuid = UUID.randomUUID();
            sessionMap.put(uuid, savedUser);
            System.out.println("Create session " + uuid + " for " + savedUser.getEmail());

            new Thread(() -> emailService.sendEmail(user.getEmail(), "Willkommen bei DogNeeds", "Hallo " + user.getFirstName() + ",\nes freut uns sehr, dass du dich bei DogNeeds angemeldet hast.\nDu kannst jetzt direkt losshoppen und tolle Produkte für deinen Vierbeiner entdecken!")).start();
            return "success;" + uuid;
        } catch (Exception e) {
            System.out.println("Tried to register " + jsonObject);
            e.printStackTrace();
            return "error;" + e.getMessage();
        }
    }

    /**
     * checks if a session is still valid
     *
     * @param session session-id
     * @return "true" if valid, "false" if not
     */
    public String auth(UUID session) {
        System.out.println("Auth check for " + session + ": " + sessionMap.containsKey(session));
        return String.valueOf(sessionMap.containsKey(session));
    }

    /**
     * request data of the user
     *
     * @param session session-id
     * @param request string-array of the data that is requested
     * @return the data in the requested order, seperated by ';'
     */
    public String request(UUID session, String[] request) {
        StringBuilder reply = new StringBuilder();

        // only if the session is valid, we can access the data
        if (!sessionMap.containsKey(session)) {
            return "error;invalidsession";
        }

        User user = sessionMap.get(session);
        for (int i = 0; i < request.length; i++) {
            String s = request[i];

            // what is requested
            switch (s) {
                case "id" -> reply.append(user.getUid());
                case "email" -> reply.append(user.getEmail());
                case "password" -> reply.append(user.getPassword());
                case "firstName" -> reply.append(user.getFirstName());
                case "lastName" -> reply.append(user.getLastName());
                case "address:id" -> reply.append(user.getAid().getAid());
                case "address:plz" -> reply.append(user.getAid().getPlz());
                case "address:city" -> reply.append(user.getAid().getCity());
                case "address:street" -> reply.append(user.getAid().getStreet());
                case "address:number" -> reply.append(user.getAid().getNumber());
            }

            // only add a ';' if we're not at the end of the request
            if (i != request.length - 1) {
                reply.append(";");
            }
        }

        System.out.println("Requst from " + session + " reply: " + reply);
        return reply.toString();
    }

    /**
     * checks if a user is member of the group "Admin"
     *
     * @param session session-id for the user
     * @return "true" if the user is an "admin", "false" if not
     */
    public boolean isAdmin(UUID session) {
        if (!sessionMap.containsKey(session)) {
            return false;
        }
        System.out.println("Admin-Check for " + session + ": " + sessionMap.get(session).getUgid().getName().equalsIgnoreCase("Admin"));
        return sessionMap.get(session).getUgid().getName().equalsIgnoreCase("Admin");
    }

    /**
     * get a list of all users
     *
     * @return collection of users
     */
    public Collection<User> getUsers() {
        Collection<User> users = userRepository.getAllUsers();
        for (User user : users) {
            user.setPassword("");
        }
        return users;
    }

    /**
     * get the details of a user
     *
     * @param session the session of the requester
     * @param userId  the userId of the requested user
     * @return the user object or null if not allowed/found
     */
    public User getUser(UUID session, Long userId) {
        // permission check
        if (!sessionMap.containsKey(session)) {
            return null;
        }
        boolean allow = sessionMap.get(session).getUid().longValue() == userId;
        // user to get = user who requests
        // user who requests is an admin
        if (isAdmin(session)) {
            allow = true;
        }
        if (!allow) {
            return null;
        }

        return userRepository.getUserById(userId);
    }

    /**
     * edits a user
     *
     * @param jsonObject contains session of the requester & userId of the user to edit
     * @return success/error
     */
    public String editUser(JSONObject jsonObject) {
        UUID uuid = UUID.fromString(String.valueOf(jsonObject.getString("session")));
        if (!sessionMap.containsKey(uuid)) {
            return "error;invalidsession";
        }
        if (!permissionCheck(uuid, jsonObject.getLong("userId"))) {
            return "error;nopermission";
        }

        User user = userRepository.getUserById(jsonObject.getLong("userId"));
        Address address = user.getAid();
        List<String> changedAttributes = new ArrayList<>();
        String oldMail;
        if (jsonObject.has("email")) {
            if (userRepository.isEmailRegistered(jsonObject.getString("email")) != null) {
                return "error;emailnotavailable";
            }
            String change = "E-Mail-Adresse: " + user.getEmail();
            oldMail = user.getEmail();
            user.setEmail(jsonObject.getString("email"));
            change += " -> " + user.getEmail();
            changedAttributes.add(change);
        } else {
            oldMail = null;
        }
        if (jsonObject.has("password")) {
            user.setPassword(jsonObject.getString("password"));
            changedAttributes.add("Passwort");
        }
        if (jsonObject.has("firstName")) {
            String change = "Vorname: " + user.getFirstName();
            user.setFirstName(jsonObject.getString("firstName"));
            change += " -> " + user.getFirstName();
            changedAttributes.add(change);
        }
        if (jsonObject.has("lastName")) {
            String changes = "Nachname: " + user.getLastName();
            user.setLastName(jsonObject.getString("lastName"));
            changes += " -> " + user.getLastName();
            changedAttributes.add(changes);
        }
        if (jsonObject.has("addressPlz")) {
            String change = "Postleitzahl: " + address.getPlz();
            address.setPlz(jsonObject.getString("addressPlz"));
            change += " -> " + address.getPlz();
            changedAttributes.add(change);
        }
        if (jsonObject.has("addressCity")) {
            String change = "Stadt: " + address.getCity();
            address.setCity(jsonObject.getString("addressCity"));
            change += " -> " + address.getCity();
            changedAttributes.add(change);
        }
        if (jsonObject.has("addressStreet")) {
            String change = "Straße: " + address.getStreet();
            address.setStreet(jsonObject.getString("addressStreet"));
            change += " -> " + address.getStreet();
            changedAttributes.add(change);
        }
        if (jsonObject.has("addressNumber")) {
            String change = "Hausnummer: " + address.getNumber();
            address.setNumber(jsonObject.getString("addressNumber"));
            change += " -> " + address.getNumber();
            changedAttributes.add(change);
        }
        if (jsonObject.has("userGroup")) {
            String changes = "Nutzergruppe: " + user.getUgid().getName();
            user.setUgid(userGroupRepository.findUserGroupByName(jsonObject.getString("userGroup")));
            changes += " -> " + user.getUgid().getName();
            changedAttributes.add(changes);
        }

        // check if changes were made
        if (changedAttributes.size() > 0) {
            // saving the user
            userRepository.save(user);
            addressRepository.save(address);
            System.out.println("Edited user " + user.getEmail());

            // updating user session
            if (sessionMap.get(uuid).getUid().longValue() == user.getUid()) {
                sessionMap.put(uuid, user);
            }

            // preparing mail
            StringBuilder body = new StringBuilder("Hallo " + user.getFirstName() + ",\nuns ist aufgefallen, dass folgende Änderungen an deinem Account vorgenommen worden sind:\n");
            for (String s : changedAttributes) {
                body.append("- ").append(s).append("\n");
            }
            body.append("Falls du diese Änderungen nicht vorgenommen haben solltest, kontaktiere bitte umgehend unseren Support!");

            // sending mail in new thread
            new Thread(() -> emailService.sendEmail(user.getEmail(), "Änderungen an deinem DogNeeds-Account", body.toString())).start();

            // check if email has changed as well
            if (oldMail != null) {
                new Thread(() -> emailService.sendEmail(oldMail, "Änderungen an deinem DogNeeds-Account", body.toString())).start();
            }
            return "success";
        }

        return "nochanges";
    }

    /**
     * delete a user
     *
     * @param jsonObject contains session of the requester & userId of the user to delete
     * @return success/error
     */
    public String deleteUser(JSONObject jsonObject) {
        UUID uuid = UUID.fromString(String.valueOf(jsonObject.getString("session")));
        if (!sessionMap.containsKey(uuid)) {
            return "error;invalidsession";
        }
        if (!permissionCheck(uuid, jsonObject.getLong("userId"))) {
            return "error;nopermission";
        }

        User user = userRepository.getUserById(jsonObject.getLong("userId"));
        if (user == null) {
            return "error;notfound";
        }

        Iterator<UUID> iterator = sessionMap.keySet().iterator();
        while (iterator.hasNext()) {
            UUID session = iterator.next();
            if (sessionMap.get(session).getUid().longValue() == user.getUid()) {
                iterator.remove();
            }
        }

        String email = user.getEmail();

        applicationEventPublisher.publishEvent(new DeleteEvent(this, user));

        lastViewedRepository.deleteAllByUid(user); // delete last viewed items
        userRepository.deleteById(user.getUid()); // delete the user
        addressRepository.delete(user.getAid()); // delete the address
        new Thread(() -> emailService.sendEmail(email, "Löschung deines DogNeeds-Accounts", "Dein DogNeeds-Account wurde erfolgreich gelöscht!")).start();
        System.out.println("Deleted user " + email);
        return "success";
    }

    /**
     * checks if a session is allowed to access a user
     *
     * @param uuid   the uuid of the requester
     * @param userId the id of the user
     * @return true if allowed, false if not allowed
     */
    private boolean permissionCheck(UUID uuid, Long userId) {
        // user to edit = user who requests the edits
        if (sessionMap.get(uuid).getUid().longValue() == userId) {
            return true;
        }
        // user who requests the edits is an admin or doesn't have the permission to edit
        return isAdmin(uuid);
    }

    /**
     * add a viewed product
     *
     * @param session   session of the user
     * @param productId productId of the product
     * @return success/error
     */
    public String addViewedItem(UUID session, Long productId) {
        User user = sessionMap.get(session);
        Product product = productService.getProductRepository().getProductById(productId);

        if (user == null) {
            return "error;invalidsession";
        }
        if (product == null) {
            return "error;invalidproduct";
        }

        LastViewed lastViewed = new LastViewed(user, product);
        lastViewedRepository.save(lastViewed);

        return "success";
    }

    /**
     * get the last viewed products of a user
     *
     * @param session the session of the user
     * @return collection of productIds
     */
    public Collection<Long> getLastViewedProducts(UUID session) {
        ArrayList<Long> result = new ArrayList<>();
        User user = sessionMap.get(session);

        if (user == null) {
            return null;
        }

        Collection<LastViewed> lastViewedCollection = lastViewedRepository.getLastViewedByUid(user);
        for (LastViewed lw : lastViewedCollection) {
            if (!result.contains(lw.getPid().getPid()) && result.size() <= 10) {
                result.add(lw.getPid().getPid());
            }
        }

        return result;
    }

    public Collection<String> getUserGroups() {
        return userGroupRepository.getUserGroupNames();
    }

    public HashMap<UUID, User> getSessionMap() {
        return sessionMap;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    /**
     * listen for delete event
     *
     * @param event event
     */
    @Override
    public void onApplicationEvent(DeleteEvent event) {
        if (event.getObject() instanceof Product) {
            lastViewedRepository.deleteAllByPid((Product) event.getObject());
        }
    }
}
