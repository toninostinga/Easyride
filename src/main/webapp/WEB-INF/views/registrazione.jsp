<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="it.easyridedb.model.Utente" %>

<%
    // Recupera utente e dati dalla sessione
    Utente utente = (Utente) session.getAttribute("utente");
    String contextPath = request.getContextPath();
    
    // Recupera messaggi e dati dal request
    String welcomeMessage = (String) request.getAttribute("welcomeMessage");
    String successMessage = (String) request.getAttribute("successMessage");
    String errorMessage = (String) request.getAttribute("errorMessage");
    String csrfToken = (String) request.getAttribute("csrfToken");
    
    // Recupera valori form per mantenere stato in caso di errore
    String nome = request.getParameter("nome") != null ? request.getParameter("nome") : "";
    String cognome = request.getParameter("cognome") != null ? request.getParameter("cognome") : "";
    String email = request.getParameter("email") != null ? request.getParameter("email") : "";
    String ruolo = request.getParameter("ruolo") != null ? request.getParameter("ruolo") : "cliente";
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>EasyRide - Registrazione</title>
    
    <!-- CSS Esterni -->
    <link rel="stylesheet" href="<%= contextPath %>/styles/common.css">
    <link rel="stylesheet" href="<%= contextPath %>/styles/registrazione.css">
</head>
<body>
    <!-- Include Header -->
    <jsp:include page="/WEB-INF/includes/header.jsp" />
    
    <main>
        <div class="container">
            <div class="registration-form">
                <div class="form-header">
                    <h1>🚗 Registrati a EasyRide</h1>
                    <p>Crea il tuo account per iniziare a noleggiare</p>
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
                
                <!-- Form Registrazione -->
                <form method="post" action="<%= contextPath %>/registrazione" class="registration-form-container">
                    
                    <!-- Token CSRF -->
                    <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                    
                    <!-- Nome e Cognome -->
                    <div class="form-row">
                        <div class="form-group">
                            <label for="nome">👤 Nome *</label>
                            <input type="text" id="nome" name="nome" value="<%= nome %>" required maxlength="50">
                            <span class="error-message" id="nome-error"></span>
                        </div>
                        
                        <div class="form-group">
                            <label for="cognome">👤 Cognome *</label>
                            <input type="text" id="cognome" name="cognome" value="<%= cognome %>" required maxlength="50">
                            <span class="error-message" id="cognome-error"></span>
                        </div>
                    </div>
                    
                    <!-- Email -->
                    <div class="form-group">
                        <label for="email">📧 Email *</label>
                        <input type="email" id="email" name="email" value="<%= email %>" required>
                        <span class="error-message" id="email-error"></span>
                    </div>
                    
                    <!-- Password -->
                    <div class="form-group">
                        <label for="password">🔒 Password * (min. 6 caratteri)</label>
                        <input type="password" id="password" name="password" required minlength="6">
                        <div class="password-strength" id="password-strength">
                            <div class="strength-bar" id="strength-bar"></div>
                        </div>
                        <span class="error-message" id="password-error"></span>
                    </div>
                    
                    <!-- Conferma Password -->
                    <div class="form-group">
                        <label for="confermaPassword">🔒 Conferma Password *</label>
                        <input type="password" id="confermaPassword" name="confermaPassword" required>
                        <span class="error-message" id="conferma-password-error"></span>
                    </div>
                    
                    <!-- Ruolo (nascosto, default cliente) -->
                    <input type="hidden" name="ruolo" value="cliente">
                    
                    <!-- Termini e Condizioni -->
                    <div class="checkbox-group">
                        <label class="checkbox-label">
                            <input type="checkbox" name="accettaTermini" required>
                            <span class="checkmark"></span>
                            Accetto i <a href="#" target="_blank">termini e condizioni</a> e la <a href="#" target="_blank">privacy policy</a> *
                        </label>
                        <span class="error-message" id="termini-error"></span>
                    </div>
                    
                    <!-- Pulsante Submit -->
                    <button type="submit" class="btn btn-primary">
                        📝 Crea Account
                    </button>
                    
                </form>
                
                <!-- Link Login -->
                <div class="form-footer">
                    <p>Hai già un account? <a href="<%= contextPath %>/login">Accedi qui</a></p>
                    <p><a href="<%= contextPath %>/catalogo">← Torna al Catalogo</a></p>
                </div>
            </div>
        </div>
    </main>
    
    <!-- Include Footer -->
    <jsp:include page="/WEB-INF/includes/footer.jsp" />
    
    <!-- JavaScript -->
    <script src="<%= contextPath %>/scripts/registrazione.js"></script>
</body>
</html>