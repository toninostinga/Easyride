<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="it.easyridedb.model.Veicolo" %>
<%@ page import="it.easyridedb.model.Terminal" %>
<%@ page import="it.easyridedb.model.Utente" %>
<%@ page import="java.util.List" %>
<%@ page import="java.math.BigDecimal" %>
<%@page import="it.easyridedb.model.ImageUtils"%>

<%
    // Recupera dati dalla sessione e request
    Utente utente = (Utente) session.getAttribute("utente");
    String contextPath = request.getContextPath();
    
    // Dati dal servlet CON CONTROLLI NULL E SICUREZZA
    @SuppressWarnings("unchecked")
    List<Veicolo> veicoli = (List<Veicolo>) request.getAttribute("veicoli");
    @SuppressWarnings("unchecked")
    List<String> marche = (List<String>) request.getAttribute("marche");
    @SuppressWarnings("unchecked")
    List<String> tipi = (List<String>) request.getAttribute("tipi");
    @SuppressWarnings("unchecked")
    List<Terminal> terminals = (List<Terminal>) request.getAttribute("terminals");
    
    // Parametri filtri selezionati CON ESCAPE HTML
    String selectedMarca = (String) request.getAttribute("selectedMarca");
    String selectedModello = (String) request.getAttribute("selectedModello");
    String selectedTipo = (String) request.getAttribute("selectedTipo");
    String selectedTerminal = (String) request.getAttribute("selectedTerminal");
    String selectedPrezzoMin = (String) request.getAttribute("selectedPrezzoMin");
    String selectedPrezzoMax = (String) request.getAttribute("selectedPrezzoMax");
    String selectedDisponibili = (String) request.getAttribute("selectedDisponibili");
    String selectedSort = (String) request.getAttribute("selectedSort");
    
    // Messaggi
    String welcomeMessage = (String) request.getAttribute("welcomeMessage");
    String successMessage = (String) request.getAttribute("successMessage");
    String errorMessage = (String) request.getAttribute("errorMessage");
    String csrfToken = (String) request.getAttribute("csrfToken");
    
    // SICUREZZA - INIZIALIZZA LISTE VUOTE SE NULL
    if (veicoli == null) veicoli = new java.util.ArrayList<>();
    if (marche == null) marche = new java.util.ArrayList<>();
    if (tipi == null) tipi = new java.util.ArrayList<>();
    if (terminals == null) terminals = new java.util.ArrayList<>();
    
    // SICUREZZA - CSRF TOKEN DEFAULT
    if (csrfToken == null) csrfToken = "";
    
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
    <title>EasyRide - Catalogo Veicoli</title>
    
    <!-- CSRF Token per JavaScript -->
    <meta name="csrf-token" content="<%= escapeHtml.apply(csrfToken) %>">
    
    <!-- Meta tags per SEO -->
    <meta name="description" content="Catalogo veicoli EasyRide - Trova il veicolo perfetto per il tuo noleggio">
    <meta name="keywords" content="noleggio auto, veicoli, catalogo, EasyRide">
    
    <!-- CSS Esterni -->
    <link rel="stylesheet" href="<%= contextPath %>/styles/common.css">
    <link rel="stylesheet" href="<%= contextPath %>/styles/catalogo.css">
    
    <!-- Preload di risorse critiche -->
    <link rel="preload" href="<%= contextPath %>/scripts/catalogo.js" as="script">
</head>
<body data-user-logged-in="<%= utente != null ? "true" : "false" %>">
    
    <!-- Skip to main content per accessibilità -->
    <a href="#main-content" class="skip-link">Vai al contenuto principale</a>
    
    <!-- Include Header -->
    <jsp:include page="/WEB-INF/includes/header.jsp" />
    
    <main id="main-content">
        <div class="container">
            
            <!-- Messaggi CON SICUREZZA -->
            <% if (welcomeMessage != null && !welcomeMessage.trim().isEmpty()) { %>
                <div class="message welcome" role="alert">
                    <%= escapeHtml.apply(welcomeMessage) %>
                </div>
            <% } %>
            
            <% if (successMessage != null && !successMessage.trim().isEmpty()) { %>
                <div class="message success" role="alert">
                    <%= escapeHtml.apply(successMessage) %>
                </div>
            <% } %>
            
            <% if (errorMessage != null && !errorMessage.trim().isEmpty()) { %>
                <div class="message error" role="alert" aria-live="assertive">
                    <%= escapeHtml.apply(errorMessage) %>
                </div>
            <% } %>
            
            <!-- Sezione Filtri -->
            <section class="filters" aria-label="Filtri di ricerca">
                <h2>🔍 Filtra Veicoli</h2>
                
                <form id="filter-form" method="get" action="<%= contextPath %>/catalogo" novalidate>
                    <!-- Token CSRF nascosto -->
                    <input type="hidden" name="csrfToken" value="<%= escapeHtml.apply(csrfToken) %>">
                    
                    <div class="filter-row">
                        
                        <!-- Marca -->
                        <div class="filter-group">
                            <label for="marca">Marca</label>
                            <select id="marca" name="marca" aria-describedby="marca-help">
                                <option value="">Tutte le marche</option>
                                <% 
                                    if (marche != null && !marche.isEmpty()) {
                                        for (String marca : marche) { 
                                            if (marca != null && !marca.trim().isEmpty()) {
                                                String escapedMarca = escapeHtml.apply(marca);
                                                boolean isSelected = marca.equals(selectedMarca);
                                %>
                                    <option value="<%= escapedMarca %>" <%= isSelected ? "selected" : "" %>>
                                        <%= escapedMarca %>
                                    </option>
                                <% 
                                            }
                                        }
                                    }
                                %>
                            </select>
                            <small id="marca-help" class="help-text">Seleziona una marca specifica</small>
                        </div>
                        
                        <!-- Modello -->
                        <div class="filter-group">
                            <label for="modello">Modello</label>
                            <input type="text" 
                                   id="modello" 
                                   name="modello" 
                                   placeholder="Es. Panda, Golf..." 
                                   value="<%= escapeHtml.apply(selectedModello != null ? selectedModello : "") %>"
                                   maxlength="50"
                                   pattern="[a-zA-Z0-9\s\-_]{0,50}"
                                   aria-describedby="modello-help">
                            <small id="modello-help" class="help-text">Solo lettere, numeri, spazi e trattini</small>
                            <span class="error-message" id="modello-error" role="alert"></span>
                        </div>
                        
                        <!-- Tipo -->
                        <div class="filter-group">
                            <label for="tipo">Tipo Veicolo</label>
                            <select id="tipo" name="tipo" aria-describedby="tipo-help">
                                <option value="">Tutti i tipi</option>
                                <% 
                                    if (tipi != null && !tipi.isEmpty()) {
                                        for (String tipo : tipi) { 
                                            if (tipo != null && !tipo.trim().isEmpty()) {
                                                String escapedTipo = escapeHtml.apply(tipo);
                                                boolean isSelected = tipo.equals(selectedTipo);
                                %>
                                    <option value="<%= escapedTipo %>" <%= isSelected ? "selected" : "" %>>
                                        <%= escapedTipo %>
                                    </option>
                                <% 
                                            }
                                        }
                                    }
                                %>
                            </select>
                            <small id="tipo-help" class="help-text">Categoria del veicolo</small>
                        </div>
                        
                        <!-- Terminal -->
                        <div class="filter-group">
                            <label for="terminal">Terminal</label>
                            <select id="terminal" name="terminal" aria-describedby="terminal-help">
                                <option value="">Tutti i terminal</option>
                                <% 
                                    if (terminals != null && !terminals.isEmpty()) {
                                        for (Terminal terminal : terminals) { 
                                            if (terminal != null) {
                                                String terminalIdStr = String.valueOf(terminal.getId());
                                                String terminalNome = terminal.getNome() != null ? terminal.getNome() : "Terminal " + terminal.getId();
                                                String escapedNome = escapeHtml.apply(terminalNome);
                                                boolean isSelected = terminalIdStr.equals(selectedTerminal);
                                %>
                                    <option value="<%= terminal.getId() %>" <%= isSelected ? "selected" : "" %>>
                                        <%= escapedNome %>
                                    </option>
                                <% 
                                            }
                                        }
                                    }
                                %>
                            </select>
                            <small id="terminal-help" class="help-text">Punto di ritiro/consegna</small>
                        </div>
                        
                    </div>
                    
                    <div class="filter-row">
                        
                        <!-- Prezzo Min -->
                        <div class="filter-group">
                            <label for="prezzoMin">Prezzo Min (€/giorno)</label>
                            <input type="number" 
                                   id="prezzoMin" 
                                   name="prezzoMin" 
                                   min="0" 
                                   max="9999.99"
                                   step="0.01" 
                                   placeholder="0.00"
                                   value="<%= escapeHtml.apply(selectedPrezzoMin != null ? selectedPrezzoMin : "") %>"
                                   aria-describedby="prezzoMin-help"
                                   aria-invalid="false">
                            <small id="prezzoMin-help" class="help-text">Prezzo minimo per giorno</small>
                            <span class="error-message" id="prezzo-min-error" role="alert"></span>
                        </div>
                        
                        <!-- Prezzo Max -->
                        <div class="filter-group">
                            <label for="prezzoMax">Prezzo Max (€/giorno)</label>
                            <input type="number" 
                                   id="prezzoMax" 
                                   name="prezzoMax" 
                                   min="0" 
                                   max="9999.99"
                                   step="0.01" 
                                   placeholder="999.99"
                                   value="<%= escapeHtml.apply(selectedPrezzoMax != null ? selectedPrezzoMax : "") %>"
                                   aria-describedby="prezzoMax-help"
                                   aria-invalid="false">
                            <small id="prezzoMax-help" class="help-text">Prezzo massimo per giorno</small>
                            <span class="error-message" id="prezzo-max-error" role="alert"></span>
                        </div>
                        
                        <!-- Solo Disponibili -->
                        <div class="filter-group">
                            <label class="checkbox-label">
                                <input type="checkbox" 
                                       id="disponibili" 
                                       name="disponibili" 
                                       value="true"
                                       <%= "true".equals(selectedDisponibili) ? "checked" : "" %>
                                       aria-describedby="disponibili-help">
                                Solo veicoli disponibili
                            </label>
                            <small id="disponibili-help" class="help-text">Mostra solo veicoli prenotabili</small>
                        </div>
                        
                        <!-- Pulsanti -->
                        <div class="filter-group filter-actions">
                            <button type="submit" class="filter-btn" aria-describedby="filter-btn-help">
                                🔍 Filtra
                            </button>
                            <a href="<%= contextPath %>/catalogo" 
                               class="clear-btn" 
                               id="clear-filters"
                               role="button"
                               aria-describedby="clear-btn-help">
                                🗑️ Reset
                            </a>
                            <small id="filter-btn-help" class="help-text">Applica i filtri selezionati</small>
                            <small id="clear-btn-help" class="help-text">Rimuovi tutti i filtri</small>
                        </div>
                        
                    </div>
                </form>
            </section>
            
            <!-- Header Risultati -->
            <div class="results-header">
                <div class="results-count" id="results-count" role="status" aria-live="polite">
                    <%= veicoli != null ? veicoli.size() : 0 %> veicoli trovati
                </div>
                
                <div class="sort-container">
                    <label for="sort-select">Ordina per:</label>
                    <select id="sort-select" name="sort" aria-describedby="sort-help">
                        <option value="">Predefinito</option>
                        <option value="prezzo_asc" <%= "prezzo_asc".equals(selectedSort) ? "selected" : "" %>>
                            Prezzo: dal più basso
                        </option>
                        <option value="prezzo_desc" <%= "prezzo_desc".equals(selectedSort) ? "selected" : "" %>>
                            Prezzo: dal più alto
                        </option>
                        <option value="marca" <%= "marca".equals(selectedSort) ? "selected" : "" %>>
                            Marca (A-Z)
                        </option>
                        <option value="modello" <%= "modello".equals(selectedSort) ? "selected" : "" %>>
                            Modello (A-Z)
                        </option>
                    </select>
                    <small id="sort-help" class="help-text">Cambia l'ordine dei risultati</small>
                </div>
            </div>
            
            <!-- Grid Veicoli -->
            <section class="vehicles-grid" aria-label="Elenco veicoli">
                
                <% if (veicoli == null || veicoli.isEmpty()) { %>
                    <!-- Nessun risultato -->
                    <div class="no-results" role="status">
                        <h3>🚫 Nessun veicolo trovato</h3>
                        <p>Prova a modificare i filtri di ricerca o <a href="<%= contextPath %>/catalogo">visualizza tutti i veicoli</a>.</p>
                    </div>
                <% } else { %>
                    <!-- Card Veicoli -->
                    <% 
                        int cardIndex = 0;
                        for (Veicolo veicolo : veicoli) { 
                            if (veicolo != null) {
                                cardIndex++;
                                
                                // Sicurezza: escape di tutti i valori
                                String escapedTarga = escapeHtml.apply(veicolo.getTarga() != null ? veicolo.getTarga() : "");
                                String escapedMarca = escapeHtml.apply(veicolo.getMarca() != null ? veicolo.getMarca() : "N/A");
                                String escapedModello = escapeHtml.apply(veicolo.getModello() != null ? veicolo.getModello() : "N/A");
                                String escapedTipo = escapeHtml.apply(veicolo.getTipo() != null ? veicolo.getTipo() : "N/A");
                                // Gestione sicura immagine
                                String calcolatedImageUrl = ImageUtils.getVehicleImageUrl(
                                    veicolo.getImmagineUrl(), 
                                    veicolo.getTarga(), 
                                    application
                                );
                                String imageAltText = ImageUtils.getImageAltText(
                                    veicolo.getMarca(), 
                                    veicolo.getModello(), 
                                    veicolo.getTarga()
                                );
                                
                                // Trova nome terminal
                                String terminalNome = "N/A";
                                if (terminals != null && !terminals.isEmpty()) {
                                    for (Terminal t : terminals) {
                                        if (t != null && t.getId() == veicolo.getTerminalId()) {
                                            terminalNome = t.getNome() != null ? t.getNome() : "Terminal " + t.getId();
                                            break;
                                        }
                                    }
                                }
                                String escapedTerminal = escapeHtml.apply(terminalNome);
                                
                                // Gestione campi in modo semplice
                                String escapedTrasmissione = escapeHtml.apply(veicolo.getTrasmissioneDisplay() != null ? veicolo.getTrasmissioneDisplay() : "N/A");
                                String escapedCarburante = escapeHtml.apply(veicolo.getCarburanteDisplay() != null ? veicolo.getCarburanteDisplay() : "N/A");
                    %>
                        <article class="vehicle-card" 
                                 data-targa="<%= escapedTarga %>"
                                 role="article"
                                 aria-label="Veicolo <%= cardIndex %>: <%= escapedMarca %> <%= escapedModello %>"
                                 tabindex="0">
                            
                            <!-- Immagine Veicolo -->
                            <<div class="vehicle-image">
                                <img src="<%= contextPath %>/<%= calcolatedImageUrl %>" 
                                     alt="<%= escapeHtml.apply(imageAltText) %>"
                                     class="vehicle-img"
                                     loading="lazy"
                                     onerror="this.src='<%= contextPath %>/images/veicoli/default.jpg'; this.classList.add('error');">
                                
                                <!-- Badge disponibilità -->
                                <div class="availability-badge <%= veicolo.isDisponibile() ? "available" : "unavailable" %>">
                                    <%= veicolo.isDisponibile() ? "Disponibile" : "Non disponibile" %>
                                </div>
                            </div>
                                
                                <!-- Badge disponibilità -->
                                <div class="availability-badge <%= veicolo.isDisponibile() ? "available" : "unavailable" %>">
                                    <%= veicolo.isDisponibile() ? "Disponibile" : "Non disponibile" %>
                                </div>
                            </div>
                            
                            <!-- Contenuto Card -->
                            <div class="vehicle-content">
                                
                                <!-- Titolo -->
                                <div class="vehicle-title">
                                    <h3><%= escapedMarca %> <%= escapedModello %></h3>
                                </div>
                                
                                <!-- Dettagli Veicolo -->
                                <dl class="vehicle-details">
                                    <div class="vehicle-detail">
                                        <dt class="label">Tipo:</dt>
                                        <dd class="value"><%= escapedTipo %></dd>
                                    </div>
                                    <div class="vehicle-detail">
                                        <dt class="label">Targa:</dt>
                                        <dd class="value"><%= escapedTarga %></dd>
                                    </div>
                                    <div class="vehicle-detail">
                                        <dt class="label">Terminal:</dt>
                                        <dd class="value"><%= escapedTerminal %></dd>
                                    </div>
                                    <div class="vehicle-detail">
                                        <dt class="label">Trasmissione:</dt>
                                        <dd class="value"><%= escapedTrasmissione %></dd>
                                    </div>
                                    <div class="vehicle-detail">
                                        <dt class="label">Carburante:</dt>
                                        <dd class="value"><%= escapedCarburante %></dd>
                                    </div>
                                </dl>
                                
                                <!-- Prezzo -->
                                <div class="vehicle-price">
                                    <span class="price-amount">
                                        €<%= veicolo.getPrezzoPerGiorno() != null ? veicolo.getPrezzoPerGiorno() : "0.00" %>
                                    </span>
                                    <span class="price-unit">/giorno</span>
                                </div>
                                
                                <!-- Azioni -->
                                <div class="vehicle-actions">
                                    <button type="button" 
                                            class="btn btn-secondary btn-view-details" 
                                            data-targa="<%= escapedTarga %>"
                                            aria-label="Visualizza dettagli di <%= escapedMarca %> <%= escapedModello %>">
                                        👁️ Dettagli
                                    </button>
                                    
                                    <% if (veicolo.isDisponibile()) { %>
                                        <button type="button" 
                                                class="btn btn-primary btn-add-cart" 
                                                data-targa="<%= escapedTarga %>"
                                                aria-label="Prenota <%= escapedMarca %> <%= escapedModello %>"
                                                <%= utente == null ? "title='Effettua il login per prenotare'" : "" %>>
                                            🛒 Prenota
                                        </button>
                                    <% } else { %>
                                        <button type="button" 
                                                class="btn btn-disabled" 
                                                disabled
                                                aria-label="<%= escapedMarca %> <%= escapedModello %> non disponibile">
                                            ❌ Non disponibile
                                        </button>
                                    <% } %>
                                </div>
                                
                            </div>
                        </article>
                    <% 
                            }
                        } 
                    %>
                <% } %>
                
            </section>
            
            <!-- Paginazione (se implementata nel servlet) -->
            <%
                Integer currentPage = (Integer) request.getAttribute("currentPage");
                Integer totalPages = (Integer) request.getAttribute("totalPages");
                
                if (currentPage != null && totalPages != null && totalPages > 1) {
            %>
                <nav class="pagination" aria-label="Navigazione pagine" role="navigation">
                    <% if (currentPage > 1) { %>
                        <a href="?page=<%= currentPage - 1 %>" class="pagination-link" aria-label="Pagina precedente">
                            « Precedente
                        </a>
                    <% } %>
                    
                    <% 
                        int startPage = Math.max(1, currentPage - 2);
                        int endPage = Math.min(totalPages, currentPage + 2);
                        
                        for (int i = startPage; i <= endPage; i++) {
                            boolean isCurrent = (i == currentPage);
                    %>
                        <% if (isCurrent) { %>
                            <span class="pagination-link current" aria-current="page" aria-label="Pagina corrente <%= i %>">
                                <%= i %>
                            </span>
                        <% } else { %>
                            <a href="?page=<%= i %>" class="pagination-link" aria-label="Vai alla pagina <%= i %>">
                                <%= i %>
                            </a>
                        <% } %>
                    <% } %>
                    
                    <% if (currentPage < totalPages) { %>
                        <a href="?page=<%= currentPage + 1 %>" class="pagination-link" aria-label="Pagina successiva">
                            Successiva »
                        </a>
                    <% } %>
                </nav>
            <% } %>
            
        </div>
    </main>
    
    <!-- Include Footer -->
    <jsp:include page="/WEB-INF/includes/footer.jsp" />
    
    <!-- JavaScript -->
    <script src="<%= contextPath %>/scripts/catalogo.js" defer></script>
    
    <!-- Schema.org structured data per SEO -->
    <script type="application/ld+json">
    {
        "@context": "https://schema.org",
        "@type": "ItemList",
        "name": "Catalogo Veicoli EasyRide",
        "numberOfItems": <%= veicoli != null ? veicoli.size() : 0 %>,
        "itemListElement": [
            <% 
                if (veicoli != null && !veicoli.isEmpty()) {
                    for (int i = 0; i < veicoli.size(); i++) {
                        Veicolo v = veicoli.get(i);
                        if (v != null) {
                            String vMarca = v.getMarca() != null ? v.getMarca() : "";
                            String vModello = v.getModello() != null ? v.getModello() : "";
                            String vTipo = v.getTipo() != null ? v.getTipo() : "N/A";
                            BigDecimal vPrezzo = v.getPrezzoPerGiorno() != null ? v.getPrezzoPerGiorno() : BigDecimal.ZERO;
            %>
            {
                "@type": "Product",
                "position": <%= i + 1 %>,
                "name": "<%= escapeHtml.apply(vMarca + " " + vModello) %>",
                "description": "Veicolo di tipo <%= escapeHtml.apply(vTipo) %>",
                "offers": {
                    "@type": "Offer",
                    "price": "<%= vPrezzo %>",
                    "priceCurrency": "EUR",
                    "availability": "<%= v.isDisponibile() ? "https://schema.org/InStock" : "https://schema.org/OutOfStock" %>"
                }
            }<%= i < veicoli.size() - 1 ? "," : "" %>
            <% 
                        }
                    }
                }
            %>
        ]
    }
    </script>
</body>
</html>