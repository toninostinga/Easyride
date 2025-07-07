<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="it.easyridedb.model.*" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>
<%@ page import="java.math.BigDecimal" %>

<%
    // Recupera dati dalla request
    Map<String, Object> statistiche = (Map<String, Object>) request.getAttribute("statistiche");
    String contextPath = request.getContextPath();
    String csrfToken = (String) request.getAttribute("csrfToken");
    String dataOggi = (String) request.getAttribute("dataOggi");
    
    // Messaggi
    String successMessage = (String) request.getAttribute("successMessage");
    String errorMessage = (String) request.getAttribute("errorMessage");
    
    // Utente admin corrente
    Utente admin = (Utente) session.getAttribute("utente");
    String nomeAdmin = admin != null ? admin.getNome() + " " + admin.getCognome() : "Amministratore";
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>EasyRide - Dashboard Amministratore</title>
    
    <!-- CSS -->
    <link rel="stylesheet" href="<%= contextPath %>/styles/common.css">
    <link rel="stylesheet" href="<%= contextPath %>/styles/admin-dashboard.css">
    
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body class="admin-body">
    <!-- Include Header -->
    <jsp:include page="/WEB-INF/includes/header.jsp" />
    
    <main class="admin-main">
        <div class="container">
            <!-- Header Dashboard -->
            <div class="dashboard-header">
                <div class="welcome-section">
                    <h1><i class="fas fa-tachometer-alt"></i> Dashboard Amministratore</h1>
                    <p class="welcome-text">Benvenuto, <strong><%= nomeAdmin %></strong> · <%= dataOggi %></p>
                </div>
                
                <div class="quick-actions">
                    <a href="<%= contextPath %>/admin-veicoli?action=add" class="btn btn-primary">
                        <i class="fas fa-plus"></i> Nuovo Veicolo
                    </a>
                    <a href="<%= contextPath %>/admin-ordini" class="btn btn-secondary">
                        <i class="fas fa-chart-line"></i> Visualizza Ordini
                    </a>
                </div>
            </div>
            
            <!-- Messaggi -->
            <% if (successMessage != null) { %>
                <div class="alert alert-success">
                    <i class="fas fa-check-circle"></i> <%= successMessage %>
                </div>
            <% } %>
            
            <% if (errorMessage != null) { %>
                <div class="alert alert-error">
                    <i class="fas fa-exclamation-triangle"></i> <%= errorMessage %>
                </div>
            <% } %>
            
            <!-- Statistiche Principali -->
            <div class="stats-grid">
                <div class="stat-card veicoli">
                    <div class="stat-icon">
                        <i class="fas fa-car"></i>
                    </div>
                    <div class="stat-content">
                        <h3>Veicoli Totali</h3>
                        <span class="stat-number"><%= statistiche.get("totaleVeicoli") %></span>
                        <div class="stat-details">
                            <span class="detail available">
                                <%= statistiche.get("veicoliDisponibili") %> disponibili
                            </span>
                            <span class="detail in-use">
                                <%= statistiche.get("veicoliInUso") %> in uso
                            </span>
                        </div>
                    </div>
                    <div class="stat-progress">
                        <% int percentualeUtilizzo = (Integer) statistiche.get("percentualeUtilizzo"); %>
                        <div class="progress-bar">
                            <div class="progress-fill" style="width: <%= percentualeUtilizzo %>%"></div>
                        </div>
                        <span class="progress-text"><%= percentualeUtilizzo %>% utilizzo</span>
                    </div>
                </div>
                
                <div class="stat-card utenti">
                    <div class="stat-icon">
                        <i class="fas fa-users"></i>
                    </div>
                    <div class="stat-content">
                        <h3>Utenti Registrati</h3>
                        <span class="stat-number"><%= statistiche.get("totaleUtenti") %></span>
                        <div class="stat-details">
                            <span class="detail">
                                <%= statistiche.get("totaleClienti") %> clienti
                            </span>
                            <span class="detail">
                                <%= statistiche.get("totaleAdmin") %> admin
                            </span>
                        </div>
                    </div>
                </div>
                
                <div class="stat-card prenotazioni">
                    <div class="stat-icon">
                        <i class="fas fa-calendar-check"></i>
                    </div>
                    <div class="stat-content">
                        <h3>Prenotazioni</h3>
                        <span class="stat-number"><%= statistiche.get("prenotazioniMese") %></span>
                        <div class="stat-details">
                            <span class="detail confirmed">
                                <%= statistiche.get("prenotazioniConfermate") %> confermate
                            </span>
                            <span class="detail in-progress">
                                <%= statistiche.get("prenotazioniInCorso") %> in corso
                            </span>
                        </div>
                    </div>
                </div>
                
                <div class="stat-card fatturato">
                    <div class="stat-icon">
                        <i class="fas fa-euro-sign"></i>
                    </div>
                    <div class="stat-content">
                        <h3>Fatturato Totale</h3>
                        <span class="stat-number">€<%= statistiche.get("fatturatoMese") %></span>
                        <div class="stat-details">
                            <span class="detail positive">
                                <i class="fas fa-arrow-up"></i> Trend positivo
                            </span>
                        </div>
                    </div>
                </div>
            </div>
            
            <!-- Sezioni Principali -->
            <div class="dashboard-sections">
                
                <!-- Gestione Veicoli -->
                <div class="section-card">
                    <div class="section-header">
                        <h2><i class="fas fa-car-side"></i> Gestione Veicoli</h2>
                        <a href="<%= contextPath %>/admin-veicoli" class="btn btn-outline">Visualizza Tutti</a>
                    </div>
                    
                    <div class="section-content">
                        <div class="action-grid">
                            <a href="<%= contextPath %>/admin-veicoli?action=add" class="action-item add">
                                <i class="fas fa-plus-circle"></i>
                                <span>Aggiungi Veicolo</span>
                            </a>
                            
                            <a href="<%= contextPath %>/admin-veicoli" class="action-item manage">
                                <i class="fas fa-edit"></i>
                                <span>Modifica Catalogo</span>
                            </a>
                            
                            <a href="<%= contextPath %>/admin-veicoli?filterDisponibile=false" class="action-item alert">
                                <i class="fas fa-exclamation-triangle"></i>
                                <span>Veicoli Non Disponibili</span>
                            </a>
                        </div>
                        
                        <!-- Statistiche Veicoli per Marca -->
                        <div class="marca-stats">
                            <h4>Veicoli per Marca</h4>
                            <% 
                                Map<String, Integer> veicoliPerMarca = (Map<String, Integer>) statistiche.get("veicoliPerMarca");
                                if (veicoliPerMarca != null && !veicoliPerMarca.isEmpty()) {
                                    for (Map.Entry<String, Integer> entry : veicoliPerMarca.entrySet()) {
                            %>
                                <div class="marca-item">
                                    <span class="marca-name"><%= entry.getKey() %></span>
                                    <span class="marca-count"><%= entry.getValue() %></span>
                                </div>
                            <% 
                                    }
                                } else {
                            %>
                                <p class="no-data">Nessun dato disponibile</p>
                            <% } %>
                        </div>
                    </div>
                </div>
                
                <!-- Gestione Ordini -->
                <div class="section-card">
                    <div class="section-header">
                        <h2><i class="fas fa-clipboard-list"></i> Gestione Ordini</h2>
                        <a href="<%= contextPath %>/admin-ordini" class="btn btn-outline">Visualizza Tutti</a>
                    </div>
                    
                    <div class="section-content">
                        <div class="action-grid">
                            <a href="<%= contextPath %>/admin-ordini?stato=confermata" class="action-item pending">
                                <i class="fas fa-clock"></i>
                                <span>Ordini da Confermare</span>
                            </a>
                            
                            <a href="<%= contextPath %>/admin-ordini?stato=in_corso" class="action-item active">
                                <i class="fas fa-spinner"></i>
                                <span>Ordini in Corso</span>
                            </a>
                            
                            <a href="<%= contextPath %>/admin-ordini?action=stats" class="action-item stats">
                                <i class="fas fa-chart-bar"></i>
                                <span>Statistiche Dettagliate</span>
                            </a>
                        </div>
                        
                        <!-- Prenotazioni Recenti -->
                        <div class="recent-orders">
                            <h4>Prenotazioni Recenti</h4>
                            <% 
                                List<Prenotazione> prenotazioniRecenti = (List<Prenotazione>) statistiche.get("prenotazioniRecenti");
                                if (prenotazioniRecenti != null && !prenotazioniRecenti.isEmpty()) {
                                    for (Prenotazione p : prenotazioniRecenti) {
                            %>
                                <div class="order-item">
                                    <div class="order-info">
                                        <span class="order-id">#<%= p.getId() %></span>
                                        <span class="order-vehicle"><%= p.getVeicoloTarga() %></span>
                                    </div>
                                    <div class="order-details">
                                        <span class="order-status status-<%= p.getStato() %>">
                                            <%= p.getStato() %>
                                        </span>
                                        <% if (p.getPrezzoTotale() != null) { %>
                                            <span class="order-price">€<%= p.getPrezzoTotale() %></span>
                                        <% } %>
                                    </div>
                                </div>
                            <% 
                                    }
                                } else {
                            %>
                                <p class="no-data">Nessuna prenotazione recente</p>
                            <% } %>
                        </div>
                    </div>
                </div>
                
                <!-- Azioni Rapide -->
                <div class="section-card">
                    <div class="section-header">
                        <h2><i class="fas fa-bolt"></i> Azioni Rapide</h2>
                    </div>
                    
                    <div class="section-content">
                        <div class="quick-action-grid">
                            <form method="post" action="<%= contextPath %>/admin-dashboard" class="quick-form">
                                <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                                <input type="hidden" name="action" value="backup-database">
                                <button type="submit" class="quick-action-btn backup">
                                    <i class="fas fa-download"></i>
                                    <span>Backup Database</span>
                                </button>
                            </form>
                            
                            <a href="<%= contextPath %>/admin-ordini?action=export" class="quick-action-btn export">
                                <i class="fas fa-file-csv"></i>
                                <span>Esporta Ordini</span>
                            </a>
                            
                            <a href="<%= contextPath %>/admin-veicoli?filterDisponibile=true" class="quick-action-btn available">
                                <i class="fas fa-check-circle"></i>
                                <span>Veicoli Disponibili</span>
                            </a>
                            
                            <a href="<%= contextPath %>/catalogo" class="quick-action-btn preview">
                                <i class="fas fa-eye"></i>
                                <span>Anteprima Sito</span>
                            </a>
                        </div>
                    </div>
                </div>
            </div>
            
            <!-- System Info -->
            <div class="system-info">
                <div class="info-item">
                    <i class="fas fa-server"></i>
                    <span>Sistema EasyRide v1.0</span>
                </div>
                <div class="info-item">
                    <i class="fas fa-calendar"></i>
                    <span>Ultimo accesso: <%= dataOggi %></span>
                </div>
                <div class="info-item">
                    <i class="fas fa-user-shield"></i>
                    <span>Amministratore: <%= nomeAdmin %></span>
                </div>
            </div>
        </div>
    </main>
    
    <!-- Include Footer -->
    <jsp:include page="/WEB-INF/includes/footer.jsp" />
</body>
</html>