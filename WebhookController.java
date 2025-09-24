package application;

import java.time.LocalDateTime;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import model.entities.Messages;
import model.enums.Tipo;
import model.dao.MessagesDao;

import db.DB;
import queue.QueueProcessor;

@RestController
public class WebhookController {

    private final MessagesDao messagesDao = new MessagesDao(DB.getConnection());
    private final QueueProcessor queueProcessor;

    // Inject the QueueProcessor via constructor (Spring handles the instantiation)
    public WebhookController(QueueProcessor queueProcessor) {
        this.queueProcessor = queueProcessor;
    }

    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> receiveMessage(@RequestParam("From") String fromRaw,
                                                 @RequestParam("Body") String body) {

        System.out.println("Mensagem recebida de " + fromRaw + ": " + body);

        String from = fromRaw.replaceAll("^whatsapp:", "").trim();

        // Creates a message object of type USER
        Messages msgUsuario = new Messages(null, from, body, Tipo.USUARIO, LocalDateTime.now());

        // Saves the user's message in the database
        messagesDao.insert(msgUsuario);
        
      
        // Enqueues for asynchronous processing
        queueProcessor.adicionarMensagem(msgUsuario);

        
     // If the queue was empty → do not send a response, let the AI handle it
     return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body("");
    }
}

