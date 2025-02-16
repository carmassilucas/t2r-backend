package br.com.ifsp.aluno.inclusaodigital.module.chat.repository;

import br.com.ifsp.aluno.inclusaodigital.module.chat.entity.Message;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    @Query(
            value = "select tm.id, tm.content, tm.read, tm.reply_message_id, " +
                        "case when tm.sender_id = :interlocutorId then true else false end isSender, tm.created_at " +
                    "from tb_message tm where tm.chat_id = :chatId " +
                    "order by tm.created_at asc",
            nativeQuery = true
    )
    List<Object[]> findMessages(@Param("chatId") UUID chatId, @Param("interlocutorId") UUID interlocutorId);

    @Modifying
    @Query(
            value = "update tb_message " +
                    "set read = true " +
                    "where chat_id = :chatId and sender_id <> :interlocutorId and read = false",
            nativeQuery = true
    )
    void readMessages(@Param("chatId") UUID chatId, @Param("interlocutorId") UUID interlocutorId);
}
