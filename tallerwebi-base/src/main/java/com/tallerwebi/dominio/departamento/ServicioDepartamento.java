package com.tallerwebi.dominio.departamento;

import com.tallerwebi.infraestructura.RepositorioDepartamento;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class ServicioDepartamento {
    private final RepositorioDepartamento repositorioDepartamento;

    @Autowired
    public ServicioDepartamento(RepositorioDepartamento repositorioDepartamento) {
        this.repositorioDepartamento = repositorioDepartamento;
    }
    public List<Departamento> obtenerDepartamentos(){
        return repositorioDepartamento.obtenerDepartamentos();
    }
}
