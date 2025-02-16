package br.com.ifsp.aluno.inclusaodigital.module.chat.service;

import br.com.ifsp.aluno.inclusaodigital.exception.ChatNotFoundException;
import br.com.ifsp.aluno.inclusaodigital.exception.InterlocutorNotFoundException;
import br.com.ifsp.aluno.inclusaodigital.module.chat.controller.dto.ChatDetailsResponse;
import br.com.ifsp.aluno.inclusaodigital.module.chat.controller.dto.SendMessageRequest;
import br.com.ifsp.aluno.inclusaodigital.module.chat.entity.Chat;
import br.com.ifsp.aluno.inclusaodigital.module.chat.entity.InterlocutorChat;
import br.com.ifsp.aluno.inclusaodigital.module.chat.entity.InterlocutorChatId;
import br.com.ifsp.aluno.inclusaodigital.module.chat.repository.ChatRepository;
import br.com.ifsp.aluno.inclusaodigital.module.interlocutor.repository.InterlocutorRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final InterlocutorRepository interlocutorRepository;

    public ChatService(ChatRepository chatRepository, InterlocutorRepository interlocutorRepository) {
        this.chatRepository = chatRepository;
        this.interlocutorRepository = interlocutorRepository;
    }

    public UUID create(UUID senderId, UUID receiverId) {
        var chatAlreadyExistent = this.chatRepository.findByInterlocutorsIds(Set.of(senderId, receiverId));

        if (chatAlreadyExistent.isPresent())
            return chatAlreadyExistent.get().getId();

        var chat = new Chat();

        var relationship = Set.of(senderId, receiverId).stream().map(uuid -> {
            var interlocutor = this.interlocutorRepository.findById(uuid)
                    .orElseThrow(InterlocutorNotFoundException::new);

            return new InterlocutorChat(new InterlocutorChatId(uuid, chat.getId()), interlocutor, chat);
        }).collect(Collectors.toSet());

        chat.setInterlocutorChats(relationship);

        return this.chatRepository.save(chat).getId();
    }

    public List<ChatDetailsResponse> getChats(UUID id) {
        var chats = this.chatRepository.findByInterlocutorId(id);
        return mapToChatDetails(chats);
    }

    @Transactional
    public Chat get(UUID id) {
        return this.chatRepository.findById(id).orElseThrow(ChatNotFoundException::new);
    }

    private List<ChatDetailsResponse> mapToChatDetails(List<Object[]> results) {
        return results.stream()
                .map(result -> new ChatDetailsResponse(
                        (UUID) result[0],
                        (String) result[1],
                        (String) result[2],
                        (String) result[3],
                        (String) result[4],
                        (Long) result[5]
                ))
                .collect(Collectors.toList());
    }
}
