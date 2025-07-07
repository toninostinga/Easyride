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

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private UtenteDAOImpl utenteDAO;
    
    /**
     * Inizializzazione servlet - crea DAO
     */
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            utenteDAO = new UtenteDAOImpl();
            System.out.println("✅ LoginServlet MVC inizializzata correttamente");
        } catch (Exception e) {
            System.err.println("❌ Errore inizializzazione LoginServlet: " + e.getMessage());
            throw new ServletException("Impossibile inizializzare UtenteDAO", e);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // Gestisce azione logout
            String action = request.getParameter("action");
            if ("logout".equals(action)) {
                handleLogout(request, response);
                return;
            }
            
            // Controlla se utente già loggato
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("utente") != null) {
                Utente utente = (Utente) session.getAttribute("utente");
                redirectToDashboard(response, utente);
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
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/login.jsp");
            dispatcher.forward(request, response);
            
        } catch (Exception e) {
            System.err.println("Errore nel GET login: " + e.getMessage());
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
            
            // Recupera parametri
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            String remember = request.getParameter("remember");
            
            // Validazione input
            if (isInvalidInput(email, password)) {
                showLoginFormWithError(request, response, "❌ Email e password sono obbligatori");
                return;
            }
            
            // Autentica utente
            Utente utente = utenteDAO.authenticate(email.trim(), password);
            
            if (utente != null) {
                handleSuccessfulLogin(request, response, utente, remember != null);
            } else {
                handleFailedLogin(request, response);
            }
            
        } catch (Exception e) {
            System.err.println("Errore durante autenticazione: " + e.getMessage());
            showLoginFormWithError(request, response, "❌ Errore del sistema. Riprova più tardi.");
        }
    }
    
    private void handleSuccessfulLogin(HttpServletRequest request, HttpServletResponse response, 
                                     Utente utente, boolean remember) throws IOException {
        
        // Crea sessione
        HttpSession session = request.getSession(true);
        
        // Salva dati utente in sessione
        session.setAttribute("utente", utente);
        session.setAttribute("utenteId", utente.getId());
        session.setAttribute("userRole", utente.getRuolo());
        session.setAttribute("userName", utente.getNome() + " " + utente.getCognome());
        session.setAttribute("userEmail", utente.getEmail());
        
        // Imposta durata sessione
        if (remember) {
            session.setMaxInactiveInterval(7 * 24 * 60 * 60); // 7 giorni
        } else {
            session.setMaxInactiveInterval(2 * 60 * 60); // 2 ore
        }
        
        // Log successo
        System.out.println("🔐 Login riuscito: " + utente.getEmail() + 
                          " (" + utente.getRuolo() + ")");
        
        // Reset tentativi falliti
        session.removeAttribute("loginAttempts");
        
        // Redirect appropriato
        redirectToDashboard(response, utente);
    }
    
   
    private void handleFailedLogin(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Incrementa contatore tentativi
        HttpSession session = request.getSession(true);
        Integer attempts = (Integer) session.getAttribute("loginAttempts");
        attempts = (attempts == null) ? 1 : attempts + 1;
        session.setAttribute("loginAttempts", attempts);
        
        String errorMessage = "❌ Email o password non corretti";
        
        // Blocco temporaneo dopo troppi tentativi
        if (attempts >= 5) {
            errorMessage = "❌ Troppi tentativi falliti. Riprova tra 10 minuti.";
            session.setMaxInactiveInterval(10 * 60); // 10 minuti
        }
        
        System.out.println("🚫 Tentativo login fallito. Tentativi: " + attempts);
        showLoginFormWithError(request, response, errorMessage);
    }
    
    /**
     * Gestisce logout
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            Utente utente = (Utente) session.getAttribute("utente");
            if (utente != null) {
                System.out.println("👋 Logout: " + utente.getEmail());
            }
            
            // ✅ RIMUOVI TOKEN CSRF
            CSRFUtil.removeToken(request);
            
            session.invalidate();
        }
        
        showLoginFormWithError(request, response, "✅ Logout effettuato con successo");
    }
    
    /**
     * Redirect in base al ruolo utente
     */
    private void redirectToDashboard(HttpServletResponse response, Utente utente) throws IOException {
        
        String redirectURL;
        
        switch (utente.getRuolo().toLowerCase()) {
            case "admin":
                redirectURL = "admin-dashboard";
                break;
            case "cliente":
                redirectURL = "catalogo";
                break;
            default:
                redirectURL = "catalogo";
                break;
        }
        
        response.sendRedirect(redirectURL + "?welcome=true");
    }
    
    
    private void showLoginFormWithError(HttpServletRequest request, HttpServletResponse response, 
                                      String errorMessage) throws ServletException, IOException {
        
        // Prepara attributi per JSP
        if (errorMessage.contains("✅")) {
            request.setAttribute("successMessage", errorMessage);
        } else {
            request.setAttribute("errorMessage", errorMessage);
        }
        request.setAttribute("csrfToken", CSRFUtil.getToken(request));
        
        // Inoltra a JSP
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/login.jsp");
        dispatcher.forward(request, response);
    }
    
   
    private boolean isInvalidInput(String email, String password) {
        return email == null || email.trim().isEmpty() || 
               password == null || password.trim().isEmpty();
    }
    
   
    public static boolean isUserLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("utente") != null;
    }
    
    
    public static Utente getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (Utente) session.getAttribute("utente");
        }
        return null;
    }
    
   
    public static Integer getCurrentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (Integer) session.getAttribute("utenteId");
        }
        return null;
    }
    
    
    public static boolean isCurrentUserAdmin(HttpServletRequest request) {
        Utente utente = getCurrentUser(request);
        return utente != null && "admin".equals(utente.getRuolo().toLowerCase());
    }
    
   
    public static boolean isCurrentUserCliente(HttpServletRequest request) {
        Utente utente = getCurrentUser(request);
        return utente != null && "cliente".equals(utente.getRuolo().toLowerCase());
    }
}