package com.app.model;

import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.util.Callback;

import java.util.List;

/**
 * Modelo del dominio: representa a un <b>Estudiante</b> universitario.
 *
 * <p>Organizado por <b>Programa Académico (Carrera)</b> y <b>Semestre (1 al 10)</b>.
 * Todos los atributos se exponen como JavaFX Properties observables, permitiendo
 * enlace bidireccional y actualización reactiva en la interfaz sin recargar ventanas.</p>
 */
public class Entidad {

    // ------------------------------------------------------------------
    // Catálogos y reglas del dominio (fuente única de verdad)
    // ------------------------------------------------------------------

    /** Programas académicos (carreras) disponibles. */
    public static final List<String> CARRERAS = List.of(
            "Ingeniería de Software",
            "Ingeniería de Sistemas",
            "Ingeniería Industrial",
            "Administración de Empresas",
            "Contaduría Pública",
            "Diseño Gráfico",
            "Psicología",
            "Derecho"
    );

    public static final int SEMESTRE_MINIMO = 1;
    public static final int SEMESTRE_MAXIMO = 10;

    /** Semestres académicos disponibles (1 al 10). */
    public static final List<Integer> SEMESTRES = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

    public static final String ESTADO_ACTIVO = "Activo";
    public static final String ESTADO_INACTIVO = "Inactivo";
    public static final String ESTADO_GRADUADO = "Graduado";

    /** Estados académicos posibles. */
    public static final List<String> ESTADOS = List.of(ESTADO_ACTIVO, ESTADO_INACTIVO, ESTADO_GRADUADO);

    /** Formato del código estudiantil: AAA-000 (ej. EST-001). */
    public static final String PATRON_CODIGO = "^[A-Z]{3}-\\d{3}$";

    public static final double PROMEDIO_MINIMO = 0.0;
    public static final double PROMEDIO_MAXIMO = 5.0;

    // ------------------------------------------------------------------
    // Propiedades observables
    // ------------------------------------------------------------------

    private final StringProperty codigo = new SimpleStringProperty(this, "codigo", "");
    private final StringProperty nombre = new SimpleStringProperty(this, "nombre", "");
    private final StringProperty carrera = new SimpleStringProperty(this, "carrera", "");
    private final IntegerProperty semestre = new SimpleIntegerProperty(this, "semestre", 1);
    private final DoubleProperty promedio = new SimpleDoubleProperty(this, "promedio", 0.0);
    private final StringProperty estado = new SimpleStringProperty(this, "estado", ESTADO_ACTIVO);

    // ------------------------------------------------------------------
    // Constructores
    // ------------------------------------------------------------------

    public Entidad() {
    }

    /**
     * Crea un estudiante completo con programa académico y semestre.
     */
    public Entidad(String codigo, String nombre, String carrera, int semestre, double promedio, String estado) {
        setCodigo(codigo);
        setNombre(nombre);
        setCarrera(carrera);
        setSemestre(semestre);
        setPromedio(promedio);
        setEstado(estado);
    }

    // ------------------------------------------------------------------
    // Extractor para reactividad total en ObservableList
    // ------------------------------------------------------------------

    /**
     * Devuelve las propiedades a observar para que la lista emita eventos de tipo "update"
     * cuando cualquier dato de un estudiante cambie (recalculando filtros y árbol en vivo).
     */
    public static Callback<Entidad, Observable[]> extractor() {
        return e -> new Observable[]{e.codigo, e.nombre, e.carrera, e.semestre, e.promedio, e.estado};
    }

    /**
     * Copia los datos editables desde otra instancia en memoria (sin duplicar ID).
     */
    public void copiarDatosDesde(Entidad otra) {
        setNombre(otra.getNombre());
        setCarrera(otra.getCarrera());
        setSemestre(otra.getSemestre());
        setPromedio(otra.getPromedio());
        setEstado(otra.getEstado());
    }

    // ------------------------------------------------------------------
    // Getters, Setters y Properties
    // ------------------------------------------------------------------

    public String getCodigo() {
        return codigo.get();
    }

    public void setCodigo(String valor) {
        codigo.set(valor);
    }

    public StringProperty codigoProperty() {
        return codigo;
    }

    public String getNombre() {
        return nombre.get();
    }

    public void setNombre(String valor) {
        nombre.set(valor);
    }

    public StringProperty nombreProperty() {
        return nombre;
    }

    public String getCarrera() {
        return carrera.get();
    }

    public void setCarrera(String valor) {
        carrera.set(valor);
    }

    public StringProperty carreraProperty() {
        return carrera;
    }

    public int getSemestre() {
        return semestre.get();
    }

    public void setSemestre(int valor) {
        semestre.set(valor);
    }

    public IntegerProperty semestreProperty() {
        return semestre;
    }

    public double getPromedio() {
        return promedio.get();
    }

    public void setPromedio(double valor) {
        promedio.set(valor);
    }

    public DoubleProperty promedioProperty() {
        return promedio;
    }

    public String getEstado() {
        return estado.get();
    }

    public void setEstado(String valor) {
        estado.set(valor);
    }

    public StringProperty estadoProperty() {
        return estado;
    }

    @Override
    public String toString() {
        return "Entidad{codigo='%s', nombre='%s', carrera='%s', semestre=%d, promedio=%.2f, estado='%s'}"
                .formatted(getCodigo(), getNombre(), getCarrera(), getSemestre(), getPromedio(), getEstado());
    }
}
