package br.com.ifsp.aluno.inclusaodigital.module.interlocutor.service;

import br.com.ifsp.aluno.inclusaodigital.exception.CommonException;
import br.com.ifsp.aluno.inclusaodigital.exception.EmailAlreadyExistsException;
import br.com.ifsp.aluno.inclusaodigital.exception.InterlocutorNotFoundException;
import br.com.ifsp.aluno.inclusaodigital.exception.PasswordsNotMatchException;
import br.com.ifsp.aluno.inclusaodigital.module.interlocutor.controller.dto.*;
import br.com.ifsp.aluno.inclusaodigital.module.interlocutor.entity.Interlocutor;
import br.com.ifsp.aluno.inclusaodigital.module.interlocutor.repository.InterlocutorRepository;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class InterlocutorService {
    private final String authenticateJwtIssuer;

    private final String authenticateAlgorithmSecret;

    private final InterlocutorRepository interlocutorRepository;
    private final PasswordEncoder passwordEncoder;

    public InterlocutorService(@Value("authentication.jwt.issuer") String authenticateJwtIssuer,
                               @Value("${authentication.algorithm.secret}") String authenticateAlgorithmSecret,
                               InterlocutorRepository interlocutorRepository,
                               PasswordEncoder passwordEncoder) {
        this.authenticateJwtIssuer = authenticateJwtIssuer;
        this.authenticateAlgorithmSecret = authenticateAlgorithmSecret;
        this.interlocutorRepository = interlocutorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthInterlocutorResponse auth(AuthInterlocutorRequest authInterlocutorRequest) {
        var interlocutor = this.interlocutorRepository.findByEmail(authInterlocutorRequest.email())
                .orElseThrow(InterlocutorNotFoundException::new);

        var passwordsMatch = passwordEncoder.matches(
                authInterlocutorRequest.password(),
                interlocutor.getPassword()
        );

        if (!passwordsMatch)
            throw new PasswordsNotMatchException();

        var algorithm = Algorithm.HMAC256(authenticateAlgorithmSecret);
        var expiresIn = Instant.now().plus(Duration.ofHours(3));

        var token = JWT.create()
                .withIssuer(authenticateJwtIssuer)
                .withSubject(interlocutor.getId().toString())
                .withExpiresAt(expiresIn)
                .sign(algorithm);

        return new AuthInterlocutorResponse(token, expiresIn.toEpochMilli());
    }

    public Interlocutor createInterlocutor(CreateInterlocutorRequest dto) {
        this.interlocutorRepository.findByEmail(dto.email())
                .ifPresent((interlocutor) -> {
                    throw new EmailAlreadyExistsException();
                });

        var passwordEncoded = passwordEncoder.encode(dto.password().trim());

        var interlocutor = dto.toInterlocutor();
        interlocutor.setPassword(passwordEncoded);

        return this.interlocutorRepository.save(interlocutor);
    }

    public ProfileInterlocutorResponse getInterlocutorProfile(UUID id) {
        var interlocutor = this.interlocutorRepository.findById(id).orElseThrow(InterlocutorNotFoundException::new);

        return new ProfileInterlocutorResponse(
                interlocutor.getId(),
                interlocutor.getName(),
                interlocutor.getAboutMe(),
                interlocutor.getEmail(),
                interlocutor.getInterlocutorType(),
                interlocutor.getDateOfBirth(),
                interlocutor.getCurrentState(),
                interlocutor.getCurrentCity(),
                interlocutor.getProfilePicture()
        );
    }

    public void updateInterlocutor(UpdateInterlocutorRequest dto, UUID id) {
        var interlocutor = this.interlocutorRepository.findById(id).orElseThrow(InterlocutorNotFoundException::new);

        updateInterlocutor(interlocutor, dto);

        this.interlocutorRepository.saveAndFlush(interlocutor);
    }

    public void updateInterlocutorPassword(UpdatePasswordRequest dto, UUID id) {
        var interlocutor = this.interlocutorRepository.findById(id).orElseThrow(InterlocutorNotFoundException::new);

        var passwordMatches = dto.newPassword().equals(dto.confirmPassword());

        if (!passwordMatches)
            throw new PasswordsNotMatchException();

        var correctPassword = passwordEncoder.matches(dto.currentPassword(), interlocutor.getPassword());

        if (!correctPassword)
            throw new InterlocutorNotFoundException();

        var encodedPassword = passwordEncoder.encode(dto.newPassword().trim());

        interlocutor.setPassword(encodedPassword);

        this.interlocutorRepository.saveAndFlush(interlocutor);
    }

    public List<FindByFiltersResponse> findInterlocutorsByFilter(FindInterlocutorsByFilterRequest dto, UUID id) {
        var interlocutors = this.interlocutorRepository.findByFilters(dto.currentState(), dto.currentCity(), dto.name(), id);

        return interlocutors.stream().map(
                interlocutor -> new FindByFiltersResponse(
                        interlocutor.getId(),
                        interlocutor.getName(),
                        interlocutor.getAboutMe(),
                        interlocutor.getInterlocutorType(),
                        interlocutor.getCurrentState(),
                        interlocutor.getCurrentCity(),
                        interlocutor.getProfilePicture()
                )
        ).toList();
    }

    private void updateInterlocutor(Interlocutor interlocutor, UpdateInterlocutorRequest dto) {
        Arrays.stream(UpdateInterlocutorRequest.class.getDeclaredFields()).forEach(field -> {
            field.setAccessible(true);

            try {
                Object value = field.get(dto);

                if (value != null && !value.toString().trim().isEmpty()) {
                    Field interlocutorField = Interlocutor.class.getDeclaredField(field.getName());
                    interlocutorField.setAccessible(true);
                    interlocutorField.set(interlocutor, value);
                }
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new CommonException();
            }
        });
    }
}
