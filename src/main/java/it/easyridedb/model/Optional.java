package it.easyridedb.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Optional {
    
    // Enum per le categorie degli optional
    public enum CategoriaOptional {
        NAVIGAZIONE("Navigazione", "🗺️"),
        SICUREZZA_BAMBINI("Sicurezza Bambini", "👶"),
        ASSICURAZIONI("Assicurazioni", "🛡️"),
        CONNETTIVITA("Connettività", "📶"),
        CARBURANTE("Carburante", "⛽"),
        COMFORT("Comfort", "🛋️"),
        ALTRO("Altro", "📦");
        
        private final String nome;
        private final String icona;
        
        CategoriaOptional(String nome, String icona) {
            this.nome = nome;
            this.icona = icona;
        }
        
        public String getNome() { return nome; }
        public String getIcona() { return icona; }
        
        @Override
        public String toString() {
            return icona + " " + nome;
        }
    }
     
    private int id;                     
    private String nome;                
    private String descrizione;        
    private BigDecimal prezzoExtra;     
    
    // ========== COSTRUTTORI ==========
    
   
    public Optional() {
        // Inizializza con valori di default sicuri
        this.prezzoExtra = BigDecimal.ZERO;
    }
    
   
    public Optional(String nome, BigDecimal prezzoExtra) {
        setNome(nome);
        setPrezzoExtra(prezzoExtra);
    }
    
    public Optional(String nome, String descrizione, BigDecimal prezzoExtra) {
        setNome(nome);
        setDescrizione(descrizione);
        setPrezzoExtra(prezzoExtra);
    }
    
   
    public Optional(int id, String nome, String descrizione, BigDecimal prezzoExtra) {
        this.id = id;
        setNome(nome);
        setDescrizione(descrizione);
        setPrezzoExtra(prezzoExtra);
    }
    
    
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
            throw new IllegalArgumentException("Il nome dell'optional non può essere vuoto");
        }
        if (nome.length() > 100) {
            throw new IllegalArgumentException("Il nome dell'optional non può superare i 100 caratteri");
        }
        this.nome = nome.trim();
    }
    
    public String getDescrizione() {
        return descrizione;
    }
    
    public void setDescrizione(String descrizione) {
        // La descrizione può essere null/vuota
        this.descrizione = descrizione != null ? descrizione.trim() : null;
    }
    
    public BigDecimal getPrezzoExtra() {
        return prezzoExtra;
    }
    
    public void setPrezzoExtra(BigDecimal prezzoExtra) {
        if (prezzoExtra == null) {
            throw new IllegalArgumentException("Il prezzo extra non può essere nullo");
        }
        if (prezzoExtra.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Il prezzo extra non può essere negativo");
        }
        if (prezzoExtra.scale() > 2) {
            // Arrotonda a 2 decimali
            prezzoExtra = prezzoExtra.setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        this.prezzoExtra = prezzoExtra;
    }
    
    
    public boolean isValido() {
        return nome != null && !nome.trim().isEmpty() &&
               prezzoExtra != null && prezzoExtra.compareTo(BigDecimal.ZERO) >= 0;
    }
    
    
    public boolean isNuovo() {
        return id == 0;
    }
    
    public boolean isGratuito() {
        return prezzoExtra != null && prezzoExtra.compareTo(BigDecimal.ZERO) == 0;
    }
    
  
    public boolean hasDescrizione() {
        return descrizione != null && !descrizione.trim().isEmpty();
    }
    
   
    public boolean isCostoso() {
        return prezzoExtra != null && prezzoExtra.compareTo(new BigDecimal("20.00")) >= 0;
    }
    
   
    public boolean isEconomico() {
        return prezzoExtra != null && prezzoExtra.compareTo(new BigDecimal("5.00")) <= 0;
    }
    
    
    public String getPrezzoFormattato() {
        if (prezzoExtra == null) return "0,00€";
        return String.format("%.2f€", prezzoExtra);
    }
    
    
    public String getPrezzoDisplay() {
        if (isGratuito()) {
            return "Gratuito";
        }
        return "+" + getPrezzoFormattato();
    }
    
    
    public String getPrezzoConLivello() {
        String prezzo = getPrezzoDisplay();
        if (isGratuito()) {
            return prezzo + " 🆓";
        } else if (isEconomico()) {
            return prezzo + " 💚";
        } else if (isCostoso()) {
            return prezzo + " 💰";
        } else {
            return prezzo + " 💛";
        }
    }
    
   
    public BigDecimal calcolaPrezzoPerGiorni(int giorni) {
        if (prezzoExtra == null || giorni <= 0) {
            return BigDecimal.ZERO;
        }
        return prezzoExtra.multiply(BigDecimal.valueOf(giorni));
    }
    
    
    public String calcolaPrezzoPerGiorniFormattato(int giorni) {
        BigDecimal prezzo = calcolaPrezzoPerGiorni(giorni);
        return String.format("%.2f€", prezzo);
    }
    
    
    public String getDescrizioneBreve() {
        if (!hasDescrizione()) {
            return "Nessuna descrizione disponibile";
        }
        
        if (descrizione.length() <= 50) {
            return descrizione;
        }
        
        return descrizione.substring(0, 47) + "...";
    }
    
   
    public CategoriaOptional getCategoriaEnum() {
        if (nome == null) return CategoriaOptional.ALTRO;
        
        String nomeMinuscolo = nome.toLowerCase();
        
        if (nomeMinuscolo.contains("gps") || nomeMinuscolo.contains("navigatore") || 
            nomeMinuscolo.contains("mappa")) {
            return CategoriaOptional.NAVIGAZIONE;
        } else if (nomeMinuscolo.contains("seggiolino") || nomeMinuscolo.contains("bambino") ||
                   nomeMinuscolo.contains("baby") || nomeMinuscolo.contains("child")) {
            return CategoriaOptional.SICUREZZA_BAMBINI;
        } else if (nomeMinuscolo.contains("assicurazione") || nomeMinuscolo.contains("copertura") ||
                   nomeMinuscolo.contains("kasko") || nomeMinuscolo.contains("furto")) {
            return CategoriaOptional.ASSICURAZIONI;
        } else if (nomeMinuscolo.contains("wifi") || nomeMinuscolo.contains("internet") ||
                   nomeMinuscolo.contains("hotspot")) {
            return CategoriaOptional.CONNETTIVITA;
        } else if (nomeMinuscolo.contains("carburante") || nomeMinuscolo.contains("pieno") ||
                   nomeMinuscolo.contains("benzina") || nomeMinuscolo.contains("diesel")) {
            return CategoriaOptional.CARBURANTE;
        } else if (nomeMinuscolo.contains("catene") || nomeMinuscolo.contains("comfort") ||
                   nomeMinuscolo.contains("riscaldamento") || nomeMinuscolo.contains("aria")) {
            return CategoriaOptional.COMFORT;
        } else {
            return CategoriaOptional.ALTRO;
        }
    }
    
   
    public String getCategoria() {
        return getCategoriaEnum().getNome();
    }
    
 
    public String getCategoriaConIcona() {
        return getCategoriaEnum().toString();
    }
    
   
    public boolean isPopolare() {
        if (nome == null) return false;
        
        String nomeMinuscolo = nome.toLowerCase();
        return nomeMinuscolo.contains("gps") || 
               nomeMinuscolo.contains("assicurazione") ||
               nomeMinuscolo.contains("seggiolino") ||
               nomeMinuscolo.contains("navigatore") ||
               nomeMinuscolo.contains("kasko");
    }
    
   
    public boolean isEssenziale() {
        CategoriaOptional categoria = getCategoriaEnum();
        return categoria == CategoriaOptional.SICUREZZA_BAMBINI ||
               categoria == CategoriaOptional.ASSICURAZIONI;
    }
    
    
    public String getDescrizioneCompleta() {
        StringBuilder sb = new StringBuilder();
        sb.append(nome);
        
        if (hasDescrizione()) {
            sb.append(" - ").append(descrizione);
        }
        
        sb.append(" (").append(getPrezzoDisplay()).append(")");
        
        return sb.toString();
    }
    
    
    public int compareByPrezzo(Optional altro) {
        if (altro == null) return 1;
        if (this.prezzoExtra == null && altro.prezzoExtra == null) return 0;
        if (this.prezzoExtra == null) return -1;
        if (altro.prezzoExtra == null) return 1;
        return this.prezzoExtra.compareTo(altro.prezzoExtra);
    }
    
   
    public int compareByCategoriaAndPrezzo(Optional altro) {
        if (altro == null) return 1;
        
        // Prima per categoria
        int categoriaComparison = this.getCategoriaEnum().compareTo(altro.getCategoriaEnum());
        if (categoriaComparison != 0) {
            return categoriaComparison;
        }
        
        // Poi per prezzo
        return this.compareByPrezzo(altro);
    }
    
    // ========== METODI STANDARD ==========
    
    @Override
    public String toString() {
        return String.format("Optional[ID:%d] %s - %s [%s]", 
                id, 
                nome != null ? nome : "N/A", 
                getPrezzoDisplay(),
                getCategoriaConIcona());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Optional optional = (Optional) obj;
        return id == optional.id && id != 0; // Solo se ID è assegnato
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}