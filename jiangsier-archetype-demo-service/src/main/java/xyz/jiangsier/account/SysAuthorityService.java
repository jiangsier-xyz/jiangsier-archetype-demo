package xyz.jiangsier.account;

import org.springframework.lang.NonNull;
import xyz.jiangsier.model.User;

import java.util.Set;

@SuppressWarnings("UnusedReturnValue")
public interface SysAuthorityService {
    Set<String> listAuthorities(@NonNull User user);

    boolean updateAuthorities(User user, Set<String> authorities);
}
