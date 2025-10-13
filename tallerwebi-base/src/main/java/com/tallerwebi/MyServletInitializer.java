package com.tallerwebi;

import com.tallerwebi.config.DatabaseInitializationConfig;
import com.tallerwebi.config.HibernateConfig;
import com.tallerwebi.config.SpringWebConfig;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletRegistration;

public class MyServletInitializer
        extends AbstractAnnotationConfigDispatcherServletInitializer {

    // services and data sources
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[0];
    }

    // controller, view resolver, handler mapping
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{SpringWebConfig.class, HibernateConfig.class, DatabaseInitializationConfig.class};
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    // Configuración para subir archivos
    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {
        // Definimos un tamaño máximo de archivo y de request
        MultipartConfigElement multipartConfig = new MultipartConfigElement(
                null,               // directorio temporal (null usa el default)
                10_000_000,         // tamaño máximo de archivo (10 MB)
                20_000_000,         // tamaño máximo de request (20 MB)
                0                   // tamaño de umbral en memoria
        );
        registration.setMultipartConfig(multipartConfig);
    }

}
