package application;

import java.time.LocalDateTime;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import application.model.entities.Messages;
import application.model.enums.Tipo;
import application.model.dao.MessagesDao;

import application.db.DB;
import application.queue.QueueProcessor;

@RestController
public class WebhookController {

    private final MessagesDao messagesDao = new MessagesDao(DB.getConnection());
    private final QueueProcessor queueProcessor;

    // Injeta a QueueProcessor via construtor (Spring cuida da criação)
    public WebhookController(QueueProcessor queueProcessor) {
        this.queueProcessor = queueProcessor;
    }

    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> receiveMessage(@RequestParam("From") String fromRaw,
                                                 @RequestParam("Body") String body) {

        System.out.println("Mensagem recebida de " + fromRaw + ": " + body);

        String from = fromRaw.replaceAll("^whatsapp:", "").trim();

        // Cria objeto message do tipo USUARIO
        Messages msgUsuario = new Messages(null, from, body, Tipo.USUARIO, LocalDateTime.now());

        // Salva a mensagem do usuário no banco
        messagesDao.insert(msgUsuario);
        
      
        // Enfileira para processamento assíncrono
        queueProcessor.adicionarMensagem(msgUsuario);

        
     // Se a fila estava vazia → não manda resposta, deixa a IA responder
     return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body("");
    }
}
