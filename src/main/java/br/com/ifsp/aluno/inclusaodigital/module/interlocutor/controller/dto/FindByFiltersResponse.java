package br.com.ifsp.aluno.inclusaodigital.module.interlocutor.controller.dto;

import br.com.ifsp.aluno.inclusaodigital.module.interlocutor.entity.InterlocutorType;

import java.util.UUID;

public record FindByFiltersResponse(
        UUID id,
        String name,
        String aboutMe,
        InterlocutorType interlocutorType,
        String currentState,
        String currentCity,
        String profilePicture
) {
}
