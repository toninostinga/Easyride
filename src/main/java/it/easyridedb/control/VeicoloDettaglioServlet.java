package it.easyridedb.control;

import it.easyridedb.dao.VeicoloDao;
import it.easyridedb.dao.TerminalDao;
import it.easyridedb.dao.PrenotazioneDao;
import it.easyridedb.dao.impl.VeicoloDAOImpl;
import it.easyridedb.dao.impl.TerminalDAOImpl;
import it.easyridedb.dao.impl.PrenotazioneDAOImpl;
import it.easyridedb.model.Veicolo;
import it.easyridedb.model.Terminal;
import it.easyridedb.model.Prenotazione;
import it.easyridedb.model.Utente;
import it.easyridedb.util.CSRFUtil;
import it.easyridedb.model.Carrello;
import it.easyridedb.model.CarrelloItem;


import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ArrayList;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.RequestDispatcher;

@WebServlet("/veicolo-dettaglio")
public class VeicoloDettaglioServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private VeicoloDao veicoloDAO;
    private TerminalDao terminalDAO;
    private PrenotazioneDao prenotazioneDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            veicoloDAO = new VeicoloDAOImpl();
            terminalDAO = new TerminalDAOImpl();
            prenotazioneDAO = new PrenotazioneDAOImpl();
            System.out.println("✅ VeicoloDettaglioServlet inizializzata correttamente");
        } catch (Exception e) {
            System.err.println("❌ Errore inizializzazione VeicoloDettaglioServlet: " + e.getMessage());
            throw new ServletException("Impossibile inizializzare DAOs", e);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
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
            
            // Recupera dati aggiuntivi
            Terminal terminal = getTerminalVeicolo(veicolo);
            List<Veicolo> veicoliCorrelati = getVeicoliCorrelati(veicolo);
            
            // Calcola statistiche disponibilità
            StatisticheDisponibilita stats = calcolaStatisticheDisponibilita(veicolo);
            
            // Prepara dati per form prenotazione
            preparaDatiPrenotazione(request, veicolo);
            
            // Prepara attributi per JSP
            request.setAttribute("veicolo", veicolo);
            request.setAttribute("terminal", terminal);
            request.setAttribute("veicoliCorrelati", veicoliCorrelati);
            request.setAttribute("statistiche", stats);
            request.setAttribute("csrfToken", CSRFUtil.getToken(request));
            
            // Gestione messaggi
            String successMessage = request.getParameter("success");
            String errorMessage = request.getParameter("error");
            if (successMessage != null) request.setAttribute("successMessage", successMessage);
            if (errorMessage != null) request.setAttribute("errorMessage", errorMessage);
            
            // Forward a JSP
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/veicolo-dettaglio.jsp");
            dispatcher.forward(request, response);
            
        } catch (Exception e) {
            System.err.println("Errore nel dettaglio veicolo: " + e.getMessage());
            e.printStackTrace(); // Aggiunto per debug
            response.sendRedirect("catalogo?error=Errore nel caricamento del veicolo");
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
            String targa = request.getParameter("targa");
            
            switch (action != null ? action : "") {
                case "add-to-cart":
                    aggiungiAlCarrello(request, response);
                    break;
                case "quick-booking":
                    prenotazioneRapida(request, response);
                    break;
                case "check-availability":
                    verificaDisponibilita(request, response);
                    break;
                default:
                    response.sendRedirect("veicolo-dettaglio?targa=" + targa + "&error=Azione non valida");
            }
            
        } catch (Exception e) {
            System.err.println("Errore nel POST dettaglio veicolo: " + e.getMessage());
            String targa = request.getParameter("targa");
            response.sendRedirect("veicolo-dettaglio?targa=" + targa + "&error=Errore nell'operazione");
        }
    }
    
    private Terminal getTerminalVeicolo(Veicolo veicolo) {
        try {
            if (veicolo.getTerminalId() > 0) {
                return terminalDAO.findById(veicolo.getTerminalId());
            }
        } catch (Exception e) {
            System.err.println("Errore recupero terminal: " + e.getMessage());
        }
        return null;
    }
    
    private List<Veicolo> getVeicoliCorrelati(Veicolo veicolo) {
        List<Veicolo> correlati = new ArrayList<>();
        
        try {
            // Prima prova per marca
            List<Veicolo> stessaMarca = veicoloDAO.findByMarca(veicolo.getMarca());
            for (Veicolo v : stessaMarca) {
                if (!v.getTarga().equals(veicolo.getTarga()) && v.isDisponibile()) {
                    correlati.add(v);
                    if (correlati.size() >= 3) break; // Massimo 3
                }
            }
            
            // Se non abbastanza, aggiungi per tipo
            if (correlati.size() < 3 && veicolo.getTipo() != null) {
                VeicoloDAOImpl veicoloImpl = (VeicoloDAOImpl) veicoloDAO;
                List<Veicolo> stessoTipo = veicoloImpl.findByTipo(veicolo.getTipo());
                for (Veicolo v : stessoTipo) {
                    if (!v.getTarga().equals(veicolo.getTarga()) && 
                        !correlati.contains(v) && v.isDisponibile()) {
                        correlati.add(v);
                        if (correlati.size() >= 3) break;
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Errore recupero veicoli correlati: " + e.getMessage());
        }
        
        return correlati;
    }
    
    
    private StatisticheDisponibilita calcolaStatisticheDisponibilita(Veicolo veicolo) {
        StatisticheDisponibilita stats = new StatisticheDisponibilita();
        
        try {
            LocalDate oggi = LocalDate.now();
            int giorniDisponibili = 0;
            int giorniOccupati = 0;
            
            // Controlla i prossimi 30 giorni
            for (int i = 0; i < 30; i++) {
                LocalDate data = oggi.plusDays(i);
                Date sqlDate = Date.valueOf(data);
                
                if (veicoloDAO.isDisponibileInPeriodo(veicolo.getTarga(), sqlDate, sqlDate)) {
                    giorniDisponibili++;
                } else {
                    giorniOccupati++;
                }
            }
            
            stats.giorniDisponibili = giorniDisponibili;
            stats.giorniOccupati = giorniOccupati;
            stats.percentualeDisponibilita = (giorniDisponibili * 100) / 30;
            
            // MODIFICA: Trova il prossimo periodo disponibile e converti LocalDate in Date
            for (int i = 0; i < 90; i++) {
                LocalDate data = oggi.plusDays(i);
                Date sqlDate = Date.valueOf(data);
                
                if (veicoloDAO.isDisponibileInPeriodo(veicolo.getTarga(), sqlDate, sqlDate)) {
                    // Conversione da LocalDate a java.util.Date
                    stats.prossimaDataDisponibile = java.sql.Date.valueOf(data);
                    break;
                }
            }
            
        } catch (Exception e) {
            System.err.println("Errore calcolo statistiche: " + e.getMessage());
            e.printStackTrace(); // Aggiunto per debug
        }
        
        return stats;
    }
    
    
    private void preparaDatiPrenotazione(HttpServletRequest request, Veicolo veicolo) {
        try {
            // Date predefinite
            LocalDate oggi = LocalDate.now();
            LocalDate domani = oggi.plusDays(1);
            LocalDate finePeriodo = domani.plusDays(3);
            
            request.setAttribute("dataRitiroDefault", domani.toString());
            request.setAttribute("dataRestituzioneDefault", finePeriodo.toString());
            
            // Calcola prezzo esempio per 3 giorni
            BigDecimal prezzoEsempio = veicolo.getPrezzoPerGiorno().multiply(BigDecimal.valueOf(3));
            request.setAttribute("prezzoEsempio", prezzoEsempio);
            
            // Lista terminal disponibili
            List<Terminal> terminals = terminalDAO.findAll();
            request.setAttribute("terminals", terminals);
            
        } catch (Exception e) {
            System.err.println("Errore preparazione dati prenotazione: " + e.getMessage());
        }
    }
    
   
    private void aggiungiAlCarrello(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        String targa = request.getParameter("targa");
        String dataRitiroStr = request.getParameter("dataRitiro");
        String dataRestituzioneStr = request.getParameter("dataRestituzione");
        
        try {
            // Validazioni base
            if (dataRitiroStr == null || dataRestituzioneStr == null) {
                response.sendRedirect("veicolo-dettaglio?targa=" + targa + "&error=Date non specificate");
                return;
            }
            
            LocalDate dataRitiro = LocalDate.parse(dataRitiroStr);
            LocalDate dataRestituzione = LocalDate.parse(dataRestituzioneStr);
            
            if (dataRitiro.isBefore(LocalDate.now()) || dataRestituzione.isBefore(dataRitiro)) {
                response.sendRedirect("veicolo-dettaglio?targa=" + targa + "&error=Date non valide");
                return;
            }
            
            // Verifica disponibilità
            Date sqlDataRitiro = Date.valueOf(dataRitiro);
            Date sqlDataRestituzione = Date.valueOf(dataRestituzione);
            
            if (!veicoloDAO.isDisponibileInPeriodo(targa, sqlDataRitiro, sqlDataRestituzione)) {
                response.sendRedirect("veicolo-dettaglio?targa=" + targa + "&error=Veicolo non disponibile nel periodo selezionato");
                return;
            }
            
            // Recupera dati veicolo
            Veicolo veicolo = veicoloDAO.findByTarga(targa);
            if (veicolo == null) {
                response.sendRedirect("veicolo-dettaglio?targa=" + targa + "&error=Veicolo non trovato");
                return;
            }
            
            // *** AGGIUNTA REALE AL CARRELLO ***
            HttpSession session = request.getSession(true);
            
            // Ottieni o crea carrello dalla sessione
            Carrello carrello = (Carrello) session.getAttribute("carrello");
            if (carrello == null) {
                carrello = new Carrello();
                session.setAttribute("carrello", carrello);
                System.out.println("🛒 Creato nuovo carrello per sessione: " + session.getId());
            }
            
            // Crea CarrelloItem
            CarrelloItem item = new CarrelloItem(
                veicolo.getTarga(),
                dataRitiro,
                dataRestituzione,
                veicolo.getTerminalId(), // Terminal ritiro (stesso del veicolo)
                veicolo.getTerminalId()  // Terminal restituzione (stesso del veicolo)
            );
            
            // Imposta dati veicolo
            item.setMarca(veicolo.getMarca());
            item.setModello(veicolo.getModello());
            item.setTipo(veicolo.getTipo());
            item.setCarburante(veicolo.getCarburante());
            item.setTrasmissione(veicolo.getTrasmissione());
            item.setPrezzoPerGiorno(veicolo.getPrezzoPerGiorno());
            item.setImmagineUrl(veicolo.getImmagineUrl());
            
            // Imposta nomi terminal
            Terminal terminal = getTerminalVeicolo(veicolo);
            if (terminal != null) {
                item.setNomeTerminalRitiro(terminal.getNome());
                item.setNomeTerminalRestituzione(terminal.getNome());
            }
            
            // Aggiungi al carrello
            if (carrello.aggiungiItem(item)) {
                session.setAttribute("carrello", carrello);
                System.out.println("✅ Veicolo aggiunto al carrello: " + veicolo.getMarca() + " " + veicolo.getModello());
                System.out.println("📊 Carrello ora contiene: " + carrello.getNumeroItemTotali() + " item(s)");
                
                response.sendRedirect("veicolo-dettaglio?targa=" + targa + "&success=Veicolo aggiunto al carrello");
            } else {
                response.sendRedirect("veicolo-dettaglio?targa=" + targa + "&error=Impossibile aggiungere al carrello. Verifica le date.");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Errore aggiunta carrello: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect("veicolo-dettaglio?targa=" + targa + "&error=Errore nell'aggiunta al carrello");
        }
    }
    
    private void prenotazioneRapida(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        // Verifica autenticazione
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("login?message=Accesso richiesto per prenotare");
            return;
        }
        
        Utente utente = (Utente) session.getAttribute("utente");
        if (utente == null) {
            response.sendRedirect("login?message=Accesso richiesto per prenotare");
            return;
        }
        
        // Reindirizza alla prenotazione normale con parametri
        String targa = request.getParameter("targa");
        String dataRitiro = request.getParameter("dataRitiro");
        String dataRestituzione = request.getParameter("dataRestituzione");
        
        String url = "prenotazione?targa=" + targa + 
                    "&dataRitiro=" + dataRitiro + 
                    "&dataRestituzione=" + dataRestituzione;
        
        response.sendRedirect(url);
    }
    
    private void verificaDisponibilita(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        String targa = request.getParameter("targa");
        String dataRitiroStr = request.getParameter("dataRitiro");
        String dataRestituzioneStr = request.getParameter("dataRestituzione");
        
        try {
            LocalDate dataRitiro = LocalDate.parse(dataRitiroStr);
            LocalDate dataRestituzione = LocalDate.parse(dataRestituzioneStr);
            
            Date sqlDataRitiro = Date.valueOf(dataRitiro);
            Date sqlDataRestituzione = Date.valueOf(dataRestituzione);
            
            boolean disponibile = veicoloDAO.isDisponibileInPeriodo(targa, sqlDataRitiro, sqlDataRestituzione);
            
            // Calcola prezzo totale
            long giorni = ChronoUnit.DAYS.between(dataRitiro, dataRestituzione);
            Veicolo veicolo = veicoloDAO.findByTarga(targa);
            BigDecimal prezzoTotale = veicolo.getPrezzoPerGiorno().multiply(BigDecimal.valueOf(giorni));
            
            // Risposta JSON
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            String json = String.format(
                "{\"disponibile\": %b, \"prezzoTotale\": %.2f, \"giorni\": %d}",
                disponibile, prezzoTotale.doubleValue(), giorni
            );
            
            response.getWriter().write(json);
            
        } catch (Exception e) {
            System.err.println("Errore verifica disponibilità: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
   
    public static class StatisticheDisponibilita {
        public int giorniDisponibili = 0;
        public int giorniOccupati = 0;
        public int percentualeDisponibilita = 0;
        
        // MODIFICA: Cambiato da LocalDate a Date
        public java.util.Date prossimaDataDisponibile = null;
        
        // Getter corretti
        public int getGiorniDisponibili() {
            return giorniDisponibili;
        }
        
        public int getGiorniOccupati() {
            return giorniOccupati;
        }
        
        public int getPercentualeDisponibilita() {
            return percentualeDisponibilita;
        }
        
        // MODIFICA: Getter ora ritorna Date invece di LocalDate
        public java.util.Date getProssimaDataDisponibile() {
            return prossimaDataDisponibile;
        }
        
        public String getDescrizioneDisponibilita() {
            if (percentualeDisponibilita >= 80) {
                return "Ottima disponibilità";
            } else if (percentualeDisponibilita >= 50) {
                return "Buona disponibilità";
            } else if (percentualeDisponibilita >= 20) {
                return "Disponibilità limitata";
            } else {
                return "Scarsa disponibilità";
            }
        }
        
        public String getClasseCSS() {
            if (percentualeDisponibilita >= 80) {
                return "success";
            } else if (percentualeDisponibilita >= 50) {
                return "warning";
            } else {
                return "danger";
            }
        }
    }
}