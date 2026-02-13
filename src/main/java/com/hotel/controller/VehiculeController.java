package com.hotel.controller;

import com.hotel.model.Vehicule;
import com.hotel.service.VehiculeService;
import com.hotel.util.JsonUtil;
import servlet.annotations.Controller;
import servlet.api.ApiResponse;
import servlet.annotations.mapping.GetMapping;
import servlet.annotations.mapping.PostMapping;
import servlet.annotations.RequestParam;
import servlet.ModelView;

import java.util.List;

@Controller
public class VehiculeController {

    private VehiculeService vehiculeService = new VehiculeService();

    @GetMapping(value = "/vehicules")
    public ModelView getAllVehicules() {
        ModelView mv = new ModelView();
        try {
            List<Vehicule> vehicules = vehiculeService.getAllVehicules();
            ApiResponse<List<Vehicule>> response = ApiResponse.success(vehicules);
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

    @GetMapping(value = "/vehicules/detail")
    public ModelView getVehiculeById(@RequestParam(name = "id") int id) {
        ModelView mv = new ModelView();
        try {
            Vehicule vehicule = vehiculeService.getVehiculeById(id);
            if (vehicule != null) {
                ApiResponse<Vehicule> response = ApiResponse.success(vehicule);
                mv.addAttribute("jsonResponse", JsonUtil.toJson(response));
            } else {
                ApiResponse<String> response = ApiResponse.error(404, "Véhicule non trouvé", null);
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

    @PostMapping(value = "/vehicules")
    public ModelView createVehicule(
            @RequestParam(name = "reference") String reference,
            @RequestParam(name = "place") int place,
            @RequestParam(name = "type_carburant") String typeCarburant) {

        ModelView mv = new ModelView();
        try {
            Vehicule vehicule = vehiculeService.createVehicule(reference, place, typeCarburant);
            if (vehicule != null) {
                ApiResponse<Vehicule> response = ApiResponse.success(vehicule);
                mv.addAttribute("jsonResponse", JsonUtil.toJson(response));
            } else {
                ApiResponse<String> response = ApiResponse.error(500, "Erreur lors de la création", null);
                mv.addAttribute("jsonResponse", JsonUtil.toJson(response));
            }
            mv.setView("json-response.jsp");
        } catch (IllegalArgumentException e) {
            ApiResponse<String> response = ApiResponse.error(400, e.getMessage(), null);
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

    @PostMapping(value = "/vehicules/update")
    public ModelView updateVehicule(
            @RequestParam(name = "id") int id,
            @RequestParam(name = "reference") String reference,
            @RequestParam(name = "place") int place,
            @RequestParam(name = "type_carburant") String typeCarburant) {

        ModelView mv = new ModelView();
        try {
            Vehicule vehicule = vehiculeService.updateVehicule(id, reference, place, typeCarburant);
            if (vehicule != null) {
                ApiResponse<Vehicule> response = ApiResponse.success(vehicule);
                mv.addAttribute("jsonResponse", JsonUtil.toJson(response));
            } else {
                ApiResponse<String> response = ApiResponse.error(404, "Véhicule non trouvé", null);
                mv.addAttribute("jsonResponse", JsonUtil.toJson(response));
            }
            mv.setView("json-response.jsp");
        } catch (IllegalArgumentException e) {
            ApiResponse<String> response = ApiResponse.error(400, e.getMessage(), null);
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

    @PostMapping(value = "/vehicules/delete")
    public ModelView deleteVehicule(@RequestParam(name = "id") int id) {
        ModelView mv = new ModelView();
        try {
            boolean deleted = vehiculeService.deleteVehicule(id);
            if (deleted) {
                ApiResponse<String> response = ApiResponse.success("Véhicule supprimé avec succès");
                mv.addAttribute("jsonResponse", JsonUtil.toJson(response));
            } else {
                ApiResponse<String> response = ApiResponse.error(404, "Véhicule non trouvé", null);
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
