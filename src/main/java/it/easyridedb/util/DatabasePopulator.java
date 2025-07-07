package it.easyridedb.util;

import it.easyridedb.dao.impl.VeicoloDAOImpl;
import it.easyridedb.model.Veicolo;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class DatabasePopulator {
    
    private VeicoloDAOImpl veicoloDAO;
    
    public DatabasePopulator() {
        this.veicoloDAO = new VeicoloDAOImpl();
    }
    
    /**
     * Popola il database con dati di test
     */
    public void populateDatabase() {
        System.out.println("🚀 Avvio popolamento database...");
        
        // Cancella dati esistenti (opzionale)
        System.out.println("📊 Veicoli attuali nel database: " + veicoloDAO.countAll());
        
        List<Veicolo> veicoli = createSampleVehicles();
        
        int inserted = 0;
        int skipped = 0;
        
        for (Veicolo veicolo : veicoli) {
            try {
                if (veicoloDAO.insert(veicolo)) {
                    inserted++;
                    System.out.println("✅ Inserito: " + veicolo.getNomeCompleto() + " (" + veicolo.getTarga() + ")");
                } else {
                    skipped++;
                    System.out.println("⚠️ Saltato (già esistente): " + veicolo.getTarga());
                }
            } catch (Exception e) {
                skipped++;
                System.err.println("❌ Errore inserimento " + veicolo.getTarga() + ": " + e.getMessage());
            }
        }
        
        System.out.println("\n📈 RIEPILOGO POPOLAMENTO:");
        System.out.println("✅ Veicoli inseriti: " + inserted);
        System.out.println("⚠️ Veicoli saltati: " + skipped);
        System.out.println("📊 Totale nel database: " + veicoloDAO.countAll());
        System.out.println("🚗 Veicoli disponibili: " + veicoloDAO.countDisponibili());
        
        if (inserted > 0) {
            System.out.println("\n🎉 Database popolato con successo!");
            System.out.println("💡 Ora puoi testare il catalogo su: http://localhost:8080/EasyRide/catalogo");
        }
    }
    
    /**
     * Crea una lista di veicoli di esempio
     */
    private List<Veicolo> createSampleVehicles() {
        List<Veicolo> veicoli = new ArrayList<>();
        
        // === CITY CAR ===
        veicoli.add(new Veicolo("AB123CD", "Fiat", "Panda", "city", "manuale", "benzina", 
                               new BigDecimal("25.00"), true, "fiat-panda.jpg", 1));
        
        veicoli.add(new Veicolo("EF456GH", "Volkswagen", "Up!", "city", "manuale", "benzina", 
                               new BigDecimal("28.00"), true, "volkswagen-up.jpg", 1));
        
        veicoli.add(new Veicolo("IJ789KL", "Smart", "ForTwo", "city", "automatica", "benzina", 
                               new BigDecimal("30.00"), true, "smart-fortwo.jpg", 2));
        
        veicoli.add(new Veicolo("MN012OP", "Toyota", "Aygo", "city", "manuale", "benzina", 
                               new BigDecimal("26.00"), true, "toyota-aygo.jpg", 1));
        
        // === BERLINE ===
        veicoli.add(new Veicolo("QR345ST", "Volkswagen", "Golf", "berlina", "manuale", "diesel", 
                               new BigDecimal("45.00"), true, "volkswagen-golf.jpg", 1));
        
        veicoli.add(new Veicolo("UV678WX", "Audi", "A3", "berlina", "automatica", "diesel", 
                               new BigDecimal("55.00"), true, "audi-a3.jpg", 2));
        
        veicoli.add(new Veicolo("YZ901AB", "BMW", "Serie 3", "berlina", "automatica", "diesel", 
                               new BigDecimal("65.00"), true, "bmw-serie3.jpg", 3));
        
        veicoli.add(new Veicolo("CD234EF", "Mercedes", "Classe A", "berlina", "automatica", "benzina", 
                               new BigDecimal("60.00"), true, "mercedes-classea.jpg", 2));
        
        // === SUV ===
        veicoli.add(new Veicolo("GH567IJ", "Nissan", "Qashqai", "suv", "automatica", "benzina", 
                               new BigDecimal("50.00"), true, "nissan-qashqai.jpg", 1));
        
        veicoli.add(new Veicolo("KL890MN", "BMW", "X3", "suv", "automatica", "diesel", 
                               new BigDecimal("75.00"), true, "bmw-x3.jpg", 2));
        
        veicoli.add(new Veicolo("OP123QR", "Audi", "Q5", "suv", "automatica", "diesel", 
                               new BigDecimal("80.00"), true, "audi-q5.jpg", 3));
        
        veicoli.add(new Veicolo("ST456UV", "Volvo", "XC60", "suv", "automatica", "diesel", 
                               new BigDecimal("70.00"), true, "volvo-xc60.jpg", 1));
        
        // === ELETTRICHE ===
        veicoli.add(new Veicolo("WX789YZ", "Tesla", "Model 3", "berlina", "automatica", "elettrico", 
                               new BigDecimal("85.00"), true, "tesla-model3.jpg", 2));
        
        veicoli.add(new Veicolo("AB012CD", "Nissan", "Leaf", "city", "automatica", "elettrico", 
                               new BigDecimal("40.00"), true, "nissan-leaf.jpg", 1));
        
        veicoli.add(new Veicolo("EF345GH", "BMW", "i3", "city", "automatica", "elettrico", 
                               new BigDecimal("50.00"), true, "bmw-i3.jpg", 3));
        
        // === IBRIDE ===
        veicoli.add(new Veicolo("IJ678KL", "Toyota", "Prius", "berlina", "automatica", "ibrido", 
                               new BigDecimal("42.00"), true, "toyota-prius.jpg", 1));
        
        veicoli.add(new Veicolo("MN901OP", "Honda", "Insight", "berlina", "automatica", "ibrido", 
                               new BigDecimal("38.00"), true, "honda-insight.jpg", 2));
        
        // === STATION WAGON ===
        veicoli.add(new Veicolo("QR234ST", "Volkswagen", "Passat Variant", "station-wagon", "automatica", "diesel", 
                               new BigDecimal("55.00"), true, "volkswagen-passat.jpg", 1));
        
        veicoli.add(new Veicolo("UV567WX", "Audi", "A4 Avant", "station-wagon", "automatica", "diesel", 
                               new BigDecimal("65.00"), true, "audi-a4avant.jpg", 2));
        
        // === VEICOLI NON DISPONIBILI (per test) ===
        veicoli.add(new Veicolo("YZ890AB", "Ferrari", "488", "sportiva", "automatica", "benzina", 
                               new BigDecimal("200.00"), false, "ferrari-488.jpg", 3));
        
        veicoli.add(new Veicolo("CD123EF", "Lamborghini", "Huracán", "sportiva", "automatica", "benzina", 
                               new BigDecimal("300.00"), false, "lamborghini-huracan.jpg", 3));
        
        return veicoli;
    }
    
    /**
     * Verifica lo stato del database
     */
    public void checkDatabaseStatus() {
        System.out.println("\n📊 STATO DATABASE:");
        System.out.println("Totale veicoli: " + veicoloDAO.countAll());
        System.out.println("Veicoli disponibili: " + veicoloDAO.countDisponibili());
        
        System.out.println("\n🏷️ MARCHE DISPONIBILI:");
        List<String> marche = veicoloDAO.findAllMarche();
        for (String marca : marche) {
            int count = veicoloDAO.findByMarca(marca).size();
            System.out.println("- " + marca + ": " + count + " veicoli");
        }
        
        System.out.println("\n🚗 TIPI DISPONIBILI:");
        List<String> tipi = veicoloDAO.findAllTipi();
        for (String tipo : tipi) {
            int count = veicoloDAO.findByTipo(tipo).size();
            System.out.println("- " + tipo + ": " + count + " veicoli");
        }
        
        System.out.println("\n⚡ CARBURANTI:");
        String[] carburanti = {"benzina", "diesel", "elettrico", "ibrido"};
        for (String carburante : carburanti) {
            int count = veicoloDAO.findByCarburante(carburante).size();
            System.out.println("- " + carburante + ": " + count + " veicoli");
        }
    }
    
    /**
     * Cancella tutti i dati (usare con cautela!)
     */
    public void clearDatabase() {
        System.out.println("⚠️ ATTENZIONE: Cancellazione di tutti i veicoli...");
        
        List<Veicolo> tutti = veicoloDAO.findAll();
        int deleted = 0;
        
        for (Veicolo veicolo : tutti) {
            if (veicoloDAO.delete(veicolo.getTarga())) {
                deleted++;
            }
        }
        
        System.out.println("🗑️ Cancellati " + deleted + " veicoli");
        System.out.println("📊 Veicoli rimanenti: " + veicoloDAO.countAll());
    }
    
    /**
     * Main per eseguire il popolamento
     */
    public static void main(String[] args) {
        DatabasePopulator populator = new DatabasePopulator();
        
        System.out.println("🚀 EasyRide Database Populator");
        System.out.println("================================");
        
        // Stato iniziale
        populator.checkDatabaseStatus();
        
        System.out.println("\n" + "=".repeat(50));
        
        // Popolamento
        populator.populateDatabase();
        
        System.out.println("\n" + "=".repeat(50));
        
        // Stato finale
        populator.checkDatabaseStatus();
        
        System.out.println("\n✨ Popolamento completato!");
        System.out.println("💡 Ora testa il catalogo: http://localhost:8080/EasyRide/catalogo");
    }
}