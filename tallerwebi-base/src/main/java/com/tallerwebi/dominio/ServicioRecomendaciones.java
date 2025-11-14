package com.tallerwebi.dominio;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ServicioRecomendaciones {


    private final ServicioInteraccion servicioInteraccion;
    private final LuceneService luceneService;
    private final RepositorioPublicacion repositorioPublicacion;
    private final RepositorioGustoPersonal repositorioGustoPersonal;

    @Autowired
    public ServicioRecomendaciones(ServicioInteraccion servicioInteraccion,
                                   LuceneService luceneService,
                                   RepositorioPublicacion repositorioPublicacion, RepositorioGustoPersonal repositorioGustoPersonal) {
        this.servicioInteraccion = servicioInteraccion;
        this.luceneService = luceneService;
        this.repositorioPublicacion = repositorioPublicacion;
        this.repositorioGustoPersonal = repositorioGustoPersonal;
    }

    // Busca el perfil de gustos del usuario.
    // Si no hay tags, no se pueden generar recomendaciones.
    // Si los hay, actualiza el índice de Lucene con todas las publicaciones
    // (Lucene ignora duplicados internamente) y devuelve el texto de tags.
    private String prepararContextoRecomendacion(Usuario usuario) throws Exception {
        GustosPersonal gustos = repositorioGustoPersonal.buscarPorUsuario(usuario);

        if (gustos == null || gustos.getTagsIntereses() == null || gustos.getTagsIntereses().isEmpty()) {
            System.out.println("Perfil de Gustos vacío para usuario " + usuario.getId());
            return null;
        }

//         Indexa todas las publicaciones (LuceneService ya maneja duplicados internamente)
//        List<Publicacion> todas = repositorioPublicacion.listarTodas();
//        luceneService.indexarPublicaciones(todas);

        return gustos.getTagsIntereses();
    }



//obtengo tags limpios , y ahi si lucene busca en su directorio publicaciones similares de acuerdo a los tags.
    public List<Publicacion> recomendarParaUsuario(Usuario usuario, int limite) throws Exception {

        String tags = prepararContextoRecomendacion(usuario);
        if (tags == null) return new ArrayList<>();

        System.out.println("Tags de búsqueda (Gemini): " + tags);

        List<String> idsSimilares = luceneService.buscarSimilares(tags, limite);
        System.out.println("IDs devueltos por Lucene: " + idsSimilares);

        return idsSimilares.stream()
                .map(id -> repositorioPublicacion.obtenerPublicacionCompleta(Long.parseLong(id)))
                .filter(p -> p != null)
                .filter(p -> p.getUsuario().getId() != usuario.getId())
                .distinct()
                .collect(Collectors.toList());
    }

}
