package com.app.controller;

import com.app.model.Entidad;
import com.app.model.RepositorioMemoria;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Controlador principal de la aplicación.
 *
 * <p>Implementa un organizador académico por <b>Programa</b> y por <b>Semestre</b>,
 * con navegación jerárquica mediante {@link TreeView}, filtros reactivos combinados
 * (Programa, Semestre y Búsqueda en vivo), métricas dinámicas y operaciones CRUD
 * 100% en memoria con enlace reactivo a JavaFX Properties.</p>
 */
public class MainController implements Initializable {

    public static final String TODOS_LOS_PROGRAMAS = "🎓 Todos los Programas";
    public static final String TODOS_LOS_SEMESTRES = "📚 Todos los Semestres";

    // ------------------------------------------------------------------
    // Dependencias y Estado del Formulario
    // ------------------------------------------------------------------
    private final RepositorioMemoria repositorio;
    private Entidad entidadEnEdicion = null;
    private FilteredList<Entidad> listaFiltrada;

    // ------------------------------------------------------------------
    // Elementos FXML - Formulario de Gestión
    // ------------------------------------------------------------------
    @FXML private Label lblModoFormulario;
    @FXML private Label lblMensajeFeedback;

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private ComboBox<String> cmbCarrera;
    @FXML private ComboBox<String> cmbSemestre;
    @FXML private TextField txtPromedio;
    @FXML private ComboBox<String> cmbEstado;

    @FXML private Label errCodigo;
    @FXML private Label errNombre;
    @FXML private Label errCarrera;
    @FXML private Label errSemestre;
    @FXML private Label errPromedio;
    @FXML private Label errEstado;

    @FXML private Button btnGuardar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnEliminar;

    // ------------------------------------------------------------------
    // Elementos FXML - Filtros Superiores y Búsqueda
    // ------------------------------------------------------------------
    @FXML private ComboBox<String> cmbFiltroPrograma;
    @FXML private ComboBox<String> cmbFiltroSemestre;
    @FXML private TextField txtBuscar;
    @FXML private Button btnLimpiarFiltros;
    @FXML private Label lblInfoFiltroActivo;

    // ------------------------------------------------------------------
    // Elementos FXML - Organizador Jerárquico (Árbol) y Tabla
    // ------------------------------------------------------------------
    @FXML private TreeView<String> treeOrganizador;

    @FXML private TableView<Entidad> tblEstudiantes;
    @FXML private TableColumn<Entidad, String> colCodigo;
    @FXML private TableColumn<Entidad, String> colNombre;
    @FXML private TableColumn<Entidad, String> colCarrera;
    @FXML private TableColumn<Entidad, Number> colSemestre;
    @FXML private TableColumn<Entidad, Number> colPromedio;
    @FXML private TableColumn<Entidad, String> colEstado;

    // ------------------------------------------------------------------
    // Elementos FXML - Dashboard / Métricas en Vivo
    // ------------------------------------------------------------------
    @FXML private Label lblTotalEstudiantes;
    @FXML private Label lblEstudiantesActivos;
    @FXML private Label lblPromedioGeneral;

    // Bandera para evitar bucles entre eventos del TreeView y los ComboBoxes
    private boolean actualizandoFiltrosDesdeArbol = false;

    // ------------------------------------------------------------------
    // Constructores
    // ------------------------------------------------------------------
    public MainController(RepositorioMemoria repositorio) {
        this.repositorio = repositorio;
    }

    public MainController() {
        this(new RepositorioMemoria());
    }

    // ------------------------------------------------------------------
    // Inicialización del Ciclo de Vida JavaFX
    // ------------------------------------------------------------------
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarCatalogosFormulario();
        configurarColumnasTabla();
        configurarFiltrosYBusqueda();
        configurarArbolOrganizador();
        configurarSeleccionTabla();
        configurarValidacionesEnTiempoReal();
        configurarMetricasReactiva();
        limpiarFormulario();
        aplicarFiltrosCombinados();
    }

    // ------------------------------------------------------------------
    // Configuración de Catálogos del Formulario
    // ------------------------------------------------------------------
    private void configurarCatalogosFormulario() {
        cmbCarrera.getItems().setAll(Entidad.CARRERAS);

        cmbSemestre.getItems().clear();
        for (int i = Entidad.SEMESTRE_MINIMO; i <= Entidad.SEMESTRE_MAXIMO; i++) {
            cmbSemestre.getItems().add(i + "° Semestre");
        }

        cmbEstado.getItems().setAll(Entidad.ESTADOS);
        cmbEstado.getSelectionModel().select(Entidad.ESTADO_ACTIVO);

        // Catálogos para los selectores de filtro superior
        cmbFiltroPrograma.getItems().add(TODOS_LOS_PROGRAMAS);
        cmbFiltroPrograma.getItems().addAll(Entidad.CARRERAS);
        cmbFiltroPrograma.getSelectionModel().select(TODOS_LOS_PROGRAMAS);

        cmbFiltroSemestre.getItems().add(TODOS_LOS_SEMESTRES);
        for (int i = Entidad.SEMESTRE_MINIMO; i <= Entidad.SEMESTRE_MAXIMO; i++) {
            cmbFiltroSemestre.getItems().add(i + "° Semestre");
        }
        cmbFiltroSemestre.getSelectionModel().select(TODOS_LOS_SEMESTRES);
    }

    // ------------------------------------------------------------------
    // Configuración de Columnas de la TableView
    // ------------------------------------------------------------------
    private void configurarColumnasTabla() {
        colCodigo.setCellValueFactory(cellData -> cellData.getValue().codigoProperty());
        colNombre.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());
        colCarrera.setCellValueFactory(cellData -> cellData.getValue().carreraProperty());
        colSemestre.setCellValueFactory(cellData -> cellData.getValue().semestreProperty());
        colPromedio.setCellValueFactory(cellData -> cellData.getValue().promedioProperty());
        colEstado.setCellValueFactory(cellData -> cellData.getValue().estadoProperty());

        // Badge estilizado para Semestre (ej: "4° Sem")
        colSemestre.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    getStyleClass().remove("badge-semestre");
                } else {
                    setText(item.intValue() + "° Semestre");
                    if (!getStyleClass().contains("badge-semestre")) {
                        getStyleClass().add("badge-semestre");
                    }
                }
            }
        });

        // Formato con 2 decimales para el Promedio
        colPromedio.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : String.format(Locale.US, "%.2f", item.doubleValue()));
            }
        });

        // Badge visual semántico para Estado
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("badge-activo", "badge-inactivo", "badge-graduado");
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    if (Entidad.ESTADO_ACTIVO.equalsIgnoreCase(item)) {
                        getStyleClass().add("badge-activo");
                    } else if (Entidad.ESTADO_INACTIVO.equalsIgnoreCase(item)) {
                        getStyleClass().add("badge-inactivo");
                    } else {
                        getStyleClass().add("badge-graduado");
                    }
                }
            }
        });
    }

    // ------------------------------------------------------------------
    // Reactividad: Filtros Combinados (Programa + Semestre + Texto)
    // ------------------------------------------------------------------
    private void configurarFiltrosYBusqueda() {
        listaFiltrada = new FilteredList<>(repositorio.listar(), p -> true);

        // Listeners en tiempo real para todos los criterios
        cmbFiltroPrograma.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!actualizandoFiltrosDesdeArbol) {
                sincronizarArbolDesdeFiltros();
            }
            aplicarFiltrosCombinados();
        });

        cmbFiltroSemestre.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!actualizandoFiltrosDesdeArbol) {
                sincronizarArbolDesdeFiltros();
            }
            aplicarFiltrosCombinados();
        });

        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltrosCombinados());

        // Ordenamiento natural por Programa y luego por Semestre ascendente
        SortedList<Entidad> listaOrdenada = new SortedList<>(listaFiltrada);
        Comparator<Entidad> comparadorPorDefecto = Comparator
                .comparing(Entidad::getCarrera)
                .thenComparingInt(Entidad::getSemestre)
                .thenComparing(Entidad::getCodigo);

        listaOrdenada.setComparator(comparadorPorDefecto);
        tblEstudiantes.comparatorProperty().addListener((obs, oldComp, newComp) -> {
            listaOrdenada.setComparator(Objects.requireNonNullElse(newComp, comparadorPorDefecto));
        });

        tblEstudiantes.setItems(listaOrdenada);
    }

    private void aplicarFiltrosCombinados() {
        String progSeleccionado = cmbFiltroPrograma.getValue();
        String semSeleccionado = cmbFiltroSemestre.getValue();
        String texto = txtBuscar.getText() != null ? txtBuscar.getText().trim().toLowerCase() : "";

        Integer semestreFiltro = extraerNumeroSemestre(semSeleccionado);

        listaFiltrada.setPredicate(est -> {
            // 1. Filtro por Programa
            if (progSeleccionado != null && !TODOS_LOS_PROGRAMAS.equals(progSeleccionado)) {
                if (!est.getCarrera().equalsIgnoreCase(progSeleccionado)) {
                    return false;
                }
            }

            // 2. Filtro por Semestre
            if (semestreFiltro != null) {
                if (est.getSemestre() != semestreFiltro) {
                    return false;
                }
            }

            // 3. Filtro por Texto en vivo (código, nombre, carrera, estado)
            if (!texto.isEmpty()) {
                boolean coincide = est.getCodigo().toLowerCase().contains(texto)
                        || est.getNombre().toLowerCase().contains(texto)
                        || est.getCarrera().toLowerCase().contains(texto)
                        || est.getEstado().toLowerCase().contains(texto)
                        || (est.getSemestre() + " semestre").contains(texto);
                if (!coincide) return false;
            }

            return true;
        });

        actualizarBannerInformativo(progSeleccionado, semSeleccionado);
        actualizarMetricas();
    }

    private Integer extraerNumeroSemestre(String texto) {
        if (texto == null || TODOS_LOS_SEMESTRES.equals(texto)) {
            return null;
        }
        try {
            String soloDigitos = texto.replaceAll("[^0-9]", "");
            return soloDigitos.isEmpty() ? null : Integer.parseInt(soloDigitos);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void actualizarBannerInformativo(String programa, String semestre) {
        int visibles = listaFiltrada.size();
        int total = repositorio.contar();

        String progStr = (programa == null || TODOS_LOS_PROGRAMAS.equals(programa))
                ? "Todos los Programas"
                : programa;

        String semStr = (semestre == null || TODOS_LOS_SEMESTRES.equals(semestre))
                ? "Todos los Semestres"
                : semestre;

        lblInfoFiltroActivo.setText(String.format("Mostrando %d de %d estudiantes · [%s] · [%s]",
                visibles, total, progStr, semStr));
    }

    // ------------------------------------------------------------------
    // Organizador Visual: Árbol Jerárquico por Programa y Semestre
    // ------------------------------------------------------------------
    private void configurarArbolOrganizador() {
        actualizarArbolOrganizador();

        // Selección en el árbol sincroniza filtros superiores y tabla
        treeOrganizador.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, nuevo) -> {
            if (nuevo == null) return;

            actualizandoFiltrosDesdeArbol = true;
            try {
                if (nuevo.getParent() == null) {
                    // Raíz: Todos los programas y semestres
                    cmbFiltroPrograma.setValue(TODOS_LOS_PROGRAMAS);
                    cmbFiltroSemestre.setValue(TODOS_LOS_SEMESTRES);
                } else if (nuevo.getParent().getParent() == null) {
                    // Nodo Programa
                    String nombrePrograma = limpiarEtiquetaNodo(nuevo.getValue());
                    cmbFiltroPrograma.setValue(nombrePrograma);
                    cmbFiltroSemestre.setValue(TODOS_LOS_SEMESTRES);
                } else {
                    // Nodo Semestre
                    String nombrePrograma = limpiarEtiquetaNodo(nuevo.getParent().getValue());
                    String etiquetaSemestre = limpiarEtiquetaNodo(nuevo.getValue());
                    cmbFiltroPrograma.setValue(nombrePrograma);
                    cmbFiltroSemestre.setValue(etiquetaSemestre);
                }
            } finally {
                actualizandoFiltrosDesdeArbol = false;
            }
            aplicarFiltrosCombinados();
        });
    }

    /**
     * Construye y actualiza la jerarquía del árbol con conteos reactivos:
     * Universidad -> Programas -> Semestres
     */
    private void actualizarArbolOrganizador() {
        TreeItem<String> raiz = new TreeItem<>("🏛️ Universidad (" + repositorio.contar() + ")");
        raiz.setExpanded(true);

        // Agrupamos estudiantes por Programa y luego por Semestre
        Map<String, Map<Integer, List<Entidad>>> agrupado = repositorio.listar().stream()
                .collect(Collectors.groupingBy(
                        Entidad::getCarrera,
                        TreeMap::new,
                        Collectors.groupingBy(Entidad::getSemestre, TreeMap::new, Collectors.toList())
                ));

        // Construir nodos de árbol para cada programa
        for (String programa : Entidad.CARRERAS) {
            Map<Integer, List<Entidad>> semestresDelPrograma = agrupado.getOrDefault(programa, Map.of());
            int totalEnPrograma = semestresDelPrograma.values().stream().mapToInt(List::size).sum();

            TreeItem<String> nodoPrograma = new TreeItem<>(
                    obtenerIconoPrograma(programa) + " " + programa + " (" + totalEnPrograma + ")"
            );
            nodoPrograma.setExpanded(true);

            // Sub-nodos para los semestres con estudiantes
            for (Map.Entry<Integer, List<Entidad>> entry : semestresDelPrograma.entrySet()) {
                int sem = entry.getKey();
                int cant = entry.getValue().size();
                TreeItem<String> nodoSemestre = new TreeItem<>("📘 " + sem + "° Semestre (" + cant + ")");
                nodoPrograma.getChildren().add(nodoSemestre);
            }

            raiz.getChildren().add(nodoPrograma);
        }

        treeOrganizador.setRoot(raiz);
    }

    private String obtenerIconoPrograma(String carrera) {
        if (carrera.contains("Software") || carrera.contains("Sistemas")) return "💻";
        if (carrera.contains("Industrial")) return "⚙️";
        if (carrera.contains("Diseño")) return "🎨";
        if (carrera.contains("Administración")) return "📊";
        if (carrera.contains("Contaduría")) return "📈";
        if (carrera.contains("Psicología")) return "🧠";
        if (carrera.contains("Derecho")) return "⚖️";
        return "🎓";
    }

    private String limpiarEtiquetaNodo(String valor) {
        if (valor == null) return "";
        // Remueve emojis del inicio y el conteo final "(X)"
        String limpio = valor.replaceAll("^[^\s]+\\s*", "");
        int indexParentesis = limpio.lastIndexOf("(");
        if (indexParentesis > 0) {
            limpio = limpio.substring(0, indexParentesis).trim();
        }
        return limpio;
    }

    private void sincronizarArbolDesdeFiltros() {
        String prog = cmbFiltroPrograma.getValue();
        String sem = cmbFiltroSemestre.getValue();

        if (treeOrganizador.getRoot() == null) return;

        if (TODOS_LOS_PROGRAMAS.equals(prog) || prog == null) {
            treeOrganizador.getSelectionModel().select(treeOrganizador.getRoot());
            return;
        }

        // Buscar el nodo correspondiente en el árbol
        for (TreeItem<String> nodoProg : treeOrganizador.getRoot().getChildren()) {
            if (limpiarEtiquetaNodo(nodoProg.getValue()).equalsIgnoreCase(prog)) {
                if (TODOS_LOS_SEMESTRES.equals(sem) || sem == null) {
                    treeOrganizador.getSelectionModel().select(nodoProg);
                } else {
                    for (TreeItem<String> nodoSem : nodoProg.getChildren()) {
                        if (limpiarEtiquetaNodo(nodoSem.getValue()).equalsIgnoreCase(sem)) {
                            treeOrganizador.getSelectionModel().select(nodoSem);
                            return;
                        }
                    }
                    treeOrganizador.getSelectionModel().select(nodoProg);
                }
                return;
            }
        }
    }

    // ------------------------------------------------------------------
    // Reactividad: Carga de Fila en Formulario (Edición)
    // ------------------------------------------------------------------
    private void configurarSeleccionTabla() {
        tblEstudiantes.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, seleccionado) -> {
            if (seleccionado != null) {
                cargarEnFormulario(seleccionado);
            }
        });

        btnEliminar.disableProperty().bind(tblEstudiantes.getSelectionModel().selectedItemProperty().isNull());
    }

    private void cargarEnFormulario(Entidad e) {
        entidadEnEdicion = e;
        txtCodigo.setText(e.getCodigo());
        txtCodigo.setDisable(true);
        txtNombre.setText(e.getNombre());
        cmbCarrera.getSelectionModel().select(e.getCarrera());
        cmbSemestre.getSelectionModel().select(e.getSemestre() + "° Semestre");
        txtPromedio.setText(String.format(Locale.US, "%.2f", e.getPromedio()));
        cmbEstado.getSelectionModel().select(e.getEstado());

        lblModoFormulario.setText("Modo: Editando [" + e.getCodigo() + "]");
        lblModoFormulario.getStyleClass().setAll("label-modo", "modo-edicion");
        btnGuardar.setText("Actualizar Registro");
        btnGuardar.getStyleClass().setAll("button", "btn-actualizar");
        mostrarFeedback("Estudiante cargado en formulario. Modifique los campos y presione 'Actualizar'.", false);
    }

    // ------------------------------------------------------------------
    // Validaciones en Tiempo Real
    // ------------------------------------------------------------------
    private void configurarValidacionesEnTiempoReal() {
        txtCodigo.textProperty().addListener((obs, o, n) -> {
            if (n != null && !n.equals(n.toUpperCase())) {
                txtCodigo.setText(n.toUpperCase());
            }
            validarCodigo();
        });

        txtNombre.textProperty().addListener((obs, o, n) -> validarNombre());
        cmbCarrera.valueProperty().addListener((obs, o, n) -> validarCarrera());
        cmbSemestre.valueProperty().addListener((obs, o, n) -> validarSemestre());
        txtPromedio.textProperty().addListener((obs, o, n) -> validarPromedio());
        cmbEstado.valueProperty().addListener((obs, o, n) -> validarEstado());

        BooleanBinding invalido = Bindings.createBooleanBinding(
                () -> !esFormularioValido(),
                txtCodigo.textProperty(),
                txtNombre.textProperty(),
                cmbCarrera.valueProperty(),
                cmbSemestre.valueProperty(),
                txtPromedio.textProperty(),
                cmbEstado.valueProperty()
        );
        btnGuardar.disableProperty().bind(invalido);
    }

    private boolean validarCodigo() {
        String c = txtCodigo.getText() != null ? txtCodigo.getText().trim() : "";
        if (entidadEnEdicion != null) {
            limpiarErrorControl(txtCodigo, errCodigo);
            return true;
        }
        if (c.isEmpty()) {
            marcarErrorControl(txtCodigo, errCodigo, "El código es obligatorio.");
            return false;
        }
        if (!c.matches(Entidad.PATRON_CODIGO)) {
            marcarErrorControl(txtCodigo, errCodigo, "Formato AAA-000 (ej. EST-001).");
            return false;
        }
        if (repositorio.existeCodigo(c)) {
            marcarErrorControl(txtCodigo, errCodigo, "Este código ya existe.");
            return false;
        }
        limpiarErrorControl(txtCodigo, errCodigo);
        return true;
    }

    private boolean validarNombre() {
        String n = txtNombre.getText() != null ? txtNombre.getText().trim() : "";
        if (n.isEmpty()) {
            marcarErrorControl(txtNombre, errNombre, "El nombre es obligatorio.");
            return false;
        }
        if (n.length() < 3) {
            marcarErrorControl(txtNombre, errNombre, "Mínimo 3 caracteres.");
            return false;
        }
        limpiarErrorControl(txtNombre, errNombre);
        return true;
    }

    private boolean validarCarrera() {
        String val = cmbCarrera.getValue();
        if (val == null || val.isBlank()) {
            marcarErrorControl(cmbCarrera, errCarrera, "Seleccione un programa.");
            return false;
        }
        limpiarErrorControl(cmbCarrera, errCarrera);
        return true;
    }

    private boolean validarSemestre() {
        String val = cmbSemestre.getValue();
        if (val == null || val.isBlank()) {
            marcarErrorControl(cmbSemestre, errSemestre, "Seleccione un semestre.");
            return false;
        }
        limpiarErrorControl(cmbSemestre, errSemestre);
        return true;
    }

    private boolean validarPromedio() {
        String t = txtPromedio.getText() != null ? txtPromedio.getText().trim() : "";
        if (t.isEmpty()) {
            marcarErrorControl(txtPromedio, errPromedio, "El promedio es obligatorio.");
            return false;
        }
        try {
            double v = Double.parseDouble(t.replace(",", "."));
            if (v < Entidad.PROMEDIO_MINIMO || v > Entidad.PROMEDIO_MAXIMO) {
                marcarErrorControl(txtPromedio, errPromedio, "Entre 0.0 y 5.0.");
                return false;
            }
        } catch (NumberFormatException e) {
            marcarErrorControl(txtPromedio, errPromedio, "Número inválido.");
            return false;
        }
        limpiarErrorControl(txtPromedio, errPromedio);
        return true;
    }

    private boolean validarEstado() {
        String val = cmbEstado.getValue();
        if (val == null || val.isBlank()) {
            marcarErrorControl(cmbEstado, errEstado, "Seleccione un estado.");
            return false;
        }
        limpiarErrorControl(cmbEstado, errEstado);
        return true;
    }

    private boolean esFormularioValido() {
        String codigo = txtCodigo.getText() != null ? txtCodigo.getText().trim() : "";
        String nombre = txtNombre.getText() != null ? txtNombre.getText().trim() : "";
        String carrera = cmbCarrera.getValue();
        String semestre = cmbSemestre.getValue();
        String promTxt = txtPromedio.getText() != null ? txtPromedio.getText().trim() : "";
        String estado = cmbEstado.getValue();

        if (entidadEnEdicion == null) {
            if (!codigo.matches(Entidad.PATRON_CODIGO) || repositorio.existeCodigo(codigo)) {
                return false;
            }
        }

        if (nombre.length() < 3 || carrera == null || semestre == null || estado == null) {
            return false;
        }

        try {
            double valor = Double.parseDouble(promTxt.replace(",", "."));
            return valor >= Entidad.PROMEDIO_MINIMO && valor <= Entidad.PROMEDIO_MAXIMO;
        } catch (Exception e) {
            return false;
        }
    }

    private void marcarErrorControl(javafx.scene.Node control, Label labelError, String mensaje) {
        if (!control.getStyleClass().contains("control-error")) {
            control.getStyleClass().add("control-error");
        }
        labelError.setText(mensaje);
        labelError.setVisible(true);
        labelError.setManaged(true);
    }

    private void limpiarErrorControl(javafx.scene.Node control, Label labelError) {
        control.getStyleClass().remove("control-error");
        labelError.setText("");
        labelError.setVisible(false);
        labelError.setManaged(false);
    }

    // ------------------------------------------------------------------
    // Métricas y KPIs Reactivas (Adaptadas al filtro activo)
    // ------------------------------------------------------------------
    private void configurarMetricasReactiva() {
        repositorio.listar().addListener((javafx.collections.ListChangeListener<Entidad>) c -> {
            actualizarArbolOrganizador();
            actualizarMetricas();
            aplicarFiltrosCombinados();
        });
        actualizarMetricas();
    }

    private void actualizarMetricas() {
        int visibles = listaFiltrada != null ? listaFiltrada.size() : repositorio.contar();
        long activos = (listaFiltrada != null ? listaFiltrada.stream() : repositorio.listar().stream())
                .filter(e -> Entidad.ESTADO_ACTIVO.equalsIgnoreCase(e.getEstado()))
                .count();

        double prom = visibles == 0 ? 0.0 :
                (listaFiltrada != null ? listaFiltrada.stream() : repositorio.listar().stream())
                        .mapToDouble(Entidad::getPromedio)
                        .average()
                        .orElse(0.0);

        lblTotalEstudiantes.setText(String.valueOf(visibles));
        lblEstudiantesActivos.setText(String.valueOf(activos));
        lblPromedioGeneral.setText(String.format(Locale.US, "%.2f", prom));
    }

    // ------------------------------------------------------------------
    // Operaciones CRUD
    // ------------------------------------------------------------------
    @FXML
    void onGuardar(ActionEvent event) {
        if (!esFormularioValido()) {
            mostrarFeedback("Por favor complete los campos requeridos correctamente.", true);
            return;
        }

        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();
        String carrera = cmbCarrera.getValue();
        int semestre = extraerNumeroSemestre(cmbSemestre.getValue());
        double promedio = Double.parseDouble(txtPromedio.getText().trim().replace(",", "."));
        String estado = cmbEstado.getValue();

        try {
            if (entidadEnEdicion == null) {
                // CREAR
                Entidad nuevo = new Entidad(codigo, nombre, carrera, semestre, promedio, estado);
                repositorio.crear(nuevo);
                mostrarFeedback("Estudiante [" + codigo + "] guardado exitosamente en " + carrera + ".", false);
            } else {
                // ACTUALIZAR (en el mismo objeto, sin duplicar)
                Entidad modificado = new Entidad(codigo, nombre, carrera, semestre, promedio, estado);
                repositorio.actualizar(entidadEnEdicion.getCodigo(), modificado);
                mostrarFeedback("Estudiante [" + codigo + "] actualizado correctamente.", false);
            }
            limpiarFormulario();
            actualizarArbolOrganizador();
        } catch (Exception ex) {
            mostrarFeedback("Error: " + ex.getMessage(), true);
        }
    }

    @FXML
    void onEliminar(ActionEvent event) {
        Entidad seleccionado = tblEstudiantes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarFeedback("Seleccione un estudiante de la tabla para eliminar.", true);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Eliminar al estudiante " + seleccionado.getNombre() + "?");
        alert.setContentText("Código: " + seleccionado.getCodigo()
                + "\nPrograma: " + seleccionado.getCarrera()
                + "\nSemestre: " + seleccionado.getSemestre() + "° Semestre"
                + "\n\nEsta acción removerá el registro de la memoria.");

        Optional<ButtonType> res = alert.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            boolean ok = repositorio.eliminar(seleccionado.getCodigo());
            if (ok) {
                mostrarFeedback("Estudiante [" + seleccionado.getCodigo() + "] eliminado.", false);
                limpiarFormulario();
                actualizarArbolOrganizador();
            } else {
                mostrarFeedback("No se pudo eliminar el estudiante.", true);
            }
        }
    }

    @FXML
    void onLimpiar(ActionEvent event) {
        limpiarFormulario();
        mostrarFeedback("Formulario y selección restablecidos.", false);
    }

    @FXML
    void onLimpiarFiltros(ActionEvent event) {
        cmbFiltroPrograma.setValue(TODOS_LOS_PROGRAMAS);
        cmbFiltroSemestre.setValue(TODOS_LOS_SEMESTRES);
        txtBuscar.clear();
        if (treeOrganizador.getRoot() != null) {
            treeOrganizador.getSelectionModel().select(treeOrganizador.getRoot());
        }
        aplicarFiltrosCombinados();
        mostrarFeedback("Filtros del organizador restablecidos.", false);
    }

    private void limpiarFormulario() {
        entidadEnEdicion = null;
        tblEstudiantes.getSelectionModel().clearSelection();

        txtCodigo.clear();
        txtCodigo.setDisable(false);
        txtNombre.clear();
        cmbCarrera.getSelectionModel().clearSelection();
        cmbSemestre.getSelectionModel().clearSelection();
        txtPromedio.clear();
        cmbEstado.getSelectionModel().select(Entidad.ESTADO_ACTIVO);

        limpiarErrorControl(txtCodigo, errCodigo);
        limpiarErrorControl(txtNombre, errNombre);
        limpiarErrorControl(cmbCarrera, errCarrera);
        limpiarErrorControl(cmbSemestre, errSemestre);
        limpiarErrorControl(txtPromedio, errPromedio);
        limpiarErrorControl(cmbEstado, errEstado);

        lblModoFormulario.setText("Modo: Nuevo Registro");
        lblModoFormulario.getStyleClass().setAll("label-modo", "modo-creacion");
        btnGuardar.setText("Guardar Estudiante");
        btnGuardar.getStyleClass().setAll("button", "btn-guardar");
    }

    private void mostrarFeedback(String mensaje, boolean esError) {
        lblMensajeFeedback.setText(mensaje);
        lblMensajeFeedback.getStyleClass().setAll("lbl-feedback", esError ? "feedback-error" : "feedback-exito");
    }
}
