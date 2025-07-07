package it.easyridedb.control;

import it.easyridedb.dao.impl.PrenotazioneDAOImpl;
import it.easyridedb.model.Prenotazione;
import it.easyridedb.model.Utente;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;

@WebServlet("/annulla-prenotazione")
public class AnnullaPrenotazioneServlet extends HttpServlet {
    
    private PrenotazioneDAOImpl prenotazioneDao;
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.prenotazioneDao = new PrenotazioneDAOImpl();
        System.out.println("✅ AnnullaPrenotazioneServlet inizializzata");
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Imposta la risposta come JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        
        try {
            // Verifica sessione
            HttpSession session = request.getSession(false);
            if (session == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write("{\"success\": false, \"message\": \"Sessione scaduta\"}");
                return;
            }
            
            // Verifica utente loggato
            Utente utente = (Utente) session.getAttribute("utente");
            if (utente == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write("{\"success\": false, \"message\": \"Devi effettuare il login\"}");
                return;
            }
            
            // Recupera parametri
            String prenotazioneIdStr = request.getParameter("prenotazioneId");
            String action = request.getParameter("action");
            
            // Validazione parametri
            if (prenotazioneIdStr == null || action == null || !action.equals("annulla")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"success\": false, \"message\": \"Parametri non validi\"}");
                return;
            }
            
            int prenotazioneId;
            try {
                prenotazioneId = Integer.parseInt(prenotazioneIdStr);
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"success\": false, \"message\": \"ID prenotazione non valido\"}");
                return;
            }
            
            System.out.println("🔄 Tentativo annullamento prenotazione ID: " + prenotazioneId + 
                             " da utente: " + utente.getEmail());
            
            // Recupera la prenotazione
            Prenotazione prenotazione = prenotazioneDao.findById(prenotazioneId);
            
            if (prenotazione == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write("{\"success\": false, \"message\": \"Prenotazione non trovata\"}");
                return;
            }
            
            // Verifica che la prenotazione appartenga all'utente
            if (prenotazione.getUtenteId() != utente.getId()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.write("{\"success\": false, \"message\": \"Non autorizzato ad annullare questa prenotazione\"}");
                return;
            }
            
            // Verifica che la prenotazione possa essere annullata
            if (!"confermata".equals(prenotazione.getStato())) {
                out.write("{\"success\": false, \"message\": \"La prenotazione non può essere annullata (stato: " + 
                         prenotazione.getStatoDescrizione() + ")\"}");
                return;
            }
            
            // Verifica che non sia troppo tardi per annullare (24h prima del ritiro)
            if (prenotazione.getDataRitiro() != null) {
                LocalDate oggi = LocalDate.now();
                LocalDate dataRitiro = prenotazione.getDataRitiro().toLocalDate();
                
                if (oggi.isAfter(dataRitiro.minusDays(1))) {
                    out.write("{\"success\": false, \"message\": \"Non è possibile annullare la prenotazione a meno di 24 ore dal ritiro\"}");
                    return;
                }
            }
            
            // Esegui l'annullamento
            boolean successo = prenotazioneDao.updateStato(prenotazioneId, "annullata");
            
            if (successo) {
                System.out.println("✅ Prenotazione " + prenotazioneId + " annullata con successo");
                out.write("{\"success\": true, \"message\": \"Prenotazione annullata con successo\"}");
            } else {
                System.err.println("❌ Errore nell'annullamento della prenotazione " + prenotazioneId);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write("{\"success\": false, \"message\": \"Errore nell'annullamento della prenotazione\"}");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Errore servlet annullamento: " + e.getMessage());
            e.printStackTrace();
            
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"success\": false, \"message\": \"Errore interno del server\"}");
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        response.getWriter().write("{\"success\": false, \"message\": \"Metodo non supportato. Utilizzare POST.\"}");
    }
}