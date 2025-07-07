<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="it.easyridedb.model.Utente, it.easyridedb.model.Veicolo" %>
<%@ page import="java.math.BigDecimal" %>

<%
    // Recupera dati dalla sessione e request
    Utente utente = (Utente) session.getAttribute("utente");
    Veicolo veicolo = (Veicolo) request.getAttribute("veicolo");
    String contextPath = request.getContextPath();
    
    // Messaggi e token
    String successMessage = (String) request.getAttribute("successMessage");
    String errorMessage = (String) request.getAttribute("errorMessage");
    String csrfToken = (String) request.getAttribute("csrfToken");
    
    // Date di default
    String dataRitiroDefault = (String) request.getAttribute("dataRitiroDefault");
    String dataRestituzioneDefault = (String) request.getAttribute("dataRestituzioneDefault");
    
    // Verifica che i dati necessari siano presenti
    if (veicolo == null || utente == null) {
        response.sendRedirect(contextPath + "/catalogo?error=Dati mancanti per la prenotazione");
        return;
    }
    
    // Funzione di escape HTML per sicurezza
    java.util.function.Function<String, String> escapeHtml = (str) -> {
        if (str == null) return "";
        return str.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#x27;");
    };
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Prenota <%= veicolo.getNomeCompleto() %> - EasyRide</title>
    
    <!-- CSS -->
    <link rel="stylesheet" href="<%= contextPath %>/styles/common.css">
    <link rel="stylesheet" href="<%= contextPath %>/styles/prenotazione.css">
    
    <!-- Meta per SEO -->
    <meta name="description" content="Prenota <%= veicolo.getNomeCompleto() %> con EasyRide. Noleggio facile e veloce.">
</head>
<body>
    
    <!-- Include Header -->
    <jsp:include page="/WEB-INF/includes/header.jsp" />
    
    <main class="prenotazione-main">
        <div class="container">
            
            <!-- Breadcrumb -->
            <nav class="breadcrumb" aria-label="Navigazione">
                <a href="<%= contextPath %>/">Home</a>
                <span>›</span>
                <a href="<%= contextPath %>/catalogo">Catalogo</a>
                <span>›</span>
                <span>Prenota <%= veicolo.getMarca() %> <%= veicolo.getModello() %></span>
            </nav>
            
            <!-- Messaggi -->
            <% if (successMessage != null) { %>
                <div class="message success" role="alert">
                    <%= escapeHtml.apply(successMessage) %>
                    <button type="button" class="close-btn" aria-label="Chiudi">×</button>
                </div>
            <% } %>
            
            <% if (errorMessage != null) { %>
                <div class="message error" role="alert" aria-live="assertive">
                    <%= escapeHtml.apply(errorMessage) %>
                    <button type="button" class="close-btn" aria-label="Chiudi">×</button>
                </div>
            <% } %>
            
            <div class="prenotazione-container">
                
                <!-- Sezione Veicolo -->
                <section class="vehicle-section" aria-label="Dettagli veicolo">
                    <div class="vehicle-header">
                        <h1 class="page-title">Prenota il tuo veicolo</h1>
                        <p class="page-subtitle">Completa i dettagli per confermare la prenotazione</p>
                    </div>
                    
                    <div class="vehicle-card">
                        <div class="vehicle-image">
                            <% if (veicolo.getImmagineUrl() != null && !veicolo.getImmagineUrl().isEmpty()) { %>
                                <img src="<%= contextPath %>/images/veicoli/<%= escapeHtml.apply(veicolo.getImmagineUrl()) %>" 
                                     alt="<%= escapeHtml.apply(veicolo.getNomeCompleto()) %>"
                                     onerror="this.src='<%= contextPath %>/images/no-image.jpg'">
                            <% } else { %>
                                <div class="no-image">
                                    <span class="vehicle-icon">🚗</span>
                                    <span class="no-image-text">Immagine non disponibile</span>
                                </div>
                            <% } %>
                        </div>
                        
                        <div class="vehicle-info">
                            <h2 class="vehicle-title"><%= escapeHtml.apply(veicolo.getNomeCompleto()) %></h2>
                            
                            <div class="vehicle-details">
                                <div class="detail-item">
                                    <span class="detail-label">🏷️ Targa:</span>
                                    <span class="detail-value"><%= escapeHtml.apply(veicolo.getTarga()) %></span>
                                </div>
                                <div class="detail-item">
                                    <span class="detail-label">🚙 Tipo:</span>
                                    <span class="detail-value"><%= escapeHtml.apply(veicolo.getTipo() != null ? veicolo.getTipo() : "N/A") %></span>
                                </div>
                                <div class="detail-item">
                                    <span class="detail-label">⚙️ Trasmissione:</span>
                                    <span class="detail-value"><%= escapeHtml.apply(veicolo.getTrasmissioneDisplay()) %></span>
                                </div>
                                <div class="detail-item">
                                    <span class="detail-label">⛽ Carburante:</span>
                                    <span class="detail-value"><%= escapeHtml.apply(veicolo.getCarburanteDisplay()) %></span>
                                </div>
                                <div class="detail-item">
                                    <span class="detail-label">💰 Prezzo:</span>
                                    <span class="detail-value price-highlight">€<%= veicolo.getPrezzoPerGiorno() %>/giorno</span>
                                </div>
                            </div>
                            
                            <% if (veicolo.isEcologico()) { %>
                                <div class="eco-badge">
                                    <span class="eco-icon">🌱</span>
                                    <span class="eco-text">Veicolo Ecologico</span>
                                </div>
                            <% } %>
                        </div>
                    </div>
                </section>
                
                <!-- Form Prenotazione -->
                <section class="booking-section" aria-label="Form prenotazione">
                    <form method="post" action="<%= contextPath %>/prenota" class="booking-form" id="bookingForm">
                        
                        <!-- Token CSRF -->
                        <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                        <input type="hidden" name="targa" value="<%= escapeHtml.apply(veicolo.getTarga()) %>">
                        
                        <div class="form-header">
                            <h3>📅 Seleziona Date</h3>
                            <p>Scegli quando vuoi ritirare e restituire il veicolo</p>
                        </div>
                        
                        <div class="form-row">
                            <!-- Data Ritiro -->
                            <div class="form-group">
                                <label for="dataRitiro">📥 Data Ritiro</label>
                                <input type="date" 
                                       id="dataRitiro" 
                                       name="dataRitiro" 
                                       value="<%= dataRitiroDefault %>" 
                                       min="<%= java.time.LocalDate.now() %>"
                                       max="<%= java.time.LocalDate.now().plusMonths(6) %>"
                                       required>
                                <span class="error-message" id="dataRitiro-error"></span>
                            </div>
                            
                            <!-- Data Restituzione -->
                            <div class="form-group">
                                <label for="dataRestituzione">📤 Data Restituzione</label>
                                <input type="date" 
                                       id="dataRestituzione" 
                                       name="dataRestituzione" 
                                       value="<%= dataRestituzioneDefault %>"
                                       min="<%= java.time.LocalDate.now().plusDays(1) %>"
                                       max="<%= java.time.LocalDate.now().plusMonths(6) %>"
                                       required>
                                <span class="error-message" id="dataRestituzione-error"></span>
                            </div>
                        </div>
                        
                        <!-- Note opzionali -->
                        <div class="form-group">
                            <label for="note">📝 Note (opzionale)</label>
                            <textarea id="note" 
                                      name="note" 
                                      rows="3" 
                                      maxlength="500"
                                      placeholder="Eventuali richieste speciali o note per la prenotazione..."></textarea>
                            <span class="char-counter">0/500 caratteri</span>
                        </div>
                        
                        <!-- Riepilogo Prezzo -->
                        <div class="price-summary" id="priceSummary">
                            <h4>💰 Riepilogo Prezzo</h4>
                            <div class="price-breakdown">
                                <div class="price-line">
                                    <span>Prezzo per giorno:</span>
                                    <span class="price-value">€<%= veicolo.getPrezzoPerGiorno() %></span>
                                </div>
                                <div class="price-line">
                                    <span>Numero giorni:</span>
                                    <span class="price-value" id="daysCount">1</span>
                                </div>
                                <div class="price-line subtotal">
                                    <span>Subtotale:</span>
                                    <span class="price-value" id="subtotal">€<%= veicolo.getPrezzoPerGiorno() %></span>
                                </div>
                                <div class="price-line total">
                                    <span>Totale:</span>
                                    <span class="price-value" id="totalPrice">€<%= veicolo.getPrezzoPerGiorno() %></span>
                                </div>
                            </div>
                            
                            <div class="price-note">
                                <small>📌 Il prezzo include assicurazione base. Possono applicarsi costi aggiuntivi per servizi extra.</small>
                            </div>
                        </div>
                        
                        <!-- Termini e Condizioni -->
                        <div class="terms-section">
                            <label class="checkbox-label">
                                <input type="checkbox" id="terms" name="terms" required>
                                <span class="checkmark"></span>
                                Accetto i <a href="<%= contextPath %>/termini" target="_blank">Termini e Condizioni</a> 
                                e l'<a href="<%= contextPath %>/privacy" target="_blank">Informativa Privacy</a>
                            </label>
                        </div>
                        
                        <!-- Pulsanti Azione -->
                        <div class="form-actions">
                            <a href="<%= contextPath %>/catalogo" class="btn btn-secondary">
                                ← Torna al Catalogo
                            </a>
                            <button type="submit" class="btn btn-primary" id="submitBtn">
                                <span class="btn-icon">🛒</span>
                                <span class="btn-text">Conferma Prenotazione</span>
                            </button>
                        </div>
                    </form>
                </section>
                
                <!-- Informazioni Aggiuntive -->
                <aside class="info-section" aria-label="Informazioni utili">
                    <div class="info-card">
                        <h4>ℹ️ Informazioni Utili</h4>
                        <ul class="info-list">
                            <li><strong>Documenti richiesti:</strong> Patente di guida valida e carta d'identità</li>
                            <li><strong>Età minima:</strong> 21 anni (alcune categorie richiedono 25 anni)</li>
                            <li><strong>Carburante:</strong> Il veicolo viene consegnato con il pieno</li>
                            <li><strong>Chilometraggio:</strong> Illimitato entro il territorio nazionale</li>
                            <li><strong>Assistenza:</strong> Servizio clienti 24/7 per emergenze</li>
                        </ul>
                    </div>
                    
                    <div class="info-card">
                        <h4>📞 Contatti</h4>
                        <div class="contact-info">
                            <p><strong>Servizio Clienti:</strong></p>
                            <p>📱 +39 800 123 456</p>
                            <p>✉️ info@easyride.it</p>
                            <p><strong>Emergenze 24/7:</strong></p>
                            <p>🚨 +39 800 999 888</p>
                        </div>
                    </div>
                </aside>
                
            </div>
        </div>
    </main>
    
    <!-- Include Footer -->
    <jsp:include page="/WEB-INF/includes/footer.jsp" />
    
    <!-- JavaScript -->
    <script>
        // Dati del veicolo per calcoli
        const vehicleData = {
            targa: '<%= escapeHtml.apply(veicolo.getTarga()) %>',
            pricePerDay: <%= veicolo.getPrezzoPerGiorno() %>
        };
    </script>
    <script src="<%= contextPath %>/scripts/prenotazione.js" defer></script>
    
</body>
</html>