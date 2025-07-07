<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="it.easyridedb.model.Utente, it.easyridedb.model.Prenotazione" %>
<%@ page import="java.util.List, java.text.SimpleDateFormat" %>

<%!
    
    public String escapeHtml(String str) {
        if (str == null) return "";
        return str.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#x27;");
    }
%>

<%
    
    Utente utente = (Utente) session.getAttribute("utente");
    @SuppressWarnings("unchecked")
    List<Prenotazione> prenotazioni = (List<Prenotazione>) request.getAttribute("prenotazioni");
    String contextPath = request.getContextPath();
    
   
    String successMessage = (String) request.getAttribute("successMessage");
    String errorMessage = (String) request.getAttribute("errorMessage");
    
    
    if (utente == null) {
        response.sendRedirect(contextPath + "/login?message=Devi effettuare il login");
        return;
    }
    
    // Formatter per le date
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Le mie prenotazioni - EasyRide</title>
    
    <!-- CSS -->
    <link rel="stylesheet" href="<%= contextPath %>/styles/common.css">
    <link rel="stylesheet" href="<%= contextPath %>/styles/prenotazioni-list.css">
    
   
    <meta name="description" content="Visualizza e gestisci le tue prenotazioni EasyRide">
</head>
<body>
    
    <!-- Include Header -->
    <jsp:include page="/WEB-INF/includes/header.jsp" />
    
    <main class="prenotazioni-main">
        <div class="container">
            
            <!-- Breadcrumb -->
            <nav class="breadcrumb" aria-label="Navigazione">
                <a href="<%= contextPath %>/">Home</a>
                <span>›</span>
                <span>Le mie prenotazioni</span>
            </nav>
            
            <!-- Header Sezione -->
            <div class="page-header">
                <div class="header-content">
                    <h1 class="page-title">
                        <span class="title-icon">📋</span>
                        Le mie prenotazioni
                    </h1>
                    <p class="page-subtitle">
                        Ciao <%= escapeHtml(utente.getNome()) %>, ecco le tue prenotazioni attive e passate
                    </p>
                </div>
                <div class="header-actions">
                    <a href="<%= contextPath %>/catalogo" class="btn btn-primary">
                        <span class="btn-icon">🚗</span>
                        Prenota un nuovo veicolo
                    </a>
                </div>
            </div>
            
            <!-- Messaggi -->
            <% if (successMessage != null) { %>
                <div class="message success" role="alert">
                    <span class="message-icon">✅</span>
                    <%= escapeHtml(successMessage) %>
                    <button type="button" class="close-btn" aria-label="Chiudi">×</button>
                </div>
            <% } %>
            
            <% if (errorMessage != null) { %>
                <div class="message error" role="alert" aria-live="assertive">
                    <span class="message-icon">❌</span>
                    <%= escapeHtml(errorMessage) %>
                    <button type="button" class="close-btn" aria-label="Chiudi">×</button>
                </div>
            <% } %>
            
            <!-- Contenuto Principale -->
            <div class="prenotazioni-content">
                
                <% if (prenotazioni == null || prenotazioni.isEmpty()) { %>
                    <!-- Stato vuoto -->
                    <div class="empty-state">
                        <div class="empty-icon">🚗</div>
                        <h2 class="empty-title">Nessuna prenotazione trovata</h2>
                        <p class="empty-description">
                            Non hai ancora effettuato prenotazioni con EasyRide.<br>
                            Esplora il nostro catalogo e trova il veicolo perfetto per te!
                        </p>
                        <div class="empty-actions">
                            <a href="<%= contextPath %>/catalogo" class="btn btn-primary">
                                <span class="btn-icon">🔍</span>
                                Esplora il catalogo
                            </a>
                        </div>
                    </div>
                    
                <% } else { %>
                    
                    <!-- Statistiche -->
                    <div class="stats-section">
                        <div class="stat-card">
                            <div class="stat-number"><%= prenotazioni.size() %></div>
                            <div class="stat-label">Prenotazioni totali</div>
                        </div>
                        
                        <% 
                            int confermate = 0, inCorso = 0, completate = 0;
                            for (Prenotazione p : prenotazioni) {
                                if ("confermata".equals(p.getStato())) confermate++;
                                else if ("in_corso".equals(p.getStato())) inCorso++;
                                else if ("completata".equals(p.getStato())) completate++;
                            }
                        %>
                        
                        <div class="stat-card">
                            <div class="stat-number"><%= confermate %></div>
                            <div class="stat-label">Confermate</div>
                        </div>
                        
                        <div class="stat-card">
                            <div class="stat-number"><%= inCorso %></div>
                            <div class="stat-label">In corso</div>
                        </div>
                        
                        <div class="stat-card">
                            <div class="stat-number"><%= completate %></div>
                            <div class="stat-label">Completate</div>
                        </div>
                    </div>
                    
                    <!-- Lista Prenotazioni -->
                    <div class="prenotazioni-grid">
                        <% for (Prenotazione prenotazione : prenotazioni) { %>
                            <div class="prenotazione-card">
                                
                                <!-- Header della card -->
                                <div class="card-header">
                                    <div class="vehicle-info">
                                        <h3 class="vehicle-name">
                                            <%= escapeHtml(prenotazione.getMarcaVeicolo()) %> 
                                            <%= escapeHtml(prenotazione.getModelloVeicolo()) %>
                                        </h3>
                                        <div class="vehicle-details">
                                            <span class="detail-badge">
                                                🏷️ <%= escapeHtml(prenotazione.getVeicoloTarga()) %>
                                            </span>
                                            <% if (prenotazione.getTipoVeicolo() != null && !prenotazione.getTipoVeicolo().equals("N/A")) { %>
                                                <span class="detail-badge">
                                                    🚙 <%= escapeHtml(prenotazione.getTipoVeicolo()) %>
                                                </span>
                                            <% } %>
                                        </div>
                                    </div>
                                    
                                    <div class="status-section">
                                        <span class="status-badge status-<%= prenotazione.getStato().toLowerCase() %>">
                                            <%= escapeHtml(prenotazione.getStatoDescrizione()) %>
                                        </span>
                                        <div class="booking-id">ID: #<%= prenotazione.getId() %></div>
                                    </div>
                                </div>
                                
                                <!-- Date Section -->
                                <div class="dates-section">
                                    <div class="date-info">
                                        <div class="date-label">📥 Ritiro</div>
                                        <div class="date-value">
                                            <%= dateFormat.format(prenotazione.getDataRitiro()) %>
                                        </div>
                                    </div>
                                    
                                    <div class="date-separator">→</div>
                                    
                                    <div class="date-info">
                                        <div class="date-label">📤 Restituzione</div>
                                        <div class="date-value">
                                            <%= dateFormat.format(prenotazione.getDataRestituzione()) %>
                                        </div>
                                    </div>
                                </div>
                                
                                <!-- Dettagli Prenotazione -->
                                <div class="booking-details">
                                    <div class="detail-row">
                                        <span class="detail-label">💰 Prezzo totale:</span>
                                        <span class="detail-value price">
                                            €<%= String.format("%.2f", prenotazione.getPrezzoTotale()) %>
                                        </span>
                                    </div>
                                    
                                    <div class="detail-row">
                                        <span class="detail-label">📅 Data prenotazione:</span>
                                        <span class="detail-value">
                                            <%= prenotazione.getDataPrenotazione() != null ? 
                                                dateTimeFormat.format(prenotazione.getDataPrenotazione()) : "N/A" %>
                                        </span>
                                    </div>
                                    
                                    <% if (prenotazione.getTerminalRitiroId() > 0) { %>
                                        <div class="detail-row">
                                            <span class="detail-label">🏢 Terminal:</span>
                                            <span class="detail-value">Terminal ID <%= prenotazione.getTerminalRitiroId() %></span>
                                        </div>
                                    <% } %>
                                </div>
                                
                                <!-- Azioni -->
                                <div class="card-actions">
                                    <% if ("confermata".equals(prenotazione.getStato())) { %>
                                        
                                        <button type="button" 
                                                class="btn btn-outline btn-sm"
                                                onclick="viewDetails(<%= prenotazione.getId() %>)">
                                            <span class="btn-icon">👁️</span>
                                            Dettagli
                                        </button>
                                        
                                        <button type="button" 
                                                class="btn btn-danger btn-sm"
                                                onclick="cancelBooking(<%= prenotazione.getId() %>)">
                                            <span class="btn-icon">❌</span>
                                            Annulla
                                        </button>
                                        
                                    <% } else if ("in_corso".equals(prenotazione.getStato())) { %>
                                        
                                        <button type="button" 
                                                class="btn btn-outline btn-sm"
                                                onclick="viewDetails(<%= prenotazione.getId() %>)">
                                            <span class="btn-icon">👁️</span>
                                            Dettagli
                                        </button>
                                        
                                        <div class="status-note">
                                            <span class="note-icon">🚗</span>
                                            Noleggio in corso
                                        </div>
                                        
                                    <% } else { %>
                                        
                                        <button type="button" 
                                                class="btn btn-outline btn-sm"
                                                onclick="viewDetails(<%= prenotazione.getId() %>)">
                                            <span class="btn-icon">👁️</span>
                                            Dettagli
                                        </button>
                                        
                                        <% if ("completata".equals(prenotazione.getStato())) { %>
                                            <button type="button" 
                                                    class="btn btn-secondary btn-sm"
                                                    onclick="repeatBooking('<%= escapeHtml(prenotazione.getVeicoloTarga()) %>')">
                                                <span class="btn-icon">🔄</span>
                                                Ripeti
                                            </button>
                                        <% } %>
                                        
                                    <% } %>
                                </div>
                                
                            </div>
                        <% } %>
                    </div>
                    
                <% } %>
                
            </div>
            
            
            <aside class="help-section">
                <div class="help-card">
                    <h4>❓ Hai bisogno di aiuto?</h4>
                    <p>Per qualsiasi domanda sulle tue prenotazioni, contatta il nostro servizio clienti.</p>
                    <div class="contact-info">
                        <a href="tel:+39800123456" class="contact-link">
                            📞 +39 800 123 456
                        </a>
                        <a href="mailto:info@easyride.it" class="contact-link">
                            ✉️ info@easyride.it
                        </a>
                    </div>
                </div>
            </aside>
            
        </div>
    </main>
    
    
    <jsp:include page="/WEB-INF/includes/footer.jsp" />
    
    
    <script src="<%= contextPath %>/scripts/le-mie-prenotazioni.js"></script>
    
</body>
</html>