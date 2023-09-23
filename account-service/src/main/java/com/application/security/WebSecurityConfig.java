package com.application.security;

import com.application.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;

// Note: follow spring boot 3.0.0 standard
// see: https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter
@Configuration
@EnableWebSecurity(debug = true)
public class WebSecurityConfig{
    @Value("${frontend.url}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                .cors().and()
                .csrf().disable()   // FIXME: disable temporarily
                .authorizeRequests()
            // allow these routes to be accessed without authentication
//                .requestMatchers("/**")   // match any routes
                .requestMatchers( "/register", "accountrecovery", "/test/**", "/login", "/auth/**")
                .permitAll()
//                .and()
//                .authorizeRequests()
//                .anyRequest()
//                .authenticated()
                .and()
                .formLogin()
                .disable()      // avoid it overrides custom login api
//                .usernameParameter("username")
//                .passwordParameter("password")
//                .loginPage(frontendUrl+"/login")
//                .loginProcessingUrl("/login")
//                .defaultSuccessUrl(frontendUrl, true)     // disable this. redirect will cause CORS, unresolved
//                .permitAll()
//                .failureHandler(authenticationFailureHandler())
        ;
        return http.build();
    }

    @Autowired
    UserService userService;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    AuthenticationManagerBuilder builder;

    @Bean
    public AuthenticationManager authenticationManager() {
        // How DaoAuthenticationProvider works:
        // https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/dao-authentication-provider.html#servlet-authentication-daoauthenticationprovider
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userService);
        authenticationProvider.setPasswordEncoder(bCryptPasswordEncoder);

        return new ProviderManager(authenticationProvider);
    }

    // This code not working
//    @Autowired
//    AuthenticationManagerBuilder auth;
//
//    @Bean
//    public AuthenticationManager authenticationManager() throws Exception {
//        return auth.authenticationProvider(new DaoAuthenticationProvider()).userDetailsService(userService)
//                .passwordEncoder(bCryptPasswordEncoder).and().build();
//    }


    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    // ref: https://docs.spring.io/spring-security/reference/reactive/integrations/cors.html
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList(frontendUrl));
        configuration.setAllowedMethods(Collections.singletonList("*"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
