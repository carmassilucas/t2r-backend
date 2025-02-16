package br.com.ifsp.aluno.inclusaodigital.module.chat.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SendMessageRequest(@NotBlank String content,
                                 @NotNull UUID chatId,
                                 UUID replyToId) {
}
