package it.easyridedb.dao;

import it.easyridedb.model.Terminal;
import java.util.List;

/**
 * Interfaccia DAO per la gestione dei terminal
 */
public interface TerminalDao {

    /**
     * Inserisce un nuovo terminal nel database
     * @param terminal Il terminal da inserire
     * @return true se l'inserimento è andato a buon fine, false altrimenti
     */
    boolean insert(Terminal terminal);

    /**
     * Trova un terminal tramite ID
     * @param id L'ID del terminal
     * @return Il terminal trovato o null se non esiste
     */
    Terminal findById(int id);

    /**
     * Recupera tutti i terminal
     * @return Lista di tutti i terminal
     */
    List<Terminal> findAll();

    /**
     * Trova terminal per nome
     * @param nome Il nome del terminal
     * @return Lista dei terminal con il nome specificato
     */
    List<Terminal> findByNome(String nome);

    /**
     * Trova terminal per città (estratta dall'indirizzo)
     * @param citta La città del terminal
     * @return Lista dei terminal nella città specificata
     */
    List<Terminal> findByCitta(String citta);

    /**
     * Trova terminal che contengono una stringa nell'indirizzo
     * @param indirizzo Parte dell'indirizzo da cercare
     * @return Lista dei terminal che contengono la stringa nell'indirizzo
     */
    List<Terminal> findByIndirizzo(String indirizzo);

    /**
     * Aggiorna i dati di un terminal
     * @param terminal Il terminal con i dati aggiornati
     * @return true se l'aggiornamento è andato a buon fine, false altrimenti
     */
    boolean update(Terminal terminal);

    /**
     * Aggiorna solo i contatti di un terminal
     * @param id L'ID del terminal
     * @param telefono Il nuovo numero di telefono
     * @param email La nuova email
     * @return true se l'aggiornamento è andato a buon fine, false altrimenti
     */
    boolean updateContatti(int id, String telefono, String email);

    /**
     * Elimina un terminal tramite ID
     * @param id L'ID del terminal da eliminare
     * @return true se l'eliminazione è andata a buon fine, false altrimenti
     */
    boolean delete(int id);

    /**
     * Verifica se un terminal può essere eliminato (non ha veicoli o prenotazioni associate)
     * @param id L'ID del terminal
     * @return true se può essere eliminato, false altrimenti
     */
    boolean canDelete(int id);

    /**
     * Conta il numero di veicoli in un terminal
     * @param terminalId L'ID del terminal
     * @return Numero di veicoli nel terminal
     */
    int countVeicoliInTerminal(int terminalId);

    /**
     * Conta il numero di veicoli disponibili in un terminal
     * @param terminalId L'ID del terminal
     * @return Numero di veicoli disponibili nel terminal
     */
    int countVeicoliDisponibiliInTerminal(int terminalId);

    /**
     * Conta il numero di prenotazioni attive per ritiro in un terminal
     * @param terminalId L'ID del terminal
     * @return Numero di prenotazioni attive per ritiro
     */
    int countPrenotazioniRitiroInTerminal(int terminalId);

    /**
     * Conta il numero di prenotazioni attive per restituzione in un terminal
     * @param terminalId L'ID del terminal
     * @return Numero di prenotazioni attive per restituzione
     */
    int countPrenotazioniRestituzioneInTerminal(int terminalId);

    /**
     * Trova terminal con veicoli disponibili
     * @return Lista dei terminal che hanno almeno un veicolo disponibile
     */
    List<Terminal> findWithVeicoliDisponibili();

    /**
     * Trova il terminal più vicino a un indirizzo (ricerca semplice per città)
     * @param citta La città di riferimento
     * @return Lista dei terminal nella stessa città
     */
    List<Terminal> findNearby(String citta);

    /**
     * Conta il numero totale di terminal
     * @return Numero totale di terminal
     */
    int countAll();

    /**
     * Verifica se esiste un terminal con lo stesso nome
     * @param nome Il nome da verificare
     * @return true se esiste un terminal con quel nome, false altrimenti
     */
    boolean existsByNome(String nome);

    /**
     * Verifica se esiste un terminal con lo stesso nome escludendo un ID specifico
     * @param nome Il nome da verificare
     * @param excludeId L'ID del terminal da escludere dal controllo
     * @return true se esiste un terminal con quel nome, false altrimenti
     */
    boolean existsByNome(String nome, int excludeId);

    /**
     * Trova terminal ordinati per nome
     * @return Lista dei terminal ordinati alfabeticamente per nome
     */
    List<Terminal> findAllOrderByNome();

    /**
     * Trova terminal con contatti completi (telefono e email)
     * @return Lista dei terminal che hanno sia telefono che email
     */
    List<Terminal> findWithContattiCompleti();
}