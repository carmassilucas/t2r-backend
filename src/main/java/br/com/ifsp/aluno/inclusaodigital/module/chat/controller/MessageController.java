package br.com.ifsp.aluno.inclusaodigital.module.chat.controller;

import br.com.ifsp.aluno.inclusaodigital.module.chat.controller.dto.SendMessageRequest;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Controller
public class MessageController {

    @MessageMapping("/send-message")
    @SendTo("/topic/messages")
    public String sendMessage(SendMessageRequest message) {
        return HtmlUtils.htmlEscape(message.chatId().toString());
    }
}
