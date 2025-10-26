package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.departamento.Departamento;

import java.util.List;

public interface RepositorioDepartamento {
    List<Departamento> obtenerDepartamentos();
}
