package com.app;

/**
 * Lanzador independiente de la aplicación (Evita el error de FXHelper en JDK 11+).
 *
 * <p>Al no extender de {@link javafx.application.Application}, el cargador de la JVM
 * no activa la validación restrictiva de módulos JavaFX y permite ejecutar la aplicación
 * directamente desde cualquier IDE (IntelliJ IDEA, Eclipse, VS Code) o línea de comandos
 * sin necesidad de configurar argumentos VM manuales (--module-path o --add-modules).</p>
 */
public class Launcher {

    public static void main(String[] args) {
        MainApp.main(args);
    }
}
