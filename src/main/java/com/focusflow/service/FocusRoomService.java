package com.focusflow.service;

import com.focusflow.model.FocusRoom;
import com.focusflow.model.User;
import com.focusflow.repository.FocusRoomRepository;
import com.focusflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;

@Service
public class FocusRoomService {
    @Autowired
    private FocusRoomRepository focusRoomRepository;
    @Autowired
    private UserRepository userRepository;

    public List<FocusRoom> getAllRooms() {
        return focusRoomRepository.findAll();
    }

    public FocusRoom createRoom(String name, String description) {
        FocusRoom room = new FocusRoom();
        room.setName(name);
        room.setDescription(description);
        room.setActive(true);
        return focusRoomRepository.save(room);
    }

    public String joinRoom(Long roomId, String userEmail) {
        FocusRoom room = focusRoomRepository.findById(roomId).orElse(null);
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (room != null && user != null) {
            room.getParticipants().add(user);
            focusRoomRepository.save(room);
            return "joined";
        }
        return "error";
    }

    public String leaveRoom(Long roomId, String userEmail) {
        FocusRoom room = focusRoomRepository.findById(roomId).orElse(null);
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (room != null && user != null) {
            room.getParticipants().remove(user);
            focusRoomRepository.save(room);
            return "left";
        }
        return "error";
    }

    public Set<User> getRoomUsers(Long roomId) {
        FocusRoom room = focusRoomRepository.findById(roomId).orElse(null);
        return room != null ? room.getParticipants() : Set.of();
    }
}
