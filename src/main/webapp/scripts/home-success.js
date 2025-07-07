

document.addEventListener('DOMContentLoaded', function() {
    checkSuccessMessages();
});

function checkSuccessMessages() {
    // Controlla URL parameters per messaggi di successo
    const urlParams = new URLSearchParams(window.location.search);
    const successParam = urlParams.get('success');
    
    if (successParam === 'prenotazione-completata') {
        showPrenotazioneSuccessMessage();
        
        // Pulisci URL dai parametri
        const newUrl = window.location.origin + window.location.pathname;
        window.history.replaceState({}, document.title, newUrl);
    }
    
    // Controlla anche sessionStorage (dal JavaScript del checkout)
    if (sessionStorage.getItem('checkout-success') === 'true') {
        const message = sessionStorage.getItem('checkout-message') || 'Prenotazione completata con successo!';
        
        // Rimuovi dal sessionStorage
        sessionStorage.removeItem('checkout-success');
        sessionStorage.removeItem('checkout-message');
        
        // Mostra messaggio
        showCustomSuccessMessage(message);
    }
}

function showPrenotazioneSuccessMessage() {
    const message = `
        🎉 <strong>Prenotazione Completata!</strong><br>
        La tua prenotazione è stata registrata con successo.<br>
        <small>Riceverai una email di conferma a breve.</small>
    `;
    
    createSuccessBanner(message);
    
    // Mostra anche alert per sicurezza
    setTimeout(() => {
        alert('🎉 Prenotazione completata con successo!\n\nRiceverai una email di conferma a breve.');
    }, 1000);
}

function showCustomSuccessMessage(message) {
    createSuccessBanner(`✅ ${message}`);
    
    // Alert opzionale
    setTimeout(() => {
        alert(`✅ ${message}`);
    }, 1000);
}

function createSuccessBanner(message) {
    // Rimuovi banner esistenti
    const existingBanner = document.querySelector('.success-banner');
    if (existingBanner) {
        existingBanner.remove();
    }
    
    // Crea nuovo banner
    const banner = document.createElement('div');
    banner.className = 'success-banner';
    banner.innerHTML = `
        <div class="success-content">
            <div class="success-icon">
                <i class="fas fa-check-circle"></i>
            </div>
            <div class="success-text">
                ${message}
            </div>
            <button class="success-close" onclick="this.closest('.success-banner').remove()">
                <i class="fas fa-times"></i>
            </button>
        </div>
    `;
    
    // Aggiungi stili
    addSuccessBannerStyles(banner);
    
    // Inserisci nella pagina
    document.body.prepend(banner);
    
    // Animazione di entrata
    setTimeout(() => {
        banner.classList.add('show');
    }, 100);
    
    // Auto-rimozione dopo 10 secondi
    setTimeout(() => {
        if (banner.parentElement) {
            banner.classList.add('hide');
            setTimeout(() => banner.remove(), 500);
        }
    }, 10000);
    
    // Sposta il body per far spazio al banner
    adjustBodyForBanner(true);
    
    // Rimuovi spazio quando banner viene chiuso
    banner.addEventListener('remove', () => {
        adjustBodyForBanner(false);
    });
}

function addSuccessBannerStyles(banner) {
    banner.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        background: linear-gradient(135deg, #28a745, #20c997);
        color: white;
        z-index: 9999;
        box-shadow: 0 4px 20px rgba(40, 167, 69, 0.3);
        transform: translateY(-100%);
        transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    `;
    
    const content = banner.querySelector('.success-content');
    content.style.cssText = `
        max-width: 1200px;
        margin: 0 auto;
        padding: 1rem 2rem;
        display: flex;
        align-items: center;
        gap: 1rem;
        min-height: 60px;
    `;
    
    const icon = banner.querySelector('.success-icon');
    icon.style.cssText = `
        font-size: 1.5rem;
        animation: successPulse 2s infinite;
    `;
    
    const text = banner.querySelector('.success-text');
    text.style.cssText = `
        flex: 1;
        font-weight: 600;
        line-height: 1.4;
    `;
    
    const closeBtn = banner.querySelector('.success-close');
    closeBtn.style.cssText = `
        background: rgba(255, 255, 255, 0.2);
        border: none;
        color: white;
        width: 36px;
        height: 36px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.3s ease;
        font-size: 1rem;
    `;
    
    // Hover effect per close button
    closeBtn.addEventListener('mouseenter', function() {
        this.style.background = 'rgba(255, 255, 255, 0.3)';
        this.style.transform = 'scale(1.1)';
    });
    
    closeBtn.addEventListener('mouseleave', function() {
        this.style.background = 'rgba(255, 255, 255, 0.2)';
        this.style.transform = 'scale(1)';
    });
}

function adjustBodyForBanner(show) {
    const body = document.body;
    const header = document.querySelector('header, .header, nav');
    
    if (show) {
        // Aggiungi spazio per il banner
        body.style.paddingTop = '70px';
        if (header) {
            header.style.marginTop = '70px';
        }
    } else {
        // Rimuovi spazio
        body.style.paddingTop = '';
        if (header) {
            header.style.marginTop = '';
        }
    }
}

// Aggiungi CSS per animazioni e classi di stato
function addSuccessAnimationStyles() {
    const style = document.createElement('style');
    style.id = 'success-banner-styles';
    style.textContent = `
        .success-banner.show {
            transform: translateY(0) !important;
        }
        
        .success-banner.hide {
            transform: translateY(-100%) !important;
            opacity: 0 !important;
        }
        
        @keyframes successPulse {
            0%, 100% {
                transform: scale(1);
                opacity: 1;
            }
            50% {
                transform: scale(1.1);
                opacity: 0.8;
            }
        }
        
        @keyframes successSlideIn {
            from {
                transform: translateY(-100%);
                opacity: 0;
            }
            to {
                transform: translateY(0);
                opacity: 1;
            }
        }
        
        /* Responsive */
        @media (max-width: 768px) {
            .success-banner .success-content {
                padding: 1rem !important;
                flex-direction: column;
                text-align: center;
                gap: 0.75rem;
            }
            
            .success-banner .success-text {
                font-size: 0.9rem;
            }
            
            .success-banner .success-close {
                position: absolute;
                top: 0.5rem;
                right: 0.5rem;
                width: 30px;
                height: 30px;
            }
        }
        
        /* Dark mode support */
        @media (prefers-color-scheme: dark) {
            .success-banner .success-close {
                background: rgba(0, 0, 0, 0.3);
            }
            
            .success-banner .success-close:hover {
                background: rgba(0, 0, 0, 0.5);
            }
        }
    `;
    
    // Aggiungi solo se non già presente
    if (!document.getElementById('success-banner-styles')) {
        document.head.appendChild(style);
    }
}

// Inizializza stili
addSuccessAnimationStyles();

// === UTILITY FUNCTIONS ===

// Funzione per testare il banner (da console)
function testSuccessBanner() {
    showCustomSuccessMessage('Questo è un messaggio di test! 🎉');
}

// Funzione per rimuovere tutti i banner
function removeAllSuccessBanners() {
    const banners = document.querySelectorAll('.success-banner');
    banners.forEach(banner => banner.remove());
    adjustBodyForBanner(false);
}

// Esporta funzioni per uso globale (se necessario)
window.EasyRideSuccess = {
    showMessage: showCustomSuccessMessage,
    createBanner: createSuccessBanner,
    test: testSuccessBanner,
    removeAll: removeAllSuccessBanners
};

// === AUTO-CHECK MESSAGGI ===

// Controlla parametri URL ogni volta che la pagina cambia (per SPA)
window.addEventListener('popstate', checkSuccessMessages);

// Log per debug
console.log('✅ Sistema messaggi di successo caricato');

// === INTEGRAZIONE CON SISTEMI ESISTENTI ===

// Se esiste jQuery, aggiungi supporto
if (typeof $ !== 'undefined') {
    $(document).ready(checkSuccessMessages);
    
    // Custom event per jQuery
    $(document).on('easyride:success', function(e, message) {
        showCustomSuccessMessage(message);
    });
}

// Custom events per vanilla JS
document.addEventListener('easyride:success', function(e) {
    showCustomSuccessMessage(e.detail.message);
});

// Esempio di come triggerare un messaggio custom:
// document.dispatchEvent(new CustomEvent('easyride:success', { 
//     detail: { message: 'Operazione completata!' } 
// }));