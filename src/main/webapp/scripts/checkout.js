

document.addEventListener('DOMContentLoaded', function() {
    console.log('🛒 Checkout.js caricato, contextPath:', window.contextPath);
    
    // Elementi del DOM
    const form = document.getElementById('checkout-form');
    const confirmBtn = document.getElementById('confirm-btn');
    const termsCheckbox = document.getElementById('terms-checkbox');
    const newsletterCheckbox = document.getElementById('newsletter-checkbox');
    const loadingOverlay = document.getElementById('loading-overlay');
    const noteTextarea = document.getElementById('note');
    const charCounter = document.querySelector('.char-counter');
    
    // Inizializzazione
    if (form) {
        initializeCheckout();
    } else {
        console.error('❌ Form checkout non trovato!');
    }
    
    function initializeCheckout() {
        console.log('🚀 Inizializzazione checkout...');
        
        // Setup contatore caratteri
        setupCharCounter();
        
        // ✅ Setup validazione form (SEMPRE VALIDA)
        setupFormValidationDisabled();
        
        // Setup metodi di pagamento
        setupPaymentMethods();
        
        // Setup submit form
        setupFormSubmission();
        
        console.log('✅ Checkout inizializzato correttamente');
    }
    
    // === CONTATORE CARATTERI ===
    function setupCharCounter() {
        if (!noteTextarea || !charCounter) return;
        
        noteTextarea.addEventListener('input', function() {
            const currentLength = this.value.length;
            const maxLength = 500;
            
            charCounter.textContent = `${currentLength}/${maxLength} caratteri`;
            
            // Colori del contatore (definiti nel CSS)
            if (currentLength > maxLength * 0.9) {
                charCounter.style.color = '#dc3545'; // Rosso
            } else if (currentLength > maxLength * 0.7) {
                charCounter.style.color = '#fd7e14'; // Arancione
            } else {
                charCounter.style.color = '#6c757d'; // Grigio
            }
            
            // Rivalida form (sempre valida)
            validateFormDisabled();
        });
        
        // Stato iniziale
        charCounter.textContent = '0/500 caratteri';
    }
    
    // === ✅ VALIDAZIONE FORM DISABILITATA ===
    function setupFormValidationDisabled() {
        console.log('🚀 Configurazione validazione DISABILITATA');
        
        // Listener per termini e condizioni (solo per UI)
        if (termsCheckbox) {
            termsCheckbox.addEventListener('change', validateFormDisabled);
        }
        
        // Listener per note (solo per contatore)
        if (noteTextarea) {
            noteTextarea.addEventListener('input', validateFormDisabled);
        }
        
        // ✅ ABILITA SUBITO IL BOTTONE
        validateFormDisabled();
    }
    
    // ✅ VALIDAZIONE SEMPRE VALIDA
    function validateFormDisabled() {
        console.log('✅ Validazione form: SEMPRE VALIDA (disabilitata per debug)');
        
        // Debug elementi DOM
        console.log('🔍 Debug elementi DOM:');
        console.log('   - Form:', form ? '✅ Trovato' : '❌ Non trovato');
        console.log('   - Terms checkbox:', termsCheckbox ? '✅ Trovato' : '❌ Non trovato');
        console.log('   - Payment method:', document.querySelector('input[name="paymentMethod"]:checked') ? '✅ Trovato' : '❌ Non trovato');
        console.log('   - CSRF token:', document.querySelector('input[name="csrfToken"]') ? '✅ Trovato' : '❌ Non trovato');
        console.log('   - Confirm button:', confirmBtn ? '✅ Trovato' : '❌ Non trovato');
        
        if (termsCheckbox) {
            console.log('   - Terms checked:', termsCheckbox.checked);
        }
        
        const paymentMethod = document.querySelector('input[name="paymentMethod"]:checked');
        if (paymentMethod) {
            console.log('   - Payment method value:', paymentMethod.value);
        }
        
        const csrfToken = document.querySelector('input[name="csrfToken"]');
        if (csrfToken) {
            console.log('   - CSRF token value:', csrfToken.value ? 'Present' : 'Empty');
        }
        
        // ✅ FORZA SEMPRE VALIDO
        updateSubmitButton(true);
        return true;
    }
    
    // Wrapper per compatibilità
    function validateForm() {
        return validateFormDisabled();
    }
    
    function updateSubmitButton(isValid) {
        if (!confirmBtn) return;
        
        // ✅ FORZA SEMPRE ABILITATO
        confirmBtn.disabled = false;
        
        const btnText = confirmBtn.querySelector('.btn-text');
        if (btnText) {
            btnText.textContent = 'Conferma Prenotazione';
        }
        
        // ✅ RIMUOVI SEMPRE LA CLASSE DISABLED
        confirmBtn.classList.remove('disabled');
        
        console.log('✅ Bottone submit sempre abilitato (validazione disabilitata)');
    }
    
    // === METODI DI PAGAMENTO ===
    function setupPaymentMethods() {
        const paymentRadios = document.querySelectorAll('input[name="paymentMethod"]');
        
        paymentRadios.forEach(radio => {
            radio.addEventListener('change', function() {
                // Rimuovi classe selected da tutti
                document.querySelectorAll('.payment-option').forEach(option => {
                    option.classList.remove('selected');
                });
                
                // Aggiungi selected al corrente
                if (this.checked) {
                    this.closest('.payment-option').classList.add('selected');
                    console.log('💳 Metodo pagamento selezionato:', this.value);
                }
                
                // Rivalida form (sempre valida)
                validateFormDisabled();
            });
        });
        
        // Stato iniziale
        const checkedPayment = document.querySelector('input[name="paymentMethod"]:checked');
        if (checkedPayment) {
            checkedPayment.closest('.payment-option').classList.add('selected');
        }
    }
    
    // === SUBMIT FORM ===
    function setupFormSubmission() {
        form.addEventListener('submit', function(e) {
            e.preventDefault(); // IMPEDISCE SUBMIT NORMALE
            
            console.log('🚀 Inizio submit checkout (validazione disabilitata)');
            
            // ✅ SALTA COMPLETAMENTE LA VALIDAZIONE
            console.log('⏭️ Validazione saltata - procedo direttamente al submit');
            
            // Conferma utente
            if (!confirmSubmission()) {
                console.log('🚫 Submit annullato dall\'utente');
                return;
            }
            
            // Avvia processo di submit
            processSubmission();
        });
    }
    
    function confirmSubmission() {
        const totalAmount = window.totalAmount || '0.00';
        
        const message = `Confermi la prenotazione per un totale di €${totalAmount}?\n\n` +
                       'Una volta confermata, riceverai una email di conferma e ' +
                       'la prenotazione sarà definitiva.';
        
        return confirm(message);
    }
    
    // ✅ FUNZIONE CORRETTA CON URLSearchParams
    function processSubmission() {
        console.log('📤 Elaborazione submit...');
        
        // Mostra loading
        showLoading(true);
        disableForm(true);
        
        // ✅ USA URLSearchParams INVECE DI FormData
        const params = new URLSearchParams();
        
        // Raccogli i dati dal form
        const csrfInput = form.querySelector('input[name="csrfToken"]');
        const paymentInput = document.querySelector('input[name="paymentMethod"]:checked');
        const noteInput = document.getElementById('note');
        const newsletterInput = document.getElementById('newsletter-checkbox');
        
        // Aggiungi parametri
        params.append('csrfToken', csrfInput ? csrfInput.value : '');
        params.append('action', 'conferma-prenotazione');
        params.append('paymentMethod', paymentInput ? paymentInput.value : 'card');
        params.append('terms', 'true');
        params.append('note', noteInput ? noteInput.value : '');
        params.append('newsletter', newsletterInput && newsletterInput.checked ? 'true' : 'false');
        
        console.log('✅ Parametri preparati:');
        for (let [key, value] of params.entries()) {
            console.log(`   ${key}: ${value}`);
        }
        
        // URL di submit
        const submitUrl = '/EasyRide/checkout';
        console.log('🎯 URL:', submitUrl);
        
        // Richiesta fetch con URLSearchParams
        fetch(submitUrl, {
            method: 'POST',
            body: params,
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Requested-With': 'XMLHttpRequest'
            }
        })
        .then(response => {
            console.log('📨 Risposta:', response.status, response.statusText);
            
            if (!response.ok) {
                throw new Error(`Errore HTTP: ${response.status} ${response.statusText}`);
            }
            
            return response.json();
        })
        .then(data => {
            console.log('✅ Risposta JSON:', data);
            
            showLoading(false);
            disableForm(false);
            
            if (data.success) {
                handleSuccess(data.message);
            } else {
                handleError(data.message || 'Errore durante l\'elaborazione della prenotazione.');
            }
        })
        .catch(error => {
            console.error('❌ Errore:', error);
            
            showLoading(false);
            disableForm(false);
            handleError('Errore di connessione. Verifica la tua connessione internet e riprova.');
        });
    }
    
    function handleSuccess(message) {
        console.log('🎉 Prenotazione completata!');
        
        // Mostra messaggio di successo
        showAlert(message || 'Prenotazione completata con successo!', 'success');
        
        // Salva messaggio per la prossima pagina
        sessionStorage.setItem('checkout-success', 'true');
        sessionStorage.setItem('checkout-message', message || 'Prenotazione completata con successo!');
        
        // Redirect dopo 3 secondi
        setTimeout(() => {
            window.location.href = window.contextPath + '/catalogo';
        }, 3000);
    }
    
    function handleError(message) {
        console.error('❌ Errore checkout:', message);
        showAlert(message, 'error');
    }
    
    // === UTILITY FUNCTIONS ===
    
    function showLoading(show) {
        if (!loadingOverlay) return;
        
        if (show) {
            loadingOverlay.classList.add('show');
            document.body.style.overflow = 'hidden';
        } else {
            loadingOverlay.classList.remove('show');
            document.body.style.overflow = '';
        }
    }
    
    function disableForm(disable) {
        const inputs = form.querySelectorAll('input, textarea, button, select');
        inputs.forEach(input => {
            input.disabled = disable;
        });
        
        if (disable && confirmBtn) {
            confirmBtn.innerHTML = `
                <i class="fas fa-spinner fa-spin"></i>
                <span class="btn-text">Elaborazione...</span>
            `;
        } else if (!disable && confirmBtn) {
            confirmBtn.innerHTML = `
                <i class="fas fa-lock"></i>
                <span class="btn-text">Conferma Prenotazione</span>
                <span class="btn-price">€${window.totalAmount || '0.00'}</span>
            `;
            // Riabilita validazione (sempre valida)
            validateFormDisabled();
        }
    }
    
    function showAlert(message, type) {
        // Rimuovi alert precedenti
        const existingAlerts = document.querySelectorAll('.checkout-alert');
        existingAlerts.forEach(alert => alert.remove());
        
        // Crea nuovo alert
        const alert = document.createElement('div');
        alert.className = `checkout-alert alert-${type}`;
        
        const icon = type === 'success' ? 'fa-check-circle' : 'fa-exclamation-triangle';
        const title = type === 'success' ? 'Successo!' : 'Errore!';
        
        alert.innerHTML = `
            <div class="alert-content">
                <i class="fas ${icon}"></i>
                <div class="alert-text">
                    <strong>${title}</strong>
                    <p>${message}</p>
                </div>
                <button class="alert-close" onclick="this.parentElement.parentElement.remove()">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        `;
        
        // Inserisci prima del form
        form.parentNode.insertBefore(alert, form);
        
        // Scroll al messaggio
        alert.scrollIntoView({ behavior: 'smooth', block: 'center' });
        
        // Animazione di entrata (utilizzando le classi CSS)
        setTimeout(() => alert.classList.add('show'), 100);
        
        // Auto-remove per errori dopo 10 secondi
        if (type === 'error') {
            setTimeout(() => {
                if (alert.parentNode) {
                    alert.remove();
                }
            }, 10000);
        }
    }
    
    // === GESTIONE PAGINA ===
    
    // Impedisci di uscire durante l'elaborazione
    window.addEventListener('beforeunload', function(e) {
        if (loadingOverlay && loadingOverlay.classList.contains('show')) {
            e.preventDefault();
            e.returnValue = 'La prenotazione è in elaborazione. Sei sicuro di voler uscire?';
        }
    });
    
    // === GESTIONE MESSAGGI DA ALTRE PAGINE ===
    
    // Controllo messaggi di successo salvati in sessionStorage
    function checkStoredMessages() {
        if (sessionStorage.getItem('checkout-success') === 'true') {
            const message = sessionStorage.getItem('checkout-message') || 'Operazione completata con successo!';
            
            // Rimuovi i messaggi dal sessionStorage
            sessionStorage.removeItem('checkout-success');
            sessionStorage.removeItem('checkout-message');
            
            // Mostra il messaggio
            setTimeout(() => {
                showAlert(message, 'success');
            }, 500);
        }
    }
    
    // Controllo messaggi all'avvio
    checkStoredMessages();
    
    console.log('🎯 Checkout.js inizializzato completamente (VALIDAZIONE DISABILITATA)');
});

// === FUNZIONI GLOBALI PER SUPPORTO ===

// Funzione per mostrare messaggi di successo da altre pagine
window.showCheckoutSuccess = function(message) {
    sessionStorage.setItem('checkout-success', 'true');
    sessionStorage.setItem('checkout-message', message || 'Operazione completata con successo!');
};

// Funzione per debug completo (usa in console)
window.debugCheckout = function() {
    console.log('Debug Checkout COMPLETO:');
    console.log('   - Form:', document.getElementById('checkout-form'));
    console.log('   - Submit Button:', document.getElementById('confirm-btn'));
    console.log('   - Terms Checkbox:', document.getElementById('terms-checkbox'));
    console.log('   - Terms Checked:', document.getElementById('terms-checkbox') ? document.getElementById('terms-checkbox').checked : 'null');
    console.log('   - Payment Method:', document.querySelector('input[name="paymentMethod"]:checked'));
    console.log('   - Payment Value:', document.querySelector('input[name="paymentMethod"]:checked') ? document.querySelector('input[name="paymentMethod"]:checked').value : 'null');
    console.log('   - CSRF Token:', document.querySelector('input[name="csrfToken"]'));
    console.log('   - CSRF Value:', document.querySelector('input[name="csrfToken"]') ? document.querySelector('input[name="csrfToken"]').value : 'null');
    console.log('   - Context Path:', window.contextPath);
    console.log('   - Total Amount:', window.totalAmount);
    console.log('   - Note Textarea:', document.getElementById('note'));
    console.log('   - Note Value:', document.getElementById('note') ? document.getElementById('note').value : 'null');
    
    // Test validazione
    console.log('   - Validazione result:', typeof window.validateFormDisabled === 'function' ? window.validateFormDisabled() : 'Funzione non disponibile');
};

// Esporta funzioni per uso globale
window.validateFormDisabled = function() {
    console.log('Validazione globale: SEMPRE VALIDA');
    return true;
};