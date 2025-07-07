<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="it.easyridedb.model.*" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="java.text.SimpleDateFormat" %>

<%
    // Recupera dati dalla request
    List<Prenotazione> ordini = (List<Prenotazione>) request.getAttribute("ordini");
    List<Utente> clienti = (List<Utente>) request.getAttribute("clienti");
    Map<String, Object> statistiche = (Map<String, Object>) request.getAttribute("statistiche");
    String contextPath = request.getContextPath();
    String csrfToken = (String) request.getAttribute("csrfToken");
    
    // Filtri correnti
    String dataInizio = (String) request.getAttribute("dataInizio");
    String dataFine = (String) request.getAttribute("dataFine");
    Integer clienteId = (Integer) request.getAttribute("clienteId");
    String stato = (String) request.getAttribute("stato");
    String ordinamento = (String) request.getAttribute("ordinamento");
    
    // Messaggi
    String successMessage = (String) request.getAttribute("successMessage");
    String errorMessage = (String) request.getAttribute("errorMessage");
    
    // Formatter per date - CORRETTO: uso SimpleDateFormat
    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>EasyRide - Gestione Ordini</title>
    
    <!-- CSS -->
    <link rel="stylesheet" href="<%= contextPath %>/styles/common.css">
    <link rel="stylesheet" href="<%= contextPath %>/styles/admin-ordini-list.css">
    
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body class="admin-body">
    <!-- Include Header -->
    <jsp:include page="/WEB-INF/includes/header.jsp" />
    
    <main class="admin-main">
        <div class="container">
            <!-- Header Ordini -->
            <div class="page-header">
                <div class="header-content">
                    <h1><i class="fas fa-clipboard-list"></i> Gestione Ordini</h1>
                    <p class="page-description">Visualizza e gestisci tutti gli ordini del sistema</p>
                </div>
                
                <div class="header-actions">
                    <a href="<%= contextPath %>/admin-dashboard" class="btn btn-outline">
                        <i class="fas fa-arrow-left"></i> Dashboard
                    </a>
                    <a href="<%= contextPath %>/admin-ordini?action=stats" class="btn btn-secondary">
                        <i class="fas fa-chart-bar"></i> Statistiche
                    </a>
                    <a href="<%= contextPath %>/admin-ordini?action=export<%= dataInizio != null ? "&dataInizio=" + dataInizio : "" %><%= dataFine != null ? "&dataFine=" + dataFine : "" %><%= clienteId != null ? "&clienteId=" + clienteId : "" %><%= stato != null ? "&stato=" + stato : "" %>" class="btn btn-primary">
                        <i class="fas fa-download"></i> Esporta CSV
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
            
            <!-- Statistiche Rapide -->
            <div class="stats-summary">
                <div class="stat-item">
                    <i class="fas fa-list-ol"></i>
                    <div class="stat-info">
                        <span class="stat-number"><%= statistiche.get("totaleOrdini") %></span>
                        <span class="stat-label">Ordini Totali</span>
                    </div>
                </div>
                
                <div class="stat-item">
                    <i class="fas fa-euro-sign"></i>
                    <div class="stat-info">
                        <span class="stat-number">€<%= statistiche.get("fatturatoTotale") %></span>
                        <span class="stat-label">Fatturato Totale</span>
                    </div>
                </div>
                
                <div class="stat-item">
                    <i class="fas fa-calculator"></i>
                    <div class="stat-info">
                        <span class="stat-number">€<%= statistiche.get("fatturatoMedio") %></span>
                        <span class="stat-label">Valore Medio</span>
                    </div>
                </div>
                
                <div class="stat-item">
                    <i class="fas fa-chart-pie"></i>
                    <div class="stat-info">
                        <span class="stat-number">
                            <% 
                                Map<String, Long> conteggioStati = (Map<String, Long>) statistiche.get("conteggioStati");
                                long confermate = conteggioStati != null ? conteggioStati.getOrDefault("confermata", 0L) : 0;
                            %>
                            <%= confermate %>
                        </span>
                        <span class="stat-label">Confermate</span>
                    </div>
                </div>
            </div>
            
            <!-- Filtri -->
            <div class="filters-section">
                <form method="get" action="<%= contextPath %>/admin-ordini" class="filters-form">
                    <div class="filter-group">
                        <label for="dataInizio">
                            <i class="fas fa-calendar-alt"></i> Data Inizio
                        </label>
                        <input type="date" id="dataInizio" name="dataInizio" 
                               value="<%= dataInizio != null ? dataInizio : "" %>">
                    </div>
                    
                    <div class="filter-group">
                        <label for="dataFine">
                            <i class="fas fa-calendar-alt"></i> Data Fine
                        </label>
                        <input type="date" id="dataFine" name="dataFine" 
                               value="<%= dataFine != null ? dataFine : "" %>">
                    </div>
                    
                    <div class="filter-group">
                        <label for="clienteId">
                            <i class="fas fa-user"></i> Cliente
                        </label>
                        <select id="clienteId" name="clienteId">
                            <option value="">Tutti i clienti</option>
                            <% if (clienti != null) {
                                for (Utente cliente : clienti) { %>
                                <option value="<%= cliente.getId() %>" 
                                        <%= clienteId != null && clienteId.equals(cliente.getId()) ? "selected" : "" %>>
                                    <%= cliente.getNome() %> <%= cliente.getCognome() %> (<%= cliente.getEmail() %>)
                                </option>
                            <% }
                            } %>
                        </select>
                    </div>
                    
                    <div class="filter-group">
                        <label for="stato">
                            <i class="fas fa-flag"></i> Stato
                        </label>
                        <select id="stato" name="stato">
                            <option value="">Tutti gli stati</option>
                            <option value="confermata" <%= "confermata".equals(stato) ? "selected" : "" %>>Confermata</option>
                            <option value="in_corso" <%= "in_corso".equals(stato) ? "selected" : "" %>>In Corso</option>
                            <option value="completata" <%= "completata".equals(stato) ? "selected" : "" %>>Completata</option>
                            <option value="annullata" <%= "annullata".equals(stato) ? "selected" : "" %>>Annullata</option>
                        </select>
                    </div>
                    
                    <div class="filter-group">
                        <label for="sort">
                            <i class="fas fa-sort"></i> Ordinamento
                        </label>
                        <select id="sort" name="sort">
                            <option value="">Default</option>
                            <option value="data_desc" <%= "data_desc".equals(ordinamento) ? "selected" : "" %>>Data (più recenti)</option>
                            <option value="data_asc" <%= "data_asc".equals(ordinamento) ? "selected" : "" %>>Data (più vecchi)</option>
                            <option value="prezzo_desc" <%= "prezzo_desc".equals(ordinamento) ? "selected" : "" %>>Prezzo (più alto)</option>
                            <option value="prezzo_asc" <%= "prezzo_asc".equals(ordinamento) ? "selected" : "" %>>Prezzo (più basso)</option>
                        </select>
                    </div>
                    
                    <div class="filter-actions">
                        <button type="submit" class="btn btn-primary">
                            <i class="fas fa-filter"></i> Filtra
                        </button>
                        <a href="<%= contextPath %>/admin-ordini" class="btn btn-outline">
                            <i class="fas fa-times"></i> Reset
                        </a>
                    </div>
                </form>
            </div>
            
            <!-- Lista Ordini -->
            <div class="orders-section">
                <div class="section-header">
                    <h2>
                        <i class="fas fa-list"></i> 
                        Lista Ordini 
                        <span class="count">(<%= ordini != null ? ordini.size() : 0 %> risultati)</span>
                    </h2>
                    
                    <div class="bulk-actions">
                        <button type="button" class="btn btn-outline" onclick="selectAllOrders()">
                            <i class="fas fa-check-square"></i> Seleziona Tutti
                        </button>
                        <button type="button" class="btn btn-secondary" onclick="exportSelected()">
                            <i class="fas fa-file-export"></i> Esporta Selezionati
                        </button>
                    </div>
                </div>
                
                <% if (ordini != null && !ordini.isEmpty()) { %>
                    <div class="orders-table-container">
                        <table class="orders-table">
                            <thead>
                                <tr>
                                    <th class="checkbox-col">
                                        <input type="checkbox" id="selectAll" onchange="toggleAllOrders(this)">
                                    </th>
                                    <th>ID</th>
                                    <th>Cliente</th>
                                    <th>Veicolo</th>
                                    <th>Data Ritiro</th>
                                    <th>Data Restituzione</th>
                                    <th>Prezzo</th>
                                    <th>Stato</th>
                                    <th>Data Prenotazione</th>
                                    <th class="actions-col">Azioni</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% for (Prenotazione ordine : ordini) { %>
                                    <tr class="order-row" data-order-id="<%= ordine.getId() %>">
                                        <td class="checkbox-col">
                                            <input type="checkbox" name="selectedOrders" 
                                                   value="<%= ordine.getId() %>" class="order-checkbox">
                                        </td>
                                        
                                        <td class="order-id">
                                            <a href="<%= contextPath %>/admin-ordini?action=view&id=<%= ordine.getId() %>" 
                                               class="order-link">
                                                #<%= ordine.getId() %>
                                            </a>
                                        </td>
                                        
                                        <td class="customer-info">
                                            <% 
                                                // Trova cliente corrispondente
                                                String nomeCliente = "N/A";
                                                String emailCliente = "";
                                                if (clienti != null) {
                                                    for (Utente cliente : clienti) {
                                                        if (cliente.getId() == ordine.getUtenteId()) {
                                                            nomeCliente = cliente.getNome() + " " + cliente.getCognome();
                                                            emailCliente = cliente.getEmail();
                                                            break;
                                                        }
                                                    }
                                                }
                                            %>
                                            <div class="customer-name"><%= nomeCliente %></div>
                                            <% if (!emailCliente.isEmpty()) { %>
                                                <div class="customer-email"><%= emailCliente %></div>
                                            <% } %>
                                        </td>
                                        
                                        <td class="vehicle-info">
                                            <div class="vehicle-plate"><%= ordine.getVeicoloTarga() %></div>
                                        </td>
                                        
                                        <td class="date-info">
                                            <%= ordine.getDataRitiro() != null ? 
                                                dateFormatter.format(ordine.getDataRitiro()) : "N/A" %>
                                        </td>
                                        
                                        <td class="date-info">
                                            <%= ordine.getDataRestituzione() != null ? 
                                                dateFormatter.format(ordine.getDataRestituzione()) : "N/A" %>
                                        </td>
                                        
                                        <td class="price-info">
                                            <% if (ordine.getPrezzoTotale() != null) { %>
                                                <span class="price-amount">€<%= ordine.getPrezzoTotale() %></span>
                                            <% } else { %>
                                                <span class="price-na">N/A</span>
                                            <% } %>
                                        </td>
                                        
                                        <td class="status-info">
                                            <span class="status-badge status-<%= ordine.getStato() %>">
                                                <%= ordine.getStato() %>
                                            </span>
                                        </td>
                                        
                                        <td class="date-info">
                                            <%= ordine.getDataPrenotazione() != null ? 
                                                dateFormatter.format(ordine.getDataPrenotazione()) : "N/A" %>
                                        </td>
                                        
                                        <td class="actions-col">
                                            <div class="action-buttons">
                                                <a href="<%= contextPath %>/admin-ordini?action=view&id=<%= ordine.getId() %>" 
                                                   class="btn btn-small btn-outline" title="Visualizza Dettagli">
                                                    <i class="fas fa-eye"></i>
                                                </a>
                                                
                                                <div class="dropdown">
                                                    <button class="btn btn-small btn-secondary dropdown-toggle" 
                                                            onclick="toggleDropdown(<%= ordine.getId() %>)">
                                                        <i class="fas fa-edit"></i>
                                                    </button>
                                                    <div class="dropdown-menu" id="dropdown-<%= ordine.getId() %>">
                                                        <form method="post" action="<%= contextPath %>/admin-ordini" class="dropdown-form">
                                                            <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                                                            <input type="hidden" name="action" value="update-stato">
                                                            <input type="hidden" name="ordineId" value="<%= ordine.getId() %>">
                                                            
                                                            <button type="submit" name="nuovoStato" value="confermata" 
                                                                    class="dropdown-item">
                                                                <i class="fas fa-check"></i> Conferma
                                                            </button>
                                                            <button type="submit" name="nuovoStato" value="in_corso" 
                                                                    class="dropdown-item">
                                                                <i class="fas fa-play"></i> In Corso
                                                            </button>
                                                            <button type="submit" name="nuovoStato" value="completata" 
                                                                    class="dropdown-item">
                                                                <i class="fas fa-check-circle"></i> Completa
                                                            </button>
                                                            <button type="submit" name="nuovoStato" value="annullata" 
                                                                    class="dropdown-item danger"
                                                                    onclick="return confirm('Annullare questo ordine?')">
                                                                <i class="fas fa-times"></i> Annulla
                                                            </button>
                                                        </form>
                                                    </div>
                                                </div>
                                            </div>
                                        </td>
                                    </tr>
                                <% } %>
                            </tbody>
                        </table>
                    </div>
                    
                <% } else { %>
                    <div class="no-orders">
                        <div class="no-orders-icon">
                            <i class="fas fa-inbox"></i>
                        </div>
                        <h3>Nessun ordine trovato</h3>
                        <p>Non ci sono ordini che corrispondono ai filtri selezionati.</p>
                        <a href="<%= contextPath %>/admin-ordini" class="btn btn-primary">
                            <i class="fas fa-refresh"></i> Mostra Tutti gli Ordini
                        </a>
                    </div>
                <% } %>
            </div>
        </div>
    </main>
    
    <!-- Include Footer -->
    <jsp:include page="/WEB-INF/includes/footer.jsp" />
    
    <!-- JavaScript -->
    <script src="<%= contextPath %>/scripts/admin-ordini-list.js"></script>
</body>
</html>