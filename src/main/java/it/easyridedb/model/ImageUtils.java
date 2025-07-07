package it.easyridedb.model;

import jakarta.servlet.ServletContext;
import java.io.File;


public class ImageUtils {
    
    private static final String IMAGES_DIR = "images/veicoli/";
    private static final String DEFAULT_IMAGE = "images/veicoli/default.jpg";
    
    
    public static String getVehicleImageUrl(String imageUrl, String targa, ServletContext context) {
        // Se l'immagine è specificata nel database e esiste
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            if (imageExists(imageUrl, context)) {
                return imageUrl;
            }
        }
        
        // Prova con il nome della targa (formato standard)
        if (targa != null && !targa.trim().isEmpty()) {
            String targaImageUrl = IMAGES_DIR + targa.toUpperCase() + ".jpg";
            if (imageExists(targaImageUrl, context)) {
                return targaImageUrl;
            }
            
            // Prova anche con .png
            targaImageUrl = IMAGES_DIR + targa.toUpperCase() + ".png";
            if (imageExists(targaImageUrl, context)) {
                return targaImageUrl;
            }
        }
        
        // Fallback all'immagine di default
        return DEFAULT_IMAGE;
    }
    
    private static boolean imageExists(String imageUrl, ServletContext context) {
        if (context == null || imageUrl == null) {
            return false;
        }
        
        try {
            String realPath = context.getRealPath("/" + imageUrl);
            if (realPath != null) {
                File file = new File(realPath);
                boolean exists = file.exists() && file.isFile();
                
                // Debug info nella console
                System.out.println("DEBUG ImageUtils:");
                System.out.println("  - URL richiesta: " + imageUrl);
                System.out.println("  - Path reale: " + realPath);
                System.out.println("  - File esiste: " + exists);
                
                return exists;
            }
        } catch (Exception e) {
            System.err.println("Errore controllo immagine: " + e.getMessage());
        }
        
        return false;
    }
    
 
    public static String getImageAltText(String marca, String modello, String targa) {
        if (marca != null && modello != null) {
            return marca + " " + modello + " (" + (targa != null ? targa : "veicolo") + ")";
        }
        return "Veicolo " + (targa != null ? targa : "disponibile");
    }
    
    
    public static boolean isValidImageUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        
        String lowerUrl = url.toLowerCase();
        return lowerUrl.endsWith(".jpg") || 
               lowerUrl.endsWith(".jpeg") || 
               lowerUrl.endsWith(".png") || 
               lowerUrl.endsWith(".webp");
    }
}