package it.easyridedb.dao;

import it.easyridedb.model.Optional;
import java.math.BigDecimal;
import java.util.List;


public interface OptionalDao {

    /**
     * Inserisce un nuovo optional nel database
     * @param optional L'optional da inserire
     * @return true se l'inserimento è andato a buon fine, false altrimenti
     */
    boolean insert(Optional optional);

    /**
     * Trova un optional tramite ID
     * @param id L'ID dell'optional
     * @return L'optional trovato o null se non esiste
     */
    Optional findById(int id);

    /**
     * Recupera tutti gli optional
     * @return Lista di tutti gli optional
     */
    List<Optional> findAll();

    /**
     * Trova optional per nome
     * @param nome Il nome dell'optional
     * @return Lista degli optional con il nome specificato
     */
    List<Optional> findByNome(String nome);

    /**
     * Trova optional che contengono una stringa nel nome
     * @param nomePattern Parte del nome da cercare
     * @return Lista degli optional che contengono la stringa nel nome
     */
    List<Optional> findByNomeLike(String nomePattern);

    /**
     * Trova optional gratuiti
     * @return Lista degli optional con prezzo = 0
     */
    List<Optional> findGratuiti();

    /**
     * Trova optional in una fascia di prezzo
     * @param prezzoMin Prezzo minimo
     * @param prezzoMax Prezzo massimo
     * @return Lista degli optional nella fascia di prezzo
     */
    List<Optional> findByFasciaPrezzo(BigDecimal prezzoMin, BigDecimal prezzoMax);

    /**
     * Trova optional economici (prezzo sotto una soglia)
     * @param soglia Soglia di prezzo
     * @return Lista degli optional sotto la soglia
     */
    List<Optional> findEconomici(BigDecimal soglia);

    /**
     * Trova optional costosi (prezzo sopra una soglia)
     * @param soglia Soglia di prezzo
     * @return Lista degli optional sopra la soglia
     */
    List<Optional> findCostosi(BigDecimal soglia);

    /**
     * Trova optional per categoria (basato su parole chiave nel nome)
     * @param categoria La categoria da cercare (es. "gps", "assicurazione")
     * @return Lista degli optional della categoria
     */
    List<Optional> findByCategoria(String categoria);

    /**
     * Trova optional popolari (basato su categorie predefinite)
     * @return Lista degli optional popolari (GPS, assicurazioni, seggiolini)
     */
    List<Optional> findPopolari();

    /**
     * Aggiorna i dati di un optional
     * @param optional L'optional con i dati aggiornati
     * @return true se l'aggiornamento è andato a buon fine, false altrimenti
     */
    boolean update(Optional optional);

    /**
     * Aggiorna solo il prezzo di un optional
     * @param id L'ID dell'optional
     * @param nuovoPrezzo Il nuovo prezzo
     * @return true se l'aggiornamento è andato a buon fine, false altrimenti
     */
    boolean updatePrezzo(int id, BigDecimal nuovoPrezzo);

    /**
     * Aggiorna solo la descrizione di un optional
     * @param id L'ID dell'optional
     * @param nuovaDescrizione La nuova descrizione
     * @return true se l'aggiornamento è andato a buon fine, false altrimenti
     */
    boolean updateDescrizione(int id, String nuovaDescrizione);

    /**
     * Elimina un optional tramite ID
     * @param id L'ID dell'optional da eliminare
     * @return true se l'eliminazione è andata a buon fine, false altrimenti
     */
    boolean delete(int id);

    /**
     * Verifica se un optional può essere eliminato (non è associato a prenotazioni)
     * @param id L'ID dell'optional
     * @return true se può essere eliminato, false altrimenti
     */
    boolean canDelete(int id);

    /**
     * Trova optional associati a un veicolo
     * @param veicoloTarga La targa del veicolo
     * @return Lista degli optional disponibili per il veicolo
     */
    List<Optional> findByVeicolo(String veicoloTarga);

    /**
     * Trova optional associati a una prenotazione
     * @param prenotazioneId L'ID della prenotazione
     * @return Lista degli optional selezionati per la prenotazione
     */
    List<Optional> findByPrenotazione(int prenotazioneId);

    /**
     * Associa un optional a un veicolo
     * @param veicoloTarga La targa del veicolo
     * @param optionalId L'ID dell'optional
     * @return true se l'associazione è andata a buon fine, false altrimenti
     */
    boolean associaAVeicolo(String veicoloTarga, int optionalId);

    /**
     * Rimuove l'associazione tra un optional e un veicolo
     * @param veicoloTarga La targa del veicolo
     * @param optionalId L'ID dell'optional
     * @return true se la rimozione è andata a buon fine, false altrimenti
     */
    boolean rimuoviDaVeicolo(String veicoloTarga, int optionalId);

    /**
     * Associa un optional a una prenotazione
     * @param prenotazioneId L'ID della prenotazione
     * @param optionalId L'ID dell'optional
     * @return true se l'associazione è andata a buon fine, false altrimenti
     */
    boolean associaAPrenotazione(int prenotazioneId, int optionalId);

    /**
     * Rimuove l'associazione tra un optional e una prenotazione
     * @param prenotazioneId L'ID della prenotazione
     * @param optionalId L'ID dell'optional
     * @return true se la rimozione è andata a buon fine, false altrimenti
     */
    boolean rimuoviDaPrenotazione(int prenotazioneId, int optionalId);

    /**
     * Conta quante volte un optional è stato scelto nelle prenotazioni
     * @param optionalId L'ID dell'optional
     * @return Numero di prenotazioni che hanno scelto questo optional
     */
    int countUtilizzi(int optionalId);

    /**
     * Trova gli optional più richiesti
     * @param limite Numero massimo di risultati
     * @return Lista degli optional più richiesti
     */
    List<Optional> findPiuRichiesti(int limite);

    /**
     * Calcola il ricavo totale generato da un optional
     * @param optionalId L'ID dell'optional
     * @return Ricavo totale dell'optional
     */
    BigDecimal calcolaRicavoTotale(int optionalId);

    /**
     * Conta il numero totale di optional
     * @return Numero totale di optional
     */
    int countAll();

    /**
     * Verifica se esiste un optional con lo stesso nome
     * @param nome Il nome da verificare
     * @return true se esiste un optional con quel nome, false altrimenti
     */
    boolean existsByNome(String nome);

    /**
     * Verifica se esiste un optional con lo stesso nome escludendo un ID specifico
     * @param nome Il nome da verificare
     * @param excludeId L'ID dell'optional da escludere dal controllo
     * @return true se esiste un optional con quel nome, false altrimenti
     */
    boolean existsByNome(String nome, int excludeId);

    /**
     * Trova optional ordinati per prezzo
     * @param crescente true per ordine crescente, false per decrescente
     * @return Lista degli optional ordinati per prezzo
     */
    List<Optional> findAllOrderByPrezzo(boolean crescente);

    /**
     * Trova optional ordinati per nome
     * @return Lista degli optional ordinati alfabeticamente per nome
     */
    List<Optional> findAllOrderByNome();
}