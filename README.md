# 🌱 spring-plugin-loader

`spring-plugin-loader` is a lightweight Java library that enables **dynamic plugin loading** from external JAR files in Spring-based applications.  
It isolates each plugin in its own Spring context, allowing you to load and manage modular components at runtime.

---

## ✨ Features

- 📦 Load plugins from a directory containing JARs
- 🧩 Scan Spring beans annotated with `@Component`, `@Service`, `@Repository`, or `@Configuration`
- 🛠️ Inject plugin beans into your application context
- 🗂️ Automatically load `application.properties` or `application.yml` from each plugin
- 🧱 Each plugin is isolated in its own `AnnotationConfigApplicationContext`

---

## 🚀 Getting Started

### 1. Add the dependency

If you're using **Maven**:

```xml
<dependency>
  <groupId>io.github.zorin95670</groupId>
  <artifactId>spring-plugin-loader</artifactId>
  <version>X.X.X</version>
</dependency>
````

Or with **Gradle**:

```groovy
implementation 'io.github.zorin95670:spring-plugin-loader:1.0.0'
```

> 📦 The library is compatible with Spring Boot 3.x.

---

### 2. Basic Usage

#### Step 1 – Inject the `PluginLoaderService`

```java
@Autowired
private PluginLoaderService pluginLoaderService;
```

#### Step 2 – Load plugins at runtime

```java
pluginLoaderService.loadPlugins("/path/to/plugins", List.of(MyPlugin.class));
```

#### Step 3 – Get plugin instances

```java
List<MyPlugin> plugins = pluginLoaderService.getPlugins(MyPlugin.class);
plugins.forEach(MyPlugin::doSomething);
```

### Complete example

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.boot.CommandLineRunner;

import java.util.List;

@Service
public class PluginService implements CommandLineRunner {

    @Autowired
    private PluginLoaderService pluginLoaderService;
    
    // To set in your application.property
    @Value("${plugin.loader.path}")
    private String pluginDirectoryPath;
    
    private List<MyPlugin> myPlugins;
    
    // Load plugin at start-up
    @Override
    public void run(String... args) {
        pluginLoaderService.loadPlugins(this.pluginDirectoryPath, List.of(MyPlugin.class));
        
        myPlugins = pluginLoaderService.getPlugins(MyPlugin.class);
    }
    
    public List<MyPlugin> getMyPlugins() {
        return myPlugins;
    }
}
```
---

## 🧠 How It Works

Each plugin is loaded into its own Spring `AnnotationConfigApplicationContext` that:

* Has your main application context as its parent
* Automatically scans for Spring components
* Loads its own `application.properties` or `application.yml` if present
* Can define and expose beans implementing shared interfaces

---

## 🧪 Example Plugin

```java
@Component
public class HelloPlugin implements MyPlugin {
    public void doSomething() {
        System.out.println("Hello from plugin!");
    }
}
```

---

## 🛡️ Security & Classloading

Plugins are loaded using a child-first `URLClassLoader`, ensuring isolation between plugins and the host application.

You can customize:

* Which base interfaces are scanned
* Which beans are exposed
* Plugin context lifecycle

---

## 📁 Plugin JAR Structure

Each plugin JAR may include:

```
/META-INF/
/application.yml (or application.properties)
/com/yourcompany/PluginClass.class
```

---

## 📌 License

This project is licensed under [the Apache License, Version 2.0](LICENSE).

---

## 🙋‍♂️ Questions or Contributions?

Feel free to open issues or pull requests on [GitHub](https://github.com/zorin95670/spring-plugin-loader).
Contributions are welcome!
