package it.easyridedb.dao.impl;

import it.easyridedb.dao.DatabaseConnection;
import it.easyridedb.dao.TerminalDao;
import it.easyridedb.model.Terminal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TerminalDAOImpl implements TerminalDao {
    
    // Usa DatabaseConnection
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }
    
    @Override
    public boolean insert(Terminal terminal) {
        if (terminal == null) {
            return false;
        }
        
        String sql = "INSERT INTO terminal (nome, indirizzo, telefono, email) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, terminal.getNome());
            stmt.setString(2, terminal.getIndirizzo());
            stmt.setString(3, terminal.getTelefono());
            stmt.setString(4, terminal.getEmail());
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        terminal.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
            
        } catch (SQLException e) {
            System.err.println("Errore nell'inserimento terminal: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public Terminal findById(int id) {
        if (id <= 0) {
            return null;
        }
        
        String sql = "SELECT * FROM terminal WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractTerminalFromResultSet(rs);
                }
            }
            return null;
            
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca terminal per ID: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public List<Terminal> findAll() {
        String sql = "SELECT * FROM terminal ORDER BY nome";
        List<Terminal> terminals = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                terminals.add(extractTerminalFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero di tutti i terminal: " + e.getMessage());
        }
        
        return terminals;
    }
    
    @Override
    public List<Terminal> findByNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT * FROM terminal WHERE nome LIKE ? ORDER BY nome";
        List<Terminal> terminals = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + nome + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    terminals.add(extractTerminalFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca terminal per nome: " + e.getMessage());
        }
        
        return terminals;
    }
    
    @Override
    public List<Terminal> findByCitta(String citta) {
        if (citta == null || citta.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT * FROM terminal WHERE indirizzo LIKE ? ORDER BY nome";
        List<Terminal> terminals = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + citta + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    terminals.add(extractTerminalFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca terminal per città: " + e.getMessage());
        }
        
        return terminals;
    }
    
    @Override
    public List<Terminal> findByIndirizzo(String indirizzo) {
        if (indirizzo == null || indirizzo.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT * FROM terminal WHERE indirizzo LIKE ? ORDER BY nome";
        List<Terminal> terminals = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + indirizzo + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    terminals.add(extractTerminalFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca terminal per indirizzo: " + e.getMessage());
        }
        
        return terminals;
    }
    
    @Override
    public boolean update(Terminal terminal) {
        if (terminal == null || terminal.getId() <= 0) {
            return false;
        }
        
        String sql = "UPDATE terminal SET nome = ?, indirizzo = ?, telefono = ?, email = ? WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, terminal.getNome());
            stmt.setString(2, terminal.getIndirizzo());
            stmt.setString(3, terminal.getTelefono());
            stmt.setString(4, terminal.getEmail());
            stmt.setInt(5, terminal.getId());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Errore nell'aggiornamento terminal: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean updateContatti(int id, String telefono, String email) {
        if (id <= 0) {
            return false;
        }
        
        String sql = "UPDATE terminal SET telefono = ?, email = ? WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, telefono);
            stmt.setString(2, email);
            stmt.setInt(3, id);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Errore nell'aggiornamento contatti terminal: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean delete(int id) {
        if (id <= 0) {
            return false;
        }
        
        String sql = "DELETE FROM terminal WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Errore nell'eliminazione terminal: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean canDelete(int id) {
        if (id <= 0) {
            return false;
        }
        
        // Verifica se ci sono veicoli associati
        String sqlVeicoli = "SELECT COUNT(*) FROM veicoli WHERE terminal_id = ?";
        
        // Verifica se ci sono prenotazioni associate (ritiro o restituzione)
        String sqlPrenotazioni = "SELECT COUNT(*) FROM prenotazioni WHERE terminal_ritiro_id = ? OR terminal_restituzione_id = ?";
        
        try (Connection conn = getConnection()) {
            
            // Controlla veicoli
            try (PreparedStatement stmt = conn.prepareStatement(sqlVeicoli)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return false; // Ci sono veicoli associati
                    }
                }
            }
            
            // Controlla prenotazioni
            try (PreparedStatement stmt = conn.prepareStatement(sqlPrenotazioni)) {
                stmt.setInt(1, id);
                stmt.setInt(2, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return false; // Ci sono prenotazioni associate
                    }
                }
            }
            
            return true; // Può essere eliminato
            
        } catch (SQLException e) {
            System.err.println("Errore nel controllo eliminabilità terminal: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public int countVeicoliInTerminal(int terminalId) {
        if (terminalId <= 0) {
            return 0;
        }
        
        String sql = "SELECT COUNT(*) FROM veicoli WHERE terminal_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, terminalId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel conteggio veicoli in terminal: " + e.getMessage());
        }
        
        return 0;
    }
    
    @Override
    public int countVeicoliDisponibiliInTerminal(int terminalId) {
        if (terminalId <= 0) {
            return 0;
        }
        
        String sql = "SELECT COUNT(*) FROM veicoli WHERE terminal_id = ? AND disponibile = true";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, terminalId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel conteggio veicoli disponibili in terminal: " + e.getMessage());
        }
        
        return 0;
    }
    
    @Override
    public int countPrenotazioniRitiroInTerminal(int terminalId) {
        if (terminalId <= 0) {
            return 0;
        }
        
        String sql = "SELECT COUNT(*) FROM prenotazioni WHERE terminal_ritiro_id = ? AND stato IN ('confermata', 'in_corso')";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, terminalId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel conteggio prenotazioni ritiro in terminal: " + e.getMessage());
        }
        
        return 0;
    }
    
    @Override
    public int countPrenotazioniRestituzioneInTerminal(int terminalId) {
        if (terminalId <= 0) {
            return 0;
        }
        
        String sql = "SELECT COUNT(*) FROM prenotazioni WHERE terminal_restituzione_id = ? AND stato IN ('confermata', 'in_corso')";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, terminalId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel conteggio prenotazioni restituzione in terminal: " + e.getMessage());
        }
        
        return 0;
    }
    
    @Override
    public List<Terminal> findWithVeicoliDisponibili() {
        String sql = "SELECT DISTINCT t.* FROM terminal t " +
                     "INNER JOIN veicoli v ON t.id = v.terminal_id " +
                     "WHERE v.disponibile = true ORDER BY t.nome";
        List<Terminal> terminals = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                terminals.add(extractTerminalFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero terminal con veicoli disponibili: " + e.getMessage());
        }
        
        return terminals;
    }
    
    @Override
    public List<Terminal> findNearby(String citta) {
        // Per semplicità, cerca nella stessa città
        return findByCitta(citta);
    }
    
    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM terminal";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel conteggio totale terminal: " + e.getMessage());
        }
        
        return 0;
    }
    
    @Override
    public boolean existsByNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return false;
        }
        
        String sql = "SELECT COUNT(*) FROM terminal WHERE nome = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nome);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nella verifica esistenza nome terminal: " + e.getMessage());
        }
        
        return false;
    }
    
    @Override
    public boolean existsByNome(String nome, int excludeId) {
        if (nome == null || nome.trim().isEmpty()) {
            return false;
        }
        
        String sql = "SELECT COUNT(*) FROM terminal WHERE nome = ? AND id != ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nome);
            stmt.setInt(2, excludeId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nella verifica esistenza nome terminal: " + e.getMessage());
        }
        
        return false;
    }
    
    @Override
    public List<Terminal> findAllOrderByNome() {
        return findAll(); // Già ordinato per nome
    }
    
    @Override
    public List<Terminal> findWithContattiCompleti() {
        String sql = "SELECT * FROM terminal WHERE telefono IS NOT NULL AND telefono != '' " +
                     "AND email IS NOT NULL AND email != '' ORDER BY nome";
        List<Terminal> terminals = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                terminals.add(extractTerminalFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero terminal con contatti completi: " + e.getMessage());
        }
        
        return terminals;
    }
    
    /**
     * Estrae un oggetto Terminal dal ResultSet
     */
    private Terminal extractTerminalFromResultSet(ResultSet rs) throws SQLException {
        Terminal terminal = new Terminal();
        terminal.setId(rs.getInt("id"));
        terminal.setNome(rs.getString("nome"));
        terminal.setIndirizzo(rs.getString("indirizzo"));
        terminal.setTelefono(rs.getString("telefono"));
        terminal.setEmail(rs.getString("email"));
        return terminal;
    }
}