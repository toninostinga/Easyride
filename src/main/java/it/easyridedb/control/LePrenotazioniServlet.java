package it.easyridedb.control;

import it.easyridedb.dao.impl.VeicoloDAOImpl;
import it.easyridedb.dao.impl.PrenotazioneDAOImpl;
import it.easyridedb.model.Veicolo;
import it.easyridedb.model.Prenotazione;
import it.easyridedb.model.Utente;
import it.easyridedb.control.LoginServlet;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.RequestDispatcher;

@WebServlet(urlPatterns = {"/prenotazioni", "/le-mie-prenotazioni"})
public class LePrenotazioniServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private VeicoloDAOImpl veicoloDAO;
    private PrenotazioneDAOImpl prenotazioneDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            veicoloDAO = new VeicoloDAOImpl();
            prenotazioneDAO = new PrenotazioneDAOImpl();
            System.out.println("✅ LePrenotazioniServlet inizializzata");
        } catch (Exception e) {
            System.err.println("❌ Errore inizializzazione LePrenotazioniServlet: " + e.getMessage());
            throw new ServletException("Impossibile inizializzare DAO", e);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // Verifica autenticazione
            if (!LoginServlet.isUserLoggedIn(request)) {
                response.sendRedirect("login?message=Devi effettuare il login per visualizzare le prenotazioni");
                return;
            }
            
            Utente utente = LoginServlet.getCurrentUser(request);
            
            // Recupera le prenotazioni dell'utente
            List<Prenotazione> prenotazioni = prenotazioneDAO.findByUtente(utente.getId());
            
            System.out.println("🔍 Trovate " + prenotazioni.size() + " prenotazioni per utente ID: " + utente.getId());
            
            // Per ogni prenotazione, recupera i dettagli del veicolo
            for (Prenotazione prenotazione : prenotazioni) {
                try {
                    Veicolo veicolo = veicoloDAO.findByTarga(prenotazione.getVeicoloTarga());
                    if (veicolo != null) {
                        // Aggiungi i dettagli del veicolo alla prenotazione
                        prenotazione.setMarcaVeicolo(veicolo.getMarca());
                        prenotazione.setModelloVeicolo(veicolo.getModello());
                        prenotazione.setTipoVeicolo(veicolo.getTipo());
                        
                        System.out.println("✅ Dati veicolo aggiunti per targa: " + prenotazione.getVeicoloTarga());
                    } else {
                        System.out.println("⚠️ Veicolo non trovato per targa: " + prenotazione.getVeicoloTarga());
                        // Imposta valori di default
                        prenotazione.setMarcaVeicolo("N/A");
                        prenotazione.setModelloVeicolo("Veicolo non disponibile");
                        prenotazione.setTipoVeicolo("N/A");
                    }
                } catch (Exception e) {
                    System.err.println("❌ Errore recupero veicolo per targa " + prenotazione.getVeicoloTarga() + ": " + e.getMessage());
                    // Imposta valori di default in caso di errore
                    prenotazione.setMarcaVeicolo("N/A");
                    prenotazione.setModelloVeicolo("Errore caricamento");
                    prenotazione.setTipoVeicolo("N/A");
                }
            }
            
            // Prepara attributi per JSP
            request.setAttribute("prenotazioni", prenotazioni);
            request.setAttribute("utente", utente);
            
            // Messaggi
            String successMessage = request.getParameter("success");
            String errorMessage = request.getParameter("error");
            
            if (successMessage != null) {
                request.setAttribute("successMessage", successMessage);
            }
            if (errorMessage != null) {
                request.setAttribute("errorMessage", errorMessage);
            }
            
            // Inoltra alla JSP dedicata per le prenotazioni
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/le-mie-prenotazioni.jsp");
            dispatcher.forward(request, response);
            
        } catch (Exception e) {
            System.err.println("❌ Errore nel recupero prenotazioni: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect("home?error=Errore nel caricamento delle prenotazioni");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Il POST non è gestito, reindirizza alla visualizzazione
        response.sendRedirect("prenotazioni");
    }
}