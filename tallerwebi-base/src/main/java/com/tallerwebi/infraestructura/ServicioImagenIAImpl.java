package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.ServicioImagenIA;
import org.springframework.stereotype.Service;

@Service
public class ServicioImagenIAImpl implements ServicioImagenIA {

    @Override
    public String generarImagenRelacionada(String temaPrincipal) throws Exception {

        // 🔑 El prefijo lógico que Spring espera para el Resource Handler
        final String BASE_URL = "/imagenesPublicidad/";

        String temaLimpio = temaPrincipal.toLowerCase();

        System.out.println("🖼️ Generando imagen para el tema: " + temaPrincipal);

        // 🔑 LÓGICA DE MOCK CORREGIDA: Usar el prefijo /imagenesPublicidad/
        if (temaLimpio.contains("programación") || temaLimpio.contains("software") || temaLimpio.contains("tecnología")) {
            // ✅ CORREGIDO
            return BASE_URL + "dev-background.png";
        }
        if (temaLimpio.contains("economía") || temaLimpio.contains("finanzas") || temaLimpio.contains("gestión")) {
            // ✅ CORREGIDO
            return BASE_URL + "financial-chart-art.png";
        }
        if (temaLimpio.contains("matemática") || temaLimpio.contains("algoritmos")) {
            // ✅ CORREGIDO
            return BASE_URL + "math-science-art.png";
        }

        // ✅ CORREGIDO
        return BASE_URL + "default-university-ad.png";
    }
}
