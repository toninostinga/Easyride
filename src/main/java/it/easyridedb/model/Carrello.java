package it.easyridedb.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class Carrello {
    
    private List<CarrelloItem> items;
    private BigDecimal totaleCarrello;
    private int numeroItemTotali;
    private int numeroGiorniTotali;
    private LocalDateTime ultimaModifica;
    
    // Costruttore
    public Carrello() {
        this.items = new ArrayList<>();
        this.totaleCarrello = BigDecimal.ZERO;
        this.numeroItemTotali = 0;
        this.numeroGiorniTotali = 0;
        this.ultimaModifica = LocalDateTime.now();
    }
    
   
    public boolean aggiungiItem(CarrelloItem item) {
        if (item == null || !item.isValid()) {
            return false;
        }
        
        // Controlla conflitti di date per lo stesso veicolo
        if (hasConflittoDate(item)) {
            return false;
        }
        
        // Aggiunge l'item
        items.add(item);
        ricalcolaTotali();
        aggiornaUltimaModifica();
        
        System.out.println("📦 Aggiunto al carrello: " + item.getDescrizioneVeicolo() + 
                          " dal " + item.getDataRitiro() + " al " + item.getDataRestituzione());
        return true;
    }
    
   
    public boolean rimuoviItem(String itemId) {
        if (itemId == null) return false;
        
        boolean removed = items.removeIf(item -> itemId.equals(item.getItemId()));
        
        if (removed) {
            ricalcolaTotali();
            aggiornaUltimaModifica();
            System.out.println("🗑️ Rimosso dal carrello item: " + itemId);
        }
        
        return removed;
    }
    
  
    public boolean rimuoviItemPerVeicolo(String targa, LocalDate dataRitiro, LocalDate dataRestituzione) {
        if (targa == null || dataRitiro == null || dataRestituzione == null) return false;
        
        boolean removed = items.removeIf(item -> 
            targa.equals(item.getTargaVeicolo()) &&
            dataRitiro.equals(item.getDataRitiro()) &&
            dataRestituzione.equals(item.getDataRestituzione())
        );
        
        if (removed) {
            ricalcolaTotali();
            aggiornaUltimaModifica();
            System.out.println("🗑️ Rimossa prenotazione: " + targa + " dal " + dataRitiro + " al " + dataRestituzione);
        }
        
        return removed;
    }
    
    
    public boolean aggiornaItem(String itemId, CarrelloItem nuovoItem) {
        if (itemId == null || nuovoItem == null || !nuovoItem.isValid()) {
            return false;
        }
        
        for (int i = 0; i < items.size(); i++) {
            if (itemId.equals(items.get(i).getItemId())) {
                items.set(i, nuovoItem);
                ricalcolaTotali();
                aggiornaUltimaModifica();
                System.out.println("✏️ Aggiornato item carrello: " + itemId);
                return true;
            }
        }
        
        return false;
    }
    
    
    public void svuotaCarrello() {
        items.clear();
        ricalcolaTotali();
        aggiornaUltimaModifica();
        System.out.println("🧹 Carrello svuotato");
    }
    
   
    public CarrelloItem trovaItemPerId(String itemId) {
        return items.stream()
                .filter(item -> itemId.equals(item.getItemId()))
                .findFirst()
                .orElse(null);
    }
    
    
    public boolean contieneVeicolo(String targa, LocalDate dataRitiro, LocalDate dataRestituzione) {
        return items.stream().anyMatch(item ->
            targa.equals(item.getTargaVeicolo()) &&
            dataRitiro.equals(item.getDataRitiro()) &&
            dataRestituzione.equals(item.getDataRestituzione())
        );
    }
    
   
    private boolean hasConflittoDate(CarrelloItem nuovoItem) {
        return items.stream().anyMatch(existingItem ->
            nuovoItem.getTargaVeicolo().equals(existingItem.getTargaVeicolo()) &&
            dateOverlap(nuovoItem.getDataRitiro(), nuovoItem.getDataRestituzione(),
                       existingItem.getDataRitiro(), existingItem.getDataRestituzione())
        );
    }
    
   
    private boolean dateOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }
    
  
    public void ricalcolaTotali() {
        // Ricalcola prezzo di ogni item
        items.forEach(CarrelloItem::calcolaPrezzoTotale);
        
        // Totale carrello
        totaleCarrello = items.stream()
                .map(CarrelloItem::getPrezzoTotale)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Numero items
        numeroItemTotali = items.size();
        
        // Giorni totali
        numeroGiorniTotali = items.stream()
                .mapToInt(CarrelloItem::getNumeroGiorni)
                .sum();
    }
    
  
    public BigDecimal calcolaSconto() {
        BigDecimal sconto = BigDecimal.ZERO;
        
        // Sconto 5% per prenotazioni di più di 7 giorni totali
        if (numeroGiorniTotali >= 7) {
            sconto = totaleCarrello.multiply(new BigDecimal("0.05"));
        }
        
        // Sconto 3% per più di 2 veicoli
        if (numeroItemTotali >= 3) {
            BigDecimal scontoMultiplo = totaleCarrello.multiply(new BigDecimal("0.03"));
            sconto = sconto.max(scontoMultiplo); // Prende lo sconto maggiore
        }
        
        return sconto;
    }
  
    public BigDecimal getTotaleFinale() {
        BigDecimal sconto = calcolaSconto();
        return totaleCarrello.subtract(sconto);
    }
    
  
    public List<String> getVeicoliNelCarrello() {
        return items.stream()
                .map(item -> item.getMarca() + " " + item.getModello() + " (" + item.getTargaVeicolo() + ")")
                .collect(Collectors.toList());
    }
    
   
    public List<it.easyridedb.model.Optional> getTuttiOptionalSelezionati() {
        return items.stream()
                .flatMap(item -> item.getOptionalSelezionati().stream())
                .distinct()
                .collect(Collectors.toList());
    }
    
   
    private void aggiornaUltimaModifica() {
        this.ultimaModifica = LocalDateTime.now();
    }
    
   
    public boolean isEmpty() {
        return items.isEmpty();
    }
    
  
    public boolean isValidForCheckout() {
        return !isEmpty() && 
               items.stream().allMatch(CarrelloItem::isValid) &&
               totaleCarrello.compareTo(BigDecimal.ZERO) > 0;
    }
    
   
    public String getSummary() {
        if (isEmpty()) {
            return "Carrello vuoto";
        }
        
        return String.format("Carrello: %d prenotazioni, %d giorni totali, €%.2f", 
                           numeroItemTotali, numeroGiorniTotali, totaleCarrello);
    }
    
    // ===== GETTERS =====
    
    public List<CarrelloItem> getItems() {
        return new ArrayList<>(items); // Copia difensiva
    }
    
    public BigDecimal getTotaleCarrello() {
        return totaleCarrello;
    }
    
    public int getNumeroItemTotali() {
        return numeroItemTotali;
    }
    
    public int getNumeroGiorniTotali() {
        return numeroGiorniTotali;
    }
    
    public LocalDateTime getUltimaModifica() {
        return ultimaModifica;
    }
    
    @Override
    public String toString() {
        return "Carrello{" +
                "items=" + numeroItemTotali +
                ", totale=" + totaleCarrello +
                ", giorni=" + numeroGiorniTotali +
                ", ultimaModifica=" + ultimaModifica +
                '}';
    }
}