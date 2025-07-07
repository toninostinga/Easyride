
document.addEventListener('DOMContentLoaded', function() {
    
    // Elementi del form
    var dataRitiroInput = document.getElementById('dataRitiro');
    var dataRestituzioneInput = document.getElementById('dataRestituzione');
    var noteTextarea = document.getElementById('note');
    var termsCheckbox = document.getElementById('terms');
    var submitBtn = document.getElementById('submitBtn');
    var bookingForm = document.getElementById('bookingForm');
    
    // Elementi per il calcolo prezzo
    var daysCountElement = document.getElementById('daysCount');
    var subtotalElement = document.getElementById('subtotal');
    var totalPriceElement = document.getElementById('totalPrice');
    
    // Inizializzazione
    initializeForm();
    updatePriceCalculation();
    
    // Event listeners
    dataRitiroInput.addEventListener('change', handleDateChange);
    dataRestituzioneInput.addEventListener('change', handleDateChange);
    
    noteTextarea.addEventListener('input', updateCharCounter);
    termsCheckbox.addEventListener('change', validateForm);
    
    bookingForm.addEventListener('submit', handleFormSubmit);
    
    // Chiusura messaggi
    var closeButtons = document.querySelectorAll('.close-btn');
    for (var i = 0; i < closeButtons.length; i++) {
        closeButtons[i].addEventListener('click', function() {
            this.parentElement.style.display = 'none';
        });
    }
    
    // ===== FUNZIONI DI INIZIALIZZAZIONE =====
    
    function initializeForm() {
        // Imposta date minime
        var today = new Date();
        var tomorrow = new Date(today);
        tomorrow.setDate(tomorrow.getDate() + 1);
        
        var maxDate = new Date(today);
        maxDate.setMonth(maxDate.getMonth() + 6);
        
        dataRitiroInput.min = formatDate(today);
        dataRitiroInput.max = formatDate(maxDate);
        
        dataRestituzioneInput.min = formatDate(tomorrow);
        dataRestituzioneInput.max = formatDate(maxDate);
        
        // Focus primo campo
        dataRitiroInput.focus();
        
        console.log('✅ Form prenotazione inizializzato');
    }
    
    // ===== GESTIONE DATE =====
    
    function handleDateChange() {
        validateDates();
        updatePriceCalculation();
        validateForm();
    }
    
    function validateDates() {
        var dataRitiro = dataRitiroInput.value;
        var dataRestituzione = dataRestituzioneInput.value;
        
        clearErrors();
        
        if (!dataRitiro || !dataRestituzione) {
            return false;
        }
        
        var ritiroDate = new Date(dataRitiro);
        var restituzioneDate = new Date(dataRestituzione);
        var today = new Date();
        today.setHours(0, 0, 0, 0);
        
        var isValid = true;
        
        // Verifica data ritiro
        if (ritiroDate < today) {
            showError('dataRitiro', 'La data di ritiro non può essere nel passato');
            isValid = false;
        }
        
        // Verifica data restituzione
        if (restituzioneDate <= ritiroDate) {
            showError('dataRestituzione', 'La data di restituzione deve essere successiva al ritiro');
            isValid = false;
        }
        
        // Verifica durata massima (30 giorni)
        var diffTime = Math.abs(restituzioneDate - ritiroDate);
        var diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        
        if (diffDays > 30) {
            showError('dataRestituzione', 'Il noleggio non può superare 30 giorni');
            isValid = false;
        }
        
        // Aggiorna data minima restituzione
        if (dataRitiro) {
            var minRestituzione = new Date(ritiroDate);
            minRestituzione.setDate(minRestituzione.getDate() + 1);
            dataRestituzioneInput.min = formatDate(minRestituzione);
        }
        
        return isValid;
    }
    
    // ===== CALCOLO PREZZO =====
    
    function updatePriceCalculation() {
        var dataRitiro = dataRitiroInput.value;
        var dataRestituzione = dataRestituzioneInput.value;
        
        if (!dataRitiro || !dataRestituzione) {
            resetPriceDisplay();
            return;
        }
        
        var ritiroDate = new Date(dataRitiro);
        var restituzioneDate = new Date(dataRestituzione);
        
        if (restituzioneDate <= ritiroDate) {
            resetPriceDisplay();
            return;
        }
        
        var diffTime = Math.abs(restituzioneDate - ritiroDate);
        var days = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        
        var subtotal = vehicleData.pricePerDay * days;
        var total = subtotal; // Per ora nessun costo aggiuntivo
        
        // Aggiorna display
        daysCountElement.textContent = days;
        subtotalElement.textContent = '€' + subtotal.toFixed(2);
        totalPriceElement.textContent = '€' + total.toFixed(2);
        
        // Animazione di aggiornamento
        animateElement(daysCountElement);
        animateElement(totalPriceElement);
        
        console.log('💰 Prezzo calcolato: ' + days + ' giorni = €' + total.toFixed(2));
    }
    
    function resetPriceDisplay() {
        daysCountElement.textContent = '1';
        subtotalElement.textContent = '€' + vehicleData.pricePerDay.toFixed(2);
        totalPriceElement.textContent = '€' + vehicleData.pricePerDay.toFixed(2);
    }
    
    // ===== CONTATORE CARATTERI =====
    
    function updateCharCounter() {
        var currentLength = noteTextarea.value.length;
        var maxLength = 500;
        
        var counter = document.querySelector('.char-counter');
        counter.textContent = currentLength + '/' + maxLength + ' caratteri';
        
        if (currentLength > maxLength * 0.9) {
            counter.style.color = '#dc3545';
        } else if (currentLength > maxLength * 0.7) {
            counter.style.color = '#ffc107';
        } else {
            counter.style.color = '#666';
        }
    }
    
    // ===== VALIDAZIONE FORM =====
    
    function validateForm() {
        var isValid = true;
        
        // Validazione date
        if (!validateDates()) {
            isValid = false;
        }
        
        // Validazione termini
        if (!termsCheckbox.checked) {
            isValid = false;
        }
        
        // Abilita/disabilita submit
        submitBtn.disabled = !isValid;
        
        if (isValid) {
            submitBtn.classList.remove('btn-disabled');
            submitBtn.querySelector('.btn-text').textContent = 'Conferma Prenotazione';
        } else {
            submitBtn.classList.add('btn-disabled');
            submitBtn.querySelector('.btn-text').textContent = 'Completa il form';
        }
        
        return isValid;
    }
    
    function handleFormSubmit(e) {
        if (!validateForm()) {
            e.preventDefault();
            showFormErrors();
            return false;
        }
        
        // Mostra loading
        submitBtn.disabled = true;
        submitBtn.querySelector('.btn-icon').textContent = '⏳';
        submitBtn.querySelector('.btn-text').textContent = 'Elaborazione...';
        
        console.log('📤 Invio prenotazione in corso...');
        
        // Il form si invia normalmente
        return true;
    }
    
    // ===== GESTIONE ERRORI =====
    
    function showError(fieldId, message) {
        var field = document.getElementById(fieldId);
        var errorElement = document.getElementById(fieldId + '-error');
        
        if (field && errorElement) {
            field.classList.add('error');
            errorElement.textContent = message;
            errorElement.style.display = 'block';
        }
    }
    
    function clearErrors() {
        var errorFields = document.querySelectorAll('.error');
        var errorMessages = document.querySelectorAll('.error-message');
        
        for (var i = 0; i < errorFields.length; i++) {
            errorFields[i].classList.remove('error');
        }
        
        for (var j = 0; j < errorMessages.length; j++) {
            errorMessages[j].textContent = '';
            errorMessages[j].style.display = 'none';
        }
    }
    
    function showFormErrors() {
        // Scroll al primo errore
        var firstError = document.querySelector('.error');
        if (firstError) {
            firstError.scrollIntoView({ 
                behavior: 'smooth', 
                block: 'center' 
            });
            firstError.focus();
        } else if (!termsCheckbox.checked) {
            termsCheckbox.scrollIntoView({ 
                behavior: 'smooth', 
                block: 'center' 
            });
            termsCheckbox.focus();
        }
    }
    
    // ===== UTILITY FUNCTIONS =====
    
    function formatDate(date) {
        var year = date.getFullYear();
        var month = String(date.getMonth() + 1).padStart(2, '0');
        var day = String(date.getDate()).padStart(2, '0');
        return year + '-' + month + '-' + day;
    }
    
    function animateElement(element) {
        if (!element) return;
        
        element.style.transform = 'scale(1.1)';
        element.style.transition = 'transform 0.2s ease';
        
        setTimeout(function() {
            element.style.transform = 'scale(1)';
        }, 200);
    }
    
    // ===== KEYBOARD SHORTCUTS =====
    
    document.addEventListener('keydown', function(e) {
        // Ctrl/Cmd + Enter per submit rapido
        if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
            if (validateForm()) {
                bookingForm.submit();
            }
        }
        
        // Escape per chiudere messaggi
        if (e.key === 'Escape') {
            var messages = document.querySelectorAll('.message');
            for (var i = 0; i < messages.length; i++) {
                messages[i].style.display = 'none';
            }
        }
    });
    
    // ===== AUTO-SAVE (opzionale) =====
    
    function autoSave() {
        if (!window.localStorage) return;
        
        var formData = {
            dataRitiro: dataRitiroInput.value,
            dataRestituzione: dataRestituzioneInput.value,
            note: noteTextarea.value,
            timestamp: Date.now()
        };
        
        try {
            localStorage.setItem('easyride_booking_draft_' + vehicleData.targa, JSON.stringify(formData));
        } catch (e) {
            // Ignore storage errors
        }
    }
    
    function loadAutoSave() {
        if (!window.localStorage) return;
        
        try {
            var saved = localStorage.getItem('easyride_booking_draft_' + vehicleData.targa);
            if (saved) {
                var data = JSON.parse(saved);
                
                // Solo se salvato nelle ultime 24 ore
                if (Date.now() - data.timestamp < 24 * 60 * 60 * 1000) {
                    if (data.note) {
                        noteTextarea.value = data.note;
                        updateCharCounter();
                    }
                }
            }
        } catch (e) {
            // Ignore storage errors
        }
    }
    
    // Auto-save ogni 30 secondi
    setInterval(autoSave, 30000);
    
    // Carica auto-save all'avvio
    loadAutoSave();
    
    console.log('✅ JavaScript prenotazione caricato per veicolo: ' + vehicleData.targa);
});