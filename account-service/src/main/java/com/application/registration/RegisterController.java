package com.application.registration;

import com.application.user.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(path = "/register")
public class RegisterController {
    private final RegisterService registerService;
    private final UserService userService;

    @Autowired
    public RegisterController(RegisterService registerService, UserService userService) {
        this.registerService = registerService;
        this.userService = userService;
    }

    @PostMapping
    public void registerAccount(RegistrationRequest request) throws ResponseStatusException{
        if(request.getUsername().isEmpty() || request.getPassword().isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username or password is empty");
        }

        if(!StringUtils.isAlphanumeric(request.getUsername())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username should be alphanumeric");
        }

        // check account does not exist
        if(userService.checkUserExist(request.getUsername())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account Exists");
        }

        registerService.registerAccount(request.getUsername(), request.getPassword());

        // TODO: send welcome message (Bot) (async)
    }
}
