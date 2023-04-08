package de.dogneeds.backend.chat.message;

import de.dogneeds.backend.chat.room.Room;
import de.dogneeds.backend.user.user.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;

public interface MessageRepository extends CrudRepository<Message, Long> {
    @Query("SELECT m FROM Message m WHERE m.room = ?1")
    Collection<Message> findMessagesByRoom(Room r);

    @Transactional
    @Modifying
    @Query("DELETE FROM Message m WHERE m.sender = ?1")
    void deleteAllBySender(User sender);

    @Transactional
    @Modifying
    @Query("DELETE FROM Message m WHERE m.room = ?1")
    void deleteAllByRoom(Room room);
}
