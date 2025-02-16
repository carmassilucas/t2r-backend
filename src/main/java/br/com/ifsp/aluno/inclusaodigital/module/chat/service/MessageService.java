package br.com.ifsp.aluno.inclusaodigital.module.chat.service;

import br.com.ifsp.aluno.inclusaodigital.exception.MessageNotFoundException;
import br.com.ifsp.aluno.inclusaodigital.module.chat.controller.dto.MessageResponse;
import br.com.ifsp.aluno.inclusaodigital.module.chat.controller.dto.SendMessageRequest;
import br.com.ifsp.aluno.inclusaodigital.module.chat.entity.Message;
import br.com.ifsp.aluno.inclusaodigital.module.chat.repository.MessageRepository;
import br.com.ifsp.aluno.inclusaodigital.module.interlocutor.entity.Interlocutor;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final ChatService chatService;
    private final MessageRepository messageRepository;
    private final EntityManager entityManager;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageService(ChatService chatService, MessageRepository messageRepository, EntityManager entityManager,
                          SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messageRepository = messageRepository;
        this.entityManager = entityManager;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public List<MessageResponse> getMessages(UUID chatId, UUID interlocutorId) {
        this.messageRepository.readMessages(chatId, interlocutorId);

        var messages = this.messageRepository.findMessages(chatId, interlocutorId);

        return mapToMessageResponse(messages);
    }

    @Transactional
    public void send(SendMessageRequest payload, UUID interlocutorSenderId) {
        var chat = this.chatService.get(payload.chatId());

        Message replyTo = null;

        if (payload.replyToId() != null)
            replyTo = this.messageRepository.findById(payload.replyToId()).orElseThrow(MessageNotFoundException::new);

        var sender = new Interlocutor();
        sender.setId(interlocutorSenderId);

        this.messageRepository.save(new Message(
                payload.content(),
                replyTo,
                sender,
                chat
        ));

        entityManager.flush();

        messagingTemplate.convertAndSend("/topic/messages", chat.getId());
    }

    private List<MessageResponse> mapToMessageResponse(List<Object[]> results) {
        return results.stream()
                .map(result -> new MessageResponse(
                        (UUID) result[0],
                        (String) result[1],
                        (Boolean) result[2],
                        (UUID) result[3],
                        (Boolean) result[4],
                        (Instant) result[5]
                ))
                .collect(Collectors.toList());
    }
}