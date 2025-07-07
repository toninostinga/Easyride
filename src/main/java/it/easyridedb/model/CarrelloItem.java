package it.easyridedb.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class CarrelloItem {
    
    // Identificativo univoco dell'item nel carrello
    private String itemId;
    
    // Dati veicolo
    private String targaVeicolo;
    private String marca;
    private String modello;
    private String tipo;
    private String carburante;
    private String trasmissione;
    private BigDecimal prezzoPerGiorno;
    private String immagineUrl;
    
    // Dati prenotazione
    private LocalDate dataRitiro;
    private LocalDate dataRestituzione;
    private int terminalRitiroId;
    private String nomeTerminalRitiro;
    private int terminalRestituzioneId;
    private String nomeTerminalRestituzione;
    
    // Optional selezionati
    private List<Optional> optionalSelezionati;
    
    // Calcoli prezzi
    private int numeroGiorni;
    private BigDecimal prezzoVeicolo;
    private BigDecimal prezzoOptional;
    private BigDecimal prezzoTotale;
    
    // Costruttori
    public CarrelloItem() {
        this.optionalSelezionati = new ArrayList<>();
        this.prezzoVeicolo = BigDecimal.ZERO;
        this.prezzoOptional = BigDecimal.ZERO;
        this.prezzoTotale = BigDecimal.ZERO;
    }
    
    public CarrelloItem(String targaVeicolo, LocalDate dataRitiro, LocalDate dataRestituzione,
                       int terminalRitiroId, int terminalRestituzioneId) {
        this();
        this.targaVeicolo = targaVeicolo;
        this.dataRitiro = dataRitiro;
        this.dataRestituzione = dataRestituzione;
        this.terminalRitiroId = terminalRitiroId;
        this.terminalRestituzioneId = terminalRestituzioneId;
        this.itemId = generateItemId();
        calcolaNumeroGiorni();
    }
    
    // Metodi di business logic
    
    /**
     * Calcola il numero di giorni di noleggio
     */
    public void calcolaNumeroGiorni() {
        if (dataRitiro != null && dataRestituzione != null) {
            this.numeroGiorni = (int) ChronoUnit.DAYS.between(dataRitiro, dataRestituzione);
            if (numeroGiorni <= 0) {
                numeroGiorni = 1; // Minimo 1 giorno
            }
        }
    }
    
    /**
     * Calcola il prezzo totale dell'item
     */
    public void calcolaPrezzoTotale() {
        calcolaNumeroGiorni();
        
        // Prezzo veicolo
        if (prezzoPerGiorno != null) {
            prezzoVeicolo = prezzoPerGiorno.multiply(new BigDecimal(numeroGiorni));
        } else {
            prezzoVeicolo = BigDecimal.ZERO;
        }
        
        // Prezzo optional
        prezzoOptional = BigDecimal.ZERO;
        if (optionalSelezionati != null) {
            for (Optional opt : optionalSelezionati) {
                prezzoOptional = prezzoOptional.add(
                    opt.getPrezzoExtra().multiply(new BigDecimal(numeroGiorni))
                );
            }
        }
        
        // Totale
        prezzoTotale = prezzoVeicolo.add(prezzoOptional);
    }
    
    /**
     * Aggiunge un optional alla prenotazione
     */
    public void aggiungiOptional(Optional optional) {
        if (optional != null && !contieneTipoOptional(optional.getId())) {
            optionalSelezionati.add(optional);
            calcolaPrezzoTotale();
        }
    }
    
    /**
     * Rimuove un optional dalla prenotazione
     */
    public void rimuoviOptional(int optionalId) {
        optionalSelezionati.removeIf(opt -> opt.getId() == optionalId);
        calcolaPrezzoTotale();
    }
    
    /**
     * Controlla se un optional è già selezionato
     */
    public boolean contieneTipoOptional(int optionalId) {
        return optionalSelezionati.stream()
                .anyMatch(opt -> opt.getId() == optionalId);
    }
    
    /**
     * Genera ID univoco per l'item
     */
    private String generateItemId() {
        return "ITEM_" + System.currentTimeMillis() + "_" + 
               (targaVeicolo != null ? targaVeicolo.hashCode() : 0);
    }
    
    /**
     * Verifica se la prenotazione è valida
     */
    public boolean isValid() {
        return targaVeicolo != null && !targaVeicolo.trim().isEmpty() &&
               dataRitiro != null && dataRestituzione != null &&
               dataRitiro.isBefore(dataRestituzione) &&
               terminalRitiroId > 0 && terminalRestituzioneId > 0 &&
               prezzoPerGiorno != null && prezzoPerGiorno.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Restituisce descrizione del veicolo
     */
    public String getDescrizioneVeicolo() {
        return (marca != null ? marca : "") + " " + 
               (modello != null ? modello : "") + 
               (tipo != null ? " (" + tipo + ")" : "");
    }
    
    /**
     * Restituisce lista ID degli optional selezionati
     */
    public List<Integer> getOptionalIds() {
        return optionalSelezionati.stream()
                .map(Optional::getId)
                .toList();
    }
    
    // Getters e Setters
    
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    
    public String getTargaVeicolo() { return targaVeicolo; }
    public void setTargaVeicolo(String targaVeicolo) { 
        this.targaVeicolo = targaVeicolo; 
        this.itemId = generateItemId();
    }
    
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    
    public String getModello() { return modello; }
    public void setModello(String modello) { this.modello = modello; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public String getCarburante() { return carburante; }
    public void setCarburante(String carburante) { this.carburante = carburante; }
    
    public String getTrasmissione() { return trasmissione; }
    public void setTrasmissione(String trasmissione) { this.trasmissione = trasmissione; }
    
    public BigDecimal getPrezzoPerGiorno() { return prezzoPerGiorno; }
    public void setPrezzoPerGiorno(BigDecimal prezzoPerGiorno) { 
        this.prezzoPerGiorno = prezzoPerGiorno; 
        calcolaPrezzoTotale();
    }
    
    public String getImmagineUrl() { return immagineUrl; }
    public void setImmagineUrl(String immagineUrl) { this.immagineUrl = immagineUrl; }
    
    public LocalDate getDataRitiro() { return dataRitiro; }
    public void setDataRitiro(LocalDate dataRitiro) { 
        this.dataRitiro = dataRitiro; 
        calcolaPrezzoTotale();
    }
    
    public LocalDate getDataRestituzione() { return dataRestituzione; }
    public void setDataRestituzione(LocalDate dataRestituzione) { 
        this.dataRestituzione = dataRestituzione; 
        calcolaPrezzoTotale();
    }
    
    public int getTerminalRitiroId() { return terminalRitiroId; }
    public void setTerminalRitiroId(int terminalRitiroId) { this.terminalRitiroId = terminalRitiroId; }
    
    public String getNomeTerminalRitiro() { return nomeTerminalRitiro; }
    public void setNomeTerminalRitiro(String nomeTerminalRitiro) { this.nomeTerminalRitiro = nomeTerminalRitiro; }
    
    public int getTerminalRestituzioneId() { return terminalRestituzioneId; }
    public void setTerminalRestituzioneId(int terminalRestituzioneId) { this.terminalRestituzioneId = terminalRestituzioneId; }
    
    public String getNomeTerminalRestituzione() { return nomeTerminalRestituzione; }
    public void setNomeTerminalRestituzione(String nomeTerminalRestituzione) { this.nomeTerminalRestituzione = nomeTerminalRestituzione; }
    
    public List<Optional> getOptionalSelezionati() { return optionalSelezionati; }
    public void setOptionalSelezionati(List<Optional> optionalSelezionati) { 
        this.optionalSelezionati = optionalSelezionati != null ? optionalSelezionati : new ArrayList<>(); 
        calcolaPrezzoTotale();
    }
    
    public int getNumeroGiorni() { return numeroGiorni; }
    
    public BigDecimal getPrezzoVeicolo() { return prezzoVeicolo; }
    
    public BigDecimal getPrezzoOptional() { return prezzoOptional; }
    
    public BigDecimal getPrezzoTotale() { return prezzoTotale; }
    
    // Equals e HashCode
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CarrelloItem that = (CarrelloItem) obj;
        return Objects.equals(itemId, that.itemId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(itemId);
    }
    
    @Override
    public String toString() {
        return "CarrelloItem{" +
                "itemId='" + itemId + '\'' +
                ", veicolo=" + getDescrizioneVeicolo() +
                ", dataRitiro=" + dataRitiro +
                ", dataRestituzione=" + dataRestituzione +
                ", giorni=" + numeroGiorni +
                ", prezzoTotale=" + prezzoTotale +
                '}';
    }
}