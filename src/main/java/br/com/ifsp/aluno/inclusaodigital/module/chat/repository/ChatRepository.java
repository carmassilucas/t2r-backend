package br.com.ifsp.aluno.inclusaodigital.module.chat.repository;

import br.com.ifsp.aluno.inclusaodigital.module.chat.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<Chat, UUID> {
    @Query(
            value = "select tc.* " +
                    "from tb_chat tc " +
                        "join tb_interlocutor_chat tic on tc.id = tic.chat_id " +
                    "where tc.is_group = false " +
                        "and tic.interlocutor_id in :interlocutorsIds " +
                    "group by tc.id " +
                    "having count(distinct tic.interlocutor_id) = 2",
            nativeQuery = true
    )
    Optional<Chat> findByInterlocutorsIds(@Param("interlocutorsIds") Set<UUID> interlocutorsIds);

    @Query(
            value = "select " +
                    "tc.id id, " +
                    "(select ti.name " +
                        "from tb_interlocutor ti " +
                        "join tb_interlocutor_chat tic on ti.id = tic.interlocutor_id " +
                        "where tic.chat_id = tc.id and ti.id <> :id) name, " +
                    "(select ti.profile_picture " +
                        "from tb_interlocutor ti " +
                        "join tb_interlocutor_chat tic on ti.id = tic.interlocutor_id " +
                        "where tic.chat_id = tc.id and ti.id <> :id) profilePicture," +
                    "(select ti.email " +
                        "from tb_interlocutor ti " +
                        "join tb_interlocutor_chat tic on ti.id = tic.interlocutor_id " +
                        "where tic.chat_id = tc.id and ti.id <> :id) email," +
                    "(select tm.content " +
                        "from tb_message tm " +
                        "where tm.chat_id = tc.id " +
                        "order by tm.created_at desc limit 1) lastMessage, " +
                    "(select count(tm.read) " +
                        "from tb_message tm " +
                        "where tm.chat_id = tc.id and tm.read = false and tm.sender_id <> :id) unreadMessages " +
                    "from tb_chat tc " +
                    "join tb_interlocutor_chat tic on tc.id = tic.chat_id " +
                    "where tic.interlocutor_id = :id",
            nativeQuery = true
    )
    List<Object[]> findByInterlocutorId(@Param("id") UUID id);

}
