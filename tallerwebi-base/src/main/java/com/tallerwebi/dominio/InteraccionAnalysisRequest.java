package com.tallerwebi.dominio;


/*DTO/Clase Java	Objeto temporal en memoria. Agrupa y consolida el texto de múltiples interacciones (ej.,
los últimos 10 comentarios y likes de un usuario)
en el campo textoConsolidado para enviarlo como una única solicitud a la IA de Gemini*/

public class InteraccionAnalysisRequest {
    private Long usuarioId;
    private String textoConsolidado;

}
