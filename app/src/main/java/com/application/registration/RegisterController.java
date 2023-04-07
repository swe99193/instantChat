package com.application.registration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(path = "/register")
public class RegisterController {
    @Autowired
    private final RegisterService registerService;

    public RegisterController(RegisterService registerService) {
        this.registerService = registerService;
    }

    @PostMapping
    public ModelAndView register(RegistrationRequest request){
        System.out.println("username: " + request.getUsername());
        System.out.println("password: " + request.getPassword());

        registerService.save(request.getUsername(), request.getPassword());
        return new ModelAndView("redirect:/login");
    }

    @GetMapping
    public String registerpage(Model model) {
//        model.addAttribute("name", name);
        return "register";
    }
}
