package it.easyridedb.model;

import java.math.BigDecimal;
import java.util.Objects;


public class Veicolo {
    
    // Attributi corrispondenti ai campi della tabella veicoli
    private String targa;
    private String marca;
    private String modello;
    private String tipo;
    private String trasmissione; // "manuale" o "automatica"
    private String carburante;   // "benzina", "diesel", "elettrico", "ibrido"
    private BigDecimal prezzoPerGiorno;
    private boolean disponibile;
    private String immagineUrl;
    private int terminalId;
    
    // Costruttore vuoto
    public Veicolo() {
    }
    
    // Costruttore completo
    public Veicolo(String targa, String marca, String modello, String tipo,
                   String trasmissione, String carburante, BigDecimal prezzoPerGiorno,
                   boolean disponibile, String immagineUrl, int terminalId) {
        setTarga(targa);
        setMarca(marca);
        setModello(modello);
        this.tipo = tipo;
        setTrasmissione(trasmissione);
        setCarburante(carburante);
        setPrezzoPerGiorno(prezzoPerGiorno);
        this.disponibile = disponibile;
        this.immagineUrl = immagineUrl;
        this.terminalId = terminalId;
    }
    
    // Costruttore per inserimento (senza disponibilità e terminal)
    public Veicolo(String targa, String marca, String modello, String tipo,
                   String trasmissione, String carburante, BigDecimal prezzoPerGiorno) {
        setTarga(targa);
        setMarca(marca);
        setModello(modello);
        this.tipo = tipo;
        setTrasmissione(trasmissione);
        setCarburante(carburante);
        setPrezzoPerGiorno(prezzoPerGiorno);
        this.disponibile = true; // Default disponibile
    }
    
    // Getter e Setter con validazioni
    public String getTarga() {
        return targa;
    }
    
    public void setTarga(String targa) {
        if (targa == null || targa.trim().isEmpty()) {
            throw new IllegalArgumentException("La targa non può essere vuota");
        }
        this.targa = targa.trim().toUpperCase();
    }
    
    public String getMarca() {
        return marca;
    }
    
    public void setMarca(String marca) {
        if (marca == null || marca.trim().isEmpty()) {
            throw new IllegalArgumentException("La marca non può essere vuota");
        }
        this.marca = marca.trim();
    }
    
    public String getModello() {
        return modello;
    }
    
    public void setModello(String modello) {
        if (modello == null || modello.trim().isEmpty()) {
            throw new IllegalArgumentException("Il modello non può essere vuoto");
        }
        this.modello = modello.trim();
    }
    
    public String getTipo() {
        return tipo;
    }
    
    public void setTipo(String tipo) {
        this.tipo = tipo != null ? tipo.trim() : null;
    }
    
    public String getTrasmissione() {
        return trasmissione;
    }
    
    public void setTrasmissione(String trasmissione) {
        if (trasmissione == null || trasmissione.trim().isEmpty()) {
            throw new IllegalArgumentException("La trasmissione non può essere vuota");
        }
        
        // Validazione semplice senza enum
        String t = trasmissione.trim().toLowerCase();
        if (!t.equals("manuale") && !t.equals("automatica")) {
            throw new IllegalArgumentException("Trasmissione deve essere 'manuale' o 'automatica'");
        }
        
        this.trasmissione = t;
    }
    
    public String getCarburante() {
        return carburante;
    }
    
    public void setCarburante(String carburante) {
        if (carburante == null || carburante.trim().isEmpty()) {
            throw new IllegalArgumentException("Il carburante non può essere vuoto");
        }
        
        // Validazione semplice senza enum
        String c = carburante.trim().toLowerCase();
        if (!c.equals("benzina") && !c.equals("diesel") && 
            !c.equals("elettrico") && !c.equals("ibrido")) {
            throw new IllegalArgumentException("Carburante deve essere 'benzina', 'diesel', 'elettrico' o 'ibrido'");
        }
        
        this.carburante = c;
    }
    
    public BigDecimal getPrezzoPerGiorno() {
        return prezzoPerGiorno;
    }
    
    public void setPrezzoPerGiorno(BigDecimal prezzoPerGiorno) {
        if (prezzoPerGiorno == null || prezzoPerGiorno.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Il prezzo deve essere maggiore di zero");
        }
        this.prezzoPerGiorno = prezzoPerGiorno;
    }
    
    public boolean isDisponibile() {
        return disponibile;
    }
    
    public void setDisponibile(boolean disponibile) {
        this.disponibile = disponibile;
    }
    
    public String getImmagineUrl() {
        return immagineUrl;
    }
    
    public void setImmagineUrl(String immagineUrl) {
        this.immagineUrl = immagineUrl;
    }
    
    public int getTerminalId() {
        return terminalId;
    }
    
    public void setTerminalId(int terminalId) {
        this.terminalId = terminalId;
    }
    
    // Metodi di utilità
    public String getNomeCompleto() {
        return marca + " " + modello;
    }
    
    public boolean isAutomatica() {
        return "automatica".equals(this.trasmissione);
    }
    
    public boolean isManuale() {
        return "manuale".equals(this.trasmissione);
    }
    
    public boolean isElettrico() {
        return "elettrico".equals(this.carburante);
    }
    
    public boolean isIbrido() {
        return "ibrido".equals(this.carburante);
    }
    
    public boolean isEcologico() {
        return isElettrico() || isIbrido();
    }
    
    public String getStatoDisponibilita() {
        return disponibile ? "Disponibile" : "Non disponibile";
    }
    
    public String getDescrizioneCompleta() {
        return String.format("%s %s - %s %s (€%.2f/giorno)", 
                marca, modello, trasmissione, carburante, prezzoPerGiorno);
    }
    
    /**
     * Calcola il prezzo per un numero di giorni specificato
     */
    public BigDecimal calcolaPrezzoTotale(int giorni) {
        if (giorni <= 0) {
            throw new IllegalArgumentException("I giorni devono essere maggiori di zero");
        }
        return prezzoPerGiorno.multiply(BigDecimal.valueOf(giorni));
    }
    
    /**
     * Verifica se il veicolo ha tutti i dati essenziali
     */
    public boolean isCompleto() {
        return targa != null && !targa.isEmpty() &&
               marca != null && !marca.isEmpty() &&
               modello != null && !modello.isEmpty() &&
               trasmissione != null && !trasmissione.isEmpty() &&
               carburante != null && !carburante.isEmpty() &&
               prezzoPerGiorno != null;
    }
    
    /**
     * Restituisce la trasmissione con prima lettera maiuscola per display
     */
    public String getTrasmissioneDisplay() {
        if (trasmissione == null) return "N/A";
        return trasmissione.substring(0, 1).toUpperCase() + trasmissione.substring(1);
    }
    
    /**
     * Restituisce il carburante con prima lettera maiuscola per display
     */
    public String getCarburanteDisplay() {
        if (carburante == null) return "N/A";
        return carburante.substring(0, 1).toUpperCase() + carburante.substring(1);
    }
    
    // toString per debug
    @Override
    public String toString() {
        return "Veicolo{" +
                "targa='" + targa + '\'' +
                ", marca='" + marca + '\'' +
                ", modello='" + modello + '\'' +
                ", tipo='" + tipo + '\'' +
                ", trasmissione='" + trasmissione + '\'' +
                ", carburante='" + carburante + '\'' +
                ", prezzoPerGiorno=" + prezzoPerGiorno +
                ", disponibile=" + disponibile +
                ", immagineUrl='" + immagineUrl + '\'' +
                ", terminalId=" + terminalId +
                '}';
    }
    
    // equals e hashCode basati sulla targa (chiave primaria)
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Veicolo veicolo = (Veicolo) obj;
        return Objects.equals(targa, veicolo.targa);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(targa);
    }
}