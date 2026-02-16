package com.hotel.controller;

import com.hotel.model.Vehicule;
import com.hotel.service.VehiculeService;
import servlet.annotations.Controller;
import servlet.api.ApiResponse;
import servlet.annotations.Json;
import servlet.annotations.mapping.GetMapping;
import servlet.annotations.mapping.PostMapping;
import servlet.annotations.RequestParam;

import java.sql.SQLException;
import java.util.List;

@Controller
public class VehiculeController {

    private VehiculeService vehiculeService = new VehiculeService();

    @Json
    @GetMapping(value = "/vehicules")
    public ApiResponse<?> getAllVehicules() throws SQLException {
        try {
            List<Vehicule> vehicules = vehiculeService.getAllVehicules();
            return ApiResponse.success(vehicules);
        } catch (Exception e) {
            String errorMessage = "Error fetching vehicules: " + e.getMessage();
            System.err.println(errorMessage);
            return ApiResponse.error(500, errorMessage, null);
        }
    }

    @Json
    @GetMapping(value = "/vehicules/detail")
    public ApiResponse<?> getVehiculeById(@RequestParam(name = "id") int id) throws SQLException {
        try {
            Vehicule vehicule = vehiculeService.getVehiculeById(id);
            if (vehicule != null) {
                return ApiResponse.success(vehicule);
            } else {
                return ApiResponse.error(404, "Véhicule non trouvé", null);
            }
        } catch (Exception e) {
            String errorMessage = "Error fetching vehicule: " + e.getMessage();
            System.err.println(errorMessage);
            return ApiResponse.error(500, errorMessage, null);
        }
    }

    @Json
    @PostMapping(value = "/vehicules")
    public ApiResponse<?> createVehicule(
            @RequestParam(name = "reference") String reference,
            @RequestParam(name = "place") int place,
            @RequestParam(name = "type_carburant") String typeCarburant) throws SQLException {

        try {
            Vehicule vehicule = vehiculeService.createVehicule(reference, place, typeCarburant);
            if (vehicule != null) {
                return ApiResponse.success(vehicule);
            } else {
                return ApiResponse.error(500, "Erreur lors de la création", null);
            }
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage(), null);
        } catch (Exception e) {
            String errorMessage = "Error creating vehicule: " + e.getMessage();
            System.err.println(errorMessage);
            return ApiResponse.error(500, errorMessage, null);
        }
    }

    @Json
    @PostMapping(value = "/vehicules/update")
    public ApiResponse<?> updateVehicule(
            @RequestParam(name = "id") int id,
            @RequestParam(name = "reference") String reference,
            @RequestParam(name = "place") int place,
            @RequestParam(name = "type_carburant") String typeCarburant) throws SQLException {

        try {
            Vehicule vehicule = vehiculeService.updateVehicule(id, reference, place, typeCarburant);
            if (vehicule != null) {
                return ApiResponse.success(vehicule);
            } else {
                return ApiResponse.error(404, "Véhicule non trouvé", null);
            }
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage(), null);
        } catch (Exception e) {
            String errorMessage = "Error updating vehicule: " + e.getMessage();
            System.err.println(errorMessage);
            return ApiResponse.error(500, errorMessage, null);
        }
    }

    @Json
    @PostMapping(value = "/vehicules/delete")
    public ApiResponse<?> deleteVehicule(@RequestParam(name = "id") int id) throws SQLException {
        try {
            boolean deleted = vehiculeService.deleteVehicule(id);
            if (deleted) {
                return ApiResponse.success("Véhicule supprimé avec succès");
            } else {
                return ApiResponse.error(404, "Véhicule non trouvé", null);
            }
        } catch (Exception e) {
            String errorMessage = "Error deleting vehicule: " + e.getMessage();
            System.err.println(errorMessage);
            return ApiResponse.error(500, errorMessage, null);
        }
    }
}
