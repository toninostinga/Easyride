package it.easyridedb.dao;

import it.easyridedb.model.Prenotazione;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;


public interface PrenotazioneDao {

    /**
     * Inserisce una nuova prenotazione nel database
     * @param prenotazione La prenotazione da inserire
     * @return true se l'inserimento è andato a buon fine, false altrimenti
     */
    boolean insert(Prenotazione prenotazione);

    /**
     * Trova una prenotazione tramite ID
     * @param id L'ID della prenotazione
     * @return La prenotazione trovata o null se non esiste
     */
    Prenotazione findById(int id);

    /**
     * Recupera tutte le prenotazioni
     * @return Lista di tutte le prenotazioni
     */
    List<Prenotazione> findAll();

    /**
     * Trova tutte le prenotazioni di un utente
     * @param utenteId L'ID dell'utente
     * @return Lista delle prenotazioni dell'utente
     */
    List<Prenotazione> findByUtente(int utenteId);

    /**
     * Trova prenotazioni per stato
     * @param stato Lo stato della prenotazione (confermata, in_corso, completata, annullata)
     * @return Lista delle prenotazioni con lo stato specificato
     */
    List<Prenotazione> findByStato(String stato);

    /**
     * Trova prenotazioni per veicolo
     * @param veicoloTarga La targa del veicolo
     * @return Lista delle prenotazioni per il veicolo specificato
     */
    List<Prenotazione> findByVeicolo(String veicoloTarga);

    /**
     * Trova prenotazioni per terminal di ritiro
     * @param terminalId L'ID del terminal di ritiro
     * @return Lista delle prenotazioni per il terminal specificato
     */
    List<Prenotazione> findByTerminalRitiro(int terminalId);

    /**
     * Trova prenotazioni per terminal di restituzione
     * @param terminalId L'ID del terminal di restituzione
     * @return Lista delle prenotazioni per il terminal specificato
     */
    List<Prenotazione> findByTerminalRestituzione(int terminalId);

    /**
     * Trova prenotazioni in un intervallo di date di ritiro
     * @param dataInizio Data inizio intervallo
     * @param dataFine Data fine intervallo
     * @return Lista delle prenotazioni nell'intervallo specificato
     */
    List<Prenotazione> findByDataRitiro(Date dataInizio, Date dataFine);

    /**
     * Trova prenotazioni in un intervallo di date di restituzione
     * @param dataInizio Data inizio intervallo
     * @param dataFine Data fine intervallo
     * @return Lista delle prenotazioni nell'intervallo specificato
     */
    List<Prenotazione> findByDataRestituzione(Date dataInizio, Date dataFine);

    /**
     * Trova prenotazioni create in un periodo
     * @param dataInizio Data inizio periodo
     * @param dataFine Data fine periodo
     * @return Lista delle prenotazioni create nel periodo
     */
    List<Prenotazione> findByDataPrenotazione(Timestamp dataInizio, Timestamp dataFine);

    /**
     * Trova prenotazioni attive (confermata o in corso)
     * @return Lista delle prenotazioni attive
     */
    List<Prenotazione> findAttive();

    /**
     * Trova prenotazioni scadute (che dovevano essere restituite)
     * @return Lista delle prenotazioni scadute
     */
    List<Prenotazione> findScadute();

    /**
     * Trova prenotazioni prossime al ritiro (nei prossimi X giorni)
     * @param giorni Numero di giorni di anticipo
     * @return Lista delle prenotazioni prossime al ritiro
     */
    List<Prenotazione> findProssimeAlRitiro(int giorni);

    /**
     * Aggiorna i dati di una prenotazione
     * @param prenotazione La prenotazione con i dati aggiornati
     * @return true se l'aggiornamento è andato a buon fine, false altrimenti
     */
    boolean update(Prenotazione prenotazione);

    /**
     * Aggiorna solo lo stato di una prenotazione
     * @param id L'ID della prenotazione
     * @param nuovoStato Il nuovo stato (confermata, in_corso, completata, annullata)
     * @return true se l'aggiornamento è andato a buon fine, false altrimenti
     */
    boolean updateStato(int id, String nuovoStato);

    /**
     * Aggiorna il prezzo totale di una prenotazione
     * @param id L'ID della prenotazione
     * @param prezzoTotale Il nuovo prezzo totale
     * @return true se l'aggiornamento è andato a buon fine, false altrimenti
     */
    boolean updatePrezzoTotale(int id, BigDecimal prezzoTotale);

    /**
     * Elimina una prenotazione tramite ID
     * @param id L'ID della prenotazione da eliminare
     * @return true se l'eliminazione è andata a buon fine, false altrimenti
     */
    boolean delete(int id);

    /**
     * Verifica se un veicolo ha prenotazioni conflittuali in un periodo
     * @param veicoloTarga La targa del veicolo
     * @param dataRitiro Data di ritiro
     * @param dataRestituzione Data di restituzione
     * @return true se ci sono conflitti, false altrimenti
     */
    boolean hasConflittiPrenotazione(String veicoloTarga, Date dataRitiro, Date dataRestituzione);

    /**
     * Verifica se un veicolo ha prenotazioni conflittuali escludendo una prenotazione specifica
     * @param veicoloTarga La targa del veicolo
     * @param dataRitiro Data di ritiro
     * @param dataRestituzione Data di restituzione
     * @param prenotazioneIdEsclusa ID della prenotazione da escludere dal controllo
     * @return true se ci sono conflitti, false altrimenti
     */
    boolean hasConflittiPrenotazione(String veicoloTarga, Date dataRitiro, Date dataRestituzione, int prenotazioneIdEsclusa);

    /**
     * Calcola il fatturato totale in un periodo
     * @param dataInizio Data inizio periodo
     * @param dataFine Data fine periodo
     * @return Fatturato totale nel periodo
     */
    BigDecimal calcolaFatturatoInPeriodo(Date dataInizio, Date dataFine);

    /**
     * Conta il numero di prenotazioni per stato
     * @param stato Lo stato delle prenotazioni da contare
     * @return Numero di prenotazioni con lo stato specificato
     */
    int countByStato(String stato);

    /**
     * Conta il numero totale di prenotazioni
     * @return Numero totale di prenotazioni
     */
    int countAll();

    /**
     * Trova le prenotazioni più recenti di un utente
     * @param utenteId L'ID dell'utente
     * @param limite Numero massimo di prenotazioni da restituire
     * @return Lista delle prenotazioni più recenti dell'utente
     */
    List<Prenotazione> findRecentByUtente(int utenteId, int limite);

    /**
     * Trova i veicoli più prenotati in un periodo
     * @param dataInizio Data inizio periodo
     * @param dataFine Data fine periodo
     * @param limite Numero massimo di risultati
     * @return Lista delle targhe dei veicoli più prenotati
     */
    List<String> findVeicoliPiuPrenotati(Date dataInizio, Date dataFine, int limite);
}