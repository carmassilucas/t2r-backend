package br.com.ifsp.aluno.inclusaodigital.module.interlocutor.controller.dto;

import br.com.ifsp.aluno.inclusaodigital.module.interlocutor.entity.InterlocutorType;

import java.time.LocalDate;
import java.util.UUID;

public record ProfileInterlocutorResponse(UUID id,
                                          String name,
                                          String aboutMe,
                                          String email,
                                          InterlocutorType interlocutorType,
                                          LocalDate dateOfBirth,
                                          String currentState,
                                          String currentCity,
                                          String profilePicture) {
}
