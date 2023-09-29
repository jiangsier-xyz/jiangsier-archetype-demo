package xyz.jiangsier.config;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import xyz.jiangsier.access.auth.customizer.OAuth2AuthorizationRequestCustomizer;
import xyz.jiangsier.access.auth.handler.OAuth2AuthenticationFailureHandler;
import xyz.jiangsier.access.auth.handler.OAuth2AuthenticationSuccessHandler;
import xyz.jiangsier.access.auth.ApiTokenAuthenticationProvider;
import xyz.jiangsier.access.auth.user.SysUserDetailsManager;
import xyz.jiangsier.service.account.SysAuthorityService;
import xyz.jiangsier.service.account.SysBindService;
import xyz.jiangsier.service.account.SysUserService;
import xyz.jiangsier.util.AuthorityUtils;

import java.util.Arrays;

@Configuration
@SuppressWarnings("unused")
public class SecurityConfig {
    @Value("${auth.role.adminUri:#{null}}")
    private String[] adminUri;

    @Value("${auth.role.apiUri:#{null}}")
    private String[] apiUri;

    @Value("${auth.role.privateUri:#{null}}")
    private String[] privateUri;

    @Value("${auth.role.publicUri:#{null}}")
    private String[] publicUri;

    @Value("${auth.login.uri}")
    private String loginUri;

    @Value("${auth.login.oauth2.successUri}")
    private String oauth2LoginSuccessUri;

    @Value("${auth.login.oauth2.failureUri}")
    private String oauth2LoginFailureUri;

    @Value("${auth.login.portal.successUri}")
    private String portalLoginSuccessUri;

    @Value("${auth.login.portal.failureUri}")
    private String portalLoginFailureUri;

    @Value("${auth.login.oauth2.baseUri:#{T(org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter).DEFAULT_AUTHORIZATION_REQUEST_BASE_URI}}")
    private String authorizationRequestBaseUri;

    private void configPrivatePath(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        if (ArrayUtils.isNotEmpty(privateUri)) {
            registry.requestMatchers(privateUri).denyAll();
        }
    }

    private void configPublicPath(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        if (ArrayUtils.isNotEmpty(publicUri)) {
            registry.requestMatchers(publicUri).permitAll().requestMatchers(oauth2LoginFailureUri).permitAll();
        }
    }

    private void configAdminPath(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        if (ArrayUtils.isNotEmpty(adminUri)) {
            registry.requestMatchers(adminUri).hasRole(AuthorityUtils.adminRole());
        }
    }

    private void configApiPath(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        if (ArrayUtils.isNotEmpty(apiUri)) {
            // registry.antMatchers(apiUri).hasRole(AuthorityUtils.clientRole());
        }
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           ClientRegistrationRepository clientRegistrationRepository,
                                           OAuth2AuthorizationRequestCustomizer oAuth2AuthorizationRequestCustomizer,
                                           OAuth2AuthorizedClientService oAuth2ClientService,
                                           UserDetailsManager userDetailsManager,
                                           ApiTokenAuthenticationProvider apiTokenAuthenticationProvider
                                           ) throws Exception {
        http.authorizeHttpRequests(registry -> {
            configPrivatePath(registry);
            configPublicPath(registry);
            configAdminPath(registry);
            configApiPath(registry);
            registry.anyRequest().authenticated();
        });

        http.formLogin(portal ->
                portal.loginPage(loginUri)
                        .permitAll()
                        .defaultSuccessUrl(portalLoginSuccessUri)
                        .failureUrl(portalLoginFailureUri)
        );

        SysUserDetailsManager sysUserDetailsManager = (SysUserDetailsManager) userDetailsManager;

        DefaultOAuth2AuthorizationRequestResolver oAuth2RequestResolver =  new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, authorizationRequestBaseUri);
        oAuth2RequestResolver.setAuthorizationRequestCustomizer(oAuth2AuthorizationRequestCustomizer);

        OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler = new OAuth2AuthenticationSuccessHandler(
                oAuth2ClientService, sysUserDetailsManager, oAuth2AuthorizationRequestCustomizer);
        oAuth2SuccessHandler.setDefaultTargetUrl(oauth2LoginSuccessUri);

        OAuth2AuthenticationFailureHandler oAuth2FailureHandler = new OAuth2AuthenticationFailureHandler(
                oAuth2AuthorizationRequestCustomizer);
        oAuth2FailureHandler.setDefaultFailureUrl(oauth2LoginFailureUri);

        http.oauth2Login(oauth2 ->
                oauth2.loginPage(loginUri)
                        .permitAll()
                        .authorizationEndpoint(endpoint ->
                                endpoint.authorizationRequestResolver(oAuth2RequestResolver))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler)
        );

        if (ArrayUtils.isNotEmpty(apiUri)) {
            http.addFilterAfter(
                    apiAuthenticationFilter(apiTokenAuthenticationProvider), UsernamePasswordAuthenticationFilter.class);
            http.csrf(csrfConf -> csrfConf.ignoringRequestMatchers(apiUri));
        }

        http.userDetailsService(userDetailsManager);

        return http.build();
    }

    @Bean
    public UserDetailsManager users(SysUserService userService,
                                    SysBindService bindService,
                                    SysAuthorityService authorityService,
                                    PasswordEncoder passwordEncoder) {
        return new SysUserDetailsManager(userService, bindService, authorityService, passwordEncoder);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    private RequestMatcher toRequestMatcher(String[] paths) {
        return new OrRequestMatcher(Arrays.stream(paths)
                .map(AntPathRequestMatcher::new)
                .map(RequestMatcher.class::cast)
                .toList());
    }

    private AuthenticationFilter apiAuthenticationFilter(ApiTokenAuthenticationProvider provider) {
        AuthenticationFilter filter = new AuthenticationFilter(provider::authenticate, provider);
        filter.setRequestMatcher(toRequestMatcher(apiUri));
        filter.setSuccessHandler((request, response, authentication) -> {});
        filter.setFailureHandler((request, response, exception) -> {});
        return filter;
    }
}
