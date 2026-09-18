package universidad.asistencia.view;

import universidad.asistencia.controller.SeccionController;
import universidad.asistencia.controller.SesionClaseController;
import universidad.asistencia.enums.EstadoSesion;
import universidad.asistencia.model.Seccion;
import universidad.asistencia.model.SesionClase;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

public class SesionClaseView {

    // Componentes vinculados desde SesionClaseView.form
    private JPanel panelPrincipal;

    private JTextField txtId;
    private JComboBox cmbSeccion;
    private JTextField txtFecha;
    private JTextField txtHoraInicio;
    private JTextField txtHoraFin;
    private JTextField txtAula;
    private JComboBox cmbEstado;

    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnCambiarEstado;

    private JTextField txtBuscar;
    private JButton btnBuscar;

    private JTable tblSesiones;
    private JButton btnSalir;

    // La View solamente conoce a los Controllers.
    private final SesionClaseController sesionClaseController;
    private final SeccionController seccionController;


    /*
     * Constructor normal.
     */
    public SesionClaseView() {

        this(
                new SesionClaseController(),
                new SeccionController()
        );
    }


    /*
     * También permitimos recibir los Controllers desde afuera.
     */
    public SesionClaseView(
            SesionClaseController sesionClaseController,
            SeccionController seccionController
    ) {

        this.sesionClaseController = sesionClaseController;
        this.seccionController = seccionController;

        configurarFormulario();
        configurarEventos();
        cargarCombos();
        cargarSesiones();
    }


    /*
     * Configuración inicial.
     */
    private void configurarFormulario() {

        // El ID lo genera SQL Server.
        txtId.setEditable(false);

        cmbEstado.setModel(
                new DefaultComboBoxModel<>(
                        EstadoSesion.values()
                )
        );

        btnActualizar.setEnabled(false);

        btnCambiarEstado.setEnabled(false);

        configurarCombos();

        configurarTabla();
    }


    /*
     * El combo de secciones muestra texto legible en vez del
     * toString() por defecto de Seccion.
     */
    @SuppressWarnings("unchecked")
    private void configurarCombos() {

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
     * Carga la lista de secciones en el combo. Usamos listar()
     * (no listarActivos()) para poder seguir mostrando sesiones
     * de una sección que ya se haya desactivado.
     */
    @SuppressWarnings("unchecked")
    private void cargarCombos() {

        try {

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
                                "Sección",
                                "Fecha",
                                "Hora Inicio",
                                "Hora Fin",
                                "Aula",
                                "Estado"
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

        tblSesiones.setModel(modelo);

        tblSesiones.setSelectionMode(
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

        btnCambiarEstado.addActionListener(
                e -> cambiarEstado()
        );

        btnBuscar.addActionListener(
                e -> buscar()
        );

        btnSalir.addActionListener(
                e -> salir()
        );

        /*
         * Cuando seleccionamos una fila,
         * recuperamos la sesión utilizando su ID.
         */
        tblSesiones
                .getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {
                        seleccionarSesion();
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

            Seccion seccion =
                    (Seccion) cmbSeccion.getSelectedItem();

            if (seccion == null) {

                mostrarError(
                        "Debe seleccionar una sección."
                );

                return;
            }

            LocalDate fecha =
                    leerFecha(txtFecha, "La fecha");

            LocalTime horaInicio =
                    leerHora(txtHoraInicio, "La hora de inicio");

            LocalTime horaFin =
                    leerHora(txtHoraFin, "La hora de fin");

            SesionClase sesion =
                    new SesionClase(
                            seccion,
                            fecha,
                            horaInicio,
                            horaFin,
                            txtAula.getText().trim(),
                            (EstadoSesion) cmbEstado.getSelectedItem()
                    );

            boolean guardado =
                    sesionClaseController.guardar(
                            sesion
                    );

            if (guardado) {

                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        "Sesión guardada correctamente.\n"
                                + "ID generado: "
                                + sesion.getIdSesion(),
                        "Sesión",
                        JOptionPane.INFORMATION_MESSAGE
                );

                limpiarFormulario();

                cargarSesiones();

            } else {

                mostrarError(
                        "No se pudo guardar la sesión."
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
    private void cargarSesiones() {

        try {

            List<SesionClase> sesiones =
                    sesionClaseController.listar();

            llenarTabla(sesiones);

        } catch (Exception e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }


    private void llenarTabla(
            List<SesionClase> sesiones
    ) {

        DefaultTableModel modelo =
                (DefaultTableModel)
                        tblSesiones.getModel();

        modelo.setRowCount(0);

        for (SesionClase sesion : sesiones) {

            modelo.addRow(
                    new Object[]{
                            sesion.getIdSesion(),
                            sesion.getSeccion().getCodigo(),
                            sesion.getFecha(),
                            sesion.getHoraInicioProgramada(),
                            sesion.getHoraFinProgramada(),
                            sesion.getAula(),
                            sesion.getEstado()
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
                    "Debe seleccionar una sesión.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        try {

            int idSesion =
                    Integer.parseInt(
                            txtId.getText()
                    );

            Seccion seccion =
                    (Seccion) cmbSeccion.getSelectedItem();

            if (seccion == null) {

                mostrarError(
                        "Debe seleccionar una sección."
                );

                return;
            }

            LocalDate fecha =
                    leerFecha(txtFecha, "La fecha");

            LocalTime horaInicio =
                    leerHora(txtHoraInicio, "La hora de inicio");

            LocalTime horaFin =
                    leerHora(txtHoraFin, "La hora de fin");

            SesionClase sesion =
                    new SesionClase(
                            idSesion,
                            seccion,
                            fecha,
                            horaInicio,
                            horaFin,
                            txtAula.getText().trim(),
                            (EstadoSesion) cmbEstado.getSelectedItem()
                    );

            boolean actualizado =
                    sesionClaseController.actualizar(
                            sesion
                    );

            if (actualizado) {

                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        "Sesión actualizada correctamente.",
                        "Sesión",
                        JOptionPane.INFORMATION_MESSAGE
                );

                limpiarFormulario();

                cargarSesiones();

            } else {

                mostrarError(
                        "No se pudo actualizar la sesión."
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
     * CAMBIAR ESTADO
     *
     * Es una operación liviana: solo toca la columna "estado",
     * no reescribe el resto de la sesión.
     * ========================================================
     */
    private void cambiarEstado() {

        if (txtId.getText().isBlank()) {

            JOptionPane.showMessageDialog(
                    panelPrincipal,
                    "Debe seleccionar una sesión.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        try {

            int idSesion =
                    Integer.parseInt(
                            txtId.getText()
                    );

            EstadoSesion estado =
                    (EstadoSesion) cmbEstado.getSelectedItem();

            boolean cambiado =
                    sesionClaseController.cambiarEstado(
                            idSesion,
                            estado
                    );

            if (cambiado) {

                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        "Estado de la sesión actualizado a "
                                + estado
                                + ".",
                        "Sesión",
                        JOptionPane.INFORMATION_MESSAGE
                );

                cargarSesiones();

                seleccionarFilaPorId(
                        idSesion
                );

            } else {

                mostrarError(
                        "No se pudo cambiar el estado "
                                + "de la sesión."
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
     * BUSCAR POR ID DE SECCIÓN
     * ========================================================
     */
    private void buscar() {

        String texto =
                txtBuscar.getText().trim();

        if (texto.isBlank()) {

            cargarSesiones();

            return;
        }

        try {

            int idSeccion =
                    Integer.parseInt(texto);

            List<SesionClase> resultado =
                    sesionClaseController
                            .listarPorSeccion(idSeccion);

            if (resultado.isEmpty()) {

                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        "No se encontraron sesiones "
                                + "para esa sección.",
                        "Búsqueda",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

            llenarTabla(resultado);

        } catch (NumberFormatException e) {

            mostrarError(
                    "El ID de sección debe ser numérico."
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
    private void seleccionarSesion() {

        int fila =
                tblSesiones.getSelectedRow();

        if (fila == -1) {
            return;
        }

        try {

            int idSesion =
                    Integer.parseInt(
                            tblSesiones
                                    .getValueAt(
                                            fila,
                                            0
                                    )
                                    .toString()
                    );

            Optional<SesionClase> resultado =
                    sesionClaseController.buscar(
                            idSesion
                    );

            if (resultado.isPresent()) {

                mostrarSesionEnFormulario(
                        resultado.get()
                );
            }

        } catch (Exception e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }


    private void mostrarSesionEnFormulario(
            SesionClase sesion
    ) {

        txtId.setText(
                String.valueOf(
                        sesion.getIdSesion()
                )
        );

        seleccionarSeccionEnCombo(
                sesion.getSeccion().getIdSeccion()
        );

        txtFecha.setText(
                String.valueOf(
                        sesion.getFecha()
                )
        );

        txtHoraInicio.setText(
                String.valueOf(
                        sesion.getHoraInicioProgramada()
                )
        );

        txtHoraFin.setText(
                String.valueOf(
                        sesion.getHoraFinProgramada()
                )
        );

        txtAula.setText(
                sesion.getAula()
        );

        cmbEstado.setSelectedItem(
                sesion.getEstado()
        );

        btnGuardar.setEnabled(false);

        btnActualizar.setEnabled(true);

        btnCambiarEstado.setEnabled(true);
    }


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


    private void seleccionarFilaPorId(
            int idSesion
    ) {

        for (
                int fila = 0;
                fila < tblSesiones.getRowCount();
                fila++
        ) {

            int idTabla =
                    Integer.parseInt(
                            tblSesiones
                                    .getValueAt(
                                            fila,
                                            0
                                    )
                                    .toString()
                    );

            if (idTabla == idSesion) {

                tblSesiones.setRowSelectionInterval(
                        fila,
                        fila
                );

                tblSesiones.scrollRectToVisible(
                        tblSesiones
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

        txtId.setText("");

        if (cmbSeccion.getItemCount() > 0) {
            cmbSeccion.setSelectedIndex(0);
        }

        txtFecha.setText("");

        txtHoraInicio.setText("");

        txtHoraFin.setText("");

        txtAula.setText("");

        cmbEstado.setSelectedItem(
                EstadoSesion.PROGRAMADA
        );

        txtBuscar.setText("");

        tblSesiones.clearSelection();

        btnGuardar.setEnabled(true);

        btnActualizar.setEnabled(false);

        btnCambiarEstado.setEnabled(false);
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
     * Lee una hora (HH:mm) de un campo de texto con
     * un mensaje de error claro si el formato es inválido.
     */
    private LocalTime leerHora(
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

            return LocalTime.parse(texto);

        } catch (DateTimeParseException e) {

            throw new IllegalArgumentException(
                    nombreCampo + " debe tener el formato HH:mm."
            );
        }
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
