package com.application.registration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/register")
public class RegisterController {
    @Autowired
    private final RegisterService registerService;

    public RegisterController(RegisterService registerService) {
        this.registerService = registerService;
    }

    @PostMapping
    public void register(RegistrationRequest request){
        System.out.println("username: " + request.getUsername());
        System.out.println("password: " + request.getPassword());

        registerService.save(request.getUsername(), request.getPassword());
    }
}
