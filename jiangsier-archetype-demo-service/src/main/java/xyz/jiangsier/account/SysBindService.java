package xyz.jiangsier.account;

import xyz.jiangsier.model.User;

import java.net.URL;
import java.util.Collection;
import java.util.Date;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public interface SysBindService {
    boolean bind(User user, String platform, String sub, URL iss, Collection<String> aud,
                 String refreshToken, Date issuedAt, Date expiresAt);
    boolean unbind(User user, String platform);
    boolean isBound(User user, String platform);
    String getRefreshToken(User user, String platform);
    String getUserIdByPlatformAndSub(String platform, String sub);
}
