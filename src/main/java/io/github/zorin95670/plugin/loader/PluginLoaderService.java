package io.github.zorin95670.plugin.loader;

import java.util.List;

import org.springframework.context.ApplicationContextAware;

/**
 * Service responsible for dynamically loading and managing Spring-based plugins.
 *
 * <p>Plugins are loaded from JAR files located in a specified directory. This service detects Spring beans annotated
 * with Component, Service, Repository, or Configuration, and can automatically import property files from each
 * plugin's JAR ({@code application.properties} and {@code application.yml}).
 *
 * <p>Each plugin is loaded within its own AnnotationConfigApplicationContext, with the main application context set as
 * its parent to allow dependency sharing.
 */
public interface PluginLoaderService extends ApplicationContextAware {

  /**
   * Loads plugins from the specified directory and registers instances matching
   * the given types.
   *
   * @param pluginDirectoryPath the path to the directory containing plugin JARs
   * @param types a list of plugin interface types to load
   */
  void loadPlugins(String pluginDirectoryPath, List<Class<?>> types);

  /**
   * Returns all loaded plugin instances of the specified type.
   *
   * @param clazz the plugin interface type
   * @param <T> the type parameter
   * @return a list of plugin instances, or an empty list if none were loaded
   */
  <T> List<T> getPlugins(Class<T> clazz);
}
