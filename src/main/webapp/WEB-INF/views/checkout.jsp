<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="it.easyridedb.model.*" %>
<%@ page import="java.util.List" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="java.time.format.DateTimeFormatter" %>

<%
    // Controllo autenticazione
    Utente utente = (Utente) session.getAttribute("utente");
    if (utente == null) {
        response.sendRedirect(request.getContextPath() + "/login?message=Accedi per completare l'ordine");
        return;
    }
    
    // Recupera dati
    Carrello carrello = (Carrello) request.getAttribute("carrello");
    String contextPath = request.getContextPath();
    String csrfToken = (String) request.getAttribute("csrfToken");
    
    // Controllo carrello vuoto
    if (carrello == null || carrello.isEmpty()) {
        response.sendRedirect(contextPath + "/carrello?message=Il carrello è vuoto");
        return;
    }
    
    // Formatter per date
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>EasyRide - Checkout</title>
    
    <!-- CSS -->
    <link rel="stylesheet" href="<%= contextPath %>/styles/common.css">
    <link rel="stylesheet" href="<%= contextPath %>/styles/checkout.css">
    
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body>
    <!-- Include Header -->
    <jsp:include page="/WEB-INF/includes/header.jsp" />
    
    <main>
        <div class="container">
            <!-- Progress Steps -->
            <div class="checkout-progress">
                <div class="progress-step completed">
                    <i class="fas fa-shopping-cart"></i>
                    <span>Carrello</span>
                </div>
                <div class="progress-connector completed"></div>
                <div class="progress-step active">
                    <i class="fas fa-credit-card"></i>
                    <span>Checkout</span>
                </div>
                <div class="progress-connector"></div>
                <div class="progress-step">
                    <i class="fas fa-check-circle"></i>
                    <span>Conferma</span>
                </div>
            </div>
            
            <!-- Checkout Header -->
            <div class="checkout-header">
                <h1><i class="fas fa-credit-card"></i> Finalizza Prenotazione</h1>
                <p class="checkout-subtitle">Controlla i dettagli del tuo ordine e procedi con la prenotazione</p>
            </div>
            
            <!-- ✅ FORM UNICO CHE INCLUDE TUTTO -->
            <form id="checkout-form" method="post" action="<%= contextPath %>/checkout">
                <!-- ✅ Hidden fields obbligatori -->
                <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                <input type="hidden" name="action" value="conferma-prenotazione">
                
                <div class="checkout-content">
                    
                    <!-- Colonna Sinistra: Riepilogo Ordine -->
                    <div class="checkout-summary">
                        <div class="summary-card">
                            <h2><i class="fas fa-list-alt"></i> Riepilogo Ordine</h2>
                            
                            <!-- Lista Prenotazioni -->
                            <div class="order-items">
                                <% for (CarrelloItem item : carrello.getItems()) { %>
                                    <div class="order-item">
                                        <div class="item-image-mini">
                                            <%
                                                // 🔧 LOGICA CORRETTA PER LE IMMAGINI (STESSA DEL CARRELLO)
                                                String targa = item.getTargaVeicolo();
                                                String vehicleName = item.getDescrizioneVeicolo();
                                                
                                                // 1. URL principale: targa + contextPath
                                                String primaryImageUrl = contextPath + "/images/veicoli/" + targa + ".jpg";
                                                
                                                // 2. Fallback: URL dal database se presente
                                                String fallbackImageUrl = null;
                                                if (item.getImmagineUrl() != null && !item.getImmagineUrl().trim().isEmpty()) {
                                                    String dbUrl = item.getImmagineUrl().trim();
                                                    if (dbUrl.startsWith("http")) {
                                                        fallbackImageUrl = dbUrl;
                                                    } else {
                                                        fallbackImageUrl = contextPath + "/" + (dbUrl.startsWith("/") ? dbUrl.substring(1) : dbUrl);
                                                    }
                                                }
                                                
                                                // 3. Fallback finale: placeholder online
                                                String finalFallbackUrl = "https://picsum.photos/100/60?random=" + Math.abs(targa.hashCode() % 1000);
                                            %>
                                            
                                            <img id="checkout-img-<%= item.getItemId() %>"
                                                 src="<%= primaryImageUrl %>" 
                                                 alt="<%= vehicleName %> - Targa <%= targa %>"
                                                 data-targa="<%= targa %>"
                                                 data-fallback1="<%= fallbackImageUrl %>"
                                                 data-fallback2="<%= finalFallbackUrl %>"
                                                 style="
                                                    width: 100px; 
                                                    height: 60px; 
                                                    object-fit: cover; 
                                                    border-radius: 6px;
                                                    border: 1px solid #dee2e6;
                                                 "
                                                 onload="
                                                    console.log('✅ Checkout img caricata per <%= targa %>:', this.src);
                                                 "
                                                 onerror="
                                                    console.log('❌ Checkout img errore per <%= targa %>:', this.src);
                                                    
                                                    // Primo fallback: URL dal database
                                                    if (this.dataset.fallback1 && this.dataset.fallback1 !== 'null' && this.src !== this.dataset.fallback1) {
                                                        console.log('🔄 Checkout fallback 1 per <%= targa %>:', this.dataset.fallback1);
                                                        this.src = this.dataset.fallback1;
                                                        return;
                                                    }
                                                    
                                                    // Secondo fallback: placeholder online
                                                    if (this.src !== this.dataset.fallback2) {
                                                        console.log('🔄 Checkout fallback 2 per <%= targa %>:', this.dataset.fallback2);
                                                        this.src = this.dataset.fallback2;
                                                        return;
                                                    }
                                                    
                                                    // Fallback finale: nascondi immagine
                                                    console.log('⚠️ Checkout: tutti i fallback falliti per <%= targa %>');
                                                    this.style.display = 'none';
                                                    this.insertAdjacentHTML('afterend', '<div style=\"width:100px;height:60px;background:#f8f9fa;border-radius:6px;display:flex;align-items:center;justify-content:center;color:#6c757d;font-size:0.8rem;\"><i class=\"fas fa-car\"></i></div>');
                                                 "
                                                 loading="lazy">
                                        </div>
                                        
                                        <div class="item-details-mini">
                                            <h4><%= item.getDescrizioneVeicolo() %></h4>
                                            <p class="item-targa">Targa: <%= item.getTargaVeicolo() %></p>
                                            <p class="item-period">
                                                <i class="fas fa-calendar"></i>
                                                <%= item.getDataRitiro().format(dateFormatter) %> - 
                                                <%= item.getDataRestituzione().format(dateFormatter) %>
                                                (<%= item.getNumeroGiorni() %> giorni)
                                            </p>
                                            <p class="item-locations">
                                                <i class="fas fa-map-marker-alt"></i>
                                                <%= item.getNomeTerminalRitiro() %> → <%= item.getNomeTerminalRestituzione() %>
                                            </p>
                                            
                                            <!-- Optional selezionati -->
                                            <% if (item.getOptionalSelezionati() != null && !item.getOptionalSelezionati().isEmpty()) { %>
                                                <div class="item-optionals">
                                                    <small><i class="fas fa-plus"></i> Optional:</small>
                                                    <% for (Optional opt : item.getOptionalSelezionati()) { %>
                                                        <span class="optional-tag"><%= opt.getNome() %></span>
                                                    <% } %>
                                                </div>
                                            <% } %>
                                        </div>
                                        
                                        <div class="item-price-mini">
                                            <span class="price-total">€<%= item.getPrezzoTotale() %></span>
                                            <% if (item.getPrezzoOptional().compareTo(BigDecimal.ZERO) > 0) { %>
                                                <small>+ €<%= item.getPrezzoOptional() %> optional</small>
                                            <% } %>
                                        </div>
                                    </div>
                                <% } %>
                            </div>
                            
                            <!-- Totali -->
                            <div class="order-totals">
                                <div class="total-row">
                                    <span>Subtotale:</span>
                                    <span>€<%= carrello.getTotaleCarrello() %></span>
                                </div>
                                
                                <% BigDecimal sconto = carrello.calcolaSconto();
                                   if (sconto.compareTo(BigDecimal.ZERO) > 0) { %>
                                    <div class="total-row discount">
                                        <span><i class="fas fa-percentage"></i> Sconto:</span>
                                        <span>-€<%= sconto %></span>
                                    </div>
                                <% } %>
                                
                                <div class="total-row total-final">
                                    <span><strong>Totale Finale:</strong></span>
                                    <span><strong>€<%= carrello.getTotaleFinale() %></strong></span>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Colonna Destra: Form Checkout -->
                    <div class="checkout-form-section">
                        
                        <!-- Dati Cliente -->
                        <div class="checkout-card">
                            <h3><i class="fas fa-user"></i> Dati Cliente</h3>
                            <div class="customer-info">
                                <div class="customer-row">
                                    <span class="label">Nome:</span>
                                    <span class="value"><%= utente.getNome() %> <%= utente.getCognome() %></span>
                                </div>
                                <div class="customer-row">
                                    <span class="label">Email:</span>
                                    <span class="value"><%= utente.getEmail() %></span>
                                </div>
                            </div>
                            
                            <a href="<%= contextPath %>/profilo" class="edit-profile-link">
                                <i class="fas fa-edit"></i> Modifica Profilo
                            </a>
                        </div>
                        
                        <!-- ✅ Metodo Pagamento - DENTRO IL FORM -->
                        <div class="checkout-card">
                            <h3><i class="fas fa-credit-card"></i> Metodo Pagamento</h3>
                            <div class="payment-methods">
                                <label class="payment-option selected">
                                    <input type="radio" name="paymentMethod" value="card" checked>
                                    <div class="payment-content">
                                        <i class="fas fa-credit-card payment-icon"></i>
                                        <span>Carta di Credito/Debito</span>
                                        <div class="card-icons">
                                            <i class="fab fa-cc-visa"></i>
                                            <i class="fab fa-cc-mastercard"></i>
                                            <i class="fab fa-cc-amex"></i>
                                        </div>
                                    </div>
                                </label>
                                
                                <label class="payment-option">
                                    <input type="radio" name="paymentMethod" value="paypal">
                                    <div class="payment-content">
                                        <i class="fab fa-paypal payment-icon"></i>
                                        <span>PayPal</span>
                                    </div>
                                </label>
                                
                                <label class="payment-option">
                                    <input type="radio" name="paymentMethod" value="bank">
                                    <div class="payment-content">
                                        <i class="fas fa-university payment-icon"></i>
                                        <span>Bonifico Bancario</span>
                                    </div>
                                </label>
                            </div>
                        </div>
                        
                        <!-- ✅ Note Aggiuntive - DENTRO IL FORM -->
                        <div class="checkout-card">
                            <h3><i class="fas fa-sticky-note"></i> Note Aggiuntive (Opzionale)</h3>
                            <textarea name="note" id="note" rows="3" maxlength="500" 
                                      placeholder="Inserisci qui eventuali note o richieste speciali per la tua prenotazione..."></textarea>
                            <small class="char-counter">0/500 caratteri</small>
                        </div>
                        
                        <!-- ✅ Termini e Privacy - DENTRO IL FORM -->
                        <div class="checkout-card">
                            <h3><i class="fas fa-shield-alt"></i> Termini e Privacy</h3>
                            <div class="terms-section">
                                <label class="checkbox-label">
                                    <input type="checkbox" id="terms-checkbox" name="terms" value="true" required>
                                    <span class="checkmark"></span>
                                    Accetto i <a href="<%= contextPath %>/termini" target="_blank">Termini e Condizioni</a> 
                                    e l'<a href="<%= contextPath %>/privacy" target="_blank">Informativa Privacy</a>
                                </label>
                                
                                <label class="checkbox-label">
                                    <input type="checkbox" name="newsletter" id="newsletter-checkbox" value="true">
                                    <span class="checkmark"></span>
                                    Desidero ricevere offerte e promozioni via email
                                </label>
                            </div>
                        </div>
                        
                        <!-- ✅ Azioni Checkout -->
                        <div class="checkout-actions">
                            <button type="submit" id="confirm-btn" class="btn btn-primary btn-large" disabled>
                                <i class="fas fa-lock"></i>
                                <span class="btn-text">Conferma Prenotazione</span>
                                <span class="btn-price">€<%= carrello.getTotaleFinale() %></span>
                            </button>
                            
                            <a href="<%= contextPath %>/carrello" class="btn btn-outline">
                                <i class="fas fa-arrow-left"></i> Torna al Carrello
                            </a>
                            
                            <!-- Sicurezza -->
                            <div class="security-badges">
                                <div class="security-item">
                                    <i class="fas fa-shield-alt"></i>
                                    <span>Pagamenti Sicuri</span>
                                </div>
                                <div class="security-item">
                                    <i class="fas fa-lock"></i>
                                    <span>SSL Certificato</span>
                                </div>
                                <div class="security-item">
                                    <i class="fas fa-undo"></i>
                                    <span>Cancellazione Gratuita</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </form>
        </div>
    </main>
    
    <!-- Include Footer -->
    <jsp:include page="/WEB-INF/includes/footer.jsp" />
    
    <!-- Loading Overlay -->
    <div id="loading-overlay" class="loading-overlay">
        <div class="loading-content">
            <div class="loading-spinner"></div>
            <h3>Elaborazione Prenotazione...</h3>
            <p>Stiamo confermando la tua prenotazione, non chiudere la pagina.</p>
        </div>
    </div>
    
    <!-- ✅ CORRETTO -->
<script>
    window.contextPath = '<%= request.getContextPath() %>';
    window.totalAmount = '<%= carrello.getTotaleFinale() %>';
    console.log('🚀 Context Path corretto:', window.contextPath);
</script>
    <script src="<%= contextPath %>/scripts/checkout.js"></script>
</body>
</html>