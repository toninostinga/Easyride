package it.easyridedb.control;

import it.easyridedb.dao.impl.VeicoloDAOImpl;
import it.easyridedb.model.Veicolo;
import it.easyridedb.model.Utente;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.RequestDispatcher;

@WebServlet("/catalogo")
public class CatalogoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private VeicoloDAOImpl veicoloDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            veicoloDAO = new VeicoloDAOImpl();
            System.out.println("✅ CatalogoServlet MVC inizializzata correttamente");
        } catch (Exception e) {
            System.err.println("❌ Errore inizializzazione CatalogoServlet: " + e.getMessage());
            throw new ServletException("Impossibile inizializzare DAO", e);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 1. RECUPERA PARAMETRI FILTRO
            String marca = request.getParameter("marca");
            String modello = request.getParameter("modello");
            String tipo = request.getParameter("tipo");
            String carburante = request.getParameter("carburante");
            String trasmissione = request.getParameter("trasmissione");
            String prezzoMinStr = request.getParameter("prezzoMin");
            String prezzoMaxStr = request.getParameter("prezzoMax");
            String soloDisponibili = request.getParameter("disponibili");
            String ordinamento = request.getParameter("sort");
            
            // Log parametri per debug
            System.out.println("📋 CatalogoServlet - Parametri ricevuti:");
            if (isNotEmpty(marca)) System.out.println("   - Marca: " + marca);
            if (isNotEmpty(modello)) System.out.println("   - Modello: " + modello);
            if (isNotEmpty(tipo)) System.out.println("   - Tipo: " + tipo);
            if (isNotEmpty(carburante)) System.out.println("   - Carburante: " + carburante);
            if (isNotEmpty(trasmissione)) System.out.println("   - Trasmissione: " + trasmissione);
            if (isNotEmpty(prezzoMinStr)) System.out.println("   - Prezzo Min: €" + prezzoMinStr);
            if (isNotEmpty(prezzoMaxStr)) System.out.println("   - Prezzo Max: €" + prezzoMaxStr);
            if ("true".equals(soloDisponibili)) System.out.println("   - Solo disponibili: true");
            if (isNotEmpty(ordinamento)) System.out.println("   - Ordinamento: " + ordinamento);
            
            // 2. APPLICA FILTRI E OTTIENI DATI
            List<Veicolo> veicoli = getVeicoliFiltrati(marca, modello, tipo, carburante, trasmissione,
                                                     prezzoMinStr, prezzoMaxStr, soloDisponibili, ordinamento);
            
            // 3. RECUPERA DATI PER I FILTRI
            List<String> marche = veicoloDAO.findAllMarche();
            List<String> tipi = veicoloDAO.findAllTipi();
            
            // 4. STATISTICHE
            int totaleVeicoli = veicoloDAO.countAll();
            int veicoliDisponibili = veicoloDAO.countDisponibili();
            int risultatiFiltrati = veicoli.size();
            
            System.out.println("📊 Risultati: " + risultatiFiltrati + "/" + totaleVeicoli + " veicoli");
            
            // 5. PREPARA ATTRIBUTI PER JSP
            request.setAttribute("veicoli", veicoli);
            request.setAttribute("marche", marche);
            request.setAttribute("tipi", tipi);
            request.setAttribute("totaleVeicoli", totaleVeicoli);
            request.setAttribute("veicoliDisponibili", veicoliDisponibili);
            request.setAttribute("risultatiFiltrati", risultatiFiltrati);
            
            // Parametri di ricerca per mantenere stato form
            request.setAttribute("selectedMarca", marca);
            request.setAttribute("selectedModello", modello);
            request.setAttribute("selectedTipo", tipo);
            request.setAttribute("selectedCarburante", carburante);
            request.setAttribute("selectedTrasmissione", trasmissione);
            request.setAttribute("selectedPrezzoMin", prezzoMinStr);
            request.setAttribute("selectedPrezzoMax", prezzoMaxStr);
            request.setAttribute("selectedDisponibili", soloDisponibili);
            request.setAttribute("selectedSort", ordinamento);
            
            // Messaggi
            String welcomeMessage = request.getParameter("welcome");
            String successMessage = request.getParameter("success");
            String errorMessage = request.getParameter("error");
            
            if (welcomeMessage != null) {
                request.setAttribute("welcomeMessage", "Benvenuto in EasyRide! Esplora i nostri veicoli.");
            }
            if (successMessage != null) {
                request.setAttribute("successMessage", successMessage);
            }
            if (errorMessage != null) {
                request.setAttribute("errorMessage", errorMessage);
            }
            
            // 6. INOLTRA A JSP (PATH CORRETTO PER WEB-INF/views/)
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/catalogo.jsp");
            dispatcher.forward(request, response);
            
        } catch (Exception e) {
            System.err.println("❌ Errore nel catalogo: " + e.getMessage());
            e.printStackTrace();
            
            // In caso di errore, mostra tutti i veicoli
            try {
                List<Veicolo> tuttiVeicoli = veicoloDAO.findAll();
                request.setAttribute("veicoli", tuttiVeicoli);
                request.setAttribute("errorMessage", "Errore nel caricamento filtri. Mostrando tutti i veicoli.");
                RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/catalogo.jsp");
                dispatcher.forward(request, response);
            } catch (Exception e2) {
                throw new ServletException("Errore grave nel database", e2);
            }
        }
    }
    
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        try {
            if ("addToCart".equals(action)) {
                handleAddToCart(request, response);
            } else if ("viewDetails".equals(action)) {
                handleViewDetails(request, response);
            } else {
                response.sendRedirect("catalogo");
            }
        } catch (Exception e) {
            System.err.println("❌ Errore nell'azione POST: " + e.getMessage());
            response.sendRedirect("catalogo?error=Errore nell'operazione richiesta");
        }
    }
    
   
    private List<Veicolo> getVeicoliFiltrati(String marca, String modello, String tipo, String carburante,
                                           String trasmissione, String prezzoMinStr, String prezzoMaxStr, 
                                           String soloDisponibili, String ordinamento) {
        
        List<Veicolo> veicoli;
        
        // Applica filtro principale per performance
        if ("true".equals(soloDisponibili)) {
            veicoli = veicoloDAO.findByDisponibile();
            System.out.println("🔍 Filtro disponibili: " + veicoli.size() + " veicoli");
        } else if (isNotEmpty(marca)) {
            veicoli = veicoloDAO.findByMarca(marca);
            System.out.println("🔍 Filtro marca '" + marca + "': " + veicoli.size() + " veicoli");
        } else if (isNotEmpty(tipo)) {
            veicoli = veicoloDAO.findByTipo(tipo);
            System.out.println("🔍 Filtro tipo '" + tipo + "': " + veicoli.size() + " veicoli");
        } else if (isNotEmpty(carburante)) {
            veicoli = veicoloDAO.findByCarburante(carburante);
            System.out.println("🔍 Filtro carburante '" + carburante + "': " + veicoli.size() + " veicoli");
        } else if (isNotEmpty(trasmissione)) {
            veicoli = veicoloDAO.findByTrasmissione(trasmissione);
            System.out.println("🔍 Filtro trasmissione '" + trasmissione + "': " + veicoli.size() + " veicoli");
        } else {
            veicoli = veicoloDAO.findAll();
            System.out.println("🔍 Tutti i veicoli: " + veicoli.size());
        }
        
        // Applica filtri aggiuntivi
        veicoli = applyAdditionalFilters(veicoli, marca, modello, tipo, carburante, trasmissione,
                                       prezzoMinStr, prezzoMaxStr, soloDisponibili);
        
        // Applica ordinamento
        veicoli = applySorting(veicoli, ordinamento);
        
        return veicoli;
    }
    
    private List<Veicolo> applyAdditionalFilters(List<Veicolo> veicoli, String marca, String modello, 
                                               String tipo, String carburante, String trasmissione,
                                               String prezzoMinStr, String prezzoMaxStr, String soloDisponibili) {
        
        return veicoli.stream()
            .filter(v -> marca == null || marca.isEmpty() || v.getMarca().equalsIgnoreCase(marca))
            .filter(v -> modello == null || modello.isEmpty() || v.getModello().toLowerCase().contains(modello.toLowerCase()))
            .filter(v -> tipo == null || tipo.isEmpty() || v.getTipo().equalsIgnoreCase(tipo))
            .filter(v -> carburante == null || carburante.isEmpty() || v.getCarburante().equalsIgnoreCase(carburante))
            .filter(v -> trasmissione == null || trasmissione.isEmpty() || v.getTrasmissione().equalsIgnoreCase(trasmissione))
            .filter(v -> {
                if (prezzoMinStr == null || prezzoMinStr.isEmpty()) return true;
                try {
                    BigDecimal prezzoMin = new BigDecimal(prezzoMinStr);
                    return v.getPrezzoPerGiorno().compareTo(prezzoMin) >= 0;
                } catch (NumberFormatException e) {
                    return true;
                }
            })
            .filter(v -> {
                if (prezzoMaxStr == null || prezzoMaxStr.isEmpty()) return true;
                try {
                    BigDecimal prezzoMax = new BigDecimal(prezzoMaxStr);
                    return v.getPrezzoPerGiorno().compareTo(prezzoMax) <= 0;
                } catch (NumberFormatException e) {
                    return true;
                }
            })
            .filter(v -> !"true".equals(soloDisponibili) || v.isDisponibile())
            .collect(Collectors.toList());
    }
    
    private List<Veicolo> applySorting(List<Veicolo> veicoli, String ordinamento) {
        if (ordinamento == null || ordinamento.isEmpty()) {
            ordinamento = "marca"; // Default
        }
        
        switch (ordinamento) {
            case "prezzo_asc":
                return veicoli.stream()
                    .sorted((v1, v2) -> v1.getPrezzoPerGiorno().compareTo(v2.getPrezzoPerGiorno()))
                    .collect(Collectors.toList());
            case "prezzo_desc":
                return veicoli.stream()
                    .sorted((v1, v2) -> v2.getPrezzoPerGiorno().compareTo(v1.getPrezzoPerGiorno()))
                    .collect(Collectors.toList());
            case "marca":
                return veicoli.stream()
                    .sorted((v1, v2) -> {
                        int marcaComp = v1.getMarca().compareToIgnoreCase(v2.getMarca());
                        return marcaComp != 0 ? marcaComp : v1.getModello().compareToIgnoreCase(v2.getModello());
                    })
                    .collect(Collectors.toList());
            case "modello":
                return veicoli.stream()
                    .sorted((v1, v2) -> v1.getModello().compareToIgnoreCase(v2.getModello()))
                    .collect(Collectors.toList());
            default:
                return veicoli.stream()
                    .sorted((v1, v2) -> {
                        int marcaComp = v1.getMarca().compareToIgnoreCase(v2.getMarca());
                        return marcaComp != 0 ? marcaComp : v1.getModello().compareToIgnoreCase(v2.getModello());
                    })
                    .collect(Collectors.toList());
        }
    }
    
    private void handleAddToCart(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        // Verifica autenticazione
        if (!isUserLoggedIn(request)) {
            response.sendRedirect("login?message=Devi effettuare il login per prenotare");
            return;
        }
        
        String targa = request.getParameter("targa");
        if (isNotEmpty(targa)) {
            // Verificare che il veicolo esista e sia disponibile
            Veicolo veicolo = veicoloDAO.findByTarga(targa);
            if (veicolo != null && veicolo.isDisponibile()) {
                // TODO: Implementare logica carrello con sessione
                System.out.println("🛒 Veicolo " + targa + " aggiunto al carrello");
                response.sendRedirect("catalogo?success=Veicolo aggiunto al carrello");
            } else {
                response.sendRedirect("catalogo?error=Veicolo non disponibile");
            }
        } else {
            response.sendRedirect("catalogo?error=Errore nell'aggiunta al carrello");
        }
    }
    
    private void handleViewDetails(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        String targa = request.getParameter("targa");
        if (isNotEmpty(targa)) {
            // TODO: Creare pagina dettagli veicolo
            response.sendRedirect("catalogo?info=Dettagli veicolo " + targa);
        } else {
            response.sendRedirect("catalogo");
        }
    }
    
   
    private boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
    
    private boolean isUserLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        
        Utente utente = (Utente) session.getAttribute("utente");
        return utente != null;
    }
    
    private Utente getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        
        return (Utente) session.getAttribute("utente");
    }
}