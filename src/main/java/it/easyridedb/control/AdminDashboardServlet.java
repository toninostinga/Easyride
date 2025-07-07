package it.easyridedb.control;

import it.easyridedb.dao.VeicoloDao;
import it.easyridedb.dao.UtenteDao;
import it.easyridedb.dao.PrenotazioneDao;
import it.easyridedb.dao.TerminalDao;
import it.easyridedb.dao.impl.VeicoloDAOImpl;
import it.easyridedb.dao.impl.UtenteDAOImpl;
import it.easyridedb.dao.impl.PrenotazioneDAOImpl;
import it.easyridedb.dao.impl.TerminalDAOImpl;
import it.easyridedb.model.Utente;
import it.easyridedb.model.Prenotazione;
import it.easyridedb.model.Veicolo;
import it.easyridedb.model.Terminal;
import it.easyridedb.util.CSRFUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.RequestDispatcher;

@WebServlet("/admin-dashboard")
public class AdminDashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private VeicoloDao veicoloDAO;
    private UtenteDao utenteDAO;
    private PrenotazioneDao prenotazioneDAO;
    private TerminalDao terminalDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            veicoloDAO = new VeicoloDAOImpl();
            utenteDAO = new UtenteDAOImpl();
            prenotazioneDAO = new PrenotazioneDAOImpl();
            terminalDAO = new TerminalDAOImpl();
            System.out.println("✅ AdminDashboardServlet inizializzata correttamente");
        } catch (Exception e) {
            System.err.println("❌ Errore inizializzazione AdminDashboardServlet: " + e.getMessage());
            throw new ServletException("Impossibile inizializzare DAOs", e);
        }
    }
    
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // Controllo autenticazione admin
            if (!isAdminAuthenticated(request, response)) {
                return;
            }
            
            // Recupera statistiche dashboard
            Map<String, Object> statistiche = getStatisticheDashboard();
            
            // Prepara attributi per JSP
            request.setAttribute("statistiche", statistiche);
            request.setAttribute("csrfToken", CSRFUtil.getToken(request));
            request.setAttribute("dataOggi", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            
            // Gestione messaggi
            String successMessage = request.getParameter("success");
            String errorMessage = request.getParameter("error");
            if (successMessage != null) request.setAttribute("successMessage", successMessage);
            if (errorMessage != null) request.setAttribute("errorMessage", errorMessage);
            
            // Forward a JSP
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/admin-dashboard.jsp");
            dispatcher.forward(request, response);
            
        } catch (Exception e) {
            System.err.println("Errore nel dashboard admin: " + e.getMessage());
            handleError(request, response, "Errore nel caricamento della dashboard: " + e.getMessage());
        }
    }
    
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // Controllo autenticazione admin
            if (!isAdminAuthenticated(request, response)) {
                return;
            }
            
            // Validazione CSRF
            if (!CSRFUtil.validateToken(request)) {
                String redirectUrl = CSRFUtil.handleCSRFError(request);
                response.sendRedirect(redirectUrl);
                return;
            }
            
            String action = request.getParameter("action");
            
            switch (action != null ? action : "") {
                case "toggle-veicolo":
                    toggleDisponibilitaVeicolo(request, response);
                    break;
                case "backup-database":
                    // Placeholder per backup
                    response.sendRedirect("admin-dashboard?success=Backup in corso...");
                    break;
                default:
                    response.sendRedirect("admin-dashboard?error=Azione non valida");
            }
            
        } catch (Exception e) {
            System.err.println("Errore nel POST dashboard admin: " + e.getMessage());
            handleError(request, response, "Errore nell'operazione: " + e.getMessage());
        }
    }
    
    private Map<String, Object> getStatisticheDashboard() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // ===== STATISTICHE VEICOLI =====
            int totaleVeicoli = veicoloDAO.countAll();
            int veicoliDisponibili = veicoloDAO.countDisponibili();
            int veicoliInUso = totaleVeicoli - veicoliDisponibili;
            
            stats.put("totaleVeicoli", totaleVeicoli);
            stats.put("veicoliDisponibili", veicoliDisponibili);
            stats.put("veicoliInUso", veicoliInUso);
            stats.put("percentualeUtilizzo", 
                     totaleVeicoli > 0 ? (veicoliInUso * 100 / totaleVeicoli) : 0);
            
            // Veicoli per marca
            Map<String, Integer> veicoliPerMarca = calcolaVeicoliPerMarca();
            stats.put("veicoliPerMarca", veicoliPerMarca);
            
            // ===== STATISTICHE UTENTI =====
            List<Utente> clienti = utenteDAO.findByRuolo("cliente");
            List<Utente> admin = utenteDAO.findByRuolo("admin");
            int totaleUtenti = clienti.size() + admin.size();
            
            stats.put("totaleUtenti", totaleUtenti);
            stats.put("totaleClienti", clienti.size());
            stats.put("totaleAdmin", admin.size());
            
            // ===== STATISTICHE PRENOTAZIONI =====
            int prenotazioniConfermate = prenotazioneDAO.countByStato("confermata");
            int prenotazioniInCorso = prenotazioneDAO.countByStato("in_corso");
            int prenotazioniCompletate = prenotazioneDAO.countByStato("completata");
            int totalePrenotazioni = prenotazioneDAO.countAll();
            
            stats.put("prenotazioniOggi", prenotazioniConfermate);
            stats.put("prenotazioniMese", totalePrenotazioni);
            stats.put("prenotazioniConfermate", prenotazioniConfermate);
            stats.put("prenotazioniInCorso", prenotazioniInCorso);
            stats.put("prenotazioniCompletate", prenotazioniCompletate);
            
            // Fatturato
            BigDecimal fatturatoTotale = calcolaFatturatoTotale();
            stats.put("fatturatoMese", fatturatoTotale);
            
            // Prenotazioni recenti
            List<Prenotazione> prenotazioniRecenti = getPrenotazioniRecenti(5);
            stats.put("prenotazioniRecenti", prenotazioniRecenti);
            
            // ===== STATISTICHE TERMINAL =====
            List<Terminal> tuttiTerminal = terminalDAO.findAll();
            stats.put("totaleTerminal", tuttiTerminal.size());
            
        } catch (Exception e) {
            System.err.println("Errore recupero statistiche: " + e.getMessage());
            e.printStackTrace();
            
            // Valori default in caso di errore
            setDefaultStats(stats);
        }
        
        return stats;
    }
    
   
    private Map<String, Integer> calcolaVeicoliPerMarca() {
        Map<String, Integer> veicoliPerMarca = new HashMap<>();
        
        try {
            List<Veicolo> tuttiVeicoli = veicoloDAO.findAll();
            for (Veicolo v : tuttiVeicoli) {
                String marca = v.getMarca();
                if (marca != null) {
                    veicoliPerMarca.put(marca, veicoliPerMarca.getOrDefault(marca, 0) + 1);
                }
            }
        } catch (Exception e) {
            System.err.println("Errore calcolo veicoli per marca: " + e.getMessage());
        }
        
        return veicoliPerMarca;
    }
    
    private BigDecimal calcolaFatturatoTotale() {
        BigDecimal fatturatoTotale = BigDecimal.ZERO;
        
        try {
            List<Prenotazione> tuttePrenotazioni = prenotazioneDAO.findAll();
            for (Prenotazione p : tuttePrenotazioni) {
                if (p.getPrezzoTotale() != null && 
                    ("completata".equals(p.getStato()) || "in_corso".equals(p.getStato()))) {
                    fatturatoTotale = fatturatoTotale.add(p.getPrezzoTotale());
                }
            }
        } catch (Exception e) {
            System.err.println("Errore calcolo fatturato: " + e.getMessage());
        }
        
        return fatturatoTotale;
    }
    
   
    private List<Prenotazione> getPrenotazioniRecenti(int limite) {
        List<Prenotazione> prenotazioniRecenti = new ArrayList<>();
        
        try {
            List<Prenotazione> tuttePrenotazioni = prenotazioneDAO.findAll();
            int max = Math.min(limite, tuttePrenotazioni.size());
            
            for (int i = 0; i < max; i++) {
                prenotazioniRecenti.add(tuttePrenotazioni.get(i));
            }
        } catch (Exception e) {
            System.err.println("Errore recupero prenotazioni recenti: " + e.getMessage());
        }
        
        return prenotazioniRecenti;
    }
    
    
    private void setDefaultStats(Map<String, Object> stats) {
        stats.put("totaleVeicoli", 0);
        stats.put("veicoliDisponibili", 0);
        stats.put("veicoliInUso", 0);
        stats.put("percentualeUtilizzo", 0);
        stats.put("totaleUtenti", 0);
        stats.put("totaleClienti", 0);
        stats.put("totaleAdmin", 0);
        stats.put("prenotazioniOggi", 0);
        stats.put("prenotazioniMese", 0);
        stats.put("prenotazioniConfermate", 0);
        stats.put("prenotazioniInCorso", 0);
        stats.put("fatturatoMese", BigDecimal.ZERO);
        stats.put("totaleTerminal", 0);
        stats.put("veicoliPerMarca", new HashMap<String, Integer>());
        stats.put("prenotazioniRecenti", new ArrayList<Prenotazione>());
    }
    
   
    private void toggleDisponibilitaVeicolo(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        String targa = request.getParameter("targa");
        if (targa == null || targa.trim().isEmpty()) {
            response.sendRedirect("admin-dashboard?error=Targa veicolo non specificata");
            return;
        }
        
        try {
            // Recupera veicolo attuale
            Veicolo veicolo = veicoloDAO.findByTarga(targa);
            if (veicolo == null) {
                response.sendRedirect("admin-dashboard?error=Veicolo non trovato");
                return;
            }
            
            // Toggle disponibilità
            boolean nuovaDisponibilita = !veicolo.isDisponibile();
            
            // Usa il metodo specifico dell'interfaccia
            boolean success = veicoloDAO.updateDisponibilita(targa, nuovaDisponibilita);
            
            if (success) {
                String stato = nuovaDisponibilita ? "disponibile" : "non disponibile";
                response.sendRedirect("admin-dashboard?success=Veicolo " + targa + " ora è " + stato);
            } else {
                response.sendRedirect("admin-dashboard?error=Errore nell'aggiornamento del veicolo");
            }
            
        } catch (Exception e) {
            System.err.println("Errore toggle veicolo: " + e.getMessage());
            response.sendRedirect("admin-dashboard?error=Errore nell'operazione");
        }
    }
    
    // ===== CONTROLLI ACCESSO =====
    
   
    private boolean isAdminAuthenticated(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("login?message=Accesso richiesto");
            return false;
        }
        
        Utente utente = (Utente) session.getAttribute("utente");
        if (utente == null) {
            response.sendRedirect("login?message=Accesso richiesto");
            return false;
        }
        
        if (!"admin".equalsIgnoreCase(utente.getRuolo())) {
            response.sendRedirect("catalogo?error=Accesso negato - Solo amministratori");
            return false;
        }
        
        return true;
    }
    
   
    private void handleError(HttpServletRequest request, HttpServletResponse response, String errorMessage) 
            throws ServletException, IOException {
        
        request.setAttribute("errorMessage", errorMessage);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/error.jsp");
        dispatcher.forward(request, response);
    }
    
    public static boolean isCurrentUserAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Utente utente = (Utente) session.getAttribute("utente");
            return utente != null && "admin".equalsIgnoreCase(utente.getRuolo());
        }
        return false;
    }
    
    public static String getAdminName(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Utente utente = (Utente) session.getAttribute("utente");
            if (utente != null && "admin".equalsIgnoreCase(utente.getRuolo())) {
                return utente.getNome() + " " + utente.getCognome();
            }
        }
        return "Amministratore";
    }
}