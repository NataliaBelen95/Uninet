package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

public class VistaNuevoUsuario extends VistaWeb {
    // ... constructor ...

    public VistaNuevoUsuario(Page page) {
        super(page);
    }

    public void escribirEMAIL(String email) {
        page.fill("#email", email);
    }

    public void escribirClave(String clave) {
        page.fill("#password", clave);
    }

    public void escribirNombre(String nombre) {
        page.fill("#nombre", nombre);
    }

    public void escribirApellido(String apellido) {
        page.fill("#apellido", apellido);
    }

    public void escribirDNI(String dni) {
        page.fill("#dni", dni);
    }

    public void escribirFechaNacimiento(String fecha) {

        page.fill("#fecha", fecha);
    }
    public void escribirEmailOculto(String email) {
        String selector = "input[name='email']";

        // Inyección de JavaScript nativa para cambiar el valor del campo oculto
        // Argumento 0: selector CSS
        // Argumento 1: el valor a establecer
        page.evaluate("([selector, value]) => {" +
                "  const element = document.querySelector(selector);" +
                "  if (element) { element.value = value; }" +
                "}", new Object[]{selector, email});
    }
    public void seleccionarDepartamento(String idDepartamento) {
        page.selectOption("#departamento", idDepartamento);
        page.dispatchEvent("#departamento", "change");
        //porque esta hidden
        page.waitForSelector("#carrera option[value='1']",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED));
    }

    public void seleccionarCarrera(String idCarrera) {

        page.selectOption("#carrera", idCarrera);
    }
    public void escribirCodigo(String codigo) {
        // Asumiendo que el campo de código tiene el ID 'codigo' o similar
        page.fill("#codigo", codigo);
    }

    public void darClickEnValidarCodigo() {
        // Asumiendo que el botón de validación tiene el ID 'btn-validar' o similar
        page.click("button:has-text('Confirmar')");
    }
    public void darClickEnRegistrarme() {
        page.click("#btn-registrarme");
    }
}