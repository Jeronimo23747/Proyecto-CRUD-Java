package com.app;

import com.app.controller.MainController;
import com.app.model.RepositorioMemoria;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * Punto de entrada de la aplicación JavaFX.
 *
 * <p>Responsabilidades:</p>
 * <ol>
 *     <li>Crear el repositorio en memoria (Modelo).</li>
 *     <li>Cargar la vista FXML e inyectar el repositorio en el Controlador
 *         mediante una <i>controller factory</i> (inyección de dependencias).</li>
 *     <li>Aplicar la hoja de estilos y mostrar la ventana principal.</li>
 * </ol>
 */
public class MainApp extends Application {

    private static final String RUTA_VISTA = "/com/app/view/main-view.fxml";
    private static final String RUTA_ESTILOS = "/com/app/styles/styles.css";

    @Override
    public void start(Stage stage) throws IOException {
        // 1. Modelo: datos en memoria (ArrayList + ObservableList)
        RepositorioMemoria repositorio = new RepositorioMemoria();
        repositorio.cargarDatosDemostracion();

        // 2. Vista + Controlador: el controlador recibe el repositorio por constructor
        FXMLLoader loader = new FXMLLoader(recurso(RUTA_VISTA));
        loader.setControllerFactory(tipo -> {
            if (tipo == MainController.class) {
                return new MainController(repositorio);
            }
            try {
                return tipo.getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("No se pudo crear el controlador " + tipo.getName(), e);
            }
        });
        Parent raiz = loader.load();

        // 3. Escena con estilos. La hoja también se declara en el FXML (para la
        //    vista previa de Scene Builder); registrarla en la Scene garantiza que
        //    ventanas emergentes (ComboBox, menús contextuales, tooltips) la hereden.
        Scene escena = new Scene(raiz, 1360, 820);
        escena.getStylesheets().add(recurso(RUTA_ESTILOS).toExternalForm());

        stage.setTitle("Sistema de Gestión de Estudiantes · CRUD JavaFX");
        stage.setMinWidth(1150);
        stage.setMinHeight(700);
        stage.setScene(escena);
        stage.show();
    }

    private static URL recurso(String ruta) {
        return Objects.requireNonNull(MainApp.class.getResource(ruta),
                () -> "No se encontró el recurso: " + ruta);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
