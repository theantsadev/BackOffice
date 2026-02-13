package com.hotel;

import com.hotel.model.Token;
import com.hotel.service.TokenService;

import java.sql.Timestamp;
import java.util.List;

/**
 * Classe principale pour générer et gérer les tokens
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Gestion des Tokens ===\n");

        TokenService tokenService = new TokenService();

        try {
            // Suppression des tokens expirés
            int deletedCount = tokenService.deleteExpiredTokens();
            System.out.println("Tokens expirés supprimés: " + deletedCount);

            // Génération de nouveaux tokens (expiration dans 60 minutes)
            System.out.println("\n--- Génération de nouveaux tokens ---");
            
            Token token1 = tokenService.generateToken(60);  // expire dans 1 heure
            System.out.println("Token 1 créé: " + token1.getToken());
            System.out.println("  Expiration: " + token1.getDateHeureExpiration());

            Token token2 = tokenService.generateToken(120); // expire dans 2 heures
            System.out.println("Token 2 créé: " + token2.getToken());
            System.out.println("  Expiration: " + token2.getDateHeureExpiration());

            Token token3 = tokenService.generateToken(1440); // expire dans 24 heures
            System.out.println("Token 3 créé: " + token3.getToken());
            System.out.println("  Expiration: " + token3.getDateHeureExpiration());

            // Liste de tous les tokens
            System.out.println("\n--- Liste de tous les tokens ---");
            List<Token> tokens = tokenService.getAllTokens();
            for (Token t : tokens) {
                String status = t.isExpired() ? "EXPIRÉ" : "VALIDE";
                System.out.println("ID: " + t.getId() + 
                                   " | Token: " + t.getToken().substring(0, 8) + "..." +
                                   " | Expiration: " + t.getDateHeureExpiration() + 
                                   " | Status: " + status);
            }

            // Test de validation d'un token
            System.out.println("\n--- Test de validation ---");
            boolean isValid = tokenService.isTokenValid(token1.getToken());
            System.out.println("Token 1 valide: " + isValid);

            System.out.println("\n=== Fin du programme ===");

        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
