package it.easyridedb.dao;

import it.easyridedb.model.Utente;
import java.util.List;

/**
 * Interfaccia DAO per la gestione degli utenti
 */
public interface UtenteDao {
    
    /**
     * Inserisce un nuovo utente nel database
     * @param utente L'utente da inserire
     * @return true se l'inserimento è andato a buon fine, false altrimenti
     */
    boolean insert(Utente utente);
    
    /**
     * Trova un utente tramite email
     * @param email L'email dell'utente
     * @return L'utente trovato o null se non esiste
     */
    Utente findByEmail(String email);
    
    /**
     * Trova un utente tramite ID
     * @param id L'ID dell'utente
     * @return L'utente trovato o null se non esiste
     */
    Utente findById(int id);
    
    /**
     * Recupera tutti gli utenti
     * @return Lista di tutti gli utenti
     */
    List<Utente> findAll();
    
    /**
     * Aggiorna i dati di un utente
     * @param utente L'utente con i dati aggiornati
     * @return true se l'aggiornamento è andato a buon fine, false altrimenti
     */
    boolean update(Utente utente);
    
    /**
     * Elimina un utente tramite ID
     * @param id L'ID dell'utente da eliminare
     * @return true se l'eliminazione è andata a buon fine, false altrimenti
     */
    boolean delete(int id);
    
    /**
     * Autentica un utente
     * @param email L'email dell'utente
     * @param password La password dell'utente
     * @return true se le credenziali sono corrette, false altrimenti
     */
    public Utente authenticate(String email, String password);
    
    /**
     * Trova utenti per ruolo
     * @param ruolo Il ruolo da cercare (cliente/admin)
     * @return Lista degli utenti con il ruolo specificato
     */
    List<Utente> findByRuolo(String ruolo);
}
