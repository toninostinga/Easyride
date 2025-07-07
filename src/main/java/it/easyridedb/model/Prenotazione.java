package it.easyridedb.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

public class Prenotazione {
    private int id;
    private int utenteId;
    private String veicoloTarga;
    private Date dataRitiro;
    private Date dataRestituzione;
    private int terminalRitiroId;
    private int terminalRestituzioneId;
    private BigDecimal prezzoTotale;
    private Timestamp dataPrenotazione;
    private String stato; // CAMBIATO: String invece di enum
    
    // Oggetti correlati (per facilitare le query)
    private Utente utente;
    private Veicolo veicolo;
    private Terminal terminalRitiro;
    private Terminal terminalRestituzione;
    private List<Optional> optionalSelezionati;
    
    // ===== CAMPI AGGIUNTI PER LA VISUALIZZAZIONE =====
    private String marcaVeicolo;
    private String modelloVeicolo;
    private String tipoVeicolo;
    
    // Costruttori
    public Prenotazione() {
        this.stato = "confermata"; // Default
    }
    
    public Prenotazione(int utenteId, String veicoloTarga, Date dataRitiro, 
                       Date dataRestituzione, int terminalRitiroId, 
                       int terminalRestituzioneId) {
        setUtenteId(utenteId);
        setVeicoloTarga(veicoloTarga);
        setDataRitiro(dataRitiro);
        setDataRestituzione(dataRestituzione);
        setTerminalRitiroId(terminalRitiroId);
        setTerminalRestituzioneId(terminalRestituzioneId);
        this.stato = "confermata";
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getUtenteId() {
        return utenteId;
    }
    
    public void setUtenteId(int utenteId) {
        this.utenteId = utenteId;
    }
    
    public String getVeicoloTarga() {
        return veicoloTarga;
    }
    
    public void setVeicoloTarga(String veicoloTarga) {
        this.veicoloTarga = veicoloTarga;
    }
    
    public Date getDataRitiro() {
        return dataRitiro;
    }
    
    public void setDataRitiro(Date dataRitiro) {
        this.dataRitiro = dataRitiro;
    }
    
    public Date getDataRestituzione() {
        return dataRestituzione;
    }
    
    public void setDataRestituzione(Date dataRestituzione) {
        this.dataRestituzione = dataRestituzione;
    }
    
    public int getTerminalRitiroId() {
        return terminalRitiroId;
    }
    
    public void setTerminalRitiroId(int terminalRitiroId) {
        this.terminalRitiroId = terminalRitiroId;
    }
    
    public int getTerminalRestituzioneId() {
        return terminalRestituzioneId;
    }
    
    public void setTerminalRestituzioneId(int terminalRestituzioneId) {
        this.terminalRestituzioneId = terminalRestituzioneId;
    }
    
    public BigDecimal getPrezzoTotale() {
        return prezzoTotale;
    }
    
    public void setPrezzoTotale(BigDecimal prezzoTotale) {
        this.prezzoTotale = prezzoTotale;
    }
    
    public Timestamp getDataPrenotazione() {
        return dataPrenotazione;
    }
    
    public void setDataPrenotazione(Timestamp dataPrenotazione) {
        this.dataPrenotazione = dataPrenotazione;
    }
    
    
    public String getStato() {
        return stato;
    }
    
    public void setStato(String stato) {
        this.stato = stato;
    }
    
   
    public Utente getUtente() {
        return utente;
    }
    
    public void setUtente(Utente utente) {
        this.utente = utente;
    }
    
    public Veicolo getVeicolo() {
        return veicolo;
    }
    
    public void setVeicolo(Veicolo veicolo) {
        this.veicolo = veicolo;
    }
    
    public Terminal getTerminalRitiro() {
        return terminalRitiro;
    }
    
    public void setTerminalRitiro(Terminal terminalRitiro) {
        this.terminalRitiro = terminalRitiro;
    }
    
    public Terminal getTerminalRestituzione() {
        return terminalRestituzione;
    }
    
    public void setTerminalRestituzione(Terminal terminalRestituzione) {
        this.terminalRestituzione = terminalRestituzione;
    }
    
    public List<Optional> getOptionalSelezionati() {
        return optionalSelezionati;
    }
    
    public void setOptionalSelezionati(List<Optional> optionalSelezionati) {
        this.optionalSelezionati = optionalSelezionati;
    }
    
    
    
    public String getMarcaVeicolo() {
        return marcaVeicolo;
    }
    
    public void setMarcaVeicolo(String marcaVeicolo) {
        this.marcaVeicolo = marcaVeicolo;
    }
    
    public String getModelloVeicolo() {
        return modelloVeicolo;
    }
    
    public void setModelloVeicolo(String modelloVeicolo) {
        this.modelloVeicolo = modelloVeicolo;
    }
    
    public String getTipoVeicolo() {
        return tipoVeicolo;
    }
    
    public void setTipoVeicolo(String tipoVeicolo) {
        this.tipoVeicolo = tipoVeicolo;
    }
    
   
    public String getStatoDescrizione() {
        if (stato == null) return "Sconosciuto";
        
        switch (stato.toLowerCase()) {
            case "confermata":
                return "Confermata";
            case "in_corso":
                return "In corso";
            case "completata":
                return "Completata";
            case "annullata":
                return "Annullata";
            default:
                return stato;
        }
    }
    
    
    public String getNomeCompletoVeicolo() {
        if (marcaVeicolo != null && modelloVeicolo != null) {
            return marcaVeicolo + " " + modelloVeicolo;
        }
        return "Veicolo con targa: " + (veicoloTarga != null ? veicoloTarga : "N/A");
    }
    
    // Metodi di utilità
    public long getGiorniNoleggio() {
        if (dataRitiro != null && dataRestituzione != null) {
            LocalDate ritiro = dataRitiro.toLocalDate();
            LocalDate restituzione = dataRestituzione.toLocalDate();
            return ChronoUnit.DAYS.between(ritiro, restituzione);
        }
        return 0;
    }
    
    public boolean isAttiva() {
        return "confermata".equals(stato) || "in_corso".equals(stato);
    }
    
    public boolean isCompletata() {
        return "completata".equals(stato);
    }
    
    public boolean isAnnullata() {
        return "annullata".equals(stato);
    }
    
    public boolean isStessoTerminal() {
        return terminalRitiroId == terminalRestituzioneId;
    }
    
    public boolean isProssimAlRitiro() {
        if (dataRitiro == null) return false;
        
        LocalDate oggi = LocalDate.now();
        LocalDate ritiro = dataRitiro.toLocalDate();
        long giorniMancanti = ChronoUnit.DAYS.between(oggi, ritiro);
        
        return giorniMancanti >= 0 && giorniMancanti <= 3;
    }
    
    public boolean isScaduta() {
        if (dataRestituzione == null) return false;
        
        LocalDate oggi = LocalDate.now();
        LocalDate restituzione = dataRestituzione.toLocalDate();
        
        return oggi.isAfter(restituzione) && isAttiva();
    }
    
    /**
     * Calcola il prezzo base (senza optional) per la durata del noleggio
     */
    public BigDecimal calcolaPrezzoBase() {
        if (veicolo != null && veicolo.getPrezzoPerGiorno() != null) {
            long giorni = getGiorniNoleggio();
            return veicolo.getPrezzoPerGiorno().multiply(BigDecimal.valueOf(giorni));
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * Calcola il prezzo degli optional selezionati
     */
    public BigDecimal calcolaPrezzoOptional() {
        if (optionalSelezionati == null || optionalSelezionati.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return optionalSelezionati.stream()
                .filter(Objects::nonNull)
                .map(Optional::getPrezzoExtra)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
   
    public String getDescrizioneCompleta() {
        StringBuilder sb = new StringBuilder();
        
        if (veicolo != null) {
            sb.append(veicolo.getMarca()).append(" ").append(veicolo.getModello());
        } else if (marcaVeicolo != null && modelloVeicolo != null) {
            sb.append(marcaVeicolo).append(" ").append(modelloVeicolo);
        } else {
            sb.append("Targa: ").append(veicoloTarga);
        }
        
        sb.append(" dal ").append(dataRitiro)
          .append(" al ").append(dataRestituzione)
          .append(" (").append(getGiorniNoleggio()).append(" giorni)");
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return "Prenotazione{" +
                "id=" + id +
                ", utenteId=" + utenteId +
                ", veicoloTarga='" + veicoloTarga + '\'' +
                ", dataRitiro=" + dataRitiro +
                ", dataRestituzione=" + dataRestituzione +
                ", prezzoTotale=" + prezzoTotale +
                ", stato='" + stato + '\'' +
                ", giorni=" + getGiorniNoleggio() +
                "}";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Prenotazione that = (Prenotazione) obj;
        return id == that.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}