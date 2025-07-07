<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="it.easyridedb.model.*" %>
<%@ page import="java.util.List" %>

<%
    // Recupera dati dalla request
    List<Veicolo> veicoli = (List<Veicolo>) request.getAttribute("veicoli");
    List<Terminal> terminals = (List<Terminal>) request.getAttribute("terminals");
    List<String> marche = (List<String>) request.getAttribute("marche");
    List<String> carburanti = (List<String>) request.getAttribute("carburanti");
    String contextPath = request.getContextPath();
    String csrfToken = (String) request.getAttribute("csrfToken");
    
    // Filtri correnti
    String filterMarca = (String) request.getAttribute("filterMarca");
    String filterCarburante = (String) request.getAttribute("filterCarburante");
    String filterDisponibile = (String) request.getAttribute("filterDisponibile");
    
    // Paginazione
    Integer currentPage = (Integer) request.getAttribute("currentPage");
    Integer pageSize = (Integer) request.getAttribute("pageSize");
    Integer totalVeicoli = (Integer) request.getAttribute("totalVeicoli");
    
    if (currentPage == null) currentPage = 1;
    if (pageSize == null) pageSize = 10;
    if (totalVeicoli == null) totalVeicoli = 0;
    
    // Messaggi
    String successMessage = (String) request.getAttribute("successMessage");
    String errorMessage = (String) request.getAttribute("errorMessage");
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>EasyRide - Gestione Veicoli</title>
    
    <!-- CSS -->
    <link rel="stylesheet" href="<%= contextPath %>/styles/common.css">
    <link rel="stylesheet" href="<%= contextPath %>/styles/admin-veicoli-list.css">
    
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body class="admin-body">
    <!-- Include Header -->
    <jsp:include page="/WEB-INF/includes/header.jsp" />
    
    <main class="admin-main">
        <div class="container">
            <!-- Header Veicoli -->
            <div class="page-header">
                <div class="header-content">
                    <div class="breadcrumb">
                        <a href="<%= contextPath %>/admin-dashboard">
                            <i class="fas fa-tachometer-alt"></i> Dashboard
                        </a>
                        <span class="separator">/</span>
                        <span class="current">Gestione Veicoli</span>
                    </div>
                    
                    <h1><i class="fas fa-car"></i> Gestione Veicoli</h1>
                    <p class="page-description">Gestisci il catalogo dei veicoli disponibili per il noleggio</p>
                </div>
                
                <div class="header-actions">
                    <a href="<%= contextPath %>/admin-dashboard" class="btn btn-outline">
                        <i class="fas fa-arrow-left"></i> Dashboard
                    </a>
                    <a href="<%= contextPath %>/admin-veicoli?action=add" class="btn btn-primary">
                        <i class="fas fa-plus"></i> Nuovo Veicolo
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
                    <i class="fas fa-car"></i>
                    <div class="stat-info">
                        <span class="stat-number"><%= totalVeicoli %></span>
                        <span class="stat-label">Veicoli Totali</span>
                    </div>
                </div>
                
                <div class="stat-item">
                    <i class="fas fa-check-circle"></i>
                    <div class="stat-info">
                        <span class="stat-number">
                            <% 
                                int disponibili = 0;
                                if (veicoli != null) {
                                    for (Veicolo v : veicoli) {
                                        if (v.isDisponibile()) disponibili++;
                                    }
                                }
                            %>
                            <%= disponibili %>
                        </span>
                        <span class="stat-label">Disponibili</span>
                    </div>
                </div>
                
                <div class="stat-item">
                    <i class="fas fa-times-circle"></i>
                    <div class="stat-info">
                        <span class="stat-number"><%= totalVeicoli - disponibili %></span>
                        <span class="stat-label">Non Disponibili</span>
                    </div>
                </div>
                
                <div class="stat-item">
                    <i class="fas fa-percentage"></i>
                    <div class="stat-info">
                        <span class="stat-number">
                            <%= totalVeicoli > 0 ? String.format("%.1f", (disponibili * 100.0 / totalVeicoli)) : "0" %>%
                        </span>
                        <span class="stat-label">Tasso Disponibilità</span>
                    </div>
                </div>
            </div>
            
            <!-- Filtri -->
            <div class="filters-section">
                <form method="get" action="<%= contextPath %>/admin-veicoli" class="filters-form">
                    <div class="filter-group">
                        <label for="filterMarca">
                            <i class="fas fa-tags"></i> Marca
                        </label>
                        <select id="filterMarca" name="filterMarca">
                            <option value="">Tutte le marche</option>
                            <% if (marche != null) {
                                for (String marca : marche) { %>
                                <option value="<%= marca %>" 
                                        <%= marca.equals(filterMarca) ? "selected" : "" %>>
                                    <%= marca %>
                                </option>
                            <% }
                            } %>
                        </select>
                    </div>
                    
                    <div class="filter-group">
                        <label for="filterCarburante">
                            <i class="fas fa-gas-pump"></i> Carburante
                        </label>
                        <select id="filterCarburante" name="filterCarburante">
                            <option value="">Tutti i carburanti</option>
                            <% if (carburanti != null) {
                                for (String carburante : carburanti) { %>
                                <option value="<%= carburante %>" 
                                        <%= carburante.equals(filterCarburante) ? "selected" : "" %>>
                                    <%= carburante %>
                                </option>
                            <% }
                            } %>
                        </select>
                    </div>
                    
                    <div class="filter-group">
                        <label for="filterDisponibile">
                            <i class="fas fa-toggle-on"></i> Disponibilità
                        </label>
                        <select id="filterDisponibile" name="filterDisponibile">
                            <option value="">Tutti</option>
                            <option value="true" <%= "true".equals(filterDisponibile) ? "selected" : "" %>>Disponibili</option>
                            <option value="false" <%= "false".equals(filterDisponibile) ? "selected" : "" %>>Non Disponibili</option>
                        </select>
                    </div>
                    
                    <div class="filter-group">
                        <label for="pageSize">
                            <i class="fas fa-list"></i> Elementi per pagina
                        </label>
                        <select id="pageSize" name="pageSize">
                            <option value="10" <%= pageSize == 10 ? "selected" : "" %>>10</option>
                            <option value="25" <%= pageSize == 25 ? "selected" : "" %>>25</option>
                            <option value="50" <%= pageSize == 50 ? "selected" : "" %>>50</option>
                        </select>
                    </div>
                    
                    <div class="filter-actions">
                        <button type="submit" class="btn btn-primary">
                            <i class="fas fa-filter"></i> Filtra
                        </button>
                        <a href="<%= contextPath %>/admin-veicoli" class="btn btn-outline">
                            <i class="fas fa-times"></i> Reset
                        </a>
                    </div>
                </form>
            </div>
            
            <!-- Lista Veicoli -->
            <div class="vehicles-section">
                <div class="section-header">
                    <h2>
                        <i class="fas fa-list"></i> 
                        Lista Veicoli 
                        <span class="count">(<%= veicoli != null ? veicoli.size() : 0 %> risultati)</span>
                    </h2>
                    
                    <div class="bulk-actions">
                        <button type="button" class="btn btn-outline" onclick="selectAllVehicles()">
                            <i class="fas fa-check-square"></i> Seleziona Tutti
                        </button>
                        <button type="button" class="btn btn-danger" onclick="deleteSelected()">
                            <i class="fas fa-trash"></i> Elimina Selezionati
                        </button>
                    </div>
                </div>
                
                <% if (veicoli != null && !veicoli.isEmpty()) { %>
                    
                    <!-- Vista Griglia -->
                    <div class="view-toggle">
                        <button type="button" class="toggle-btn active" onclick="switchView('grid')">
                            <i class="fas fa-th-large"></i> Griglia
                        </button>
                        <button type="button" class="toggle-btn" onclick="switchView('table')">
                            <i class="fas fa-list"></i> Tabella
                        </button>
                    </div>
                    
                    <!-- Vista Griglia -->
                    <div id="gridView" class="vehicles-grid">
                        <% for (Veicolo veicolo : veicoli) { %>
                            <div class="vehicle-card" data-vehicle-id="<%= veicolo.getTarga() %>">
                                <div class="vehicle-checkbox">
                                    <input type="checkbox" name="selectedVehicles" 
                                           value="<%= veicolo.getTarga() %>" class="vehicle-checkbox-input">
                                </div>
                                
                                <div class="vehicle-image">
                                    <% if (veicolo.getImmagineUrl() != null && !veicolo.getImmagineUrl().isEmpty()) { %>
                                        <img src="<%= veicolo.getImmagineUrl() %>" alt="<%= veicolo.getMarca() %> <%= veicolo.getModello() %>">
                                    <% } else { %>
                                        <div class="vehicle-placeholder">
                                            <i class="fas fa-car"></i>
                                        </div>
                                    <% } %>
                                    
                                    <div class="vehicle-status">
                                        <span class="status-badge <%= veicolo.isDisponibile() ? "available" : "unavailable" %>">
                                            <%= veicolo.isDisponibile() ? "Disponibile" : "Non Disponibile" %>
                                        </span>
                                    </div>
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
                                    </div>
                                    
                                    <div class="vehicle-price">
                                        <span class="price-amount">€<%= veicolo.getPrezzoPerGiorno() %></span>
                                        <span class="price-unit">/giorno</span>
                                    </div>
                                </div>
                                
                                <div class="vehicle-actions">
                                    <a href="<%= contextPath %>/admin-veicoli?action=view&targa=<%= veicolo.getTarga() %>" 
                                       class="btn btn-small btn-outline" title="Visualizza Dettagli">
                                        <i class="fas fa-eye"></i>
                                    </a>
                                    
                                    <a href="<%= contextPath %>/admin-veicoli?action=edit&targa=<%= veicolo.getTarga() %>" 
                                       class="btn btn-small btn-secondary" title="Modifica">
                                        <i class="fas fa-edit"></i>
                                    </a>
                                    
                                    <form method="post" action="<%= contextPath %>/admin-veicoli" style="display: inline;">
                                        <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                                        <input type="hidden" name="action" value="toggle-disponibilita">
                                        <input type="hidden" name="targa" value="<%= veicolo.getTarga() %>">
                                        <button type="submit" 
                                                class="btn btn-small <%= veicolo.isDisponibile() ? "btn-warning" : "btn-success" %>"
                                                title="<%= veicolo.isDisponibile() ? "Disabilita" : "Abilita" %>">
                                            <i class="fas fa-<%= veicolo.isDisponibile() ? "pause" : "play" %>"></i>
                                        </button>
                                    </form>
                                    
                                    <form method="post" action="<%= contextPath %>/admin-veicoli" style="display: inline;">
                                        <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="targa" value="<%= veicolo.getTarga() %>">
                                        <button type="submit" 
                                                class="btn btn-small btn-danger" 
                                                title="Elimina"
                                                onclick="return confirm('Eliminare il veicolo <%= veicolo.getTarga() %>?')">
                                            <i class="fas fa-trash"></i>
                                        </button>
                                    </form>
                                </div>
                            </div>
                        <% } %>
                    </div>
                    
                    <!-- Vista Tabella -->
                    <div id="tableView" class="vehicles-table-container" style="display: none;">
                        <table class="vehicles-table">
                            <thead>
                                <tr>
                                    <th class="checkbox-col">
                                        <input type="checkbox" id="selectAll" onchange="toggleAllVehicles(this)">
                                    </th>
                                    <th>Immagine</th>
                                    <th>Targa</th>
                                    <th>Marca/Modello</th>
                                    <th>Tipo</th>
                                    <th>Carburante</th>
                                    <th>Trasmissione</th>
                                    <th>Prezzo/Giorno</th>
                                    <th>Disponibilità</th>
                                    <th>Terminal</th>
                                    <th class="actions-col">Azioni</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% for (Veicolo veicolo : veicoli) { %>
                                    <tr class="vehicle-row" data-vehicle-id="<%= veicolo.getTarga() %>">
                                        <td class="checkbox-col">
                                            <input type="checkbox" name="selectedVehicles" 
                                                   value="<%= veicolo.getTarga() %>" class="vehicle-checkbox">
                                        </td>
                                        
                                        <td class="image-col">
                                            <div class="table-vehicle-image">
                                                <% if (veicolo.getImmagineUrl() != null && !veicolo.getImmagineUrl().isEmpty()) { %>
                                                    <img src="<%= veicolo.getImmagineUrl() %>" alt="<%= veicolo.getMarca() %> <%= veicolo.getModello() %>">
                                                <% } else { %>
                                                    <div class="image-placeholder">
                                                        <i class="fas fa-car"></i>
                                                    </div>
                                                <% } %>
                                            </div>
                                        </td>
                                        
                                        <td class="plate-col">
                                            <a href="<%= contextPath %>/admin-veicoli?action=view&targa=<%= veicolo.getTarga() %>" 
                                               class="plate-link">
                                                <%= veicolo.getTarga() %>
                                            </a>
                                        </td>
                                        
                                        <td class="brand-model-col">
                                            <div class="brand-model">
                                                <span class="brand"><%= veicolo.getMarca() %></span>
                                                <span class="model"><%= veicolo.getModello() %></span>
                                            </div>
                                        </td>
                                        
                                        <td class="type-col">
                                            <%= veicolo.getTipo() != null ? veicolo.getTipo() : "N/A" %>
                                        </td>
                                        
                                        <td class="fuel-col">
                                            <span class="fuel-badge fuel-<%= veicolo.getCarburante() %>">
                                                <%= veicolo.getCarburante() %>
                                            </span>
                                        </td>
                                        
                                        <td class="transmission-col">
                                            <%= veicolo.getTrasmissione() %>
                                        </td>
                                        
                                        <td class="price-col">
                                            <span class="price-amount">€<%= veicolo.getPrezzoPerGiorno() %></span>
                                        </td>
                                        
                                        <td class="availability-col">
                                            <span class="status-badge <%= veicolo.isDisponibile() ? "available" : "unavailable" %>">
                                                <%= veicolo.isDisponibile() ? "Disponibile" : "Non Disponibile" %>
                                            </span>
                                        </td>
                                        
                                        <td class="terminal-col">
                                            <% 
                                                String nomeTerminal = "N/A";
                                                if (terminals != null && veicolo.getTerminalId() > 0) {
                                                    for (Terminal terminal : terminals) {
                                                        if (terminal.getId() == veicolo.getTerminalId()) {
                                                            nomeTerminal = terminal.getNome();
                                                            break;
                                                        }
                                                    }
                                                }
                                            %>
                                            <%= nomeTerminal %>
                                        </td>
                                        
                                        <td class="actions-col">
                                            <div class="table-actions">
                                                <a href="<%= contextPath %>/admin-veicoli?action=view&targa=<%= veicolo.getTarga() %>" 
                                                   class="btn btn-small btn-outline" title="Visualizza">
                                                    <i class="fas fa-eye"></i>
                                                </a>
                                                
                                                <a href="<%= contextPath %>/admin-veicoli?action=edit&targa=<%= veicolo.getTarga() %>" 
                                                   class="btn btn-small btn-secondary" title="Modifica">
                                                    <i class="fas fa-edit"></i>
                                                </a>
                                                
                                                <div class="dropdown">
                                                    <button class="btn btn-small btn-outline dropdown-toggle" 
                                                            onclick="toggleActionDropdown('<%= veicolo.getTarga() %>')">
                                                        <i class="fas fa-ellipsis-v"></i>
                                                    </button>
                                                    <div class="dropdown-menu" id="actions-<%= veicolo.getTarga() %>">
                                                        <form method="post" action="<%= contextPath %>/admin-veicoli" class="dropdown-form">
                                                            <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                                                            <input type="hidden" name="action" value="toggle-disponibilita">
                                                            <input type="hidden" name="targa" value="<%= veicolo.getTarga() %>">
                                                            <button type="submit" class="dropdown-item">
                                                                <i class="fas fa-<%= veicolo.isDisponibile() ? "pause" : "play" %>"></i>
                                                                <%= veicolo.isDisponibile() ? "Disabilita" : "Abilita" %>
                                                            </button>
                                                        </form>
                                                        
                                                        <form method="post" action="<%= contextPath %>/admin-veicoli" class="dropdown-form">
                                                            <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                                                            <input type="hidden" name="action" value="delete">
                                                            <input type="hidden" name="targa" value="<%= veicolo.getTarga() %>">
                                                            <button type="submit" class="dropdown-item danger"
                                                                    onclick="return confirm('Eliminare il veicolo <%= veicolo.getTarga() %>?')">
                                                                <i class="fas fa-trash"></i> Elimina
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
                    <div class="no-vehicles">
                        <div class="no-vehicles-icon">
                            <i class="fas fa-car-crash"></i>
                        </div>
                        <h3>Nessun veicolo trovato</h3>
                        <p>Non ci sono veicoli che corrispondono ai filtri selezionati.</p>
                        <div class="no-vehicles-actions">
                            <a href="<%= contextPath %>/admin-veicoli?action=add" class="btn btn-primary">
                                <i class="fas fa-plus"></i> Aggiungi Primo Veicolo
                            </a>
                            <a href="<%= contextPath %>/admin-veicoli" class="btn btn-outline">
                                <i class="fas fa-refresh"></i> Mostra Tutti
                            </a>
                        </div>
                    </div>
                <% } %>
            </div>
            
            <!-- Paginazione -->
            <% if (veicoli != null && !veicoli.isEmpty() && totalVeicoli > pageSize) { %>
                <div class="pagination-section">
                    <div class="pagination">
                        <% 
                            int totalPages = (int) Math.ceil((double) totalVeicoli / pageSize);
                            int startPage = Math.max(1, currentPage - 2);
                            int endPage = Math.min(totalPages, currentPage + 2);
                        %>
                        
                        <!-- Pulsante Precedente -->
                        <% if (currentPage > 1) { %>
                            <a href="<%= contextPath %>/admin-veicoli?page=<%= currentPage - 1 %>&pageSize=<%= pageSize %>" 
                               class="pagination-btn">
                                <i class="fas fa-chevron-left"></i> Precedente
                            </a>
                        <% } %>
                        
                        <!-- Numeri pagina -->
                        <% for (int i = startPage; i <= endPage; i++) { %>
                            <a href="<%= contextPath %>/admin-veicoli?page=<%= i %>&pageSize=<%= pageSize %>" 
                               class="pagination-number <%= i == currentPage ? "active" : "" %>">
                                <%= i %>
                            </a>
                        <% } %>
                        
                        <!-- Pulsante Successivo -->
                        <% if (currentPage < totalPages) { %>
                            <a href="<%= contextPath %>/admin-veicoli?page=<%= currentPage + 1 %>&pageSize=<%= pageSize %>" 
                               class="pagination-btn">
                                Successivo <i class="fas fa-chevron-right"></i>
                            </a>
                        <% } %>
                    </div>
                    
                    <div class="pagination-info">
                        Pagina <%= currentPage %> di <%= totalPages %> 
                        (<%= totalVeicoli %> veicoli totali)
                    </div>
                </div>
            <% } %>
        </div>
    </main>
    
    <!-- Include Footer -->
    <jsp:include page="/WEB-INF/includes/footer.jsp" />
    
    <!-- JavaScript -->
    <script src="<%= contextPath %>/scripts/admin-veicoli-list.js"></script>
</body>
</html>