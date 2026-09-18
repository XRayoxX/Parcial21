package universidad.asistencia.view;

import universidad.asistencia.controller.EstudianteController;
import universidad.asistencia.controller.InscripcionController;
import universidad.asistencia.controller.SeccionController;
import universidad.asistencia.model.Estudiante;
import universidad.asistencia.model.Inscripcion;
import universidad.asistencia.model.Seccion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

public class InscripcionesView {

    // Componentes vinculados desde InscripcionesView.form
    private JPanel panelPrincipal;

    private JTextField txtId;
    private JComboBox cmbEstudiante;
    private JComboBox cmbSeccion;
    private JTextField txtFechaInscripcion;
    private JTextField txtFechaRetiro;

    private JCheckBox chkActivo;

    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnDesactivar;

    private JTextField txtBuscar;
    private JButton btnBuscar;

    private JTable tblInscripciones;
    private JButton btnSalir;

    // La View solamente conoce a los Controllers.
    private final InscripcionController inscripcionController;
    private final EstudianteController estudianteController;
    private final SeccionController seccionController;


    /*
     * Constructor normal.
     */
    public InscripcionesView() {

        this(
                new InscripcionController(),
                new EstudianteController(),
                new SeccionController()
        );
    }


    /*
     * También permitimos recibir los Controllers desde afuera.
     */
    public InscripcionesView(
            InscripcionController inscripcionController,
            EstudianteController estudianteController,
            SeccionController seccionController
    ) {

        this.inscripcionController = inscripcionController;
        this.estudianteController = estudianteController;
        this.seccionController = seccionController;

        configurarFormulario();
        configurarEventos();
        cargarCombos();
        cargarInscripciones();
    }


    /*
     * Configuración inicial.
     */
    private void configurarFormulario() {

        // El ID lo genera SQL Server.
        txtId.setEditable(false);

        /*
         * El estado y la fecha de retiro no se editan directamente:
         * se completan automáticamente al usar el botón Retirar.
         */
        chkActivo.setEnabled(false);
        chkActivo.setSelected(true);

        txtFechaRetiro.setEditable(false);

        btnActualizar.setEnabled(false);

        btnDesactivar.setText("Retirar");
        btnDesactivar.setEnabled(false);

        configurarCombos();

        configurarTabla();
    }


    /*
     * Los combos muestran texto legible en vez del toString()
     * por defecto de Estudiante/Seccion.
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

        cmbSeccion.setRenderer(new DefaultListCellRenderer() {

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

                if (value instanceof Seccion seccion) {

                    setText(
                            seccion.getCodigo()
                                    + " - "
                                    + seccion.getCurso().getNombre()
                    );
                }

                return this;
            }
        });
    }


    /*
     * Carga la lista de estudiantes y secciones en los combos.
     *
     * Usamos listar() (no listarActivos()) para que, al editar una
     * inscripción existente, el combo pueda seguir mostrando un
     * estudiante o sección que ya se haya desactivado.
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

            DefaultComboBoxModel<Seccion> modeloSecciones =
                    new DefaultComboBoxModel<>();

            for (Seccion seccion : seccionController.listar()) {
                modeloSecciones.addElement(seccion);
            }

            cmbSeccion.setModel(modeloSecciones);

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
                                "Carnet",
                                "Estudiante",
                                "Sección",
                                "Fecha Inscripción",
                                "Fecha Retiro",
                                "Activa"
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

        tblInscripciones.setModel(modelo);

        tblInscripciones.setSelectionMode(
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

        btnActualizar.addActionListener(
                e -> actualizar()
        );

        btnDesactivar.addActionListener(
                e -> retirar()
        );

        btnBuscar.addActionListener(
                e -> buscar()
        );

        btnSalir.addActionListener(
                e -> salir()
        );

        /*
         * Cuando seleccionamos una fila,
         * recuperamos la inscripción utilizando su ID.
         */
        tblInscripciones
                .getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {
                        seleccionarInscripcion();
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

            Seccion seccion =
                    (Seccion) cmbSeccion.getSelectedItem();

            if (estudiante == null || seccion == null) {

                mostrarError(
                        "Debe seleccionar un estudiante y una sección."
                );

                return;
            }

            LocalDate fechaInscripcion =
                    leerFecha(txtFechaInscripcion, "La fecha de inscripción");

            Inscripcion inscripcion =
                    new Inscripcion(
                            estudiante,
                            seccion,
                            fechaInscripcion
                    );

            boolean guardado =
                    inscripcionController.guardar(
                            inscripcion
                    );

            if (guardado) {

                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        "Inscripción guardada correctamente.\n"
                                + "ID generado: "
                                + inscripcion.getIdInscripcion(),
                        "Inscripción",
                        JOptionPane.INFORMATION_MESSAGE
                );

                limpiarFormulario();

                cargarInscripciones();

            } else {

                mostrarError(
                        "No se pudo guardar la inscripción."
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
    private void cargarInscripciones() {

        try {

            List<Inscripcion> inscripciones =
                    inscripcionController.listar();

            llenarTabla(inscripciones);

        } catch (Exception e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }


    /*
     * Llena la tabla con la lista de inscripciones recibida.
     */
    private void llenarTabla(
            List<Inscripcion> inscripciones
    ) {

        DefaultTableModel modelo =
                (DefaultTableModel)
                        tblInscripciones.getModel();

        modelo.setRowCount(0);

        for (Inscripcion inscripcion : inscripciones) {

            modelo.addRow(
                    new Object[]{
                            inscripcion.getIdInscripcion(),
                            inscripcion.getEstudiante().getCarnet(),
                            inscripcion.getEstudiante().getNombres()
                                    + " "
                                    + inscripcion.getEstudiante().getApellidos(),
                            inscripcion.getSeccion().getCodigo(),
                            inscripcion.getFechaInscripcion(),

                            inscripcion.getFechaRetiro() == null
                                    ? ""
                                    : inscripcion.getFechaRetiro(),

                            inscripcion.isActiva()
                                    ? "Sí"
                                    : "No"
                    }
            );
        }
    }


    /*
     * ========================================================
     * UPDATE
     * ========================================================
     */
    private void actualizar() {

        if (txtId.getText().isBlank()) {

            JOptionPane.showMessageDialog(
                    panelPrincipal,
                    "Debe seleccionar una inscripción.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        try {

            int idInscripcion =
                    Integer.parseInt(
                            txtId.getText()
                    );

            Estudiante estudiante =
                    (Estudiante) cmbEstudiante.getSelectedItem();

            Seccion seccion =
                    (Seccion) cmbSeccion.getSelectedItem();

            if (estudiante == null || seccion == null) {

                mostrarError(
                        "Debe seleccionar un estudiante y una sección."
                );

                return;
            }

            LocalDate fechaInscripcion =
                    leerFecha(txtFechaInscripcion, "La fecha de inscripción");

            LocalDate fechaRetiro =
                    txtFechaRetiro.getText().isBlank()
                            ? null
                            : leerFecha(txtFechaRetiro, "La fecha de retiro");

            Inscripcion inscripcion =
                    new Inscripcion(
                            idInscripcion,
                            estudiante,
                            seccion,
                            fechaInscripcion,
                            fechaRetiro,

                            /*
                             * Conservamos el estado actual.
                             */
                            chkActivo.isSelected()
                    );

            boolean actualizado =
                    inscripcionController.actualizar(
                            inscripcion
                    );

            if (actualizado) {

                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        "Inscripción actualizada correctamente.",
                        "Inscripción",
                        JOptionPane.INFORMATION_MESSAGE
                );

                limpiarFormulario();

                cargarInscripciones();

            } else {

                mostrarError(
                        "No se pudo actualizar la inscripción."
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
     * RETIRAR
     *
     * No existe "reactivar" una inscripción: una vez retirada,
     * el Repository no ofrece forma de revertirlo. Por eso el
     * botón siempre dice "Retirar" y se deshabilita si la
     * inscripción ya no está activa.
     * ========================================================
     */
    private void retirar() {

        if (txtId.getText().isBlank()) {

            JOptionPane.showMessageDialog(
                    panelPrincipal,
                    "Debe seleccionar una inscripción.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        String fechaTexto =
                JOptionPane.showInputDialog(
                        panelPrincipal,
                        "Fecha de retiro (AAAA-MM-DD):",
                        LocalDate.now().toString()
                );

        if (fechaTexto == null || fechaTexto.isBlank()) {
            return;
        }

        try {

            LocalDate fechaRetiro =
                    LocalDate.parse(fechaTexto.trim());

            int idInscripcion =
                    Integer.parseInt(
                            txtId.getText()
                    );

            boolean retirado =
                    inscripcionController.retirar(
                            idInscripcion,
                            fechaRetiro
                    );

            if (retirado) {

                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        "Inscripción retirada correctamente.",
                        "Inscripción",
                        JOptionPane.INFORMATION_MESSAGE
                );

                cargarInscripciones();

                seleccionarFilaPorId(
                        idInscripcion
                );

            } else {

                mostrarError(
                        "No se pudo retirar la inscripción."
                );
            }

        } catch (DateTimeParseException e) {

            mostrarError(
                    "La fecha de retiro debe tener el formato AAAA-MM-DD."
            );

        } catch (Exception e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }


    /*
     * ========================================================
     * BUSCAR POR ID DE ESTUDIANTE
     * ========================================================
     */
    private void buscar() {

        String texto =
                txtBuscar.getText().trim();

        /*
         * Si no escribió nada, mostramos nuevamente
         * todas las inscripciones.
         */
        if (texto.isBlank()) {

            cargarInscripciones();

            return;
        }

        try {

            int idEstudiante =
                    Integer.parseInt(texto);

            List<Inscripcion> resultado =
                    inscripcionController
                            .listarPorEstudiante(idEstudiante);

            if (resultado.isEmpty()) {

                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        "No se encontraron inscripciones "
                                + "para ese estudiante.",
                        "Búsqueda",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

            llenarTabla(resultado);

        } catch (NumberFormatException e) {

            mostrarError(
                    "El ID de estudiante debe ser numérico."
            );

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
    private void seleccionarInscripcion() {

        int fila =
                tblInscripciones.getSelectedRow();

        if (fila == -1) {
            return;
        }

        try {

            int idInscripcion =
                    Integer.parseInt(
                            tblInscripciones
                                    .getValueAt(
                                            fila,
                                            0
                                    )
                                    .toString()
                    );

            Optional<Inscripcion> resultado =
                    inscripcionController.buscar(
                            idInscripcion
                    );

            if (resultado.isPresent()) {

                mostrarInscripcionEnFormulario(
                        resultado.get()
                );
            }

        } catch (Exception e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }


    /*
     * Coloca la información del objeto en los controles.
     */
    private void mostrarInscripcionEnFormulario(
            Inscripcion inscripcion
    ) {

        txtId.setText(
                String.valueOf(
                        inscripcion.getIdInscripcion()
                )
        );

        seleccionarEstudianteEnCombo(
                inscripcion.getEstudiante().getIdEstudiante()
        );

        seleccionarSeccionEnCombo(
                inscripcion.getSeccion().getIdSeccion()
        );

        txtFechaInscripcion.setText(
                String.valueOf(
                        inscripcion.getFechaInscripcion()
                )
        );

        txtFechaRetiro.setText(
                inscripcion.getFechaRetiro() == null
                        ? ""
                        : inscripcion.getFechaRetiro().toString()
        );

        chkActivo.setSelected(
                inscripcion.isActiva()
        );


        /*
         * Estamos trabajando con un registro existente.
         */
        btnGuardar.setEnabled(false);

        btnActualizar.setEnabled(true);

        /*
         * Solo se puede retirar una inscripción que sigue activa.
         */
        btnDesactivar.setEnabled(
                inscripcion.isActiva()
        );
    }


    /*
     * Ubica en el combo de estudiantes el que tiene el ID indicado.
     */
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
     * Ubica en el combo de secciones la que tiene el ID indicado.
     */
    private void seleccionarSeccionEnCombo(
            int idSeccion
    ) {

        for (
                int i = 0;
                i < cmbSeccion.getItemCount();
                i++
        ) {

            Seccion seccion =
                    (Seccion) cmbSeccion.getItemAt(i);

            if (seccion.getIdSeccion() == idSeccion) {

                cmbSeccion.setSelectedIndex(i);

                return;
            }
        }
    }


    /*
     * Busca visualmente en la JTable el registro
     * seleccionado por una búsqueda.
     */
    private void seleccionarFilaPorId(
            int idInscripcion
    ) {

        for (
                int fila = 0;
                fila < tblInscripciones.getRowCount();
                fila++
        ) {

            int idTabla =
                    Integer.parseInt(
                            tblInscripciones
                                    .getValueAt(
                                            fila,
                                            0
                                    )
                                    .toString()
                    );

            if (idTabla == idInscripcion) {

                tblInscripciones.setRowSelectionInterval(
                        fila,
                        fila
                );

                tblInscripciones.scrollRectToVisible(
                        tblInscripciones
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


    /*
     * Limpia la pantalla para ingresar una nueva inscripción.
     */
    private void limpiarFormulario() {

        txtId.setText("");

        if (cmbEstudiante.getItemCount() > 0) {
            cmbEstudiante.setSelectedIndex(0);
        }

        if (cmbSeccion.getItemCount() > 0) {
            cmbSeccion.setSelectedIndex(0);
        }

        txtFechaInscripcion.setText("");

        txtFechaRetiro.setText("");

        txtBuscar.setText("");

        chkActivo.setSelected(true);

        tblInscripciones.clearSelection();

        btnGuardar.setEnabled(true);

        btnActualizar.setEnabled(false);

        btnDesactivar.setEnabled(false);
    }


    /*
     * Lee una fecha (AAAA-MM-DD) de un campo de texto con
     * un mensaje de error claro si el formato es inválido.
     */
    private LocalDate leerFecha(
            JTextField campo,
            String nombreCampo
    ) {

        String texto =
                campo.getText().trim();

        if (texto.isBlank()) {

            throw new IllegalArgumentException(
                    nombreCampo + " es obligatoria."
            );
        }

        try {

            return LocalDate.parse(texto);

        } catch (DateTimeParseException e) {

            throw new IllegalArgumentException(
                    nombreCampo + " debe tener el formato AAAA-MM-DD."
            );
        }
    }


    /*
     * Todos los errores de la View pasan por un único método.
     */
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


    /*
     * Salir de este catálogo significa regresar al MainForm.
     */
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


    /*
     * El JFrame principal utilizará este JPanel.
     */
    public JPanel getPanelPrincipal() {

        return panelPrincipal;
    }
}
