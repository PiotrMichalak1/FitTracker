package com.fittracker.fittracker.response;

import com.fittracker.fittracker.entity.User;

public record RegisterResponse (String id, String username, String email) {

    //TODO: add unit test
    public static RegisterResponse fromUser(User user) {
        return new RegisterResponse(user.getId(), user.getUsername(), user.getEmail());
    }
}