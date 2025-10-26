package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Carrera;
import com.tallerwebi.dominio.ServicioCarrera;
import com.tallerwebi.presentacion.DTO.CarreraDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ControladorCarrera {

    @Autowired
    private ServicioCarrera servicioCarrera;


    //evita que spring intente convertir las relaciones materias, usuarios o deptos y solo devuelv lo que necesito
    @GetMapping("/carreras-por-departamento")
    @ResponseBody
    public List<CarreraDTO> obtenerCarrerasPorDepartamento(@RequestParam("departamentoId") Integer departamentoId) {
        List<Carrera> carreras = servicioCarrera.buscarPorDepartamento(departamentoId);
        return carreras.stream()
                .map(CarreraDTO::new)
                .collect(Collectors.toList());
    }



}