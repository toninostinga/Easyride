package it.easyridedb.util;

import it.easyridedb.dao.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseTester {
    
    public static void main(String[] args) {
        System.out.println("🔍 DatabaseTester - Debug problema popolamento");
        System.out.println("================================================");
        
        testConnection();
        checkTableStructure();
        checkTableData();
        testDirectInsert();
    }
    
    private static void testConnection() {
        System.out.println("\n1️⃣ Test connessione base:");
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("✅ Connessione OK");
            System.out.println("   Database: " + conn.getCatalog());
            System.out.println("   URL: " + DatabaseConnection.getProperty("db.url"));
        } catch (Exception e) {
            System.out.println("❌ Errore connessione: " + e.getMessage());
        }
    }
    
    private static void checkTableStructure() {
        System.out.println("\n2️⃣ Controllo struttura tabella veicoli:");
        
        String sql = "DESCRIBE veicoli";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            System.out.println("📋 Struttura tabella veicoli:");
            while (rs.next()) {
                String field = rs.getString("Field");
                String type = rs.getString("Type");
                String nullable = rs.getString("Null");
                String key = rs.getString("Key");
                System.out.printf("   %-20s %-20s %-5s %-5s%n", field, type, nullable, key);
            }
            
        } catch (SQLException e) {
            System.out.println("❌ Errore controllo struttura: " + e.getMessage());
            System.out.println("💡 La tabella 'veicoli' esiste?");
        }
    }
    
    private static void checkTableData() {
        System.out.println("\n3️⃣ Controllo dati esistenti:");
        
        // Count totale
        String countSql = "SELECT COUNT(*) as totale FROM veicoli";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(countSql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                int totale = rs.getInt("totale");
                System.out.println("📊 Totale veicoli nel database: " + totale);
            }
            
        } catch (SQLException e) {
            System.out.println("❌ Errore count: " + e.getMessage());
        }
        
        // Prime 5 righe
        String selectSql = "SELECT targa, marca, modello, disponibile FROM veicoli LIMIT 5";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectSql);
             ResultSet rs = stmt.executeQuery()) {
            
            System.out.println("🚗 Prime 5 righe:");
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                System.out.printf("   %-10s %-10s %-15s %s%n", 
                    rs.getString("targa"),
                    rs.getString("marca"), 
                    rs.getString("modello"),
                    rs.getBoolean("disponibile") ? "✅" : "❌"
                );
            }
            
            if (!hasData) {
                System.out.println("   (Nessun dato trovato)");
            }
            
        } catch (SQLException e) {
            System.out.println("❌ Errore select: " + e.getMessage());
        }
    }
    
    private static void testDirectInsert() {
        System.out.println("\n4️⃣ Test inserimento diretto:");
        
        String testTarga = "TEST001";
        
        // Prima cancella se esiste
        String deleteSql = "DELETE FROM veicoli WHERE targa = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            
            stmt.setString(1, testTarga);
            int deleted = stmt.executeUpdate();
            System.out.println("🗑️ Righe cancellate con targa " + testTarga + ": " + deleted);
            
        } catch (SQLException e) {
            System.out.println("⚠️ Errore cancellazione test: " + e.getMessage());
        }
        
        // Poi inserisci
        String insertSql = "INSERT INTO veicoli (targa, marca, modello, tipo, trasmissione, carburante, prezzo_per_giorno, disponibile, terminal_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            
            stmt.setString(1, testTarga);
            stmt.setString(2, "TestMarca");
            stmt.setString(3, "TestModello");
            stmt.setString(4, "test");
            stmt.setString(5, "manuale");
            stmt.setString(6, "benzina");
            stmt.setBigDecimal(7, new java.math.BigDecimal("99.99"));
            stmt.setBoolean(8, true);
            stmt.setInt(9, 1);
            
            int inserted = stmt.executeUpdate();
            System.out.println("➕ Righe inserite: " + inserted);
            
            if (inserted > 0) {
                System.out.println("✅ Inserimento diretto funziona!");
                
                // Verifica che sia stato inserito
                String verifySql = "SELECT COUNT(*) FROM veicoli WHERE targa = ?";
                try (PreparedStatement verifyStmt = conn.prepareStatement(verifySql)) {
                    verifyStmt.setString(1, testTarga);
                    try (ResultSet rs = verifyStmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            System.out.println("✅ Verifica: veicolo test trovato nel database");
                        } else {
                            System.out.println("❌ Verifica: veicolo test NON trovato!");
                        }
                    }
                }
                
                // Pulisci
                try (PreparedStatement cleanStmt = conn.prepareStatement(deleteSql)) {
                    cleanStmt.setString(1, testTarga);
                    cleanStmt.executeUpdate();
                    System.out.println("🧹 Veicolo test rimosso");
                }
            }
            
        } catch (SQLException e) {
            System.out.println("❌ Errore inserimento test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}