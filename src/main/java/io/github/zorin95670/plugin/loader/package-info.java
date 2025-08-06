/**
 * Provides the core plugin loading infrastructure for dynamically registering Spring-based plugins.
 *
 * <p>This package includes:
 * <ul>
 *   <li>Support for loading plugins from external JAR files</li>
 *   <li>Integration with Spring's {@link org.springframework.context.ApplicationContext}</li>
 *   <li>Automatic scanning of components annotated with {@code @Component}, {@code @Service}, {@code @Repository},
 *   and {@code @Configuration}</li>
 *   <li>Support for loading plugin-specific {@code application.properties} and {@code application.yml}</li>
 * </ul>
 *
 * <p>Plugins are loaded into isolated {@link org.springframework.context.annotation.AnnotationConfigApplicationContext}
 * instances, allowing them to be modular and context-aware.
 */
package io.github.zorin95670.plugin.loader;
