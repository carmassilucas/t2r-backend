package br.com.ifsp.aluno.inclusaodigital.module.interlocutor.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import br.com.ifsp.aluno.inclusaodigital.exception.EmailAlreadyExistsException;
import br.com.ifsp.aluno.inclusaodigital.exception.InterlocutorNotFoundException;
import br.com.ifsp.aluno.inclusaodigital.exception.PasswordsNotMatchException;
import br.com.ifsp.aluno.inclusaodigital.module.interlocutor.controller.dto.*;
import br.com.ifsp.aluno.inclusaodigital.module.interlocutor.entity.Interlocutor;
import br.com.ifsp.aluno.inclusaodigital.module.interlocutor.entity.InterlocutorType;
import br.com.ifsp.aluno.inclusaodigital.module.interlocutor.repository.InterlocutorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class InterlocutorServiceTest {

    @InjectMocks
    private InterlocutorService interlocutorService;

    @Mock
    private InterlocutorRepository interlocutorRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Environment environment;

    private CreateInterlocutorRequest request;

    @BeforeEach
    public void setup() {
        request = new CreateInterlocutorRequest(
                "Fulano de Tal",
                null,
                "email@test.com",
                "senhasecreta",
                LocalDate.now().minusYears(22),
                "SP",
                "São Paulo",
                InterlocutorType.Values.collaborator
        );

        ReflectionTestUtils.setField(interlocutorService, "authenticateAlgorithmSecret", "5158ad4e-2123-4513-9bb1-b5e8532a5787");
    }

    @Test
    @DisplayName("Não deve autenticar interlocutor inexistente")
    void nao_deve_autenticar_interlocutor_inexistente() {
        AuthInterlocutorRequest request = new AuthInterlocutorRequest("email@test.com", "senhasecreta");
        when(interlocutorRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        assertThrows(InterlocutorNotFoundException.class, () -> interlocutorService.auth(request));
    }

    @Test
    @DisplayName("Não deve autenticar se a senha estiver incorreta")
    void nao_deve_autenticar_senha_incorreta() {
        Interlocutor interlocutor = new Interlocutor();
        interlocutor.setPassword("senhasecreta");
        AuthInterlocutorRequest request = new AuthInterlocutorRequest("email@test.com", "senhaincorreta");

        when(interlocutorRepository.findByEmail(request.email())).thenReturn(Optional.of(interlocutor));
        when(passwordEncoder.matches(request.password(), interlocutor.getPassword())).thenReturn(false);

        assertThrows(PasswordsNotMatchException.class, () -> interlocutorService.auth(request));
    }

    @Test
    @DisplayName("Deve autenticar interlocutor com sucesso")
    void deve_autenticar_com_sucesso() {
        Interlocutor interlocutor = new Interlocutor();
        interlocutor.setId(UUID.randomUUID());
        interlocutor.setPassword("senhasecreta");
        AuthInterlocutorRequest request = new AuthInterlocutorRequest("email@test.com", "senhasecreta");

        when(interlocutorRepository.findByEmail(request.email())).thenReturn(Optional.of(interlocutor));
        when(passwordEncoder.matches(request.password(), interlocutor.getPassword())).thenReturn(true);
        System.setProperty("authentication.algorithm.secret", "5158ad4e-2123-4513-9bb1-b5e8532a5787");

        AuthInterlocutorResponse response = interlocutorService.auth(request);

        assertNotNull(response);
        assertNotNull(response.access_token());
        assertTrue(response.expires_in() > Instant.now().toEpochMilli());
    }

    @Test
    @DisplayName("Não deve criar interlocutor com email já existente")
    void nao_deve_criar_interlocutor_email_existente() {
        when(interlocutorRepository.findByEmail(request.email())).thenReturn(Optional.of(new Interlocutor()));
        assertThrows(EmailAlreadyExistsException.class, () -> interlocutorService.createInterlocutor(request));
    }

    @Test
    @DisplayName("Deve criar interlocutor com sucesso")
    void deve_criar_interlocutor() {
        Interlocutor interlocutor = request.toInterlocutor();
        interlocutor.setPassword("senhacodificada");

        when(interlocutorRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password().trim())).thenReturn("senhacodificada");
        when(interlocutorRepository.save(any(Interlocutor.class))).thenReturn(interlocutor);

        Interlocutor savedInterlocutor = interlocutorService.createInterlocutor(request);
        assertNotNull(savedInterlocutor);
        assertEquals("senhacodificada", savedInterlocutor.getPassword());
    }


    @Test
    @DisplayName("Não deve encontrar perfil de interlocutor inexistente")
    void nao_deve_encontrar_perfil_interlocutor() {
        UUID id = UUID.randomUUID();
        when(interlocutorRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(InterlocutorNotFoundException.class, () -> interlocutorService.getInterlocutorProfile(id));
    }

    @Test
    @DisplayName("Deve retornar perfil do interlocutor")
    void deveRetornarPerfilInterlocutor() {
        UUID id = UUID.randomUUID();
        Interlocutor interlocutor = new Interlocutor();
        interlocutor.setId(id);
        interlocutor.setName("Fulano de Tal");

        when(interlocutorRepository.findById(id)).thenReturn(Optional.of(interlocutor));

        ProfileInterlocutorResponse response = interlocutorService.getInterlocutorProfile(id);
        assertNotNull(response);
        assertEquals("Fulano de Tal", response.name());
    }

    @Test
    @DisplayName("Não deve atualizar senha se a confirmação falhar")
    void nao_deve_atualizar_senha_confirmacao_falhou() {
        UUID id = UUID.randomUUID();
        UpdatePasswordRequest request = new UpdatePasswordRequest("senhasecreta", "novasenhasecreta", "novasenhasecretadiferente");
        when(interlocutorRepository.findById(id)).thenReturn(Optional.of(new Interlocutor()));
        assertThrows(PasswordsNotMatchException.class, () -> interlocutorService.updateInterlocutorPassword(request, id));
    }

    @Test
    @DisplayName("Deve atualizar senha com sucesso")
    void deve_atualizar_senha() {
        UUID id = UUID.randomUUID();
        Interlocutor interlocutor = new Interlocutor();
        interlocutor.setPassword("senhasecreta");
        UpdatePasswordRequest request = new UpdatePasswordRequest("senhasecreta", "novasenhasecreta", "novasenhasecreta");

        when(interlocutorRepository.findById(id)).thenReturn(Optional.of(interlocutor));
        when(passwordEncoder.matches(request.currentPassword(), interlocutor.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(request.newPassword().trim())).thenReturn("novasenhasecretacodificada");

        interlocutorService.updateInterlocutorPassword(request, id);
        assertEquals("novasenhasecretacodificada", interlocutor.getPassword());
    }
}

