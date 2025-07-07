package it.easyridedb.control;

import it.easyridedb.dao.impl.UtenteDAOImpl;
import it.easyridedb.model.Utente;
import it.easyridedb.util.CSRFUtil;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.RequestDispatcher;


@WebServlet("/registrazione")
public class RegistrazioneServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private UtenteDAOImpl utenteDAO;
    
    /**
     * Inizializzazione servlet
     */
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            utenteDAO = new UtenteDAOImpl();
            System.out.println("✅ RegistrazioneServlet MVC inizializzata correttamente");
        } catch (Exception e) {
            System.err.println("❌ Errore inizializzazione RegistrazioneServlet: " + e.getMessage());
            throw new ServletException("Impossibile inizializzare UtenteDAO", e);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // Controlla se utente già loggato
            if (LoginServlet.isUserLoggedIn(request)) {
                Utente utente = LoginServlet.getCurrentUser(request);
                response.sendRedirect("admin".equals(utente.getRuolo()) ? "admin-dashboard" : "catalogo");
                return;
            }
            
            // Prepara attributi per JSP
            request.setAttribute("csrfToken", CSRFUtil.getToken(request));
            
            // Gestisci messaggi da URL
            String message = request.getParameter("message");
            if (message != null) {
                request.setAttribute("welcomeMessage", message);
            }
            
            // Inoltra a JSP
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/registrazione.jsp");
            dispatcher.forward(request, response);
            
        } catch (Exception e) {
            System.err.println("Errore nel GET registrazione: " + e.getMessage());
            request.setAttribute("errorMessage", "Errore nel caricamento della pagina: " + e.getMessage());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/error.jsp");
            dispatcher.forward(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // ✅ VALIDAZIONE CSRF
            if (!CSRFUtil.validateToken(request)) {
                String redirectUrl = CSRFUtil.handleCSRFError(request);
                response.sendRedirect(redirectUrl);
                return;
            }
            
            // Recupera parametri dal form
            String nome = request.getParameter("nome");
            String cognome = request.getParameter("cognome");
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            String confermaPassword = request.getParameter("confermaPassword");
            String ruolo = request.getParameter("ruolo");
            String accettaTermini = request.getParameter("accettaTermini");
            
            // Validazione completa
            String errorMessage = validateRegistrationData(nome, cognome, email, password, 
                                                         confermaPassword, ruolo, accettaTermini);
            
            if (errorMessage != null) {
                showRegistrationFormWithError(request, response, errorMessage);
                return;
            }
            
            // Verifica email già esistente
            if (utenteDAO.emailExists(email.trim())) {
                showRegistrationFormWithError(request, response, 
                    "❌ Email già registrata. <a href='login'>Vai al login</a>");
                return;
            }
            
            // Crea nuovo utente
            Utente nuovoUtente = new Utente(
                nome.trim(),
                cognome.trim(), 
                email.trim().toLowerCase(),
                password,
                ruolo != null ? ruolo : "cliente" // Default cliente
            );
            
            // Inserisce nel database
            boolean registrationSuccess = utenteDAO.insert(nuovoUtente);
            
            if (registrationSuccess) {
                handleSuccessfulRegistration(request, response, nuovoUtente);
            } else {
                showRegistrationFormWithError(request, response, 
                    "❌ Errore durante la registrazione. Riprova.");
            }
            
        } catch (IllegalArgumentException e) {
            showRegistrationFormWithError(request, response, "❌ " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Errore durante registrazione: " + e.getMessage());
            showRegistrationFormWithError(request, response, 
                "❌ Errore del sistema. Riprova più tardi.");
        }
    }
    
    
    private String validateRegistrationData(String nome, String cognome, String email, 
                                          String password, String confermaPassword, 
                                          String ruolo, String accettaTermini) {
        
        // Controllo campi obbligatori
        if (isEmpty(nome)) return "Il nome è obbligatorio";
        if (isEmpty(cognome)) return "Il cognome è obbligatorio";
        if (isEmpty(email)) return "L'email è obbligatoria";
        if (isEmpty(password)) return "La password è obbligatoria";
        if (isEmpty(confermaPassword)) return "La conferma password è obbligatoria";
        
        // Validazione lunghezze
        if (nome.trim().length() < 2) return "Il nome deve contenere almeno 2 caratteri";
        if (cognome.trim().length() < 2) return "Il cognome deve contenere almeno 2 caratteri";
        
        // Validazione email
        if (!Utente.isEmailValid(email)) return "Formato email non valido";
        
        // Validazione password
        if (!Utente.isPasswordValid(password)) return "La password deve contenere almeno 6 caratteri";
        
        // Conferma password
        if (!password.equals(confermaPassword)) return "Le password non coincidono";
        
        // Validazione ruolo (se specificato)
        if (ruolo != null && !ruolo.trim().isEmpty()) {
            if (!"cliente".equals(ruolo) && !"admin".equals(ruolo)) {
                return "Ruolo non valido";
            }
        }
        
        // Accettazione termini
        if (!"on".equals(accettaTermini)) return "Devi accettare i termini e condizioni";
        
        return null; // Tutto OK
    }
    
    
    private void handleSuccessfulRegistration(HttpServletRequest request, HttpServletResponse response, 
                                            Utente nuovoUtente) throws IOException {
        
        // Log successo
        System.out.println("📝 Nuova registrazione: " + nuovoUtente.getEmail() + 
                          " (" + nuovoUtente.getRuolo() + ")");
        
        // Auto-login
        HttpSession session = request.getSession(true);
        session.setAttribute("utente", nuovoUtente);
        session.setAttribute("utenteId", nuovoUtente.getId());
        session.setAttribute("userRole", nuovoUtente.getRuolo());
        session.setAttribute("userName", nuovoUtente.getNomeCompleto());
        session.setMaxInactiveInterval(2 * 60 * 60); // 2 ore
        
        // Redirect in base al ruolo
        String redirectURL = "admin".equals(nuovoUtente.getRuolo()) ? "admin-dashboard" : "catalogo";
        response.sendRedirect(redirectURL + "?welcome=true");
    }
    
   
    private void showRegistrationFormWithError(HttpServletRequest request, HttpServletResponse response, 
                                             String errorMessage) throws ServletException, IOException {
        
        // Prepara attributi per JSP
        request.setAttribute("errorMessage", errorMessage);
        request.setAttribute("csrfToken", CSRFUtil.getToken(request));
        
        // Inoltra a JSP
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/registrazione.jsp");
        dispatcher.forward(request, response);
    }
    
    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}