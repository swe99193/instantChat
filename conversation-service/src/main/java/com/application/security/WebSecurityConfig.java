package com.application.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;
import java.util.List;

// Note: follow spring boot 3.0.0 standard
// see: https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
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
                .and()
                .authorizeRequests()
                .anyRequest()
                .authenticated()
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


    // ref: https://docs.spring.io/spring-security/reference/reactive/integrations/cors.html
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(frontendUrl, "https://localhost:3000", "http://localhost:3000"));
        configuration.setAllowedMethods(Collections.singletonList("*"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
