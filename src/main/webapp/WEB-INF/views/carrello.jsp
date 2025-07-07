<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="it.easyridedb.model.*" %>
<%@ page import="java.util.List" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="java.time.format.DateTimeFormatter" %>

<%
    // Recupera dati dalla request
    Carrello carrello = (Carrello) request.getAttribute("carrello");
    List<Optional> tuttiOptional = (List<Optional>) request.getAttribute("tuttiOptional");
    String contextPath = request.getContextPath();
    String csrfToken = (String) request.getAttribute("csrfToken");
    
    // Messaggi
    String successMessage = (String) request.getAttribute("successMessage");
    String errorMessage = (String) request.getAttribute("errorMessage");
    
    // Formatter per date
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    // Utente corrente
    Utente utente = (Utente) session.getAttribute("utente");
    boolean isLoggedIn = utente != null;
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>EasyRide - Il tuo Carrello</title>
    
    <!-- CSS -->
    <link rel="stylesheet" href="<%= contextPath %>/styles/common.css">
    <link rel="stylesheet" href="<%= contextPath %>/styles/carrello.css">
    
    <!-- Font Awesome per le icone -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body>
    <!-- Include Header -->
    <jsp:include page="/WEB-INF/includes/header.jsp" />
    
    <main>
        <div class="container">
            <div class="carrello-header">
                <h1>🛒 Il tuo Carrello</h1>
                <% if (carrello != null && !carrello.isEmpty()) { %>
                    <p class="carrello-summary">
                        <%= carrello.getNumeroItemTotali() %> prenotazioni · 
                        <%= carrello.getNumeroGiorniTotali() %> giorni totali
                    </p>
                <% } %>
            </div>
            
            <!-- Messaggi -->
            <% if (successMessage != null) { %>
                <div class="message success">✅ <%= successMessage %></div>
            <% } %>
            
            <% if (errorMessage != null) { %>
                <div class="message error">❌ <%= errorMessage %></div>
            <% } %>
            
            <!-- Contenuto Carrello -->
            <% if (carrello == null || carrello.isEmpty()) { %>
                <!-- Carrello Vuoto -->
                <div class="carrello-vuoto">
                    <div class="empty-icon">🛒</div>
                    <h2>Il tuo carrello è vuoto</h2>
                    <p>Aggiungi dei veicoli al carrello per iniziare a pianificare il tuo viaggio!</p>
                    <a href="<%= contextPath %>/catalogo" class="btn btn-primary">
                        🚗 Sfoglia Catalogo
                    </a>
                </div>
                
            <% } else { %>
                <!-- Carrello con Items -->
                <div class="carrello-content">
                    
                    <!-- Lista Items -->
                    <div class="carrello-items">
                        <% for (CarrelloItem item : carrello.getItems()) { %>
                            <div class="carrello-item" data-item-id="<%= item.getItemId() %>">
                                
                                <!-- 🚗 IMMAGINE VEICOLO - VERSIONE SEMPLIFICATA E FUNZIONANTE -->
                                <div class="item-image-container" onclick="openVehicleImageZoom(this)" title="Clicca per ingrandire">
                                    <%
                                        // 🔧 LOGICA SEMPLIFICATA PER IL CARICAMENTO IMMAGINI
                                        String targa = item.getTargaVeicolo();
                                        String vehicleName = item.getDescrizioneVeicolo();
                                        
                                        // 1. URL principale basato sulla targa (come hai detto che sono nominate)
                                        String primaryImageUrl = contextPath + "/images/veicoli/" + targa + ".jpg";
                                        
                                        // 2. URL di fallback se presente nel database
                                        String fallbackImageUrl = null;
                                        if (item.getImmagineUrl() != null && !item.getImmagineUrl().trim().isEmpty()) {
                                            String dbUrl = item.getImmagineUrl().trim();
                                            if (dbUrl.startsWith("http")) {
                                                fallbackImageUrl = dbUrl;
                                            } else {
                                                fallbackImageUrl = contextPath + "/" + (dbUrl.startsWith("/") ? dbUrl.substring(1) : dbUrl);
                                            }
                                        }
                                        
                                        // 3. URL di backup finale (immagine placeholder online)
                                        String finalFallbackUrl = "https://picsum.photos/320/180?random=" + Math.abs(targa.hashCode() % 1000);
                                        
                                        // Badge dinamico basato su caratteristiche
                                        String badgeText = "🚗 AUTO";
                                        String badgeGradient = "linear-gradient(135deg, #6c757d, #495057)";
                                        
                                        String carburante = item.getCarburante().toLowerCase();
                                        String trasmissione = item.getTrasmissione().toLowerCase();
                                        
                                        if (carburante.contains("elettrico")) {
                                            badgeText = "⚡ ELETTRICO";
                                            badgeGradient = "linear-gradient(135deg, #28a745, #20c997)";
                                        } else if (carburante.contains("ibrido")) {
                                            badgeText = "🔋 IBRIDO";  
                                            badgeGradient = "linear-gradient(135deg, #17a2b8, #20c997)";
                                        } else if (carburante.contains("diesel")) {
                                            badgeText = "⛽ DIESEL";
                                            badgeGradient = "linear-gradient(135deg, #fd7e14, #e67e22)";
                                        } else if (carburante.contains("benzina")) {
                                            badgeText = "⛽ BENZINA";
                                            badgeGradient = "linear-gradient(135deg, #dc3545, #c82333)";
                                        }
                                        
                                        if (trasmissione.contains("automatica")) {
                                            badgeText += " AUTO";
                                        }
                                    %>
                                    
                                    <!-- Container immagine -->
                                    <div class="vehicle-image-wrapper" style="
                                        position: relative; 
                                        width: 100%; 
                                        height: 200px; 
                                        background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%); 
                                        border-radius: 12px; 
                                        overflow: hidden; 
                                        cursor: zoom-in;
                                        box-shadow: 0 4px 16px rgba(0,0,0,0.1);
                                        transition: all 0.3s ease;
                                    ">
                                        
                                        <!-- Immagine principale -->
                                        <img id="vehicle-img-<%= item.getItemId() %>" 
                                             src="<%= primaryImageUrl %>"
                                             alt="<%= vehicleName %> - Targa <%= targa %>"
                                             data-targa="<%= targa %>"
                                             data-fallback1="<%= fallbackImageUrl %>"
                                             data-fallback2="<%= finalFallbackUrl %>"
                                             style="
                                                width: 100%; 
                                                height: 100%; 
                                                object-fit: cover; 
                                                transition: transform 0.3s ease;
                                                opacity: 1;
                                             "
                                             onload="
                                                console.log('✅ Immagine caricata per <%= targa %>:', this.src);
                                                this.style.opacity = '1';
                                                this.nextElementSibling.style.display = 'none';
                                             "
                                             onerror="
                                                console.log('❌ Errore caricamento per <%= targa %>:', this.src);
                                                
                                                // Primo fallback: URL dal database
                                                if (this.dataset.fallback1 && this.dataset.fallback1 !== 'null' && this.src !== this.dataset.fallback1) {
                                                    console.log('🔄 Tentativo fallback 1 per <%= targa %>:', this.dataset.fallback1);
                                                    this.src = this.dataset.fallback1;
                                                    return;
                                                }
                                                
                                                // Secondo fallback: immagine online
                                                if (this.src !== this.dataset.fallback2) {
                                                    console.log('🔄 Tentativo fallback 2 per <%= targa %>:', this.dataset.fallback2);
                                                    this.src = this.dataset.fallback2;
                                                    return;
                                                }
                                                
                                                // Tutti i fallback falliti: mostra placeholder
                                                console.log('⚠️ Tutti i fallback falliti per <%= targa %>, mostro placeholder');
                                                this.style.display = 'none';
                                                this.nextElementSibling.style.display = 'flex';
                                             "
                                             loading="lazy">
                                        
                                        <!-- Placeholder di emergenza -->
                                        <div class="image-placeholder" style="
                                            display: none;
                                            position: absolute;
                                            top: 0; left: 0; right: 0; bottom: 0;
                                            background: linear-gradient(135deg, #e9ecef, #dee2e6);
                                            align-items: center;
                                            justify-content: center;
                                            flex-direction: column;
                                            color: #6c757d;
                                            font-size: 0.9rem;
                                        ">
                                            <i class="fas fa-car" style="font-size: 2rem; margin-bottom: 0.5rem; opacity: 0.5;"></i>
                                            <span style="font-weight: 600;"><%= vehicleName %></span>
                                            <small style="opacity: 0.7; margin-top: 0.25rem;">Targa: <%= targa %></small>
                                        </div>
                                        
                                        <!-- Badge tipo veicolo -->
                                        <div class="vehicle-badge" style="
                                            position: absolute;
                                            top: 12px;
                                            left: 12px;
                                            background: <%= badgeGradient %>;
                                            color: white;
                                            padding: 6px 12px;
                                            border-radius: 20px;
                                            font-size: 0.75rem;
                                            font-weight: 700;
                                            z-index: 10;
                                            box-shadow: 0 2px 12px rgba(0,0,0,0.3);
                                            backdrop-filter: blur(10px);
                                            text-shadow: 0 1px 2px rgba(0,0,0,0.2);
                                            text-transform: uppercase;
                                            letter-spacing: 0.5px;
                                        ">
                                            <%= badgeText %>
                                        </div>
                                        
                                        <!-- Hover overlay -->
                                        <div class="image-hover-overlay" style="
                                            position: absolute;
                                            top: 0; left: 0; right: 0; bottom: 0;
                                            background: rgba(0,0,0,0.1);
                                            opacity: 0;
                                            transition: opacity 0.3s ease;
                                            display: flex;
                                            align-items: center;
                                            justify-content: center;
                                            color: white;
                                            font-size: 1.2rem;
                                        ">
                                            <i class="fas fa-search-plus"></i>
                                        </div>
                                    </div>
                                </div>
                                
                                <!-- Dettagli Item -->
                                <div class="item-details">
                                    <div class="item-header">
                                        <h3><%= item.getDescrizioneVeicolo() %></h3>
                                        <span class="item-targa">Targa: <%= item.getTargaVeicolo() %></span>
                                    </div>
                                    
                                    <div class="item-info">
                                        <div class="info-row">
                                            <i class="fas fa-calendar-alt info-icon"></i>
                                            <span class="info-label">Periodo:</span>
                                            <span class="info-value">
                                                <%= item.getDataRitiro().format(dateFormatter) %> - 
                                                <%= item.getDataRestituzione().format(dateFormatter) %>
                                                (<%= item.getNumeroGiorni() %> giorni)
                                            </span>
                                        </div>
                                        
                                        <div class="info-row">
                                            <i class="fas fa-map-marker-alt info-icon"></i>
                                            <span class="info-label">Ritiro:</span>
                                            <span class="info-value"><%= item.getNomeTerminalRitiro() %></span>
                                        </div>
                                        
                                        <div class="info-row">
                                            <i class="fas fa-map-marker-alt info-icon"></i>
                                            <span class="info-label">Restituzione:</span>
                                            <span class="info-value"><%= item.getNomeTerminalRestituzione() %></span>
                                        </div>
                                        
                                        <div class="info-row">
                                            <i class="fas fa-cog info-icon"></i>
                                            <span class="info-label">Caratteristiche:</span>
                                            <span class="info-value">
                                                <%= item.getCarburante() %> · <%= item.getTrasmissione() %>
                                            </span>
                                        </div>
                                    </div>
                                    
                                    <!-- Optional -->
                                    <div class="item-optional">
                                        <h4><i class="fas fa-plus-circle"></i> Optional Selezionati</h4>
                                        <form method="post" action="<%= contextPath %>/carrello" class="optional-form">
                                            <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                                            <input type="hidden" name="action" value="aggiorna-optional">
                                            <input type="hidden" name="itemId" value="<%= item.getItemId() %>">
                                            
                                            <div class="optional-grid">
                                                <% if (tuttiOptional != null) {
                                                    for (Optional opt : tuttiOptional) {
                                                        boolean isSelected = item.contieneTipoOptional(opt.getId());
                                                %>
                                                    <label class="optional-item">
                                                        <input type="checkbox" name="optionalIds" value="<%= opt.getId() %>" 
                                                               <%= isSelected ? "checked" : "" %>>
                                                        <span class="optional-name"><%= opt.getNome() %></span>
                                                        <span class="optional-price">+€<%= opt.getPrezzoExtra() %>/giorno</span>
                                                    </label>
                                                <% }
                                                } %>
                                            </div>
                                            
                                            <button type="submit" class="btn btn-small btn-secondary">
                                                <i class="fas fa-save"></i> Aggiorna Optional
                                            </button>
                                        </form>
                                    </div>
                                </div>
                                
                                <!-- Prezzi Item -->
                                <div class="item-pricing">
                                    <div class="pricing-row">
                                        <span><i class="fas fa-car pricing-icon"></i> Veicolo:</span>
                                        <span>€<%= item.getPrezzoVeicolo() %></span>
                                    </div>
                                    
                                    <% if (item.getPrezzoOptional().compareTo(BigDecimal.ZERO) > 0) { %>
                                        <div class="pricing-row">
                                            <span><i class="fas fa-plus pricing-icon"></i> Optional:</span>
                                            <span>€<%= item.getPrezzoOptional() %></span>
                                        </div>
                                    <% } %>
                                    
                                    <div class="pricing-total">
                                        <span><i class="fas fa-calculator pricing-icon"></i> Totale:</span>
                                        <span>€<%= item.getPrezzoTotale() %></span>
                                    </div>
                                    
                                    <!-- Azioni Item -->
                                    <div class="item-actions">
                                        <form method="post" action="<%= contextPath %>/carrello" style="display: inline;" class="remove-form">
                                            <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                                            <input type="hidden" name="action" value="rimuovi">
                                            <input type="hidden" name="itemId" value="<%= item.getItemId() %>">
                                            <input type="hidden" name="vehicleName" value="<%= item.getDescrizioneVeicolo() %>">
                                            <button type="button" class="btn btn-small btn-danger remove-btn" 
                                                    data-vehicle-name="<%= item.getDescrizioneVeicolo() %>">
                                                <i class="fas fa-trash"></i> Rimuovi
                                            </button>
                                        </form>
                                    </div>
                                </div>
                            </div>
                        <% } %>
                    </div>
                    
                    <!-- Sidebar Totali -->
                    <div class="carrello-sidebar">
                        <div class="totali-card">
                            <h3><i class="fas fa-chart-line"></i> Riepilogo Ordine</h3>
                            
                            <div class="totali-details">
                                <div class="totale-row">
                                    <span><i class="fas fa-list"></i> Prenotazioni:</span>
                                    <span><%= carrello.getNumeroItemTotali() %></span>
                                </div>
                                
                                <div class="totale-row">
                                    <span><i class="fas fa-calendar-day"></i> Giorni totali:</span>
                                    <span><%= carrello.getNumeroGiorniTotali() %></span>
                                </div>
                                
                                <div class="totale-row">
                                    <span><i class="fas fa-calculator"></i> Subtotale:</span>
                                    <span>€<%= carrello.getTotaleCarrello() %></span>
                                </div>
                                
                                <% BigDecimal sconto = carrello.calcolaSconto();
                                   if (sconto.compareTo(BigDecimal.ZERO) > 0) { %>
                                    <div class="totale-row discount">
                                        <span><i class="fas fa-percentage"></i> Sconto:</span>
                                        <span>-€<%= sconto %></span>
                                    </div>
                                <% } %>
                                
                                <div class="totale-finale">
                                    <span><i class="fas fa-euro-sign"></i> Totale Finale:</span>
                                    <span>€<%= carrello.getTotaleFinale() %></span>
                                </div>
                            </div>
                            
                            <!-- Azioni Carrello -->
                            <div class="carrello-actions">
                                <% if (isLoggedIn) { %>
                                    <a href="<%= contextPath %>/checkout" class="btn btn-primary btn-large">
                                        <i class="fas fa-credit-card"></i> Procedi al Checkout
                                    </a>
                                <% } else { %>
                                    <a href="<%= contextPath %>/login?message=Accedi per completare l'ordine" 
                                       class="btn btn-primary btn-large">
                                        <i class="fas fa-sign-in-alt"></i> Accedi per Ordinare
                                    </a>
                                <% } %>
                                
                                <a href="<%= contextPath %>/catalogo" class="btn btn-secondary">
                                    <i class="fas fa-plus"></i> Aggiungi Altri Veicoli
                                </a>
                                
                                <form method="post" action="<%= contextPath %>/carrello" style="margin-top: 1rem;" class="clear-cart-form">
                                    <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                                    <input type="hidden" name="action" value="svuota">
                                    <button type="button" class="btn btn-outline btn-danger clear-cart-btn">
                                        <i class="fas fa-broom"></i> Svuota Carrello
                                    </button>
                                </form>
                            </div>
                            
                            <!-- Info Sconto -->
                            <% if (carrello.getNumeroGiorniTotali() < 7 || carrello.getNumeroItemTotali() < 3) { %>
                                <div class="sconto-info">
                                    <h4><i class="fas fa-lightbulb"></i> Ottieni Sconti!</h4>
                                    <ul>
                                        <% if (carrello.getNumeroGiorniTotali() < 7) { %>
                                            <li><i class="fas fa-percentage"></i> 5% di sconto con 7+ giorni totali</li>
                                        <% } %>
                                        <% if (carrello.getNumeroItemTotali() < 3) { %>
                                            <li><i class="fas fa-percentage"></i> 3% di sconto con 3+ veicoli</li>
                                        <% } %>
                                    </ul>
                                </div>
                            <% } %>
                        </div>
                    </div>
                </div>
            <% } %>
        </div>
    </main>
    
    <!-- Include Footer -->
    <jsp:include page="/WEB-INF/includes/footer.jsp" />
    
    <!-- JavaScript -->
    <script>
        // Variabile globale per il context path
        window.contextPath = '<%= contextPath %>';
        
        // Hover effect per immagini
        document.addEventListener('DOMContentLoaded', function() {
            const imageWrappers = document.querySelectorAll('.vehicle-image-wrapper');
            
            imageWrappers.forEach(wrapper => {
                const overlay = wrapper.querySelector('.image-hover-overlay');
                const img = wrapper.querySelector('img');
                
                wrapper.addEventListener('mouseenter', function() {
                    if (overlay && img.style.display !== 'none') {
                        overlay.style.opacity = '1';
                        img.style.transform = 'scale(1.05)';
                    }
                });
                
                wrapper.addEventListener('mouseleave', function() {
                    if (overlay) {
                        overlay.style.opacity = '0';
                        img.style.transform = 'scale(1)';
                    }
                });
            });
            
            // Debug: mostra gli URL delle immagini nella console
            const images = document.querySelectorAll('img[data-targa]');
            images.forEach(img => {
                console.log('🔍 Debug immagine per targa ' + img.dataset.targa + ':', {
                    src: img.src,
                    fallback1: img.dataset.fallback1,
                    fallback2: img.dataset.fallback2
                });
            });
        });
        
        // Funzione per zoom immagine (da implementare se necessario)
        function openVehicleImageZoom(element) {
            const img = element.querySelector('img');
            if (img && img.style.display !== 'none') {
                // Implementa qui il modal di zoom se necessario
                console.log('Zoom per immagine:', img.src);
            }
        }
    </script>
    <script src="<%= contextPath %>/scripts/carrello.js"></script>
</body>
</html>