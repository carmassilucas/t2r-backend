package br.com.ifsp.aluno.inclusaodigital.module.chat.controller.dto;

import java.util.UUID;

public record ChatDetailsResponse(UUID id,
                                  String name,
                                  String profilePicture,
                                  String email,
                                  String lastMessage,
                                  Long unreadMessages) {
}
