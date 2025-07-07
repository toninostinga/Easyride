<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="it.easyridedb.model.*" %>
<%@ page import="java.util.List" %>

<%
    // Recupera dati dalla request
    Veicolo veicolo = (Veicolo) request.getAttribute("veicolo");
    List<Terminal> terminals = (List<Terminal>) request.getAttribute("terminals");
    String contextPath = request.getContextPath();
    String csrfToken = (String) request.getAttribute("csrfToken");
    String action = (String) request.getAttribute("action");
    String pageTitle = (String) request.getAttribute("pageTitle");
    
    // Determina se è modifica o creazione
    boolean isEdit = "update".equals(action);
    boolean isCreate = "create".equals(action);
    
    if (pageTitle == null) {
        pageTitle = isEdit ? "Modifica Veicolo" : "Nuovo Veicolo";
    }
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>EasyRide - <%= pageTitle %></title>
    
    <!-- CSS -->
    <link rel="stylesheet" href="<%= contextPath %>/styles/common.css">
    <link rel="stylesheet" href="<%= contextPath %>/styles/admin-veicoli-form.css">
    
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body class="admin-body">
    <!-- Include Header -->
    <jsp:include page="/WEB-INF/includes/header.jsp" />
    
    <main class="admin-main">
        <div class="container">
            <!-- Header Form -->
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
                        <span class="current"><%= pageTitle %></span>
                    </div>
                    
                    <h1>
                        <i class="fas fa-<%= isEdit ? "edit" : "plus" %>"></i> 
                        <%= pageTitle %>
                    </h1>
                    
                    <% if (isEdit && veicolo != null) { %>
                        <p class="page-description">Modifica le informazioni del veicolo <%= veicolo.getTarga() %></p>
                    <% } else { %>
                        <p class="page-description">Aggiungi un nuovo veicolo al catalogo</p>
                    <% } %>
                </div>
                
                <div class="header-actions">
                    <a href="<%= contextPath %>/admin-veicoli" class="btn btn-outline">
                        <i class="fas fa-arrow-left"></i> Torna alla Lista
                    </a>
                    <% if (isEdit && veicolo != null) { %>
                        <a href="<%= contextPath %>/admin-veicoli?action=view&targa=<%= veicolo.getTarga() %>" class="btn btn-secondary">
                            <i class="fas fa-eye"></i> Visualizza Dettagli
                        </a>
                    <% } %>
                </div>
            </div>
            
            <!-- Form Principale -->
            <div class="form-container">
                <form method="post" action="<%= contextPath %>/admin-veicoli" class="vehicle-form" id="vehicleForm">
                    <!-- CSRF Token -->
                    <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                    <input type="hidden" name="action" value="<%= action %>">
                    
                    <% if (isEdit && veicolo != null) { %>
                        <input type="hidden" name="targaOriginale" value="<%= veicolo.getTarga() %>">
                    <% } %>
                    
                    <!-- Sezione Informazioni Principali -->
                    <div class="form-section">
                        <div class="section-header">
                            <h2><i class="fas fa-info-circle"></i> Informazioni Principali</h2>
                            <p class="section-description">Inserisci le informazioni base del veicolo</p>
                        </div>
                        
                        <div class="form-grid">
                            <!-- Targa -->
                            <div class="form-group">
                                <label for="targa" class="required">
                                    <i class="fas fa-id-card"></i> Targa
                                </label>
                                <input type="text" 
                                       id="targa" 
                                       name="targa" 
                                       value="<%= veicolo != null ? veicolo.getTarga() : "" %>"
                                       placeholder="Es. AB123CD"
                                       required
                                       maxlength="20"
                                       pattern="[A-Z0-9]{2,20}"
                                       title="Inserisci una targa valida (lettere maiuscole e numeri)"
                                       class="form-input">
                                <div class="field-help">
                                    Formato: lettere maiuscole e numeri (es. AB123CD)
                                </div>
                                <div class="error-message" id="targa-error"></div>
                            </div>
                            
                            <!-- Marca -->
                            <div class="form-group">
                                <label for="marca" class="required">
                                    <i class="fas fa-tag"></i> Marca
                                </label>
                                <input type="text" 
                                       id="marca" 
                                       name="marca" 
                                       value="<%= veicolo != null ? veicolo.getMarca() : "" %>"
                                       placeholder="Es. Toyota"
                                       required
                                       maxlength="50"
                                       class="form-input">
                                <div class="error-message" id="marca-error"></div>
                            </div>
                            
                            <!-- Modello -->
                            <div class="form-group">
                                <label for="modello" class="required">
                                    <i class="fas fa-car"></i> Modello
                                </label>
                                <input type="text" 
                                       id="modello" 
                                       name="modello" 
                                       value="<%= veicolo != null ? veicolo.getModello() : "" %>"
                                       placeholder="Es. Corolla"
                                       required
                                       maxlength="50"
                                       class="form-input">
                                <div class="error-message" id="modello-error"></div>
                            </div>
                            
                            <!-- Tipo -->
                            <div class="form-group">
                                <label for="tipo">
                                    <i class="fas fa-shapes"></i> Tipo/Categoria
                                </label>
                                <select id="tipo" name="tipo" class="form-select">
                                    <option value="">Seleziona tipo</option>
                                    <option value="Berlina" <%= veicolo != null && "Berlina".equals(veicolo.getTipo()) ? "selected" : "" %>>Berlina</option>
                                    <option value="SUV" <%= veicolo != null && "SUV".equals(veicolo.getTipo()) ? "selected" : "" %>>SUV</option>
                                    <option value="Hatchback" <%= veicolo != null && "Hatchback".equals(veicolo.getTipo()) ? "selected" : "" %>>Hatchback</option>
                                    <option value="Station Wagon" <%= veicolo != null && "Station Wagon".equals(veicolo.getTipo()) ? "selected" : "" %>>Station Wagon</option>
                                    <option value="Coupé" <%= veicolo != null && "Coupé".equals(veicolo.getTipo()) ? "selected" : "" %>>Coupé</option>
                                    <option value="Convertibile" <%= veicolo != null && "Convertibile".equals(veicolo.getTipo()) ? "selected" : "" %>>Convertibile</option>
                                    <option value="Monovolume" <%= veicolo != null && "Monovolume".equals(veicolo.getTipo()) ? "selected" : "" %>>Monovolume</option>
                                    <option value="Pickup" <%= veicolo != null && "Pickup".equals(veicolo.getTipo()) ? "selected" : "" %>>Pickup</option>
                                    <option value="Furgone" <%= veicolo != null && "Furgone".equals(veicolo.getTipo()) ? "selected" : "" %>>Furgone</option>
                                </select>
                                <div class="field-help">Categoria del veicolo (opzionale)</div>
                                <div class="error-message" id="tipo-error"></div>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Sezione Caratteristiche Tecniche -->
                    <div class="form-section">
                        <div class="section-header">
                            <h2><i class="fas fa-cogs"></i> Caratteristiche Tecniche</h2>
                            <p class="section-description">Specifiche tecniche del veicolo</p>
                        </div>
                        
                        <div class="form-grid">
                            <!-- Carburante -->
                            <div class="form-group">
                                <label for="carburante" class="required">
                                    <i class="fas fa-gas-pump"></i> Carburante
                                </label>
                                <select id="carburante" name="carburante" class="form-select" required>
                                    <option value="">Seleziona carburante</option>
                                    <option value="benzina" <%= veicolo != null && "benzina".equals(veicolo.getCarburante()) ? "selected" : "" %>>Benzina</option>
                                    <option value="diesel" <%= veicolo != null && "diesel".equals(veicolo.getCarburante()) ? "selected" : "" %>>Diesel</option>
                                    <option value="elettrico" <%= veicolo != null && "elettrico".equals(veicolo.getCarburante()) ? "selected" : "" %>>Elettrico</option>
                                    <option value="ibrido" <%= veicolo != null && "ibrido".equals(veicolo.getCarburante()) ? "selected" : "" %>>Ibrido</option>
                                </select>
                                <div class="error-message" id="carburante-error"></div>
                            </div>
                            
                            <!-- Trasmissione -->
                            <div class="form-group">
                                <label for="trasmissione" class="required">
                                    <i class="fas fa-cogs"></i> Trasmissione
                                </label>
                                <select id="trasmissione" name="trasmissione" class="form-select" required>
                                    <option value="">Seleziona trasmissione</option>
                                    <option value="manuale" <%= veicolo != null && "manuale".equals(veicolo.getTrasmissione()) ? "selected" : "" %>>Manuale</option>
                                    <option value="automatica" <%= veicolo != null && "automatica".equals(veicolo.getTrasmissione()) ? "selected" : "" %>>Automatica</option>
                                </select>
                                <div class="error-message" id="trasmissione-error"></div>
                            </div>
                            
                            <!-- Prezzo per Giorno -->
                            <div class="form-group">
                                <label for="prezzoPerGiorno" class="required">
                                    <i class="fas fa-euro-sign"></i> Prezzo per Giorno
                                </label>
                                <div class="input-group">
                                    <span class="input-prefix">€</span>
                                    <input type="number" 
                                           id="prezzoPerGiorno" 
                                           name="prezzoPerGiorno" 
                                           value="<%= veicolo != null ? veicolo.getPrezzoPerGiorno() : "" %>"
                                           placeholder="50.00"
                                           required
                                           min="1"
                                           max="9999"
                                           step="0.01"
                                           class="form-input">
                                </div>
                                <div class="field-help">Prezzo di noleggio giornaliero in euro</div>
                                <div class="error-message" id="prezzoPerGiorno-error"></div>
                            </div>
                            
                            <!-- Terminal -->
                            <div class="form-group">
                                <label for="terminalId">
                                    <i class="fas fa-map-marker-alt"></i> Terminal di Appartenenza
                                </label>
                                <select id="terminalId" name="terminalId" class="form-select">
                                    <option value="">Nessun terminal specifico</option>
                                    <% if (terminals != null) {
                                        for (Terminal terminal : terminals) { %>
                                        <option value="<%= terminal.getId() %>" 
                                                <%= veicolo != null && veicolo.getTerminalId() == terminal.getId() ? "selected" : "" %>>
                                            <%= terminal.getNome() %>
                                        </option>
                                    <% }
                                    } %>
                                </select>
                                <div class="field-help">Terminal dove il veicolo è generalmente disponibile</div>
                                <div class="error-message" id="terminalId-error"></div>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Sezione Immagine e Disponibilità -->
                    <div class="form-section">
                        <div class="section-header">
                            <h2><i class="fas fa-image"></i> Immagine e Disponibilità</h2>
                            <p class="section-description">URL dell'immagine e stato di disponibilità</p>
                        </div>
                        
                        <div class="form-grid">
                            <!-- URL Immagine -->
                            <div class="form-group full-width">
                                <label for="immagineUrl">
                                    <i class="fas fa-link"></i> URL Immagine
                                </label>
                                <input type="url" 
                                       id="immagineUrl" 
                                       name="immagineUrl" 
                                       value="<%= veicolo != null && veicolo.getImmagineUrl() != null ? veicolo.getImmagineUrl() : "" %>"
                                       placeholder="https://esempio.com/immagine-veicolo.jpg"
                                       maxlength="255"
                                       class="form-input">
                                <div class="field-help">
                                    URL dell'immagine del veicolo (opzionale). Formati supportati: JPG, PNG, WebP
                                </div>
                                <div class="error-message" id="immagineUrl-error"></div>
                            </div>
                            
                            <!-- Preview Immagine -->
                            <div class="form-group full-width">
                                <div class="image-preview" id="imagePreview">
                                    <% if (veicolo != null && veicolo.getImmagineUrl() != null && !veicolo.getImmagineUrl().isEmpty()) { %>
                                        <img src="<%= veicolo.getImmagineUrl() %>" alt="Preview veicolo" id="previewImg">
                                    <% } else { %>
                                        <div class="preview-placeholder" id="previewPlaceholder">
                                            <i class="fas fa-image"></i>
                                            <span>Anteprima immagine</span>
                                        </div>
                                    <% } %>
                                </div>
                            </div>
                            
                            <!-- Disponibilità -->
                            <div class="form-group">
                                <label class="checkbox-label">
                                    <input type="checkbox" 
                                           id="disponibile" 
                                           name="disponibile" 
                                           value="true"
                                           <%= veicolo == null || veicolo.isDisponibile() ? "checked" : "" %>
                                           class="form-checkbox">
                                    <span class="checkbox-custom"></span>
                                    <span class="checkbox-text">
                                        <i class="fas fa-check-circle"></i> Veicolo Disponibile per il Noleggio
                                    </span>
                                </label>
                                <div class="field-help">
                                    Se disabilitato, il veicolo non sarà disponibile per nuove prenotazioni
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Azioni Form -->
                    <div class="form-actions">
                        <div class="actions-left">
                            <a href="<%= contextPath %>/admin-veicoli" class="btn btn-outline">
                                <i class="fas fa-times"></i> Annulla
                            </a>
                        </div>
                        
                        <div class="actions-right">
                            <button type="button" class="btn btn-secondary" onclick="validateForm()">
                                <i class="fas fa-check"></i> Valida Dati
                            </button>
                            
                            <button type="submit" class="btn btn-primary" id="submitBtn">
                                <i class="fas fa-save"></i> 
                                <%= isEdit ? "Aggiorna Veicolo" : "Crea Veicolo" %>
                            </button>
                        </div>
                    </div>
                </form>
            </div>
            
            <!-- Sidebar Info -->
            <div class="sidebar-info">
                <div class="info-card">
                    <div class="info-header">
                        <h3><i class="fas fa-lightbulb"></i> Suggerimenti</h3>
                    </div>
                    <div class="info-content">
                        <div class="tip-item">
                            <i class="fas fa-star"></i>
                            <div>
                                <strong>Targa:</strong> Usa il formato standard italiano o internazionale
                            </div>
                        </div>
                        
                        <div class="tip-item">
                            <i class="fas fa-star"></i>
                            <div>
                                <strong>Prezzo:</strong> Considera la categoria del veicolo e la concorrenza
                            </div>
                        </div>
                        
                        <div class="tip-item">
                            <i class="fas fa-star"></i>
                            <div>
                                <strong>Immagine:</strong> Usa immagini di alta qualità per migliorare le prenotazioni
                            </div>
                        </div>
                        
                        <div class="tip-item">
                            <i class="fas fa-star"></i>
                            <div>
                                <strong>Terminal:</strong> Assegna un terminal per facilitare la gestione
                            </div>
                        </div>
                    </div>
                </div>
                
                <% if (isEdit && veicolo != null) { %>
                    <div class="info-card">
                        <div class="info-header">
                            <h3><i class="fas fa-history"></i> Storico</h3>
                        </div>
                        <div class="info-content">
                            <div class="history-item">
                                <strong>Creato:</strong> Data creazione non disponibile
                            </div>
                            <div class="history-item">
                                <strong>Ultima modifica:</strong> Data modifica non disponibile
                            </div>
                            <div class="history-item">
                                <strong>Prenotazioni totali:</strong> Da implementare
                            </div>
                        </div>
                    </div>
                <% } %>
            </div>
        </div>
    </main>
    
    <!-- Include Footer -->
    <jsp:include page="/WEB-INF/includes/footer.jsp" />
    
    <!-- JavaScript -->
    <script src="<%= contextPath %>/scripts/admin-veicoli-form.js"></script>
</body>
</html>