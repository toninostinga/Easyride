package it.easyridedb.dao.impl;

import it.easyridedb.dao.DatabaseConnection;
import it.easyridedb.dao.PrenotazioneDao;
import it.easyridedb.model.Prenotazione;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PrenotazioneDAOImpl implements PrenotazioneDao {
    
    // Usa DatabaseConnection
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }
    
    @Override
    public boolean insert(Prenotazione prenotazione) {
        if (prenotazione == null) {
            return false;
        }
        
        String sql = "INSERT INTO prenotazioni (utente_id, veicolo_targa, data_ritiro, data_restituzione, " +
                     "terminal_ritiro_id, terminal_restituzione_id, prezzo_totale, stato) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, prenotazione.getUtenteId());
            stmt.setString(2, prenotazione.getVeicoloTarga());
            stmt.setDate(3, prenotazione.getDataRitiro());
            stmt.setDate(4, prenotazione.getDataRestituzione());
            stmt.setInt(5, prenotazione.getTerminalRitiroId());
            stmt.setInt(6, prenotazione.getTerminalRestituzioneId());
            stmt.setBigDecimal(7, prenotazione.getPrezzoTotale());
            stmt.setString(8, prenotazione.getStato());
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        prenotazione.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
            
        } catch (SQLException e) {
            System.err.println("Errore nell'inserimento prenotazione: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public Prenotazione findById(int id) {
        if (id <= 0) {
            return null;
        }
        
        String sql = "SELECT * FROM prenotazioni WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractPrenotazioneFromResultSet(rs);
                }
            }
            return null;
            
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca prenotazione per ID: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public List<Prenotazione> findAll() {
        String sql = "SELECT * FROM prenotazioni ORDER BY data_prenotazione DESC";
        List<Prenotazione> prenotazioni = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                prenotazioni.add(extractPrenotazioneFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero di tutte le prenotazioni: " + e.getMessage());
        }
        
        return prenotazioni;
    }
    
    @Override
    public List<Prenotazione> findByUtente(int utenteId) {
        if (utenteId <= 0) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT * FROM prenotazioni WHERE utente_id = ? ORDER BY data_prenotazione DESC";
        List<Prenotazione> prenotazioni = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, utenteId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prenotazioni.add(extractPrenotazioneFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero prenotazioni per utente: " + e.getMessage());
        }
        
        return prenotazioni;
    }
    
    @Override
    public List<Prenotazione> findByStato(String stato) {
        if (stato == null || stato.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT * FROM prenotazioni WHERE stato = ? ORDER BY data_prenotazione DESC";
        List<Prenotazione> prenotazioni = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, stato);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prenotazioni.add(extractPrenotazioneFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero prenotazioni per stato: " + e.getMessage());
        }
        
        return prenotazioni;
    }
    
    @Override
    public List<Prenotazione> findByVeicolo(String veicoloTarga) {
        if (veicoloTarga == null || veicoloTarga.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT * FROM prenotazioni WHERE veicolo_targa = ? ORDER BY data_ritiro DESC";
        List<Prenotazione> prenotazioni = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, veicoloTarga);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prenotazioni.add(extractPrenotazioneFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero prenotazioni per veicolo: " + e.getMessage());
        }
        
        return prenotazioni;
    }
    
    @Override
    public List<Prenotazione> findByTerminalRitiro(int terminalId) {
        if (terminalId <= 0) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT * FROM prenotazioni WHERE terminal_ritiro_id = ? ORDER BY data_ritiro";
        List<Prenotazione> prenotazioni = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, terminalId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prenotazioni.add(extractPrenotazioneFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero prenotazioni per terminal ritiro: " + e.getMessage());
        }
        
        return prenotazioni;
    }
    
    @Override
    public List<Prenotazione> findByTerminalRestituzione(int terminalId) {
        if (terminalId <= 0) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT * FROM prenotazioni WHERE terminal_restituzione_id = ? ORDER BY data_restituzione";
        List<Prenotazione> prenotazioni = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, terminalId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prenotazioni.add(extractPrenotazioneFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero prenotazioni per terminal restituzione: " + e.getMessage());
        }
        
        return prenotazioni;
    }
    
    @Override
    public List<Prenotazione> findByDataRitiro(Date dataInizio, Date dataFine) {
        if (dataInizio == null || dataFine == null) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT * FROM prenotazioni WHERE data_ritiro BETWEEN ? AND ? ORDER BY data_ritiro";
        List<Prenotazione> prenotazioni = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, dataInizio);
            stmt.setDate(2, dataFine);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prenotazioni.add(extractPrenotazioneFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero prenotazioni per data ritiro: " + e.getMessage());
        }
        
        return prenotazioni;
    }
    
    @Override
    public List<Prenotazione> findByDataRestituzione(Date dataInizio, Date dataFine) {
        if (dataInizio == null || dataFine == null) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT * FROM prenotazioni WHERE data_restituzione BETWEEN ? AND ? ORDER BY data_restituzione";
        List<Prenotazione> prenotazioni = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, dataInizio);
            stmt.setDate(2, dataFine);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prenotazioni.add(extractPrenotazioneFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero prenotazioni per data restituzione: " + e.getMessage());
        }
        
        return prenotazioni;
    }
    
    @Override
    public List<Prenotazione> findByDataPrenotazione(Timestamp dataInizio, Timestamp dataFine) {
        if (dataInizio == null || dataFine == null) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT * FROM prenotazioni WHERE data_prenotazione BETWEEN ? AND ? ORDER BY data_prenotazione DESC";
        List<Prenotazione> prenotazioni = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, dataInizio);
            stmt.setTimestamp(2, dataFine);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prenotazioni.add(extractPrenotazioneFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero prenotazioni per data prenotazione: " + e.getMessage());
        }
        
        return prenotazioni;
    }
    
    @Override
    public List<Prenotazione> findAttive() {
        String sql = "SELECT * FROM prenotazioni WHERE stato IN ('confermata', 'in_corso') ORDER BY data_ritiro";
        List<Prenotazione> prenotazioni = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                prenotazioni.add(extractPrenotazioneFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero prenotazioni attive: " + e.getMessage());
        }
        
        return prenotazioni;
    }
    
    @Override
    public List<Prenotazione> findScadute() {
        String sql = "SELECT * FROM prenotazioni WHERE data_restituzione < CURDATE() " +
                     "AND stato IN ('confermata', 'in_corso') ORDER BY data_restituzione";
        List<Prenotazione> prenotazioni = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                prenotazioni.add(extractPrenotazioneFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero prenotazioni scadute: " + e.getMessage());
        }
        
        return prenotazioni;
    }
    
    @Override
    public List<Prenotazione> findProssimeAlRitiro(int giorni) {
        if (giorni <= 0) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT * FROM prenotazioni WHERE data_ritiro BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL ? DAY) " +
                     "AND stato = 'confermata' ORDER BY data_ritiro";
        List<Prenotazione> prenotazioni = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, giorni);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prenotazioni.add(extractPrenotazioneFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero prenotazioni prossime al ritiro: " + e.getMessage());
        }
        
        return prenotazioni;
    }
    
    @Override
    public boolean update(Prenotazione prenotazione) {
        if (prenotazione == null || prenotazione.getId() <= 0) {
            return false;
        }
        
        String sql = "UPDATE prenotazioni SET utente_id = ?, veicolo_targa = ?, data_ritiro = ?, " +
                     "data_restituzione = ?, terminal_ritiro_id = ?, terminal_restituzione_id = ?, " +
                     "prezzo_totale = ?, stato = ? WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, prenotazione.getUtenteId());
            stmt.setString(2, prenotazione.getVeicoloTarga());
            stmt.setDate(3, prenotazione.getDataRitiro());
            stmt.setDate(4, prenotazione.getDataRestituzione());
            stmt.setInt(5, prenotazione.getTerminalRitiroId());
            stmt.setInt(6, prenotazione.getTerminalRestituzioneId());
            stmt.setBigDecimal(7, prenotazione.getPrezzoTotale());
            stmt.setString(8, prenotazione.getStato());
            stmt.setInt(9, prenotazione.getId());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Errore nell'aggiornamento prenotazione: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean updateStato(int id, String nuovoStato) {
        if (id <= 0 || nuovoStato == null || nuovoStato.trim().isEmpty()) {
            return false;
        }
        
        String sql = "UPDATE prenotazioni SET stato = ? WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nuovoStato);
            stmt.setInt(2, id);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Errore nell'aggiornamento stato prenotazione: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean updatePrezzoTotale(int id, BigDecimal prezzoTotale) {
        if (id <= 0 || prezzoTotale == null) {
            return false;
        }
        
        String sql = "UPDATE prenotazioni SET prezzo_totale = ? WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBigDecimal(1, prezzoTotale);
            stmt.setInt(2, id);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Errore nell'aggiornamento prezzo prenotazione: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean delete(int id) {
        if (id <= 0) {
            return false;
        }
        
        String sql = "DELETE FROM prenotazioni WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Errore nell'eliminazione prenotazione: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean hasConflittiPrenotazione(String veicoloTarga, Date dataRitiro, Date dataRestituzione) {
        return hasConflittiPrenotazione(veicoloTarga, dataRitiro, dataRestituzione, -1);
    }
    
    @Override
    public boolean hasConflittiPrenotazione(String veicoloTarga, Date dataRitiro, Date dataRestituzione, int prenotazioneIdEsclusa) {
        if (veicoloTarga == null || dataRitiro == null || dataRestituzione == null) {
            return true; // In caso di dubbio, assume conflitto
        }
        
        String sql = "SELECT COUNT(*) FROM prenotazioni " +
                     "WHERE veicolo_targa = ? AND stato IN ('confermata', 'in_corso') " +
                     "AND NOT (data_restituzione <= ? OR data_ritiro >= ?)";
        
        if (prenotazioneIdEsclusa > 0) {
            sql += " AND id != ?";
        }
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, veicoloTarga);
            stmt.setDate(2, dataRitiro);
            stmt.setDate(3, dataRestituzione);
            
            if (prenotazioneIdEsclusa > 0) {
                stmt.setInt(4, prenotazioneIdEsclusa);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; // Conflitto se count > 0
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel controllo conflitti prenotazione: " + e.getMessage());
            return true; // In caso di errore, assume conflitto per sicurezza
        }
        
        return false;
    }
    
    @Override
    public BigDecimal calcolaFatturatoInPeriodo(Date dataInizio, Date dataFine) {
        if (dataInizio == null || dataFine == null) {
            return BigDecimal.ZERO;
        }
        
        String sql = "SELECT SUM(prezzo_totale) FROM prenotazioni " +
                     "WHERE stato = 'completata' AND data_restituzione BETWEEN ? AND ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, dataInizio);
            stmt.setDate(2, dataFine);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal fatturato = rs.getBigDecimal(1);
                    return fatturato != null ? fatturato : BigDecimal.ZERO;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel calcolo fatturato: " + e.getMessage());
        }
        
        return BigDecimal.ZERO;
    }
    
    @Override
    public int countByStato(String stato) {
        if (stato == null || stato.trim().isEmpty()) {
            return 0;
        }
        
        String sql = "SELECT COUNT(*) FROM prenotazioni WHERE stato = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, stato);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel conteggio prenotazioni per stato: " + e.getMessage());
        }
        
        return 0;
    }
    
    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM prenotazioni";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel conteggio totale prenotazioni: " + e.getMessage());
        }
        
        return 0;
    }
    
    @Override
    public List<Prenotazione> findRecentByUtente(int utenteId, int limite) {
        if (utenteId <= 0 || limite <= 0) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT * FROM prenotazioni WHERE utente_id = ? " +
                     "ORDER BY data_prenotazione DESC LIMIT ?";
        List<Prenotazione> prenotazioni = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, utenteId);
            stmt.setInt(2, limite);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prenotazioni.add(extractPrenotazioneFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero prenotazioni recenti utente: " + e.getMessage());
        }
        
        return prenotazioni;
    }
    
    @Override
    public List<String> findVeicoliPiuPrenotati(Date dataInizio, Date dataFine, int limite) {
        if (dataInizio == null || dataFine == null || limite <= 0) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT veicolo_targa, COUNT(*) as prenotazioni_count " +
                     "FROM prenotazioni WHERE data_prenotazione BETWEEN ? AND ? " +
                     "GROUP BY veicolo_targa ORDER BY prenotazioni_count DESC LIMIT ?";
        List<String> targhe = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, dataInizio);
            stmt.setDate(2, dataFine);
            stmt.setInt(3, limite);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    targhe.add(rs.getString("veicolo_targa"));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero veicoli più prenotati: " + e.getMessage());
        }
        
        return targhe;
    }
    
    /**
     * Estrae un oggetto Prenotazione dal ResultSet
     */
    private Prenotazione extractPrenotazioneFromResultSet(ResultSet rs) throws SQLException {
        Prenotazione prenotazione = new Prenotazione();
        prenotazione.setId(rs.getInt("id"));
        prenotazione.setUtenteId(rs.getInt("utente_id"));
        prenotazione.setVeicoloTarga(rs.getString("veicolo_targa"));
        prenotazione.setDataRitiro(rs.getDate("data_ritiro"));
        prenotazione.setDataRestituzione(rs.getDate("data_restituzione"));
        prenotazione.setTerminalRitiroId(rs.getInt("terminal_ritiro_id"));
        prenotazione.setTerminalRestituzioneId(rs.getInt("terminal_restituzione_id"));
        prenotazione.setPrezzoTotale(rs.getBigDecimal("prezzo_totale"));
        prenotazione.setDataPrenotazione(rs.getTimestamp("data_prenotazione"));
        prenotazione.setStato(rs.getString("stato"));  // Usa il setter String che converte in enum
        return prenotazione;
    }
}