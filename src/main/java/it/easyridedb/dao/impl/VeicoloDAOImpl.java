package it.easyridedb.dao.impl;

import it.easyridedb.dao.DatabaseConnection;
import it.easyridedb.dao.VeicoloDao;
import it.easyridedb.model.Veicolo;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione DAO per la gestione dei veicoli
 * VERSIONE COMPLETA E CORRETTA per il database esistente
 */
public class VeicoloDAOImpl implements VeicoloDao {
    
    // Usa DatabaseConnection
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }
    
    @Override
    public boolean insert(Veicolo veicolo) {
        if (veicolo == null) {
            return false;
        }
        
        String sql = "INSERT INTO veicoli (targa, marca, modello, tipo, trasmissione, carburante, " +
                     "prezzo_per_giorno, disponibile, immagine_url, terminal_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // ← AGGIUNGI
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, veicolo.getTarga());
                stmt.setString(2, veicolo.getMarca());
                stmt.setString(3, veicolo.getModello());
                stmt.setString(4, veicolo.getTipo());
                stmt.setString(5, veicolo.getTrasmissione());
                stmt.setString(6, veicolo.getCarburante());
                stmt.setBigDecimal(7, veicolo.getPrezzoPerGiorno());
                stmt.setBoolean(8, veicolo.isDisponibile());
                stmt.setString(9, veicolo.getImmagineUrl());
                stmt.setInt(10, veicolo.getTerminalId());
                
                int result = stmt.executeUpdate();
                
                if (result > 0) {
                    conn.commit(); // ← AGGIUNGI
                    System.out.println("✅ Veicolo inserito: " + veicolo.getTarga()); // ← AGGIUNGI
                    return true;
                } else {
                    conn.rollback(); // ← AGGIUNGI
                    return false;
                }
            }
            
        } catch (SQLIntegrityConstraintViolationException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {} // ← AGGIUNGI
            System.err.println("Targa già esistente: " + veicolo.getTarga());
            return false;
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {} // ← AGGIUNGI
            System.err.println("Errore nell'inserimento veicolo: " + e.getMessage());
            e.printStackTrace(); // ← AGGIUNGI per vedere errore completo
            return false;
        } finally {
            if (conn != null) {
                try { 
                    conn.setAutoCommit(true); // ← RIPRISTINA
                    conn.close(); 
                } catch (SQLException e) {}
            }
        }
    }
    @Override
    public Veicolo findByTarga(String targa) {
        if (targa == null || targa.trim().isEmpty()) {
            return null;
        }
        
        String sql = "SELECT * FROM veicoli WHERE targa = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, targa);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractVeicoloFromResultSet(rs);
                }
            }
            return null;
            
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca veicolo per targa: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public List<Veicolo> findAll() {
        String sql = "SELECT * FROM veicoli ORDER BY marca, modello";
        List<Veicolo> veicoli = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                veicoli.add(extractVeicoloFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero di tutti i veicoli: " + e.getMessage());
        }
        
        return veicoli;
    }
    
    @Override
    public List<Veicolo> findByDisponibile() {
        String sql = "SELECT * FROM veicoli WHERE disponibile = true ORDER BY marca, modello";
        List<Veicolo> veicoli = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                veicoli.add(extractVeicoloFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero dei veicoli disponibili: " + e.getMessage());
        }
        
        return veicoli;
    }
    
    @Override
    public List<Veicolo> findByTerminal(int terminalId) {
        if (terminalId <= 0) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT * FROM veicoli WHERE terminal_id = ? ORDER BY marca, modello";
        List<Veicolo> veicoli = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, terminalId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    veicoli.add(extractVeicoloFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero dei veicoli per terminal: " + e.getMessage());
        }
        
        return veicoli;
    }
    
    @Override
    public List<Veicolo> findByMarca(String marca) {
        if (marca == null || marca.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT * FROM veicoli WHERE marca = ? ORDER BY modello";
        List<Veicolo> veicoli = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, marca);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    veicoli.add(extractVeicoloFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero dei veicoli per marca: " + e.getMessage());
        }
        
        return veicoli;
    }
    
    @Override
    public List<Veicolo> findByCarburante(String carburante) {
        if (carburante == null || carburante.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT * FROM veicoli WHERE carburante = ? ORDER BY marca, modello";
        List<Veicolo> veicoli = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, carburante);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    veicoli.add(extractVeicoloFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero dei veicoli per carburante: " + e.getMessage());
        }
        
        return veicoli;
    }
    
    @Override
    public List<Veicolo> findByTrasmissione(String trasmissione) {
        if (trasmissione == null || trasmissione.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT * FROM veicoli WHERE trasmissione = ? ORDER BY marca, modello";
        List<Veicolo> veicoli = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, trasmissione);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    veicoli.add(extractVeicoloFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero dei veicoli per trasmissione: " + e.getMessage());
        }
        
        return veicoli;
    }
    
    @Override
    public List<Veicolo> findByFasciaPrezzo(BigDecimal prezzoMin, BigDecimal prezzoMax) {
        if (prezzoMin == null || prezzoMax == null || prezzoMin.compareTo(prezzoMax) > 0) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT * FROM veicoli WHERE prezzo_per_giorno BETWEEN ? AND ? ORDER BY prezzo_per_giorno";
        List<Veicolo> veicoli = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBigDecimal(1, prezzoMin);
            stmt.setBigDecimal(2, prezzoMax);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    veicoli.add(extractVeicoloFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero dei veicoli per fascia prezzo: " + e.getMessage());
        }
        
        return veicoli;
    }
    
    @Override
    public boolean update(Veicolo veicolo) {
        if (veicolo == null || veicolo.getTarga() == null) {
            return false;
        }
        
        String sql = "UPDATE veicoli SET marca = ?, modello = ?, tipo = ?, trasmissione = ?, " +
                     "carburante = ?, prezzo_per_giorno = ?, disponibile = ?, immagine_url = ?, " +
                     "terminal_id = ? WHERE targa = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, veicolo.getMarca());
            stmt.setString(2, veicolo.getModello());
            stmt.setString(3, veicolo.getTipo());
            stmt.setString(4, veicolo.getTrasmissione());
            stmt.setString(5, veicolo.getCarburante());
            stmt.setBigDecimal(6, veicolo.getPrezzoPerGiorno());
            stmt.setBoolean(7, veicolo.isDisponibile());
            stmt.setString(8, veicolo.getImmagineUrl());
            stmt.setInt(9, veicolo.getTerminalId());
            stmt.setString(10, veicolo.getTarga());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Errore nell'aggiornamento veicolo: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean updateDisponibilita(String targa, boolean disponibile) {
        if (targa == null || targa.trim().isEmpty()) {
            return false;
        }
        
        String sql = "UPDATE veicoli SET disponibile = ? WHERE targa = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBoolean(1, disponibile);
            stmt.setString(2, targa);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Errore nell'aggiornamento disponibilità veicolo: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean updateTerminal(String targa, int terminalId) {
        if (targa == null || targa.trim().isEmpty() || terminalId <= 0) {
            return false;
        }
        
        String sql = "UPDATE veicoli SET terminal_id = ? WHERE targa = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, terminalId);
            stmt.setString(2, targa);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Errore nell'aggiornamento terminal veicolo: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean delete(String targa) {
        if (targa == null || targa.trim().isEmpty()) {
            return false;
        }
        
        String sql = "DELETE FROM veicoli WHERE targa = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, targa);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Errore nell'eliminazione veicolo: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean isDisponibileInPeriodo(String targa, Date dataInizio, Date dataFine) {
        if (targa == null || dataInizio == null || dataFine == null) {
            return false;
        }
        
        // Prima verifica che il veicolo esista ed sia disponibile in generale
        Veicolo veicolo = findByTarga(targa);
        if (veicolo == null || !veicolo.isDisponibile()) {
            return false;
        }
        
        String sql = "SELECT COUNT(*) FROM prenotazioni p " +
                     "WHERE p.veicolo_targa = ? AND p.stato IN ('confermata', 'in_corso') " +
                     "AND NOT (p.data_restituzione <= ? OR p.data_ritiro >= ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, targa);
            stmt.setDate(2, dataInizio);
            stmt.setDate(3, dataFine);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0; // Disponibile se non ci sono conflitti
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel controllo disponibilità periodo: " + e.getMessage());
        }
        
        return false;
    }
    
    @Override
    public List<Veicolo> findDisponibiliInPeriodo(Date dataInizio, Date dataFine) {
        if (dataInizio == null || dataFine == null) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT * FROM veicoli v WHERE v.disponibile = true " +
                     "AND v.targa NOT IN (" +
                     "SELECT p.veicolo_targa FROM prenotazioni p " +
                     "WHERE p.stato IN ('confermata', 'in_corso') " +
                     "AND NOT (p.data_restituzione <= ? OR p.data_ritiro >= ?)" +
                     ") ORDER BY v.marca, v.modello";
        
        List<Veicolo> veicoli = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, dataInizio);
            stmt.setDate(2, dataFine);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    veicoli.add(extractVeicoloFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero veicoli disponibili in periodo: " + e.getMessage());
        }
        
        return veicoli;
    }
    
    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM veicoli";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel conteggio totale veicoli: " + e.getMessage());
        }
        
        return 0;
    }
    
    @Override
    public int countDisponibili() {
        String sql = "SELECT COUNT(*) FROM veicoli WHERE disponibile = true";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel conteggio veicoli disponibili: " + e.getMessage());
        }
        
        return 0;
    }
    
    // ========== METODI EXTRA NECESSARI PER IL CATALOGOSERVLET ==========
    
    /**
     * Trova veicoli per modello - NECESSARIO PER CATALOGOSERVLET
     * @param modello Il modello del veicolo
     * @return Lista dei veicoli del modello specificato
     */
    public List<Veicolo> findByModello(String modello) {
        if (modello == null || modello.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT * FROM veicoli WHERE modello = ? ORDER BY marca";
        List<Veicolo> veicoli = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, modello);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    veicoli.add(extractVeicoloFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero dei veicoli per modello: " + e.getMessage());
        }
        
        return veicoli;
    }
    
    /**
     * Trova veicoli per tipo - NECESSARIO PER CATALOGOSERVLET
     * @param tipo Il tipo di veicolo
     * @return Lista dei veicoli del tipo specificato
     */
    public List<Veicolo> findByTipo(String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT * FROM veicoli WHERE tipo = ? ORDER BY marca, modello";
        List<Veicolo> veicoli = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, tipo);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    veicoli.add(extractVeicoloFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero dei veicoli per tipo: " + e.getMessage());
        }
        
        return veicoli;
    }
    
    /**
     * Trova tutte le marche uniche - NECESSARIO PER CATALOGOSERVLET
     * @return Lista delle marche disponibili
     */
    public List<String> findAllMarche() {
        String sql = "SELECT DISTINCT marca FROM veicoli WHERE marca IS NOT NULL ORDER BY marca";
        List<String> marche = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String marca = rs.getString("marca");
                if (marca != null && !marca.trim().isEmpty()) {
                    marche.add(marca);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero delle marche: " + e.getMessage());
        }
        
        return marche;
    }
    
    /**
     * Trova tutti i tipi unici - NECESSARIO PER CATALOGOSERVLET
     * @return Lista dei tipi disponibili
     */
    public List<String> findAllTipi() {
        String sql = "SELECT DISTINCT tipo FROM veicoli WHERE tipo IS NOT NULL ORDER BY tipo";
        List<String> tipi = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String tipo = rs.getString("tipo");
                if (tipo != null && !tipo.trim().isEmpty()) {
                    tipi.add(tipo);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero dei tipi: " + e.getMessage());
        }
        
        return tipi;
    }
    
    /**
     * Estrae un oggetto Veicolo dal ResultSet
     * Usa SOLO i campi che esistono nel database reale
     */
    private Veicolo extractVeicoloFromResultSet(ResultSet rs) throws SQLException {
        Veicolo veicolo = new Veicolo();
        veicolo.setTarga(rs.getString("targa"));
        veicolo.setMarca(rs.getString("marca"));
        veicolo.setModello(rs.getString("modello"));
        veicolo.setTipo(rs.getString("tipo"));
        veicolo.setTrasmissione(rs.getString("trasmissione"));
        veicolo.setCarburante(rs.getString("carburante"));
        veicolo.setPrezzoPerGiorno(rs.getBigDecimal("prezzo_per_giorno"));
        veicolo.setDisponibile(rs.getBoolean("disponibile"));
        veicolo.setImmagineUrl(rs.getString("immagine_url"));
        veicolo.setTerminalId(rs.getInt("terminal_id"));
        
        return veicolo;
    }
}