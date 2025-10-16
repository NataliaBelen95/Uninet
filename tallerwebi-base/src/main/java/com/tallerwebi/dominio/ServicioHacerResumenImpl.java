package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.NoSePudoExtraerElTextoDelPDFException;
import com.tallerwebi.dominio.excepcion.NoSePudoGenerarResumenDelPDFException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import java.io.File;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;


@Service
public class ServicioHacerResumenImpl implements ServicioHacerResumen {
    private final ComponentClienteGemini  componentClienteGemini;

    public ServicioHacerResumenImpl(ComponentClienteGemini componentClienteGemini) {
        this.componentClienteGemini = componentClienteGemini;
    }

    @Override
    public String extraerTexto(String rutaArchivo) throws NoSePudoExtraerElTextoDelPDFException {
        File archivoPDF = new File(rutaArchivo);
        //con esto voy a intentar abrir el pdf para poder leerlo
        try (PDDocument documento = PDDocument.load(archivoPDF)) {
            PDFTextStripper stripper = new PDFTextStripper();//esto extrae tdo el texto del pdf
            return stripper.getText(documento);//devuelvo el texto como un string
        } catch (Exception e) {
            throw new NoSePudoExtraerElTextoDelPDFException();
        }
    }

    @Override
    public String generarResumen(String texto) throws NoSePudoGenerarResumenDelPDFException {
            // creo la instruccion o prompt para pasar a la IA
            String prompt = "Generá un resumen de este texto usando formato **Markdown** " +
                    "(con encabezados, negritas y listas) para hacerlo más legible. " +
                    "El resumen debe estar estructurado en secciones:\n" + texto;

        // 2. Delegar la comunicación HTTP al cliente, que también maneja las excepciones
        try {
            String markdownResult=componentClienteGemini.generarContenido(prompt);
            return convertirMarkDownAHtml(markdownResult);
        } catch (NoSePudoGenerarResumenDelPDFException e) {
            // El cliente ya arrojó la excepción correcta, solo la propagamos.
            throw e;
        }
    }

    @Override
    public String convertirMarkDownAHtml(String markdown) {
        // 1. Crear el Parser y el Renderer sin opciones (DataSet) explícitas
        // Usamos el constructor builder() que NO requiere un MutableDataSet.
        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();

        // 2. Parsear la cadena Markdown al árbol de nodos
        // Importa el Node de flexmark si aún no lo has hecho:
        // import com.vladsch.flexmark.util.ast.Node;
        com.vladsch.flexmark.util.ast.Node document = parser.parse(markdown);

        // 2. Renderizar el árbol de nodos a una cadena HTML
        return renderer.render(document);
    }
}


