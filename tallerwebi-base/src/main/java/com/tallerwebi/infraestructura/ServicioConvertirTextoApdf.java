package com.tallerwebi.infraestructura;

import java.io.File;
import java.io.IOException;


public interface ServicioConvertirTextoApdf {
    File generarPdf(String resumen, String nombreArchivo)throws IOException;;
}
