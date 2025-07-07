<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="it.easyridedb.model.*" %>
<%@ page import="java.text.SimpleDateFormat" %>

<%
    // Recupera dati dalla request
    Prenotazione ordine = (Prenotazione) request.getAttribute("ordine");
    Utente cliente = (Utente) request.getAttribute("cliente");
    Veicolo veicolo = (Veicolo) request.getAttribute("veicolo");
    String contextPath = request.getContextPath();
    String csrfToken = (String) request.getAttribute("csrfToken");
    
    // Formatter per date - USO SOLO SIMPLEDATEFORMAT
    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
    SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    
    // Controllo se ordine esiste
    if (ordine == null) {
        response.sendRedirect(contextPath + "/admin-ordini?error=Ordine non trovato");
        return;
    }
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>EasyRide - Dettagli Ordine #<%= ordine.getId() %></title>
    
    <!-- CSS -->
    <link rel="stylesheet" href="<%= contextPath %>/styles/common.css">
    <link rel="stylesheet" href="<%= contextPath %>/styles/admin-ordini-view.css">
    
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
                        <a href="<%= contextPath %>/admin-ordini">
                            <i class="fas fa-clipboard-list"></i> Ordini
                        </a>
                        <span class="separator">/</span>
                        <span class="current">Ordine #<%= ordine.getId() %></span>
                    </div>
                    
                    <h1>
                        <i class="fas fa-receipt"></i> 
                        Dettagli Ordine #<%= ordine.getId() %>
                    </h1>
                    
                    <div class="order-status-header">
                        <span class="status-badge status-<%= ordine.getStato() %>">
                            <%= ordine.getStato() %>
                        </span>
                        <span class="order-date">
                            <i class="fas fa-calendar"></i>
                            Creato il <%= ordine != null && ordine.getDataPrenotazione() != null ? 
                                dateTimeFormatter.format(ordine.getDataPrenotazione()) : "N/A" %>
                        </span>
                    </div>
                </div>
                
                <div class="header-actions">
                    <a href="<%= contextPath %>/admin-ordini" class="btn btn-outline">
                        <i class="fas fa-arrow-left"></i> Torna alla Lista
                    </a>
                    <button type="button" class="btn btn-secondary" onclick="window.print()">
                        <i class="fas fa-print"></i> Stampa
                    </button>
                </div>
            </div>
            
            <!-- Contenuto Principale -->
            <div class="order-content">
                
                <!-- Informazioni Ordine -->
                <div class="info-section">
                    <div class="section-card order-info-card">
                        <div class="card-header">
                            <h2><i class="fas fa-info-circle"></i> Informazioni Ordine</h2>
                        </div>
                        
                        <div class="card-content">
                            <div class="info-grid">
                                <div class="info-item">
                                    <label>ID Ordine</label>
                                    <value>#<%= ordine.getId() %></value>
                                </div>
                                
                                <div class="info-item">
                                    <label>Stato</label>
                                    <value>
                                        <span class="status-badge status-<%= ordine.getStato() %>">
                                            <%= ordine.getStato() %>
                                        </span>
                                    </value>
                                </div>
                                
                                <div class="info-item">
                                    <label>Data Prenotazione</label>
                                    <value>
                                        <%= ordine.getDataPrenotazione() != null ? 
                                            dateTimeFormatter.format(ordine.getDataPrenotazione()) : "N/A" %>
                                    </value>
                                </div>
                                
                                <div class="info-item">
                                    <label>Data Ritiro</label>
                                    <value>
                                        <i class="fas fa-calendar-plus"></i>
                                        <%= ordine.getDataRitiro() != null ? 
                                            dateTimeFormatter.format(ordine.getDataRitiro()) : "N/A" %>
                                    </value>
                                </div>
                                
                                <div class="info-item">
                                    <label>Data Restituzione</label>
                                    <value>
                                        <i class="fas fa-calendar-minus"></i>
                                        <%= ordine.getDataRestituzione() != null ? 
                                            dateTimeFormatter.format(ordine.getDataRestituzione()) : "N/A" %>
                                    </value>
                                </div>
                                
                                <div class="info-item">
                                    <label>Durata Noleggio</label>
                                    <value>
                                        <% 
                                            long giorni = 1; // Default
                                            if (ordine.getDataRitiro() != null && ordine.getDataRestituzione() != null) {
                                                long diffInMillis = ordine.getDataRestituzione().getTime() - ordine.getDataRitiro().getTime();
                                                giorni = diffInMillis / (24 * 60 * 60 * 1000);
                                                if (giorni <= 0) giorni = 1;
                                            }
                                        %>
                                        <i class="fas fa-clock"></i>
                                        <%= giorni %> giorn<%= giorni == 1 ? "o" : "i" %>
                                    </value>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Informazioni Cliente -->
                    <div class="section-card customer-info-card">
                        <div class="card-header">
                            <h2><i class="fas fa-user"></i> Informazioni Cliente</h2>
                        </div>
                        
                        <div class="card-content">
                            <% if (cliente != null) { %>
                                <div class="customer-profile">
                                    <div class="customer-avatar">
                                        <i class="fas fa-user-circle"></i>
                                    </div>
                                    
                                    <div class="customer-details">
                                        <h3><%= cliente.getNome() %> <%= cliente.getCognome() %></h3>
                                        
                                        <div class="customer-contacts">
                                            <div class="contact-item">
                                                <i class="fas fa-envelope"></i>
                                                <a href="mailto:<%= cliente.getEmail() %>"><%= cliente.getEmail() %></a>
                                            </div>
                                            
                                            <div class="contact-item">
                                                <i class="fas fa-user-tag"></i>
                                                <span>Cliente ID: <%= cliente.getId() %></span>
                                            </div>
                                            
                                            <div class="contact-item">
                                                <i class="fas fa-calendar-alt"></i>
                                                <span>Registrato: <%= cliente.getDataRegistrazione() != null ? 
                                                    dateFormatter.format(cliente.getDataRegistrazione()) : "N/A" %></span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            <% } else { %>
                                <div class="no-data">
                                    <i class="fas fa-user-times"></i>
                                    <p>Informazioni cliente non disponibili</p>
                                </div>
                            <% } %>
                        </div>
                    </div>
                </div>
                
                <!-- Informazioni Veicolo -->
                <div class="vehicle-section">
                    <div class="section-card vehicle-info-card">
                        <div class="card-header">
                            <h2><i class="fas fa-car"></i> Veicolo Noleggiato</h2>
                        </div>
                        
                        <div class="card-content">
                            <% if (veicolo != null) { %>
                                <div class="vehicle-details">
                                    <div class="vehicle-image">
                                        <% if (veicolo.getImmagineUrl() != null && !veicolo.getImmagineUrl().isEmpty()) { %>
                                            <img src="<%= veicolo.getImmagineUrl() %>" alt="<%= veicolo.getMarca() %> <%= veicolo.getModello() %>">
                                        <% } else { %>
                                            <div class="vehicle-placeholder">
                                                <i class="fas fa-car"></i>
                                            </div>
                                        <% } %>
                                    </div>
                                    
                                    <div class="vehicle-info">
                                        <h3><%= veicolo.getMarca() %> <%= veicolo.getModello() %></h3>
                                        <div class="vehicle-plate">Targa: <%= veicolo.getTarga() %></div>
                                        
                                        <div class="vehicle-specs">
                                            <div class="spec-item">
                                                <i class="fas fa-tag"></i>
                                                <span><%= veicolo.getTipo() != null ? veicolo.getTipo() : "N/A" %></span>
                                            </div>
                                            
                                            <div class="spec-item">
                                                <i class="fas fa-gas-pump"></i>
                                                <span><%= veicolo.getCarburante() %></span>
                                            </div>
                                            
                                            <div class="spec-item">
                                                <i class="fas fa-cogs"></i>
                                                <span><%= veicolo.getTrasmissione() %></span>
                                            </div>
                                            
                                            <div class="spec-item">
                                                <i class="fas fa-euro-sign"></i>
                                                <span>€<%= veicolo.getPrezzoPerGiorno() %>/giorno</span>
                                            </div>
                                            
                                            <div class="spec-item">
                                                <i class="fas fa-circle availability-<%= veicolo.isDisponibile() ? "available" : "unavailable" %>"></i>
                                                <span><%= veicolo.isDisponibile() ? "Disponibile" : "Non Disponibile" %></span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            <% } else { %>
                                <div class="no-data">
                                    <i class="fas fa-car-crash"></i>
                                    <p>Informazioni veicolo non disponibili</p>
                                    <small>Targa: <%= ordine.getVeicoloTarga() %></small>
                                </div>
                            <% } %>
                        </div>
                    </div>
                </div>
                
                <!-- Dettagli Prezzo -->
                <div class="pricing-section">
                    <div class="section-card pricing-card">
                        <div class="card-header">
                            <h2><i class="fas fa-calculator"></i> Dettagli Prezzo</h2>
                        </div>
                        
                        <div class="card-content">
                            <div class="pricing-breakdown">
                                <% if (veicolo != null && ordine.getPrezzoTotale() != null) { %>
                                    <div class="pricing-item">
                                        <span>Prezzo giornaliero</span>
                                        <span>€<%= veicolo.getPrezzoPerGiorno() %></span>
                                    </div>
                                    
                                    <div class="pricing-item">
                                        <span>Numero giorni</span>
                                        <span><%= giorni %></span>
                                    </div>
                                    
                                    <div class="pricing-item">
                                        <span>Subtotale veicolo</span>
                                        <span>€<%= veicolo.getPrezzoPerGiorno().multiply(java.math.BigDecimal.valueOf(giorni)) %></span>
                                    </div>
                                    
                                    <!-- Placeholder per optional -->
                                    <!-- <div class="pricing-item">
                                        <span>Optional</span>
                                        <span>€0.00</span>
                                    </div> -->
                                    
                                    <div class="pricing-separator"></div>
                                    
                                    <div class="pricing-total">
                                        <span>Totale Finale</span>
                                        <span>€<%= ordine.getPrezzoTotale() %></span>
                                    </div>
                                <% } else { %>
                                    <div class="pricing-total">
                                        <span>Totale Ordine</span>
                                        <span>
                                            <% if (ordine.getPrezzoTotale() != null) { %>
                                                €<%= ordine.getPrezzoTotale() %>
                                            <% } else { %>
                                                <span class="price-na">Non disponibile</span>
                                            <% } %>
                                        </span>
                                    </div>
                                <% } %>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- Azioni Amministratore -->
                <div class="actions-section">
                    <div class="section-card actions-card">
                        <div class="card-header">
                            <h2><i class="fas fa-tools"></i> Azioni Amministratore</h2>
                        </div>
                        
                        <div class="card-content">
                            <div class="admin-actions">
                                
                                <!-- Cambio Stato -->
                                <div class="action-group">
                                    <h4><i class="fas fa-edit"></i> Cambia Stato Ordine</h4>
                                    <div class="status-actions">
                                        <form method="post" action="<%= contextPath %>/admin-ordini" class="status-form">
                                            <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                                            <input type="hidden" name="action" value="update-stato">
                                            <input type="hidden" name="ordineId" value="<%= ordine.getId() %>">
                                            
                                            <div class="status-buttons">
                                                <% if (!"confermata".equals(ordine.getStato())) { %>
                                                    <button type="submit" name="nuovoStato" value="confermata" 
                                                            class="btn btn-warning">
                                                        <i class="fas fa-check"></i> Conferma
                                                    </button>
                                                <% } %>
                                                
                                                <% if (!"in_corso".equals(ordine.getStato())) { %>
                                                    <button type="submit" name="nuovoStato" value="in_corso" 
                                                            class="btn btn-info">
                                                        <i class="fas fa-play"></i> Metti in Corso
                                                    </button>
                                                <% } %>
                                                
                                                <% if (!"completata".equals(ordine.getStato())) { %>
                                                    <button type="submit" name="nuovoStato" value="completata" 
                                                            class="btn btn-success">
                                                        <i class="fas fa-check-circle"></i> Completa
                                                    </button>
                                                <% } %>
                                                
                                                <% if (!"annullata".equals(ordine.getStato())) { %>
                                                    <button type="submit" name="nuovoStato" value="annullata" 
                                                            class="btn btn-danger"
                                                            onclick="return confirm('Sei sicuro di voler annullare questo ordine?')">
                                                        <i class="fas fa-times"></i> Annulla
                                                    </button>
                                                <% } %>
                                            </div>
                                        </form>
                                    </div>
                                </div>
                                
                                <!-- Altri Azioni -->
                                <div class="action-group">
                                    <h4><i class="fas fa-cogs"></i> Altre Azioni</h4>
                                    <div class="other-actions">
                                        <% if (cliente != null) { %>
                                            <a href="mailto:<%= cliente.getEmail() %>?subject=Ordine EasyRide %23<%= ordine.getId() %>" 
                                               class="btn btn-outline">
                                                <i class="fas fa-envelope"></i> Contatta Cliente
                                            </a>
                                        <% } %>
                                        
                                        <% if (veicolo != null) { %>
                                            <a href="<%= contextPath %>/admin-veicoli?action=view&targa=<%= veicolo.getTarga() %>" 
                                               class="btn btn-outline">
                                                <i class="fas fa-car"></i> Dettagli Veicolo
                                            </a>
                                        <% } %>
                                        
                                        <button type="button" class="btn btn-outline" onclick="window.print()">
                                            <i class="fas fa-print"></i> Stampa Ordine
                                        </button>
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