package com.tallerwebi.dominio;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ServicioRecomendaciones {


    private final ServicioInteraccion servicioInteraccion;

    private final LuceneService luceneService;

    private final RepositorioPublicacion repositorioPublicacion;

    @Autowired
    public ServicioRecomendaciones(ServicioInteraccion servicioInteraccion,
                                   LuceneService luceneService,
                                   RepositorioPublicacion repositorioPublicacion) {
        this.servicioInteraccion = servicioInteraccion;
        this.luceneService = luceneService;
        this.repositorioPublicacion = repositorioPublicacion;
    }


    public String construirPerfilUsuario(Usuario usuario) {
        List<Interaccion> likes = servicioInteraccion.obtenerInteraccionesDeUsuario(usuario)
                .stream()
                .filter(i -> "LIKE".equals(i.getTipo()))
                //  ignorar likes sobre sus propias publicaciones
                .filter(i -> i.getPublicacion().getUsuario().getId() != usuario.getId())
                .collect(Collectors.toList());

        return likes.stream()
                .map(i -> i.getPublicacion().getDescripcion())
                .reduce("", (a, b) -> a + " " + b);
    }

    public List<Publicacion> recomendarParaUsuario(Usuario usuario, int limite) throws Exception {
        // Indexar todas las publicaciones actuales siempre
        List<Publicacion> todas = repositorioPublicacion.listarTodas();
        luceneService.indexarPublicaciones(todas);

        String perfil = construirPerfilUsuario(usuario);
        System.out.println("Perfil del usuario: " + perfil);

        List<String> idsSimilares = luceneService.buscarSimilares(perfil, limite);
        System.out.println("IDs devueltos por Lucene: " + idsSimilares);

        return idsSimilares.stream()
                .map(id -> repositorioPublicacion.obtenerPublicacionCompleta(Long.parseLong(id)))
                .filter(p -> p != null)
                .distinct()
                .collect(Collectors.toList());
    }

}
