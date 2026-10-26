package com.attendance.util;

import com.attendance.model.User;

/**
 * Very simple in-memory "session" holder — since this is a desktop app,
 * we just keep the logged-in user in a static field while the app runs.
 */
public class SessionManager {
    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }
}
