package it.easyridedb.dao.impl;

import it.easyridedb.dao.DatabaseConnection;
import it.easyridedb.dao.UtenteDao;
import it.easyridedb.model.Utente;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtenteDAOImpl implements UtenteDao {
    
    // Usa DatabaseConnection invece di credenziali hardcoded
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }
    
    @Override
    public boolean insert(Utente utente) {
        if (utente == null) {
            return false;
        }
        
        String sql = "INSERT INTO utenti (nome, cognome, email, passwd, ruolo) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, utente.getNome());
            stmt.setString(2, utente.getCognome());
            stmt.setString(3, utente.getEmail());
            stmt.setString(4, utente.getPasswd());
            stmt.setString(5, utente.getRuolo());
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        utente.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
            
        } catch (SQLIntegrityConstraintViolationException e) {
            // Email già esistente
            System.err.println("Email già registrata: " + utente.getEmail());
            return false;
        } catch (SQLException e) {
            System.err.println("Errore nell'inserimento utente: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public Utente findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        
        String sql = "SELECT * FROM utenti WHERE email = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractUtenteFromResultSet(rs);
                }
            }
            return null;
            
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca utente per email: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public Utente findById(int id) {
        if (id <= 0) {
            return null;
        }
        
        String sql = "SELECT * FROM utenti WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractUtenteFromResultSet(rs);
                }
            }
            return null;
            
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca utente per ID: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public List<Utente> findAll() {
        String sql = "SELECT * FROM utenti ORDER BY data_registrazione DESC";
        List<Utente> utenti = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                utenti.add(extractUtenteFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero di tutti gli utenti: " + e.getMessage());
        }
        
        return utenti;
    }
    
    @Override
    public boolean update(Utente utente) {
        if (utente == null || utente.getId() <= 0) {
            return false;
        }
        
        String sql = "UPDATE utenti SET nome = ?, cognome = ?, email = ?, passwd = ?, ruolo = ? WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, utente.getNome());
            stmt.setString(2, utente.getCognome());
            stmt.setString(3, utente.getEmail());
            stmt.setString(4, utente.getPasswd());
            stmt.setString(5, utente.getRuolo());
            stmt.setInt(6, utente.getId());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Errore nell'aggiornamento utente: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean delete(int id) {
        if (id <= 0) {
            return false;
        }
        
        String sql = "DELETE FROM utenti WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Errore nell'eliminazione utente: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public Utente authenticate(String email, String password) {
        if (email == null || email.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            return null;
        }
        
        String sql = "SELECT * FROM utenti WHERE email = ? AND passwd = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            stmt.setString(2, password);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractUtenteFromResultSet(rs);
                }
            }
            return null;
            
        } catch (SQLException e) {
            System.err.println("Errore nell'autenticazione: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public List<Utente> findByRuolo(String ruolo) {
        if (ruolo == null || ruolo.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT * FROM utenti WHERE ruolo = ? ORDER BY data_registrazione DESC";
        List<Utente> utenti = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, ruolo);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    utenti.add(extractUtenteFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca utenti per ruolo: " + e.getMessage());
        }
        
        return utenti;
    }
    
    /**
     * Verifica se un'email è già registrata
     * Metodo utility aggiuntivo per evitare duplicati
     */
    public boolean emailExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        String sql = "SELECT COUNT(*) FROM utenti WHERE email = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nella verifica email: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Estrae un oggetto Utente dal ResultSet
     */
    private Utente extractUtenteFromResultSet(ResultSet rs) throws SQLException {
        Utente utente = new Utente();
        utente.setId(rs.getInt("id"));
        utente.setNome(rs.getString("nome"));
        utente.setCognome(rs.getString("cognome"));
        utente.setEmail(rs.getString("email"));
        utente.setPasswd(rs.getString("passwd"));
        utente.setRuolo(rs.getString("ruolo"));
        utente.setDataRegistrazione(rs.getTimestamp("data_registrazione"));
        return utente;
    }
}