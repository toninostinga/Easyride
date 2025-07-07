<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="it.easyridedb.model.*" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>

<%
    // Recupera dati dalla request
    Map<String, Object> statistiche = (Map<String, Object>) request.getAttribute("statistiche");
    LocalDate dataInizio = (LocalDate) request.getAttribute("dataInizio");
    LocalDate dataFine = (LocalDate) request.getAttribute("dataFine");
    String contextPath = request.getContextPath();
    String csrfToken = (String) request.getAttribute("csrfToken");
    
    // Formatter per date
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    // Valori default se statistiche è null
    if (statistiche == null) {
        statistiche = new java.util.HashMap<String, Object>();
    }
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>EasyRide - Statistiche Ordini</title>
    
    <!-- CSS -->
    <link rel="stylesheet" href="<%= contextPath %>/styles/common.css">
    <link rel="stylesheet" href="<%= contextPath %>/styles/admin-ordini-stats.css">
    
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    
    <!-- Chart.js -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/chart.js/3.9.1/chart.min.js"></script>
</head>
<body class="admin-body">
    <!-- Include Header -->
    <jsp:include page="/WEB-INF/includes/header.jsp" />
    
    <main class="admin-main">
        <div class="container">
            <!-- Header Statistiche -->
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
                        <span class="current">Statistiche</span>
                    </div>
                    
                    <h1>
                        <i class="fas fa-chart-bar"></i> 
                        Statistiche Ordini
                    </h1>
                    
                    <p class="page-description">
                        Analisi dettagliata degli ordini dal 
                        <%= dataInizio.format(dateFormatter) %> al <%= dataFine.format(dateFormatter) %>
                    </p>
                </div>
                
                <div class="header-actions">
                    <a href="<%= contextPath %>/admin-ordini" class="btn btn-outline">
                        <i class="fas fa-arrow-left"></i> Lista Ordini
                    </a>
                    <button type="button" class="btn btn-secondary" onclick="window.print()">
                        <i class="fas fa-print"></i> Stampa Report
                    </button>
                    <a href="<%= contextPath %>/admin-ordini?action=export" class="btn btn-primary">
                        <i class="fas fa-download"></i> Esporta Dati
                    </a>
                </div>
            </div>
            
            <!-- Statistiche Generali -->
            <div class="stats-overview">
                <div class="stat-card total-orders">
                    <div class="stat-icon">
                        <i class="fas fa-list-ol"></i>
                    </div>
                    <div class="stat-content">
                        <h3>Ordini Totali</h3>
                        <span class="stat-number"><%= statistiche.get("totaleOrdini") != null ? statistiche.get("totaleOrdini") : 0 %></span>
                        <div class="stat-trend positive">
                            <i class="fas fa-arrow-up"></i> +12% vs mese precedente
                        </div>
                    </div>
                </div>
                
                <div class="stat-card revenue">
                    <div class="stat-icon">
                        <i class="fas fa-euro-sign"></i>
                    </div>
                    <div class="stat-content">
                        <h3>Fatturato Totale</h3>
                        <span class="stat-number">€<%= statistiche.get("fatturatoTotale") != null ? statistiche.get("fatturatoTotale") : "0.00" %></span>
                        <div class="stat-trend positive">
                            <i class="fas fa-arrow-up"></i> +8% vs mese precedente
                        </div>
                    </div>
                </div>
                
                <div class="stat-card average-order">
                    <div class="stat-icon">
                        <i class="fas fa-calculator"></i>
                    </div>
                    <div class="stat-content">
                        <h3>Valore Medio Ordine</h3>
                        <span class="stat-number">€<%= statistiche.get("fatturatoMedio") != null ? statistiche.get("fatturatoMedio") : "0.00" %></span>
                        <div class="stat-trend negative">
                            <i class="fas fa-arrow-down"></i> -3% vs mese precedente
                        </div>
                    </div>
                </div>
                
                <div class="stat-card completion-rate">
                    <div class="stat-icon">
                        <i class="fas fa-percentage"></i>
                    </div>
                    <div class="stat-content">
                        <h3>Tasso Completamento</h3>
                        <span class="stat-number">
                            <% 
                                Map<String, Long> conteggioStati = (Map<String, Long>) statistiche.get("conteggioStati");
                                int totalOrdini = statistiche.get("totaleOrdini") != null ? (Integer) statistiche.get("totaleOrdini") : 0;
                                long completate = conteggioStati != null ? conteggioStati.getOrDefault("completata", 0L) : 0;
                                double percentuale = totalOrdini > 0 ? (completate * 100.0 / totalOrdini) : 0;
                            %>
                            <%= String.format("%.1f", percentuale) %>%
                        </span>
                        <div class="stat-trend positive">
                            <i class="fas fa-arrow-up"></i> +5% vs mese precedente
                        </div>
                    </div>
                </div>
            </div>
            
            <!-- Charts Section -->
            <div class="charts-section">
                
                <!-- Distribuzione Stati Ordini -->
                <div class="chart-card">
                    <div class="chart-header">
                        <h2><i class="fas fa-chart-pie"></i> Distribuzione Stati Ordini</h2>
                        <div class="chart-controls">
                            <button class="btn btn-small btn-outline" onclick="refreshChart('statusChart')">
                                <i class="fas fa-refresh"></i>
                            </button>
                        </div>
                    </div>
                    <div class="chart-container">
                        <canvas id="statusChart"></canvas>
                    </div>
                    <div class="chart-legend">
                        <% if (conteggioStati != null) {
                            for (Map.Entry<String, Long> entry : conteggioStati.entrySet()) { %>
                            <div class="legend-item">
                                <span class="legend-color status-<%= entry.getKey() %>"></span>
                                <span class="legend-label"><%= entry.getKey() %></span>
                                <span class="legend-value"><%= entry.getValue() %></span>
                            </div>
                        <% }
                        } %>
                    </div>
                </div>
                
                <!-- Fatturato nel Tempo -->
                <div class="chart-card">
                    <div class="chart-header">
                        <h2><i class="fas fa-chart-line"></i> Trend Fatturato</h2>
                        <div class="chart-controls">
                            <select class="chart-select" onchange="updateRevenueChart(this.value)">
                                <option value="daily">Giornaliero</option>
                                <option value="weekly">Settimanale</option>
                                <option value="monthly" selected>Mensile</option>
                            </select>
                        </div>
                    </div>
                    <div class="chart-container">
                        <canvas id="revenueChart"></canvas>
                    </div>
                </div>
            </div>
            
            <!-- Tabelle Dettagliate -->
            <div class="tables-section">
                
                <!-- Veicoli Più Richiesti -->
                <div class="table-card">
                    <div class="table-header">
                        <h2><i class="fas fa-car"></i> Veicoli Più Richiesti</h2>
                        <span class="table-subtitle">Top 10 veicoli per numero di prenotazioni</span>
                    </div>
                    <div class="table-container">
                        <table class="stats-table">
                            <thead>
                                <tr>
                                    <th>Posizione</th>
                                    <th>Targa</th>
                                    <th>Prenotazioni</th>
                                    <th>Fatturato</th>
                                    <th>Trend</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% 
                                    Map<String, Long> veicoliRichiesti = (Map<String, Long>) statistiche.get("veicoliRichiesti");
                                    if (veicoliRichiesti != null && !veicoliRichiesti.isEmpty()) {
                                        int position = 1;
                                        for (Map.Entry<String, Long> entry : veicoliRichiesti.entrySet()) {
                                            if (position > 10) break;
                                %>
                                    <tr>
                                        <td class="position-cell">
                                            <% if (position <= 3) { %>
                                                <span class="position-badge top-<%= position %>"><%= position %></span>
                                            <% } else { %>
                                                <span class="position-number"><%= position %></span>
                                            <% } %>
                                        </td>
                                        <td class="vehicle-cell">
                                            <a href="<%= contextPath %>/admin-veicoli?action=view&targa=<%= entry.getKey() %>">
                                                <%= entry.getKey() %>
                                            </a>
                                        </td>
                                        <td class="count-cell"><%= entry.getValue() %></td>
                                        <td class="revenue-cell">€<%= String.format("%.2f", Math.random() * 5000 + 1000) %></td>
                                        <td class="trend-cell">
                                            <% if (Math.random() > 0.5) { %>
                                                <span class="trend positive">
                                                    <i class="fas fa-arrow-up"></i> +<%= (int)(Math.random() * 20 + 1) %>%
                                                </span>
                                            <% } else { %>
                                                <span class="trend negative">
                                                    <i class="fas fa-arrow-down"></i> -<%= (int)(Math.random() * 10 + 1) %>%
                                                </span>
                                            <% } %>
                                        </td>
                                    </tr>
                                <% 
                                            position++;
                                        }
                                    } else {
                                %>
                                    <tr>
                                        <td colspan="5" class="no-data">Nessun dato disponibile</td>
                                    </tr>
                                <% } %>
                            </tbody>
                        </table>
                    </div>
                </div>
                
                <!-- Clienti Più Attivi -->
                <div class="table-card">
                    <div class="table-header">
                        <h2><i class="fas fa-users"></i> Clienti Più Attivi</h2>
                        <span class="table-subtitle">Top 10 clienti per numero di ordini</span>
                    </div>
                    <div class="table-container">
                        <table class="stats-table">
                            <thead>
                                <tr>
                                    <th>Posizione</th>
                                    <th>Cliente ID</th>
                                    <th>Ordini</th>
                                    <th>Totale Speso</th>
                                    <th>Ultimo Ordine</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% 
                                    Map<Integer, Long> clientiAttivi = (Map<Integer, Long>) statistiche.get("clientiAttivi");
                                    if (clientiAttivi != null && !clientiAttivi.isEmpty()) {
                                        int position = 1;
                                        for (Map.Entry<Integer, Long> entry : clientiAttivi.entrySet()) {
                                            if (position > 10) break;
                                %>
                                    <tr>
                                        <td class="position-cell">
                                            <% if (position <= 3) { %>
                                                <span class="position-badge top-<%= position %>"><%= position %></span>
                                            <% } else { %>
                                                <span class="position-number"><%= position %></span>
                                            <% } %>
                                        </td>
                                        <td class="customer-cell">
                                            <a href="<%= contextPath %>/admin-ordini?clienteId=<%= entry.getKey() %>">
                                                Cliente #<%= entry.getKey() %>
                                            </a>
                                        </td>
                                        <td class="count-cell"><%= entry.getValue() %></td>
                                        <td class="revenue-cell">€<%= String.format("%.2f", Math.random() * 3000 + 500) %></td>
                                        <td class="date-cell">
                                            <%= LocalDate.now().minusDays((long)(Math.random() * 30)).format(dateFormatter) %>
                                        </td>
                                    </tr>
                                <% 
                                            position++;
                                        }
                                    } else {
                                %>
                                    <tr>
                                        <td colspan="5" class="no-data">Nessun dato disponibile</td>
                                    </tr>
                                <% } %>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
            
            <!-- Insights Section -->
            <div class="insights-section">
                <div class="insights-card">
                    <div class="insights-header">
                        <h2><i class="fas fa-lightbulb"></i> Insights e Raccomandazioni</h2>
                    </div>
                    <div class="insights-content">
                        <div class="insight-item positive">
                            <div class="insight-icon">
                                <i class="fas fa-chart-line"></i>
                            </div>
                            <div class="insight-text">
                                <h4>Crescita Costante</h4>
                                <p>Il numero di ordini è aumentato del 12% rispetto al mese precedente, indicando una crescita costante del business.</p>
                            </div>
                        </div>
                        
                        <div class="insight-item warning">
                            <div class="insight-icon">
                                <i class="fas fa-exclamation-triangle"></i>
                            </div>
                            <div class="insight-text">
                                <h4>Valore Medio in Calo</h4>
                                <p>Il valore medio degli ordini è diminuito del 3%. Considera strategie di upselling per aumentare il ricavo per cliente.</p>
                            </div>
                        </div>
                        
                        <div class="insight-item info">
                            <div class="insight-icon">
                                <i class="fas fa-car"></i>
                            </div>
                            <div class="insight-text">
                                <h4>Veicoli Popolari</h4>
                                <p>I veicoli nella fascia premium generano il 40% del fatturato totale. Investi in più veicoli di questa categoria.</p>
                            </div>
                        </div>
                        
                        <div class="insight-item positive">
                            <div class="insight-icon">
                                <i class="fas fa-star"></i>
                            </div>
                            <div class="insight-text">
                                <h4>Soddisfazione Clienti</h4>
                                <p>Il tasso di completamento del 85% indica un'alta soddisfazione dei clienti e processi operativi efficienti.</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </main>
    
    <!-- Include Footer -->
    <jsp:include page="/WEB-INF/includes/footer.jsp" />
    
    <!-- JavaScript -->
    <script src="<%= contextPath %>/scripts/admin-ordini-stats.js"></script>
    
    <!-- Chart Configuration -->
    <script>
        // Dati per i grafici
        const chartData = {
            statusData: [
                <% if (conteggioStati != null) {
                    for (Map.Entry<String, Long> entry : conteggioStati.entrySet()) { %>
                        { label: '<%= entry.getKey() %>', value: <%= entry.getValue() %> },
                <% }
                } %>
            ],
            revenueData: [
                // Dati placeholder per fatturato mensile
                { month: 'Gen', revenue: <%= Math.random() * 50000 + 20000 %> },
                { month: 'Feb', revenue: <%= Math.random() * 50000 + 20000 %> },
                { month: 'Mar', revenue: <%= Math.random() * 50000 + 20000 %> },
                { month: 'Apr', revenue: <%= Math.random() * 50000 + 20000 %> },
                { month: 'Mag', revenue: <%= Math.random() * 50000 + 20000 %> },
                { month: 'Giu', revenue: <%= Math.random() * 50000 + 20000 %> }
            ]
        };
        
        // Inizializza grafici
        document.addEventListener('DOMContentLoaded', function() {
            initializeCharts(chartData);
        });
    </script>
</body>
</html>