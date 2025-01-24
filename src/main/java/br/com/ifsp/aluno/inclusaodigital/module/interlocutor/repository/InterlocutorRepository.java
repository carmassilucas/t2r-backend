package br.com.ifsp.aluno.inclusaodigital.module.interlocutor.repository;

import br.com.ifsp.aluno.inclusaodigital.module.interlocutor.entity.Interlocutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InterlocutorRepository extends JpaRepository<Interlocutor, UUID> {
    Optional<Interlocutor> findByEmail(String email);

    @Query(
            value = "select interlocutor.* " +
                    "from tb_interlocutor interlocutor " +
                    "where (:state is null or interlocutor.current_state ilike concat('%', :state, '%')) " +
                    "and (:city is null or interlocutor.current_city ilike concat('%', :city, '%')) " +
                    "and (:name is null or interlocutor.name ilike concat('%', :name, '%')) " +
                    "and interlocutor.id <> :id",
            nativeQuery = true
    )
    List<Interlocutor> findByFilters(
            @Param("state") String state,
            @Param("city") String city,
            @Param("name") String name,
            @Param("id") UUID id
    );
}
