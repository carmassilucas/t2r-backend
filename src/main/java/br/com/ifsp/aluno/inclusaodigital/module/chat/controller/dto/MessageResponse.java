package br.com.ifsp.aluno.inclusaodigital.module.chat.controller.dto;

import java.time.Instant;
import java.util.UUID;

public record MessageResponse(UUID id,
                              String content,
                              Boolean read,
                              UUID replyTo,
                              Boolean isSender,
                              Instant createdAt) {
}
