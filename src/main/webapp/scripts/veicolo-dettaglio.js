

// Variabili globali
let prezzoPerGiorno = 50.00; // Default, sarà aggiornato dalla pagina
let isCheckingAvailability = false;

/**
 * Inizializzazione quando la pagina è caricata
 */
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚗 Inizializzazione pagina veicolo dettaglio');
    
    // Inizializza componenti
    initPriceCalculator();
    initFormValidation();
    initGallery();
    initAvailabilityChecker();
    
    // Imposta date iniziali
    setMinDates();
    
    console.log('✅ Pagina inizializzata correttamente');
});

/**
 * Inizializza il calcolatore prezzi
 */
function initPriceCalculator() {
    // Estrai prezzo dalla pagina
    const priceElement = document.querySelector('.amount');
    if (priceElement) {
        const priceText = priceElement.textContent.replace(/[^\d.,]/g, '');
        prezzoPerGiorno = parseFloat(priceText.replace(',', '.')) || 50.00;
    }
    
    console.log('💰 Prezzo per giorno:', prezzoPerGiorno);
    
    // Event listeners per calcolo automatico
    const dataRitiro = document.getElementById('dataRitiro');
    const dataRestituzione = document.getElementById('dataRestituzione');
    
    if (dataRitiro) {
        dataRitiro.addEventListener('change', function() {
            validateDateField(this);
            calculatePrice();
        });
    }
    
    if (dataRestituzione) {
        dataRestituzione.addEventListener('change', function() {
            validateDateField(this);
            calculatePrice();
        });
    }
}

/**
 * Calcola e aggiorna il prezzo totale
 */
function calculatePrice() {
    const dataRitiro = document.getElementById('dataRitiro');
    const dataRestituzione = document.getElementById('dataRestituzione');
    const selectedDays = document.getElementById('selectedDays');
    const totalPrice = document.getElementById('totalPrice');
    
    if (!dataRitiro || !dataRestituzione || !selectedDays || !totalPrice) {
        return;
    }
    
    const ritiro = dataRitiro.value;
    const restituzione = dataRestituzione.value;
    
    if (ritiro && restituzione) {
        const dataRitiroObj = new Date(ritiro);
        const dataRestituzioneObj = new Date(restituzione);
        
        if (dataRestituzioneObj > dataRitiroObj) {
            const giorni = Math.ceil((dataRestituzioneObj - dataRitiroObj) / (1000 * 60 * 60 * 24));
            const totale = giorni * prezzoPerGiorno;
            
            // Aggiorna UI
            selectedDays.textContent = giorni;
            totalPrice.textContent = '€' + totale.toFixed(2);
            
            console.log(`💳 Prezzo calcolato: ${giorni} giorni × €${prezzoPerGiorno} = €${totale.toFixed(2)}`);
        }
    }
}

/**
 * Inizializza validazione form con regex
 */
function initFormValidation() {
    const form = document.getElementById('bookingForm');
    if (!form) return;
    
    // Event listener per submit
    form.addEventListener('submit', function(e) {
        console.log('📝 Validazione form in corso...');
        
        if (!validateAllFields()) {
            e.preventDefault();
            showMessage('Correggi gli errori nel form prima di continuare', 'error');
            return false;
        }
        
        console.log('✅ Form validato correttamente');
        return true;
    });
    
    // Event listeners per validazione real-time
    const dataRitiro = document.getElementById('dataRitiro');
    const dataRestituzione = document.getElementById('dataRestituzione');
    
    if (dataRitiro) {
        dataRitiro.addEventListener('blur', function() {
            validateDateField(this);
        });
    }
    
    if (dataRestituzione) {
        dataRestituzione.addEventListener('blur', function() {
            validateDateField(this);
        });
    }
}

/**
 * Valida campo data con regex e logica business
 */
function validateDateField(field) {
    const value = field.value;
    clearFieldError(field);
    
    if (!value) {
        showFieldError(field, 'Questo campo è obbligatorio');
        return false;
    }
    
    // Regex per formato data YYYY-MM-DD
    const dateRegex = /^\d{4}-\d{2}-\d{2}$/;
    if (!dateRegex.test(value)) {
        showFieldError(field, 'Formato data non valido (richiesto: YYYY-MM-DD)');
        return false;
    }
    
    // Validazione logica
    const date = new Date(value);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    if (field.id === 'dataRitiro' && date < today) {
        showFieldError(field, 'La data di ritiro non può essere nel passato');
        return false;
    }
    
    // Validazione date correlate
    if (field.id === 'dataRestituzione') {
        const dataRitiro = document.getElementById('dataRitiro');
        if (dataRitiro && dataRitiro.value) {
            const ritiroDate = new Date(dataRitiro.value);
            if (date <= ritiroDate) {
                showFieldError(field, 'La data di restituzione deve essere successiva al ritiro');
                return false;
            }
            
            // Controllo periodo massimo (30 giorni)
            const daysDiff = Math.ceil((date - ritiroDate) / (1000 * 60 * 60 * 24));
            if (daysDiff > 30) {
                showFieldError(field, 'Il periodo massimo di noleggio è 30 giorni');
                return false;
            }
        }
    }
    
    return true;
}

/**
 * Valida tutti i campi del form
 */
function validateAllFields() {
    const dataRitiro = document.getElementById('dataRitiro');
    const dataRestituzione = document.getElementById('dataRestituzione');
    
    let isValid = true;
    
    if (dataRitiro && !validateDateField(dataRitiro)) {
        isValid = false;
    }
    
    if (dataRestituzione && !validateDateField(dataRestituzione)) {
        isValid = false;
    }
    
    return isValid;
}

/**
 * Mostra errore per un campo specifico (modifica DOM, no alert)
 */
function showFieldError(field, message) {
    if (!field) return;
    
    // Rimuovi errore esistente
    clearFieldError(field);
    
    // Aggiungi classe errore
    field.classList.add('is-invalid');
    
    // Crea elemento errore
    const errorDiv = document.createElement('div');
    errorDiv.className = 'field-error';
    errorDiv.innerHTML = `<i class="fas fa-exclamation-triangle"></i> ${message}`;
    
    // Inserisci dopo il campo
    field.parentNode.insertBefore(errorDiv, field.nextSibling);
    
    // Aggiungi stili se non esistono
    addErrorStyles();
}

/**
 * Rimuovi errore da un campo
 */
function clearFieldError(field) {
    if (!field) return;
    
    field.classList.remove('is-invalid');
    const errorElement = field.parentNode.querySelector('.field-error');
    if (errorElement) {
        errorElement.remove();
    }
}

/**
 * Aggiungi stili per errori (solo una volta)
 */
function addErrorStyles() {
    if (document.querySelector('.field-error-styles')) return;
    
    const styles = document.createElement('style');
    styles.className = 'field-error-styles';
    styles.textContent = `
        .form-control.is-invalid {
            border-color: #dc3545;
            box-shadow: 0 0 0 3px rgba(220, 53, 69, 0.1);
        }
        .field-error {
            display: flex;
            align-items: center;
            gap: 0.25rem;
            margin-top: 0.25rem;
            color: #dc3545;
            font-size: 0.8rem;
            font-weight: 500;
            animation: slideInError 0.3s ease-out;
        }
        @keyframes slideInError {
            from { opacity: 0; transform: translateY(-5px); }
            to { opacity: 1; transform: translateY(0); }
        }
    `;
    document.head.appendChild(styles);
}

/**
 * Inizializza controllo disponibilità AJAX
 */
function initAvailabilityChecker() {
    const checkBtn = document.getElementById('checkAvailabilityBtn');
    if (checkBtn) {
        checkBtn.addEventListener('click', function() {
            checkAvailabilityAjax();
        });
    }
}

/**
 * Controllo disponibilità via AJAX
 */
function checkAvailabilityAjax() {
    if (isCheckingAvailability) return;
    
    const dataRitiro = document.getElementById('dataRitiro');
    const dataRestituzione = document.getElementById('dataRestituzione');
    const targa = document.querySelector('input[name="targa"]');
    const csrfToken = document.querySelector('input[name="csrfToken"]');
    
    if (!dataRitiro || !dataRestituzione || !targa) {
        showMessage('Campi mancanti per il controllo', 'error');
        return;
    }
    
    if (!dataRitiro.value || !dataRestituzione.value) {
        showMessage('Seleziona le date di ritiro e restituzione', 'error');
        return;
    }
    
    // Valida prima di controllare
    if (!validateAllFields()) {
        showMessage('Correggi gli errori nelle date', 'error');
        return;
    }
    
    isCheckingAvailability = true;
    
    // Aggiorna UI
    const checkBtn = document.getElementById('checkAvailabilityBtn');
    const originalText = checkBtn.innerHTML;
    checkBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Controllo...';
    checkBtn.disabled = true;
    
    // Prepara dati
    const formData = new URLSearchParams({
        action: 'check-availability',
        targa: targa.value,
        dataRitiro: dataRitiro.value,
        dataRestituzione: dataRestituzione.value,
        csrfToken: csrfToken ? csrfToken.value : ''
    });
    
    console.log('🔍 Controllo disponibilità AJAX...');
    
    // Richiesta AJAX
    fetch(window.location.pathname, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: formData
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Errore nella risposta del server');
        }
        return response.json();
    })
    .then(data => {
        showAvailabilityResult(data);
        console.log('✅ Risultato disponibilità:', data);
    })
    .catch(error => {
        console.error('❌ Errore AJAX:', error);
        showMessage('Errore nel controllo della disponibilità', 'error');
    })
    .finally(() => {
        // Ripristina UI
        isCheckingAvailability = false;
        checkBtn.innerHTML = originalText;
        checkBtn.disabled = false;
    });
}

/**
 * Mostra risultato controllo disponibilità
 */
function showAvailabilityResult(data) {
    const availabilityCheck = document.getElementById('availabilityCheck');
    const checkResult = document.getElementById('checkResult');
    
    if (!availabilityCheck || !checkResult) return;
    
    if (data.disponibile) {
        checkResult.innerHTML = `
            <div class="check-success" style="background: #d4edda; color: #155724; padding: 1rem; border-radius: 8px; border: 1px solid #c3e6cb;">
                <i class="fas fa-check-circle"></i>
                <strong>Disponibile!</strong> 
                ${data.giorni} giorni - Totale: €${data.prezzoTotale.toFixed(2)}
            </div>
        `;
        showMessage('Veicolo disponibile nel periodo selezionato!', 'success');
    } else {
        checkResult.innerHTML = `
            <div class="check-error" style="background: #f8d7da; color: #721c24; padding: 1rem; border-radius: 8px; border: 1px solid #f5c6cb;">
                <i class="fas fa-times-circle"></i>
                <strong>Non Disponibile</strong> nel periodo selezionato.
                <br><small>Prova con date diverse.</small>
            </div>
        `;
        showMessage('Veicolo non disponibile nel periodo selezionato', 'error');
    }
    
    // Mostra risultato
    availabilityCheck.style.display = 'block';
}

/**
 * Inizializza gallery immagini
 */
function initGallery() {
    const thumbnails = document.querySelectorAll('.thumbnail');
    const mainImage = document.getElementById('mainImage');
    
    if (!mainImage || thumbnails.length === 0) return;
    
    thumbnails.forEach(function(thumbnail, index) {
        thumbnail.addEventListener('click', function() {
            // Rimuovi classe active da tutti
            thumbnails.forEach(t => t.classList.remove('active'));
            
            // Aggiungi active al cliccato
            this.classList.add('active');
            
            // Cambia immagine principale
            const img = this.querySelector('img');
            if (img) {
                mainImage.src = img.src;
                mainImage.alt = img.alt;
            }
        });
    });
    
    console.log('🖼️ Gallery inizializzata con', thumbnails.length, 'immagini');
}

/**
 * Imposta date minime (domani)
 */
function setMinDates() {
    const dataRitiro = document.getElementById('dataRitiro');
    const dataRestituzione = document.getElementById('dataRestituzione');
    
    if (!dataRitiro || !dataRestituzione) return;
    
    const oggi = new Date();
    const domani = new Date();
    domani.setDate(oggi.getDate() + 1);
    
    const tomorrow = domani.toISOString().split('T')[0];
    dataRitiro.min = tomorrow;
    dataRestituzione.min = tomorrow;
    
    console.log('📅 Date minime impostate a:', tomorrow);
}

/**
 * Mostra messaggio temporaneo (modifica DOM, no alert)
 */
function showMessage(message, type = 'info', duration = 5000) {
    // Rimuovi messaggi esistenti
    const existingMessages = document.querySelectorAll('.temp-message');
    existingMessages.forEach(msg => msg.remove());
    
    // Crea nuovo messaggio
    const messageDiv = document.createElement('div');
    messageDiv.className = `temp-message alert alert-${type}`;
    
    const icon = type === 'error' ? 'exclamation-triangle' : 
                 type === 'success' ? 'check-circle' : 'info-circle';
    
    messageDiv.innerHTML = `
        <i class="fas fa-${icon}"></i>
        <span>${message}</span>
        <button type="button" onclick="this.parentElement.remove()" style="background: none; border: none; color: inherit; margin-left: auto; cursor: pointer;">
            <i class="fas fa-times"></i>
        </button>
    `;
    
    // Stili inline per compatibilità
    messageDiv.style.cssText = `
        display: flex;
        align-items: center;
        gap: 0.5rem;
        padding: 1rem;
        margin-bottom: 1rem;
        border-radius: 8px;
        font-weight: 500;
        position: relative;
        animation: slideInMessage 0.3s ease-out;
        background: ${type === 'success' ? '#d4edda' : type === 'error' ? '#f8d7da' : '#cce5ff'};
        color: ${type === 'success' ? '#155724' : type === 'error' ? '#721c24' : '#004085'};
        border: 1px solid ${type === 'success' ? '#c3e6cb' : type === 'error' ? '#f5c6cb' : '#b3d7ff'};
    `;
    
    // Aggiungi alla pagina
    const container = document.querySelector('.container');
    if (container) {
        container.insertBefore(messageDiv, container.firstChild);
        
        // Rimuovi automaticamente
        setTimeout(() => {
            if (messageDiv.parentNode) {
                messageDiv.style.opacity = '0';
                messageDiv.style.transform = 'translateY(-10px)';
                setTimeout(() => messageDiv.remove(), 300);
            }
        }, duration);
    }
    
    // Aggiungi stili animazione se non esistono
    if (!document.querySelector('.message-animation-styles')) {
        const styles = document.createElement('style');
        styles.className = 'message-animation-styles';
        styles.textContent = `
            @keyframes slideInMessage {
                from { opacity: 0; transform: translateY(-10px); }
                to { opacity: 1; transform: translateY(0); }
            }
        `;
        document.head.appendChild(styles);
    }
    
    console.log(`💬 Messaggio mostrato (${type}):`, message);
}

/**
 * Chiudi alert (per compatibilità)
 */
function closeAlert(alertId) {
    const alert = document.getElementById(alertId);
    if (alert) {
        alert.style.opacity = '0';
        setTimeout(() => alert.remove(), 300);
    }
}

// Funzioni utility per validation avanzata con regex
const ValidationUtils = {
    // Email regex
    email: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
    
    // Telefono italiano
    phone: /^[\+]?[0-9\s\-\(\)]{6,}$/,
    
    // Targa italiana
    targa: /^[A-Z]{2}[0-9]{3}[A-Z]{2}$/,
    
    // Data ISO
    date: /^\d{4}-\d{2}-\d{2}$/,
    
    // Validazione email
    validateEmail: function(email) {
        return this.email.test(email);
    },
    
    // Validazione telefono
    validatePhone: function(phone) {
        return this.phone.test(phone);
    },
    
    // Validazione targa
    validateTarga: function(targa) {
        return this.targa.test(targa.toUpperCase());
    }
};

// Esporta funzioni per uso esterno
window.VeicoloDettaglio = {
    calculatePrice: calculatePrice,
    checkAvailability: checkAvailabilityAjax,
    showMessage: showMessage,
    validateAllFields: validateAllFields,
    ValidationUtils: ValidationUtils
};
// AGGIUNGI QUESTO AL TUO veicolo-dettaglio.js

// Migliora la funzione per mostrare notifiche carrello
function showCartNotification(vehicleName, action = 'added') {
    // Rimuovi notifiche esistenti
    const existingNotifications = document.querySelectorAll('.cart-notification');
    existingNotifications.forEach(n => n.remove());
    
    // Crea notifica
    const notification = document.createElement('div');
    notification.className = 'cart-notification';
    
    const messages = {
        added: `✅ ${vehicleName} aggiunto al carrello!`,
        removed: `🗑️ ${vehicleName} rimosso dal carrello`,
        updated: `✏️ ${vehicleName} aggiornato nel carrello`
    };
    
    notification.innerHTML = `
        <div class="notification-content">
            <div class="notification-icon">🛒</div>
            <div class="notification-text">
                <div class="notification-title">${messages[action]}</div>
                <div class="notification-subtitle">Controlla il tuo carrello</div>
            </div>
            <div class="notification-actions">
                <button class="btn-view-cart" onclick="window.location.href='${window.contextPath || ''}/carrello'">
                    Vai al Carrello
                </button>
                <button class="btn-close" onclick="this.closest('.cart-notification').remove()">×</button>
            </div>
        </div>
    `;
    
    // Stili per la notifica
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: white;
        border-radius: 12px;
        box-shadow: 0 8px 32px rgba(0,0,0,0.2);
        z-index: 10000;
        max-width: 400px;
        transform: translateX(450px);
        transition: transform 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55);
        overflow: hidden;
        border-left: 4px solid #27ae60;
    `;
    
    // Stili per il contenuto
    const style = document.createElement('style');
    style.textContent = `
        .notification-content {
            display: flex;
            align-items: center;
            padding: 1rem;
            gap: 1rem;
        }
        .notification-icon {
            font-size: 2rem;
            animation: bounce 0.6s ease-in-out;
        }
        .notification-text {
            flex: 1;
        }
        .notification-title {
            font-weight: 600;
            color: #2c3e50;
            margin-bottom: 0.25rem;
        }
        .notification-subtitle {
            font-size: 0.85rem;
            color: #7f8c8d;
        }
        .notification-actions {
            display: flex;
            flex-direction: column;
            gap: 0.5rem;
        }
        .btn-view-cart {
            background: #3498db;
            color: white;
            border: none;
            padding: 0.5rem 1rem;
            border-radius: 6px;
            font-size: 0.8rem;
            cursor: pointer;
            transition: background 0.2s;
        }
        .btn-view-cart:hover {
            background: #2980b9;
        }
        .btn-close {
            background: none;
            border: none;
            color: #7f8c8d;
            cursor: pointer;
            font-size: 1.2rem;
            padding: 0.25rem;
            border-radius: 50%;
            width: 24px;
            height: 24px;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .btn-close:hover {
            background: #ecf0f1;
            color: #2c3e50;
        }
        @keyframes bounce {
            0%, 20%, 50%, 80%, 100% { transform: translateY(0); }
            40% { transform: translateY(-10px); }
            60% { transform: translateY(-5px); }
        }
    `;
    
    if (!document.querySelector('.cart-notification-styles')) {
        style.className = 'cart-notification-styles';
        document.head.appendChild(style);
    }
    
    document.body.appendChild(notification);
    
    // Mostra la notifica
    setTimeout(() => {
        notification.style.transform = 'translateX(0)';
    }, 10);
    
    // Nascondi automaticamente dopo 5 secondi
    setTimeout(() => {
        if (notification.parentNode) {
            notification.style.transform = 'translateX(450px)';
            setTimeout(() => notification.remove(), 400);
        }
    }, 5000);
    
    // Anima l'icona del carrello nell'header
    animateCartIcon();
}

// Anima l'icona del carrello
function animateCartIcon() {
    const cartLinks = document.querySelectorAll('a[href*="carrello"], .carrello-link');
    cartLinks.forEach(link => {
        link.style.animation = 'cartBounce 0.6s ease-in-out';
        setTimeout(() => {
            link.style.animation = '';
        }, 600);
    });
    
    // Aggiungi stile per l'animazione se non esiste
    if (!document.querySelector('.cart-bounce-styles')) {
        const style = document.createElement('style');
        style.className = 'cart-bounce-styles';
        style.textContent = `
            @keyframes cartBounce {
                0%, 100% { transform: scale(1); }
                25% { transform: scale(1.1); }
                50% { transform: scale(1.05); }
                75% { transform: scale(1.1); }
            }
        `;
        document.head.appendChild(style);
    }
}

// MODIFICA LA GESTIONE DEL FORM NEL TUO veicolo-dettaglio.js
// Trova questa parte nel tuo initFormValidation() e sostituiscila:

function initFormValidation() {
    const form = document.getElementById('bookingForm');
    if (!form) return;
    
    // Event listener per submit
    form.addEventListener('submit', function(e) {
        console.log('📝 Validazione form in corso...');
        
        if (!validateAllFields()) {
            e.preventDefault();
            showMessage('Correggi gli errori nel form prima di continuare', 'error');
            return false;
        }
        
        // Se la validazione è OK, mostra la notifica dopo l'invio
        const vehicleName = document.querySelector('.vehicle-title .brand').textContent + ' ' + 
                          document.querySelector('.vehicle-title .model').textContent;
        
        // Mostra notifica dopo un breve delay per permettere il redirect
        setTimeout(() => {
            showCartNotification(vehicleName, 'added');
        }, 100);
        
        console.log('✅ Form validato correttamente');
        return true;
    });
    
    // ... resto del codice esistente
}
console.log('🎯 JavaScript veicolo dettaglio caricato correttamente');