package it.easyridedb.model;

import java.sql.Timestamp;
import java.util.regex.Pattern;

public class Utente {
    
    // Pattern per validazione email
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    // Attributi corrispondenti ai campi della tabella utenti
    private int id;
    private String nome;
    private String cognome;
    private String email;
    private String passwd;
    private String ruolo; // "cliente" o "admin"
    private Timestamp dataRegistrazione;
    
    // Costruttore vuoto
    public Utente() {
    }
    
    // Costruttore completo
    public Utente(int id, String nome, String cognome, String email,
                  String passwd, String ruolo, Timestamp dataRegistrazione) {
        this.id = id;
        this.nome = nome;
        this.cognome = cognome;
        setEmail(email); // Usa il setter per validazione
        this.passwd = passwd;
        setRuolo(ruolo); // Usa il setter per validazione
        this.dataRegistrazione = dataRegistrazione;
    }
    
    // Costruttore per registrazione (senza id e timestamp)
    public Utente(String nome, String cognome, String email, String passwd, String ruolo) {
        this.nome = nome;
        this.cognome = cognome;
        setEmail(email); // Usa il setter per validazione
        this.passwd = passwd;
        setRuolo(ruolo); // Usa il setter per validazione
    }
    
    // Getter e Setter
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
            throw new IllegalArgumentException("Il nome non può essere vuoto");
        }
        this.nome = nome.trim();
    }
    
    public String getCognome() {
        return cognome;
    }
    
    public void setCognome(String cognome) {
        if (cognome == null || cognome.trim().isEmpty()) {
            throw new IllegalArgumentException("Il cognome non può essere vuoto");
        }
        this.cognome = cognome.trim();
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("L'email non può essere vuota");
        }
        
        String emailTrimmed = email.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(emailTrimmed).matches()) {
            throw new IllegalArgumentException("Formato email non valido");
        }
        
        this.email = emailTrimmed;
    }
    
    public String getPasswd() {
        return passwd;
    }
    
    public void setPasswd(String passwd) {
        if (passwd == null || passwd.length() < 6) {
            throw new IllegalArgumentException("La password deve essere di almeno 6 caratteri");
        }
        this.passwd = passwd;
    }
    
    public String getRuolo() {
        return ruolo;
    }
    
    public void setRuolo(String ruolo) {
        if (ruolo == null || ruolo.trim().isEmpty()) {
            throw new IllegalArgumentException("Il ruolo non può essere vuoto");
        }
        
        String ruoloLower = ruolo.trim().toLowerCase();
        
        // Valida che sia un ruolo valido
        if (!"cliente".equals(ruoloLower) && !"admin".equals(ruoloLower)) {
            throw new IllegalArgumentException("Ruolo non valido. Deve essere 'cliente' o 'admin'");
        }
        
        this.ruolo = ruoloLower;
    }
    
    public Timestamp getDataRegistrazione() {
        return dataRegistrazione;
    }
    
    public void setDataRegistrazione(Timestamp dataRegistrazione) {
        this.dataRegistrazione = dataRegistrazione;
    }
    
    // Metodi di utilità
    public boolean isAdmin() {
        return "admin".equals(this.ruolo);
    }
    
    public boolean isCliente() {
        return "cliente".equals(this.ruolo);
    }
    
    public String getNomeCompleto() {
        return nome + " " + cognome;
    }
    
    /**
     * Restituisce le iniziali dell'utente 
     */
    public String getIniziali() {
        String iniziali = "";
        if (nome != null && !nome.isEmpty()) {
            iniziali += nome.charAt(0);
        }
        if (cognome != null && !cognome.isEmpty()) {
            iniziali += cognome.charAt(0);
        }
        return iniziali.toUpperCase();
    }
    
    /**
     * Verifica se l'utente ha un nome completo valido
     */
    public boolean hasNomeCompleto() {
        return nome != null && !nome.trim().isEmpty() && 
               cognome != null && !cognome.trim().isEmpty();
    }
    
    /**
     * Maschera l'email per la privacy (es. "mario@gmail.com" → "m***@gmail.com")
     */
    public String getEmailMasked() {
        if (email == null || email.length() < 3) {
            return email;
        }
        
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return email;
        }
        
        return email.charAt(0) + "***" + email.substring(atIndex);
    }
    
    /**
     * Restituisce una descrizione user-friendly del ruolo
     */
    public String getRuoloDisplay() {
        if ("admin".equals(ruolo)) {
            return "Amministratore";
        } else if ("cliente".equals(ruolo)) {
            return "Cliente";
        }
        return ruolo;
    }
    
    /**
     * Verifica se la password soddisfa i criteri minimi
     */
    public static boolean isPasswordValid(String password) {
        return password != null && password.length() >= 6;
    }
    
    /**
     * Verifica se l'email ha un formato valido
     */
    public static boolean isEmailValid(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim().toLowerCase()).matches();
    }
    
    // toString per debug (SENZA password per sicurezza)
    @Override
    public String toString() {
        return "Utente{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", cognome='" + cognome + '\'' +
                ", email='" + email + '\'' +
                ", ruolo='" + ruolo + '\'' +
                ", dataRegistrazione=" + dataRegistrazione +
                '}';
    }
    
    // equals e hashCode per confronti
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Utente utente = (Utente) obj;
        return id == utente.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}