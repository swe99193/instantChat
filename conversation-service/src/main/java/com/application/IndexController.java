package com.application;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class IndexController {

    @GetMapping("/auth")
    public String authenticateSessionUser(Principal principal) {
        if(principal != null)
            System.out.println("✅ Principal name: " + principal.getName());
        else
            System.out.println("❌ Principal null");
        return principal != null? principal.getName(): null;
    }

}
