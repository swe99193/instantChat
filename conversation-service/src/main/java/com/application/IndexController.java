package com.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Slf4j
@RestController
public class IndexController {

    @GetMapping("/auth")
    public String authenticateSessionUser(Principal principal) {
        if(principal != null)
            log.info("✅ Principal name: " + principal.getName());
        else
            log.info("❌ Principal null");
        return principal != null? principal.getName(): null;
    }

}
