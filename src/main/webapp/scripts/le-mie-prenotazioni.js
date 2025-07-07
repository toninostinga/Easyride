
document.addEventListener('DOMContentLoaded', function() {
    console.log('✅ Le mie prenotazioni JavaScript caricato');
    
   
    initializePrenotazioniPage();
    
    
    initializeMessageClosers();
    
    
    initializeCardAnimations();
});


function initializePrenotazioniPage() {
    animateStats();
    
    
    addTooltips();
    
   
    handleResponsiveActions();
}


function initializeMessageClosers() {
    var closeButtons = document.querySelectorAll('.close-btn');
    closeButtons.forEach(function(btn) {
        btn.addEventListener('click', function() {
            var message = this.parentElement;
            message.style.transition = 'opacity 0.3s ease';
            message.style.opacity = '0';
            
            setTimeout(function() {
                message.style.display = 'none';
            }, 300);
        });
    });
}


function initializeCardAnimations() {
    var cards = document.querySelectorAll('.prenotazione-card');
    
    
    if ('IntersectionObserver' in window) {
        var observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting) {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateY(0)';
                }
            });
        }, {
            threshold: 0.1
        });
        
        cards.forEach(function(card) {
            card.style.opacity = '0';
            card.style.transform = 'translateY(20px)';
            card.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
            observer.observe(card);
        });
    }
}


function animateStats() {
    var statNumbers = document.querySelectorAll('.stat-number');
    
    statNumbers.forEach(function(statNumber, index) {
        var finalValue = parseInt(statNumber.textContent);
        statNumber.textContent = '0';
        
        // Animazione contatore
        setTimeout(function() {
            animateCounter(statNumber, 0, finalValue, 1000);
        }, index * 200);
    });
}


function animateCounter(element, start, end, duration) {
    var startTime = Date.now();
    
    function updateCounter() {
        var elapsed = Date.now() - startTime;
        var progress = Math.min(elapsed / duration, 1);
        
        
        var easedProgress = 1 - Math.pow(1 - progress, 3);
        
        var currentValue = Math.round(start + (end - start) * easedProgress);
        element.textContent = currentValue;
        
        if (progress < 1) {
            requestAnimationFrame(updateCounter);
        }
    }
    
    requestAnimationFrame(updateCounter);
}


function viewDetails(bookingId) {
    console.log('Visualizza dettagli prenotazione:', bookingId);
    
    
    showNotification('Visualizzazione dettagli prenotazione #' + bookingId, 'info');
    
    
}


function cancelBooking(bookingId) {
    if (confirm('Sei sicuro di voler annullare questa prenotazione?\n\nQuesta azione non può essere annullata.')) {
        console.log('Annullamento prenotazione:', bookingId);
        
        
        var cancelButton = document.querySelector('[onclick*="cancelBooking(' + bookingId + ')"]');
        if (cancelButton) {
            cancelButton.disabled = true;
            cancelButton.innerHTML = '<span class="btn-icon">⏳</span> Annullamento...';
        }
        
       
        fetch('annulla-prenotazione', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: 'prenotazioneId=' + encodeURIComponent(bookingId) + '&action=annulla'
        })
        .then(response => {
            // Gestisci diversi status code
            if (response.status === 401) {
                throw new Error('Sessione scaduta. Effettua nuovamente il login.');
            } else if (response.status === 403) {
                throw new Error('Non sei autorizzato ad annullare questa prenotazione.');
            } else if (response.status === 404) {
                throw new Error('Prenotazione non trovata.');
            } else if (!response.ok) {
                throw new Error('Errore del server (' + response.status + ')');
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                showNotification('Prenotazione #' + bookingId + ' annullata con successo', 'success');
                
                
                setTimeout(function() {
                    window.location.reload();
                }, 1500);
            } else {
              
                showNotification('Errore: ' + data.message, 'error');
                
                
                if (cancelButton) {
                    cancelButton.disabled = false;
                    cancelButton.innerHTML = '<span class="btn-icon">❌</span> Annulla';
                }
            }
        })
        .catch(error => {
            console.error('Errore annullamento:', error);
            
            // Gestisci errori specifici
            if (error.message.includes('Sessione scaduta')) {
                showNotification(error.message + ' Reindirizzamento al login...', 'error');
                setTimeout(function() {
                    window.location.href = 'login?message=Sessione scaduta';
                }, 2000);
            } else {
                showNotification('Errore: ' + error.message, 'error');
            }
            
            // Riabilita il pulsante
            if (cancelButton) {
                cancelButton.disabled = false;
                cancelButton.innerHTML = '<span class="btn-icon">❌</span> Annulla';
            }
        });
    }
}


function repeatBooking(targa) {
    console.log('Ripeti prenotazione per veicolo:', targa);
    
    
    showNotification('Reindirizzamento alla prenotazione per ' + targa + '...', 'info');
    
   
    setTimeout(function() {
        window.location.href = 'prenota?targa=' + encodeURIComponent(targa);
    }, 1000);
}

function showNotification(message, type) {
    type = type || 'info';
    
    
    var existingNotifications = document.querySelectorAll('.temp-notification');
    existingNotifications.forEach(function(notification) {
        notification.remove();
    });
    
   
    var notification = document.createElement('div');
    notification.className = 'temp-notification temp-notification-' + type;
    notification.innerHTML = message;
    
   
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: ${type === 'success' ? '#4caf50' : type === 'error' ? '#f44336' : '#2196f3'};
        color: white;
        padding: 15px 20px;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        z-index: 10000;
        max-width: 400px;
        opacity: 0;
        transform: translateX(100%);
        transition: all 0.3s ease;
    `;
    
    document.body.appendChild(notification);
    
    
    setTimeout(function() {
        notification.style.opacity = '1';
        notification.style.transform = 'translateX(0)';
    }, 100);
    
   
    setTimeout(function() {
        notification.style.opacity = '0';
        notification.style.transform = 'translateX(100%)';
        
        setTimeout(function() {
            if (notification.parentNode) {
                notification.remove();
            }
        }, 300);
    }, 4000);
}


function addTooltips() {
    var statusBadges = document.querySelectorAll('.status-badge');
    
    statusBadges.forEach(function(badge) {
        var status = badge.textContent.trim().toLowerCase();
        var tooltip = '';
        
        switch (status) {
            case 'confermata':
                tooltip = 'La prenotazione è confermata e attiva';
                break;
            case 'in corso':
                tooltip = 'Il noleggio è attualmente in corso';
                break;
            case 'completata':
                tooltip = 'Il noleggio è stato completato con successo';
                break;
            case 'annullata':
                tooltip = 'La prenotazione è stata annullata';
                break;
        }
        
        if (tooltip) {
            badge.title = tooltip;
        }
    });
}


function handleResponsiveActions() {
    
    var mobileToggle = document.querySelector('.mobile-menu-toggle');
    if (mobileToggle) {
        mobileToggle.addEventListener('click', function() {
            document.body.classList.toggle('mobile-menu-open');
        });
    }
    
    
    window.addEventListener('resize', function() {
        debounce(function() {
            adjustLayoutForScreenSize();
        }, 250)();
    });
}


function adjustLayoutForScreenSize() {
    var isMobile = window.innerWidth <= 768;
    var cards = document.querySelectorAll('.prenotazione-card');
    
    cards.forEach(function(card) {
        if (isMobile) {
            card.classList.add('mobile-layout');
        } else {
            card.classList.remove('mobile-layout');
        }
    });
}


function debounce(func, wait) {
    var timeout;
    return function executedFunction() {
        var later = function() {
            clearTimeout(timeout);
            func.apply(null, arguments);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}


function formatDate(date) {
    if (!(date instanceof Date)) {
        date = new Date(date);
    }
    
    return date.toLocaleDateString('it-IT', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
    });
}


function formatPrice(amount) {
    return '€' + parseFloat(amount).toFixed(2).replace('.', ',');
}


window.addEventListener('beforeunload', function() {
    console.log('🔄 Uscita da Le mie prenotazioni');
});


window.EasyRidePrenotazioni = {
    viewDetails: viewDetails,
    cancelBooking: cancelBooking,
    repeatBooking: repeatBooking,
    showNotification: showNotification
};