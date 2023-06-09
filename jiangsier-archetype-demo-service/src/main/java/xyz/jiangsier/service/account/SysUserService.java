package xyz.jiangsier.service.account;

import xyz.jiangsier.model.User;

import java.util.List;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface SysUserService {
    boolean createUser(User user);
    boolean updateUser(User user);
    boolean deleteUser(String username);
    boolean userExists(String username);
    User loadUserByUserId(String userId);
    User loadUserByUsername(String username);
    User loadUserByUsernameAndPassword(String username, String password);
    User loadUserByUsernameAndPlatform(String username, String platform);
    List<User> listUsers(int limit, int offset);
}
