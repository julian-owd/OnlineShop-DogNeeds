package de.dogneeds.backend.service;

import de.dogneeds.backend.chat.message.Message;
import de.dogneeds.backend.chat.message.MessageRepository;
import de.dogneeds.backend.chat.room.Room;
import de.dogneeds.backend.chat.room.RoomRepository;
import de.dogneeds.backend.events.DeleteEvent;
import de.dogneeds.backend.support.Type;
import de.dogneeds.backend.user.user.User;
import de.dogneeds.backend.user.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.UUID;

@Service
public class SupportService implements ApplicationListener<DeleteEvent> {

    @Autowired
    private EmailService emailService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    /**
     * check if a user already has an opened room
     *
     * @param session the session of the user
     * @return the id of the room or 0 if none is open
     */
    public Long hasOpenRoom(UUID session) {
        User user = userService.getSessionMap().get(session);
        if (user == null) {
            return 0L;
        }

        Room room = roomRepository.hasOpenRoom(user);
        if (room == null) {
            return 0L;
        }
        return room.getRid();
    }

    /**
     * create a new room
     *
     * @param session the session of the user
     * @return the id of the room
     */
    public Long createRoom(UUID session) {
        if (hasOpenRoom(session) != 0) {
            return 0L;
        }

        User user = userService.getSessionMap().get(session);
        if (user == null) {
            return 0L;
        }

        System.out.println("Create room for " + user.getEmail());
        Room room = new Room(user);
        roomRepository.save(room);

        Collection<User> users = userRepository.getAllUsers();
        for (User admins : users) {
            if (admins.getUgid().getName().equals("Admin")) {
                new Thread(() -> emailService.sendEmail(admins.getEmail(), "Neue Support-Anfrage", "Der Benutzer " + user.getFirstName() + " " + user.getLastName() + " hat eine neue Support-Anfrage erstellt!")).start();
            }
        }
        return room.getRid();
    }

    /**
     * get rooms that are not yet being supported
     *
     * @param session the session who requests this
     * @return a collection of unsupported rooms
     */
    public Collection<Room> getUnsupportedRooms(UUID session) {
        User user = userService.getSessionMap().get(session);
        if (user == null) {
            return null;
        }

        return roomRepository.findUnsupportedRooms();
    }

    /**
     * get a list of rooms for a supporter that he's in charge of
     *
     * @param session the id of the user
     * @return a collection of his rooms
     */
    public Collection<Room> getSupportedRooms(UUID session) {
        User user = userService.getSessionMap().get(session);
        if (user == null) {
            return null;
        }

        return roomRepository.findSupportedRooms(user);
    }

    /**
     * claim a room
     *
     * @param session the id of the supporter
     * @param roomId  the id of the room
     * @return true if the room was claimed successfully
     */
    public boolean setSupporter(UUID session, Long roomId) {
        User supporter = userService.getSessionMap().get(session);
        if (supporter == null) {
            return false;
        }

        Room room = roomRepository.findRoomByRid(roomId);
        if (room == null) {
            return false;
        }

        if (room.getSupporter() != null) {
            return false;
        }

        room.setSupporter(supporter);
        roomRepository.save(room);
        System.out.println(supporter.getEmail() + " is now supporting room " + room.getRid());
        return true;
    }

    /**
     * closes a room
     *
     * @param session the session of the user
     * @param roomId  the id of the room
     * @return true if the room was closed successfully
     */
    public boolean closeRoom(UUID session, Long roomId) {
        User supporter = userService.getSessionMap().get(session);
        if (supporter == null) {
            return false;
        }

        Room room = roomRepository.findRoomByRid(roomId);
        if (room == null) {
            return false;
        }

        room.setOpen(false);
        roomRepository.save(room);
        System.out.println(supporter.getEmail() + " closed room " + room.getRid());
        return true;
    }

    /**
     * free the room so other supporters can enter again
     *
     * @param session the session of the user
     * @param roomId  the id of the room
     * @return true if the process was completed successfully
     */
    public boolean freeRoom(UUID session, Long roomId) {
        User supporter = userService.getSessionMap().get(session);
        if (supporter == null) {
            return false;
        }

        Room room = roomRepository.findRoomByRid(roomId);
        if (room == null) {
            return false;
        }

        room.setSupporter(null);
        roomRepository.save(room);
        System.out.println(supporter.getEmail() + " freed room " + room.getRid());
        return true;
    }

    /**
     * log a new message
     *
     * @param roomId the id of the room
     * @param userId the id of the user
     * @param text   the message
     * @param type   the type (JOIN/MESSAGE/FREE)
     */
    public void logMessage(Long roomId, Long userId, String text, Type type) {
        Room room = roomRepository.findRoomByRid(roomId);
        if (room == null) {
            return;
        }

        User user = userService.getUserRepository().getUserById(userId);
        if (user == null) {
            return;
        }

        messageRepository.save(new Message(text, type.toString(), user, room));
    }

    /**
     * get a list of previous messages
     *
     * @param session the user who is requesting the messages
     * @param roomId  the id of the room
     * @return a collection of messages
     */
    public Collection<Message> getMessages(UUID session, Long roomId) {
        // check if the room exists
        Room room = roomRepository.findRoomByRid(roomId);
        if (room == null) {
            return null;
        }

        // checks if the user has an active session
        User user = userService.getSessionMap().get(session);
        if (user == null) {
            return null;
        }

        // check if the user is allowed to load the messages
        if (user.getUid().longValue() != room.getCreator().getUid()) {
            if (room.getSupporter() != null) {
                if (user.getUid().longValue() != room.getSupporter().getUid()) {
                    return null;
                }
            } else {
                return null;
            }
        }

        return messageRepository.findMessagesByRoom(room);
    }

    /**
     * listen for delete event
     *
     * @param event event
     */
    @Override
    public void onApplicationEvent(DeleteEvent event) {
        if (event.getObject() instanceof User) {
            messageRepository.deleteAllBySender((User) event.getObject());
            for (Room room : roomRepository.findAllByCreator((User) event.getObject())) {
                messageRepository.deleteAllByRoom(room);
            }
            roomRepository.deleteAllByCreator((User) event.getObject());
            roomRepository.removeSupporterFromRooms((User) event.getObject());
        }
    }
}
