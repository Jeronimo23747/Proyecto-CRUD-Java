package com.app.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

/**
 * Repositorio en memoria (sin persistencia externa).
 *
 * <p>Gestiona una colección interna de tipo {@link ArrayList} envuelta en
 * {@link ObservableList} con un extractor de propiedades. Los cambios de atributos
 * en cualquier estudiante notifican automáticamente a la interfaz, recalculando
 * en tiempo real el organizador por programa y semestre.</p>
 */
public class RepositorioMemoria {

    private final List<Entidad> almacenamiento = new ArrayList<>();

    private final ObservableList<Entidad> registros =
            FXCollections.observableList(almacenamiento, Entidad.extractor());

    private final ObservableList<Entidad> vistaSoloLectura =
            FXCollections.unmodifiableObservableList(registros);

    // ------------------------------------------------------------------
    // READ
    // ------------------------------------------------------------------

    public ObservableList<Entidad> listar() {
        return vistaSoloLectura;
    }

    public Optional<Entidad> buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        String buscado = codigo.trim();
        return registros.stream()
                .filter(e -> e.getCodigo().equalsIgnoreCase(buscado))
                .findFirst();
    }

    public boolean existeCodigo(String codigo) {
        return buscarPorCodigo(codigo).isPresent();
    }

    public int contar() {
        return registros.size();
    }

    // ------------------------------------------------------------------
    // CREATE
    // ------------------------------------------------------------------

    public Entidad crear(Entidad nueva) {
        Objects.requireNonNull(nueva, "La entidad no puede ser nula.");
        validarCodigo(nueva.getCodigo());
        validarDatos(nueva);
        if (existeCodigo(nueva.getCodigo())) {
            throw new IllegalArgumentException("Ya existe un estudiante con el código " + nueva.getCodigo() + ".");
        }
        registros.add(nueva);
        return nueva;
    }

    // ------------------------------------------------------------------
    // UPDATE
    // ------------------------------------------------------------------

    public Entidad actualizar(String codigo, Entidad datos) {
        Objects.requireNonNull(datos, "Los datos no pueden ser nulos.");
        Entidad existente = buscarPorCodigo(codigo)
                .orElseThrow(() -> new NoSuchElementException("No existe un estudiante con el código " + codigo + "."));
        validarDatos(datos);
        existente.copiarDatosDesde(datos);
        return existente;
    }

    // ------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------

    public boolean eliminar(String codigo) {
        Optional<Entidad> encontrado = buscarPorCodigo(codigo);
        encontrado.ifPresent(registros::remove);
        return encontrado.isPresent();
    }

    // ------------------------------------------------------------------
    // Datos de demostración estructurados por Programa y Semestre
    // ------------------------------------------------------------------

    public void cargarDatosDemostracion() {
        // Ingeniería de Software
        crear(new Entidad("EST-001", "Laura Martínez Gómez", "Ingeniería de Software", 4, 4.65, Entidad.ESTADO_ACTIVO));
        crear(new Entidad("EST-002", "Carlos Andrés Rojas", "Ingeniería de Software", 2, 3.80, Entidad.ESTADO_ACTIVO));
        crear(new Entidad("EST-003", "Mateo Gómez Peña", "Ingeniería de Software", 4, 4.10, Entidad.ESTADO_ACTIVO));
        crear(new Entidad("EST-004", "Esteban Quintero", "Ingeniería de Software", 7, 4.40, Entidad.ESTADO_ACTIVO));

        // Ingeniería de Sistemas
        crear(new Entidad("EST-005", "Valentina Torres", "Ingeniería de Sistemas", 6, 4.20, Entidad.ESTADO_GRADUADO));
        crear(new Entidad("EST-006", "Santiago Herrera", "Ingeniería de Sistemas", 2, 2.95, Entidad.ESTADO_INACTIVO));
        crear(new Entidad("EST-007", "Alejandro Bedoya", "Ingeniería de Sistemas", 6, 4.15, Entidad.ESTADO_ACTIVO));

        // Diseño Gráfico
        crear(new Entidad("EST-008", "Mariana López Ruiz", "Diseño Gráfico", 8, 4.90, Entidad.ESTADO_ACTIVO));
        crear(new Entidad("EST-009", "Julián Castaño", "Diseño Gráfico", 3, 3.45, Entidad.ESTADO_ACTIVO));

        // Administración de Empresas
        crear(new Entidad("EST-010", "Daniela Ospina", "Administración de Empresas", 5, 4.05, Entidad.ESTADO_GRADUADO));
        crear(new Entidad("EST-011", "Andrés Felipe Mejía", "Administración de Empresas", 1, 3.10, Entidad.ESTADO_ACTIVO));

        // Psicología & Derecho
        crear(new Entidad("EST-012", "Sofía Restrepo Cano", "Psicología", 7, 4.55, Entidad.ESTADO_ACTIVO));
        crear(new Entidad("EST-013", "Alejandro Morales", "Derecho", 9, 3.90, Entidad.ESTADO_ACTIVO));
        crear(new Entidad("EST-014", "Camila Vargas Silva", "Contaduría Pública", 4, 4.30, Entidad.ESTADO_ACTIVO));
    }

    // ------------------------------------------------------------------
    // Reglas de negocio y validación defensiva
    // ------------------------------------------------------------------

    private void validarCodigo(String codigo) {
        if (codigo == null || !codigo.trim().matches(Entidad.PATRON_CODIGO)) {
            throw new IllegalArgumentException("El código debe tener el formato AAA-000 (ej. EST-001).");
        }
    }

    private void validarDatos(Entidad e) {
        if (e.getNombre() == null || e.getNombre().trim().length() < 3) {
            throw new IllegalArgumentException("El nombre debe tener al menos 3 caracteres.");
        }
        if (e.getCarrera() == null || e.getCarrera().isBlank()) {
            throw new IllegalArgumentException("El programa académico es obligatorio.");
        }
        if (e.getSemestre() < Entidad.SEMESTRE_MINIMO || e.getSemestre() > Entidad.SEMESTRE_MAXIMO) {
            throw new IllegalArgumentException("El semestre debe estar comprendido entre 1 y 10.");
        }
        if (e.getPromedio() < Entidad.PROMEDIO_MINIMO || e.getPromedio() > Entidad.PROMEDIO_MAXIMO) {
            throw new IllegalArgumentException("El promedio debe estar entre 0.0 y 5.0.");
        }
        if (!Entidad.ESTADOS.contains(e.getEstado())) {
            throw new IllegalArgumentException("Estado no válido: " + e.getEstado());
        }
    }
}
