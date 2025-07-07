package it.easyridedb.dao.impl;

import it.easyridedb.dao.DatabaseConnection;
import it.easyridedb.dao.OptionalDao;
import it.easyridedb.model.Optional;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OptionalDAOImpl implements OptionalDao {
    
    // Usa DatabaseConnection
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }
    
    @Override
    public boolean insert(Optional optional) {
        if (optional == null) {
            return false;
        }
        
        String sql = "INSERT INTO optional (nome, descrizione, prezzo_extra) VALUES (?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, optional.getNome());
            stmt.setString(2, optional.getDescrizione());
            stmt.setBigDecimal(3, optional.getPrezzoExtra());
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        optional.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
            
        } catch (SQLException e) {
            System.err.println("Errore nell'inserimento optional: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public Optional findById(int id) {
        if (id <= 0) {
            return null;
        }
        
        String sql = "SELECT * FROM optional WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractOptionalFromResultSet(rs);
                }
            }
            return null;
            
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca optional per ID: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public List<Optional> findAll() {
        String sql = "SELECT * FROM optional ORDER BY nome";
        List<Optional> optionals = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                optionals.add(extractOptionalFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero di tutti gli optional: " + e.getMessage());
        }
        
        return optionals;
    }
    
    @Override
    public List<Optional> findByNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT * FROM optional WHERE nome = ? ORDER BY prezzo_extra";
        List<Optional> optionals = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nome);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    optionals.add(extractOptionalFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca optional per nome: " + e.getMessage());
        }
        
        return optionals;
    }
    
    @Override
    public List<Optional> findByNomeLike(String nomePattern) {
        if (nomePattern == null || nomePattern.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT * FROM optional WHERE nome LIKE ? ORDER BY nome";
        List<Optional> optionals = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + nomePattern + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    optionals.add(extractOptionalFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca optional per pattern nome: " + e.getMessage());
        }
        
        return optionals;
    }
    
    @Override
    public List<Optional> findGratuiti() {
        String sql = "SELECT * FROM optional WHERE prezzo_extra = 0 ORDER BY nome";
        List<Optional> optionals = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                optionals.add(extractOptionalFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero optional gratuiti: " + e.getMessage());
        }
        
        return optionals;
    }
    
    @Override
    public List<Optional> findByFasciaPrezzo(BigDecimal prezzoMin, BigDecimal prezzoMax) {
        if (prezzoMin == null || prezzoMax == null || prezzoMin.compareTo(prezzoMax) > 0) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT * FROM optional WHERE prezzo_extra BETWEEN ? AND ? ORDER BY prezzo_extra";
        List<Optional> optionals = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBigDecimal(1, prezzoMin);
            stmt.setBigDecimal(2, prezzoMax);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    optionals.add(extractOptionalFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca optional per fascia prezzo: " + e.getMessage());
        }
        
        return optionals;
    }
    
    @Override
    public List<Optional> findEconomici(BigDecimal soglia) {
        if (soglia == null) {
            soglia = new BigDecimal("10.00"); // Default
        }
        
        String sql = "SELECT * FROM optional WHERE prezzo_extra < ? ORDER BY prezzo_extra";
        List<Optional> optionals = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBigDecimal(1, soglia);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    optionals.add(extractOptionalFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca optional economici: " + e.getMessage());
        }
        
        return optionals;
    }
    
    @Override
    public List<Optional> findCostosi(BigDecimal soglia) {
        if (soglia == null) {
            soglia = new BigDecimal("50.00"); // Default
        }
        
        String sql = "SELECT * FROM optional WHERE prezzo_extra > ? ORDER BY prezzo_extra DESC";
        List<Optional> optionals = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBigDecimal(1, soglia);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    optionals.add(extractOptionalFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca optional costosi: " + e.getMessage());
        }
        
        return optionals;
    }
    
    @Override
    public List<Optional> findByCategoria(String categoria) {
        if (categoria == null || categoria.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT * FROM optional WHERE nome LIKE ? ORDER BY nome";
        List<Optional> optionals = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + categoria + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    optionals.add(extractOptionalFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca optional per categoria: " + e.getMessage());
        }
        
        return optionals;
    }
    
    @Override
    public List<Optional> findPopolari() {
        // Cerca optional con parole chiave popolari
        String sql = "SELECT * FROM optional WHERE " +
                     "nome LIKE '%gps%' OR nome LIKE '%navigatore%' OR " +
                     "nome LIKE '%assicurazione%' OR nome LIKE '%seggiolino%' " +
                     "ORDER BY nome";
        List<Optional> optionals = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                optionals.add(extractOptionalFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero optional popolari: " + e.getMessage());
        }
        
        return optionals;
    }
    
    @Override
    public boolean update(Optional optional) {
        if (optional == null || optional.getId() <= 0) {
            return false;
        }
        
        String sql = "UPDATE optional SET nome = ?, descrizione = ?, prezzo_extra = ? WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, optional.getNome());
            stmt.setString(2, optional.getDescrizione());
            stmt.setBigDecimal(3, optional.getPrezzoExtra());
            stmt.setInt(4, optional.getId());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Errore nell'aggiornamento optional: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean updatePrezzo(int id, BigDecimal nuovoPrezzo) {
        if (id <= 0 || nuovoPrezzo == null) {
            return false;
        }
        
        String sql = "UPDATE optional SET prezzo_extra = ? WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBigDecimal(1, nuovoPrezzo);
            stmt.setInt(2, id);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Errore nell'aggiornamento prezzo optional: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean updateDescrizione(int id, String nuovaDescrizione) {
        if (id <= 0) {
            return false;
        }
        
        String sql = "UPDATE optional SET descrizione = ? WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nuovaDescrizione);
            stmt.setInt(2, id);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Errore nell'aggiornamento descrizione optional: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean delete(int id) {
        if (id <= 0) {
            return false;
        }
        
        String sql = "DELETE FROM optional WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Errore nell'eliminazione optional: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean canDelete(int id) {
        if (id <= 0) {
            return false;
        }
        
        // Verifica se ci sono associazioni con veicoli o prenotazioni
        String sqlVeicoli = "SELECT COUNT(*) FROM veicoli_optional WHERE optional_id = ?";
        String sqlPrenotazioni = "SELECT COUNT(*) FROM prenotazioni_optional WHERE optional_id = ?";
        
        try (Connection conn = getConnection()) {
            
            // Controlla associazioni con veicoli
            try (PreparedStatement stmt = conn.prepareStatement(sqlVeicoli)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return false;
                    }
                }
            }
            
            // Controlla associazioni con prenotazioni
            try (PreparedStatement stmt = conn.prepareStatement(sqlPrenotazioni)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return false;
                    }
                }
            }
            
            return true;
            
        } catch (SQLException e) {
            System.err.println("Errore nel controllo eliminabilità optional: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public List<Optional> findByVeicolo(String veicoloTarga) {
        if (veicoloTarga == null || veicoloTarga.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT o.* FROM optional o " +
                     "INNER JOIN veicoli_optional vo ON o.id = vo.optional_id " +
                     "WHERE vo.veicolo_targa = ? ORDER BY o.nome";
        List<Optional> optionals = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, veicoloTarga);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    optionals.add(extractOptionalFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero optional per veicolo: " + e.getMessage());
        }
        
        return optionals;
    }
    
    @Override
    public List<Optional> findByPrenotazione(int prenotazioneId) {
        if (prenotazioneId <= 0) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT o.* FROM optional o " +
                     "INNER JOIN prenotazioni_optional po ON o.id = po.optional_id " +
                     "WHERE po.prenotazioni_id = ? ORDER BY o.nome";
        List<Optional> optionals = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, prenotazioneId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    optionals.add(extractOptionalFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero optional per prenotazione: " + e.getMessage());
        }
        
        return optionals;
    }
    
    @Override
    public boolean associaAVeicolo(String veicoloTarga, int optionalId) {
        if (veicoloTarga == null || veicoloTarga.trim().isEmpty() || optionalId <= 0) {
            return false;
        }
        
        String sql = "INSERT INTO veicoli_optional (veicolo_targa, optional_id) VALUES (?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, veicoloTarga);
            stmt.setInt(2, optionalId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Errore nell'associazione optional a veicolo: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean rimuoviDaVeicolo(String veicoloTarga, int optionalId) {
        if (veicoloTarga == null || veicoloTarga.trim().isEmpty() || optionalId <= 0) {
            return false;
        }
        
        String sql = "DELETE FROM veicoli_optional WHERE veicolo_targa = ? AND optional_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, veicoloTarga);
            stmt.setInt(2, optionalId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Errore nella rimozione optional da veicolo: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean associaAPrenotazione(int prenotazioneId, int optionalId) {
        if (prenotazioneId <= 0 || optionalId <= 0) {
            return false;
        }
        
        String sql = "INSERT INTO prenotazioni_optional (prenotazioni_id, optional_id) VALUES (?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, prenotazioneId);
            stmt.setInt(2, optionalId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Errore nell'associazione optional a prenotazione: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean rimuoviDaPrenotazione(int prenotazioneId, int optionalId) {
        if (prenotazioneId <= 0 || optionalId <= 0) {
            return false;
        }
        
        String sql = "DELETE FROM prenotazioni_optional WHERE prenotazioni_id = ? AND optional_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, prenotazioneId);
            stmt.setInt(2, optionalId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Errore nella rimozione optional da prenotazione: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public int countUtilizzi(int optionalId) {
        if (optionalId <= 0) {
            return 0;
        }
        
        String sql = "SELECT COUNT(*) FROM prenotazioni_optional WHERE optional_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, optionalId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel conteggio utilizzi optional: " + e.getMessage());
        }
        
        return 0;
    }
    
    @Override
    public List<Optional> findPiuRichiesti(int limite) {
        if (limite <= 0) {
            return new ArrayList<>();
        }
        
        String sql = "SELECT o.*, COUNT(po.optional_id) as utilizzi " +
                     "FROM optional o " +
                     "LEFT JOIN prenotazioni_optional po ON o.id = po.optional_id " +
                     "GROUP BY o.id " +
                     "ORDER BY utilizzi DESC, o.nome " +
                     "LIMIT ?";
        List<Optional> optionals = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limite);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    optionals.add(extractOptionalFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero optional più richiesti: " + e.getMessage());
        }
        
        return optionals;
    }
    
    @Override
    public BigDecimal calcolaRicavoTotale(int optionalId) {
        if (optionalId <= 0) {
            return BigDecimal.ZERO;
        }
        
        String sql = "SELECT SUM(o.prezzo_extra) " +
                     "FROM optional o " +
                     "INNER JOIN prenotazioni_optional po ON o.id = po.optional_id " +
                     "INNER JOIN prenotazioni p ON po.prenotazioni_id = p.id " +
                     "WHERE o.id = ? AND p.stato = 'completata'";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, optionalId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal ricavo = rs.getBigDecimal(1);
                    return ricavo != null ? ricavo : BigDecimal.ZERO;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel calcolo ricavo optional: " + e.getMessage());
        }
        
        return BigDecimal.ZERO;
    }
    
    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM optional";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel conteggio totale optional: " + e.getMessage());
        }
        
        return 0;
    }
    
    @Override
    public boolean existsByNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return false;
        }
        
        String sql = "SELECT COUNT(*) FROM optional WHERE nome = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nome);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nella verifica esistenza nome optional: " + e.getMessage());
        }
        
        return false;
    }
    
    @Override
    public boolean existsByNome(String nome, int excludeId) {
        if (nome == null || nome.trim().isEmpty()) {
            return false;
        }
        
        String sql = "SELECT COUNT(*) FROM optional WHERE nome = ? AND id != ?";
        
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
            System.err.println("Errore nella verifica esistenza nome optional: " + e.getMessage());
        }
        
        return false;
    }
    
    @Override
    public List<Optional> findAllOrderByPrezzo(boolean crescente) {
        String order = crescente ? "ASC" : "DESC";
        String sql = "SELECT * FROM optional ORDER BY prezzo_extra " + order + ", nome";
        List<Optional> optionals = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                optionals.add(extractOptionalFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Errore nel recupero optional ordinati per prezzo: " + e.getMessage());
        }
        
        return optionals;
    }
    
    @Override
    public List<Optional> findAllOrderByNome() {
        return findAll(); // Già ordinato per nome
    }
    
    
    private Optional extractOptionalFromResultSet(ResultSet rs) throws SQLException {
        Optional optional = new Optional();
        optional.setId(rs.getInt("id"));
        optional.setNome(rs.getString("nome"));
        optional.setDescrizione(rs.getString("descrizione"));
        optional.setPrezzoExtra(rs.getBigDecimal("prezzo_extra"));
        return optional;
    }
}