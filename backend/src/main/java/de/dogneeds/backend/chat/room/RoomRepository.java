package de.dogneeds.backend.chat.room;

import de.dogneeds.backend.user.user.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;

public interface RoomRepository extends CrudRepository<Room, Long> {
    @Query("SELECT r FROM Room r WHERE r.creator = ?1 AND r.open")
    Room hasOpenRoom(User user);

    @Query("SELECT r FROM Room r WHERE r.rid = ?1")
    Room findRoomByRid(Long roomId);

    @Query("SELECT r FROM Room r WHERE r.supporter IS NULL AND r.open")
    Collection<Room> findUnsupportedRooms();

    @Query("SELECT r FROM Room r WHERE r.supporter = ?1 AND r.open")
    Collection<Room> findSupportedRooms(User user);

    @Query("SELECT r FROM Room r WHERE r.creator = ?1")
    Collection<Room> findAllByCreator(User user);

    @Transactional
    @Modifying
    @Query("DELETE FROM Room r WHERE r.creator = ?1")
    void deleteAllByCreator(User user);

    @Transactional
    @Modifying
    @Query("UPDATE Room r SET r.supporter = null WHERE r.supporter = ?1")
    void removeSupporterFromRooms(User user);
}
