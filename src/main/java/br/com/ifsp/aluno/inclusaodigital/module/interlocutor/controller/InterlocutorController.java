package br.com.ifsp.aluno.inclusaodigital.module.interlocutor.controller;

import br.com.ifsp.aluno.inclusaodigital.module.interlocutor.controller.dto.*;
import br.com.ifsp.aluno.inclusaodigital.module.interlocutor.entity.Interlocutor;
import br.com.ifsp.aluno.inclusaodigital.module.interlocutor.service.InterlocutorService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/interlocutor")
public class InterlocutorController {

    private final InterlocutorService interlocutorService;

    public InterlocutorController(InterlocutorService interlocutorService) {
        this.interlocutorService = interlocutorService;
    }

    @PostMapping("/auth")
    public ResponseEntity<AuthInterlocutorResponse> auth(@RequestBody AuthInterlocutorRequest authInterlocutorRequest) {
            var token = this.interlocutorService.auth(authInterlocutorRequest);
            return ResponseEntity.ok().body(token);
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreateInterlocutorRequest dto) {
            var interlocutor = this.interlocutorService.createInterlocutor(dto);
            return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/search")
    public ResponseEntity<List<FindByFiltersResponse>> findByFilter(@Valid @RequestBody FindInterlocutorsByFilterRequest dto,
                                                           HttpServletRequest httpServletRequest) {
        var id = UUID.fromString((String) httpServletRequest.getAttribute("interlocutor_id"));

        var interlocutors = this.interlocutorService.findInterlocutorsByFilter(dto, id);

        return ResponseEntity.ok().body(interlocutors);
    }

    @PutMapping
    public ResponseEntity<Void> update(@RequestBody UpdateInterlocutorRequest updateInterlocutorRequest,
                                         HttpServletRequest httpServletRequest) {
        var id = UUID.fromString((String) httpServletRequest.getAttribute("interlocutor_id"));

        this.interlocutorService.updateInterlocutor(updateInterlocutorRequest, id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest dto,
                                                 HttpServletRequest httpServletRequest) {
        var id = UUID.fromString((String) httpServletRequest.getAttribute("interlocutor_id"));

        this.interlocutorService.updateInterlocutorPassword(dto, id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileInterlocutorResponse> profile(HttpServletRequest httpServletRequest) {
        var id = UUID.fromString((String) httpServletRequest.getAttribute("interlocutor_id"));

        var interlocutorProfile = this.interlocutorService.getInterlocutorProfile(id);
        return ResponseEntity.ok().body(interlocutorProfile);
    }
}
