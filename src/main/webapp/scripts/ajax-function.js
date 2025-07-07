

// ===== CONFIGURAZIONE GLOBALE =====
var AJAX_CONFIG = {
    baseUrl: '/easyride',
    timeout: 10000,
    retryAttempts: 2,
    csrfTokenName: 'csrfToken'
};

// ===== UTILITY FUNCTIONS =====

function getCSRFToken() {
    var tokenInput = document.querySelector('input[name="csrfToken"]');
    var tokenMeta = document.querySelector('meta[name="csrf-token"]');
    
    if (tokenInput) return tokenInput.value;
    if (tokenMeta) return tokenMeta.getAttribute('content');
    
    console.warn('⚠️ Token CSRF non trovato');
    return '';
}

function mostraNotifica(tipo, messaggio, durata) {
    if (typeof durata === 'undefined') durata = 5000;
    
    // Rimuovi notifiche esistenti
    var notificheEsistenti = document.querySelectorAll('.ajax-notification');
    for (var i = 0; i < notificheEsistenti.length; i++) {
        notificheEsistenti[i].remove();
    }
    
    // Crea nuova notifica
    var notification = document.createElement('div');
    notification.className = 'ajax-notification ' + tipo;
    
    notification.innerHTML = 
        '<div style="display: flex; align-items: center; justify-content: space-between;">' +
            '<div style="display: flex; align-items: center;">' +
                '<i class="fas fa-' + (tipo === 'success' ? 'check-circle' : 'exclamation-triangle') + '" ' +
                   'style="margin-right: 10px; font-size: 18px;"></i>' +
                '<span>' + messaggio + '</span>' +
            '</div>' +
            '<button class="close-btn" onclick="this.closest(\'.ajax-notification\').remove()">' +
                '&times;' +
            '</button>' +
        '</div>';
    
    document.body.appendChild(notification);
    
    // Rimuovi automaticamente
    setTimeout(function() {
        if (notification.parentElement) {
            notification.style.animation = 'slideOutRight 0.3s ease-in';
            setTimeout(function() { 
                if (notification.parentElement) notification.remove(); 
            }, 300);
        }
    }, durata);
}

function setLoadingState(elemento, loading) {
    if (!elemento) return;
    
    if (loading) {
        elemento.dataset.originalContent = elemento.innerHTML;
        elemento.innerHTML = '<span class="loading-spinner"></span> Caricamento...';
        elemento.disabled = true;
        elemento.classList.add('btn-loading');
    } else {
        elemento.innerHTML = elemento.dataset.originalContent || elemento.innerHTML;
        elemento.disabled = false;
        elemento.classList.remove('btn-loading');
    }
}

function aggiornaContatorieCarrello(items, totale) {
    var contatori = document.querySelectorAll('.carrello-counter');
    var totali = document.querySelectorAll('.carrello-totale');
    
    for (var i = 0; i < contatori.length; i++) {
        var el = contatori[i];
        el.textContent = items;
        el.classList.add('updating');
        (function(element) {
            setTimeout(function() { element.classList.remove('updating'); }, 800);
        })(el);
    }
    
    for (var j = 0; j < totali.length; j++) {
        totali[j].textContent = '€' + parseFloat(totale).toFixed(2);
    }
}

// ===== AJAX FUNCTIONS - VEICOLI =====

function verificaDisponibilitaVeicolo(targa, dataRitiro, dataRestituzione, callback) {
    var loadingElement = document.getElementById('disponibilita-loading');
    var resultElement = document.getElementById('disponibilita-result');
    
    if (loadingElement) {
        loadingElement.style.display = 'block';
        loadingElement.innerHTML = '<div class="disponibilita-loading"><i class="fas fa-spinner fa-spin"></i> Verifica disponibilità...</div>';
    }
    
    if (resultElement) resultElement.innerHTML = '';
    
    var formData = new FormData();
    formData.append('action', 'verifica-disponibilita');
    formData.append('targa', targa);
    formData.append('dataRitiro', dataRitiro);
    formData.append('dataRestituzione', dataRestituzione);
    formData.append('csrfToken', getCSRFToken());
    
    fetch(AJAX_CONFIG.baseUrl + '/ajax-veicoli', {
        method: 'POST',
        body: formData
    })
    .then(function(response) {
        if (!response.ok) throw new Error('Errore di rete');
        return response.json();
    })
    .then(function(data) {
        if (loadingElement) loadingElement.style.display = 'none';
        
        if (data.success) {
            var resultHtml = '';
            
            if (data.disponibile) {
                resultHtml = 
                    '<div class="alert alert-success disponibilita-result">' +
                        '<div class="row">' +
                            '<div class="col-md-8">' +
                                '<h5><i class="fas fa-check-circle"></i> Veicolo Disponibile!</h5>' +
                                '<p class="mb-1">Dal ' + data.dataRitiro + ' al ' + data.dataRestituzione + '</p>' +
                                '<small class="text-muted">' + data.giorni + ' giorni di noleggio</small>' +
                            '</div>' +
                            '<div class="col-md-4 text-right">' +
                                '<h4 class="text-success mb-0">€' + data.prezzoTotale + '</h4>' +
                                '<small class="text-muted">€' + data.prezzoPerGiorno + '/giorno</small>' +
                            '</div>' +
                        '</div>' +
                    '</div>';
                
                abilitaBottonePrenotazione(true);
                updateHiddenFormFields({
                    prezzoTotale: data.prezzoTotale,
                    giorni: data.giorni
                });
                
            } else {
                resultHtml = 
                    '<div class="alert alert-warning disponibilita-result">' +
                        '<h5><i class="fas fa-exclamation-triangle"></i> Non Disponibile</h5>' +
                        '<p class="mb-0">' + (data.message || 'Veicolo già prenotato nelle date selezionate') + '</p>' +
                    '</div>';
                
                abilitaBottonePrenotazione(false);
            }
            
            if (resultElement) resultElement.innerHTML = resultHtml;
            if (callback) callback(data);
            
        } else {
            if (resultElement) {
                resultElement.innerHTML = 
                    '<div class="alert alert-danger disponibilita-result">' +
                        '<i class="fas fa-times-circle"></i> ' + data.message +
                    '</div>';
            }
            abilitaBottonePrenotazione(false);
            mostraNotifica('error', data.message);
        }
    })
    .catch(function(error) {
        console.error('Errore verifica disponibilità:', error);
        
        if (loadingElement) loadingElement.style.display = 'none';
        if (resultElement) {
            resultElement.innerHTML = 
                '<div class="alert alert-danger disponibilita-result">' +
                    '<i class="fas fa-wifi"></i> Errore di connessione. Riprova più tardi.' +
                '</div>';
        }
        
        mostraNotifica('error', 'Errore di connessione. Verifica la tua connessione internet.');
        abilitaBottonePrenotazione(false);
    });
}

function toggleVeicoloDisponibilita(targa) {
    var toggleSwitch = document.querySelector('[data-toggle-targa="' + targa + '"]');
    var statusBadge = document.querySelector('[data-status-targa="' + targa + '"]');
    
    if (!toggleSwitch) {
        mostraNotifica('error', 'Elemento toggle non trovato');
        return;
    }
    
    var originalState = toggleSwitch.checked;
    toggleSwitch.disabled = true;
    
    var formData = new FormData();
    formData.append('action', 'toggle-disponibilita');
    formData.append('targa', targa);
    formData.append('csrfToken', getCSRFToken());
    
    fetch(AJAX_CONFIG.baseUrl + '/ajax-veicoli', {
        method: 'POST',
        body: formData
    })
    .then(function(response) { return response.json(); })
    .then(function(data) {
        if (data.success) {
            // Aggiorna UI
            if (statusBadge) {
                statusBadge.textContent = data.disponibile ? 'Disponibile' : 'Non Disponibile';
                statusBadge.className = 'badge ' + (data.disponibile ? 'badge-success' : 'badge-danger');
            }
            
            // Aggiorna riga tabella se presente
            var row = toggleSwitch.closest('tr');
            if (row) {
                row.className = data.disponibile ? 'table-success' : 'table-warning';
            }
            
            mostraNotifica('success', 
                'Veicolo ' + targa + ' ' + (data.disponibile ? 'reso disponibile' : 'reso non disponibile'));
            
        } else {
            toggleSwitch.checked = originalState;
            mostraNotifica('error', data.message || 'Errore nell\'aggiornamento');
        }
    })
    .catch(function(error) {
        console.error('Errore toggle AJAX:', error);
        toggleSwitch.checked = originalState;
        mostraNotifica('error', 'Errore di connessione');
    })
    .finally(function() {
        toggleSwitch.disabled = false;
    });
}

function ricercaVeicoli(filtri, containerSelector) {
    if (typeof containerSelector === 'undefined') containerSelector = '#veicoli-container';
    
    var container = document.querySelector(containerSelector);
    var loadingElement = document.getElementById('loading-veicoli');
    var contatoreElement = document.getElementById('contatore-risultati');
    
    if (!container) {
        console.error('Container veicoli non trovato:', containerSelector);
        return;
    }
    
    if (loadingElement) loadingElement.style.display = 'block';
    
    // Costruisci URL con parametri
    var params = new URLSearchParams();
    for (var key in filtri) {
        if (filtri[key] && filtri[key] !== '') {
            params.append(key, filtri[key]);
        }
    }
    
    fetch(AJAX_CONFIG.baseUrl + '/ajax-veicoli?action=ricerca&' + params.toString(), {
        method: 'GET',
        headers: {
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
    .then(function(response) { return response.json(); })
    .then(function(data) {
        if (data.success) {
            var html = '';
            
            if (data.veicoli && data.veicoli.length > 0) {
                for (var i = 0; i < data.veicoli.length; i++) {
                    html += generaCardVeicolo(data.veicoli[i]);
                }
            } else {
                html = 
                    '<div class="col-12">' +
                        '<div class="alert alert-info text-center">' +
                            '<i class="fas fa-search"></i>' +
                            '<h5>Nessun veicolo trovato</h5>' +
                            '<p>Prova a modificare i filtri di ricerca</p>' +
                        '</div>' +
                    '</div>';
            }
            
            container.innerHTML = html;
            
            if (contatoreElement) {
                contatoreElement.textContent = data.count + ' veicoli trovati';
            }
            
            inizializzaEventListenersVeicoli();
            
        } else {
            container.innerHTML = 
                '<div class="col-12">' +
                    '<div class="alert alert-warning text-center">' +
                        '<i class="fas fa-exclamation-triangle"></i>' +
                        'Errore nella ricerca: ' + data.message +
                    '</div>' +
                '</div>';
        }
    })
    .catch(function(error) {
        console.error('Errore ricerca AJAX:', error);
        container.innerHTML = 
            '<div class="col-12">' +
                '<div class="alert alert-danger text-center">' +
                    '<i class="fas fa-wifi"></i>' +
                    'Errore di connessione. Riprova più tardi.' +
                '</div>' +
            '</div>';
    })
    .finally(function() {
        if (loadingElement) loadingElement.style.display = 'none';
    });
}

// ===== AJAX FUNCTIONS - CARRELLO =====

function aggiungiAlCarrello(targa, dataRitiro, dataRestituzione, optionalIds) {
    if (typeof optionalIds === 'undefined') optionalIds = [];
    
    var button = document.querySelector('[data-targa="' + targa + '"]') || 
                 document.querySelector('[data-action="aggiungi-carrello"][data-veicolo="' + targa + '"]');
    
    if (!button) {
        mostraNotifica('error', 'Pulsante non trovato');
        return;
    }
    
    setLoadingState(button, true);
    
    var formData = new FormData();
    formData.append('action', 'aggiungi');
    formData.append('targa', targa);
    formData.append('dataRitiro', dataRitiro);
    formData.append('dataRestituzione', dataRestituzione);
    if (optionalIds.length > 0) {
        formData.append('optional', JSON.stringify(optionalIds));
    }
    formData.append('csrfToken', getCSRFToken());
    
    fetch(AJAX_CONFIG.baseUrl + '/ajax-carrello', {
        method: 'POST',
        body: formData
    })
    .then(function(response) { return response.json(); })
    .then(function(data) {
        if (data.success) {
            aggiornaContatorieCarrello(data.itemsCarrello, data.totaleCarrello);
            mostraNotifica('success', 'Veicolo aggiunto al carrello!');
            
            // Cambia temporaneamente il pulsante
            button.innerHTML = '<i class="fas fa-check"></i> Aggiunto';
            button.classList.remove('btn-primary');
            button.classList.add('btn-success');
            
            setTimeout(function() {
                setLoadingState(button, false);
                button.classList.remove('btn-success');
                button.classList.add('btn-primary');
            }, 2000);
            
        } else {
            setLoadingState(button, false);
            mostraNotifica('error', data.message || 'Errore nell\'aggiunta al carrello');
        }
    })
    .catch(function(error) {
        console.error('Errore AJAX carrello:', error);
        setLoadingState(button, false);
        mostraNotifica('error', 'Errore di connessione');
    });
}

function rimuoviDalCarrello(targa) {
    var formData = new FormData();
    formData.append('action', 'rimuovi');
    formData.append('targa', targa);
    formData.append('csrfToken', getCSRFToken());
    
    fetch(AJAX_CONFIG.baseUrl + '/ajax-carrello', {
        method: 'POST',
        body: formData
    })
    .then(function(response) { return response.json(); })
    .then(function(data) {
        if (data.success) {
            // Rimuovi riga dalla tabella
            var row = document.querySelector('[data-carrello-targa="' + targa + '"]');
            if (row) {
                row.style.animation = 'fadeOut 0.3s ease-out';
                setTimeout(function() { row.remove(); }, 300);
            }
            
            aggiornaContatorieCarrello(data.itemsCarrello, data.totaleCarrello);
            mostraNotifica('success', 'Veicolo rimosso dal carrello');
        } else {
            mostraNotifica('error', data.message);
        }
    })
    .catch(function(error) {
        console.error('Errore rimozione carrello:', error);
        mostraNotifica('error', 'Errore di connessione');
    });
}

function svuotaCarrello() {
    if (!confirm('Sei sicuro di voler svuotare il carrello?')) {
        return;
    }
    
    var formData = new FormData();
    formData.append('action', 'svuota');
    formData.append('csrfToken', getCSRFToken());
    
    fetch(AJAX_CONFIG.baseUrl + '/ajax-carrello', {
        method: 'POST',
        body: formData
    })
    .then(function(response) { return response.json(); })
    .then(function(data) {
        if (data.success) {
            var righeCarrello = document.querySelectorAll('[data-carrello-targa]');
            for (var i = 0; i < righeCarrello.length; i++) {
                righeCarrello[i].remove();
            }
            
            aggiornaContatorieCarrello(0, '0.00');
            
            var tbody = document.querySelector('#carrello-tbody');
            if (tbody) {
                tbody.innerHTML = 
                    '<tr>' +
                        '<td colspan="6" class="text-center text-muted">' +
                            '<i class="fas fa-shopping-cart"></i>' +
                            'Il carrello è vuoto' +
                        '</td>' +
                    '</tr>';
            }
            
            mostraNotifica('success', 'Carrello svuotato');
        } else {
            mostraNotifica('error', data.message);
        }
    })
    .catch(function(error) {
        console.error('Errore svuota carrello:', error);
        mostraNotifica('error', 'Errore di connessione');
    });
}

// ===== AJAX FUNCTIONS - ADMIN =====

function aggiornaStatisticheAdmin() {
    var refreshButton = document.getElementById('refresh-stats');
    var lastUpdateElement = document.getElementById('ultimo-aggiornamento');
    
    if (refreshButton) {
        setLoadingState(refreshButton, true);
    }
    
    fetch(AJAX_CONFIG.baseUrl + '/ajax-admin-stats', {
        method: 'GET',
        headers: {
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
    .then(function(response) { return response.json(); })
    .then(function(data) {
        if (data.success) {
            var stats = data.stats;
            
            // Aggiorna elementi delle statistiche
            updateStatElement('totale-veicoli', stats.totaleVeicoli);
            updateStatElement('veicoli-disponibili', stats.veicoliDisponibili);
            updateStatElement('veicoli-in-uso', stats.veicoliInUso);
            updateStatElement('percentuale-utilizzo', stats.percentualeUtilizzo + '%');
            
            updateStatElement('totale-utenti', stats.totaleUtenti);
            updateStatElement('totale-clienti', stats.totaleClienti);
            updateStatElement('totale-admin', stats.totaleAdmin);
            
            updateStatElement('prenotazioni-confermate', stats.prenotazioniConfermate);
            updateStatElement('prenotazioni-in-corso', stats.prenotazioniInCorso);
            updateStatElement('prenotazioni-oggi', stats.prenotazioniOggi);
            updateStatElement('fatturato-mese', '€' + stats.fatturatoMese);
            
            updateStatElement('totale-terminal', stats.totaleTerminal);
            
            if (lastUpdateElement) {
                var now = new Date().toLocaleTimeString('it-IT');
                lastUpdateElement.textContent = 'Ultimo aggiornamento: ' + now;
            }
            
            if (typeof aggiornaGraficoVeicoliPerMarca === 'function' && stats.veicoliPerMarca) {
                aggiornaGraficoVeicoliPerMarca(stats.veicoliPerMarca);
            }
            
            mostraNotifica('success', 'Statistiche aggiornate', 2000);
            
        } else {
            mostraNotifica('error', 'Errore nell\'aggiornamento: ' + data.message);
        }
    })
    .catch(function(error) {
        console.error('Errore AJAX admin stats:', error);
        mostraNotifica('error', 'Errore di connessione');
    })
    .finally(function() {
        if (refreshButton) {
            setLoadingState(refreshButton, false);
        }
    });
}

// ===== UTILITY FUNCTIONS =====

function updateStatElement(elementId, value) {
    var element = document.getElementById(elementId);
    if (element) {
        element.classList.add('updating');
        setTimeout(function() {
            element.textContent = value;
            element.classList.remove('updating');
        }, 100);
    }
}

function abilitaBottonePrenotazione(enabled) {
    var buttons = document.querySelectorAll('[data-action="prenota"], #btn-prenota');
    for (var i = 0; i < buttons.length; i++) {
        var button = buttons[i];
        button.disabled = !enabled;
        if (enabled) {
            button.classList.remove('btn-secondary');
            button.classList.add('btn-primary');
        } else {
            button.classList.remove('btn-primary');
            button.classList.add('btn-secondary');
        }
    }
}

function updateHiddenFormFields(data) {
    var fields = ['prezzoTotale', 'giorni'];
    for (var i = 0; i < fields.length; i++) {
        var field = fields[i];
        var input = document.querySelector('input[name="' + field + '"]');
        if (input && data[field]) {
            input.value = data[field];
        }
    }
}

function generaCardVeicolo(veicolo) {
    return 
        '<div class="col-md-6 col-lg-4 mb-4">' +
            '<div class="card h-100 veicolo-card">' +
                '<div class="card-img-top-container">' +
                    '<img src="' + (veicolo.immagineUrl || '/images/no-image.jpg') + '" ' +
                         'class="card-img-top" alt="' + veicolo.marca + ' ' + veicolo.modello + '">' +
                    '<div class="card-img-overlay-badge">' +
                        '<span class="badge ' + (veicolo.disponibile ? 'badge-success' : 'badge-danger') + '">' +
                            (veicolo.disponibile ? 'Disponibile' : 'Non Disponibile') +
                        '</span>' +
                    '</div>' +
                '</div>' +
                '<div class="card-body d-flex flex-column">' +
                    '<h5 class="card-title">' + veicolo.marca + ' ' + veicolo.modello + '</h5>' +
                    '<p class="card-text">' + (veicolo.tipo || '') + '</p>' +
                    '<div class="mb-3">' +
                        '<small class="text-muted">' +
                            '<i class="fas fa-gas-pump"></i> ' + veicolo.carburante + ' | ' +
                            '<i class="fas fa-cogs"></i> ' + veicolo.trasmissione +
                        '</small>' +
                    '</div>' +
                    '<div class="mt-auto">' +
                        '<div class="d-flex justify-content-between align-items-center">' +
                            '<h4 class="text-primary mb-0">€' + veicolo.prezzoPerGiorno + '/giorno</h4>' +
                            '<button class="btn btn-primary btn-sm" ' +
                                    'data-action="aggiungi-carrello" ' +
                                    'data-targa="' + veicolo.targa + '"' +
                                    (veicolo.disponibile ? '' : ' disabled') + '>' +
                                '<i class="fas fa-shopping-cart"></i> Aggiungi' +
                            '</button>' +
                        '</div>' +
                    '</div>' +
                '</div>' +
            '</div>' +
        '</div>';
}

function inizializzaEventListenersVeicoli() {
    // Event listeners per pulsanti aggiungi carrello
    var buttonsCarrello = document.querySelectorAll('[data-action="aggiungi-carrello"]');
    for (var i = 0; i < buttonsCarrello.length; i++) {
        buttonsCarrello[i].addEventListener('click', function(e) {
            e.preventDefault();
            
            var targa = this.dataset.targa;
            var dataRitiroEl = document.getElementById('dataRitiro');
            var dataRestituzioneEl = document.getElementById('dataRestituzione');
            var dataRitiroInput = document.querySelector('input[name="dataRitiro"]');
            var dataRestituzioneInput = document.querySelector('input[name="dataRestituzione"]');
            
            var dataRitiro = (dataRitiroEl && dataRitiroEl.value) || (dataRitiroInput && dataRitiroInput.value);
            var dataRestituzione = (dataRestituzioneEl && dataRestituzioneEl.value) || (dataRestituzioneInput && dataRestituzioneInput.value);
            
            if (!dataRitiro || !dataRestituzione) {
                mostraNotifica('error', 'Seleziona le date di ritiro e restituzione');
                return;
            }
            
            if (new Date(dataRestituzione) <= new Date(dataRitiro)) {
                mostraNotifica('error', 'La data di restituzione deve essere successiva al ritiro');
                return;
            }
            
            aggiungiAlCarrello(targa, dataRitiro, dataRestituzione);
        });
    }
    
    // Event listeners per toggle admin
    var toggles = document.querySelectorAll('[data-action="toggle-disponibilita"]');
    for (var j = 0; j < toggles.length; j++) {
        toggles[j].addEventListener('change', function() {
            var targa = this.dataset.toggleTarga;
            if (targa) {
                toggleVeicoloDisponibilita(targa);
            }
        });
    }
}

// ===== INIZIALIZZAZIONE =====

document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 AJAX functions inizializzate');
    
    // Inizializza event listeners
    inizializzaEventListenersVeicoli();
    
    // Auto-refresh statistiche admin ogni 30 secondi
    if (document.getElementById('admin-dashboard')) {
        setInterval(aggiornaStatisticheAdmin, 30000);
        console.log('📊 Auto-refresh admin attivato');
    }
    
    // Event listener per campi data (verifica disponibilità automatica)
    var dataFields = document.querySelectorAll('input[type="date"]');
    for (var i = 0; i < dataFields.length; i++) {
        dataFields[i].addEventListener('change', function() {
            var targaInput = document.querySelector('input[name="targa"]');
            var dataRitiroEl = document.getElementById('dataRitiro');
            var dataRestituzioneEl = document.getElementById('dataRestituzione');
            
            var targa = targaInput ? targaInput.value : null;
            var dataRitiro = dataRitiroEl ? dataRitiroEl.value : null;
            var dataRestituzione = dataRestituzioneEl ? dataRestituzioneEl.value : null;
            
            if (targa && dataRitiro && dataRestituzione) {
                verificaDisponibilitaVeicolo(targa, dataRitiro, dataRestituzione);
            }
        });
    }
    
    // Event listener per refresh statistiche
    var refreshBtn = document.getElementById('refresh-stats');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', aggiornaStatisticheAdmin);
    }
});

// Esporta funzioni per uso globale
window.EasyRideAjax = {
    verificaDisponibilitaVeicolo: verificaDisponibilitaVeicolo,
    aggiungiAlCarrello: aggiungiAlCarrello,
    rimuoviDalCarrello: rimuoviDalCarrello,
    svuotaCarrello: svuotaCarrello,
    ricercaVeicoli: ricercaVeicoli,
    aggiornaStatisticheAdmin: aggiornaStatisticheAdmin,
    toggleVeicoloDisponibilita: toggleVeicoloDisponibilita,
    mostraNotifica: mostraNotifica
};