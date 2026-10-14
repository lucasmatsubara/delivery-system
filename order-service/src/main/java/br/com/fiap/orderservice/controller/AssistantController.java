package br.com.fiap.orderservice.controller;

import br.com.fiap.orderservice.dto.AssistantRequest;
import br.com.fiap.orderservice.dto.AssistantResponse;
import br.com.fiap.orderservice.exception.InvalidRequestException;
import br.com.fiap.orderservice.service.ChatService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/assistant")
public class AssistantController {

    private static final int MAX_QUESTION_LENGTH = 500;

    private final ChatService chatService;

    public AssistantController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public AssistantResponse ask(@RequestBody AssistantRequest request) {
        if (request.question() == null || request.question().isBlank()) {
            throw new InvalidRequestException("Question is required");
        }
        if (request.question().length() > MAX_QUESTION_LENGTH) {
            throw new InvalidRequestException("Question must have at most " + MAX_QUESTION_LENGTH + " characters");
        }
        return new AssistantResponse(chatService.ask(request.question().strip()));
    }

}
