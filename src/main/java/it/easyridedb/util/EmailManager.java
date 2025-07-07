package it.easyridedb.util;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// ✅ TORNA A javax.mail dato che hai quelle librerie
import javax.mail.*;
import javax.mail.internet.*;

import it.easyridedb.model.*;


public class EmailManager {
    
    private static EmailManager instance;
    private Properties emailConfig;
    private ExecutorService emailExecutor;
    private boolean isEnabled = true;
    
    // Singleton pattern
    public static synchronized EmailManager getInstance() {
        if (instance == null) {
            instance = new EmailManager();
        }
        return instance;
    }
    
    private EmailManager() {
        loadConfiguration();
        emailExecutor = Executors.newFixedThreadPool(3); // Max 3 email contemporanee
        testConnection();
    }
    
    private void loadConfiguration() {
        emailConfig = new Properties();
        
        try {
            // ✅ CERCA IL TUO FILE: email.properties (non email-config.properties)
            InputStream input = getClass().getClassLoader().getResourceAsStream("email.properties");
            if (input != null) {
                emailConfig.load(input);
                input.close();
                System.out.println("✅ Configurazione email caricata da email.properties");
                System.out.println("📧 Email configurata: " + emailConfig.getProperty("mail.username"));
                System.out.println("📧 Admin email: " + emailConfig.getProperty("mail.admin.email"));
            } else {
                // Configurazione di default se file non esiste
                loadDefaultConfiguration();
                System.out.println("⚠️ File email.properties non trovato, uso configurazione default");
            }
        } catch (IOException e) {
            System.err.println("❌ Errore caricamento configurazione email: " + e.getMessage());
            loadDefaultConfiguration();
        }
    }
    
    private void loadDefaultConfiguration() {
        // ✅ CONFIGURAZIONE CORRETTA CON LE TUE EMAIL DAL FILE email.properties
        emailConfig.setProperty("mail.smtp.host", "smtp.gmail.com");
        emailConfig.setProperty("mail.smtp.port", "587");
        emailConfig.setProperty("mail.smtp.auth", "true");
        emailConfig.setProperty("mail.smtp.starttls.enable", "true");
        emailConfig.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");
        emailConfig.setProperty("mail.username", "antoninostinga0313@gmail.com"); // ✅ LA TUA EMAIL
        emailConfig.setProperty("mail.password", "sfvh axum pkci klhm"); // ✅ LA TUA APP PASSWORD
        emailConfig.setProperty("mail.admin.email", "antoninostinga0313@gmail.com"); // ✅ EMAIL ADMIN
        emailConfig.setProperty("mail.from.name", "EasyRide System");
        emailConfig.setProperty("mail.subject.prefix", "[EasyRide]");
        emailConfig.setProperty("mail.debug", "false");
        emailConfig.setProperty("mail.test.mode", "false"); // ✅ INVIO REALE
        emailConfig.setProperty("mail.send.copy.to.admin", "true");
        emailConfig.setProperty("mail.connection.timeout", "10000");
        emailConfig.setProperty("mail.read.timeout", "10000");
        
        System.out.println("✅ Configurazione email di default caricata");
        System.out.println("📧 Email configurata: antoninostinga0313@gmail.com");
        System.out.println("📧 Admin email: antoninostinga0313@gmail.com");
    }
    
    private void testConnection() {
        if (isTestMode()) {
            System.out.println("🧪 Modalità test email attiva - le email non verranno inviate");
            return;
        }
        
        try {
            Session session = createEmailSession();
            Transport transport = session.getTransport("smtp");
            transport.connect();
            transport.close();
            System.out.println("✅ Connessione email testata con successo");
        } catch (Exception e) {
            System.err.println("❌ Test connessione email fallito: " + e.getMessage());
            System.err.println("💡 Verifica che l'App Password sia corretta e la 2-Step Verification sia attiva");
            isEnabled = false;
        }
    }
    
    // === METODI PUBBLICI ===
    
    /**
     * Invia email di notifica all'admin per nuova prenotazione
     */
    public CompletableFuture<Boolean> inviaNotificaAdminAsync(Utente utente, Carrello carrello, 
                                                             String note, String paymentMethod) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return inviaNotificaAdmin(utente, carrello, note, paymentMethod);
            } catch (Exception e) {
                System.err.println("❌ Errore invio email admin: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }, emailExecutor);
    }
    
    /**
     * Invia email di conferma al cliente
     */
    public CompletableFuture<Boolean> inviaConfermaClienteAsync(Utente utente, Carrello carrello, 
                                                               String bookingId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return inviaConfermaCliente(utente, carrello, bookingId);
            } catch (Exception e) {
                System.err.println("❌ Errore invio email cliente: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }, emailExecutor);
    }
    
    // === IMPLEMENTAZIONI PRIVATE ===
    
    private boolean inviaNotificaAdmin(Utente utente, Carrello carrello, String note, String paymentMethod) {
        if (!isEnabled) {
            System.out.println("📧 Email disabilitata - connessione fallita");
            return false;
        }
        
        if (isTestMode()) {
            System.out.println("📧 [TEST] Email admin non inviata (modalità test)");
            return true;
        }
        
        try {
            Session session = createEmailSession();
            MimeMessage message = new MimeMessage(session);
            
            // Mittente
            String fromEmail = emailConfig.getProperty("mail.username");
            String fromName = emailConfig.getProperty("mail.from.name", "EasyRide System");
            message.setFrom(new InternetAddress(fromEmail, fromName));
            
            // Destinatario
            String adminEmail = emailConfig.getProperty("mail.admin.email");
            if (adminEmail == null || adminEmail.trim().isEmpty()) {
                System.err.println("❌ Email admin non configurata!");
                return false;
            }
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(adminEmail));
            
            // Oggetto
            String subject = emailConfig.getProperty("mail.subject.prefix", "[EasyRide]") + 
                           " Nuova Prenotazione - " + utente.getNome() + " " + utente.getCognome();
            message.setSubject(subject);
            
            // Corpo HTML
            String htmlBody = createAdminEmailTemplate(utente, carrello, note, paymentMethod);
            message.setContent(htmlBody, "text/html; charset=utf-8");
            
            // Timestamp
            message.setSentDate(new java.util.Date());
            
            // Invio
            Transport.send(message);
            System.out.println("✅ Email admin inviata con successo a: " + adminEmail);
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Errore invio email admin: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean inviaConfermaCliente(Utente utente, Carrello carrello, String bookingId) {
        if (!isEnabled) {
            System.out.println("📧 Email disabilitata - connessione fallita");
            return false;
        }
        
        if (isTestMode()) {
            System.out.println("📧 [TEST] Email conferma cliente non inviata (modalità test)");
            return true;
        }
        
        try {
            Session session = createEmailSession();
            MimeMessage message = new MimeMessage(session);
            
            // Mittente
            String fromEmail = emailConfig.getProperty("mail.username");
            String fromName = emailConfig.getProperty("mail.from.name", "EasyRide System");
            message.setFrom(new InternetAddress(fromEmail, fromName));
            
            // Destinatario
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(utente.getEmail()));
            
            // Copia admin (opzionale)
            if ("true".equals(emailConfig.getProperty("mail.send.copy.to.admin"))) {
                String adminEmail = emailConfig.getProperty("mail.admin.email");
                if (adminEmail != null && !adminEmail.trim().isEmpty()) {
                    message.addRecipient(Message.RecipientType.BCC, new InternetAddress(adminEmail));
                }
            }
            
            // Oggetto
            String subject = "✅ Conferma Prenotazione EasyRide #" + bookingId;
            message.setSubject(subject);
            
            // Corpo HTML
            String htmlBody = createCustomerEmailTemplate(utente, carrello, bookingId);
            message.setContent(htmlBody, "text/html; charset=utf-8");
            
            // Timestamp
            message.setSentDate(new java.util.Date());
            
            // Invio
            Transport.send(message);
            System.out.println("✅ Email conferma inviata con successo a: " + utente.getEmail());
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Errore invio email cliente: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private Session createEmailSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", emailConfig.getProperty("mail.smtp.host"));
        props.put("mail.smtp.port", emailConfig.getProperty("mail.smtp.port"));
        props.put("mail.smtp.auth", emailConfig.getProperty("mail.smtp.auth"));
        props.put("mail.smtp.starttls.enable", emailConfig.getProperty("mail.smtp.starttls.enable"));
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.protocols", emailConfig.getProperty("mail.smtp.ssl.protocols", "TLSv1.2"));
        
        // Timeout settings
        String timeout = emailConfig.getProperty("mail.connection.timeout", "10000");
        props.put("mail.smtp.connectiontimeout", timeout);
        props.put("mail.smtp.timeout", emailConfig.getProperty("mail.read.timeout", "10000"));
        
        // Debug mode
        if ("true".equals(emailConfig.getProperty("mail.debug"))) {
            props.put("mail.debug", "true");
        }
        
        // Autenticazione
        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                    emailConfig.getProperty("mail.username"),
                    emailConfig.getProperty("mail.password")
                );
            }
        };
        
        return Session.getInstance(props, auth);
    }
    
    // === TEMPLATE EMAIL ===
    
    private String createAdminEmailTemplate(Utente utente, Carrello carrello, String note, String paymentMethod) {
        StringBuilder html = new StringBuilder();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset='UTF-8'><title>Nuova Prenotazione EasyRide</title></head>");
        html.append("<body style='margin:0;padding:0;font-family:Arial,sans-serif;background:#f8f9fa;'>");
        
        // Container principale
        html.append("<div style='max-width:600px;margin:0 auto;background:white;'>");
        
        // Header
        html.append("<div style='background:linear-gradient(135deg,#007bff,#0056b3);color:white;padding:2rem;text-align:center;'>");
        html.append("<h1 style='margin:0;font-size:1.8rem;'>🚗 Nuova Prenotazione EasyRide</h1>");
        html.append("<p style='margin:0.5rem 0 0 0;opacity:0.9;font-size:0.9rem;'>")
                  .append(LocalDateTime.now().format(timeFormatter)).append("</p>");
        html.append("</div>");
        
        // Dati cliente
        html.append("<div style='padding:2rem;background:#e8f4fd;border-left:4px solid #007bff;'>");
        html.append("<h2 style='margin:0 0 1rem 0;color:#2c3e50;'>👤 Dati Cliente</h2>");
        html.append("<table style='width:100%;border-collapse:collapse;'>");
        html.append("<tr><td style='padding:0.5rem 0;font-weight:600;width:30%;'>Nome:</td>");
        html.append("<td>").append(utente.getNome()).append(" ").append(utente.getCognome()).append("</td></tr>");
        html.append("<tr><td style='padding:0.5rem 0;font-weight:600;'>Email:</td>");
        html.append("<td>").append(utente.getEmail()).append("</td></tr>");
        html.append("<tr><td style='padding:0.5rem 0;font-weight:600;'>ID Utente:</td>");
        html.append("<td>#").append(utente.getId()).append("</td></tr>");
        html.append("</table>");
        html.append("</div>");
        
        // Prenotazioni
        html.append("<div style='padding:2rem;'>");
        html.append("<h2 style='margin:0 0 1.5rem 0;color:#2c3e50;'>🚙 Dettagli Prenotazioni</h2>");
        
        for (CarrelloItem item : carrello.getItems()) {
            html.append("<div style='border:2px solid #dee2e6;border-radius:8px;padding:1.5rem;margin-bottom:1.5rem;background:#f8f9fa;'>");
            html.append("<h3 style='margin:0 0 1rem 0;color:#007bff;font-size:1.2rem;'>")
                      .append(item.getDescrizioneVeicolo()).append("</h3>");
            
            html.append("<table style='width:100%;border-collapse:collapse;'>");
            html.append("<tr><td style='padding:0.3rem 0;font-weight:600;width:30%;'>Targa:</td>");
            html.append("<td>").append(item.getTargaVeicolo()).append("</td></tr>");
            html.append("<tr><td style='padding:0.3rem 0;font-weight:600;'>Periodo:</td>");
            html.append("<td>").append(item.getDataRitiro().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            html.append(" - ").append(item.getDataRestituzione().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            html.append(" <strong>(").append(item.getNumeroGiorni()).append(" giorni)</strong></td></tr>");
            html.append("<tr><td style='padding:0.3rem 0;font-weight:600;'>Ritiro:</td>");
            html.append("<td>").append(item.getNomeTerminalRitiro()).append("</td></tr>");
            html.append("<tr><td style='padding:0.3rem 0;font-weight:600;'>Restituzione:</td>");
            html.append("<td>").append(item.getNomeTerminalRestituzione()).append("</td></tr>");
            html.append("<tr><td style='padding:0.3rem 0;font-weight:600;'>Caratteristiche:</td>");
            html.append("<td>").append(item.getCarburante()).append(" • ").append(item.getTrasmissione()).append("</td></tr>");
            html.append("<tr><td style='padding:0.3rem 0;font-weight:600;color:#28a745;'>Prezzo:</td>");
            html.append("<td style='font-size:1.1rem;font-weight:bold;color:#28a745;'>€").append(item.getPrezzoTotale()).append("</td></tr>");
            html.append("</table>");
            
            // ✅ Optional selezionati
            if (item.getOptionalSelezionati() != null && !item.getOptionalSelezionati().isEmpty()) {
                html.append("<div style='margin-top:1rem;padding-top:1rem;border-top:1px solid #dee2e6;'>");
                html.append("<strong style='color:#6c757d;'>Optional selezionati:</strong><br>");
                for (Optional opt : item.getOptionalSelezionati()) {
                    html.append("<span style='display:inline-block;background:#e7f3ff;color:#007bff;padding:0.2rem 0.6rem;border-radius:12px;margin:0.2rem 0.2rem 0 0;font-size:0.8rem;'>")
                          .append(opt.getNome()).append(" (+€").append(opt.getPrezzoExtra()).append(")</span>");
                }
                html.append("</div>");
            }
            
            html.append("</div>");
        }
        html.append("</div>");
        
        // Totali
        html.append("<div style='padding:2rem;background:#e8f5e8;border-left:4px solid #28a745;'>");
        html.append("<h2 style='margin:0 0 1rem 0;color:#2c3e50;'>💰 Riepilogo Finanziario</h2>");
        html.append("<table style='width:100%;border-collapse:collapse;font-size:1rem;'>");
        html.append("<tr><td style='padding:0.5rem 0;'>Numero Prenotazioni:</td>");
        html.append("<td style='text-align:right;font-weight:600;'>").append(carrello.getNumeroItemTotali()).append("</td></tr>");
        html.append("<tr><td style='padding:0.5rem 0;'>Giorni Totali:</td>");
        html.append("<td style='text-align:right;font-weight:600;'>").append(carrello.getNumeroGiorniTotali()).append("</td></tr>");
        html.append("<tr><td style='padding:0.5rem 0;'>Subtotale:</td>");
        html.append("<td style='text-align:right;font-weight:600;'>€").append(carrello.getTotaleCarrello()).append("</td></tr>");
        
        BigDecimal sconto = carrello.calcolaSconto();
        if (sconto.compareTo(BigDecimal.ZERO) > 0) {
            html.append("<tr style='color:#28a745;'><td style='padding:0.5rem 0;'>Sconto Applicato:</td>");
            html.append("<td style='text-align:right;font-weight:600;'>-€").append(sconto).append("</td></tr>");
        }
        
        html.append("<tr style='border-top:2px solid #28a745;font-size:1.2rem;color:#28a745;'>");
        html.append("<td style='padding:1rem 0 0 0;'><strong>TOTALE FINALE:</strong></td>");
        html.append("<td style='text-align:right;padding:1rem 0 0 0;'><strong>€").append(carrello.getTotaleFinale()).append("</strong></td></tr>");
        html.append("</table>");
        html.append("</div>");
        
        // Metodo pagamento e note
        html.append("<div style='padding:2rem;background:#fff3cd;border-left:4px solid #ffc107;'>");
        html.append("<h2 style='margin:0 0 1rem 0;color:#2c3e50;'>💳 Informazioni Aggiuntive</h2>");
        html.append("<p><strong>Metodo di Pagamento:</strong> ").append(getPaymentMethodName(paymentMethod)).append("</p>");
        if (note != null && !note.trim().isEmpty()) {
            html.append("<p><strong>Note del Cliente:</strong></p>");
            html.append("<div style='background:white;padding:1rem;border-radius:6px;border-left:3px solid #007bff;font-style:italic;'>")
                      .append(note.replace("\n", "<br>")).append("</div>");
        }
        html.append("</div>");
        
        // Footer
        html.append("<div style='padding:2rem;text-align:center;background:#2c3e50;color:white;'>");
        html.append("<p style='margin:0;font-size:0.9rem;'>🤖 Email automatica generata dal sistema EasyRide</p>");
        html.append("<p style='margin:0.5rem 0 0 0;font-size:0.8rem;opacity:0.8;'>Per supporto tecnico contattare l'amministratore di sistema</p>");
        html.append("</div>");
        
        html.append("</div></body></html>");
        
        return html.toString();
    }
    
    private String createCustomerEmailTemplate(Utente utente, Carrello carrello, String bookingId) {
        // Template più semplice per il cliente
        StringBuilder html = new StringBuilder();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset='UTF-8'><title>Conferma Prenotazione EasyRide</title></head>");
        html.append("<body style='margin:0;padding:0;font-family:Arial,sans-serif;background:#f8f9fa;'>");
        
        html.append("<div style='max-width:600px;margin:0 auto;background:white;'>");
        
        // Header
        html.append("<div style='background:linear-gradient(135deg,#28a745,#20c997);color:white;padding:2rem;text-align:center;'>");
        html.append("<h1 style='margin:0;'>✅ Prenotazione Confermata!</h1>");
        html.append("<p style='margin:0.5rem 0 0 0;opacity:0.9;'>ID Prenotazione: #").append(bookingId).append("</p>");
        html.append("</div>");
        
        // Saluto
        html.append("<div style='padding:2rem;'>");
        html.append("<h2 style='color:#2c3e50;'>Ciao ").append(utente.getNome()).append("!</h2>");
        html.append("<p>La tua prenotazione è stata registrata con successo. Ecco i dettagli:</p>");
        html.append("</div>");
        
        // Dettagli semplificati
        html.append("<div style='padding:0 2rem 2rem 2rem;'>");
        for (CarrelloItem item : carrello.getItems()) {
            html.append("<div style='border:1px solid #dee2e6;border-radius:8px;padding:1.5rem;margin-bottom:1rem;'>");
            html.append("<h3 style='margin:0 0 0.5rem 0;color:#007bff;'>").append(item.getDescrizioneVeicolo()).append("</h3>");
            html.append("<p style='margin:0.25rem 0;'><strong>Periodo:</strong> ")
                      .append(item.getDataRitiro().format(dateFormatter)).append(" - ")
                      .append(item.getDataRestituzione().format(dateFormatter)).append("</p>");
            html.append("<p style='margin:0.25rem 0;'><strong>Ritiro:</strong> ").append(item.getNomeTerminalRitiro()).append("</p>");
            html.append("<p style='margin:0.25rem 0;'><strong>Totale:</strong> €").append(item.getPrezzoTotale()).append("</p>");
            html.append("</div>");
        }
        html.append("</div>");
        
        // Totale finale
        html.append("<div style='padding:2rem;background:#e8f5e8;text-align:center;'>");
        html.append("<h2 style='margin:0;color:#28a745;'>Totale: €").append(carrello.getTotaleFinale()).append("</h2>");
        html.append("</div>");
        
        // Footer
        html.append("<div style='padding:2rem;text-align:center;background:#f8f9fa;color:#6c757d;'>");
        html.append("<p>Grazie per aver scelto EasyRide! 🚗</p>");
        html.append("<p style='font-size:0.9rem;'>Per qualsiasi domanda, contattaci all'indirizzo: ")
                  .append(emailConfig.getProperty("mail.admin.email", "support@easyride.com")).append("</p>");
        html.append("</div>");
        
        html.append("</div></body></html>");
        
        return html.toString();
    }
    
    // === UTILITY METHODS ===
    
    private String getPaymentMethodName(String method) {
        switch (method != null ? method : "unknown") {
            case "card": return "💳 Carta di Credito/Debito";
            case "paypal": return "🅿️ PayPal";
            case "bank": return "🏦 Bonifico Bancario";
            default: return "❓ Non specificato";
        }
    }
    
    private boolean isTestMode() {
        return "true".equals(emailConfig.getProperty("mail.test.mode", "false"));
    }
    
    public boolean isEnabled() {
        return isEnabled;
    }
    
    public void setTestMode(boolean testMode) {
        emailConfig.setProperty("mail.test.mode", String.valueOf(testMode));
    }
    
    public void shutdown() {
        if (emailExecutor != null && !emailExecutor.isShutdown()) {
            emailExecutor.shutdown();
            System.out.println("🔌 EmailManager shutdown completato");
        }
    }
}