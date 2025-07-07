

document.addEventListener('DOMContentLoaded', function() {
    
    // Elementi del form
    const form = document.querySelector('.auth-form');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const submitButton = document.querySelector('.btn-primary');
    
    // Validazione in tempo reale
    emailInput.addEventListener('input', validateEmail);
    emailInput.addEventListener('blur', validateEmail);
    
    passwordInput.addEventListener('input', validatePassword);
    passwordInput.addEventListener('blur', validatePassword);
    
    // Validazione al submit
    form.addEventListener('submit', function(e) {
        const isValid = validateAll();
        if (!isValid) {
            e.preventDefault();
            showFormErrors();
        }
    });
    
    // Auto-focus primo campo vuoto
    if (emailInput.value.trim() === '') {
        emailInput.focus();
    } else if (passwordInput.value.trim() === '') {
        passwordInput.focus();
    }
    
    // Funzioni di validazione
    function validateEmail() {
        const email = emailInput.value.trim();
        const errorElement = document.getElementById('email-error');
        
        if (email === '') {
            showError(emailInput, errorElement, 'L\'email è obbligatoria');
            return false;
        }
        
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            showError(emailInput, errorElement, 'Formato email non valido');
            return false;
        }
        
        showSuccess(emailInput, errorElement);
        return true;
    }
    
    function validatePassword() {
        const password = passwordInput.value;
        const errorElement = document.getElementById('password-error');
        
        if (password === '') {
            showError(passwordInput, errorElement, 'La password è obbligatoria');
            return false;
        }
        
        if (password.length < 6) {
            showError(passwordInput, errorElement, 'La password deve contenere almeno 6 caratteri');
            return false;
        }
        
        showSuccess(passwordInput, errorElement);
        return true;
    }
    
    function validateAll() {
        const isEmailValid = validateEmail();
        const isPasswordValid = validatePassword();
        
        return isEmailValid && isPasswordValid;
    }
    
    function showError(inputElement, errorElement, message) {
        inputElement.classList.remove('success');
        inputElement.classList.add('error');
        errorElement.textContent = message;
        errorElement.style.display = 'block';
    }
    
    function showSuccess(inputElement, errorElement) {
        inputElement.classList.remove('error');
        inputElement.classList.add('success');
        errorElement.textContent = '';
        errorElement.style.display = 'none';
    }
    
    function showFormErrors() {
        // Scorri verso l'alto al primo errore
        const firstError = document.querySelector('.error');
        if (firstError) {
            firstError.scrollIntoView({ 
                behavior: 'smooth', 
                block: 'center' 
            });
            firstError.focus();
        }
    }
    
    // Email lowercase automatico
    emailInput.addEventListener('blur', function() {
        this.value = this.value.toLowerCase().trim();
    });
    
    // Gestione "Ricordami" con local storage (solo UI, non sicurezza)
    const rememberCheckbox = document.querySelector('input[name="remember"]');
    const savedEmail = localStorage.getItem('easyride_remember_email');
    
    if (savedEmail && emailInput.value.trim() === '') {
        emailInput.value = savedEmail;
        rememberCheckbox.checked = true;
    }
    
    form.addEventListener('submit', function() {
        if (rememberCheckbox.checked && validateEmail()) {
            localStorage.setItem('easyride_remember_email', emailInput.value.trim());
        } else {
            localStorage.removeItem('easyride_remember_email');
        }
    });
});