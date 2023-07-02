package com.application.conversation_list;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;


@RestController
@RequestMapping(path = "/conversationList")
public class ConversationUserController {
    private final ConversationUserService conversationUserService;

    @Autowired
    public ConversationUserController(ConversationUserService conversationUserService) {
        this.conversationUserService = conversationUserService;
    }

    /**
    * return a list of conversation user for a user
    */
    @GetMapping
    public List<ConversationUser> listConversationUser(Principal principal){
        return conversationUserService.listConversationUser(principal.getName());
    }
}
