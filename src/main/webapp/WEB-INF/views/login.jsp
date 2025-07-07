<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="it.easyridedb.model.Utente" %>

<%
    // Recupera utente e dati dalla sessione
    Utente utente = (Utente) session.getAttribute("utente");
    String contextPath = request.getContextPath();
    
    // Recupera messaggi dal request
    String welcomeMessage = (String) request.getAttribute("welcomeMessage");
    String successMessage = (String) request.getAttribute("successMessage");
    String errorMessage = (String) request.getAttribute("errorMessage");
    String csrfToken = (String) request.getAttribute("csrfToken");
    
    // Recupera email se presente (per form compilato)
    String email = request.getParameter("email") != null ? request.getParameter("email") : "";
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>EasyRide - Login</title>
    
    <!-- CSS Esterni -->
    <link rel="stylesheet" href="<%= contextPath %>/styles/common.css">
    <link rel="stylesheet" href="<%= contextPath %>/styles/login.css">
</head>
<body>
    <!-- Include Header -->
    <jsp:include page="/WEB-INF/includes/header.jsp" />
    
    <main>
        <div class="container">
            <div class="form-container">
                <div class="form-header">
                    <h1>🚗 Accedi a EasyRide</h1>
                    <p>Entra nel tuo account per gestire le prenotazioni</p>
                </div>
                
                <!-- Messaggi -->
                <% if (welcomeMessage != null) { %>
                    <div class="message welcome"><%= welcomeMessage %></div>
                <% } %>
                
                <% if (successMessage != null) { %>
                    <div class="message success"><%= successMessage %></div>
                <% } %>
                
                <% if (errorMessage != null) { %>
                    <div class="message error"><%= errorMessage %></div>
                <% } %>
                
                <!-- Form Login -->
                <form method="post" action="<%= contextPath %>/login" class="auth-form">
                    
                    <!-- Token CSRF -->
                    <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                    
                    <!-- Email -->
                    <div class="form-group">
                        <label for="email">📧 Email</label>
                        <input type="email" id="email" name="email" value="<%= email %>" required>
                        <span class="error-message" id="email-error"></span>
                    </div>
                    
                    <!-- Password -->
                    <div class="form-group">
                        <label for="password">🔒 Password</label>
                        <input type="password" id="password" name="password" required>
                        <span class="error-message" id="password-error"></span>
                    </div>
                    
                    <!-- Remember Me -->
                    <div class="checkbox-group">
                        <label class="checkbox-label">
                            <input type="checkbox" name="remember">
                            <span class="checkmark"></span>
                            Ricordami per 7 giorni
                        </label>
                    </div>
                    
                    <!-- Submit Button -->
                    <button type="submit" class="btn btn-primary">
                        🔐 Accedi
                    </button>
                    
                </form>
                
                <!-- Link Registrazione e Recovery -->
                <div class="form-footer">
                    <p>Non hai un account? <a href="<%= contextPath %>/registrazione">Registrati qui</a></p>
                    <p><a href="<%= contextPath %>/recupera-password">Password dimenticata?</a></p>
                    <p><a href="<%= contextPath %>/catalogo">← Torna al Catalogo</a></p>
                </div>
            </div>
        </div>
    </main>
    
    <!-- Include Footer -->
    <jsp:include page="/WEB-INF/includes/footer.jsp" />
    
    <!-- JavaScript -->
    <script src="<%= contextPath %>/scripts/login.js"></script>
</body>
</html>