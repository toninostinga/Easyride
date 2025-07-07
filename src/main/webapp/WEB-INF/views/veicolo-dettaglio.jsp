<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="it.easyridedb.model.ImageUtils" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${veicolo.marca} ${veicolo.modello} - EasyRide</title>
    
    <!-- CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/common.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/veicolo-dettaglio.css">
    
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    
    <!-- Meta per SEO -->
    <meta name="description" content="Prenota ${veicolo.marca} ${veicolo.modello} - ${veicolo.carburanteDisplay} ${veicolo.trasmissioneDisplay} da €${veicolo.prezzoPerGiorno}/giorno">
    <meta property="og:title" content="${veicolo.marca} ${veicolo.modello} - EasyRide">
    <meta property="og:description" content="Noleggia ${veicolo.marca} ${veicolo.modello} con EasyRide">
    <meta property="og:image" content="${veicolo.immagineUrl}">
</head>
<body>
    <%
        // *** CALCOLO IMMAGINE VEICOLO USANDO IMAGEUTILS ***
        String vehicleImageUrl = "";
        String vehicleImageAlt = "";
        
        if (request.getAttribute("veicolo") != null) {
            it.easyridedb.model.Veicolo veicoloObj = (it.easyridedb.model.Veicolo) request.getAttribute("veicolo");
            
            vehicleImageUrl = ImageUtils.getVehicleImageUrl(
                veicoloObj.getImmagineUrl(), 
                veicoloObj.getTarga(), 
                application
            );
            
            vehicleImageAlt = ImageUtils.getImageAltText(
                veicoloObj.getMarca(), 
                veicoloObj.getModello(), 
                veicoloObj.getTarga()
            );
        }
        
        // Rendi disponibili per EL
        pageContext.setAttribute("vehicleImageUrl", vehicleImageUrl);
        pageContext.setAttribute("vehicleImageAlt", vehicleImageAlt);
    %>
    
    <!-- Header -->
    <%@ include file="/WEB-INF/includes/header.jsp" %>
    
    <!-- Breadcrumb -->
    <div class="breadcrumb-container">
        <div class="container">
            <nav class="breadcrumb">
                <a href="${pageContext.request.contextPath}/" class="breadcrumb-item">
                    <i class="fas fa-home"></i> Home
                </a>
                <span class="breadcrumb-separator">›</span>
                <a href="${pageContext.request.contextPath}/catalogo" class="breadcrumb-item">Catalogo</a>
                <span class="breadcrumb-separator">›</span>
                <span class="breadcrumb-current">${veicolo.marca} ${veicolo.modello}</span>
            </nav>
        </div>
    </div>
    
    <!-- Main Content -->
    <main class="main-content">
        <div class="container">
            
            <!-- Alert Messages -->
            <c:if test="${not empty successMessage}">
                <div class="alert alert-success" id="successAlert">
                    <i class="fas fa-check-circle"></i>
                    <span>${successMessage}</span>
                    <button type="button" class="alert-close" onclick="closeAlert('successAlert')">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
            </c:if>
            
            <c:if test="${not empty errorMessage}">
                <div class="alert alert-error" id="errorAlert">
                    <i class="fas fa-exclamation-triangle"></i>
                    <span>${errorMessage}</span>
                    <button type="button" class="alert-close" onclick="closeAlert('errorAlert')">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
            </c:if>
            
            <!-- Vehicle Detail Layout -->
            <div class="vehicle-detail-layout">
                
                <!-- Left Column - Image & Gallery -->
                <div class="vehicle-images">
                    <div class="main-image-container">
                        <img src="${pageContext.request.contextPath}/${vehicleImageUrl}" 
                             alt="${vehicleImageAlt}" 
                             class="main-image" 
                             id="mainImage"
                             onclick="openImageLightbox(this)"
                             onerror="this.src='${pageContext.request.contextPath}/images/veicoli/default.jpg';">
                        
                        <!-- Hint zoom -->
                        <div class="zoom-hint">
                            <i class="fas fa-search-plus"></i>
                            Clicca per ingrandire
                        </div>
                        
                        <!-- Availability Badge -->
                        <div class="availability-badge ${veicolo.disponibile ? 'available' : 'unavailable'}">
                            <i class="fas ${veicolo.disponibile ? 'fa-check-circle' : 'fa-times-circle'}"></i>
                            <span>${veicolo.disponibile ? 'Disponibile' : 'Non Disponibile'}</span>
                        </div>
                        
                        <!-- Vehicle Type Badge -->
                        <c:if test="${not empty veicolo.tipo}">
                            <div class="type-badge">
                                <i class="fas fa-car"></i>
                                <span>${veicolo.tipo}</span>
                            </div>
                        </c:if>
                    </div>
                    
                    <!-- Thumbnail Gallery -->
                    <div class="thumbnail-gallery">
                        <div class="thumbnail active" onclick="changeMainImage('${pageContext.request.contextPath}/${vehicleImageUrl}')">
                            <img src="${pageContext.request.contextPath}/${vehicleImageUrl}" 
                                 alt="Vista principale"
                                 onerror="this.src='${pageContext.request.contextPath}/images/veicoli/default.jpg';">
                        </div>
                        <!-- Placeholder thumbnails -->
                        <div class="thumbnail" onclick="changeMainImage('${pageContext.request.contextPath}/images/car-interior.jpg')">
                            <img src="${pageContext.request.contextPath}/images/car-interior.jpg" 
                                 alt="Interni"
                                 onerror="this.style.display='none';">
                        </div>
                        <div class="thumbnail" onclick="changeMainImage('${pageContext.request.contextPath}/images/car-engine.jpg')">
                            <img src="${pageContext.request.contextPath}/images/car-engine.jpg" 
                                 alt="Motore"
                                 onerror="this.style.display='none';">
                        </div>
                    </div>
                </div>
                
                <!-- Right Column - Details & Booking -->
                <div class="vehicle-info">
                    
                    <!-- Vehicle Header -->
                    <div class="vehicle-header">
                        <h1 class="vehicle-title">
                            <span class="brand">${veicolo.marca}</span>
                            <span class="model">${veicolo.modello}</span>
                        </h1>
                        
                        <div class="vehicle-subtitle">
                            <span class="targa">Targa: ${veicolo.targa}</span>
                            <c:if test="${not empty terminal}">
                                <span class="terminal">
                                    <i class="fas fa-map-marker-alt"></i>
                                    ${terminal.nome}
                                </span>
                            </c:if>
                        </div>
                        
                        <!-- Price -->
                        <div class="price-section">
                            <div class="main-price">
                                <span class="currency">€</span>
                                <span class="amount"><fmt:formatNumber value="${veicolo.prezzoPerGiorno}" pattern="0.00"/></span>
                                <span class="period">/giorno</span>
                            </div>
                            <c:if test="${not empty prezzoEsempio}">
                                <div class="price-note">
                                    <i class="fas fa-info-circle"></i>
                                    Prezzo esempio 3 giorni: €<fmt:formatNumber value="${prezzoEsempio}" pattern="0.00"/>
                                </div>
                            </c:if>
                        </div>
                    </div>
                    
                    <!-- Vehicle Specifications -->
                    <div class="specifications">
                        <h3 class="section-title">
                            <i class="fas fa-cogs"></i>
                            Specifiche Tecniche
                        </h3>
                        
                        <div class="specs-grid">
                            <div class="spec-item">
                                <div class="spec-icon">
                                    <i class="fas fa-gas-pump"></i>
                                </div>
                                <div class="spec-content">
                                    <span class="spec-label">Carburante</span>
                                    <span class="spec-value">${veicolo.carburanteDisplay}</span>
                                </div>
                            </div>
                            
                            <div class="spec-item">
                                <div class="spec-icon">
                                    <i class="fas fa-cog"></i>
                                </div>
                                <div class="spec-content">
                                    <span class="spec-label">Trasmissione</span>
                                    <span class="spec-value">${veicolo.trasmissioneDisplay}</span>
                                </div>
                            </div>
                            
                            <div class="spec-item">
                                <div class="spec-icon">
                                    <i class="fas fa-users"></i>
                                </div>
                                <div class="spec-content">
                                    <span class="spec-label">Passeggeri</span>
                                    <span class="spec-value">5 persone</span>
                                </div>
                            </div>
                            
                            <div class="spec-item">
                                <div class="spec-icon">
                                    <i class="fas fa-suitcase"></i>
                                </div>
                                <div class="spec-content">
                                    <span class="spec-label">Bagagli</span>
                                    <span class="spec-value">3 valigie</span>
                                </div>
                            </div>
                            
                            <c:if test="${veicolo.ecologico}">
                                <div class="spec-item eco">
                                    <div class="spec-icon">
                                        <i class="fas fa-leaf"></i>
                                    </div>
                                    <div class="spec-content">
                                        <span class="spec-label">Ecologico</span>
                                        <span class="spec-value">Zero emissioni</span>
                                    </div>
                                </div>
                            </c:if>
                        </div>
                    </div>
                    
                    <!-- Availability Status - CORRETTO CON CONTROLLI NULL -->
                    <c:if test="${not empty statistiche}">
                        <div class="availability-section">
                            <h3 class="section-title">
                                <i class="fas fa-calendar-check"></i>
                                Disponibilità
                            </h3>
                            
                            <div class="availability-stats">
                                <div class="stat-item">
                                    <div class="stat-circle ${not empty statistiche.classeCSS ? statistiche.classeCSS : 'info'}">
                                        <span class="stat-percentage">
                                            <c:choose>
                                                <c:when test="${not empty statistiche.percentualeDisponibilita}">
                                                    ${statistiche.percentualeDisponibilita}%
                                                </c:when>
                                                <c:otherwise>N/A</c:otherwise>
                                            </c:choose>
                                        </span>
                                    </div>
                                    <div class="stat-label">
                                        <c:choose>
                                            <c:when test="${not empty statistiche.descrizioneDisponibilita}">
                                                ${statistiche.descrizioneDisponibilita}
                                            </c:when>
                                            <c:otherwise>Disponibilità in caricamento...</c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                                
                                <div class="availability-details">
                                    <c:if test="${not empty statistiche.giorniDisponibili}">
                                        <div class="detail-row">
                                            <i class="fas fa-check-circle text-success"></i>
                                            <span>${statistiche.giorniDisponibili} giorni disponibili (prossimi 30)</span>
                                        </div>
                                    </c:if>
                                    <c:if test="${not empty statistiche.giorniOccupati}">
                                        <div class="detail-row">
                                            <i class="fas fa-times-circle text-danger"></i>
                                            <span>${statistiche.giorniOccupati} giorni occupati</span>
                                        </div>
                                    </c:if>
                                    <c:if test="${not empty statistiche.prossimaDataDisponibile}">
                                        <div class="detail-row">
                                            <i class="fas fa-calendar-alt text-info"></i>
                                            <span>Prossima data libera: <fmt:formatDate value="${statistiche.prossimaDataDisponibile}" pattern="dd/MM/yyyy"/></span>
                                        </div>
                                    </c:if>
                                </div>
                            </div>
                        </div>
                    </c:if>
                    
                    <!-- Booking Form -->
                    <div class="booking-section">
                        <h3 class="section-title">
                            <i class="fas fa-calendar-plus"></i>
                            Prenota Subito
                        </h3>
                        
                        <form class="booking-form" id="bookingForm" method="post" action="${pageContext.request.contextPath}/veicolo-dettaglio">
                            <input type="hidden" name="targa" value="${veicolo.targa}">
                            <input type="hidden" name="action" value="add-to-cart">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                            
                            <div class="form-row">
                                <div class="form-group">
                                    <label for="dataRitiro">
                                        <i class="fas fa-calendar-alt"></i>
                                        Data Ritiro
                                    </label>
                                    <input type="date" 
                                           id="dataRitiro" 
                                           name="dataRitiro" 
                                           value="${dataRitiroDefault}"
                                           required
                                           class="form-control">
                                </div>
                                
                                <div class="form-group">
                                    <label for="dataRestituzione">
                                        <i class="fas fa-calendar-alt"></i>
                                        Data Restituzione
                                    </label>
                                    <input type="date" 
                                           id="dataRestituzione" 
                                           name="dataRestituzione" 
                                           value="${dataRestituzioneDefault}"
                                           required
                                           class="form-control">
                                </div>
                            </div>
                            
                            <!-- Price Calculator -->
                            <div class="price-calculator" id="priceCalculator">
                                <div class="calculator-row">
                                    <span class="calculator-label">Giorni selezionati:</span>
                                    <span class="calculator-value" id="selectedDays">3</span>
                                </div>
                                <div class="calculator-row">
                                    <span class="calculator-label">Prezzo per giorno:</span>
                                    <span class="calculator-value">€<fmt:formatNumber value="${veicolo.prezzoPerGiorno}" pattern="0.00"/></span>
                                </div>
                                <div class="calculator-row total">
                                    <span class="calculator-label">Totale stimato:</span>
                                    <span class="calculator-value" id="totalPrice">
                                        <c:choose>
                                            <c:when test="${not empty prezzoEsempio}">
                                                €<fmt:formatNumber value="${prezzoEsempio}" pattern="0.00"/>
                                            </c:when>
                                            <c:otherwise>€0.00</c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>
                            </div>
                            
                            <!-- Availability Check -->
                            <div class="availability-check" id="availabilityCheck" style="display: none;">
                                <div class="check-result" id="checkResult"></div>
                            </div>
                            
                            <!-- Action Buttons -->
                            <div class="booking-actions">
                                <button type="button" 
                                        class="btn btn-secondary btn-check" 
                                        id="checkAvailabilityBtn">
                                    <i class="fas fa-search"></i>
                                    Verifica Disponibilità
                                </button>
                                
                                <button type="submit" 
                                        class="btn btn-primary btn-book" 
                                        id="addToCartBtn">
                                    <i class="fas fa-shopping-cart"></i>
                                    Aggiungi al Carrello
                                </button>
                                
                                <c:if test="${not empty sessionScope.utente}">
                                    <button type="button" 
                                            class="btn btn-success btn-quick" 
                                            id="quickBookBtn">
                                        <i class="fas fa-bolt"></i>
                                        Prenota Subito
                                    </button>
                                </c:if>
                            </div>
                            
                            <div class="booking-note">
                                <i class="fas fa-info-circle"></i>
                                <small>Prenotazione senza impegno. Potrai cancellare gratuitamente fino a 24h prima.</small>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
            
            <!-- Additional Sections -->
            <div class="additional-sections">
                
                <!-- Features & Included -->
                <div class="features-section">
                    <h3 class="section-title">
                        <i class="fas fa-star"></i>
                        Caratteristiche Incluse
                    </h3>
                    
                    <div class="features-grid">
                        <div class="feature-item">
                            <i class="fas fa-shield-alt"></i>
                            <span>Assicurazione Completa</span>
                        </div>
                        <div class="feature-item">
                            <i class="fas fa-gas-pump"></i>
                            <span>Serbatoio Pieno</span>
                        </div>
                        <div class="feature-item">
                            <i class="fas fa-road"></i>
                            <span>Chilometraggio Illimitato</span>
                        </div>
                        <div class="feature-item">
                            <i class="fas fa-phone"></i>
                            <span>Assistenza 24/7</span>
                        </div>
                        <div class="feature-item">
                            <i class="fas fa-map-marked-alt"></i>
                            <span>GPS Incluso</span>
                        </div>
                        <div class="feature-item">
                            <i class="fas fa-wifi"></i>
                            <span>WiFi a Bordo</span>
                        </div>
                    </div>
                </div>
                
                <!-- Related Vehicles -->
                <c:if test="${not empty veicoliCorrelati}">
                    <div class="related-vehicles">
                        <h3 class="section-title">
                            <i class="fas fa-car"></i>
                            Veicoli Simili
                        </h3>
                        
                        <div class="related-grid">
                            <c:forEach items="${veicoliCorrelati}" var="correlato">
                                <div class="related-card">
                                    <div class="related-image">
                                        <c:choose>
                                            <c:when test="${not empty correlato.immagineUrl}">
                                                <img src="${correlato.immagineUrl}" 
                                                     alt="${correlato.marca} ${correlato.modello}">
                                            </c:when>
                                            <c:otherwise>
                                                <img src="${pageContext.request.contextPath}/images/default-car.jpg" 
                                                     alt="${correlato.marca} ${correlato.modello}">
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="related-info">
                                        <h4 class="related-title">${correlato.marca} ${correlato.modello}</h4>
                                        <div class="related-specs">
                                            <span class="spec">${correlato.carburanteDisplay}</span>
                                            <span class="spec">${correlato.trasmissioneDisplay}</span>
                                        </div>
                                        <div class="related-price">
                                            €<fmt:formatNumber value="${correlato.prezzoPerGiorno}" pattern="0.00"/>/giorno
                                        </div>
                                        <a href="${pageContext.request.contextPath}/veicolo-dettaglio?targa=${correlato.targa}" 
                                           class="btn btn-outline btn-sm">
                                            Vedi Dettagli
                                        </a>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </c:if>
            </div>
            
            <!-- Lightbox per ingrandire immagine -->
            <div id="imageLightbox" class="image-lightbox" onclick="closeImageLightbox()" style="display: none;">
                <span class="lightbox-close" onclick="closeImageLightbox()">
                    <i class="fas fa-times"></i>
                </span>
                <img id="lightboxImage" class="lightbox-image" src="" alt="">
            </div>
            
        </div>
    </main>
    
    <!-- Footer -->
    <%@ include file="/WEB-INF/includes/footer.jsp" %>
    
    <!-- JavaScript - Store price for calculator -->
    <script>
        // Global variable for price calculation
        window.vehiclePrice = ${not empty veicolo.prezzoPerGiorno ? veicolo.prezzoPerGiorno : 0};
        window.vehicleTarga = '${not empty veicolo.targa ? veicolo.targa : ""}';
        window.csrfToken = '${not empty csrfToken ? csrfToken : ""}';
        window.contextPath = '${pageContext.request.contextPath}';
    </script>
    
    <!-- External JavaScript -->
    <script src="${pageContext.request.contextPath}/scripts/veicolo-dettaglio.js"></script>
    
    <!-- Page-specific JavaScript -->
    <script>
        // *** FUNZIONI PER GESTIONE IMMAGINI ***
        
        // Apri lightbox per ingrandire immagine
        function openImageLightbox(img) {
            const lightbox = document.getElementById('imageLightbox');
            const lightboxImage = document.getElementById('lightboxImage');
            
            if (lightbox && lightboxImage) {
                lightboxImage.src = img.src;
                lightboxImage.alt = img.alt;
                lightbox.style.display = 'block';
                document.body.style.overflow = 'hidden';
            }
        }
        
        // Chiudi lightbox
        function closeImageLightbox() {
            const lightbox = document.getElementById('imageLightbox');
            if (lightbox) {
                lightbox.style.display = 'none';
                document.body.style.overflow = 'auto';
            }
        }
        
        // Cambia immagine principale tramite thumbnail
        function changeMainImage(newSrc) {
            const mainImage = document.getElementById('mainImage');
            if (mainImage) {
                mainImage.src = newSrc;
                
                // Aggiorna thumbnail attive
                document.querySelectorAll('.thumbnail').forEach(thumb => {
                    thumb.classList.remove('active');
                });
                event.currentTarget.classList.add('active');
            }
        }
        
        // Chiudi lightbox con ESC
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape' || e.key === 'Esc') {
                closeImageLightbox();
            }
        });
        
        // Chiudi lightbox cliccando fuori dall'immagine
        document.addEventListener('DOMContentLoaded', function() {
            const lightbox = document.getElementById('imageLightbox');
            if (lightbox) {
                lightbox.addEventListener('click', function(e) {
                    if (e.target === this) {
                        closeImageLightbox();
                    }
                });
            }
        });
        
        // Quick book button handler
        function handleQuickBook() {
            const form = document.getElementById('bookingForm');
            const actionInput = form.querySelector('input[name="action"]');
            if (actionInput) {
                actionInput.value = 'quick-booking';
                form.submit();
            }
        }
        
        // Close alert function
        function closeAlert(alertId) {
            const alert = document.getElementById(alertId);
            if (alert) {
                alert.style.opacity = '0';
                setTimeout(() => alert.remove(), 300);
            }
        }
        
        // Initialize quick book button after page load
        document.addEventListener('DOMContentLoaded', function() {
            const quickBookBtn = document.getElementById('quickBookBtn');
            if (quickBookBtn) {
                quickBookBtn.addEventListener('click', handleQuickBook);
            }
        });
    </script>
</body>
</html>