package com.application;

import com.application.chat.ChatRequestModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

@Controller
public class IndexController {
    @GetMapping("/")
    public String main(Model model) {
//        model.addAttribute("name", name);
        return "index";
    }

    /**
     *  map html form to java class (model)
     *  ref: https://spring.io/guides/gs/handling-form-submission/
     */
    @PostMapping("/chat")
    public String chat(@ModelAttribute ChatRequestModel chatRequestModel, Model model, Principal principal) {
        System.out.println("HIT api: /chat");
        System.out.println("session user: " + principal.getName());	// get sender name
        System.out.println("chat with (receiver): " + chatRequestModel.getReceiver());

        model.addAttribute("sender", principal.getName());
        model.addAttribute("receiver", chatRequestModel.getReceiver());
        return "chat";
    }
}
