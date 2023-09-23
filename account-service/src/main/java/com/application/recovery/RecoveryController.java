package com.application.recovery;

import com.application.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(path = "/accountrecovery")
public class RecoveryController {
    private final RecoveryService recoveryService;
    private final UserService userService;

    @Autowired
    public RecoveryController(RecoveryService recoveryService, UserService userService) {
        this.recoveryService = recoveryService;
        this.userService = userService;
    }

    @PutMapping
    public void resetPassword(RecoveryRequest request) throws ResponseStatusException{
        if(request.getUsername().isEmpty() || request.getPassword().isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username or password is empty");
        }

        // check account exists
        if(!userService.checkUserExist(request.getUsername()))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account does not exists");

        recoveryService.resetPassword(request.getUsername(), request.getPassword());
    }
}
