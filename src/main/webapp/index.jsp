<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="it.easyridedb.model.Utente" %>

<%
    // Recupera dati dalla sessione
    Utente utente = (Utente) session.getAttribute("utente");
    String contextPath = request.getContextPath();
    
    // Messaggi opzionali
    String welcomeMessage = (String) request.getAttribute("welcomeMessage");
    String successMessage = (String) request.getAttribute("successMessage");
    String errorMessage = (String) request.getAttribute("errorMessage");
    
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
    <title>EasyRide</title>
    
    <!-- Meta tags per SEO -->
    <meta name="description" content="EasyRide - Il modo più semplice per noleggiare veicoli online. Scopri la nostra flotta di auto, furgoni e veicoli ecologici.">
    <meta name="keywords" content="noleggio auto, car rental, veicoli, auto elettriche, EasyRide">
    <meta name="author" content="EasyRide Team">
    
    <!-- Open Graph per social media -->
    <meta property="og:title" content="EasyRide - Noleggio Veicoli Online">
    <meta property="og:description" content="Il modo più semplice per noleggiare veicoli online">
    <meta property="og:type" content="website">
    <meta property="og:url" content="<%= request.getRequestURL() %>">
    
    <!-- CSS -->
    <link rel="stylesheet" href="<%= contextPath %>/styles/common.css">
    <link rel="stylesheet" href="<%= contextPath %>/styles/index.css">
    
    <!-- Preconnect per performance -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
</head>
<body>
    
    <!-- Include Header -->
    <jsp:include page="/WEB-INF/includes/header.jsp" />
    
    <main id="main-content">
        
        <!-- Messaggi -->
        <% if (welcomeMessage != null && !welcomeMessage.trim().isEmpty()) { %>
            <div class="message welcome" role="alert">
                <%= escapeHtml.apply(welcomeMessage) %>
                <button type="button" class="close-btn" aria-label="Chiudi messaggio">×</button>
            </div>
        <% } %>
        
        <% if (successMessage != null && !successMessage.trim().isEmpty()) { %>
            <div class="message success" role="alert">
                <%= escapeHtml.apply(successMessage) %>
                <button type="button" class="close-btn" aria-label="Chiudi messaggio">×</button>
            </div>
        <% } %>
        
        <% if (errorMessage != null && !errorMessage.trim().isEmpty()) { %>
            <div class="message error" role="alert" aria-live="assertive">
                <%= escapeHtml.apply(errorMessage) %>
                <button type="button" class="close-btn" aria-label="Chiudi messaggio">×</button>
            </div>
        <% } %>
        
        <!-- Hero Section -->
        <section class="hero" aria-label="Sezione principale">
            <div class="hero-content">
                <div class="hero-text">
                    <h1 class="hero-title">
                        Benvenuto in <span class="brand-highlight">EasyRide</span>
                    </h1>
                    <p class="hero-subtitle">
                        Il modo più semplice e veloce per noleggiare il veicolo perfetto per le tue esigenze.
                        <br>Scopri la nostra ampia flotta e prenota online in pochi click.
                    </p>
                    
                    <div class="hero-stats">
                        <div class="stat-item">
                            <span class="stat-number">500+</span>
                            <span class="stat-label">Veicoli disponibili</span>
                        </div>
                        <div class="stat-item">
                            <span class="stat-number">50+</span>
                            <span class="stat-label">Terminal in Italia</span>
                        </div>
                        <div class="stat-item">
                            <span class="stat-number">24/7</span>
                            <span class="stat-label">Supporto clienti</span>
                        </div>
                    </div>
                    
                    <div class="hero-actions">
                        <a href="<%= contextPath %>/catalogo" class="btn btn-primary btn-lg">
                            🚗 Esplora Veicoli
                        </a>
                        <% if (utente == null) { %>
                            <a href="<%= contextPath %>/registrazione.jsp" class="btn btn-secondary btn-lg">
                                📝 Registrati Gratis
                            </a>
                        <% } else { %>
                            <a href="<%= contextPath %>/prenotazioni" class="btn btn-secondary btn-lg">
                                📋 Le Mie Prenotazioni
                            </a>
                        <% } %>
                    </div>
                </div>
                
                <div class="hero-image">
                    <div class="hero-car-animation">
                        <div class="car-icon">🚗</div>
                        <div class="road-lines"></div>
                    </div>
                </div>
            </div>
        </section>
        
        <!-- Features Section -->
        <section class="features" aria-label="I nostri servizi">
            <div class="container">
                <h2 class="section-title">Perché scegliere EasyRide?</h2>
                <p class="section-subtitle">
                    Offriamo il miglior servizio di noleggio veicoli con tecnologia all'avanguardia
                </p>
                
                <div class="features-grid">
                    <div class="feature-card">
                        <div class="feature-icon">🔍</div>
                        <h3 class="feature-title">Ricerca Avanzata</h3>
                        <p class="feature-description">
                            Filtra per marca, modello, tipo di carburante e prezzo. 
                            Trova il veicolo perfetto in secondi.
                        </p>
                    </div>
                    
                    <div class="feature-card">
                        <div class="feature-icon">⚡</div>
                        <h3 class="feature-title">Prenotazione Istantanea</h3>
                        <p class="feature-description">
                            Prenota online 24/7 senza attese. 
                            Conferma immediata e gestione semplificata.
                        </p>
                    </div>
                    
                    <div class="feature-card">
                        <div class="feature-icon">🌱</div>
                        <h3 class="feature-title">Veicoli Ecologici</h3>
                        <p class="feature-description">
                            Ampia selezione di auto elettriche e ibride. 
                            Viaggia rispettando l'ambiente.
                        </p>
                    </div>
                    
                    <div class="feature-card">
                        <div class="feature-icon">💰</div>
                        <h3 class="feature-title">Prezzi Trasparenti</h3>
                        <p class="feature-description">
                            Nessun costo nascosto. Prezzi chiari e competitivi 
                            con la migliore qualità del servizio.
                        </p>
                    </div>
                    
                    <div class="feature-card">
                        <div class="feature-icon">🛡️</div>
                        <h3 class="feature-title">Sicurezza Garantita</h3>
                        <p class="feature-description">
                            Tutti i veicoli sono controllati e assicurati. 
                            La tua sicurezza è la nostra priorità.
                        </p>
                    </div>
                    
                    <div class="feature-card">
                        <div class="feature-icon">📱</div>
                        <h3 class="feature-title">App Mobile</h3>
                        <p class="feature-description">
                            Gestisci le prenotazioni dal tuo smartphone. 
                            Interfaccia responsive e user-friendly.
                        </p>
                    </div>
                </div>
            </div>
        </section>
        
        <!-- Vehicle Types Section -->
        <section class="vehicle-types" aria-label="Tipi di veicoli">
            <div class="container">
                <h2 class="section-title">La Nostra Flotta</h2>
                <p class="section-subtitle">
                    Scegli tra diverse categorie di veicoli per ogni esigenza
                </p>
                
                <div class="vehicle-grid">
                    <div class="vehicle-type-card">
                        <div class="vehicle-type-icon">🚙</div>
                        <h3 class="vehicle-type-title">City Car</h3>
                        <p class="vehicle-type-description">
                            Perfette per la città. Compatte, economiche e facili da parcheggiare.
                        </p>
                        <div class="vehicle-type-features">
                            <span class="feature-tag">Economiche</span>
                            <span class="feature-tag">Maneggevoli</span>
                        </div>
                        <a href="<%= contextPath %>/catalogo?tipo=city" class="btn btn-outline">
                            Esplora City Car
                        </a>
                    </div>
                    
                    <div class="vehicle-type-card">
                        <div class="vehicle-type-icon">🚗</div>
                        <h3 class="vehicle-type-title">Berline</h3>
                        <p class="vehicle-type-description">
                            Comfort e stile per viaggi di lavoro e famiglia. Spazio e prestazioni.
                        </p>
                        <div class="vehicle-type-features">
                            <span class="feature-tag">Comfort</span>
                            <span class="feature-tag">Prestazioni</span>
                        </div>
                        <a href="<%= contextPath %>/catalogo?tipo=berlina" class="btn btn-outline">
                            Esplora Berline
                        </a>
                    </div>
                    
                    <div class="vehicle-type-card">
                        <div class="vehicle-type-icon">🚐</div>
                        <h3 class="vehicle-type-title">SUV</h3>
                        <p class="vehicle-type-description">
                            Ideali per avventure e famiglia numerosa. Spazio, sicurezza e versatilità.
                        </p>
                        <div class="vehicle-type-features">
                            <span class="feature-tag">Spazioso</span>
                            <span class="feature-tag">Sicuro</span>
                        </div>
                        <a href="<%= contextPath %>/catalogo?tipo=suv" class="btn btn-outline">
                            Esplora SUV
                        </a>
                    </div>
                    
                    <div class="vehicle-type-card featured">
                        <div class="featured-badge">🌟 Popolare</div>
                        <div class="vehicle-type-icon">⚡</div>
                        <h3 class="vehicle-type-title">Elettriche</h3>
                        <p class="vehicle-type-description">
                            Zero emissioni, massima tecnologia. Il futuro della mobilità è qui.
                        </p>
                        <div class="vehicle-type-features">
                            <span class="feature-tag eco">Eco-friendly</span>
                            <span class="feature-tag tech">Hi-tech</span>
                        </div>
                        <a href="<%= contextPath %>/catalogo?carburante=elettrico" class="btn btn-primary">
                            Esplora Elettriche
                        </a>
                    </div>
                </div>
            </div>
        </section>
        
        <!-- Quick Search Section -->
        <section class="quick-search" aria-label="Ricerca rapida">
            <div class="container">
                <div class="quick-search-content">
                    <h2 class="section-title text-white">Trova il tuo veicolo ideale</h2>
                    <p class="section-subtitle text-white">
                        Usa i filtri rapidi per trovare subito quello che cerchi
                    </p>
                    
                    <form class="quick-search-form" method="get" action="<%= contextPath %>/catalogo">
                        <div class="search-row">
                            <div class="search-field">
                                <label for="quick-tipo" class="sr-only">Tipo veicolo</label>
                                <select id="quick-tipo" name="tipo" class="search-select">
                                    <option value="">Tutti i tipi</option>
                                    <option value="city">City Car</option>
                                    <option value="berlina">Berlina</option>
                                    <option value="suv">SUV</option>
                                    <option value="station-wagon">Station Wagon</option>
                                </select>
                            </div>
                            
                            <div class="search-field">
                                <label for="quick-carburante" class="sr-only">Carburante</label>
                                <select id="quick-carburante" name="carburante" class="search-select">
                                    <option value="">Tutti i carburanti</option>
                                    <option value="benzina">Benzina</option>
                                    <option value="diesel">Diesel</option>
                                    <option value="elettrico">Elettrico</option>
                                    <option value="ibrido">Ibrido</option>
                                </select>
                            </div>
                            
                            <div class="search-field">
                                <label for="quick-prezzo" class="sr-only">Prezzo massimo</label>
                                <select id="quick-prezzo" name="prezzoMax" class="search-select">
                                    <option value="">Qualsiasi prezzo</option>
                                    <option value="50">Fino a €50/giorno</option>
                                    <option value="100">Fino a €100/giorno</option>
                                    <option value="150">Fino a €150/giorno</option>
                                    <option value="200">Oltre €150/giorno</option>
                                </select>
                            </div>
                            
                            <button type="submit" class="btn btn-primary btn-search">
                                🔍 Cerca Ora
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </section>
        
        <!-- CTA Section -->
        <section class="cta-section" aria-label="Call to action">
            <div class="container">
                <div class="cta-content">
                    <h2 class="cta-title">Pronto a iniziare il tuo viaggio?</h2>
                    <p class="cta-description">
                        Unisciti a migliaia di clienti soddisfatti che hanno scelto EasyRide 
                        per la loro mobilità. Registrati ora e scopri le nostre offerte esclusive.
                    </p>
                    
                    <div class="cta-actions">
                        <% if (utente == null) { %>
                            <a href="<%= contextPath %>/registrazione.jsp" class="btn btn-primary btn-xl">
                                🚀 Registrati Ora
                            </a>
                            <a href="<%= contextPath %>/catalogo" class="btn btn-outline-white btn-xl">
                                👀 Esplora Senza Registrazione
                            </a>
                        <% } else { %>
                            <p class="welcome-user">
                                Ciao <strong><%= escapeHtml.apply(utente.getNome()) %></strong>! 
                                Benvenuto di nuovo in EasyRide.
                            </p>
                            <a href="<%= contextPath %>/catalogo" class="btn btn-primary btn-xl">
                                🚗 Esplora Veicoli
                            </a>
                            <a href="<%= contextPath %>/prenotazioni" class="btn btn-outline-white btn-xl">
                                📋 Le Mie Prenotazioni
                            </a>
                        <% } %>
                    </div>
                </div>
            </div>
        </section>
        
    </main>
    
    <!-- Include Footer -->
    <jsp:include page="/WEB-INF/includes/footer.jsp" />
    
    <!-- JavaScript -->
    <script src="<%= contextPath %>/scripts/index.js" defer></script>
    
    <!-- Schema.org structured data per SEO -->
    <script type="application/ld+json">
    {
        "@context": "https://schema.org",
        "@type": "Organization",
        "name": "EasyRide",
        "description": "Servizio di noleggio veicoli online",
        "url": "<%= request.getRequestURL() %>",
        "logo": "<%= contextPath %>/images/logo.png",
        "contactPoint": {
            "@type": "ContactPoint",
            "telephone": "+39-800-123456",
            "contactType": "customer service",
            "availableLanguage": ["Italian"]
        },
        "sameAs": [
            "https://www.facebook.com/easyride",
            "https://www.twitter.com/easyride"
        ]
    }
    </script>
</body>
</html>