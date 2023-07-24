package com.application.conversation_list;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;


@RestController
@RequestMapping(path = "/conversationList")
public class ConversationListController {
    private final ConversationListService conversationListService;

    @Autowired
    public ConversationListController(ConversationListService conversationListService) {
        this.conversationListService = conversationListService;
    }

    /**
    * return a list of conversation user for a user
    */
    @GetMapping
    public List<ConversationUser> listConversationUser(Principal principal){
        return conversationListService.listConversationUser(principal.getName());
    }
}
