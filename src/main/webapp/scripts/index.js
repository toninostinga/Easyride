

document.addEventListener('DOMContentLoaded', function() {
    
    // Inizializzazione
    initializeScrollAnimations();
    initializeMessages();
    initializeQuickSearch();
    initializeStatsCounter();
    initializeFeatureCards();
    
    // === GESTIONE MESSAGGI ===
    
    function initializeMessages() {
        var messages = document.querySelectorAll('.message');
        
        for (var i = 0; i < messages.length; i++) {
            var message = messages[i];
            
            // Auto-hide per messaggi di successo e welcome dopo 5 secondi
            if (message.classList.contains('success') || message.classList.contains('welcome')) {
                setTimeout(function(msg) {
                    return function() {
                        hideMessage(msg);
                    };
                }(message), 5000);
            }
            
            // Aggiungi event listener al pulsante chiudi
            var closeBtn = message.querySelector('.close-btn');
            if (closeBtn) {
                closeBtn.addEventListener('click', function(msg) {
                    return function() {
                        hideMessage(msg);
                    };
                }(message));
            }
        }
    }
    
    function hideMessage(messageElement) {
        if (messageElement) {
            messageElement.style.opacity = '0';
            messageElement.style.transform = 'translateX(100%)';
            setTimeout(function() {
                if (messageElement.parentNode) {
                    messageElement.parentNode.removeChild(messageElement);
                }
            }, 300);
        }
    }
    
    // === ANIMAZIONI SCROLL ===
    
    function initializeScrollAnimations() {
        // Intersection Observer per animazioni al scroll
        if (window.IntersectionObserver) {
            var observerOptions = {
                threshold: 0.1,
                rootMargin: '0px 0px -50px 0px'
            };
            
            var observer = new IntersectionObserver(function(entries) {
                for (var i = 0; i < entries.length; i++) {
                    var entry = entries[i];
                    if (entry.isIntersecting) {
                        entry.target.classList.add('fade-in-up');
                        observer.unobserve(entry.target);
                    }
                }
            }, observerOptions);
            
            // Osserva elementi da animare
            var elementsToAnimate = document.querySelectorAll('.feature-card, .vehicle-type-card, .section-title, .section-subtitle');
            for (var i = 0; i < elementsToAnimate.length; i++) {
                observer.observe(elementsToAnimate[i]);
            }
        }
        
        // Parallax effect per hero (opzionale)
        var hero = document.querySelector('.hero');
        if (hero) {
            window.addEventListener('scroll', function() {
                var scrolled = window.pageYOffset;
                var rate = scrolled * -0.5;
                hero.style.transform = 'translateY(' + rate + 'px)';
            });
        }
    }
    
    // === QUICK SEARCH FORM ===
    
    function initializeQuickSearch() {
        var quickSearchForm = document.querySelector('.quick-search-form');
        if (!quickSearchForm) return;
        
        // Validazione form
        quickSearchForm.addEventListener('submit', function(e) {
            var isValid = validateQuickSearchForm();
            if (!isValid) {
                e.preventDefault();
                showNotification('Seleziona almeno un filtro per la ricerca', 'warning');
            }
        });
        
        // Auto-submit quando si cambia una selezione
        var selects = quickSearchForm.querySelectorAll('select');
        for (var i = 0; i < selects.length; i++) {
            selects[i].addEventListener('change', function() {
                // Aggiungi un piccolo delay per UX migliore
                setTimeout(function() {
                    var hasSelection = false;
                    var formSelects = quickSearchForm.querySelectorAll('select');
                    for (var j = 0; j < formSelects.length; j++) {
                        if (formSelects[j].value) {
                            hasSelection = true;
                            break;
                        }
                    }
                    
                    if (hasSelection) {
                        var submitBtn = quickSearchForm.querySelector('.btn-search');
                        if (submitBtn) {
                            submitBtn.textContent = '🔍 Ricerca in corso...';
                            submitBtn.disabled = true;
                        }
                        
                        // Auto-submit dopo una breve pausa
                        setTimeout(function() {
                            quickSearchForm.submit();
                        }, 500);
                    }
                }, 100);
            });
        }
    }
    
    function validateQuickSearchForm() {
        var quickSearchForm = document.querySelector('.quick-search-form');
        if (!quickSearchForm) return false;
        
        var selects = quickSearchForm.querySelectorAll('select');
        var hasValue = false;
        
        for (var i = 0; i < selects.length; i++) {
            if (selects[i].value && selects[i].value.trim() !== '') {
                hasValue = true;
                break;
            }
        }
        
        return hasValue;
    }
    
    // === COUNTER ANIMATO PER STATISTICHE ===
    
    function initializeStatsCounter() {
        var statNumbers = document.querySelectorAll('.stat-number');
        if (statNumbers.length === 0) return;
        
        var hasAnimated = false;
        
        function animateCounters() {
            if (hasAnimated) return;
            hasAnimated = true;
            
            for (var i = 0; i < statNumbers.length; i++) {
                animateCounter(statNumbers[i]);
            }
        }
        
        // Avvia animazione quando le stats sono visibili
        if (window.IntersectionObserver) {
            var observer = new IntersectionObserver(function(entries) {
                for (var i = 0; i < entries.length; i++) {
                    if (entries[i].isIntersecting) {
                        animateCounters();
                        observer.disconnect();
                        break;
                    }
                }
            }, { threshold: 0.5 });
            
            var heroStats = document.querySelector('.hero-stats');
            if (heroStats) {
                observer.observe(heroStats);
            }
        } else {
            // Fallback per browser senza IntersectionObserver
            setTimeout(animateCounters, 1000);
        }
    }
    
    function animateCounter(element) {
        var text = element.textContent;
        var hasPlus = text.includes('+');
        var number = parseInt(text.replace(/\D/g, ''));
        var suffix = text.replace(/[\d+]/g, '');
        
        if (isNaN(number)) return;
        
        var duration = 2000; // 2 secondi
        var startTime = null;
        var startValue = 0;
        
        function animate(currentTime) {
            if (!startTime) startTime = currentTime;
            var progress = Math.min((currentTime - startTime) / duration, 1);
            
            // Easing function (ease-out)
            var easedProgress = 1 - Math.pow(1 - progress, 3);
            var currentValue = Math.floor(startValue + (number - startValue) * easedProgress);
            
            element.textContent = currentValue + (hasPlus ? '+' : '') + suffix.replace(/\d/g, '');
            
            if (progress < 1) {
                requestAnimationFrame(animate);
            }
        }
        
        requestAnimationFrame(animate);
    }
    
    // === INTERAZIONI FEATURE CARDS ===
    
    function initializeFeatureCards() {
        var featureCards = document.querySelectorAll('.feature-card, .vehicle-type-card');
        
        for (var i = 0; i < featureCards.length; i++) {
            var card = featureCards[i];
            
            // Effetto tilt 3D al mouse
            card.addEventListener('mousemove', function(e) {
                var rect = this.getBoundingClientRect();
                var x = e.clientX - rect.left;
                var y = e.clientY - rect.top;
                
                var centerX = rect.width / 2;
                var centerY = rect.height / 2;
                
                var rotateX = (y - centerY) / 10;
                var rotateY = (centerX - x) / 10;
                
                this.style.transform = 'perspective(1000px) rotateX(' + rotateX + 'deg) rotateY(' + rotateY + 'deg) translateY(-8px)';
            });
            
            card.addEventListener('mouseleave', function() {
                this.style.transform = '';
            });
            
            // Aggiungi focus per accessibilità
            card.setAttribute('tabindex', '0');
            card.addEventListener('focus', function() {
                this.style.transform = 'translateY(-8px) scale(1.02)';
            });
            
            card.addEventListener('blur', function() {
                this.style.transform = '';
            });
        }
    }
    
    // === UTILITY FUNCTIONS ===
    
    function showNotification(message, type) {
        type = type || 'info';
        
        // Rimuovi notifiche esistenti
        var existingNotifications = document.querySelectorAll('.notification');
        for (var i = 0; i < existingNotifications.length; i++) {
            existingNotifications[i].remove();
        }
        
        // Crea nuova notifica
        var notification = document.createElement('div');
        notification.className = 'notification ' + type;
        notification.innerHTML = 
            '<div class="notification-content">' +
                '<span class="notification-message">' + message + '</span>' +
                '<button type="button" class="notification-close" onclick="this.parentElement.parentElement.remove()" title="Chiudi">×</button>' +
            '</div>';
        
        // Stili inline
        var bgColor = type === 'error' ? '#dc3545' : 
                     type === 'success' ? '#28a745' : 
                     type === 'warning' ? '#ffc107' : '#17a2b8';
        
        notification.style.cssText = 
            'position: fixed; top: 20px; right: 20px; background: ' + bgColor + '; ' +
            'color: white; padding: 15px 20px; border-radius: 8px; ' +
            'box-shadow: 0 4px 12px rgba(0,0,0,0.15); z-index: 10000; ' +
            'min-width: 300px; max-width: 500px; font-weight: 500;';
        
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
    
    // === SMOOTH SCROLLING PER LINK INTERNI ===
    
    var internalLinks = document.querySelectorAll('a[href^="#"]');
    for (var i = 0; i < internalLinks.length; i++) {
        internalLinks[i].addEventListener('click', function(e) {
            var targetId = this.getAttribute('href').substring(1);
            var targetElement = document.getElementById(targetId);
            
            if (targetElement) {
                e.preventDefault();
                targetElement.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    }
    
    // === PERFORMANCE: LAZY LOADING IMMAGINI ===
    
    function initializeLazyLoading() {
        if (window.IntersectionObserver) {
            var imageObserver = new IntersectionObserver(function(entries) {
                for (var i = 0; i < entries.length; i++) {
                    var entry = entries[i];
                    if (entry.isIntersecting) {
                        var img = entry.target;
                        if (img.dataset.src) {
                            img.src = img.dataset.src;
                            img.classList.remove('lazy');
                            imageObserver.unobserve(img);
                        }
                    }
                }
            });
            
            var lazyImages = document.querySelectorAll('img[data-src]');
            for (var i = 0; i < lazyImages.length; i++) {
                imageObserver.observe(lazyImages[i]);
            }
        }
    }
    
    initializeLazyLoading();
    
    // === GESTIONE ERRORI ===
    
    window.addEventListener('error', function(e) {
        console.error('Errore JavaScript:', e.error);
        // Non mostrare errori tecnici all'utente in produzione
        if (window.location.hostname === 'localhost' || window.location.hostname.indexOf('dev') !== -1) {
            showNotification('Errore tecnico: ' + e.message, 'error');
        }
    });
    
    // === ACCESSIBILITÀ ===
    
    // Gestione keyboard navigation per elementi interattivi
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Enter') {
            var target = e.target;
            if (target.classList.contains('feature-card') || target.classList.contains('vehicle-type-card')) {
                var link = target.querySelector('a');
                if (link) {
                    link.click();
                }
            }
        }
    });
    
    // Miglioramento focus visibility
    var focusableElements = document.querySelectorAll('a, button, input, select, [tabindex]');
    for (var i = 0; i < focusableElements.length; i++) {
        focusableElements[i].addEventListener('focus', function() {
            this.style.outline = '2px solid #007bff';
            this.style.outlineOffset = '2px';
        });
        
        focusableElements[i].addEventListener('blur', function() {
            this.style.outline = '';
            this.style.outlineOffset = '';
        });
    }
    
    console.log('✅ EasyRide Homepage inizializzata correttamente!');
});