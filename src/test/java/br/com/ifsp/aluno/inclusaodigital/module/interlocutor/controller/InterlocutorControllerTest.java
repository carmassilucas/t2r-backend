package br.com.ifsp.aluno.inclusaodigital.module.interlocutor.controller;

import br.com.ifsp.aluno.inclusaodigital.module.interlocutor.controller.dto.*;
import br.com.ifsp.aluno.inclusaodigital.module.interlocutor.entity.Interlocutor;
import br.com.ifsp.aluno.inclusaodigital.module.interlocutor.entity.InterlocutorType;
import br.com.ifsp.aluno.inclusaodigital.module.interlocutor.repository.InterlocutorRepository;
import br.com.ifsp.aluno.inclusaodigital.module.interlocutor.repository.InterlocutorTypeRepository;
import br.com.ifsp.aluno.inclusaodigital.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InterlocutorControllerTest {
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private InterlocutorRepository interlocutorRepository;

    @Autowired
    private InterlocutorTypeRepository interlocutorTypeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Interlocutor interlocutor;

    private final String issuer;
    private final String secret;

    public InterlocutorControllerTest(@Value("${authentication.jwt.issuer}") String issuer,
                                      @Value("${authentication.algorithm.secret}") String secret) {
        this.issuer = issuer;
        this.secret = secret;
    }

    @BeforeEach
    public void popularTabelas() {
        this.mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        List<InterlocutorType> types = Arrays.stream(InterlocutorType.Values.values())
                .map(type -> new InterlocutorType(type.get().getId(), type.get().getDescription()))
                .collect(Collectors.toList());

        this.interlocutorTypeRepository.saveAll(types);

        this.interlocutorRepository.deleteAll();
        this.interlocutorRepository.flush();

        interlocutor = new Interlocutor(
                "Fulano de Tal",
                null,
                "email@test.com",
                LocalDate.now().minusYears(22),
                "SP",
                "Campinas",
                InterlocutorType.Values.collaborator
        );
        interlocutor.setPassword(passwordEncoder.encode("senhasecreta"));

        interlocutor = this.interlocutorRepository.save(interlocutor);
    }

    @Test
    @DisplayName("Deve autenticar interlocutor com sucesso")
    void deve_autenticar_com_sucesso() throws Exception {
        var authRequest = new AuthInterlocutorRequest("email@test.com", "senhasecreta");

        this.mvc.perform(MockMvcRequestBuilders.post("/interlocutor/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.objectToJSON(authRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.access_token").exists());
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar autenticar com senha errada")
    void deve_retornar_erro_autenticacao_senha_errada() throws Exception {
        var authRequest = new AuthInterlocutorRequest("email@test.com", "senhaerrada");

        this.mvc.perform(MockMvcRequestBuilders.post("/interlocutor/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.objectToJSON(authRequest)))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Deve criar um novo interlocutor")
    void deve_criar_um_interlocutor() throws Exception {
        var createRequest = new CreateInterlocutorRequest(
                "Fulano de Tal",
                null,
                "create@test.com",
                "senhasecreta",
                LocalDate.now().minusYears(22),
                "SP",
                "Campinas",
                InterlocutorType.Values.collaborator
        );

        this.mvc.perform(MockMvcRequestBuilders.post("/interlocutor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.objectToJSON(createRequest)))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @DisplayName("Deve buscar interlocutores por filtro")
    void deve_buscar_interlocutores_por_filtro() throws Exception {
        var filterRequest = new FindInterlocutorsByFilterRequest("SP", null, null);

        this.mvc.perform(MockMvcRequestBuilders.post("/interlocutor/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.objectToJSON(filterRequest))
                        .header("Authorization", "Bearer " + TestUtils.generateToken(issuer, interlocutor.getId(), secret)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Deve atualizar um interlocutor")
    void deve_atualizar_interlocutor() throws Exception {
        var updateRequest = new UpdateInterlocutorRequest(
                "Fulano de Tal Atualizado", null, null, null, null
        );

        this.mvc.perform(MockMvcRequestBuilders.put("/interlocutor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.objectToJSON(updateRequest))
                        .header("Authorization", TestUtils.generateToken(issuer, interlocutor.getId(), secret)))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @DisplayName("Deve atualizar senha do interlocutor")
    void deve_atualizar_senha_interlocutor() throws Exception {
        var request = new UpdatePasswordRequest("senhasecreta", "novasenha", "novasenha");

        this.mvc.perform(MockMvcRequestBuilders.patch("/interlocutor/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.objectToJSON(request))
                        .header("Authorization", TestUtils.generateToken(issuer, interlocutor.getId(), secret)))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @DisplayName("Deve buscar perfil do interlocutor")
    void deve_buscar_perfil_interlocutor() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.get("/interlocutor/profile")
                        .header("Authorization", TestUtils.generateToken(issuer, interlocutor.getId(), secret)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("email@test.com"));
    }
}
