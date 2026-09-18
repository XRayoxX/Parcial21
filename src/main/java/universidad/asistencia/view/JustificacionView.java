package universidad.asistencia.view;

import universidad.asistencia.controller.DocenteController;
import universidad.asistencia.controller.EstudianteController;
import universidad.asistencia.controller.InscripcionController;
import universidad.asistencia.controller.JustificacionController;
import universidad.asistencia.controller.SesionClaseController;
import universidad.asistencia.enums.EstadoJustificacion;
import universidad.asistencia.model.Docente;
import universidad.asistencia.model.Estudiante;
import universidad.asistencia.model.Inscripcion;
import universidad.asistencia.model.Justificacion;
import universidad.asistencia.model.SesionClase;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class JustificacionView {

    private static final String FILTRO_TODAS = "TODAS";

    // Componentes vinculados desde JustificacionView.form
    private JPanel panelPrincipal;

    private JComboBox cmbEstudiante;
    private JComboBox cmbSesion;
    private JComboBox cmbDocenteRegistra;
    private JTextField txtMotivo;
    private JTextField txtObservacion;
    private JTextField txtEstado;
    private JComboBox cmbDocenteResuelve;

    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnAprobar;
    private JButton btnRechazar;

    private JComboBox cmbFiltroEstado;
    private JButton btnBuscar;

    private JTable tblJustificaciones;
    private JButton btnSalir;

    // La View solamente conoce a los Controllers.
    private final JustificacionController justificacionController;
    private final EstudianteController estudianteController;
    private final SesionClaseController sesionClaseController;
    private final DocenteController docenteController;
    private final InscripcionController inscripcionController;

    /*
     * ID de la justificación seleccionada en la tabla.
     * Vacío mientras estamos creando una justificación nueva.
     */
    private Integer idJustificacionSeleccionada;

    /*
     * Todas las sesiones cargadas al abrir el formulario.
     * cmbSesion siempre se rellena filtrando esta lista según
     * el estudiante seleccionado.
     */
    private List<SesionClase> todasLasSesiones = new ArrayList<>();


    /*
     * Constructor normal.
     */
    public JustificacionView() {

        this(
                new JustificacionController(),
                new EstudianteController(),
                new SesionClaseController(),
                new DocenteController(),
                new InscripcionController()
        );
    }


    /*
     * También permitimos recibir los Controllers desde afuera.
     */
    public JustificacionView(
            JustificacionController justificacionController,
            EstudianteController estudianteController,
            SesionClaseController sesionClaseController,
            DocenteController docenteController,
            InscripcionController inscripcionController
    ) {

        this.justificacionController = justificacionController;
        this.estudianteController = estudianteController;
        this.sesionClaseController = sesionClaseController;
        this.docenteController = docenteController;
        this.inscripcionController = inscripcionController;

        configurarFormulario();
        configurarEventos();
        cargarCombos();
        cargarJustificaciones();
    }


    /*
     * Configuración inicial.
     */
    private void configurarFormulario() {

        txtEstado.setEditable(false);

        cmbFiltroEstado.setModel(
                new DefaultComboBoxModel<>(
                        new String[]{
                                FILTRO_TODAS,
                                EstadoJustificacion.PENDIENTE.name(),
                                EstadoJustificacion.APROBADA.name(),
                                EstadoJustificacion.RECHAZADA.name()
                        }
                )
        );

        btnAprobar.setEnabled(false);
        btnRechazar.setEnabled(false);

        configurarCombos();

        configurarTabla();
    }


    /*
     * Renderers para que los combos muestren texto legible
     * en vez del toString() por defecto de los modelos.
     */
    @SuppressWarnings("unchecked")
    private void configurarCombos() {

        cmbEstudiante.setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(
                    JList list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {

                super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus
                );

                if (value instanceof Estudiante estudiante) {

                    setText(
                            estudiante.getCarnet()
                                    + " - "
                                    + estudiante.getNombres()
                                    + " "
                                    + estudiante.getApellidos()
                    );
                }

                return this;
            }
        });

        cmbSesion.setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(
                    JList list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {

                super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus
                );

                if (value instanceof SesionClase sesion) {

                    setText(
                            sesion.getSeccion().getCodigo()
                                    + " - "
                                    + sesion.getFecha()
                    );
                }

                return this;
            }
        });

        DefaultListCellRenderer rendererDocente =
                new DefaultListCellRenderer() {

                    @Override
                    public Component getListCellRendererComponent(
                            JList list,
                            Object value,
                            int index,
                            boolean isSelected,
                            boolean cellHasFocus
                    ) {

                        super.getListCellRendererComponent(
                                list, value, index, isSelected, cellHasFocus
                        );

                        if (value instanceof Docente docente) {

                            setText(
                                    docente.getCodigoEmpleado()
                                            + " - "
                                            + docente.getNombres()
                                            + " "
                                            + docente.getApellidos()
                            );
                        }

                        return this;
                    }
                };

        cmbDocenteRegistra.setRenderer(rendererDocente);

        cmbDocenteResuelve.setRenderer(rendererDocente);

        /*
         * Al cambiar de estudiante, la Sesión se filtra para
         * mostrar solo sesiones de secciones donde ese estudiante
         * tiene una inscripción activa.
         */
        cmbEstudiante.addItemListener(e -> {

            if (e.getStateChange() != java.awt.event.ItemEvent.SELECTED) {
                return;
            }

            filtrarSesionesPorEstudiante();
        });
    }


    /*
     * Carga estudiantes, sesiones y docentes en los combos.
     * Usamos listar() (no listarActivos()) para poder seguir
     * mostrando justificaciones antiguas de datos ya inactivos.
     */
    @SuppressWarnings("unchecked")
    private void cargarCombos() {

        try {

            DefaultComboBoxModel<Estudiante> modeloEstudiantes =
                    new DefaultComboBoxModel<>();

            for (Estudiante estudiante : estudianteController.listar()) {
                modeloEstudiantes.addElement(estudiante);
            }

            cmbEstudiante.setModel(modeloEstudiantes);

            todasLasSesiones =
                    sesionClaseController.listar();

            filtrarSesionesPorEstudiante();

            DefaultComboBoxModel<Docente> modeloDocentesRegistra =
                    new DefaultComboBoxModel<>();

            DefaultComboBoxModel<Docente> modeloDocentesResuelve =
                    new DefaultComboBoxModel<>();

            for (Docente docente : docenteController.listar()) {
                modeloDocentesRegistra.addElement(docente);
                modeloDocentesResuelve.addElement(docente);
            }

            cmbDocenteRegistra.setModel(modeloDocentesRegistra);

            cmbDocenteResuelve.setModel(modeloDocentesResuelve);

        } catch (Exception e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }


    /*
     * Reconstruye el modelo de cmbSesion con las sesiones de
     * las secciones donde el estudiante seleccionado tiene una
     * inscripción activa. Si no hay estudiante seleccionado,
     * muestra todas las sesiones sin filtrar.
     */
    @SuppressWarnings("unchecked")
    private void filtrarSesionesPorEstudiante() {

        Estudiante estudiante =
                (Estudiante) cmbEstudiante.getSelectedItem();

        DefaultComboBoxModel<SesionClase> modeloSesiones =
                new DefaultComboBoxModel<>();

        if (estudiante == null) {

            for (SesionClase sesion : todasLasSesiones) {
                modeloSesiones.addElement(sesion);
            }

            cmbSesion.setModel(modeloSesiones);

            return;
        }

        try {

            Set<Integer> seccionesInscritas =
                    inscripcionController
                            .listarPorEstudiante(estudiante.getIdEstudiante())
                            .stream()
                            .filter(Inscripcion::isActiva)
                            .map(i -> i.getSeccion().getIdSeccion())
                            .collect(Collectors.toSet());

            for (SesionClase sesion : todasLasSesiones) {

                if (seccionesInscritas.contains(sesion.getSeccion().getIdSeccion())) {
                    modeloSesiones.addElement(sesion);
                }
            }

            cmbSesion.setModel(modeloSesiones);

        } catch (Exception e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }


    /*
     * Configuración de JTable.
     */
    private void configurarTabla() {

        DefaultTableModel modelo =
                new DefaultTableModel(
                        new Object[]{
                                "ID",
                                "Estudiante",
                                "Sesión",
                                "Motivo",
                                "Fecha",
                                "Estado",
                                "Docente Resuelve"
                        },
                        0
                ) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column
                    ) {
                        return false;
                    }
                };

        tblJustificaciones.setModel(modelo);

        tblJustificaciones.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
    }


    /*
     * Eventos de los controles.
     */
    private void configurarEventos() {

        btnNuevo.addActionListener(
                e -> nuevo()
        );

        btnGuardar.addActionListener(
                e -> guardar()
        );

        btnAprobar.addActionListener(
                e -> resolver(EstadoJustificacion.APROBADA)
        );

        btnRechazar.addActionListener(
                e -> resolver(EstadoJustificacion.RECHAZADA)
        );

        btnBuscar.addActionListener(
                e -> filtrar()
        );

        btnSalir.addActionListener(
                e -> salir()
        );

        tblJustificaciones
                .getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {
                        seleccionarJustificacion();
                    }
                });
    }


    /*
     * ========================================================
     * CREATE
     * ========================================================
     */
    private void guardar() {

        try {

            Estudiante estudiante =
                    (Estudiante) cmbEstudiante.getSelectedItem();

            SesionClase sesion =
                    (SesionClase) cmbSesion.getSelectedItem();

            Docente docenteRegistra =
                    (Docente) cmbDocenteRegistra.getSelectedItem();

            if (estudiante == null || sesion == null || docenteRegistra == null) {

                mostrarError(
                        "Debe seleccionar estudiante, sesión "
                                + "y docente que registra."
                );

                return;
            }

            String motivo =
                    txtMotivo.getText().trim();

            String observacion =
                    txtObservacion.getText().trim();

            Justificacion justificacion =
                    new Justificacion(
                            estudiante,
                            sesion,
                            docenteRegistra,
                            motivo,
                            observacion.isBlank() ? null : observacion
                    );

            boolean guardado =
                    justificacionController.registrarJustificacion(
                            justificacion
                    );

            if (guardado) {

                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        "Justificación registrada correctamente.",
                        "Justificación",
                        JOptionPane.INFORMATION_MESSAGE
                );

                limpiarFormulario();

                cargarJustificaciones();

            } else {

                mostrarError(
                        "No se pudo registrar la justificación."
                );
            }

        } catch (Exception e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }


    /*
     * ========================================================
     * READ - LISTAR
     * ========================================================
     */
    private void cargarJustificaciones() {

        try {

            List<Justificacion> justificaciones =
                    justificacionController.listar();

            llenarTabla(justificaciones);

        } catch (Exception e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }


    private void llenarTabla(
            List<Justificacion> justificaciones
    ) {

        DefaultTableModel modelo =
                (DefaultTableModel)
                        tblJustificaciones.getModel();

        modelo.setRowCount(0);

        for (Justificacion justificacion : justificaciones) {

            modelo.addRow(
                    new Object[]{
                            justificacion.getIdJustificacion(),

                            justificacion.getEstudiante().getCarnet()
                                    + " - "
                                    + justificacion.getEstudiante().getNombres(),

                            justificacion.getSesionClase().getSeccion().getCodigo()
                                    + " ("
                                    + justificacion.getSesionClase().getFecha()
                                    + ")",

                            justificacion.getMotivo(),
                            justificacion.getFecha(),
                            justificacion.getEstado(),

                            justificacion.getDocenteResuelve() == null
                                    ? "-"
                                    : justificacion.getDocenteResuelve().getCodigoEmpleado()
                    }
            );
        }
    }


    /*
     * ========================================================
     * APROBAR / RECHAZAR
     * ========================================================
     */
    private void resolver(
            EstadoJustificacion estado
    ) {

        if (idJustificacionSeleccionada == null) {

            JOptionPane.showMessageDialog(
                    panelPrincipal,
                    "Debe seleccionar una justificación.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        Docente docenteResuelve =
                (Docente) cmbDocenteResuelve.getSelectedItem();

        if (docenteResuelve == null) {

            mostrarError(
                    "Debe seleccionar el docente que resuelve."
            );

            return;
        }

        String accion =
                estado == EstadoJustificacion.APROBADA
                        ? "aprobar"
                        : "rechazar";

        int respuesta =
                JOptionPane.showConfirmDialog(
                        panelPrincipal,
                        "¿Está seguro de "
                                + accion
                                + " esta justificación?",
                        "Confirmar",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            Justificacion resuelta =
                    estado == EstadoJustificacion.APROBADA
                            ? justificacionController.aprobarJustificacion(
                                    idJustificacionSeleccionada,
                                    docenteResuelve.getIdDocente()
                            )
                            : justificacionController.rechazarJustificacion(
                                    idJustificacionSeleccionada,
                                    docenteResuelve.getIdDocente()
                            );

            JOptionPane.showMessageDialog(
                    panelPrincipal,
                    "Justificación "
                            + (estado == EstadoJustificacion.APROBADA
                                    ? "aprobada"
                                    : "rechazada")
                            + " correctamente.",
                    "Justificación",
                    JOptionPane.INFORMATION_MESSAGE
            );

            int id =
                    resuelta.getIdJustificacion();

            cargarJustificaciones();

            seleccionarFilaPorId(
                    id
            );

        } catch (Exception e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }


    /*
     * ========================================================
     * FILTRAR POR ESTADO
     * ========================================================
     */
    private void filtrar() {

        try {

            String filtro =
                    (String) cmbFiltroEstado.getSelectedItem();

            List<Justificacion> resultado =
                    FILTRO_TODAS.equals(filtro)
                            ? justificacionController.listar()
                            : justificacionController.listarPorEstado(
                                    EstadoJustificacion.valueOf(filtro)
                            );

            llenarTabla(resultado);

        } catch (Exception e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }


    /*
     * ========================================================
     * SELECCIÓN DESDE JTable
     * ========================================================
     */
    private void seleccionarJustificacion() {

        int fila =
                tblJustificaciones.getSelectedRow();

        if (fila == -1) {
            return;
        }

        try {

            int idJustificacion =
                    Integer.parseInt(
                            tblJustificaciones
                                    .getValueAt(
                                            fila,
                                            0
                                    )
                                    .toString()
                    );

            Optional<Justificacion> resultado =
                    justificacionController.buscar(
                            idJustificacion
                    );

            if (resultado.isPresent()) {

                mostrarJustificacionEnFormulario(
                        resultado.get()
                );
            }

        } catch (Exception e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }


    private void mostrarJustificacionEnFormulario(
            Justificacion justificacion
    ) {

        idJustificacionSeleccionada =
                justificacion.getIdJustificacion();

        seleccionarEstudianteEnCombo(
                justificacion.getEstudiante().getIdEstudiante()
        );

        seleccionarSesionEnCombo(
                justificacion.getSesionClase().getIdSesion()
        );

        seleccionarDocenteEnCombo(
                cmbDocenteRegistra,
                justificacion.getDocenteRegistra().getIdDocente()
        );

        txtMotivo.setText(
                justificacion.getMotivo()
        );

        txtObservacion.setText(
                justificacion.getObservacion() == null
                        ? ""
                        : justificacion.getObservacion()
        );

        txtEstado.setText(
                justificacion.getEstado().toString()
        );

        if (justificacion.getDocenteResuelve() != null) {

            seleccionarDocenteEnCombo(
                    cmbDocenteResuelve,
                    justificacion.getDocenteResuelve().getIdDocente()
            );
        }

        boolean pendiente =
                justificacion.getEstado()
                        == EstadoJustificacion.PENDIENTE;

        btnGuardar.setEnabled(false);

        btnAprobar.setEnabled(pendiente);

        btnRechazar.setEnabled(pendiente);
    }


    private void seleccionarEstudianteEnCombo(
            int idEstudiante
    ) {

        for (
                int i = 0;
                i < cmbEstudiante.getItemCount();
                i++
        ) {

            Estudiante estudiante =
                    (Estudiante) cmbEstudiante.getItemAt(i);

            if (estudiante.getIdEstudiante() == idEstudiante) {

                cmbEstudiante.setSelectedIndex(i);

                return;
            }
        }
    }

    /*
     * Si la sesión de una justificación ya guardada quedó fuera
     * del filtro (p. ej. el estudiante ya no tiene inscripción
     * activa en esa sección), la reinsertamos en el combo para
     * poder seguir mostrando el registro histórico sin perderlo.
     */
    @SuppressWarnings("unchecked")
    private void seleccionarSesionEnCombo(
            int idSesion
    ) {

        for (
                int i = 0;
                i < cmbSesion.getItemCount();
                i++
        ) {

            SesionClase sesion =
                    (SesionClase) cmbSesion.getItemAt(i);

            if (sesion.getIdSesion() == idSesion) {

                cmbSesion.setSelectedIndex(i);

                return;
            }
        }

        for (SesionClase sesion : todasLasSesiones) {

            if (sesion.getIdSesion() == idSesion) {

                ((DefaultComboBoxModel<SesionClase>) cmbSesion.getModel())
                        .addElement(sesion);

                cmbSesion.setSelectedItem(sesion);

                return;
            }
        }
    }

    private void seleccionarDocenteEnCombo(
            JComboBox combo,
            int idDocente
    ) {

        for (
                int i = 0;
                i < combo.getItemCount();
                i++
        ) {

            Docente docente =
                    (Docente) combo.getItemAt(i);

            if (docente.getIdDocente() == idDocente) {

                combo.setSelectedIndex(i);

                return;
            }
        }
    }


    private void seleccionarFilaPorId(
            int idJustificacion
    ) {

        for (
                int fila = 0;
                fila < tblJustificaciones.getRowCount();
                fila++
        ) {

            int idTabla =
                    Integer.parseInt(
                            tblJustificaciones
                                    .getValueAt(
                                            fila,
                                            0
                                    )
                                    .toString()
                    );

            if (idTabla == idJustificacion) {

                tblJustificaciones.setRowSelectionInterval(
                        fila,
                        fila
                );

                tblJustificaciones.scrollRectToVisible(
                        tblJustificaciones
                                .getCellRect(
                                        fila,
                                        0,
                                        true
                                )
                );

                break;
            }
        }
    }


    /*
     * ========================================================
     * NUEVO
     * ========================================================
     */
    private void nuevo() {

        limpiarFormulario();
    }


    private void limpiarFormulario() {

        idJustificacionSeleccionada = null;

        if (cmbEstudiante.getItemCount() > 0) {
            cmbEstudiante.setSelectedIndex(0);
        }

        if (cmbSesion.getItemCount() > 0) {
            cmbSesion.setSelectedIndex(0);
        }

        if (cmbDocenteRegistra.getItemCount() > 0) {
            cmbDocenteRegistra.setSelectedIndex(0);
        }

        if (cmbDocenteResuelve.getItemCount() > 0) {
            cmbDocenteResuelve.setSelectedIndex(0);
        }

        txtMotivo.setText("");

        txtObservacion.setText("");

        txtEstado.setText("");

        tblJustificaciones.clearSelection();

        btnGuardar.setEnabled(true);

        btnAprobar.setEnabled(false);

        btnRechazar.setEnabled(false);
    }


    private void mostrarError(
            String mensaje
    ) {

        if (
                mensaje == null
                        || mensaje.isBlank()
        ) {

            mensaje =
                    "Ocurrió un error inesperado.";
        }

        JOptionPane.showMessageDialog(
                panelPrincipal,
                mensaje,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }


    private void salir() {

        int respuesta =
                JOptionPane.showConfirmDialog(
                        panelPrincipal,
                        "¿Desea regresar al menú principal?",
                        "Regresar",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

        if (respuesta == JOptionPane.YES_OPTION) {

            java.awt.Window ventanaActual =
                    SwingUtilities.getWindowAncestor(
                            panelPrincipal
                    );

            if (ventanaActual != null) {
                ventanaActual.dispose();
            }
        }
    }


    public JPanel getPanelPrincipal() {

        return panelPrincipal;
    }
}
