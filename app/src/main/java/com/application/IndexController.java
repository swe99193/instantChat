package com.application;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class IndexController {
    @GetMapping("/")
    public String main(Model model) {
//        model.addAttribute("name", name);
        return "index";
    }

/*
    map html form to java class (model)
    ref: https://spring.io/guides/gs/handling-form-submission/
*/
    @PostMapping("/chat")
    public String chat(@ModelAttribute ChatRequestModel chatRequestModel, Model model) {
        System.out.println("HIT api: /chat");
        System.out.println("chat with (username): " + chatRequestModel.getUsername());

        model.addAttribute("username", "userABC");
        return "chat";
    }
}
