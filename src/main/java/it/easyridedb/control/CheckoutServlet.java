package it.easyridedb.control;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import it.easyridedb.model.*;
import it.easyridedb.dao.DatabaseConnection;
import it.easyridedb.dao.impl.PrenotazioneDAOImpl;
import it.easyridedb.util.EmailManager;

@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("🛒 Checkout GET - Visualizzazione pagina");
        
        // Controllo autenticazione
        HttpSession session = request.getSession(false);
        Utente utente = (session != null) ? (Utente) session.getAttribute("utente") : null;
        
        if (utente == null) {
            response.sendRedirect(request.getContextPath() + "/login?message=Accedi per completare l'ordine");
            return;
        }
        
        // Recupera carrello
        Carrello carrello = (Carrello) session.getAttribute("carrello");
        if (carrello == null || carrello.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/carrello?message=Il carrello è vuoto");
            return;
        }
        
        
        System.out.println("✅ Validazione carrello saltata temporaneamente per permettere il checkout");
        System.out.println("🔍 Dettagli carrello per debug:");
        for (CarrelloItem item : carrello.getItems()) {
            System.out.println("   - " + item.getDescrizioneVeicolo() + ": " + 
                             item.getDataRitiro() + " → " + item.getDataRestituzione() + 
                             " (€" + item.getPrezzoTotale() + ")");
        }
        
        
        String csrfToken = generateSimpleCSRFToken(session);
        
        
        request.setAttribute("carrello", carrello);
        request.setAttribute("csrfToken", csrfToken);
        
      
        request.getRequestDispatcher("/WEB-INF/views/checkout.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("💳 Checkout POST - Elaborazione prenotazione");
        System.out.println("📝 DEBUG COMPLETO - TUTTI I PARAMETRI RICEVUTI:");
        System.out.println("   - Content-Type: " + request.getContentType());
        System.out.println("   - Method: " + request.getMethod());
        System.out.println("   - Content-Length: " + request.getContentLength());
        System.out.println("   - Character Encoding: " + request.getCharacterEncoding());
        
        System.out.println("📋 Parametri del form:");
        if (request.getParameterMap().isEmpty()) {
            System.out.println("   ❌ NESSUN PARAMETRO RICEVUTO!");
        } else {
            request.getParameterMap().forEach((key, values) -> {
                System.out.println("   ✅ " + key + ": " + String.join(", ", values));
            });
        }
        
        String csrfFromRequest = request.getParameter("csrfToken");
        System.out.println("🔍 Debug specifico CSRF Token:");
        System.out.println("   - request.getParameter('csrfToken'): '" + csrfFromRequest + "'");
        System.out.println("   - È null?: " + (csrfFromRequest == null));
        System.out.println("   - È vuoto?: " + (csrfFromRequest != null && csrfFromRequest.isEmpty()));
        
        // Controllo autenticazione
        HttpSession session = request.getSession(false);
        Utente utente = (session != null) ? (Utente) session.getAttribute("utente") : null;
        
        if (utente == null) {
            sendJsonResponse(response, false, "Sessione scaduta. Accedi nuovamente.");
            return;
        }
        
        String csrfToken = request.getParameter("csrfToken");
        if (!validateSimpleCSRFToken(session, csrfToken)) {
            System.out.println("❌ VALIDAZIONE CSRF FALLITA - Invio risposta di errore");
            sendJsonResponse(response, false, "Token di sicurezza non valido");
            return;
        }
        
        System.out.println("✅ CSRF Token validato con successo!");
        
        // Recupera carrello
        Carrello carrello = (Carrello) session.getAttribute("carrello");
        if (carrello == null || carrello.isEmpty()) {
            sendJsonResponse(response, false, "Il carrello è vuoto");
            return;
        }
        
        // Recupera dati form
        String action = request.getParameter("action");
        System.out.println("🎯 Action ricevuta: '" + action + "'");
        
        if ("conferma-prenotazione".equals(action)) {
            elaboraPrenotazione(request, response, utente, carrello);
        } else {
            System.out.println("❌ Action non valida: '" + action + "'");
            sendJsonResponse(response, false, "Azione non valida");
        }
    }
    
    private void elaboraPrenotazione(HttpServletRequest request, HttpServletResponse response, 
                                   Utente utente, Carrello carrello) throws ServletException, IOException {
        
        // ✅ RECUPERA SESSION DAL REQUEST
        HttpSession session = request.getSession();
        
        System.out.println("🔄 Elaborazione prenotazione per utente: " + utente.getEmail());
        
        Connection conn = null;
        
        try {
            // Recupera dati form
            String note = request.getParameter("note");
            String paymentMethod = request.getParameter("paymentMethod");
            String newsletter = request.getParameter("newsletter");
            
            System.out.println("📝 Dati ricevuti:");
            System.out.println("   - Note: " + note);
            System.out.println("   - Metodo pagamento: " + paymentMethod);
            System.out.println("   - Newsletter: " + newsletter);
            
            // ✅ USA LA  CLASSE DatabaseConnection
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // ✅ USA IL  DAO PATTERN
            PrenotazioneDAOImpl prenotazioneDAO = new PrenotazioneDAOImpl();
            
            // Per ogni item nel carrello, crea una prenotazione
            for (CarrelloItem item : carrello.getItems()) {
                
                // ✅ Crea oggetto Prenotazione usando il tuo model (con java.sql.Date)
                Prenotazione prenotazione = new Prenotazione();
                prenotazione.setUtenteId(utente.getId());
                prenotazione.setVeicoloTarga(item.getTargaVeicolo());
                
                // ✅ CONVERSIONE CORRETTA: LocalDate → java.sql.Date
                prenotazione.setDataRitiro(java.sql.Date.valueOf(item.getDataRitiro()));
                prenotazione.setDataRestituzione(java.sql.Date.valueOf(item.getDataRestituzione()));
                
                prenotazione.setTerminalRitiroId(item.getTerminalRitiroId());
                prenotazione.setTerminalRestituzioneId(item.getTerminalRestituzioneId());
                prenotazione.setPrezzoTotale(item.getPrezzoTotale());
                prenotazione.setStato("confermata");
                
                // ✅ USA IL  METODO DAO CORRETTO
                boolean success = prenotazioneDAO.insert(prenotazione);
                
                if (success) {
                    System.out.println("✅ Prenotazione creata con ID: " + prenotazione.getId() + 
                                     " per veicolo: " + item.getTargaVeicolo());
                    
                    // ✅ Salva optional se presenti
                    if (item.getOptionalSelezionati() != null && !item.getOptionalSelezionati().isEmpty()) {
                        salvaOptionalPrenotazione(conn, prenotazione.getId(), item.getOptionalSelezionati());
                    }
                    
                } else {
                    throw new SQLException("Errore nella creazione della prenotazione per " + item.getTargaVeicolo());
                }
            }
            
            // Commit transazione
            conn.commit();
            System.out.println("💾 Transazione completata con successo");
            
            // 📧 GENERA BOOKING ID PER LE EMAIL
            String bookingId = generateBookingId(utente.getId());
            System.out.println("🎫 Booking ID generato: " + bookingId);
            
            // 📧 INVIA ENTRAMBE LE EMAIL (asincrono)
            try {
                System.out.println("📧 Iniziando invio email...");
                EmailManager emailManager = EmailManager.getInstance();
                
                // 1️⃣ EMAIL ALL'ADMIN con tutti i dettagli
                CompletableFuture<Boolean> adminEmailFuture = emailManager.inviaNotificaAdminAsync(utente, carrello, note, paymentMethod);
                
                // 2️⃣ EMAIL DI CONFERMA AL CLIENTE
                CompletableFuture<Boolean> customerEmailFuture = emailManager.inviaConfermaClienteAsync(utente, carrello, bookingId);
                
                // Aspetta che entrambe le email siano inviate (opzionale - per logging)
                CompletableFuture.allOf(adminEmailFuture, customerEmailFuture).thenRun(() -> {
                    System.out.println("✅ Tutte le email sono state elaborate!");
                });
                
                System.out.println("📤 Email in elaborazione asincrona...");
                System.out.println("   - Admin email: antoninostinga0313@gmail.com");
                System.out.println("   - Cliente email: " + utente.getEmail());
                
            } catch (Exception e) {
                System.err.println("⚠️ Errore invio email (non critico): " + e.getMessage());
                e.printStackTrace();
                // Non bloccare il processo per errori email
            }
            
            // Svuota carrello
            session.removeAttribute("carrello");
            System.out.println("🛒 Carrello svuotato");
            
            // ✅ RISPOSTA JSON DI SUCCESSO
            sendJsonResponse(response, true, "Prenotazione completata con successo! Riceverai una email di conferma.");
            
        } catch (Exception e) {
            System.err.println("❌ Errore durante l'elaborazione: " + e.getMessage());
            e.printStackTrace();
            
            // Rollback transazione
            try {
                if (conn != null) {
                    conn.rollback();
                    System.out.println("🔄 Rollback eseguito");
                }
            } catch (SQLException ex) {
                System.err.println("❌ Errore durante rollback: " + ex.getMessage());
            }
            
            // ✅ RISPOSTA JSON DI ERRORE
            sendJsonResponse(response, false, "Errore durante l'elaborazione della prenotazione. Riprova.");
            
        } finally {
            // Chiudi connessione
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("❌ Errore chiusura connessione: " + e.getMessage());
            }
        }
    }
    
    // ✅ NUOVO METODO PER RISPONDERE CON JSON
    private void sendJsonResponse(HttpServletResponse response, boolean success, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        String jsonResponse = String.format(
            "{\"success\": %b, \"message\": \"%s\"}", 
            success, 
            message.replace("\"", "\\\"") // Escape quotes
        );
        
        out.print(jsonResponse);
        out.flush();
        
        System.out.println("📤 Risposta JSON inviata: " + jsonResponse);
    }
    
    // === CSRF TOKEN SEMPLICE ===
    
    private String generateSimpleCSRFToken(HttpSession session) {
        String token = "csrf_" + System.currentTimeMillis() + "_" + Math.random();
        session.setAttribute("csrfToken", token);
        
        // ✅ DEBUG GENERAZIONE TOKEN
        System.out.println("🔐 CSRF Token generato:");
        System.out.println("   - Token: '" + token + "'");
        System.out.println("   - Session ID: " + session.getId());
        
        return token;
    }
    
    private boolean validateSimpleCSRFToken(HttpSession session, String token) {
        String sessionToken = (String) session.getAttribute("csrfToken");
        
        // ✅ DEBUG DETTAGLIATO
        System.out.println("🔍 CSRF Token Validation:");
        System.out.println("   - Token ricevuto dal form: '" + token + "'");
        System.out.println("   - Token salvato in sessione: '" + sessionToken + "'");
        System.out.println("   - Session ID: " + session.getId());
        System.out.println("   - Session creazione: " + new java.util.Date(session.getCreationTime()));
        System.out.println("   - Session ultimo accesso: " + new java.util.Date(session.getLastAccessedTime()));
        
        if (sessionToken == null) {
            System.out.println("❌ Token in sessione è NULL!");
            return false;
        }
        
        if (token == null) {
            System.out.println("❌ Token ricevuto è NULL!");
            return false;
        }
        
        boolean tokensEqual = sessionToken.equals(token);
        System.out.println("   - Tokens uguali: " + tokensEqual);
        
        if (!tokensEqual) {
            System.out.println("❌ Token non corrispondono!");
            System.out.println("   - Lunghezza sessione: " + sessionToken.length());
            System.out.println("   - Lunghezza ricevuto: " + token.length());
            
            // Debug caratteri differenti
            int minLength = Math.min(sessionToken.length(), token.length());
            for (int i = 0; i < minLength; i++) {
                if (sessionToken.charAt(i) != token.charAt(i)) {
                    System.out.println("   - Primo carattere diverso alla posizione " + i + ": '" + 
                                     sessionToken.charAt(i) + "' vs '" + token.charAt(i) + "'");
                    break;
                }
            }
            return false;
        }
        
        System.out.println("✅ Token valido!");
        return true;
    }
    
    
    
   
    private boolean salvaOptionalPrenotazione(Connection conn, int prenotazioneId, List<Optional> optionals) throws SQLException {
        if (optionals == null || optionals.isEmpty()) {
            return true; // Nessun optional da salvare
        }
        
        String sql = "INSERT INTO prenotazioni_optional (prenotazioni_id, optional_id) VALUES (?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (Optional optional : optionals) {
                stmt.setInt(1, prenotazioneId);
                stmt.setInt(2, optional.getId());
                stmt.addBatch();
            }
            
            int[] results = stmt.executeBatch();
            System.out.println("✅ Salvati " + results.length + " optional per prenotazione " + prenotazioneId);
            return true;
            
        } catch (SQLException e) {
            System.err.println("❌ Errore salvataggio optional: " + e.getMessage());
            throw e; // Rilancia per far fallire la transazione
        }
    }
    
   
    private String generateBookingId(int utenteId) {
      
        java.time.LocalDate today = java.time.LocalDate.now();
        String dateStr = today.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = Integer.toHexString((int)(Math.random() * 65536)).toUpperCase();
        
        return String.format("ER-%s-%d-%s", dateStr, utenteId, randomStr);
    }
}