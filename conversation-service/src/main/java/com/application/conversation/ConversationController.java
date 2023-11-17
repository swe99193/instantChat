package com.application.conversation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/conversation")
public class ConversationController {
    private final ConversationService conversationService;

    @Autowired
    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    /**
     * return a list of conversation user for a user
     */
    @GetMapping
    public List<Map<String, Object>> listConversation(Principal principal){
        return conversationService.listConversation(principal.getName());
    }

    @PostMapping
    public Conversation createConversation(@RequestParam String receiver, Principal principal){
        return conversationService.createConversation(principal.getName(), receiver);
    }
}

