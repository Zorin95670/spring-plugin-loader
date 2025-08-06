package io.github.zorin95670.plugin.loader;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.lang.NonNull;
import org.springframework.plugin.core.Plugin;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service responsible for loading and managing Spring-based plugins dynamically.
 * <p>
 * Plugins are loaded from JAR files located in a given directory. The service supports
 * loading Spring beans annotated with {@link Component}, {@link Service}, {@link Repository},
 * or {@link Configuration}, and can import property files from each plugin's JAR
 * ({@code application.properties} and {@code application.yml}).
 * <p>
 * Each plugin is loaded into its own {@link AnnotationConfigApplicationContext} with
 * the main application context as its parent.
 */
@Service
public class PluginLoaderServiceImpl implements PluginLoaderService {

  /**
   * Property source name used for properties loaded from plugin application.properties files.
   */
  private static final String PROPERTY_SOURCE_PROPERTIES = "pluginProperties";

  /**
   * Property source name used for properties loaded from plugin application.yml files.
   */
  private static final String PROPERTY_SOURCE_YAML = "pluginYamlProperties";

  /**
   * Map of plugin types to loaded plugin instances.
   */
  private final Map<Class<?>, List<Object>> plugins = new HashMap<>();

  /**
   * The main Spring application context, used as parent context for plugin contexts.
   */
  private ConfigurableApplicationContext mainContext;

  /**
   * Default constructor.
   */
  public PluginLoaderServiceImpl() {

  }

  @Override
  public void setApplicationContext(final @NonNull ApplicationContext applicationContext) throws BeansException {
    this.mainContext = (ConfigurableApplicationContext) applicationContext;
  }

  @Override
  public <T> List<T> getPlugins(final Class<T> clazz) {
    List<Object> list = plugins.getOrDefault(clazz, Collections.emptyList());
    return (List<T>) list;
  }

  @Override
  public void loadPlugins(final String pluginDirectoryPath, final List<Class<?>> types) {
    plugins.clear();

    types.forEach(type -> {
      plugins.put(type, new ArrayList<>());
    });

    loadPluginsFromDirectory(new File(pluginDirectoryPath), types);
  }

  /**
   * Loads all plugin JARs from a given directory and registers beans of the
   * specified types.
   *
   * @param pluginDir the directory containing plugin JAR files
   * @param types a list of plugin interface types to register
   */
  public void loadPluginsFromDirectory(final File pluginDir, final List<Class<?>> types) {
    if (pluginDir == null || !pluginDir.isDirectory()) {
      throw new RuntimeException("Plugin directory is invalid or does not exist");
    }

    File[] jars = Optional.ofNullable(pluginDir.listFiles((dir, name) -> name.endsWith(".jar")))
        .orElse(new File[0]);

    Stream.of(jars).forEach((jar) -> loadAndRegisterPluginBeans(jar, types));
  }

  /**
   * Loads a plugin JAR and registers Spring beans matching the given types.
   *
   * @param jarFile the plugin JAR file
   * @param types a list of plugin interface types to register
   */
  public void loadAndRegisterPluginBeans(final File jarFile, final List<Class<?>> types) {
    URLClassLoader pluginClassLoader = createClassLoader(jarFile);
    if (pluginClassLoader == null) {
      return;
    }

    Set<Class<?>> annotatedClasses = findAnnotatedClasses(jarFile, pluginClassLoader);
    if (annotatedClasses.isEmpty()) {
      return;
    }

    Set<String> basePackages = annotatedClasses.stream()
        .map(clazz -> clazz.getPackage().getName())
        .collect(Collectors.toSet());

    AnnotationConfigApplicationContext pluginContext = new AnnotationConfigApplicationContext();
    pluginContext.setParent(mainContext);
    pluginContext.setClassLoader(pluginClassLoader);

    loadPropertiesIntoEnvironment(jarFile, pluginContext.getEnvironment());

    basePackages.forEach(pluginContext::scan);
    pluginContext.refresh();

    registerPlugins(pluginContext, types);
  }

  /**
   * Creates a {@link URLClassLoader} for the given plugin JAR file.
   *
   * @param jarFile the plugin JAR
   * @return the class loader for the plugin
   */
  public URLClassLoader createClassLoader(final File jarFile) {
    try {
      URL[] urls = new URL[] {jarFile.toURI().toURL()};
      return new URLClassLoader(urls, this.getClass().getClassLoader());
    } catch (MalformedURLException e) {
      throw new RuntimeException("Failed to create class loader for plugin jar", e);
    }
  }

  /**
   * Loads plugin properties from {@code application.properties} and
   * {@code application.yml} files into the given Spring environment.
   *
   * @param jarFile the plugin JAR file
   * @param environment the Spring environment to add properties to
   */
  public void loadPropertiesIntoEnvironment(final File jarFile, final ConfigurableEnvironment environment) {
    MutablePropertySources propertySources = environment.getPropertySources();

    try (JarFile jar = new JarFile(jarFile)) {
      Pattern pattern = Pattern.compile("application\\.(properties|ya?ml)");

      Collections.list(jar.entries()).stream()
          .filter(entry -> pattern.matcher(entry.getName()).matches())
          .forEach(entry -> loadSinglePropertySource(jarFile, entry, propertySources));
    } catch (IOException e) {
      System.err.printf("Error reading jar file for properties: %s\n", jarFile.getName());
    }
  }

  /**
   * Loads a single property source (properties or YAML) from the plugin JAR.
   *
   * @param jarFile the plugin JAR
   * @param entry the JAR entry representing the property file
   * @param propertySources the Spring property sources to update
   */
  public void loadSinglePropertySource(final File jarFile,
                                       final JarEntry entry,
                                       final MutablePropertySources propertySources) {
    String name = entry.getName();
    String extension = name.substring(name.lastIndexOf('.') + 1);

    try {
      URL jarUrl = jarFile.toURI().toURL();
      URL resourceUrl = URI.create("jar:" + jarUrl + "!/" + name).toURL();

      try (InputStream inputStream = resourceUrl.openStream()) {
        if ("properties".equalsIgnoreCase(extension)) {
          Properties props = new Properties();
          props.load(inputStream);
          propertySources.addLast(new PropertiesPropertySource(PROPERTY_SOURCE_PROPERTIES, props));
        } else {
          Map<String, Object> yamlMap = new Yaml().load(inputStream);
          if (yamlMap != null) {
            propertySources.addLast(new MapPropertySource(PROPERTY_SOURCE_YAML, yamlMap));
          }
        }
        System.out.printf("✅ Loaded %s from plugin: %s\n", name, jarFile.getName());
      }
    } catch (IOException e) {
      System.err.printf("Failed to load property source '%s' from plugin: %s\n", name, jarFile.getName());
    }
  }

  /**
   * Scans the plugin JAR for classes annotated with Spring stereotypes.
   *
   * @param jarFile the plugin JAR
   * @param loader the class loader for the plugin
   * @return a set of classes annotated with {@link Component}, {@link Service},
   *         {@link Repository}, or {@link Configuration}
   */
  public Set<Class<?>> findAnnotatedClasses(final File jarFile, final ClassLoader loader) {
    Set<Class<?>> annotatedClasses = new HashSet<>();

    try (JarFile jar = new JarFile(jarFile)) {
      Collections.list(jar.entries()).stream()
          .filter(entry -> !entry.isDirectory())
          .filter(entry -> entry.getName().endsWith(".class"))
          .filter(entry -> !entry.getName().contains("module-info"))
          .map(entry -> entry.getName().replace('/', '.').replace('\\', '.'))
          .map(className -> className.substring(0, className.length() - ".class".length()))
          .map(className -> {
            try {
              return Class.forName(className, false, loader);
            } catch (ClassNotFoundException e) {
              return null;
            }
          })
          .filter(Objects::nonNull)
          .filter(clazz -> clazz.isAnnotationPresent(Component.class)
              || clazz.isAnnotationPresent(Service.class)
              || clazz.isAnnotationPresent(Repository.class)
              || clazz.isAnnotationPresent(Configuration.class))
          .forEach(annotatedClasses::add);
    } catch (IOException e) {
      System.err.printf("Failed to read jar file to find annotated classes: %s\n", jarFile.getName());
    }

    return annotatedClasses;
  }

  /**
   * Registers plugin beans of specified types into the internal plugin map.
   *
   * @param pluginContext the Spring context containing plugin beans
   * @param types the list of plugin interface types to register
   */
  public void registerPlugins(final AnnotationConfigApplicationContext pluginContext, final List<Class<?>> types) {
    pluginContext.getBeansOfType(Plugin.class).forEach((name, plugin) -> {
      boolean loaded = types.stream().anyMatch((type) -> {
          if (type.isInstance(plugin)) {
            plugins.get(type).add(plugin);
            return true;
          }

          return false;
      });

      if (loaded) {
        System.out.printf("✅ Loaded plugin: %s => %s\n", name, plugin.getClass());
      }
    });
  }
}
