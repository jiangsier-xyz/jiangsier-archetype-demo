package xyz.jiangsier.access.auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.Transient;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import xyz.jiangsier.access.auth.user.SysUserDetails;
import xyz.jiangsier.util.AuthorityUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Transient
public class ApiTokenAuthenticationToken extends AbstractAuthenticationToken {
    private final Set<String> tokens;
    private final SysUserDetails sysUser;

    private static List<GrantedAuthority> getAuthorities(SysUserDetails sysUser) {
        if (sysUser == null) {
            return null;
        }
        List<GrantedAuthority> authorities = new LinkedList<>(sysUser.getAuthorities());
        authorities.add(new SimpleGrantedAuthority(AuthorityUtils.CLIENT));
        return authorities;
    }

    public ApiTokenAuthenticationToken(
            SysUserDetails sysUser, Set<String> tokens, boolean authenticated) {
        super(getAuthorities(sysUser));
        this.sysUser = sysUser;
        this.tokens = tokens;
        setAuthenticated(authenticated);
    }

    @Override
    public Object getCredentials() {
        return tokens;
    }

    @Override
    public Object getPrincipal() {
        return sysUser;
    }
}
