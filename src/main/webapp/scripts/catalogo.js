

document.addEventListener('DOMContentLoaded', function() {
    
    // Elementi del DOM - AGGIORNATI PER CORRISPONDERE AL JSP
    var filterForm = document.getElementById('filter-form');
    var sortSelect = document.getElementById('sort-select');
    var clearFiltersBtn = document.getElementById('clear-filters');
    var vehicleCards = document.querySelectorAll('.vehicle-card');
    var resultsCount = document.getElementById('results-count');
    
    // Inizializzazione SOLO se gli elementi esistono
    if (filterForm) initializeFilters();
    if (sortSelect) initializeSorting();
    if (vehicleCards.length > 0) initializeVehicleCards();
    if (resultsCount) updateResultsCount();
    initializeNotifications();
    
    // === GESTIONE FILTRI ===
    
    function initializeFilters() {
        // Validazione filtri prezzo in tempo reale
        var prezzoMinInput = document.getElementById('prezzoMin');
        var prezzoMaxInput = document.getElementById('prezzoMax');
        
        if (prezzoMinInput && prezzoMaxInput) {
            prezzoMinInput.addEventListener('input', validatePriceRange);
            prezzoMaxInput.addEventListener('input', validatePriceRange);
            prezzoMinInput.addEventListener('blur', validatePriceRange);
            prezzoMaxInput.addEventListener('blur', validatePriceRange);
        }
        
        // Auto-submit filtri quando cambiano
        if (filterForm) {
            var filterInputs = filterForm.querySelectorAll('input, select');
            for (var i = 0; i < filterInputs.length; i++) {
                var input = filterInputs[i];
                if (input.type !== 'submit' && input.id !== 'disponibili') {
                    input.addEventListener('change', debounce(autoSubmitFilters, 500));
                }
            }
        }
        
        // Checkbox disponibili submit immediato
        var disponibiliCheckbox = document.getElementById('disponibili');
        if (disponibiliCheckbox) {
            disponibiliCheckbox.addEventListener('change', autoSubmitFilters);
        }
        
        // Clear filters
        if (clearFiltersBtn) {
            clearFiltersBtn.addEventListener('click', clearAllFilters);
        }
        
        // Validazione modello con regex
        var modelloInput = document.getElementById('modello');
        if (modelloInput) {
            modelloInput.addEventListener('input', validateModello);
        }
    }
    
    function validateModello() {
        var modelloInput = document.getElementById('modello');
        if (!modelloInput) return true;
        
        var modello = modelloInput.value.trim();
        var regex = /^[a-zA-Z0-9\s\-_]{0,50}$/;
        
        if (modello && !regex.test(modello)) {
            showFieldError(modelloInput, null, 'Il modello può contenere solo lettere, numeri, spazi e trattini');
            return false;
        } else {
            clearFieldError(modelloInput);
            return true;
        }
    }
    
    function validatePriceRange() {
        var prezzoMinInput = document.getElementById('prezzoMin');
        var prezzoMaxInput = document.getElementById('prezzoMax');
        
        // Controlli null
        if (!prezzoMinInput || !prezzoMaxInput) return true;
        
        var prezzoMin = parseFloat(prezzoMinInput.value) || 0;
        var prezzoMax = parseFloat(prezzoMaxInput.value) || 0;
        
        var minError = document.getElementById('prezzo-min-error');
        var maxError = document.getElementById('prezzo-max-error');
        
        // Reset errori
        clearFieldError(prezzoMinInput);
        clearFieldError(prezzoMaxInput);
        
        var isValid = true;
        
        // Validazione range
        if (prezzoMinInput.value && prezzoMin < 0) {
            showFieldError(prezzoMinInput, minError, 'Il prezzo minimo non può essere negativo');
            isValid = false;
        }
        
        if (prezzoMaxInput.value && prezzoMax < 0) {
            showFieldError(prezzoMaxInput, maxError, 'Il prezzo massimo non può essere negativo');
            isValid = false;
        }
        
        if (prezzoMinInput.value && prezzoMaxInput.value && prezzoMin > prezzoMax) {
            showFieldError(prezzoMaxInput, maxError, 'Il prezzo massimo deve essere maggiore del minimo');
            isValid = false;
        }
        
        // Validazione valori ragionevoli
        if (prezzoMin > 10000) {
            showFieldError(prezzoMinInput, minError, 'Il prezzo sembra troppo alto');
            isValid = false;
        }
        
        if (prezzoMax > 10000) {
            showFieldError(prezzoMaxInput, maxError, 'Il prezzo sembra troppo alto');
            isValid = false;
        }
        
        return isValid;
    }
    
    function showFieldError(input, errorElement, message) {
        if (input) {
            input.classList.add('error');
            input.setAttribute('aria-invalid', 'true');
        }
        if (errorElement) {
            errorElement.textContent = message;
            errorElement.style.display = 'block';
        }
    }
    
    function clearFieldError(input) {
        if (input) {
            input.classList.remove('error');
            input.removeAttribute('aria-invalid');
        }
        // Trova e pulisci il messaggio di errore associato
        var errorElement = document.getElementById(input.id + '-error');
        if (errorElement) {
            errorElement.textContent = '';
            errorElement.style.display = 'none';
        }
    }
    
    function autoSubmitFilters() {
        if (validatePriceRange() && validateModello() && filterForm) {
            // Aggiungi loading
            var submitBtn = filterForm.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '🔄 Filtrando...';
            }
            
            filterForm.submit();
        }
    }
    
    function clearAllFilters(e) {
        e.preventDefault();
        
        if (!filterForm) return;
        
        // Reset tutti gli input
        var inputs = filterForm.querySelectorAll('input[type="text"], input[type="number"]');
        for (var i = 0; i < inputs.length; i++) {
            inputs[i].value = '';
            clearFieldError(inputs[i]);
        }
        
        // Reset select
        var selects = filterForm.querySelectorAll('select');
        for (var j = 0; j < selects.length; j++) {
            selects[j].selectedIndex = 0;
        }
        
        // Reset checkbox
        var checkboxes = filterForm.querySelectorAll('input[type="checkbox"]');
        for (var k = 0; k < checkboxes.length; k++) {
            checkboxes[k].checked = false;
        }
        
        // Submit form vuoto
        filterForm.submit();
    }
    
    // === GESTIONE ORDINAMENTO ===
    
    function initializeSorting() {
        if (!sortSelect || !filterForm) return;
        
        sortSelect.addEventListener('change', function() {
            // Aggiunge parametro sort alla form e la invia
            var sortValue = this.value;
            
            // Rimuovi input sort precedenti
            var existingSortInputs = filterForm.querySelectorAll('input[name="sort"]');
            for (var i = 0; i < existingSortInputs.length; i++) {
                existingSortInputs[i].remove();
            }
            
            if (sortValue) {
                // Aggiungi nuovo input sort
                var sortInput = document.createElement('input');
                sortInput.type = 'hidden';
                sortInput.name = 'sort';
                sortInput.value = sortValue;
                filterForm.appendChild(sortInput);
            }
            
            filterForm.submit();
        });
    }
    
    // === GESTIONE CARD VEICOLI ===
    
    function initializeVehicleCards() {
        for (var i = 0; i < vehicleCards.length; i++) {
            var card = vehicleCards[i];
            
            // Animazione hover con transizione fluida
            card.addEventListener('mouseenter', function() {
                this.style.transition = 'transform 0.3s ease, box-shadow 0.3s ease';
                this.style.transform = 'translateY(-5px)';
                this.style.boxShadow = '0 8px 25px rgba(0,0,0,0.15)';
            });
            
            card.addEventListener('mouseleave', function() {
                this.style.transform = 'translateY(0)';
                this.style.boxShadow = '';
            });
            
            // Gestione click su card
            card.addEventListener('click', function(e) {
                // Non attivare se si clicca sui pulsanti o sui loro contenuti
                if (e.target.tagName === 'BUTTON' || 
                    e.target.closest('button') || 
                    e.target.closest('.vehicle-actions')) {
                    return;
                }
                
                var targa = this.dataset.targa;
                if (targa) {
                    window.location.href = 'veicolo-dettaglio?targa=' + encodeURIComponent(targa);
                }
            });
            
            // Gestione pulsanti azioni
            var addToCartBtn = card.querySelector('.btn-add-cart');
            var viewDetailsBtn = card.querySelector('.btn-view-details');
            
            if (addToCartBtn) {
                addToCartBtn.addEventListener('click', function(e) {
                    e.stopPropagation();
                    handleAddToCart(this);
                });
            }
            
            if (viewDetailsBtn) {
                viewDetailsBtn.addEventListener('click', function(e) {
                    e.stopPropagation();
                    var targa = this.dataset.targa;
                    if (targa) {
                        window.location.href = 'veicolo-dettaglio?targa=' + encodeURIComponent(targa);
                    }
                });
            }
        }
    }
    
    function handleAddToCart(button) {
        var targa = button.dataset.targa;
        
        if (!targa) {
            showNotification('Errore: Veicolo non trovato', 'error');
            return;
        }
        
        // Verifica login
        var isLoggedIn = document.body.dataset.userLoggedIn === 'true';
        if (!isLoggedIn) {
            if (confirm('Devi effettuare il login per prenotare. Vuoi andare alla pagina di login?')) {
                // Salva la pagina corrente per il redirect dopo login
                var currentUrl = window.location.href;
                window.location.href = 'login?redirect=' + encodeURIComponent(currentUrl);
            }
            return;
        }
        
        // Disabilita pulsante temporaneamente
        button.disabled = true;
        var originalText = button.innerHTML;
        button.innerHTML = '⏳ Aggiungendo...';
        
        // Crea form nascosto per POST con CSRF
        var form = document.createElement('form');
        form.method = 'POST';
        form.action = 'catalogo';
        form.style.display = 'none';
        
        // Token CSRF
        var csrfMeta = document.querySelector('meta[name="csrf-token"]');
        var csrfToken = csrfMeta ? csrfMeta.getAttribute('content') : '';
        if (csrfToken) {
            var csrfInput = document.createElement('input');
            csrfInput.type = 'hidden';
            csrfInput.name = 'csrfToken';
            csrfInput.value = csrfToken;
            form.appendChild(csrfInput);
        }
        
        // Action e targa
        var actionInput = document.createElement('input');
        actionInput.type = 'hidden';
        actionInput.name = 'action';
        actionInput.value = 'addToCart';
        form.appendChild(actionInput);
        
        var targaInput = document.createElement('input');
        targaInput.type = 'hidden';
        targaInput.name = 'targa';
        targaInput.value = targa;
        form.appendChild(targaInput);
        
        document.body.appendChild(form);
        form.submit();
        
        // Ripristina pulsante in caso di errore (timeout di sicurezza)
        setTimeout(function() {
            if (button) {
                button.disabled = false;
                button.innerHTML = originalText;
            }
            if (form && form.parentNode) {
                form.parentNode.removeChild(form);
            }
        }, 10000);
    }
    
    // === GESTIONE NOTIFICHE ===
    
    function initializeNotifications() {
        // Auto-hide per messaggi esistenti
        var messages = document.querySelectorAll('.message');
        for (var i = 0; i < messages.length; i++) {
            var message = messages[i];
            
            // Auto-hide dopo 5 secondi per messaggi di successo e welcome
            if (message.classList.contains('success') || message.classList.contains('welcome')) {
                setTimeout(function(msg) {
                    return function() { hideMessage(msg); };
                }(message), 5000);
            }
            
            // Aggiungi pulsante di chiusura se non esiste
            if (!message.querySelector('.close-btn')) {
                var closeBtn = document.createElement('button');
                closeBtn.type = 'button';
                closeBtn.className = 'close-btn';
                closeBtn.innerHTML = '×';
                closeBtn.onclick = function(msg) {
                    return function() { hideMessage(msg); };
                }(message);
                message.appendChild(closeBtn);
            }
        }
    }
    
    function hideMessage(messageElement) {
        if (messageElement) {
            messageElement.style.opacity = '0';
            messageElement.style.transform = 'translateY(-20px)';
            setTimeout(function() {
                if (messageElement.parentNode) {
                    messageElement.parentNode.removeChild(messageElement);
                }
            }, 300);
        }
    }
    
    // === UTILITY FUNCTIONS ===
    
    function updateResultsCount() {
        if (!resultsCount) return;
        
        var count = vehicleCards.length;
        var text = count === 0 ? 'Nessun veicolo trovato' :
                   count === 1 ? '1 veicolo trovato' : 
                   count + ' veicoli trovati';
        resultsCount.textContent = text;
    }
    
    function showNotification(message, type) {
        type = type || 'info';
        
        // Rimuovi notifiche esistenti dello stesso tipo
        var existingNotifications = document.querySelectorAll('.notification.' + type);
        for (var i = 0; i < existingNotifications.length; i++) {
            existingNotifications[i].remove();
        }
        
        // Crea notification toast
        var notification = document.createElement('div');
        notification.className = 'notification ' + type;
        notification.innerHTML = 
            '<div class="notification-content">' +
                '<span class="notification-message">' + message + '</span>' +
                '<button type="button" class="notification-close" onclick="this.parentElement.parentElement.remove()" title="Chiudi">×</button>' +
            '</div>';
        
        // Stili inline per assicurarsi che funzioni
        var bgColor = type === 'error' ? '#dc3545' : type === 'success' ? '#28a745' : '#17a2b8';
        
        notification.style.position = 'fixed';
        notification.style.top = '20px';
        notification.style.right = '20px';
        notification.style.background = bgColor;
        notification.style.color = 'white';
        notification.style.padding = '15px 20px';
        notification.style.borderRadius = '5px';
        notification.style.boxShadow = '0 4px 12px rgba(0,0,0,0.15)';
        notification.style.zIndex = '9999';
        notification.style.minWidth = '300px';
        notification.style.maxWidth = '500px';
        
        // Aggiungi al body
        document.body.appendChild(notification);
        
        // Auto-remove dopo 5 secondi
        setTimeout(function() {
            if (notification.parentElement) {
                notification.style.opacity = '0';
                notification.style.transform = 'translateX(100%)';
                setTimeout(function() {
                    if (notification.parentElement) {
                        notification.remove();
                    }
                }, 300);
            }
        }, 5000);
    }
    
    function debounce(func, wait) {
        var timeout;
        return function() {
            var context = this;
            var args = arguments;
            var later = function() {
                timeout = null;
                func.apply(context, args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }
    
    // === LAZY LOADING IMAGES ===
    
    function initializeLazyLoading() {
        if (window.IntersectionObserver) {
            var imageObserver = new IntersectionObserver(function(entries, observer) {
                for (var i = 0; i < entries.length; i++) {
                    var entry = entries[i];
                    if (entry.isIntersecting) {
                        var img = entry.target;
                        if (img.dataset.src) {
                            // Precarica l'immagine
                            var imageLoader = new Image();
                            imageLoader.onload = function() {
                                img.src = img.dataset.src;
                                img.classList.remove('lazy');
                                img.classList.add('loaded');
                            };
                            imageLoader.onerror = function() {
                                // Fallback in caso di errore
                                img.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjEyMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZGRkIi8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIxNCIgZmlsbD0iIzk5OSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPkltbWFnaW5lIG5vbiBkaXNwb25pYmlsZTwvdGV4dD48L3N2Zz4=';
                                img.classList.remove('lazy');
                                img.classList.add('error');
                            };
                            imageLoader.src = img.dataset.src;
                            observer.unobserve(img);
                        }
                    }
                }
            }, {
                rootMargin: '50px 0px',
                threshold: 0.01
            });
            
            // Osserva tutte le immagini lazy
            var lazyImages = document.querySelectorAll('img[data-src]');
            for (var i = 0; i < lazyImages.length; i++) {
                lazyImages[i].classList.add('lazy');
                imageObserver.observe(lazyImages[i]);
            }
        } else {
            // Fallback per browser che non supportano IntersectionObserver
            var fallbackImages = document.querySelectorAll('img[data-src]');
            for (var i = 0; i < fallbackImages.length; i++) {
                var img = fallbackImages[i];
                img.src = img.dataset.src;
                img.classList.remove('lazy');
            }
        }
    }
    
    // Inizializza lazy loading
    initializeLazyLoading();
    
    // === ACCESSIBILITY IMPROVEMENTS ===
    
    // Gestione keyboard navigation
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Enter' && e.target.classList.contains('vehicle-card')) {
            e.target.click();
        }
    });
    
    // Aggiungi attributi ARIA
    for (var i = 0; i < vehicleCards.length; i++) {
        var card = vehicleCards[i];
        card.setAttribute('role', 'article');
        card.setAttribute('aria-label', 'Veicolo ' + (i + 1));
        card.setAttribute('tabindex', '0');
    }
    
    // === GESTIONE ERRORI GLOBALI ===
    
    window.addEventListener('error', function(e) {
        console.error('Errore JavaScript:', e.error);
        // Non mostrare errori tecnici all'utente in produzione
        if (window.location.hostname === 'localhost' || window.location.hostname.indexOf('dev') !== -1) {
            showNotification('Errore tecnico: ' + e.message, 'error');
        }
    });
    
    // Gestione errori Promise non catturate (per browser che supportano Promise)
    if (window.Promise && window.addEventListener) {
        window.addEventListener('unhandledrejection', function(e) {
            console.error('Promise rejection non gestita:', e.reason);
            e.preventDefault();
        });
    }
});