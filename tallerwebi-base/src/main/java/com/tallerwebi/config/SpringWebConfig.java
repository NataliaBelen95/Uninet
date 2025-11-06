package com.tallerwebi.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;


@EnableWebMvc
@Configuration
@ComponentScan({"com.tallerwebi.presentacion", "com.tallerwebi.dominio", "com.tallerwebi.infraestructura", "com.tallerwebi.config"})
@PropertySource("classpath:application.properties")
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class SpringWebConfig implements WebMvcConfigurer, org.springframework.scheduling.annotation.AsyncConfigurer {

    // Spring + Thymeleaf need this
    @Autowired
    private ApplicationContext applicationContext;



    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**").addResourceLocations("/resources/core/css/");
        registry.addResourceHandler("/js/**").addResourceLocations("/resources/core/js/");
        registry.addResourceHandler("/imagenes/**").addResourceLocations("/resources/core/imagenes/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("/webjars/");
        //registry.addResourceHandler("/archivos_pdf/**").addResourceLocations("/resources/core/archivos_pdf/");
        // registry.addResourceHandler("/archivos_pdf/**").addResourceLocations("C:/Users/rocam/OneDrive/Escritorio/TALLER WEB 1/Uninet/tallerwebi-base/archivos_pdf");
        registry.addResourceHandler("/archivos_pdf/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/archivos_pdf/");
        registry.addResourceHandler("/archivosPublicacion/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/archivosPublicacion/");
        registry.addResourceHandler("/perfiles/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/perfiles/");
        registry.addResourceHandler("/imagenesPublicidad/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/imagenesPublicidad/");
    }

    // https://www.thymeleaf.org/doc/tutorials/3.0/thymeleafspring.html
    // Spring + Thymeleaf
    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        // SpringResourceTemplateResolver automatically integrates with Spring's own
        // resource resolution infrastructure, which is highly recommended.
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setApplicationContext(this.applicationContext);
        templateResolver.setPrefix("/WEB-INF/views/thymeleaf/");
        templateResolver.setSuffix(".html");
        // HTML is the default value, added here for the sake of clarity.
        templateResolver.setTemplateMode(TemplateMode.HTML);
        // Template cache is true by default. Set to false ifc you want
        // templates to be automatically updated when modified.
        templateResolver.setCacheable(true);
        return templateResolver;
    }

    // Spring + Thymeleaf
    @Bean
    public SpringTemplateEngine templateEngine() {
        // SpringTemplateEngine automatically applies SpringStandardDialect and
        // enables Spring's own MessageSource message resolution mechanisms.
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());
        // Enabling the SpringEL compiler with Spring 4.2.4 or newer can
        // speed up execution in most scenarios, but might be incompatible
        // with specific cases when expressions in one template are reused
        // across different data types, so this flag is "false" by default
        // for safer backwards compatibility.
        templateEngine.setEnableSpringELCompiler(true);
        return templateEngine;
    }
    // Spring + Thymeleaf
    // Configure Thymeleaf View Resolver
    @Bean
    public ThymeleafViewResolver viewResolver() {
        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
        viewResolver.setTemplateEngine(templateEngine());
        return viewResolver;
    }

    @Bean
    public MultipartResolver multipartResolver() {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        multipartResolver.setMaxUploadSize(10 * 1024 * 1024); // 10 MB por ejemplo
        multipartResolver.setDefaultEncoding("utf-8");
        return multipartResolver;
    }

//para que permita subir archivos pdf
//    @Bean
//    public MultipartResolver multipartResolver() {
//        return new StandardServletMultipartResolver();
//    }

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername("fran.caco.fv@gmail.com");
        mailSender.setPassword("gjtp nwjm eshu rndf");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    @Bean(name = "geminiTaskExecutor")
    public Executor geminiTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Hilos base que siempre están activos (ajusta este valor según tu carga)
        executor.setCorePoolSize(5);

        // Hilos máximos que se pueden crear para manejar picos de demanda
        executor.setMaxPoolSize(25);

        // Tareas que esperan en cola cuando todos los hilos están ocupados
        executor.setQueueCapacity(500);

        // Prefijo para los hilos (ayuda en el debug)
        executor.setThreadNamePrefix("Gemini-Async-");

        executor.initialize();
        return executor;
    }
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }
    @Bean
    @Qualifier("objectMapperGemini")
    public ObjectMapper objectMapperGemini() {
        return new ObjectMapper();
    }
    // Método que proporciona el ejecutor asíncrono (Usará el TaskExecutor)
    @Override
    public Executor getAsyncExecutor() {
        return geminiTaskExecutor();
    }

    // Método que maneja las excepciones no capturadas de los hilos @Async
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, params) -> {
            System.err.println("------------------------------------------------------------------------");
            System.err.println("🚨 EXCEPCIÓN ASÍNCRONA NO CAPTURADA EN GEMINI SERVICE 🚨");
            System.err.println("Método fallido: " + method.getName());
            System.err.println("Causa: " + throwable.getMessage());
            throwable.printStackTrace(System.err);
            System.err.println("------------------------------------------------------------------------");
        };
    }

}