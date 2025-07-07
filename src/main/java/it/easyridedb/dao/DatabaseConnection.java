package it.easyridedb.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    // Properties per la configurazione del database
    private static final Properties DB_PROPS = loadDatabaseProperties();

    private static Properties loadDatabaseProperties() {
        Properties props = new Properties();
        try (InputStream input = DatabaseConnection.class
                .getClassLoader()
                .getResourceAsStream("database.properties")) {
            
            if (input == null) {
                System.err.println("❌ File database.properties non trovato!");
                System.err.println("Assicurati che sia in src/main/resources/");
                return props; // Restituisce properties vuoto
            }
            
            props.load(input);
            System.out.println("✅ Configurazione database caricata da database.properties");
            
        } catch (IOException e) {
            System.err.println("❌ Errore nel caricamento di database.properties:");
            e.printStackTrace();
        }
        return props;
    }

    public static Connection getConnection() throws SQLException {
        try {
            // Carica il driver MySQL
            String driver = DB_PROPS.getProperty("db.driver");
            if (driver == null) {
                throw new SQLException("Driver non specificato nel file properties");
            }
            Class.forName(driver);

            // Ottieni i parametri di connessione
            String url = DB_PROPS.getProperty("db.url");
            String username = DB_PROPS.getProperty("db.username");
            String password = DB_PROPS.getProperty("db.password");

            // Verifica che tutti i parametri siano presenti
            if (url == null || username == null || password == null) {
                throw new SQLException("Parametri di connessione mancanti nel file properties");
            }

            // Crea e restituisce la connessione
            return DriverManager.getConnection(url, username, password);

        } catch (ClassNotFoundException e) {
            System.err.println("Driver MySQL non trovato!");
            e.printStackTrace();
            throw new SQLException("Driver non disponibile", e);
        }
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ CONNESSIONE RIUSCITA!");
                System.out.println("Database: " + conn.getCatalog());
                System.out.println("URL: " + DB_PROPS.getProperty("db.url"));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ ERRORE CONNESSIONE:");
            System.err.println("Messaggio: " + e.getMessage());
            System.err.println("Codice errore: " + e.getErrorCode());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Metodo per ottenere una proprietà specifica (utile per debug)
     */
    public static String getProperty(String key) {
        return DB_PROPS.getProperty(key);
    }

    /**
     * Main per testare la connessione
     */
    public static void main(String[] args) {
        System.out.println("🔍 Test connessione database EasyRide...");
        System.out.println("📁 Caricamento configurazione da database.properties...");

        // Verifica che le properties siano caricate
        if (DB_PROPS.isEmpty()) {
            System.out.println("💥 Impossibile caricare la configurazione!");
            System.out.println("Controlla che database.properties sia in src/main/resources/");
            return;
        }

        if (testConnection()) {
            System.out.println("🎉 Database pronto per l'uso!");
        } else {
            System.out.println("💥 Problema con la connessione. Controlla:");
            System.out.println("1. MySQL Server è avviato?");
            System.out.println("2. Database 'easyridedb' esiste?");
            System.out.println("3. Username/password corretti in database.properties?");
            System.out.println("4. Porta 3306 libera?");
        }
    }
}