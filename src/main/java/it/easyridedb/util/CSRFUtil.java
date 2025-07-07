package it.easyridedb.util;

import java.security.SecureRandom;
import java.util.Base64;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;


public class CSRFUtil {
    
    private static final String CSRF_TOKEN_ATTRIBUTE = "csrfToken";
    private static final int TOKEN_LENGTH = 32;
    private static final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * Genera un nuovo token CSRF e lo salva in sessione
     */
    public static String generateToken(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        
        // Genera token casuale
        byte[] tokenBytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        
        // Salva in sessione
        session.setAttribute(CSRF_TOKEN_ATTRIBUTE, token);
        
        System.out.println("🔐 Generato nuovo token CSRF: " + token.substring(0, 8) + "...");
        return token;
    }
    
    /**
     * Ottiene il token CSRF dalla sessione (genera se non esiste)
     */
    public static String getToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        
        if (session == null) {
            return generateToken(request);
        }
        
        String token = (String) session.getAttribute(CSRF_TOKEN_ATTRIBUTE);
        if (token == null || token.trim().isEmpty()) {
            return generateToken(request);
        }
        
        return token;
    }
    
    /**
     * Valida token CSRF ricevuto dal form
     */
    public static boolean validateToken(HttpServletRequest request, String receivedToken) {
        if (receivedToken == null || receivedToken.trim().isEmpty()) {
            System.out.println("❌ Token CSRF mancante");
            return false;
        }
        
        HttpSession session = request.getSession(false);
        if (session == null) {
            System.out.println("❌ Sessione non trovata per validazione CSRF");
            return false;
        }
        
        String sessionToken = (String) session.getAttribute(CSRF_TOKEN_ATTRIBUTE);
        if (sessionToken == null) {
            System.out.println("❌ Token CSRF non trovato in sessione");
            return false;
        }
        
        boolean isValid = sessionToken.equals(receivedToken);
        
        if (isValid) {
            System.out.println("✅ Token CSRF valido");
            // Regenera token per sicurezza (one-time use)
            generateToken(request);
        } else {
            System.out.println("❌ Token CSRF non valido. Atteso: " + sessionToken.substring(0, 8) + 
                             "... Ricevuto: " + receivedToken.substring(0, Math.min(8, receivedToken.length())) + "...");
        }
        
        return isValid;
    }
    
    /**
     * Valida token CSRF dal parametro request
     */
    public static boolean validateToken(HttpServletRequest request) {
        String receivedToken = request.getParameter("csrfToken");
        return validateToken(request, receivedToken);
    }
    
    /**
     * Genera HTML input hidden per token CSRF
     */
    public static String generateHiddenInput(HttpServletRequest request) {
        String token = getToken(request);
        return "<input type=\"hidden\" name=\"csrfToken\" value=\"" + token + "\">";
    }
    
    /**
     * Rimuove token dalla sessione (per logout)
     */
    public static void removeToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(CSRF_TOKEN_ATTRIBUTE);
            System.out.println("🧹 Token CSRF rimosso dalla sessione");
        }
    }
    
    /**
     * Verifica se la richiesta necessita protezione CSRF
     */
    public static boolean requiresCSRFProtection(HttpServletRequest request) {
        String method = request.getMethod().toUpperCase();
        // Proteggi solo richieste che modificano dati
        return "POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method);
    }
    
    /**
     * Gestisce errore CSRF - redirect con messaggio
     */
    public static String handleCSRFError(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        String contextPath = request.getContextPath();
        
        // Se possibile, torna alla pagina di origine
        if (referer != null && referer.startsWith(request.getScheme() + "://" + request.getServerName())) {
            String returnUrl = referer;
            char separator = returnUrl.contains("?") ? '&' : '?';
            return returnUrl + separator + "error=Token di sicurezza non valido. Riprova.";
        }
        
        // Altrimenti vai al catalogo
        return contextPath + "/catalogo?error=Token di sicurezza non valido. Riprova.";
    }
}