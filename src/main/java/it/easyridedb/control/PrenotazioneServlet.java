package it.easyridedb.control;

import it.easyridedb.dao.impl.VeicoloDAOImpl;
import it.easyridedb.dao.impl.PrenotazioneDAOImpl;
import it.easyridedb.model.Veicolo;
import it.easyridedb.model.Prenotazione;
import it.easyridedb.model.Utente;
import it.easyridedb.util.CSRFUtil;
import it.easyridedb.control.LoginServlet;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.RequestDispatcher;

@WebServlet("/prenota")
public class PrenotazioneServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private VeicoloDAOImpl veicoloDAO;
    private PrenotazioneDAOImpl prenotazioneDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            veicoloDAO = new VeicoloDAOImpl();
            prenotazioneDAO = new PrenotazioneDAOImpl();
            System.out.println("✅ PrenotazioneServlet inizializzata");
        } catch (Exception e) {
            System.err.println("❌ Errore inizializzazione PrenotazioneServlet: " + e.getMessage());
            throw new ServletException("Impossibile inizializzare DAO", e);
        }
    }
    
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // Verifica autenticazione
            if (!LoginServlet.isUserLoggedIn(request)) {
                response.sendRedirect("login?message=Devi effettuare il login per prenotare");
                return;
            }
            
            Utente utente = LoginServlet.getCurrentUser(request);
            String targa = request.getParameter("targa");
            
            if (targa == null || targa.trim().isEmpty()) {
                response.sendRedirect("catalogo?error=Veicolo non specificato");
                return;
            }
            
            // Recupera veicolo
            Veicolo veicolo = veicoloDAO.findByTarga(targa);
            if (veicolo == null) {
                response.sendRedirect("catalogo?error=Veicolo non trovato");
                return;
            }
            
            if (!veicolo.isDisponibile()) {
                response.sendRedirect("catalogo?error=Veicolo non disponibile");
                return;
            }
            
            // Prepara attributi per JSP
            request.setAttribute("veicolo", veicolo);
            request.setAttribute("utente", utente);
            request.setAttribute("csrfToken", CSRFUtil.getToken(request));
            
            // Date di default (oggi + 1, oggi + 2)
            LocalDate domani = LocalDate.now().plusDays(1);
            LocalDate dopodomani = LocalDate.now().plusDays(2);
            
            request.setAttribute("dataRitiroDefault", domani.toString());
            request.setAttribute("dataRestituzioneDefault", dopodomani.toString());
            
            // Messaggi
            String successMessage = request.getParameter("success");
            String errorMessage = request.getParameter("error");
            
            if (successMessage != null) {
                request.setAttribute("successMessage", successMessage);
            }
            if (errorMessage != null) {
                request.setAttribute("errorMessage", errorMessage);
            }
            
            // Inoltra al JSP
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/prenotazione.jsp");
            dispatcher.forward(request, response);
            
        } catch (Exception e) {
            System.err.println("❌ Errore GET prenotazione: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect("catalogo?error=Errore nel caricamento form prenotazione");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // Verifica CSRF
            if (!CSRFUtil.validateToken(request)) {
                String redirectUrl = CSRFUtil.handleCSRFError(request);
                response.sendRedirect(redirectUrl);
                return;
            }
            
            // Verifica autenticazione
            if (!LoginServlet.isUserLoggedIn(request)) {
                response.sendRedirect("login?message=Sessione scaduta. Effettua di nuovo il login");
                return;
            }
            
            Utente utente = LoginServlet.getCurrentUser(request);
            
            // Recupera parametri
            String targa = request.getParameter("targa");
            String dataRitiroStr = request.getParameter("dataRitiro");
            String dataRestituzioneStr = request.getParameter("dataRestituzione");
            String note = request.getParameter("note");
            
            // Validazione input
            String validationError = validatePrenotazioneInput(targa, dataRitiroStr, dataRestituzioneStr);
            if (validationError != null) {
                response.sendRedirect("prenota?targa=" + targa + "&error=" + validationError);
                return;
            }
            
            // Converti date
            LocalDate dataRitiro = LocalDate.parse(dataRitiroStr);
            LocalDate dataRestituzione = LocalDate.parse(dataRestituzioneStr);
            
            // Validazione business logic
            String businessError = validatePrenotazioneBusiness(targa, dataRitiro, dataRestituzione);
            if (businessError != null) {
                response.sendRedirect("prenota?targa=" + targa + "&error=" + businessError);
                return;
            }
            
            // Crea prenotazione
            Prenotazione prenotazione = createPrenotazione(utente.getId(), targa, dataRitiro, dataRestituzione, note);
            
            if (prenotazioneDAO.insert(prenotazione)) {
                System.out.println("✅ Prenotazione creata: ID " + prenotazione.getId() + 
                                 " per utente " + utente.getEmail());
                
                // Redirect a conferma
                response.sendRedirect("prenotazione-conferma?id=" + prenotazione.getId());
            } else {
                response.sendRedirect("prenota?targa=" + targa + "&error=Errore nel salvataggio prenotazione");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Errore POST prenotazione: " + e.getMessage());
            e.printStackTrace();
            
            String targa = request.getParameter("targa");
            response.sendRedirect("prenota?targa=" + targa + "&error=Errore interno del sistema");
        }
    }
    
    private String validatePrenotazioneInput(String targa, String dataRitiroStr, String dataRestituzioneStr) {
        if (targa == null || targa.trim().isEmpty()) {
            return "Veicolo non specificato";
        }
        
        if (dataRitiroStr == null || dataRitiroStr.trim().isEmpty()) {
            return "Data di ritiro obbligatoria";
        }
        
        if (dataRestituzioneStr == null || dataRestituzioneStr.trim().isEmpty()) {
            return "Data di restituzione obbligatoria";
        }
        
        try {
            LocalDate dataRitiro = LocalDate.parse(dataRitiroStr);
            LocalDate dataRestituzione = LocalDate.parse(dataRestituzioneStr);
            LocalDate oggi = LocalDate.now();
            
            if (dataRitiro.isBefore(oggi)) {
                return "La data di ritiro non può essere nel passato";
            }
            
            if (dataRestituzione.isBefore(dataRitiro)) {
                return "La data di restituzione deve essere successiva al ritiro";
            }
            
            if (dataRestituzione.equals(dataRitiro)) {
                return "Il noleggio deve durare almeno 1 giorno";
            }
            
            long giorni = ChronoUnit.DAYS.between(dataRitiro, dataRestituzione);
            if (giorni > 30) {
                return "Il noleggio non può superare 30 giorni";
            }
            
        } catch (Exception e) {
            return "Formato date non valido";
        }
        
        return null; // Tutto OK
    }
    
    private String validatePrenotazioneBusiness(String targa, LocalDate dataRitiro, LocalDate dataRestituzione) {
        // Verifica che il veicolo esista e sia disponibile
        Veicolo veicolo = veicoloDAO.findByTarga(targa);
        if (veicolo == null) {
            return "Veicolo non trovato";
        }
        
        if (!veicolo.isDisponibile()) {
            return "Veicolo non disponibile";
        }
        
        // Verifica conflitti con altre prenotazioni
        Date sqlDataRitiro = Date.valueOf(dataRitiro);
        Date sqlDataRestituzione = Date.valueOf(dataRestituzione);
        
        if (prenotazioneDAO.hasConflittiPrenotazione(targa, sqlDataRitiro, sqlDataRestituzione)) {
            return "Il veicolo è già prenotato in questo periodo. Scegli date diverse";
        }
        
        return null; // Tutto OK
    }
    
    private Prenotazione createPrenotazione(int utenteId, String targa, LocalDate dataRitiro, 
                                          LocalDate dataRestituzione, String note) {
        
        Veicolo veicolo = veicoloDAO.findByTarga(targa);
        long giorni = ChronoUnit.DAYS.between(dataRitiro, dataRestituzione);
        BigDecimal prezzoTotale = veicolo.getPrezzoPerGiorno().multiply(BigDecimal.valueOf(giorni));
        
        Prenotazione prenotazione = new Prenotazione();
        prenotazione.setUtenteId(utenteId);
        prenotazione.setVeicoloTarga(targa);
        prenotazione.setDataRitiro(Date.valueOf(dataRitiro));
        prenotazione.setDataRestituzione(Date.valueOf(dataRestituzione));
        prenotazione.setPrezzoTotale(prezzoTotale);
        prenotazione.setStato("confermata");
        
        // Terminal di default (TODO: permettere scelta)
        prenotazione.setTerminalRitiroId(1);
        prenotazione.setTerminalRestituzioneId(1);
        
        
        return prenotazione;
    }
}