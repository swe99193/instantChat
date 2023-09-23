package com.application.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping(path = "/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/exists")
    public boolean checkUserExist(@RequestParam String username){
        return userService.checkUserExist(username);
    }

    @PutMapping("/setpassword")
    public void setNewPassword(@RequestBody Map<String, Object> body, Principal principal) throws ResponseStatusException {

        if(!body.containsKey("password") || body.get("password").toString().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password should not be empty");

        userService.setNewPassword(principal.getName(), body.get("password").toString());
    }
}
