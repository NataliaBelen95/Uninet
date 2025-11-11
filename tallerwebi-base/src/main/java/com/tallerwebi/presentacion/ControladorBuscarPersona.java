package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioUsuario;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.presentacion.DTO.DatosUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ControladorBuscarPersona {
    private final ServicioUsuario servicioUsuario;

    @Autowired
    public ControladorBuscarPersona(ServicioUsuario servicioUsuario) {
        this.servicioUsuario = servicioUsuario;
    }
    @GetMapping("/buscarPersona")
    @ResponseBody
    public List<DatosUsuario> buscarPersona(@RequestParam("query") String query) {
        List<Usuario> usuariosEncontrados = servicioUsuario.buscarUsuarionNombreOEmail(query);

        // Mapear los usuarios encontrados a un DTO (DatosUsuario)
        List<DatosUsuario> resultados = usuariosEncontrados.stream()
                .map(usuario -> {
                    DatosUsuario dto = new DatosUsuario();
                    dto.setId(usuario.getId());
                    dto.setNombre(usuario.getNombre());
                    dto.setApellido(usuario.getApellido());
                    dto.setEmail(usuario.getEmail());
                    dto.setFotoPerfil(usuario.getFotoPerfil());
                    dto.setSlug(usuario.getSlug());
                    return dto;
                })
                .collect(Collectors.toList());

        // Devolver los resultados como JSON
        return resultados;
    }

}
