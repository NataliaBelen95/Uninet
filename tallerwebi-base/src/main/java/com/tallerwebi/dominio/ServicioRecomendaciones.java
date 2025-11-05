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


//    public String construirPerfilUsuario(Usuario usuario) {
//        List<Interaccion> likes = servicioInteraccion.obtenerInteraccionesDeUsuario(usuario)
//                .stream()
//                .filter(i -> "LIKE".equals(i.getTipo()))
//                //  ignorar likes sobre sus propias publicaciones
//                .filter(i -> i.getPublicacion().getUsuario().getId() != usuario.getId())
//                .collect(Collectors.toList());
//
//        return likes.stream()
//                .map(i -> i.getPublicacion().getDescripcion())
//                .reduce("", (a, b) -> a + " " + b);
//    }

    public List<Publicacion> recomendarParaUsuario(Usuario usuario, int limite) throws Exception {

        // 1. Obtener el perfil de gustos ANALIZADO por Gemini
        GustosPersonal gustos = repositorioGustoPersonal.buscarPorUsuario(usuario);

        // ➡️ Verificar si hay gustos analizados (Lógica de salida correcta)
        if (gustos == null || gustos.getTagsIntereses() == null || gustos.getTagsIntereses().isEmpty()) {
            System.out.println("Perfil de Gustos (Gemini) vacío. No hay recomendación inteligente.");
            return new ArrayList<>();
        }

        // 2. Indexar (mantener la indexación, protegida por el check interno en LuceneService)
        List<Publicacion> todas = repositorioPublicacion.listarTodas();
        luceneService.indexarPublicaciones(todas);

        // 3. Usar los TAGS de Gemini como texto de búsqueda para Lucene
        String tagsParaBusqueda = gustos.getTagsIntereses();

        System.out.println("Tags de búsqueda (Gemini): " + tagsParaBusqueda);

        List<String> idsSimilares = luceneService.buscarSimilares(tagsParaBusqueda, limite);
        System.out.println("IDs devueltos por Lucene usando tags: " + idsSimilares);

        // 4. Mapear IDs a Publicaciones y aplicar filtros
        return idsSimilares.stream()
                // 4a. Mapeo a objeto Publicacion
                .map(id -> repositorioPublicacion.obtenerPublicacionCompleta(Long.parseLong(id)))
                .filter(p -> p != null)

                // 🔑 CORRECCIÓN 1: Excluir las publicaciones donde el autor es el usuario logueado
                .filter(p -> p.getUsuario().getId() != usuario.getId())

                // 🔑 CORRECCIÓN 2: Opcional: Eliminar publicaciones genéricas que no tienen tema (si las hay)
                .filter(p -> !p.getDescripcion().toLowerCase().contains("publicación vieja de prueba"))

                .distinct()
                .collect(Collectors.toList());
    }

}
