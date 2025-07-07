package it.easyridedb.dao;

import it.easyridedb.model.Veicolo;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

/**
 * Interfaccia DAO per la gestione dei veicoli
 */
public interface VeicoloDao {

    /**
     * Inserisce un nuovo veicolo nel database
     * @param veicolo Il veicolo da inserire
     * @return true se l'inserimento è andato a buon fine, false altrimenti
     */
    boolean insert(Veicolo veicolo);

    /**
     * Trova un veicolo tramite targa
     * @param targa La targa del veicolo
     * @return Il veicolo trovato o null se non esiste
     */
    Veicolo findByTarga(String targa);

    /**
     * Recupera tutti i veicoli
     * @return Lista di tutti i veicoli
     */
    List<Veicolo> findAll();

    /**
     * Trova tutti i veicoli disponibili
     * @return Lista dei veicoli disponibili
     */
    List<Veicolo> findByDisponibile();

    /**
     * Trova veicoli per terminal
     * @param terminalId L'ID del terminal
     * @return Lista dei veicoli nel terminal specificato
     */
    List<Veicolo> findByTerminal(int terminalId);

    /**
     * Trova veicoli per marca
     * @param marca La marca del veicolo
     * @return Lista dei veicoli della marca specificata
     */
    List<Veicolo> findByMarca(String marca);

    /**
     * Trova veicoli per tipo di carburante
     * @param carburante Il tipo di carburante
     * @return Lista dei veicoli con il carburante specificato
     */
    List<Veicolo> findByCarburante(String carburante);

    /**
     * Trova veicoli per tipo di trasmissione
     * @param trasmissione Il tipo di trasmissione
     * @return Lista dei veicoli con la trasmissione specificata
     */
    List<Veicolo> findByTrasmissione(String trasmissione);

    /**
     * Trova veicoli in una fascia di prezzo
     * @param prezzoMin Prezzo minimo per giorno
     * @param prezzoMax Prezzo massimo per giorno
     * @return Lista dei veicoli nella fascia di prezzo
     */
    List<Veicolo> findByFasciaPrezzo(BigDecimal prezzoMin, BigDecimal prezzoMax);

    /**
     * Aggiorna i dati di un veicolo
     * @param veicolo Il veicolo con i dati aggiornati
     * @return true se l'aggiornamento è andato a buon fine, false altrimenti
     */
    boolean update(Veicolo veicolo);

    /**
     * Aggiorna solo la disponibilità di un veicolo
     * @param targa La targa del veicolo
     * @param disponibile Nuovo stato di disponibilità
     * @return true se l'aggiornamento è andato a buon fine, false altrimenti
     */
    boolean updateDisponibilita(String targa, boolean disponibile);

    /**
     * Aggiorna il terminal di un veicolo
     * @param targa La targa del veicolo
     * @param terminalId Il nuovo ID del terminal
     * @return true se l'aggiornamento è andato a buon fine, false altrimenti
     */
    boolean updateTerminal(String targa, int terminalId);

    /**
     * Elimina un veicolo tramite targa
     * @param targa La targa del veicolo da eliminare
     * @return true se l'eliminazione è andata a buon fine, false altrimenti
     */
    boolean delete(String targa);

    /**
     * Verifica se un veicolo è disponibile in un periodo specifico
     * @param targa La targa del veicolo
     * @param dataInizio Data inizio periodo
     * @param dataFine Data fine periodo
     * @return true se il veicolo è disponibile nel periodo, false altrimenti
     */
    boolean isDisponibileInPeriodo(String targa, Date dataInizio, Date dataFine);

    /**
     * Trova veicoli disponibili in un periodo specifico
     * @param dataInizio Data inizio periodo
     * @param dataFine Data fine periodo
     * @return Lista dei veicoli disponibili nel periodo
     */
    List<Veicolo> findDisponibiliInPeriodo(Date dataInizio, Date dataFine);

    /**
     * Conta il numero totale di veicoli
     * @return Numero totale di veicoli
     */
    int countAll();

    /**
     * Conta il numero di veicoli disponibili
     * @return Numero di veicoli disponibili
     */
    int countDisponibili();
}