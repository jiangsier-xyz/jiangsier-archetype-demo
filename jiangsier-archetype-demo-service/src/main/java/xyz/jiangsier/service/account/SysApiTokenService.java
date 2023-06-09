package xyz.jiangsier.service.account;

import xyz.jiangsier.model.User;
import xyz.jiangsier.util.PolicyUtils;

import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.util.List;

@SuppressWarnings("UnusedReturnValue")
public interface SysApiTokenService {
    String createToken(User user, ApiTokenType type, String policy, TemporalAmount duration);

    default String createToken(User user, TemporalAmount duration) {
        return createToken(user, ApiTokenType.ACCESS, PolicyUtils.all(), duration);
    }

    default String createToken(User user) {
        return createToken(user, Duration.ofDays(1));
    }

    List<String> listTokens(User user);

    boolean deleteToken(String token);

    boolean disableToken(String token);

    boolean isEnabled(String token);

    User getUser(String token);
}
