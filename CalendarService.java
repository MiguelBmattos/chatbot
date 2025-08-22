package services;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.GoogleAuthorizeUtil;

public class CalendarService {

	private static final Logger logger = LoggerFactory.getLogger(CalendarService.class);
    private final Calendar calendar;

    public CalendarService() {
        try {
			this.calendar = GoogleAuthorizeUtil.getCalendarService();
		} catch (Exception e) {
			logger.error("Erro ao inicializar o Google Calendar", e);
			throw new RuntimeException("Erro ao inicializar o CalendarSErvice", e);
		}
    }

    // Verifica se há disponibilidade no horário desejado
    public boolean estaDisponivel(String medico, LocalDateTime inicio, int duracaoMin) throws IOException {
        LocalDateTime fim = inicio.plusMinutes(duracaoMin);

        DateTime timeMin = new DateTime(java.util.Date.from(inicio.atZone(ZoneId.systemDefault()).toInstant()));
        DateTime timeMax = new DateTime(java.util.Date.from(fim.atZone(ZoneId.systemDefault()).toInstant()));

        Events events = calendar.events().list("primary")
                .setTimeMin(timeMin)
                .setTimeMax(timeMax)
                .setSingleEvents(true)
                .setOrderBy("startTime")
                .execute();

        List<Event> items = events.getItems();
        for (Event e : items) {
            if (e.getSummary() != null && e.getSummary().toLowerCase().contains(medico.toLowerCase())) {
                return false; // Já tem evento no mesmo horário
            }
        }

        return true;
    }

    // Cria um evento no Google Calendar
    public void criarEvento(String medico, LocalDateTime inicio, int duracaoMin) throws IOException {
        LocalDateTime fim = inicio.plusMinutes(duracaoMin);

        Event event = new Event()
                .setSummary("Consulta com " + medico)
                .setDescription("Agendamento automático via chatbot WhatsApp.")
                .setStart(new EventDateTime()
                        .setDateTime(new DateTime(java.util.Date.from(inicio.atZone(ZoneId.systemDefault()).toInstant())))
                        .setTimeZone("America/Sao_Paulo"))
                .setEnd(new EventDateTime()
                        .setDateTime(new DateTime(java.util.Date.from(fim.atZone(ZoneId.systemDefault()).toInstant())))
                        .setTimeZone("America/Sao_Paulo"));

        calendar.events().insert("primary", event).execute();
    }
}
