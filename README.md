# Sistema Profesional de Gestión y Organizador de Estudiantes (CRUD JavaFX)

Aplicación de escritorio nativa profesional desarrollada en **Java** y **JavaFX**, siguiendo el patrón arquitectónico **MVC (Modelo - Vista - Controlador)** desacoplado, con enlace de datos reactivo y almacenamiento de datos en memoria sin persistencia externa.

Ahora cuenta con un **Organizador Académico Multidimensional** que estructura y divide a los estudiantes por **Programa Académico (Carrera)** y por **Semestre (1 al 10)**.

---

## 1. Arquitectura del Proyecto (Patrón MVC Desacoplado)

```text
Proyecto CRUD Java/
├── .mvn/
│   └── maven.config                      # Configuración de transporte y red para Maven
├── .idea/                                # Configuración del espacio de trabajo IntelliJ IDEA
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── app/
│       │           ├── Launcher.java                   # Lanzador desacoplado (Bypass FXHelper en JDK 11+)
│       │           ├── MainApp.java                    # Punto de entrada de la aplicación JavaFX (Lifecycle)
│       │           ├── controller/
│       │           │   └── MainController.java         # Controlador (Initializable, TreeView, Bindings, Eventos, CRUD)
│       │           └── model/
│       │               ├── Entidad.java                # Modelo con JavaFX Properties reactivas (Programa, Semestre, etc.)
│       │               └── RepositorioMemoria.java     # Repositorio en memoria (ArrayList + ObservableList)
│       └── resources/
│           └── com/
│               └── app/
│                   ├── view/
│                   │   └── main-view.fxml              # Vista FXML moderna con Organizador Jerárquico (SplitPane)
│                   └── styles/
│                       └── styles.css                  # Hoja de estilos Dark Slate (Cero gris plano nativo)
├── ejecutar.bat                          # Script de ejecución con un clic para Windows
├── pom.xml                               # Configuración de dependencias y plugins Maven (JavaFX 21)
└── README.md                             # Documentación exhaustiva del sistema
```

### Justificación Técnica de la Estructura:
1. **Separación de Responsabilidades**:
   - `model/`: Contiene las clases del dominio y lógica de datos pura (`Entidad`, `RepositorioMemoria`), sin acoplamiento a elementos de UI.
   - `controller/`: Orquesta la interacción del usuario, gestiona el `TreeView` de programas y semestres, realiza validaciones en tiempo real y actualiza el modelo.
   - `view/`: Archivo FXML declarativo con `SplitPane` horizontal que integra la jerarquía académica y la tabla de datos.
   - `styles/`: Hoja de estilos CSS aislada con variables semánticas, badges de semestres/estados y temas oscuros profesionales.
2. **Inyección de Dependencias**:
   - `MainApp` instancia el `RepositorioMemoria` y lo inyecta en `MainController` mediante un `ControllerFactory` en `FXMLLoader`.

---

## 2. Nuevo Organizador por Programa y por Semestre

En lugar de un inventario plano y desordenado, la interfaz ahora divide a los estudiantes con una doble vía de navegación:

1. **Estructura Jerárquica en Árbol (`TreeView`)**:
   - Muestra la Universidad dividida por cada **Programa Académico** (`💻 Ingeniería de Software`, `⚙️ Ingeniería de Sistemas`, `🎨 Diseño Gráfico`, etc.).
   - Dentro de cada programa, desglosa los **Semestres** activos (`📘 1° Semestre`, `📘 4° Semestre`, etc.) con el conteo en tiempo real de estudiantes matriculados.
   - Al hacer clic en cualquier programa o semestre del árbol, la tabla y los filtros se sincronizan automáticamente.
2. **Barra de Filtros Combinados**:
   - **Selector de Programa**: Permite alternar entre "Todos los Programas" o un programa puntual.
   - **Selector de Semestre**: Permite filtrar un semestre específico (del 1° al 10°) o ver todos.
   - **Buscador en Vivo (`TextField`)**: Búsqueda reactiva por código, nombre o estado dentro del grupo seleccionado.
   - **Botón Restablecer**: Vuelve a la vista general con un solo clic.
3. **Formulario CRUD Adaptado**:
   - Ahora incluye el selector obligatorio de **Semestre Académico** (1° al 10°).
   - Validaciones en vivo para evitar registros incompletos.
4. **Tabla Enriquecida con Badges**:
   - Columna exclusiva para **Semestre** con badge visual índigo (`1° Semestre`, `4° Semestre`, etc.).
   - Columna para **Programa Académico**.
   - Columna para **Estado** (Activo, Inactivo, Graduado).

---

## 3. Funcionamiento de la Reactividad Estricta

Bajo ninguna circunstancia se recarga la ventana (`Stage` o `Scene`). La aplicación responde instantáneamente mediante:

1. **JavaFX Properties (`StringProperty`, `IntegerProperty`, `DoubleProperty`)**:
   - El modelo `Entidad` expone propiedades observables.
   - Cada columna de la `TableView` se asocia con `setCellValueFactory`.
2. **Extractor de Propiedades**:
   - `FXCollections.observableList(almacenamiento, Entidad.extractor())`: Cualquier edición sobre un estudiante emite un evento que recalcula el árbol de programas y semestres, los conteos y las métricas.
3. **Métricas y KPIs Reactivas**:
   - Las tarjetas superiores calculan en vivo:
     * Estudiantes visibles en el filtro actual
     * Activos en esa cohorte
     * Promedio del grupo seleccionado
4. **Carga Automática y Actualización en Sitio**:
   - Al hacer clic en cualquier fila de la tabla, se cargan todos los campos (incluyendo el semestre) en el formulario y el botón cambia dinámicamente a "Actualizar Registro" sin duplicar objetos.

---

## 4. Instrucciones de Compilación y Ejecución

### Opción A: Ejecución con Un Solo Clic (Recomendada en Windows)
Haz doble clic sobre el archivo:
```text
ejecutar.bat
```

---

### Opción B: Desde IntelliJ IDEA
1. Abre el archivo **`Launcher.java`** en `src/main/java/com/app/Launcher.java`.
2. Haz clic en el botón verde de **Play** (`Run 'Launcher.main()'`).

---

### Opción C: Desde la Terminal (PowerShell / CMD)
```powershell
$env:JAVA_HOME = "C:\Users\USUARIO\.jdks\jbr-25.0.4.1"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
& "C:\Users\USUARIO\Desktop\IntelliJ IDEA 2026.2.0.1\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" compile javafx:run
```
