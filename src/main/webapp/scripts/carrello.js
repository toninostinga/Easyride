

document.addEventListener('DOMContentLoaded', function() {
    initializeCarrello();
});

function initializeCarrello() {
    console.log('🛒 Inizializzazione carrello JavaScript');
    
    // Inizializza componenti originali
    initOptionalUpdates();
    initItemRemoval();
    initQuantityUpdates();
    initPriceCalculations();
    // ✅ RIMOSSA COMPLETAMENTE initCheckoutValidation();
    initKeyboardShortcuts();
    
    // Inizializza nuove funzionalità
    initImageHandling();
    initNotificationSystem();
    initAdvancedFeatures();
    
    // ✅ FORZA CHECKOUT LIBERO
    ensureCheckoutFreedom();
    
    // Auto-save carrello ogni 30 secondi (se modificato)
    setInterval(autoSaveCarrello, 30000);
    
    console.log('✅ Carrello JavaScript inizializzato completamente (CHECKOUT LIBERO)');
}

// ===== ✅ CHECKOUT COMPLETAMENTE LIBERO =====

function ensureCheckoutFreedom() {
    console.log('🚀 Assicurando libertà completa di checkout...');
    
    // Override globali
    window.validateCarrelloForCheckout = function() {
        console.log('✅ Validazione carrello: SEMPRE TRUE');
        return true;
    };
    
    // Rimuovi qualsiasi listener esistente dai link checkout
    setTimeout(() => {
        const checkoutLinks = document.querySelectorAll('a[href*="checkout"]');
        checkoutLinks.forEach(link => {
            // Clona il link per rimuovere TUTTI i listener
            const newLink = link.cloneNode(true);
            if (link.parentNode) {
                link.parentNode.replaceChild(newLink, link);
            }
        });
        console.log('✅ Tutti i listener checkout rimossi completamente');
    }, 100);
    
    console.log('🎯 Checkout ora è completamente libero!');
}

// ===== GESTIONE IMMAGINI AVANZATA =====

function initImageHandling() {
    console.log('🖼️ Inizializzando gestione immagini...');
    
    // Controlla immagini già caricate ma fallite
    const images = document.querySelectorAll('.item-image img');
    images.forEach(img => {
        if (img.complete && img.naturalHeight === 0) {
            handleImageError(img);
        }
    });
    
    // Inizializza zoom per tutte le immagini
    const imageContainers = document.querySelectorAll('.item-image');
    imageContainers.forEach(container => {
        const img = container.querySelector('img');
        if (img && img.style.display !== 'none') {
            container.style.cursor = 'zoom-in';
        }
    });
}

// Gestione errori immagini con fallback multipli
function handleImageError(img) {
    console.log('❌ Errore caricamento immagine:', img.src);
    
    const targa = img.getAttribute('data-targa');
    const vehicleName = img.getAttribute('data-vehicle');
    
    // Array di fallback in ordine di priorità
    const fallbackUrls = [
        window.contextPath + '/images/veicoli/' + targa + '.png',
        window.contextPath + '/images/veicoli/' + targa + '.jpeg', 
        window.contextPath + '/images/veicoli/' + targa.toLowerCase() + '.jpg',
        window.contextPath + '/images/veicoli/' + targa.toUpperCase() + '.jpg',
        `https://picsum.photos/300/200?random=${Math.floor(Math.random() * 1000)}`,
        `https://via.placeholder.com/300x200/3498db/ffffff?text=${encodeURIComponent(vehicleName || 'Auto')}`
    ];
    
    // Prova il prossimo fallback
    if (!img.dataset.fallbackIndex) {
        img.dataset.fallbackIndex = '0';
    }
    
    const fallbackIndex = parseInt(img.dataset.fallbackIndex);
    
    if (fallbackIndex < fallbackUrls.length) {
        console.log(`🔄 Tentativo fallback ${fallbackIndex + 1}:`, fallbackUrls[fallbackIndex]);
        img.dataset.fallbackIndex = (fallbackIndex + 1).toString();
        img.src = fallbackUrls[fallbackIndex];
    } else {
        // Ultimo resort: crea placeholder personalizzato
        console.log('🎨 Creando placeholder personalizzato per:', vehicleName);
        createCustomPlaceholder(img, vehicleName, targa);
    }
}

// Crea placeholder personalizzato quando tutte le immagini falliscono
function createCustomPlaceholder(img, vehicleName, targa) {
    const container = img.parentElement;
    
    // Nascondi immagine rotta
    img.style.display = 'none';
    
    // Crea placeholder solo se non esiste già
    if (!container.querySelector('.custom-placeholder')) {
        const placeholder = document.createElement('div');
        placeholder.className = 'custom-placeholder';
        placeholder.innerHTML = `
            <i class="fas fa-car" style="font-size: 2.5rem; margin-bottom: 0.5rem; color: #95a5a6;"></i>
            <div style="font-size: 0.9rem; line-height: 1.3; margin-bottom: 0.3rem; color: #7f8c8d; font-weight: 600;">
                ${vehicleName || 'Veicolo'}
            </div>
            <div style="font-size: 0.8rem; opacity: 0.8; background: rgba(255,255,255,0.7); 
                        padding: 0.2rem 0.5rem; border-radius: 10px; color: #6c757d;">
                Targa: ${targa || 'N/A'}
            </div>
            <div style="font-size: 0.7rem; opacity: 0.6; margin-top: 0.3rem; color: #95a5a6;">
                Immagine non disponibile
            </div>
        `;
        
        container.appendChild(placeholder);
        
        // Rimuovi cursor zoom-in dal container
        container.style.cursor = 'default';
    }
}

// Gestione zoom immagini
function openVehicleImageZoom(container) {
    const img = container.querySelector('img');
    
    if (!img || img.style.display === 'none') {
        showCartNotification('Immagine non disponibile per questo veicolo', 'info');
        return;
    }
    
    // Crea overlay per zoom
    const overlay = document.createElement('div');
    overlay.className = 'image-zoom-overlay';
    
    // Immagine ingrandita
    const zoomedImg = document.createElement('img');
    zoomedImg.src = img.src;
    zoomedImg.alt = img.alt;
    
    // Bottone chiusura
    const closeBtn = document.createElement('button');
    closeBtn.className = 'zoom-close-btn';
    closeBtn.innerHTML = '<i class="fas fa-times"></i>';
    
    overlay.appendChild(zoomedImg);
    overlay.appendChild(closeBtn);
    document.body.appendChild(overlay);
    
    // Previeni scroll del body
    document.body.style.overflow = 'hidden';
    
    // Mostra con animazione
    setTimeout(() => overlay.classList.add('show'), 50);
    
    // Gestori per chiusura
    const closeZoom = () => {
        overlay.classList.remove('show');
        setTimeout(() => {
            if (overlay.parentNode) {
                overlay.remove();
            }
            document.body.style.overflow = 'auto';
        }, 300);
    };
    
    // Eventi di chiusura
    overlay.addEventListener('click', function(e) {
        if (e.target === overlay || e.target === closeBtn) {
            closeZoom();
        }
    });
    
    closeBtn.addEventListener('click', closeZoom);
    
    // Chiusura con tasto ESC
    const handleEscape = (e) => {
        if (e.key === 'Escape') {
            closeZoom();
            document.removeEventListener('keydown', handleEscape);
        }
    };
    document.addEventListener('keydown', handleEscape);
}

// ===== SISTEMA NOTIFICHE AVANZATO =====

function initNotificationSystem() {
    console.log('🔔 Inizializzando sistema notifiche...');
    
    // Controlla parametri URL per notifiche automatiche
    const urlParams = new URLSearchParams(window.location.search);
    
    if (urlParams.get('notification') === 'added') {
        const vehicleName = urlParams.get('vehicle') || 'Veicolo';
        setTimeout(() => {
            showCartNotification(`✅ ${vehicleName} aggiunto al carrello!`, 'success');
        }, 500);
        
        // Pulisce URL dai parametri
        window.history.replaceState({}, document.title, window.location.pathname);
    }
    
    if (urlParams.get('notification') === 'removed') {
        setTimeout(() => {
            showCartNotification('Elemento rimosso dal carrello', 'warning');
        }, 500);
    }
}

// Funzione principale per mostrare notifiche carrello
function showCartNotification(message, type = 'success', duration = 4000) {
    // Rimuovi notifiche esistenti
    const existing = document.querySelectorAll('.cart-notification');
    existing.forEach(n => n.remove());
    
    // Crea notifica
    const notification = document.createElement('div');
    notification.className = `cart-notification ${type}`;
    
    const icons = {
        success: '✅',
        error: '❌', 
        warning: '⚠️',
        info: 'ℹ️'
    };
    
    notification.innerHTML = `
        <div style="display: flex; align-items: center; justify-content: space-between; gap: 1rem;">
            <div style="display: flex; align-items: center; gap: 0.5rem;">
                <span style="font-size: 1.2rem;">${icons[type] || icons.info}</span>
                <span>${message}</span>
            </div>
            <button onclick="this.closest('.cart-notification').remove()" 
                    style="background: none; border: none; color: white; font-size: 1.5rem; 
                           cursor: pointer; opacity: 0.8; transition: opacity 0.2s;">×</button>
        </div>
    `;
    
    document.body.appendChild(notification);
    
    // Animazione entrata
    setTimeout(() => notification.classList.add('show'), 100);
    
    // Anima icona carrello nell'header
    animateCartIcon();
    
    // Rimozione automatica
    setTimeout(() => {
        if (notification.parentNode) {
            notification.classList.remove('show');
            setTimeout(() => notification.remove(), 400);
        }
    }, duration);
}

// Animazione icona carrello nell'header
function animateCartIcon() {
    const cartElements = document.querySelectorAll('a[href*="carrello"], .carrello-link, [class*="cart"]');
    
    cartElements.forEach(element => {
        element.style.animation = 'cartIconBounce 0.8s ease-in-out';
        element.style.boxShadow = '0 0 20px rgba(52, 152, 219, 0.6)';
        element.style.transition = 'box-shadow 0.3s ease';
        
        setTimeout(() => {
            element.style.animation = '';
            element.style.boxShadow = '';
        }, 800);
    });
    
    // Aggiungi animazione CSS se non esiste
    if (!document.querySelector('.cart-icon-animation')) {
        const style = document.createElement('style');
        style.className = 'cart-icon-animation';
        style.textContent = `
            @keyframes cartIconBounce {
                0%, 100% { transform: scale(1) rotate(0deg); }
                15% { transform: scale(1.15) rotate(-5deg); }
                30% { transform: scale(1.1) rotate(3deg); }
                45% { transform: scale(1.2) rotate(-2deg); }
                60% { transform: scale(1.05) rotate(1deg); }
                75% { transform: scale(1.1) rotate(-1deg); }
            }
        `;
        document.head.appendChild(style);
    }
}

// ===== GESTIONE OPTIONAL (ORIGINALE MIGLIORATA) =====

function initOptionalUpdates() {
    const optionalForms = document.querySelectorAll('.optional-form');
    
    optionalForms.forEach(form => {
        const checkboxes = form.querySelectorAll('input[type="checkbox"]');
        
        checkboxes.forEach(checkbox => {
            checkbox.addEventListener('change', function() {
                updateOptionalPreview(form);
                highlightChanges(form);
            });
        });
        
        // Auto-submit dopo un breve delay
        let timeoutId;
        checkboxes.forEach(checkbox => {
            checkbox.addEventListener('change', function() {
                clearTimeout(timeoutId);
                timeoutId = setTimeout(() => {
                    submitOptionalUpdate(form);
                }, 1500);
            });
        });
    });
}

function updateOptionalPreview(form) {
    const itemId = form.querySelector('input[name="itemId"]').value;
    const checkedBoxes = form.querySelectorAll('input[type="checkbox"]:checked');
    const carrelloItem = form.closest('.carrello-item');
    
    // Calcola nuovo prezzo optional
    let prezzoOptional = 0;
    checkedBoxes.forEach(checkbox => {
        const optionalItem = checkbox.closest('.optional-item');
        const prezzoText = optionalItem.querySelector('.optional-price').textContent;
        const prezzo = parseFloat(prezzoText.replace(/[€+\/giorno]/g, ''));
        prezzoOptional += prezzo;
    });
    
    // Recupera giorni dal DOM
    const giorni = getNumeroGiorni(carrelloItem);
    const prezzoOptionalTotale = prezzoOptional * giorni;
    
    // Aggiorna preview visivo
    updateOptionalPreviewDisplay(carrelloItem, prezzoOptionalTotale);
    
    console.log(`💰 Preview optional aggiornato per item ${itemId}: €${prezzoOptionalTotale}`);
}

function updateOptionalPreviewDisplay(carrelloItem, prezzoOptional) {
    const pricingSection = carrelloItem.querySelector('.item-pricing');
    let optionalRow = pricingSection.querySelector('.pricing-row-optional');
    
    if (prezzoOptional > 0) {
        if (!optionalRow) {
            optionalRow = document.createElement('div');
            optionalRow.className = 'pricing-row pricing-row-optional preview';
            optionalRow.innerHTML = '<span><i class="fas fa-plus pricing-icon"></i>Optional:</span><span class="optional-price-display">€0</span>';
            
            const totalRow = pricingSection.querySelector('.pricing-total');
            totalRow.parentNode.insertBefore(optionalRow, totalRow);
        }
        
        optionalRow.querySelector('.optional-price-display').textContent = `€${prezzoOptional.toFixed(2)}`;
        optionalRow.style.display = 'flex';
    } else if (optionalRow) {
        optionalRow.style.display = 'none';
    }
}

function submitOptionalUpdate(form) {
    const submitBtn = form.querySelector('button[type="submit"]');
    const originalText = submitBtn.textContent;
    
    // Indica aggiornamento in corso
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Aggiornamento...';
    submitBtn.disabled = true;
    
    const carrelloItem = form.closest('.carrello-item');
    carrelloItem.classList.add('updating');
    
    // Prepara dati
    const formData = new FormData(form);
    
    fetch(form.action, {
        method: 'POST',
        body: formData
    })
    .then(response => {
        if (response.ok) {
            return response.text();
        }
        throw new Error('Errore nell\'aggiornamento');
    })
    .then(html => {
        showCartNotification('✅ Optional aggiornati con successo', 'success');
        
        // Rimuovi classi di preview
        carrelloItem.querySelectorAll('.preview').forEach(el => {
            el.classList.remove('preview');
        });
        
        // Aggiorna totale carrello nella sidebar
        updateTotaleCarrello();
        
        console.log('✅ Optional aggiornati via AJAX');
    })
    .catch(error => {
        console.error('❌ Errore aggiornamento optional:', error);
        showCartNotification('❌ Errore nell\'aggiornamento degli optional', 'error');
    })
    .finally(() => {
        // Ripristina button
        submitBtn.innerHTML = originalText;
        submitBtn.disabled = false;
        carrelloItem.classList.remove('updating');
    });
}

// ===== RIMOZIONE ITEMS (ORIGINALE MIGLIORATA) =====

function initItemRemoval() {
    const removeButtons = document.querySelectorAll('.remove-btn');
    
    removeButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            confirmItemRemoval(this);
        });
    });
}

function confirmItemRemoval(button) {
    const carrelloItem = button.closest('.carrello-item');
    const veicoloNome = carrelloItem.querySelector('.item-header h3').textContent;
    
    // Dialog di conferma personalizzato
    showCustomConfirm(
        '🗑️ Rimuovi dal Carrello',
        `Sei sicuro di voler rimuovere ${veicoloNome} dal carrello?`,
        'Questa azione non può essere annullata.',
        () => {
            removeItemWithAnimation(button.closest('form'), carrelloItem);
        }
    );
}

function removeItemWithAnimation(form, carrelloItem) {
    // Animazione di rimozione
    carrelloItem.style.transition = 'all 0.3s ease-out';
    carrelloItem.style.transform = 'translateX(-100%)';
    carrelloItem.style.opacity = '0';
    
    setTimeout(() => {
        const formData = new FormData(form);
        
        fetch(form.action, {
            method: 'POST',
            body: formData
        })
        .then(response => {
            if (response.ok) {
                carrelloItem.remove();
                showCartNotification('✅ Prenotazione rimossa dal carrello', 'success');
                updateTotaleCarrello();
                checkEmptyCarrello();
            } else {
                throw new Error('Errore nella rimozione');
            }
        })
        .catch(error => {
            console.error('❌ Errore rimozione item:', error);
            showCartNotification('❌ Errore nella rimozione della prenotazione', 'error');
            
            // Ripristina item se errore
            carrelloItem.style.transform = 'translateX(0)';
            carrelloItem.style.opacity = '1';
        });
    }, 300);
}

// Dialog di conferma personalizzato
function showCustomConfirm(title, message, subtitle, onConfirm, onCancel) {
    // Crea overlay
    const overlay = document.createElement('div');
    overlay.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0,0,0,0.6);
        z-index: 15000;
        display: flex;
        align-items: center;
        justify-content: center;
        opacity: 0;
        transition: opacity 0.3s ease;
        backdrop-filter: blur(5px);
    `;
    
    // Crea dialog
    const dialog = document.createElement('div');
    dialog.style.cssText = `
        background: white;
        border-radius: 20px;
        padding: 2.5rem;
        max-width: 450px;
        width: 90%;
        text-align: center;
        box-shadow: 0 25px 80px rgba(0,0,0,0.3);
        transform: scale(0.8) translateY(50px);
        transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
    `;
    
    dialog.innerHTML = `
        <div style="font-size: 3.5rem; margin-bottom: 1.5rem;">🤔</div>
        <h3 style="color: #2c3e50; margin-bottom: 1rem; font-size: 1.4rem; font-weight: 700;">${title}</h3>
        <p style="margin-bottom: 0.5rem; color: #34495e; font-size: 1rem; line-height: 1.5;">${message}</p>
        <p style="margin-bottom: 2.5rem; color: #7f8c8d; font-size: 0.85rem; line-height: 1.4;">${subtitle}</p>
        <div style="display: flex; gap: 1rem; justify-content: center;">
            <button class="btn-cancel" style="background: #95a5a6; color: white; border: none; padding: 0.8rem 2rem; 
                    border-radius: 25px; cursor: pointer; font-weight: 600; font-size: 0.95rem; transition: all 0.3s ease;">
                <i class='fas fa-times'></i> Annulla
            </button>
            <button class="btn-confirm" style="background: linear-gradient(135deg, #e74c3c, #c0392b); color: white; 
                    border: none; padding: 0.8rem 2rem; border-radius: 25px; cursor: pointer; font-weight: 600; 
                    font-size: 0.95rem; transition: all 0.3s ease; box-shadow: 0 4px 15px rgba(231, 76, 60, 0.3);">
                <i class='fas fa-check'></i> Conferma
            </button>
        </div>
    `;
    
    overlay.appendChild(dialog);
    document.body.appendChild(overlay);
    
    // Mostra dialog
    requestAnimationFrame(() => {
        overlay.style.opacity = '1';
        dialog.style.transform = 'scale(1) translateY(0)';
    });
    
    // Previeni scroll
    document.body.style.overflow = 'hidden';
    
    // Gestori eventi
    const closeDialog = () => {
        overlay.style.opacity = '0';
        dialog.style.transform = 'scale(0.8) translateY(50px)';
        setTimeout(() => {
            if (overlay.parentNode) {
                overlay.remove();
            }
            document.body.style.overflow = 'auto';
        }, 400);
    };
    
    dialog.querySelector('.btn-cancel').addEventListener('click', () => {
        closeDialog();
        if (onCancel) onCancel();
    });
    
    dialog.querySelector('.btn-confirm').addEventListener('click', () => {
        closeDialog();
        if (onConfirm) onConfirm();
    });
    
    overlay.addEventListener('click', function(e) {
        if (e.target === overlay) {
            closeDialog();
            if (onCancel) onCancel();
        }
    });
}

// ===== CALCOLI E AGGIORNAMENTI (ORIGINALI) =====

function initQuantityUpdates() {
    // Placeholder per future funzionalità di quantità
    console.log('📊 Sistema quantità inizializzato');
}

function initPriceCalculations() {
    // Ricalcola prezzi quando necessario
    recalculateAllPrices();
}

function recalculateAllPrices() {
    const carrelloItems = document.querySelectorAll('.carrello-item');
    let totaleCarrello = 0;
    
    carrelloItems.forEach(item => {
        const prezzoTotaleEl = item.querySelector('.pricing-total span:last-child');
        if (prezzoTotaleEl) {
            const prezzo = parseFloat(prezzoTotaleEl.textContent.replace('€', ''));
            totaleCarrello += prezzo;
        }
    });
    
    updateTotaleDisplay(totaleCarrello);
}

function updateTotaleCarrello() {
    // Richiesta AJAX per ottenere totali aggiornati
    fetch(`${getContextPath()}/api/carrello/totali`, {
        method: 'GET',
        headers: {
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
    .then(response => response.json())
    .then(data => {
        updateTotaleDisplay(data.totaleCarrello);
        updateCounterInHeader(data.numeroItems);
    })
    .catch(error => {
        console.warn('⚠️ Impossibile aggiornare totali via AJAX, usando calcolo locale');
        recalculateAllPrices();
    });
}

function updateTotaleDisplay(totale) {
    const totaleElements = document.querySelectorAll('.totale-finale span:last-child');
    totaleElements.forEach(el => {
        el.textContent = `€${totale.toFixed(2)}`;
    });
}

function updateCounterInHeader(numeroItems) {
    // Aggiorna contatore carrello nell'header se presente
    const counterElement = document.querySelector('.carrello-counter');
    if (counterElement) {
        counterElement.textContent = numeroItems;
        
        if (numeroItems > 0) {
            counterElement.style.display = 'inline-block';
        } else {
            counterElement.style.display = 'none';
        }
    }
}

// ===== FEATURES AVANZATE =====

function initAdvancedFeatures() {
    initAdvancedHoverEffects();
    initClearCartButton();
    addRippleEffects();
}

function initAdvancedHoverEffects() {
    const items = document.querySelectorAll('.carrello-item');
    
    items.forEach(item => {
        item.addEventListener('mouseenter', function() {
            this.style.transition = 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)';
            this.style.transform = 'translateY(-4px) scale(1.01)';
            this.style.boxShadow = '0 15px 35px rgba(0,0,0,0.15)';
        });
        
        item.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0) scale(1)';
            this.style.boxShadow = '0 4px 12px rgba(0,0,0,0.08)';
        });
    });
}

function initClearCartButton() {
    const clearCartBtn = document.querySelector('.clear-cart-btn');
    
    if (clearCartBtn) {
        clearCartBtn.addEventListener('click', function(e) {
            e.preventDefault();
            const form = this.closest('.clear-cart-form');
            
            showCustomConfirm(
                '🧹 Svuota Carrello',
                'Sei sicuro di voler svuotare completamente il carrello?',
                'Tutti gli elementi verranno rimossi e questa azione non può essere annullata.',
                () => {
                    form.submit();
                }
            );
        });
    }
}

function addRippleEffects() {
    const buttons = document.querySelectorAll('.btn');
    
    buttons.forEach(btn => {
        btn.addEventListener('click', function(e) {
            const ripple = document.createElement('span');
            const rect = this.getBoundingClientRect();
            const size = Math.max(rect.width, rect.height);
            const x = e.clientX - rect.left - size / 2;
            const y = e.clientY - rect.top - size / 2;
            
            ripple.style.cssText = `
                position: absolute;
                width: ${size}px;
                height: ${size}px;
                left: ${x}px;
                top: ${y}px;
                background: rgba(255,255,255,0.6);
                border-radius: 50%;
                transform: scale(0);
                animation: ripple 0.6s linear;
                pointer-events: none;
            `;
            
            this.style.position = 'relative';
            this.style.overflow = 'hidden';
            this.appendChild(ripple);
            
            setTimeout(() => {
                if (ripple.parentNode) {
                    ripple.remove();
                }
            }, 600);
        });
    });
    
    // Aggiungi animazione ripple se non esiste
    if (!document.querySelector('.ripple-animation')) {
        const style = document.createElement('style');
        style.className = 'ripple-animation';
        style.textContent = `
            @keyframes ripple {
                to {
                    transform: scale(4);
                    opacity: 0;
                }
            }
        `;
        document.head.appendChild(style);
    }
}

// ===== KEYBOARD SHORTCUTS MODIFICATI =====

function initKeyboardShortcuts() {
    document.addEventListener('keydown', function(e) {
        // Ctrl/Cmd + Delete = Svuota carrello
        if ((e.ctrlKey || e.metaKey) && e.key === 'Delete') {
            const svuotaButton = document.querySelector('.clear-cart-btn');
            if (svuotaButton) {
                svuotaButton.click();
            }
        }
        
        // ✅ CTRL+ENTER MODIFICATO - FORZA CHECKOUT SENZA VALIDAZIONI
        if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
            const checkoutButton = document.querySelector('a[href*="checkout"]');
            if (checkoutButton) {
                console.log('🚀 Checkout forzato da tastiera - nessuna validazione');
                // Vai direttamente al link
                window.location.href = checkoutButton.href;
            }
        }
        
        // ESC = Chiudi notifiche
        if (e.key === 'Escape') {
            const notifications = document.querySelectorAll('.cart-notification');
            notifications.forEach(n => n.remove());
        }
    });
}

// ===== AUTO-SAVE (ORIGINALE) =====

let lastModificationTime = Date.now();
let isModified = false;

function markAsModified() {
    isModified = true;
    lastModificationTime = Date.now();
}

function autoSaveCarrello() {
    if (!isModified || Date.now() - lastModificationTime < 5000) {
        return;
    }
    
    console.log('💾 Auto-save carrello...');
    
    // Salva stato corrente nel localStorage
    try {
        localStorage.setItem('carrello_backup', JSON.stringify({
            timestamp: Date.now(),
            url: window.location.href
        }));
    } catch (e) {
        console.warn('⚠️ Impossibile salvare backup carrello:', e);
    }
    
    isModified = false;
}

// ===== UTILITY FUNCTIONS (ORIGINALI + NUOVE) =====

function getNumeroGiorni(carrelloItem) {
    const infoRows = carrelloItem.querySelectorAll('.info-row');
    for (let row of infoRows) {
        const label = row.querySelector('.info-label');
        if (label && label.textContent.includes('Periodo')) {
            const value = row.querySelector('.info-value').textContent;
            const match = value.match(/\((\d+) giorni\)/);
            return match ? parseInt(match[1]) : 1;
        }
    }
    return 1;
}

function getDataFromItem(item, tipo) {
    const infoRows = item.querySelectorAll('.info-row');
    for (let row of infoRows) {
        const label = row.querySelector('.info-label');
        if (label && label.textContent.includes('Periodo')) {
            const value = row.querySelector('.info-value').textContent;
            const dates = value.split(' - ');
            if (tipo === 'ritiro') {
                return dates[0];
            } else if (tipo === 'restituzione') {
                return dates[1].split(' (')[0];
            }
        }
    }
    return null;
}

function checkEmptyCarrello() {
    const carrelloItems = document.querySelectorAll('.carrello-item');
    
    if (carrelloItems.length === 0) {
        setTimeout(() => {
            window.location.reload();
        }, 1000);
    }
}

function highlightChanges(form) {
    const carrelloItem = form.closest('.carrello-item');
    carrelloItem.classList.add('modified');
    
    setTimeout(() => {
        carrelloItem.classList.remove('modified');
    }, 2000);
}

function getContextPath() {
    return window.contextPath || window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));
}

function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// ===== MESSAGES E NOTIFICATIONS (LEGACY SUPPORT) =====

function showSuccessMessage(message) {
    showCartNotification(message, 'success');
}

function showErrorMessage(message) {
    showCartNotification(message, 'error');
}

function showNotification(message, type) {
    showCartNotification(message, type);
}

function showCustomConfirm_Legacy(title, message) {
    // Per compatibilità con codice esistente
    return confirm(`${title}\n\n${message}`);
}

// ===== EVENT LISTENERS GLOBALI =====

// Marca carrello come modificato quando ci sono cambiamenti
document.addEventListener('change', function(e) {
    if (e.target.matches('.carrello-item input, .carrello-item select')) {
        markAsModified();
    }
});

// Conferma prima di lasciare la pagina se ci sono modifiche non salvate
window.addEventListener('beforeunload', function(e) {
    if (isModified && Date.now() - lastModificationTime < 30000) {
        e.preventDefault();
        return 'Ci sono modifiche non salvate al carrello. Sei sicuro di voler uscire?';
    }
});

// ===== EXPORT FUNCTIONS FOR GLOBAL USE =====

// Esporta funzioni per uso dal JSP e altri script
window.handleImageError = handleImageError;
window.showCartNotification = showCartNotification;
window.openVehicleImageZoom = openVehicleImageZoom;
window.createCustomPlaceholder = createCustomPlaceholder;
window.showCustomConfirm = showCustomConfirm;
window.animateCartIcon = animateCartIcon;
window.markAsModified = markAsModified;

// ✅ EXPORT GLOBALI PER CHECKOUT LIBERO
window.validateCarrelloForCheckout = function() {
    console.log('✅ Validazione carrello globale: SEMPRE VALIDO');
    return true;
};

window.forceCheckout = function() {
    const checkoutLink = document.querySelector('a[href*="checkout"]');
    if (checkoutLink) {
        console.log('🚀 Checkout forzato programmaticamente');
        window.location.href = checkoutLink.href;
    }
};

console.log('🛒 Carrello JavaScript completo caricato e pronto! (CHECKOUT COMPLETAMENTE LIBERO)');