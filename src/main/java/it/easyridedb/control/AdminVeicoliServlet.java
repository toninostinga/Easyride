package it.easyridedb.control;

import it.easyridedb.dao.VeicoloDao;
import it.easyridedb.dao.TerminalDao;
import it.easyridedb.dao.impl.VeicoloDAOImpl;
import it.easyridedb.dao.impl.TerminalDAOImpl;
import it.easyridedb.model.Veicolo;
import it.easyridedb.model.Terminal;
import it.easyridedb.model.Utente;
import it.easyridedb.util.CSRFUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.RequestDispatcher;


@WebServlet("/admin-veicoli")
public class AdminVeicoliServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private VeicoloDao veicoloDAO;
    private TerminalDao terminalDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            veicoloDAO = new VeicoloDAOImpl();
            terminalDAO = new TerminalDAOImpl();
            System.out.println("✅ AdminVeicoliServlet inizializzata correttamente");
        } catch (Exception e) {
            System.err.println("❌ Errore inizializzazione AdminVeicoliServlet: " + e.getMessage());
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
            
            String action = request.getParameter("action");
            
            switch (action != null ? action : "list") {
                case "list":
                    visualizzaListaVeicoli(request, response);
                    break;
                case "add":
                    mostraFormAggiungi(request, response);
                    break;
                case "edit":
                    mostraFormModifica(request, response);
                    break;
                case "view":
                    visualizzaDettagliVeicolo(request, response);
                    break;
                case "search":
                    ricercaVeicoli(request, response);
                    break;
                default:
                    visualizzaListaVeicoli(request, response);
            }
            
        } catch (Exception e) {
            System.err.println("Errore nel GET admin veicoli: " + e.getMessage());
            handleError(request, response, "Errore nel caricamento: " + e.getMessage());
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
                case "create":
                    creaVeicolo(request, response);
                    break;
                case "update":
                    aggiornaVeicolo(request, response);
                    break;
                case "delete":
                    eliminaVeicolo(request, response);
                    break;
                case "toggle-disponibilita":
                    toggleDisponibilita(request, response);
                    break;
                case "bulk-delete":
                    eliminazioneMultipla(request, response);
                    break;
                default:
                    response.sendRedirect("admin-veicoli?error=Azione non valida");
            }
            
        } catch (Exception e) {
            System.err.println("Errore nel POST admin veicoli: " + e.getMessage());
            handleError(request, response, "Errore nell'operazione: " + e.getMessage());
        }
    }
    
    private void visualizzaListaVeicoli(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // Parametri paginazione
            int page = getIntParameter(request, "page", 1);
            int pageSize = getIntParameter(request, "pageSize", 10);
            String sortBy = request.getParameter("sortBy");
            String sortOrder = request.getParameter("sortOrder");
            String filterMarca = request.getParameter("filterMarca");
            String filterCarburante = request.getParameter("filterCarburante");
            String filterDisponibile = request.getParameter("filterDisponibile");
            
            // Recupera veicoli con filtri
            List<Veicolo> veicoli = getVeicoliWithFilters(filterMarca, filterCarburante, filterDisponibile);
            
            // Recupera dati di supporto
            List<Terminal> terminals = terminalDAO.findAll();
            
            // Marche e carburanti distinti - simulazione (implementare later se necessario)
            List<String> marche = List.of("Toyota", "Volkswagen", "BMW", "Audi", "Mercedes"); // Placeholder
            List<String> carburanti = List.of("benzina", "diesel", "elettrico", "ibrido"); // Placeholder
            
            // Prepara attributi per JSP
            request.setAttribute("veicoli", veicoli);
            request.setAttribute("terminals", terminals);
            request.setAttribute("marche", marche);
            request.setAttribute("carburanti", carburanti);
            request.setAttribute("currentPage", page);
            request.setAttribute("pageSize", pageSize);
            request.setAttribute("totalVeicoli", veicoli.size());
            request.setAttribute("csrfToken", CSRFUtil.getToken(request));
            
            // Mantieni filtri
            request.setAttribute("filterMarca", filterMarca);
            request.setAttribute("filterCarburante", filterCarburante);
            request.setAttribute("filterDisponibile", filterDisponibile);
            
            // Gestione messaggi
            String successMessage = request.getParameter("success");
            String errorMessage = request.getParameter("error");
            if (successMessage != null) request.setAttribute("successMessage", successMessage);
            if (errorMessage != null) request.setAttribute("errorMessage", errorMessage);
            
            // Forward a JSP
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/admin-veicoli-list.jsp");
            dispatcher.forward(request, response);
            
        } catch (Exception e) {
            System.err.println("Errore lista veicoli: " + e.getMessage());
            handleError(request, response, "Errore nel caricamento della lista veicoli");
        }
    }
    
    private void mostraFormAggiungi(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // Recupera dati di supporto
            List<Terminal> terminals = terminalDAO.findAll();
            
            // Prepara attributi per JSP
            request.setAttribute("terminals", terminals);
            request.setAttribute("csrfToken", CSRFUtil.getToken(request));
            request.setAttribute("action", "create");
            request.setAttribute("pageTitle", "Aggiungi Nuovo Veicolo");
            
            // Forward a form JSP
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/admin-veicoli-form.jsp");
            dispatcher.forward(request, response);
            
        } catch (Exception e) {
            System.err.println("Errore form aggiungi: " + e.getMessage());
            response.sendRedirect("admin-veicoli?error=Errore nel caricamento del form");
        }
    }
    
    private void mostraFormModifica(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String targa = request.getParameter("targa");
        if (targa == null || targa.trim().isEmpty()) {
            response.sendRedirect("admin-veicoli?error=Targa veicolo non specificata");
            return;
        }
        
        try {
            // Recupera veicolo
            Veicolo veicolo = veicoloDAO.findByTarga(targa);
            if (veicolo == null) {
                response.sendRedirect("admin-veicoli?error=Veicolo non trovato");
                return;
            }
            
            // Recupera dati di supporto
            List<Terminal> terminals = terminalDAO.findAll();
            
            // Prepara attributi per JSP
            request.setAttribute("veicolo", veicolo);
            request.setAttribute("terminals", terminals);
            request.setAttribute("csrfToken", CSRFUtil.getToken(request));
            request.setAttribute("action", "update");
            request.setAttribute("pageTitle", "Modifica Veicolo: " + targa);
            
            // Forward a form JSP
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/admin-veicoli-form.jsp");
            dispatcher.forward(request, response);
            
        } catch (Exception e) {
            System.err.println("Errore form modifica: " + e.getMessage());
            response.sendRedirect("admin-veicoli?error=Errore nel caricamento del veicolo");
        }
    }
    
    private void visualizzaDettagliVeicolo(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String targa = request.getParameter("targa");
        if (targa == null || targa.trim().isEmpty()) {
            response.sendRedirect("admin-veicoli?error=Targa veicolo non specificata");
            return;
        }
        
        try {
            // Recupera veicolo e dati correlati
            Veicolo veicolo = veicoloDAO.findByTarga(targa);
            if (veicolo == null) {
                response.sendRedirect("admin-veicoli?error=Veicolo non trovato");
                return;
            }
            
            // Recupera terminal associato - terminalId è int primitivo (mai null)
            Terminal terminal = null;
            if (veicolo.getTerminalId() > 0) {
                terminal = terminalDAO.findById(veicolo.getTerminalId());
            }
            
            // Recupera storico prenotazioni (ultime 10)
            // List<Prenotazione> storicoPrenotazioni = prenotazioneDAO.findByVeicolo(targa, 10);
            
            // Prepara attributi per JSP
            request.setAttribute("veicolo", veicolo);
            request.setAttribute("terminal", terminal);
            // request.setAttribute("storicoPrenotazioni", storicoPrenotazioni);
            request.setAttribute("csrfToken", CSRFUtil.getToken(request));
            
            // Forward a JSP dettagli
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/admin-veicoli-view.jsp");
            dispatcher.forward(request, response);
            
        } catch (Exception e) {
            System.err.println("Errore dettagli veicolo: " + e.getMessage());
            response.sendRedirect("admin-veicoli?error=Errore nel caricamento dei dettagli");
        }
    }
    
    private void ricercaVeicoli(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Reindirizza alla lista con parametri di ricerca
        visualizzaListaVeicoli(request, response);
    }
    
    private void creaVeicolo(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        try {
            // Recupera e valida parametri
            Veicolo veicolo = buildVeicoloFromRequest(request);
            if (veicolo == null) {
                response.sendRedirect("admin-veicoli?action=add&error=Dati non validi");
                return;
            }
            
            // Verifica che la targa non esista già
            if (veicoloDAO.findByTarga(veicolo.getTarga()) != null) {
                response.sendRedirect("admin-veicoli?action=add&error=Targa già esistente");
                return;
            }
            
            // Inserisci nel database
            boolean success = veicoloDAO.insert(veicolo);
            
            if (success) {
                System.out.println("✅ Veicolo creato: " + veicolo.getTarga());
                response.sendRedirect("admin-veicoli?success=Veicolo " + veicolo.getTarga() + " creato con successo");
            } else {
                response.sendRedirect("admin-veicoli?action=add&error=Errore nella creazione del veicolo");
            }
            
        } catch (Exception e) {
            System.err.println("Errore creazione veicolo: " + e.getMessage());
            response.sendRedirect("admin-veicoli?action=add&error=Errore nell'inserimento: " + e.getMessage());
        }
    }
 
    private void aggiornaVeicolo(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        String targaOriginale = request.getParameter("targaOriginale");
        if (targaOriginale == null || targaOriginale.trim().isEmpty()) {
            response.sendRedirect("admin-veicoli?error=Targa originale non specificata");
            return;
        }
        
        try {
            // Recupera e valida parametri
            Veicolo veicolo = buildVeicoloFromRequest(request);
            if (veicolo == null) {
                response.sendRedirect("admin-veicoli?action=edit&targa=" + targaOriginale + "&error=Dati non validi");
                return;
            }
            
            // Se la targa è cambiata, verifica che la nuova non esista
            if (!targaOriginale.equals(veicolo.getTarga())) {
                if (veicoloDAO.findByTarga(veicolo.getTarga()) != null) {
                    response.sendRedirect("admin-veicoli?action=edit&targa=" + targaOriginale + "&error=Nuova targa già esistente");
                    return;
                }
            }
            
            // Aggiorna nel database
            boolean success = veicoloDAO.update(veicolo);
            
            if (success) {
                System.out.println("✅ Veicolo aggiornato: " + veicolo.getTarga());
                response.sendRedirect("admin-veicoli?success=Veicolo " + veicolo.getTarga() + " aggiornato con successo");
            } else {
                response.sendRedirect("admin-veicoli?action=edit&targa=" + targaOriginale + "&error=Errore nell'aggiornamento del veicolo");
            }
            
        } catch (Exception e) {
            System.err.println("Errore aggiornamento veicolo: " + e.getMessage());
            response.sendRedirect("admin-veicoli?action=edit&targa=" + targaOriginale + "&error=Errore nell'aggiornamento: " + e.getMessage());
        }
    }
    
    private void eliminaVeicolo(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        String targa = request.getParameter("targa");
        if (targa == null || targa.trim().isEmpty()) {
            response.sendRedirect("admin-veicoli?error=Targa veicolo non specificata");
            return;
        }
        
        try {
            // Verifica che il veicolo esista
            Veicolo veicolo = veicoloDAO.findByTarga(targa);
            if (veicolo == null) {
                response.sendRedirect("admin-veicoli?error=Veicolo non trovato");
                return;
            }
            
            
            
            // Elimina dal database
            boolean success = veicoloDAO.delete(targa);
            
            if (success) {
                System.out.println("🗑️ Veicolo eliminato: " + targa);
                response.sendRedirect("admin-veicoli?success=Veicolo " + targa + " eliminato con successo");
            } else {
                response.sendRedirect("admin-veicoli?error=Errore nell'eliminazione del veicolo");
            }
            
        } catch (Exception e) {
            System.err.println("Errore eliminazione veicolo: " + e.getMessage());
            response.sendRedirect("admin-veicoli?error=Errore nell'eliminazione: " + e.getMessage());
        }
    }
    
    private void toggleDisponibilita(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        String targa = request.getParameter("targa");
        if (targa == null || targa.trim().isEmpty()) {
            response.sendRedirect("admin-veicoli?error=Targa veicolo non specificata");
            return;
        }
        
        try {
            // Recupera veicolo attuale
            Veicolo veicolo = veicoloDAO.findByTarga(targa);
            if (veicolo == null) {
                response.sendRedirect("admin-veicoli?error=Veicolo non trovato");
                return;
            }
            
            // Toggle disponibilità - usa isDisponibile() (standard Java per boolean)
            boolean nuovaDisponibilita = !veicolo.isDisponibile();
            boolean success = veicoloDAO.updateDisponibilita(targa, nuovaDisponibilita);
            
            if (success) {
                String stato = nuovaDisponibilita ? "disponibile" : "non disponibile";
                response.sendRedirect("admin-veicoli?success=Veicolo " + targa + " ora è " + stato);
            } else {
                response.sendRedirect("admin-veicoli?error=Errore nell'aggiornamento della disponibilità");
            }
            
        } catch (Exception e) {
            System.err.println("Errore toggle disponibilità: " + e.getMessage());
            response.sendRedirect("admin-veicoli?error=Errore nell'operazione");
        }
    }
    
    private void eliminazioneMultipla(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        String[] targhe = request.getParameterValues("selectedVeicoli");
        if (targhe == null || targhe.length == 0) {
            response.sendRedirect("admin-veicoli?error=Nessun veicolo selezionato");
            return;
        }
        
        try {
            int eliminati = 0;
            for (String targa : targhe) {
                if (veicoloDAO.delete(targa)) {
                    eliminati++;
                }
            }
            
            response.sendRedirect("admin-veicoli?success=" + eliminati + " veicoli eliminati su " + targhe.length + " selezionati");
            
        } catch (Exception e) {
            System.err.println("Errore eliminazione multipla: " + e.getMessage());
            response.sendRedirect("admin-veicoli?error=Errore nell'eliminazione multipla");
        }
    }
    
    private Veicolo buildVeicoloFromRequest(HttpServletRequest request) {
        try {
            String targa = request.getParameter("targa");
            String marca = request.getParameter("marca");
            String modello = request.getParameter("modello");
            String tipo = request.getParameter("tipo");
            String trasmissione = request.getParameter("trasmissione");
            String carburante = request.getParameter("carburante");
            String prezzoStr = request.getParameter("prezzoPerGiorno");
            String disponibileStr = request.getParameter("disponibile");
            String immagineUrl = request.getParameter("immagineUrl");
            String terminalIdStr = request.getParameter("terminalId");
            
            // Validazione campi obbligatori
            if (isNullOrEmpty(targa) || isNullOrEmpty(marca) || isNullOrEmpty(modello) || 
                isNullOrEmpty(trasmissione) || isNullOrEmpty(carburante) || isNullOrEmpty(prezzoStr)) {
                return null;
            }
            
            // Parsing e validazione
            BigDecimal prezzo = new BigDecimal(prezzoStr);
            if (prezzo.compareTo(BigDecimal.ZERO) <= 0) {
                return null;
            }
            
            boolean disponibile = "true".equals(disponibileStr) || "on".equals(disponibileStr);
            int terminalId = isNullOrEmpty(terminalIdStr) ? 0 : Integer.parseInt(terminalIdStr); // int primitivo, default 0
            
            // Crea oggetto Veicolo - usa setters appropriati
            Veicolo veicolo = new Veicolo();
            veicolo.setTarga(targa.trim().toUpperCase());
            veicolo.setMarca(marca.trim());
            veicolo.setModello(modello.trim());
            veicolo.setTipo(tipo != null ? tipo.trim() : null);
            veicolo.setTrasmissione(trasmissione);
            veicolo.setCarburante(carburante);
            veicolo.setPrezzoPerGiorno(prezzo);
            veicolo.setDisponibile(disponibile); // Standard setter per boolean
            veicolo.setImmagineUrl(immagineUrl != null ? immagineUrl.trim() : null);
            veicolo.setTerminalId(terminalId); // int primitivo
            
            return veicolo;
            
        } catch (Exception e) {
            System.err.println("Errore build veicolo: " + e.getMessage());
            return null;
        }
    }
    
    private List<Veicolo> getVeicoliWithFilters(String marca, String carburante, String disponibile) {
        try {
            if (isNullOrEmpty(marca) && isNullOrEmpty(carburante) && isNullOrEmpty(disponibile)) {
                return veicoloDAO.findAll();
            }
            
            // Applica filtri (implementa logica di filtro nei DAO se necessario)
            List<Veicolo> veicoli = veicoloDAO.findAll();
            
            // Filtro marca
            if (!isNullOrEmpty(marca)) {
                veicoli = veicoli.stream()
                    .filter(v -> marca.equalsIgnoreCase(v.getMarca()))
                    .toList();
            }
            
            // Filtro carburante
            if (!isNullOrEmpty(carburante)) {
                veicoli = veicoli.stream()
                    .filter(v -> carburante.equalsIgnoreCase(v.getCarburante()))
                    .toList();
            }
            
            // Filtro disponibilità - usa isDisponibile() (standard Java per boolean)
            if (!isNullOrEmpty(disponibile)) {
                boolean filtroDisp = "true".equals(disponibile);
                veicoli = veicoli.stream()
                    .filter(v -> v.isDisponibile() == filtroDisp)
                    .toList();
            }
            
            return veicoli;
            
        } catch (Exception e) {
            System.err.println("Errore filtri veicoli: " + e.getMessage());
            return veicoloDAO.findAll();
        }
    }
    
    private int getIntParameter(HttpServletRequest request, String name, int defaultValue) {
        try {
            String value = request.getParameter(name);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
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
}