<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="it.easyridedb.model.*" %>

<%
    // Recupera dati dalla request
    Veicolo veicolo = (Veicolo) request.getAttribute("veicolo");
    Terminal terminal = (Terminal) request.getAttribute("terminal");
    String contextPath = request.getContextPath();
    String csrfToken = (String) request.getAttribute("csrfToken");
    
    // Controllo se veicolo esiste
    if (veicolo == null) {
        response.sendRedirect(contextPath + "/admin-veicoli?error=Veicolo non trovato");
        return;
    }
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>EasyRide - Dettagli Veicolo <%= veicolo.getTarga() %></title>
    
    <!-- CSS -->
    <link rel="stylesheet" href="<%= contextPath %>/styles/common.css">
    <link rel="stylesheet" href="<%= contextPath %>/styles/admin-veicoli-view.css">
    
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body class="admin-body">
    <!-- Include Header -->
    <jsp:include page="/WEB-INF/includes/header.jsp" />
    
    <main class="admin-main">
        <div class="container">
            <!-- Header Dettagli -->
            <div class="page-header">
                <div class="header-content">
                    <div class="breadcrumb">
                        <a href="<%= contextPath %>/admin-dashboard">
                            <i class="fas fa-tachometer-alt"></i> Dashboard
                        </a>
                        <span class="separator">/</span>
                        <a href="<%= contextPath %>/admin-veicoli">
                            <i class="fas fa-car"></i> Veicoli
                        </a>
                        <span class="separator">/</span>
                        <span class="current">Dettagli <%= veicolo.getTarga() %></span>
                    </div>
                    
                    <h1>
                        <i class="fas fa-car"></i> 
                        <%= veicolo.getMarca() %> <%= veicolo.getModello() %>
                    </h1>
                    
                    <div class="vehicle-status-header">
                        <span class="status-badge <%= veicolo.isDisponibile() ? "available" : "unavailable" %>">
                            <%= veicolo.isDisponibile() ? "Disponibile" : "Non Disponibile" %>
                        </span>
                        <span class="vehicle-plate">Targa: <%= veicolo.getTarga() %></span>
                    </div>
                </div>
                
                <div class="header-actions">
                    <a href="<%= contextPath %>/admin-veicoli" class="btn btn-outline">
                        <i class="fas fa-arrow-left"></i> Torna alla Lista
                    </a>
                    <a href="<%= contextPath %>/admin-veicoli?action=edit&targa=<%= veicolo.getTarga() %>" class="btn btn-secondary">
                        <i class="fas fa-edit"></i> Modifica
                    </a>
                    <button type="button" class="btn btn-primary" onclick="window.print()">
                        <i class="fas fa-print"></i> Stampa
                    </button>
                </div>
            </div>
            
            <!-- Contenuto Principale -->
            <div class="vehicle-content">
                
                <!-- Sezione Immagine e Info Base -->
                <div class="main-section">
                    <div class="vehicle-showcase">
                        <div class="vehicle-image-large">
                            <% if (veicolo.getImmagineUrl() != null && !veicolo.getImmagineUrl().isEmpty()) { %>
                                <img src="<%= veicolo.getImmagineUrl() %>" alt="<%= veicolo.getMarca() %> <%= veicolo.getModello() %>">
                            <% } else { %>
                                <div class="image-placeholder">
                                    <i class="fas fa-car"></i>
                                    <span>Nessuna immagine disponibile</span>
                                </div>
                            <% } %>
                            
                            <div class="image-overlay">
                                <div class="overlay-content">
                                    <h2><%= veicolo.getMarca() %> <%= veicolo.getModello() %></h2>
                                    <div class="vehicle-price">
                                        <span class="price-amount">€<%= veicolo.getPrezzoPerGiorno() %></span>
                                        <span class="price-unit">/giorno</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="quick-specs">
                            <div class="spec-item">
                                <i class="fas fa-id-card"></i>
                                <div>
                                    <span class="spec-label">Targa</span>
                                    <span class="spec-value"><%= veicolo.getTarga() %></span>
                                </div>
                            </div>
                            
                            <div class="spec-item">
                                <i class="fas fa-gas-pump"></i>
                                <div>
                                    <span class="spec-label">Carburante</span>
                                    <span class="spec-value"><%= veicolo.getCarburante() %></span>
                                </div>
                            </div>
                            
                            <div class="spec-item">
                                <i class="fas fa-cogs"></i>
                                <div>
                                    <span class="spec-label">Trasmissione</span>
                                    <span class="spec-value"><%= veicolo.getTrasmissione() %></span>
                                </div>
                            </div>
                            
                            <div class="spec-item">
                                <i class="fas fa-shapes"></i>
                                <div>
                                    <span class="spec-label">Tipo</span>
                                    <span class="spec-value"><%= veicolo.getTipo() != null ? veicolo.getTipo() : "N/A" %></span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- Dettagli Completi -->
                <div class="details-section">
                    
                    <!-- Informazioni Tecniche -->
                    <div class="detail-card">
                        <div class="card-header">
                            <h3><i class="fas fa-cogs"></i> Informazioni Tecniche</h3>
                        </div>
                        <div class="card-content">
                            <div class="detail-grid">
                                <div class="detail-item">
                                    <label>Marca</label>
                                    <value><%= veicolo.getMarca() %></value>
                                </div>
                                
                                <div class="detail-item">
                                    <label>Modello</label>
                                    <value><%= veicolo.getModello() %></value>
                                </div>
                                
                                <div class="detail-item">
                                    <label>Tipo/Categoria</label>
                                    <value><%= veicolo.getTipo() != null ? veicolo.getTipo() : "Non specificato" %></value>
                                </div>
                                
                                <div class="detail-item">
                                    <label>Carburante</label>
                                    <value>
                                        <span class="fuel-badge fuel-<%= veicolo.getCarburante() %>">
                                            <%= veicolo.getCarburante() %>
                                        </span>
                                    </value>
                                </div>
                                
                                <div class="detail-item">
                                    <label>Trasmissione</label>
                                    <value><%= veicolo.getTrasmissione() %></value>
                                </div>
                                
                                <div class="detail-item">
                                    <label>Prezzo per Giorno</label>
                                    <value>
                                        <span class="price-highlight">€<%= veicolo.getPrezzoPerGiorno() %></span>
                                    </value>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Disponibilità e Localizzazione -->
                    <div class="detail-card">
                        <div class="card-header">
                            <h3><i class="fas fa-map-marker-alt"></i> Disponibilità e Localizzazione</h3>
                        </div>
                        <div class="card-content">
                            <div class="availability-info">
                                <div class="availability-status">
                                    <div class="status-indicator <%= veicolo.isDisponibile() ? "available" : "unavailable" %>">
                                        <i class="fas fa-<%= veicolo.isDisponibile() ? "check-circle" : "times-circle" %>"></i>
                                    </div>
                                    <div class="status-text">
                                        <h4><%= veicolo.isDisponibile() ? "Veicolo Disponibile" : "Veicolo Non Disponibile" %></h4>
                                        <p>
                                            <%= veicolo.isDisponibile() ? 
                                                "Questo veicolo è attualmente disponibile per nuove prenotazioni" : 
                                                "Questo veicolo non è disponibile per nuove prenotazioni" %>
                                        </p>
                                    </div>
                                </div>
                                
                                <% if (terminal != null) { %>
                                    <div class="terminal-info">
                                        <h4><i class="fas fa-building"></i> Terminal di Appartenenza</h4>
                                        <div class="terminal-details">
                                            <div class="terminal-name"><%= terminal.getNome() %></div>
                                            <% if (terminal.getIndirizzo() != null) { %>
                                                <div class="terminal-address">
                                                    <i class="fas fa-map-marker"></i> <%= terminal.getIndirizzo() %>
                                                </div>
                                            <% } %>
                                            <% if (terminal.getTelefono() != null) { %>
                                                <div class="terminal-contact">
                                                    <i class="fas fa-phone"></i> <%= terminal.getTelefono() %>
                                                </div>
                                            <% } %>
                                            <% if (terminal.getEmail() != null) { %>
                                                <div class="terminal-contact">
                                                    <i class="fas fa-envelope"></i> 
                                                    <a href="mailto:<%= terminal.getEmail() %>"><%= terminal.getEmail() %></a>
                                                </div>
                                            <% } %>
                                        </div>
                                    </div>
                                <% } else { %>
                                    <div class="terminal-info">
                                        <div class="no-terminal">
                                            <i class="fas fa-question-circle"></i>
                                            <span>Nessun terminal specifico assegnato</span>
                                        </div>
                                    </div>
                                <% } %>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Statistiche e Prenotazioni -->
                    <div class="detail-card">
                        <div class="card-header">
                            <h3><i class="fas fa-chart-bar"></i> Statistiche e Prenotazioni</h3>
                        </div>
                        <div class="card-content">
                            <div class="stats-grid">
                                <div class="stat-item">
                                    <div class="stat-icon">
                                        <i class="fas fa-calendar-check"></i>
                                    </div>
                                    <div class="stat-info">
                                        <span class="stat-number">--</span>
                                        <span class="stat-label">Prenotazioni Totali</span>
                                    </div>
                                </div>
                                
                                <div class="stat-item">
                                    <div class="stat-icon">
                                        <i class="fas fa-euro-sign"></i>
                                    </div>
                                    <div class="stat-info">
                                        <span class="stat-number">--</span>
                                        <span class="stat-label">Fatturato Generato</span>
                                    </div>
                                </div>
                                
                                <div class="stat-item">
                                    <div class="stat-icon">
                                        <i class="fas fa-clock"></i>
                                    </div>
                                    <div class="stat-info">
                                        <span class="stat-number">--</span>
                                        <span class="stat-label">Giorni Noleggiati</span>
                                    </div>
                                </div>
                                
                                <div class="stat-item">
                                    <div class="stat-icon">
                                        <i class="fas fa-star"></i>
                                    </div>
                                    <div class="stat-info">
                                        <span class="stat-number">--</span>
                                        <span class="stat-label">Valutazione Media</span>
                                    </div>
                                </div>
                            </div>
                            
                            <div class="booking-notice">
                                <i class="fas fa-info-circle"></i>
                                <span>Le statistiche dettagliate delle prenotazioni saranno implementate in una versione futura</span>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- Azioni Amministratore -->
                <div class="actions-section">
                    <div class="action-card">
                        <div class="card-header">
                            <h3><i class="fas fa-tools"></i> Azioni Amministratore</h3>
                        </div>
                        <div class="card-content">
                            <div class="admin-actions">
                                
                                <!-- Gestione Disponibilità -->
                                <div class="action-group">
                                    <h4><i class="fas fa-toggle-on"></i> Gestione Disponibilità</h4>
                                    <div class="action-buttons">
                                        <form method="post" action="<%= contextPath %>/admin-veicoli" class="action-form">
                                            <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                                            <input type="hidden" name="action" value="toggle-disponibilita">
                                            <input type="hidden" name="targa" value="<%= veicolo.getTarga() %>">
                                            
                                            <% if (veicolo.isDisponibile()) { %>
                                                <button type="submit" class="btn btn-warning" 
                                                        onclick="return confirm('Rendere non disponibile il veicolo <%= veicolo.getTarga() %>?')">
                                                    <i class="fas fa-pause"></i> Disabilita Veicolo
                                                </button>
                                            <% } else { %>
                                                <button type="submit" class="btn btn-success">
                                                    <i class="fas fa-play"></i> Abilita Veicolo
                                                </button>
                                            <% } %>
                                        </form>
                                    </div>
                                </div>
                                
                                <!-- Modifica e Gestione -->
                                <div class="action-group">
                                    <h4><i class="fas fa-edit"></i> Modifica e Gestione</h4>
                                    <div class="action-buttons">
                                        <a href="<%= contextPath %>/admin-veicoli?action=edit&targa=<%= veicolo.getTarga() %>" 
                                           class="btn btn-primary">
                                            <i class="fas fa-edit"></i> Modifica Informazioni
                                        </a>
                                        
                                        <a href="<%= contextPath %>/admin-ordini?veicolo=<%= veicolo.getTarga() %>" 
                                           class="btn btn-secondary">
                                            <i class="fas fa-list"></i> Visualizza Prenotazioni
                                        </a>
                                        
                                        <button type="button" class="btn btn-outline" onclick="window.print()">
                                            <i class="fas fa-print"></i> Stampa Scheda
                                        </button>
                                    </div>
                                </div>
                                
                                <!-- Azioni Pericolose -->
                                <div class="action-group danger-zone">
                                    <h4><i class="fas fa-exclamation-triangle"></i> Zona Pericolosa</h4>
                                    <div class="danger-warning">
                                        <i class="fas fa-warning"></i>
                                        <span>Le azioni seguenti sono irreversibili. Procedi con cautela.</span>
                                    </div>
                                    <div class="action-buttons">
                                        <form method="post" action="<%= contextPath %>/admin-veicoli" class="action-form">
                                            <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="targa" value="<%= veicolo.getTarga() %>">
                                            
                                            <button type="submit" class="btn btn-danger" 
                                                    onclick="return confirm('ATTENZIONE: Eliminare definitivamente il veicolo <%= veicolo.getTarga() %>?\n\nQuesta azione non può essere annullata e rimuoverà il veicolo dal sistema.\n\nDigita ELIMINA per confermare:') && prompt('Digita ELIMINA per confermare:') === 'ELIMINA'">
                                                <i class="fas fa-trash"></i> Elimina Veicolo
                                            </button>
                                        </form>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </main>
    
    <!-- Include Footer -->
    <jsp:include page="/WEB-INF/includes/footer.jsp" />
</body>
</html>