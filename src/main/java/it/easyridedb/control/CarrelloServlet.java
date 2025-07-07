package it.easyridedb.control;

import it.easyridedb.dao.VeicoloDao;
import it.easyridedb.dao.TerminalDao;
import it.easyridedb.dao.OptionalDao;
import it.easyridedb.dao.impl.VeicoloDAOImpl;
import it.easyridedb.dao.impl.TerminalDAOImpl;
import it.easyridedb.dao.impl.OptionalDAOImpl;
import it.easyridedb.model.Veicolo;
import it.easyridedb.model.Terminal;
import it.easyridedb.model.Utente;
import it.easyridedb.model.Carrello;
import it.easyridedb.model.CarrelloItem;
// Evitiamo import di Optional per evitare conflitto con java.util.Optional
import it.easyridedb.util.CSRFUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.RequestDispatcher;


@WebServlet("/carrello")
public class CarrelloServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private VeicoloDao veicoloDAO;
    private TerminalDao terminalDAO;
    private OptionalDao optionalDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            veicoloDAO = new VeicoloDAOImpl();
            terminalDAO = new TerminalDAOImpl();
            optionalDAO = new OptionalDAOImpl();
            System.out.println("✅ CarrelloServlet inizializzata correttamente");
        } catch (Exception e) {
            System.err.println("❌ Errore inizializzazione CarrelloServlet: " + e.getMessage());
            throw new ServletException("Impossibile inizializzare DAOs", e);
        }
    }
    
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            String action = request.getParameter("action");
            
            if ("aggiungi".equals(action)) {
                // Prepara form per aggiungere al carrello
                prepareAddForm(request, response);
            } else {
                // Visualizza carrello
                visualizzaCarrello(request, response);
            }
            
        } catch (Exception e) {
            System.err.println("Errore nel GET carrello: " + e.getMessage());
            handleError(request, response, "Errore nel caricamento del carrello: " + e.getMessage());
        }
    }
    
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // Validazione CSRF
            if (!CSRFUtil.validateToken(request)) {
                String redirectUrl = CSRFUtil.handleCSRFError(request);
                response.sendRedirect(redirectUrl);
                return;
            }
            
            String action = request.getParameter("action");
            
            switch (action != null ? action : "") {
                case "aggiungi":
                    aggiungiAlCarrello(request, response);
                    break;
                case "rimuovi":
                    rimuoviDalCarrello(request, response);
                    break;
                case "aggiorna":
                    aggiornaCarrello(request, response);
                    break;
                case "svuota":
                    svuotaCarrello(request, response);
                    break;
                case "aggiorna-optional":
                    aggiornaOptional(request, response);
                    break;
                default:
                    response.sendRedirect("carrello?error=Azione non valida");
            }
            
        } catch (Exception e) {
            System.err.println("Errore nel POST carrello: " + e.getMessage());
            handleError(request, response, "Errore nell'operazione: " + e.getMessage());
        }
    }
    
  
    private void aggiungiAlCarrello(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Controlla autenticazione (opzionale per clienti non registrati)
        HttpSession session = request.getSession(true);
        
        // Recupera parametri
        String targaVeicolo = request.getParameter("targaVeicolo");
        String dataRitiroStr = request.getParameter("dataRitiro");
        String dataRestituzioneStr = request.getParameter("dataRestituzione");
        String terminalRitiroIdStr = request.getParameter("terminalRitiroId");
        String terminalRestituzioneIdStr = request.getParameter("terminalRestituzioneId");
        String[] optionalIds = request.getParameterValues("optionalIds");
        
        // Validazione parametri
        if (isInvalidBookingData(targaVeicolo, dataRitiroStr, dataRestituzioneStr, 
                                terminalRitiroIdStr, terminalRestituzioneIdStr)) {
            response.sendRedirect("catalogo?error=Dati prenotazione non validi");
            return;
        }
        
        try {
            // Parsing date
            LocalDate dataRitiro = LocalDate.parse(dataRitiroStr);
            LocalDate dataRestituzione = LocalDate.parse(dataRestituzioneStr);
            int terminalRitiroId = Integer.parseInt(terminalRitiroIdStr);
            int terminalRestituzioneId = Integer.parseInt(terminalRestituzioneIdStr);
            
            // Validazione business
            if (dataRitiro.isAfter(dataRestituzione) || dataRitiro.isBefore(LocalDate.now())) {
                response.sendRedirect("catalogo?error=Date non valide");
                return;
            }
            
            // Recupera dati veicolo dal database
            Veicolo veicolo = veicoloDAO.findByTarga(targaVeicolo);
            if (veicolo == null || !veicolo.isDisponibile()) {
                response.sendRedirect("catalogo?error=Veicolo non disponibile");
                return;
            }
            
            // Crea item carrello
            CarrelloItem item = creaCarrelloItem(veicolo, dataRitiro, dataRestituzione, 
                                               terminalRitiroId, terminalRestituzioneId);
            
            // Aggiungi optional se selezionati
            if (optionalIds != null) {
                aggiungiOptionalAllItem(item, optionalIds);
            }
            
            // Ottieni o crea carrello
            Carrello carrello = getOrCreateCarrello(session);
            
            // Aggiungi al carrello
            if (carrello.aggiungiItem(item)) {
                session.setAttribute("carrello", carrello);
                response.sendRedirect("carrello?success=Veicolo aggiunto al carrello");
            } else {
                response.sendRedirect("catalogo?error=Impossibile aggiungere al carrello. Controlla le date.");
            }
            
        } catch (Exception e) {
            System.err.println("Errore aggiunta carrello: " + e.getMessage());
            response.sendRedirect("catalogo?error=Errore nell'aggiunta al carrello");
        }
    }
    
    /**
     * Rimuove un item dal carrello
     */
    private void rimuoviDalCarrello(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String itemId = request.getParameter("itemId");
        if (itemId == null || itemId.trim().isEmpty()) {
            response.sendRedirect("carrello?error=ID item non valido");
            return;
        }
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            Carrello carrello = (Carrello) session.getAttribute("carrello");
            if (carrello != null && carrello.rimuoviItem(itemId)) {
                session.setAttribute("carrello", carrello);
                response.sendRedirect("carrello?success=Prenotazione rimossa dal carrello");
            } else {
                response.sendRedirect("carrello?error=Impossibile rimuovere la prenotazione");
            }
        } else {
            response.sendRedirect("carrello?error=Sessione non valida");
        }
    }
     
    private void svuotaCarrello(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            Carrello carrello = (Carrello) session.getAttribute("carrello");
            if (carrello != null) {
                carrello.svuotaCarrello();
                session.setAttribute("carrello", carrello);
            }
            response.sendRedirect("carrello?success=Carrello svuotato");
        } else {
            response.sendRedirect("carrello");
        }
    }
    
    private void aggiornaOptional(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String itemId = request.getParameter("itemId");
        String[] optionalIds = request.getParameterValues("optionalIds");
        
        if (itemId == null) {
            response.sendRedirect("carrello?error=ID item non valido");
            return;
        }
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            Carrello carrello = (Carrello) session.getAttribute("carrello");
            if (carrello != null) {
                CarrelloItem item = carrello.trovaItemPerId(itemId);
                if (item != null) {
                    // Reset optional e aggiungi nuovi
                    item.getOptionalSelezionati().clear();
                    if (optionalIds != null) {
                        aggiungiOptionalAllItem(item, optionalIds);
                    }
                    carrello.ricalcolaTotali();
                    session.setAttribute("carrello", carrello);
                    response.sendRedirect("carrello?success=Optional aggiornati");
                } else {
                    response.sendRedirect("carrello?error=Prenotazione non trovata");
                }
            } else {
                response.sendRedirect("carrello?error=Carrello non trovato");
            }
        } else {
            response.sendRedirect("carrello");
        }
    }
    
    private void aggiornaCarrello(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Per ora redirect al carrello
        response.sendRedirect("carrello");
    }
    
   
   
    private void visualizzaCarrello(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(true);
        Carrello carrello = getOrCreateCarrello(session);
        
        // Prepara attributi per JSP
        request.setAttribute("carrello", carrello);
        request.setAttribute("csrfToken", CSRFUtil.getToken(request));
        
        // Recupera tutti gli optional disponibili per modifica
        try {
            List<it.easyridedb.model.Optional> tuttiOptional = optionalDAO.findAll();
            request.setAttribute("tuttiOptional", tuttiOptional);
        } catch (Exception e) {
            System.err.println("Errore recupero optional: " + e.getMessage());
        }
        
        // Gestione messaggi
        String successMessage = request.getParameter("success");
        String errorMessage = request.getParameter("error");
        if (successMessage != null) request.setAttribute("successMessage", successMessage);
        if (errorMessage != null) request.setAttribute("errorMessage", errorMessage);
        
        // Forward a JSP
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/carrello.jsp");
        dispatcher.forward(request, response);
    }
    
    private void prepareAddForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String targaVeicolo = request.getParameter("targa");
        if (targaVeicolo == null) {
            response.sendRedirect("catalogo?error=Veicolo non specificato");
            return;
        }
        
        try {
            // Recupera dati veicolo
            Veicolo veicolo = veicoloDAO.findByTarga(targaVeicolo);
            if (veicolo == null) {
                response.sendRedirect("catalogo?error=Veicolo non trovato");
                return;
            }
            
            // Recupera terminal e optional
            List<Terminal> terminals = terminalDAO.findAll();
            List<it.easyridedb.model.Optional> optionals = optionalDAO.findAll();
            
            // Prepara attributi
            request.setAttribute("veicolo", veicolo);
            request.setAttribute("terminals", terminals);
            request.setAttribute("optionals", optionals);
            request.setAttribute("csrfToken", CSRFUtil.getToken(request));
            
            // Forward al form
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/aggiungi-carrello.jsp");
            dispatcher.forward(request, response);
            
        } catch (Exception e) {
            System.err.println("Errore preparazione form: " + e.getMessage());
            response.sendRedirect("catalogo?error=Errore nel caricamento dei dati");
        }
    }
    
   
    private Carrello getOrCreateCarrello(HttpSession session) {
        Carrello carrello = (Carrello) session.getAttribute("carrello");
        if (carrello == null) {
            carrello = new Carrello();
            session.setAttribute("carrello", carrello);
        }
        return carrello;
    }
    
    private CarrelloItem creaCarrelloItem(Veicolo veicolo, LocalDate dataRitiro, LocalDate dataRestituzione,
                                        int terminalRitiroId, int terminalRestituzioneId) throws Exception {
        
        CarrelloItem item = new CarrelloItem(veicolo.getTarga(), dataRitiro, dataRestituzione, 
                                           terminalRitiroId, terminalRestituzioneId);
        
        // Imposta dati veicolo
        item.setMarca(veicolo.getMarca());
        item.setModello(veicolo.getModello());
        item.setTipo(veicolo.getTipo());
        item.setCarburante(veicolo.getCarburante());
        item.setTrasmissione(veicolo.getTrasmissione());
        item.setPrezzoPerGiorno(veicolo.getPrezzoPerGiorno());
        item.setImmagineUrl(veicolo.getImmagineUrl());
        
        // Recupera e imposta nomi terminal
        Terminal terminalRitiro = terminalDAO.findById(terminalRitiroId);
        Terminal terminalRestituzione = terminalDAO.findById(terminalRestituzioneId);
        
        if (terminalRitiro != null) item.setNomeTerminalRitiro(terminalRitiro.getNome());
        if (terminalRestituzione != null) item.setNomeTerminalRestituzione(terminalRestituzione.getNome());
        
        return item;
    }
    
    private void aggiungiOptionalAllItem(CarrelloItem item, String[] optionalIds) {
        if (optionalIds == null) return;
        
        try {
            for (String optionalIdStr : optionalIds) {
                int optionalId = Integer.parseInt(optionalIdStr);
                it.easyridedb.model.Optional optional = optionalDAO.findById(optionalId);
                if (optional != null) {
                    item.aggiungiOptional(optional);
                }
            }
        } catch (Exception e) {
            System.err.println("Errore aggiunta optional: " + e.getMessage());
        }
    }
    
    private boolean isInvalidBookingData(String targa, String dataRitiro, String dataRestituzione,
                                       String terminalRitiro, String terminalRestituzione) {
        return targa == null || targa.trim().isEmpty() ||
               dataRitiro == null || dataRitiro.trim().isEmpty() ||
               dataRestituzione == null || dataRestituzione.trim().isEmpty() ||
               terminalRitiro == null || terminalRitiro.trim().isEmpty() ||
               terminalRestituzione == null || terminalRestituzione.trim().isEmpty();
    }
    
    private void handleError(HttpServletRequest request, HttpServletResponse response, String errorMessage) 
            throws ServletException, IOException {
        
        request.setAttribute("errorMessage", errorMessage);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/error.jsp");
        dispatcher.forward(request, response);
    }
    
  
    public static int getNumeroItemCarrello(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Carrello carrello = (Carrello) session.getAttribute("carrello");
            return carrello != null ? carrello.getNumeroItemTotali() : 0;
        }
        return 0;
    }
    
    public static BigDecimal getTotaleCarrello(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Carrello carrello = (Carrello) session.getAttribute("carrello");
            return carrello != null ? carrello.getTotaleCarrello() : BigDecimal.ZERO;
        }
        return BigDecimal.ZERO;
    }
}