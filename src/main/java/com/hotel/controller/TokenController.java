package com.hotel.controller;

import com.hotel.model.Token;
import com.hotel.service.TokenService;
import servlet.annotations.Controller;
import servlet.api.ApiResponse;
import servlet.annotations.Json;
import servlet.annotations.mapping.GetMapping;
import servlet.annotations.mapping.PostMapping;
import servlet.annotations.RequestParam;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Controller
public class TokenController {

    private TokenService tokenService = new TokenService();

    @Json
    @GetMapping(value = "/tokens")
    public ApiResponse<?> getAllTokens() throws SQLException {
        try {
            List<Token> tokens = tokenService.getAllTokens();
            return ApiResponse.success(tokens);
        } catch (Exception e) {
            String errorMessage = "Error fetching tokens: " + e.getMessage();
            System.err.println(errorMessage);
            return ApiResponse.error(500, errorMessage, null);
        }
    }

    @Json
    @PostMapping(value = "/tokens/generate")
    public ApiResponse<?> generateToken(@RequestParam(name = "expiration_minutes") String expirationMinutesStr)
            throws SQLException {
        try {
            int expirationMinutes = 60; // Par défaut 1 heure
            if (expirationMinutesStr != null && !expirationMinutesStr.isEmpty()) {
                expirationMinutes = Integer.parseInt(expirationMinutesStr);
            }

            Token token = tokenService.generateToken(expirationMinutes);
            return ApiResponse.success(token);
        } catch (Exception e) {
            String errorMessage = "Error generating token: " + e.getMessage();
            System.err.println(errorMessage);
            return ApiResponse.error(500, errorMessage, null);
        }
    }

    @Json
    @PostMapping(value = "/tokens/validate")
    public ApiResponse<?> validateToken(@RequestParam(name = "token") String tokenValue) throws SQLException {
        try {
            boolean isValid = tokenService.isTokenValid(tokenValue);

            if (isValid) {
                return ApiResponse.success("Token valide");
            } else {
                return ApiResponse.error(401, "Token invalide ou expiré", null);
            }
        } catch (Exception e) {
            String errorMessage = "Error validating token: " + e.getMessage();
            System.err.println(errorMessage);
            return ApiResponse.error(500, errorMessage, null);
        }
    }

    @Json
    @PostMapping(value = "/tokens/cleanup")
    public ApiResponse<?> cleanupExpiredTokens() throws SQLException {
        try {
            int deletedCount = tokenService.deleteExpiredTokens();
            return ApiResponse.success(deletedCount + " token(s) expiré(s) supprimé(s)");
        } catch (Exception e) {
            String errorMessage = "Error cleaning up tokens: " + e.getMessage();
            System.err.println(errorMessage);
            return ApiResponse.error(500, errorMessage, null);
        }
    }

    @Json
    @PostMapping(value = "/tokens/delete")
    public ApiResponse<?> deleteToken(@RequestParam(name = "id") int id) throws SQLException {
        try {
            boolean deleted = tokenService.deleteToken(id);
            if (deleted) {
                return ApiResponse.success("Token supprimé avec succès");
            } else {
                return ApiResponse.error(404, "Token non trouvé", null);
            }
        } catch (Exception e) {
            String errorMessage = "Error deleting token: " + e.getMessage();
            System.err.println(errorMessage);
            return ApiResponse.error(500, errorMessage, null);
        }
    }
}
