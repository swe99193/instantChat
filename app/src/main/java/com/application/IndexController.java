package com.application;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class IndexController {
    /**
     * a public api to identify the user of current session
     */
    @GetMapping("/auth")
    public String authenticateSessionUser(Principal principal) {
        return principal != null? principal.getName(): null;
    }
}
