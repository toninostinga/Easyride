package it.easyridedb.control;

import it.easyridedb.dao.PrenotazioneDao;
import it.easyridedb.dao.UtenteDao;
import it.easyridedb.dao.VeicoloDao;
import it.easyridedb.dao.impl.PrenotazioneDAOImpl;
import it.easyridedb.dao.impl.UtenteDAOImpl;
import it.easyridedb.dao.impl.VeicoloDAOImpl;
import it.easyridedb.model.Prenotazione;
import it.easyridedb.model.Utente;
import it.easyridedb.model.Veicolo;
import it.easyridedb.util.CSRFUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.RequestDispatcher;


@WebServlet("/admin-ordini")
public class AdminOrdiniServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private PrenotazioneDao prenotazioneDAO;
    private UtenteDao utenteDAO;
    private VeicoloDao veicoloDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            prenotazioneDAO = new PrenotazioneDAOImpl();
            utenteDAO = new UtenteDAOImpl();
            veicoloDAO = new VeicoloDAOImpl();
            System.out.println("✅ AdminOrdiniServlet inizializzata correttamente");
        } catch (Exception e) {
            System.err.println("❌ Errore inizializzazione AdminOrdiniServlet: " + e.getMessage());
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
                    visualizzaListaOrdini(request, response);
                    break;
                case "view":
                    visualizzaDettagliOrdine(request, response);
                    break;
                case "export":
                    esportaOrdini(request, response);
                    break;
                case "stats":
                    visualizzaStatistiche(request, response);
                    break;
                default:
                    visualizzaListaOrdini(request, response);
            }
            
        } catch (Exception e) {
            System.err.println("Errore nel GET admin ordini: " + e.getMessage());
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
                case "update-stato":
                    aggiornaStatoOrdine(request, response);
                    break;
                case "cancel":
                    annullaOrdine(request, response);
                    break;
                case "bulk-export":
                    esportazioneMultipla(request, response);
                    break;
                default:
                    response.sendRedirect("admin-ordini?error=Azione non valida");
            }
            
        } catch (Exception e) {
            System.err.println("Errore nel POST admin ordini: " + e.getMessage());
            handleError(request, response, "Errore nell'operazione: " + e.getMessage());
        }
    }
    
    private void visualizzaListaOrdini(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // Recupera parametri filtro
            String dataInizioStr = request.getParameter("dataInizio");
            String dataFineStr = request.getParameter("dataFine");
            String clienteIdStr = request.getParameter("clienteId");
            String stato = request.getParameter("stato");
            String ordinamento = request.getParameter("sort");
            
            // Parsing date con valori default
            LocalDate dataInizio = parseDate(dataInizioStr, LocalDate.now().minusMonths(1));
            LocalDate dataFine = parseDate(dataFineStr, LocalDate.now());
            Integer clienteId = parseInteger(clienteIdStr, null);
            
            // Recupera ordini con filtri
            List<Prenotazione> ordini = getOrdiniWithFilters(dataInizio, dataFine, clienteId, stato);
            
            // Applica ordinamento
            ordini = applySorting(ordini, ordinamento);
            
            // Calcola statistiche
            Map<String, Object> statistiche = calcolaStatisticheOrdini(ordini);
            
            // Recupera lista clienti per dropdown
            List<Utente> clienti = utenteDAO.findByRuolo("cliente");
            
            // Prepara attributi per JSP
            request.setAttribute("ordini", ordini);
            request.setAttribute("statistiche", statistiche);
            request.setAttribute("clienti", clienti);
            request.setAttribute("csrfToken", CSRFUtil.getToken(request));
            
            // Mantieni filtri
            request.setAttribute("dataInizio", dataInizio.format(DateTimeFormatter.ISO_LOCAL_DATE));
            request.setAttribute("dataFine", dataFine.format(DateTimeFormatter.ISO_LOCAL_DATE));
            request.setAttribute("clienteId", clienteId);
            request.setAttribute("stato", stato);
            request.setAttribute("ordinamento", ordinamento);
            
            // Gestione messaggi
            String successMessage = request.getParameter("success");
            String errorMessage = request.getParameter("error");
            if (successMessage != null) request.setAttribute("successMessage", successMessage);
            if (errorMessage != null) request.setAttribute("errorMessage", errorMessage);
            
            // Forward a JSP
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/admin-ordini-list.jsp");
            dispatcher.forward(request, response);
            
        } catch (Exception e) {
            System.err.println("Errore lista ordini: " + e.getMessage());
            handleError(request, response, "Errore nel caricamento della lista ordini");
        }
    }
    
    private void visualizzaDettagliOrdine(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String ordineIdStr = request.getParameter("id");
        if (ordineIdStr == null || ordineIdStr.trim().isEmpty()) {
            response.sendRedirect("admin-ordini?error=ID ordine non specificato");
            return;
        }
        
        try {
            int ordineId = Integer.parseInt(ordineIdStr);
            
            // Recupera ordine
            Prenotazione ordine = prenotazioneDAO.findById(ordineId);
            if (ordine == null) {
                response.sendRedirect("admin-ordini?error=Ordine non trovato");
                return;
            }
            
            // Recupera dati correlati
            Utente cliente = utenteDAO.findById(ordine.getUtenteId());
            Veicolo veicolo = veicoloDAO.findByTarga(ordine.getVeicoloTarga());
            
            // Prepara attributi per JSP
            request.setAttribute("ordine", ordine);
            request.setAttribute("cliente", cliente);
            request.setAttribute("veicolo", veicolo);
            request.setAttribute("csrfToken", CSRFUtil.getToken(request));
            
            // Forward a JSP dettagli
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/admin-ordini-view.jsp");
            dispatcher.forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendRedirect("admin-ordini?error=ID ordine non valido");
        } catch (Exception e) {
            System.err.println("Errore dettagli ordine: " + e.getMessage());
            response.sendRedirect("admin-ordini?error=Errore nel caricamento dei dettagli");
        }
    }
    
    private void visualizzaStatistiche(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // Periodo per statistiche (default: ultimo anno)
            LocalDate dataInizio = LocalDate.now().minusYears(1);
            LocalDate dataFine = LocalDate.now();
            
            // Calcola statistiche dettagliate
            Map<String, Object> statistiche = calcolaStatisticheAvanzate(dataInizio, dataFine);
            
            // Prepara attributi per JSP
            request.setAttribute("statistiche", statistiche);
            request.setAttribute("dataInizio", dataInizio);
            request.setAttribute("dataFine", dataFine);
            request.setAttribute("csrfToken", CSRFUtil.getToken(request));
            
            // Forward a JSP statistiche
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/admin-ordini-stats.jsp");
            dispatcher.forward(request, response);
            
        } catch (Exception e) {
            System.err.println("Errore statistiche: " + e.getMessage());
            handleError(request, response, "Errore nel caricamento delle statistiche");
        }
    }
    
    private void esportaOrdini(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        try {
            // Recupera gli stessi filtri della lista
            String dataInizioStr = request.getParameter("dataInizio");
            String dataFineStr = request.getParameter("dataFine");
            String clienteIdStr = request.getParameter("clienteId");
            String stato = request.getParameter("stato");
            
            LocalDate dataInizio = parseDate(dataInizioStr, LocalDate.now().minusMonths(1));
            LocalDate dataFine = parseDate(dataFineStr, LocalDate.now());
            Integer clienteId = parseInteger(clienteIdStr, null);
            
            List<Prenotazione> ordini = getOrdiniWithFilters(dataInizio, dataFine, clienteId, stato);
            
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=\"ordini_" + 
                             LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv\"");
            
           
            StringBuilder csv = new StringBuilder();
            csv.append("ID,Cliente,Email,Veicolo,Data Ritiro,Data Restituzione,Prezzo Totale,Stato,Data Prenotazione\n");
            
            for (Prenotazione ordine : ordini) {
                try {
                    Utente cliente_ord = utenteDAO.findById(ordine.getUtenteId());
                    csv.append(ordine.getId()).append(",")
                       .append(cliente_ord != null ? "\"" + cliente_ord.getNome() + " " + cliente_ord.getCognome() + "\"" : "N/A").append(",")
                       .append(cliente_ord != null ? cliente_ord.getEmail() : "N/A").append(",")
                       .append("\"").append(ordine.getVeicoloTarga()).append("\",")
                       .append(ordine.getDataRitiro()).append(",")
                       .append(ordine.getDataRestituzione()).append(",")
                       .append(ordine.getPrezzoTotale() != null ? ordine.getPrezzoTotale() : "0").append(",")
                       .append(ordine.getStato()).append(",") // Ora getStato() restituisce String
                       .append(ordine.getDataPrenotazione())
                       .append("\n");
                } catch (Exception e) {
                    System.err.println("Errore export ordine " + ordine.getId() + ": " + e.getMessage());
                }
            }
            
            response.getWriter().write(csv.toString());
            response.getWriter().flush();
            
        } catch (Exception e) {
            System.err.println("Errore esportazione: " + e.getMessage());
            response.sendRedirect("admin-ordini?error=Errore nell'esportazione");
        }
    }
    
   
    private void aggiornaStatoOrdine(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        String ordineIdStr = request.getParameter("ordineId");
        String nuovoStato = request.getParameter("nuovoStato");
        
        if (ordineIdStr == null || nuovoStato == null) {
            response.sendRedirect("admin-ordini?error=Parametri mancanti");
            return;
        }
        
        try {
            int ordineId = Integer.parseInt(ordineIdStr);
            
            // Valida nuovo stato
            if (!isValidStato(nuovoStato)) {
                response.sendRedirect("admin-ordini?error=Stato non valido");
                return;
            }
            
            // Aggiorna nel database
            boolean success = prenotazioneDAO.updateStato(ordineId, nuovoStato);
            
            if (success) {
                System.out.println("✅ Stato ordine " + ordineId + " aggiornato a: " + nuovoStato);
                response.sendRedirect("admin-ordini?success=Stato ordine aggiornato a: " + nuovoStato);
            } else {
                response.sendRedirect("admin-ordini?error=Errore nell'aggiornamento dello stato");
            }
            
        } catch (NumberFormatException e) {
            response.sendRedirect("admin-ordini?error=ID ordine non valido");
        } catch (Exception e) {
            System.err.println("Errore aggiornamento stato: " + e.getMessage());
            response.sendRedirect("admin-ordini?error=Errore nell'operazione");
        }
    }
    
    
    private void annullaOrdine(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        String ordineIdStr = request.getParameter("ordineId");
        if (ordineIdStr == null) {
            response.sendRedirect("admin-ordini?error=ID ordine non specificato");
            return;
        }
        
        try {
            int ordineId = Integer.parseInt(ordineIdStr);
            
            // Aggiorna stato a "annullata"
            boolean success = prenotazioneDAO.updateStato(ordineId, "annullata");
            
            if (success) {
                System.out.println("❌ Ordine " + ordineId + " annullato dall'admin");
                response.sendRedirect("admin-ordini?success=Ordine annullato con successo");
            } else {
                response.sendRedirect("admin-ordini?error=Errore nell'annullamento dell'ordine");
            }
            
        } catch (NumberFormatException e) {
            response.sendRedirect("admin-ordini?error=ID ordine non valido");
        } catch (Exception e) {
            System.err.println("Errore annullamento ordine: " + e.getMessage());
            response.sendRedirect("admin-ordini?error=Errore nell'operazione");
        }
    }
    
    
    private void esportazioneMultipla(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        String[] ordineIds = request.getParameterValues("selectedOrdini");
        if (ordineIds == null || ordineIds.length == 0) {
            response.sendRedirect("admin-ordini?error=Nessun ordine selezionato");
            return;
        }
        
        // Placeholder per implementazione futura
        response.sendRedirect("admin-ordini?success=Esportazione multipla in fase di implementazione");
    }
    
    
    private List<Prenotazione> getOrdiniWithFilters(LocalDate dataInizio, LocalDate dataFine, 
                                                  Integer clienteId, String stato) {
        try {
            // Converti LocalDate in sql.Date
            Date sqlDataInizio = Date.valueOf(dataInizio);
            Date sqlDataFine = Date.valueOf(dataFine);
            
            List<Prenotazione> ordini = prenotazioneDAO.findByDataRitiro(sqlDataInizio, sqlDataFine);
            
            // Filtro cliente
            if (clienteId != null) {
                ordini = ordini.stream()
                    .filter(o -> o.getUtenteId() == clienteId)
                    .collect(Collectors.toList());
            }
            
            // Filtro stato - ora getStato() restituisce direttamente String
            if (stato != null && !stato.trim().isEmpty() && !"tutti".equals(stato)) {
                ordini = ordini.stream()
                    .filter(o -> stato.equalsIgnoreCase(o.getStato()))
                    .collect(Collectors.toList());
            }
            
            return ordini;
            
        } catch (Exception e) {
            System.err.println("Errore filtri ordini: " + e.getMessage());
            return prenotazioneDAO.findAll();
        }
    }
    
    private List<Prenotazione> applySorting(List<Prenotazione> ordini, String ordinamento) {
        if (ordinamento == null) {
            return ordini;
        }
        
        switch (ordinamento) {
            case "data_asc":
                return ordini.stream()
                    .sorted((o1, o2) -> o1.getDataPrenotazione().compareTo(o2.getDataPrenotazione()))
                    .collect(Collectors.toList());
            case "data_desc":
                return ordini.stream()
                    .sorted((o1, o2) -> o2.getDataPrenotazione().compareTo(o1.getDataPrenotazione()))
                    .collect(Collectors.toList());
            case "prezzo_asc":
                return ordini.stream()
                    .sorted((o1, o2) -> {
                        BigDecimal p1 = o1.getPrezzoTotale() != null ? o1.getPrezzoTotale() : BigDecimal.ZERO;
                        BigDecimal p2 = o2.getPrezzoTotale() != null ? o2.getPrezzoTotale() : BigDecimal.ZERO;
                        return p1.compareTo(p2);
                    })
                    .collect(Collectors.toList());
            case "prezzo_desc":
                return ordini.stream()
                    .sorted((o1, o2) -> {
                        BigDecimal p1 = o1.getPrezzoTotale() != null ? o1.getPrezzoTotale() : BigDecimal.ZERO;
                        BigDecimal p2 = o2.getPrezzoTotale() != null ? o2.getPrezzoTotale() : BigDecimal.ZERO;
                        return p2.compareTo(p1);
                    })
                    .collect(Collectors.toList());
            default:
                return ordini;
        }
    }
    
    private Map<String, Object> calcolaStatisticheOrdini(List<Prenotazione> ordini) {
        Map<String, Object> stats = new HashMap<>();
        
        int totaleOrdini = ordini.size();
        BigDecimal fatturatoTotale = ordini.stream()
            .map(o -> o.getPrezzoTotale() != null ? o.getPrezzoTotale() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal fatturatoMedio = totaleOrdini > 0 ? 
            fatturatoTotale.divide(new BigDecimal(totaleOrdini), 2, RoundingMode.HALF_UP) : 
            BigDecimal.ZERO;
        
        // Conteggio per stato - ora getStato() restituisce String
        Map<String, Long> conteggioStati = ordini.stream()
            .collect(Collectors.groupingBy(Prenotazione::getStato, Collectors.counting()));
        
        stats.put("totaleOrdini", totaleOrdini);
        stats.put("fatturatoTotale", fatturatoTotale);
        stats.put("fatturatoMedio", fatturatoMedio);
        stats.put("conteggioStati", conteggioStati);
        
        return stats;
    }
    
    private Map<String, Object> calcolaStatisticheAvanzate(LocalDate dataInizio, LocalDate dataFine) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Statistiche generali - usa tutti gli ordini per ora
            List<Prenotazione> ordini = prenotazioneDAO.findAll();
            stats.putAll(calcolaStatisticheOrdini(ordini));
            
            // Veicoli più richiesti
            Map<String, Long> veicoliRichiesti = ordini.stream()
                .collect(Collectors.groupingBy(Prenotazione::getVeicoloTarga, Collectors.counting()));
            stats.put("veicoliRichiesti", veicoliRichiesti);
            
            // Clienti più attivi  
            Map<Integer, Long> clientiAttivi = ordini.stream()
                .collect(Collectors.groupingBy(Prenotazione::getUtenteId, Collectors.counting()));
            stats.put("clientiAttivi", clientiAttivi);
            
            // Placeholder per fatturato per mese
            stats.put("fatturatoPerMese", new HashMap<String, BigDecimal>());
            
        } catch (Exception e) {
            System.err.println("Errore statistiche avanzate: " + e.getMessage());
        }
        
        return stats;
    }
    
    private LocalDate parseDate(String dateStr, LocalDate defaultValue) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            return defaultValue;
        }
    }
  
    private Integer parseInteger(String intStr, Integer defaultValue) {
        if (intStr == null || intStr.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(intStr);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    
    private boolean isValidStato(String stato) {
        return stato != null && 
               (stato.equals("confermata") || stato.equals("in_corso") || 
                stato.equals("completata") || stato.equals("annullata"));
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