package br.com.ifsp.aluno.inclusaodigital.module.chat.controller;

import br.com.ifsp.aluno.inclusaodigital.module.chat.controller.dto.ChatDetailsResponse;
import br.com.ifsp.aluno.inclusaodigital.module.chat.controller.dto.CreateChatPayload;
import br.com.ifsp.aluno.inclusaodigital.module.chat.controller.dto.MessageResponse;
import br.com.ifsp.aluno.inclusaodigital.module.chat.controller.dto.SendMessageRequest;
import br.com.ifsp.aluno.inclusaodigital.module.chat.entity.Chat;
import br.com.ifsp.aluno.inclusaodigital.module.chat.service.ChatService;
import br.com.ifsp.aluno.inclusaodigital.module.chat.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/chats")
public class ChatController {

    private final ChatService chatService;
    private final MessageService messageService;

    public ChatController(ChatService chatService, MessageService messageService) {
        this.chatService = chatService;
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<UUID> create(HttpServletRequest request, @RequestBody CreateChatPayload payload) {
        var senderId = UUID.fromString(request.getAttribute("interlocutor_id").toString());

        var id = this.chatService.create(senderId, payload.receiverId());

        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @GetMapping
    public ResponseEntity<List<ChatDetailsResponse>> listInterlocutorChats(HttpServletRequest request) {
        var id = UUID.fromString(request.getAttribute("interlocutor_id").toString());

        var chats = this.chatService.getChats(id);

        return ResponseEntity.ok(chats);
    }

    @GetMapping("/messages/{id}")
    public ResponseEntity<List<MessageResponse>> getMessages(HttpServletRequest request, @PathVariable UUID id) {
        var interlocutorId = UUID.fromString(request.getAttribute("interlocutor_id").toString());

        var messages = this.messageService.getMessages(id, interlocutorId);

        return ResponseEntity.ok(messages);
    }

    @PostMapping("/messages/send")
    public ResponseEntity<Void> sendMessage(HttpServletRequest request,
                                            @Valid @RequestBody SendMessageRequest payload) {
        var id = UUID.fromString(request.getAttribute("interlocutor_id").toString());

        this.messageService.send(payload, id);

        return ResponseEntity.noContent().build();
    }
}
