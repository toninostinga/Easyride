package it.easyridedb.model;

import java.util.regex.Pattern;


public class Terminal {
    
    // Pattern per validazione email e telefono
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private static final Pattern TELEFONO_PATTERN = Pattern.compile(
        "^[+]?[0-9\\s\\-\\(\\)]{8,20}$"
    );
   
    private int id;                     
    private String nome;                
    private String indirizzo;           
    private String telefono;           
    private String email;               
    
    // ========== COSTRUTTORI ==========
    
   
    public Terminal() {
        // Costruttore vuoto per frameworks
    }
    
    public Terminal(String nome, String indirizzo, String telefono, String email) {
        setNome(nome);
        setIndirizzo(indirizzo);
        setTelefono(telefono);
        setEmail(email);
    }
    
   
    public Terminal(int id, String nome, String indirizzo, String telefono, String email) {
        this.id = id;
        setNome(nome);
        setIndirizzo(indirizzo);
        setTelefono(telefono);
        setEmail(email);
    }
    
    // ========== GETTER E SETTER CON VALIDAZIONI ==========
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome del terminal non può essere vuoto");
        }
        if (nome.length() > 100) {
            throw new IllegalArgumentException("Il nome del terminal non può superare i 100 caratteri");
        }
        this.nome = nome.trim();
    }
    
    public String getIndirizzo() {
        return indirizzo;
    }
    
    public void setIndirizzo(String indirizzo) {
        if (indirizzo == null || indirizzo.trim().isEmpty()) {
            throw new IllegalArgumentException("L'indirizzo del terminal non può essere vuoto");
        }
        if (indirizzo.length() > 255) {
            throw new IllegalArgumentException("L'indirizzo non può superare i 255 caratteri");
        }
        this.indirizzo = indirizzo.trim();
    }
    
    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        if (telefono != null && !telefono.trim().isEmpty()) {
            String telefonoNormalizzato = normalizzaTelefono(telefono);
            if (!TELEFONO_PATTERN.matcher(telefonoNormalizzato).matches()) {
                throw new IllegalArgumentException("Formato telefono non valido");
            }
            this.telefono = telefonoNormalizzato;
        } else {
            this.telefono = telefono; // Può essere null/vuoto
        }
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        if (email != null && !email.trim().isEmpty()) {
            String emailNormalizzata = email.trim().toLowerCase();
            if (!EMAIL_PATTERN.matcher(emailNormalizzata).matches()) {
                throw new IllegalArgumentException("Formato email non valido");
            }
            if (emailNormalizzata.length() > 100) {
                throw new IllegalArgumentException("L'email non può superare i 100 caratteri");
            }
            this.email = emailNormalizzata;
        } else {
            this.email = email; // Può essere null/vuoto
        }
    }
    
    // ========== METODI DI UTILITÀ ==========
    
    /**
     * Normalizza il numero di telefono rimuovendo spazi e caratteri speciali
     */
    private String normalizzaTelefono(String telefono) {
        if (telefono == null) return null;
        return telefono.trim().replaceAll("\\s+", " ");
    }
    
    /**
     * Verifica se tutti i campi obbligatori sono compilati
     */
    public boolean isValido() {
        return nome != null && !nome.trim().isEmpty() &&
               indirizzo != null && !indirizzo.trim().isEmpty();
    }
    
    /**
     * Verifica se ha contatti completi
     */
    public boolean hasContattiCompleti() {
        return hasTelefonoValido() && hasEmailValida();
    }
    
    /**
     * Verifica se ha almeno un contatto
     */
    public boolean hasAlmenoUnContatto() {
        return hasTelefonoValido() || hasEmailValida();
    }
    
    /**
     * Verifica se il telefono è valido
     */
    public boolean hasTelefonoValido() {
        return telefono != null && !telefono.trim().isEmpty() &&
               TELEFONO_PATTERN.matcher(telefono).matches();
    }
    
    /**
     * Verifica se l'email è in formato valido
     */
    public boolean hasEmailValida() {
        return email != null && !email.trim().isEmpty() &&
               EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Restituisce una descrizione formattata del terminal
     */
    public String getDescrizioneCompleta() {
        StringBuilder sb = new StringBuilder();
        if (nome != null) {
            sb.append(nome);
        }
        if (indirizzo != null) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(indirizzo);
        }
        return sb.toString();
    }
    
    /**
     * Restituisce descrizione completa con contatti
     */
    public String getDescrizioneConContatti() {
        StringBuilder sb = new StringBuilder(getDescrizioneCompleta());
        
        if (hasTelefonoValido()) {
            sb.append(" | Tel: ").append(telefono);
        }
        if (hasEmailValida()) {
            sb.append(" | Email: ").append(email);
        }
        
        return sb.toString();
    }
    
    /**
     * Verifica se è un nuovo terminal (ID = 0 o non assegnato)
     */
    public boolean isNuovo() {
        return id == 0;
    }
    
    /**
     * Estrae la città dall'indirizzo (assumendo formato standard)
     */
    public String getCitta() {
        if (indirizzo == null || indirizzo.trim().isEmpty()) {
            return "";
        }
        
        // Cerca pattern comuni: "Via..., Città" o "Via... - Città" 
        String[] partiVirgola = indirizzo.split(",");
        if (partiVirgola.length >= 2) {
            String ultimaParte = partiVirgola[partiVirgola.length - 1].trim();
            // Rimuovi eventuali CAP dalla fine
            return ultimaParte.replaceAll("\\d{5}\\s*$", "").trim();
        }
        
        String[] partiTrattino = indirizzo.split("-");
        if (partiTrattino.length >= 2) {
            String ultimaParte = partiTrattino[partiTrattino.length - 1].trim();
            return ultimaParte.replaceAll("\\d{5}\\s*$", "").trim();
        }
        
        return "";
    }
    
    /**
     * Estrae il CAP dall'indirizzo se presente
     */
    public String getCAP() {
        if (indirizzo == null) return "";
        
        // Cerca pattern CAP (5 cifre)
        Pattern capPattern = Pattern.compile("\\b\\d{5}\\b");
        java.util.regex.Matcher matcher = capPattern.matcher(indirizzo);
        
        if (matcher.find()) {
            return matcher.group();
        }
        
        return "";
    }
    
    /**
     * Formatta il telefono per la visualizzazione
     */
    public String getTelefonoFormattato() {
        if (!hasTelefonoValido()) {
            return "N/A";
        }
        return telefono;
    }
    
    /**
     * Restituisce l'email mascherata per privacy
     */
    public String getEmailMascherata() {
        if (!hasEmailValida()) {
            return "N/A";
        }
        
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return email;
        }
        
        return email.charAt(0) + "***" + email.substring(atIndex);
    }
    
    /**
     * Genera un identificativo breve per il terminal (prime lettere nome + ID)
     */
    public String getCodiceBreve() {
        String codice = "";
        if (nome != null && nome.length() > 0) {
            String[] parole = nome.split("\\s+");
            for (String parola : parole) {
                if (parola.length() > 0) {
                    codice += parola.charAt(0);
                }
            }
        }
        return (codice + id).toUpperCase();
    }
    
    /**
     * Verifica se il terminal è nella stessa città di un altro terminal
     */
    public boolean isStessaCitta(Terminal altro) {
        if (altro == null) return false;
        
        String questaCitta = getCitta();
        String altraCitta = altro.getCitta();
        
        return !questaCitta.isEmpty() && 
               !altraCitta.isEmpty() && 
               questaCitta.equalsIgnoreCase(altraCitta);
    }
    
    // ========== METODI STANDARD ==========
    
    @Override
    public String toString() {
        return String.format("Terminal[ID:%d] %s - %s", 
                id, nome != null ? nome : "N/A", 
                indirizzo != null ? indirizzo : "N/A");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Terminal terminal = (Terminal) obj;
        return id == terminal.id && id != 0; // Solo se ID è assegnato
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}