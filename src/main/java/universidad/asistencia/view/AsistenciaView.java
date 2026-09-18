package universidad.asistencia.view;

import universidad.asistencia.controller.AsistenciaController;
import universidad.asistencia.controller.DispositivoController;
import universidad.asistencia.controller.EstudianteController;
import universidad.asistencia.controller.InscripcionController;
import universidad.asistencia.controller.SesionClaseController;
import universidad.asistencia.enums.MedioMarcaje;
import universidad.asistencia.enums.TipoMarcaje;
import universidad.asistencia.model.Dispositivo;
import universidad.asistencia.model.Estudiante;
import universidad.asistencia.model.Inscripcion;
import universidad.asistencia.model.Marcaje;
import universidad.asistencia.model.SesionClase;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AsistenciaView {

    // Componentes vinculados desde AsistenciaView.form
    private JPanel panelPrincipal;

    private JComboBox cmbEstudiante;
    private JComboBox cmbSesion;
    private JComboBox cmbDispositivo;
    private JComboBox cmbTipo;
    private JComboBox cmbMedio;

    private JButton btnRegistrar;
    private JButton btnHistorial;
    private JButton btnPorcentaje;
    private JButton btnBajo80;

    private JTextField txtResultado;
    private JTable tblResultado;
    private JButton btnSalir;

    // La View solamente conoce a los Controllers.
    private final AsistenciaController asistenciaController;
    private final EstudianteController estudianteController;
    private final SesionClaseController sesionClaseController;
    private final DispositivoController dispositivoController;
    private final InscripcionController inscripcionController;

    /*
     * Todas las sesiones cargadas al abrir el formulario.
     * cmbSesion siempre se rellena filtrando esta lista según
     * el estudiante seleccionado.
     */
    private List<SesionClase> todasLasSesiones = new ArrayList<>();


    /*
     * Constructor normal.
     */
    public AsistenciaView() {

        this(
                new AsistenciaController(),
                new EstudianteController(),
                new SesionClaseController(),
                new DispositivoController(),
                new InscripcionController()
        );
    }


    /*
     * También permitimos recibir los Controllers desde afuera.
     */
    public AsistenciaView(
            AsistenciaController asistenciaController,
            EstudianteController estudianteController,
            SesionClaseController sesionClaseController,
            DispositivoController dispositivoController,
            InscripcionController inscripcionController
    ) {

        this.asistenciaController = asistenciaController;
        this.estudianteController = estudianteController;
        this.sesionClaseController = sesionClaseController;
        this.dispositivoController = dispositivoController;
        this.inscripcionController = inscripcionController;

        configurarFormulario();
        configurarEventos();
        cargarCombos();
        configurarTablaMarcajes();
    }


    /*
     * Configuración inicial.
     */
    private void configurarFormulario() {

        cmbTipo.setModel(
                new DefaultComboBoxModel<>(
                        TipoMarcaje.values()
                )
        );

        cmbMedio.setModel(
                new DefaultComboBoxModel<>(
                        MedioMarcaje.values()
                )
        );

        configurarCombos();
    }


    /*
     * Renderers para que los combos muestren texto legible.
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
                                    + " ("
                                    + sesion.getEstado()
                                    + ")"
                    );
                }

                return this;
            }
        });

        cmbDispositivo.setRenderer(new DefaultListCellRenderer() {

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

                if (value instanceof Dispositivo dispositivo) {

                    setText(
                            dispositivo.getCodigo()
                                    + " - "
                                    + dispositivo.getNombre()
                    );
                }

                return this;
            }
        });

        /*
         * Al cambiar de estudiante, la Sesión se filtra para
         * mostrar solo sesiones de secciones donde ese estudiante
         * tiene una inscripción activa. Evita que el usuario
         * tantee combinaciones que el Service va a rechazar.
         */
        cmbEstudiante.addItemListener(e -> {

            if (e.getStateChange() != java.awt.event.ItemEvent.SELECTED) {
                return;
            }

            filtrarSesionesPorEstudiante();
        });
    }


    /*
     * Carga estudiantes, sesiones y dispositivos activos:
     * solo tiene sentido marcar asistencia con datos vigentes.
     */
    @SuppressWarnings("unchecked")
    private void cargarCombos() {

        try {

            DefaultComboBoxModel<Estudiante> modeloEstudiantes =
                    new DefaultComboBoxModel<>();

            for (Estudiante estudiante : estudianteController.listarActivos()) {
                modeloEstudiantes.addElement(estudiante);
            }

            cmbEstudiante.setModel(modeloEstudiantes);

            todasLasSesiones =
                    sesionClaseController.listar();

            filtrarSesionesPorEstudiante();

            DefaultComboBoxModel<Dispositivo> modeloDispositivos =
                    new DefaultComboBoxModel<>();

            for (Dispositivo dispositivo : dispositivoController.listarActivos()) {
                modeloDispositivos.addElement(dispositivo);
            }

            cmbDispositivo.setModel(modeloDispositivos);

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
     * Eventos de los controles.
     */
    private void configurarEventos() {

        btnRegistrar.addActionListener(
                e -> registrarMarcaje()
        );

        btnHistorial.addActionListener(
                e -> verHistorial()
        );

        btnPorcentaje.addActionListener(
                e -> verPorcentaje()
        );

        btnBajo80.addActionListener(
                e -> verEstudiantesBajo80()
        );

        btnSalir.addActionListener(
                e -> salir()
        );
    }


    /*
     * ========================================================
     * REGISTRAR MARCAJE
     * ========================================================
     */
    private void registrarMarcaje() {

        Estudiante estudiante =
                (Estudiante) cmbEstudiante.getSelectedItem();

        SesionClase sesion =
                (SesionClase) cmbSesion.getSelectedItem();

        Dispositivo dispositivo =
                (Dispositivo) cmbDispositivo.getSelectedItem();

        TipoMarcaje tipo =
                (TipoMarcaje) cmbTipo.getSelectedItem();

        MedioMarcaje medio =
                (MedioMarcaje) cmbMedio.getSelectedItem();

        if (estudiante == null || sesion == null || dispositivo == null) {

            mostrarError(
                    "Debe seleccionar estudiante, sesión "
                            + "y dispositivo."
            );

            return;
        }

        try {

            Marcaje marcaje =
                    asistenciaController.registrarMarcaje(
                            estudiante.getIdEstudiante(),
                            sesion.getIdSesion(),
                            dispositivo.getIdDispositivo(),
                            tipo,
                            medio
                    );

            txtResultado.setText(
                    "Marcaje "
                            + marcaje.getTipo()
                            + " registrado a las "
                            + marcaje.getFechaHora()
            );

            JOptionPane.showMessageDialog(
                    panelPrincipal,
                    "Marcaje registrado correctamente.",
                    "Asistencia",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }


    /*
     * ========================================================
     * HISTORIAL DE MARCAJES
     * ========================================================
     */
    private void verHistorial() {

        Estudiante estudiante =
                (Estudiante) cmbEstudiante.getSelectedItem();

        if (estudiante == null) {

            mostrarError(
                    "Debe seleccionar un estudiante."
            );

            return;
        }

        try {

            List<Marcaje> historial =
                    asistenciaController.obtenerHistorial(
                            estudiante.getIdEstudiante()
                    );

            configurarTablaMarcajes();

            DefaultTableModel modelo =
                    (DefaultTableModel)
                            tblResultado.getModel();

            for (Marcaje marcaje : historial) {

                modelo.addRow(
                        new Object[]{
                                marcaje.getIdMarcaje(),
                                marcaje.getSesionClase().getSeccion().getCodigo(),
                                marcaje.getDispositivo().getCodigo(),
                                marcaje.getFechaHora(),
                                marcaje.getTipo(),
                                marcaje.getMedio()
                        }
                );
            }

            txtResultado.setText(
                    historial.size()
                            + " marcaje(s) encontrados para "
                            + estudiante.getCarnet()
            );

        } catch (Exception e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }


    /*
     * ========================================================
     * PORCENTAJE DE ASISTENCIA
     * ========================================================
     */
    private void verPorcentaje() {

        Estudiante estudiante =
                (Estudiante) cmbEstudiante.getSelectedItem();

        if (estudiante == null) {

            mostrarError(
                    "Debe seleccionar un estudiante."
            );

            return;
        }

        try {

            double porcentaje =
                    asistenciaController.calcularPorcentajeAsistencia(
                            estudiante.getIdEstudiante()
                    );

            txtResultado.setText(
                    String.format(
                            "%s tiene %.1f%% de asistencia.",
                            estudiante.getCarnet(),
                            porcentaje
                    )
            );

        } catch (Exception e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }


    /*
     * ========================================================
     * ESTUDIANTES BAJO 80% DE ASISTENCIA
     * ========================================================
     */
    private void verEstudiantesBajo80() {

        try {

            List<Estudiante> estudiantes =
                    asistenciaController.obtenerEstudiantesBajo80();

            configurarTablaBajo80();

            DefaultTableModel modelo =
                    (DefaultTableModel)
                            tblResultado.getModel();

            for (Estudiante estudiante : estudiantes) {

                double porcentaje =
                        asistenciaController.calcularPorcentajeAsistencia(
                                estudiante.getIdEstudiante()
                        );

                modelo.addRow(
                        new Object[]{
                                estudiante.getIdEstudiante(),
                                estudiante.getCarnet(),
                                estudiante.getNombres()
                                        + " "
                                        + estudiante.getApellidos(),

                                String.format(
                                        "%.1f%%",
                                        porcentaje
                                )
                        }
                );
            }

            txtResultado.setText(
                    estudiantes.size()
                            + " estudiante(s) con menos del 80% "
                            + "de asistencia."
            );

        } catch (Exception e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }


    /*
     * La tabla de resultados cambia de columnas según la
     * consulta: historial de marcajes o estudiantes bajo 80%.
     */
    private void configurarTablaMarcajes() {

        DefaultTableModel modelo =
                new DefaultTableModel(
                        new Object[]{
                                "ID Marcaje",
                                "Sección",
                                "Dispositivo",
                                "Fecha y Hora",
                                "Tipo",
                                "Medio"
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

        tblResultado.setModel(modelo);
    }

    private void configurarTablaBajo80() {

        DefaultTableModel modelo =
                new DefaultTableModel(
                        new Object[]{
                                "ID",
                                "Carnet",
                                "Nombre",
                                "% Asistencia"
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

        tblResultado.setModel(modelo);
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
