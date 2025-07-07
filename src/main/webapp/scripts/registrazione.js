

document.addEventListener('DOMContentLoaded', function() {
    
    // Elementi del form
    const form = document.querySelector('.registration-form-container');
    const nomeInput = document.getElementById('nome');
    const cognomeInput = document.getElementById('cognome');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const confermaPasswordInput = document.getElementById('confermaPassword');
    const terminiCheckbox = document.querySelector('input[name="accettaTermini"]');
    const submitButton = document.querySelector('.btn-primary');
    
    // Password strength indicator
    const strengthBar = document.getElementById('strength-bar');
    
    // Validazione in tempo reale
    nomeInput.addEventListener('input', validateNome);
    nomeInput.addEventListener('blur', validateNome);
    
    cognomeInput.addEventListener('input', validateCognome);
    cognomeInput.addEventListener('blur', validateCognome);
    
    emailInput.addEventListener('input', validateEmail);
    emailInput.addEventListener('blur', validateEmail);
    
    passwordInput.addEventListener('input', function() {
        validatePassword();
        updatePasswordStrength();
        validateConfermaPassword(); // Ricontrolla conferma se già compilata
    });
    passwordInput.addEventListener('blur', validatePassword);
    
    confermaPasswordInput.addEventListener('input', validateConfermaPassword);
    confermaPasswordInput.addEventListener('blur', validateConfermaPassword);
    
    terminiCheckbox.addEventListener('change', validateTermini);
    
    // Validazione al submit
    form.addEventListener('submit', function(e) {
        const isValid = validateAll();
        if (!isValid) {
            e.preventDefault();
            showFormErrors();
        }
    });
    
    // Funzioni di validazione
    function validateNome() {
        const nome = nomeInput.value.trim();
        const errorElement = document.getElementById('nome-error');
        
        if (nome === '') {
            showError(nomeInput, errorElement, 'Il nome è obbligatorio');
            return false;
        }
        
        if (nome.length < 2) {
            showError(nomeInput, errorElement, 'Il nome deve contenere almeno 2 caratteri');
            return false;
        }
        
        if (!/^[a-zA-ZÀ-ÿ\s']+$/.test(nome)) {
            showError(nomeInput, errorElement, 'Il nome può contenere solo lettere');
            return false;
        }
        
        showSuccess(nomeInput, errorElement);
        return true;
    }
    
    function validateCognome() {
        const cognome = cognomeInput.value.trim();
        const errorElement = document.getElementById('cognome-error');
        
        if (cognome === '') {
            showError(cognomeInput, errorElement, 'Il cognome è obbligatorio');
            return false;
        }
        
        if (cognome.length < 2) {
            showError(cognomeInput, errorElement, 'Il cognome deve contenere almeno 2 caratteri');
            return false;
        }
        
        if (!/^[a-zA-ZÀ-ÿ\s']+$/.test(cognome)) {
            showError(cognomeInput, errorElement, 'Il cognome può contenere solo lettere');
            return false;
        }
        
        showSuccess(cognomeInput, errorElement);
        return true;
    }
    
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
    
    function validateConfermaPassword() {
        const password = passwordInput.value;
        const confermaPassword = confermaPasswordInput.value;
        const errorElement = document.getElementById('conferma-password-error');
        
        if (confermaPassword === '') {
            showError(confermaPasswordInput, errorElement, 'La conferma password è obbligatoria');
            return false;
        }
        
        if (password !== confermaPassword) {
            showError(confermaPasswordInput, errorElement, 'Le password non coincidono');
            return false;
        }
        
        showSuccess(confermaPasswordInput, errorElement);
        return true;
    }
    
    function validateTermini() {
        const errorElement = document.getElementById('termini-error');
        
        if (!terminiCheckbox.checked) {
            showError(terminiCheckbox, errorElement, 'Devi accettare i termini e condizioni');
            return false;
        }
        
        showSuccess(terminiCheckbox, errorElement);
        return true;
    }
    
    function validateAll() {
        const isNomeValid = validateNome();
        const isCognomeValid = validateCognome();
        const isEmailValid = validateEmail();
        const isPasswordValid = validatePassword();
        const isConfermaPasswordValid = validateConfermaPassword();
        const isTerminiValid = validateTermini();
        
        return isNomeValid && isCognomeValid && isEmailValid && 
               isPasswordValid && isConfermaPasswordValid && isTerminiValid;
    }
    
    function updatePasswordStrength() {
        const password = passwordInput.value;
        
        if (password.length === 0) {
            strengthBar.className = 'strength-bar';
            return;
        }
        
        let strength = 0;
        
        // Criteri di forza
        if (password.length >= 6) strength++;
        if (password.length >= 10) strength++;
        if (/[A-Z]/.test(password)) strength++;
        if (/[a-z]/.test(password)) strength++;
        if (/[0-9]/.test(password)) strength++;
        if (/[^A-Za-z0-9]/.test(password)) strength++;
        
        // Applica classe CSS
        if (strength <= 2) {
            strengthBar.className = 'strength-bar weak';
        } else if (strength <= 4) {
            strengthBar.className = 'strength-bar medium';
        } else {
            strengthBar.className = 'strength-bar strong';
        }
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
    
    // Auto-format nome e cognome (prima lettera maiuscola)
    function formatName(input) {
        input.addEventListener('blur', function() {
            const value = this.value.trim();
            if (value) {
                this.value = value.charAt(0).toUpperCase() + value.slice(1).toLowerCase();
            }
        });
    }
    
    formatName(nomeInput);
    formatName(cognomeInput);
    
    // Email lowercase automatico
    emailInput.addEventListener('blur', function() {
        this.value = this.value.toLowerCase();
    });
});