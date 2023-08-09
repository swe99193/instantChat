package com.application;

import com.application.registration.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class IndexController {

    private final SecurityContextRepository securityContextRepository;

    private final AuthenticationManager authenticationManager;


    public IndexController(SecurityContextRepository securityContextRepository, AuthenticationManager authenticationManager) {
        this.securityContextRepository = securityContextRepository;
        this.authenticationManager = authenticationManager;
    }

    /**
     * a public api to identify the user of current session
     */
    @GetMapping("/auth")
    public String authenticateSessionUser(Principal principal) {
        if(principal != null)
            System.out.println("✅ Principal name: " + principal.getName());
        else
            System.out.println("❌ Principal null");
        return principal != null? principal.getName(): null;
    }


    /**
     * custom login
     *
     * ref: https://docs.spring.io/spring-security/reference/servlet/authentication/session-management.html#store-authentication-manually
     */
    @PostMapping("/login")
    public void login(@RequestBody User loginRequest, HttpServletRequest request, HttpServletResponse response) {
        System.out.println("🔴 custom login processing");

        UsernamePasswordAuthenticationToken token = UsernamePasswordAuthenticationToken.unauthenticated(
                loginRequest.getUsername(), loginRequest.getPassword());


        // authentication object returned from authenticationManager
        Authentication authentication = authenticationManager.authenticate(token);

        SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();

        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);

        securityContextHolderStrategy.setContext(context);

        securityContextRepository.saveContext(context, request, response);

        System.out.println("✅ custom login succeed");
    }


}
