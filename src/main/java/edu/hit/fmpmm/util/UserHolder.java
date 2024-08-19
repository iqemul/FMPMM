package edu.hit.fmpmm.util;

import edu.hit.fmpmm.dto.UserDTO;

public class UserHolder {
    private static final ThreadLocal<UserDTO> threadLocal = new ThreadLocal<>();

    public static UserDTO getUser() {
        return threadLocal.get();
    }

    public static void setUser(UserDTO user) {
        threadLocal.set(user);
    }

    public static void removeUser() {
        threadLocal.remove();
    }
}
