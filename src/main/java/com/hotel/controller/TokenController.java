package com.hotel.controller;

import com.hotel.model.Token;
import com.hotel.service.TokenService;
import com.hotel.util.JsonUtil;
import servlet.annotations.Controller;
import servlet.api.ApiResponse;
import servlet.annotations.mapping.GetMapping;
import servlet.annotations.mapping.PostMapping;
import servlet.annotations.RequestParam;
import servlet.ModelView;

import java.sql.Timestamp;
import java.util.List;

@Controller
public class TokenController {

    private TokenService tokenService = new TokenService();

    @GetMapping(value = "/tokens")
    public ModelView getAllTokens() {
        ModelView mv = new ModelView();
        try {
            List<Token> tokens = tokenService.getAllTokens();
            ApiResponse<List<Token>> response = ApiResponse.success(tokens);
            mv.addAttribute("jsonResponse", JsonUtil.toJson(response));
            mv.setView("json-response.jsp");
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse<String> response = ApiResponse.error(500, e.getMessage(), null);
            mv.addAttribute("jsonResponse", JsonUtil.toJson(response));
            mv.setView("json-response.jsp");
        }
        return mv;
    }

    @PostMapping(value = "/tokens/generate")
    public ModelView generateToken(@RequestParam(name = "expiration_minutes") String expirationMinutesStr) {
        ModelView mv = new ModelView();
        try {
            int expirationMinutes = 60; // Par défaut 1 heure
            if (expirationMinutesStr != null && !expirationMinutesStr.isEmpty()) {
                expirationMinutes = Integer.parseInt(expirationMinutesStr);
            }

            Token token = tokenService.generateToken(expirationMinutes);
            ApiResponse<Token> response = ApiResponse.success(token);
            mv.addAttribute("jsonResponse", JsonUtil.toJson(response));
            mv.setView("json-response.jsp");
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse<String> response = ApiResponse.error(500, e.getMessage(), null);
            mv.addAttribute("jsonResponse", JsonUtil.toJson(response));
            mv.setView("json-response.jsp");
        }
        return mv;
    }

    @PostMapping(value = "/tokens/validate")
    public ModelView validateToken(@RequestParam(name = "token") String tokenValue) {
        ModelView mv = new ModelView();
        try {
            boolean isValid = tokenService.isTokenValid(tokenValue);
            
            if (isValid) {
                ApiResponse<String> response = ApiResponse.success("Token valide");
                mv.addAttribute("jsonResponse", JsonUtil.toJson(response));
            } else {
                ApiResponse<String> response = ApiResponse.error(401, "Token invalide ou expiré", null);
                mv.addAttribute("jsonResponse", JsonUtil.toJson(response));
            }
            mv.setView("json-response.jsp");
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse<String> response = ApiResponse.error(500, e.getMessage(), null);
            mv.addAttribute("jsonResponse", JsonUtil.toJson(response));
            mv.setView("json-response.jsp");
        }
        return mv;
    }

    @PostMapping(value = "/tokens/cleanup")
    public ModelView cleanupExpiredTokens() {
        ModelView mv = new ModelView();
        try {
            int deletedCount = tokenService.deleteExpiredTokens();
            ApiResponse<String> response = ApiResponse.success(deletedCount + " token(s) expiré(s) supprimé(s)");
            mv.addAttribute("jsonResponse", JsonUtil.toJson(response));
            mv.setView("json-response.jsp");
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse<String> response = ApiResponse.error(500, e.getMessage(), null);
            mv.addAttribute("jsonResponse", JsonUtil.toJson(response));
            mv.setView("json-response.jsp");
        }
        return mv;
    }

    @PostMapping(value = "/tokens/delete")
    public ModelView deleteToken(@RequestParam(name = "id") int id) {
        ModelView mv = new ModelView();
        try {
            boolean deleted = tokenService.deleteToken(id);
            if (deleted) {
                ApiResponse<String> response = ApiResponse.success("Token supprimé avec succès");
                mv.addAttribute("jsonResponse", JsonUtil.toJson(response));
            } else {
                ApiResponse<String> response = ApiResponse.error(404, "Token non trouvé", null);
                mv.addAttribute("jsonResponse", JsonUtil.toJson(response));
            }
            mv.setView("json-response.jsp");
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse<String> response = ApiResponse.error(500, e.getMessage(), null);
            mv.addAttribute("jsonResponse", JsonUtil.toJson(response));
            mv.setView("json-response.jsp");
        }
        return mv;
    }
}
