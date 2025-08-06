/**
 * Module for the Spring plugin loader.
 *
 * <p>Provides dynamic plugin loading capabilities and integration
 * with Spring ApplicationContext.
 */
module io.github.zorin95670 {
    exports io.github.zorin95670.plugin.loader;

    requires spring.context;
    requires spring.core;
    requires spring.beans;
    requires spring.plugin.core;
    requires org.yaml.snakeyaml;
}
