package universidad.asistencia.view;

import universidad.asistencia.controller.DispositivoController;
import universidad.asistencia.enums.MedioMarcaje;
import universidad.asistencia.model.Dispositivo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.Optional;

public class DispositivoView {

    // Componentes vinculados desde DispositivoView.form
    private JPanel panelPrincipal;

    private JTextField txtId;
    private JTextField txtCodigo;
    private JTextField txtNombre;
    private JComboBox cmbTipo;
    private JTextField txtUbicacion;

    private JCheckBox chkActivo;

    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnDesactivar;

    private JTextField txtBuscar;
    private JButton btnBuscar;

    private JTable tblDispositivos;
    private JButton btnSalir;

    // La View solamente conoce al Controller.
    private final DispositivoController dispositivoController;


    /*
     * Constructor normal.
     */
    public DispositivoView() {

        this(new DispositivoController());
    }


    /*
     * También permitimos recibir el Controller desde afuera.
     */
    public DispositivoView(
            DispositivoController dispositivoController
    ) {

        this.dispositivoController = dispositivoController;

        configurarFormulario();
        configurarEventos();
        cargarDispositivos();
    }


    /*
     * Configuración inicial.
     */
    private void configurarFormulario() {

        // El ID lo genera SQL Server.
        txtId.setEditable(false);

        /*
         * El tipo debe coincidir con MedioMarcaje: el Service
         * valida MedioMarcaje.valueOf(tipo).
         */
        cmbTipo.setModel(
                new DefaultComboBoxModel<>(
                        MedioMarcaje.values()
                )
        );

        // Todo dispositivo nuevo inicia activo.
        chkActivo.setSelected(true);

        /*
         * El estado no se cambia directamente desde el checkbox.
         * Se cambia utilizando el botón Activar/Desactivar.
         */
        chkActivo.setEnabled(false);

        btnActualizar.setEnabled(false);

        btnDesactivar.setEnabled(false);
        btnDesactivar.setText("Desactivar");

        configurarTabla();
    }


    /*
     * Configuración de JTable.
     */
    private void configurarTabla() {

        DefaultTableModel modelo =
                new DefaultTableModel(
                        new Object[]{
                                "ID",
                                "Código",
                                "Nombre",
                                "Tipo",
                                "Ubicación",
                                "Activo"
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

        tblDispositivos.setModel(modelo);

        tblDispositivos.setSelectionMode(
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

        /*
         * El mismo botón permite activar y desactivar.
         */
        btnDesactivar.addActionListener(
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
         * recuperamos el dispositivo utilizando su ID.
         */
        tblDispositivos
                .getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {
                        seleccionarDispositivo();
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

            Dispositivo dispositivo =
                    new Dispositivo(
                            txtCodigo.getText().trim(),
                            txtNombre.getText().trim(),
                            tipoSeleccionado(),
                            txtUbicacion.getText().trim()
                    );

            boolean guardado =
                    dispositivoController.guardar(
                            dispositivo
                    );

            if (guardado) {

                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        "Dispositivo guardado correctamente.\n"
                                + "ID generado: "
                                + dispositivo.getIdDispositivo(),
                        "Dispositivo",
                        JOptionPane.INFORMATION_MESSAGE
                );

                limpiarFormulario();

                cargarDispositivos();

            } else {

                mostrarError(
                        "No se pudo guardar el dispositivo."
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
    private void cargarDispositivos() {

        try {

            List<Dispositivo> dispositivos =
                    dispositivoController.listar();

            DefaultTableModel modelo =
                    (DefaultTableModel)
                            tblDispositivos.getModel();

            modelo.setRowCount(0);

            for (Dispositivo dispositivo : dispositivos) {

                modelo.addRow(
                        new Object[]{
                                dispositivo.getIdDispositivo(),
                                dispositivo.getCodigo(),
                                dispositivo.getNombre(),
                                dispositivo.getTipo(),
                                dispositivo.getUbicacion(),

                                dispositivo.isActivo()
                                        ? "Sí"
                                        : "No"
                        }
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
     * UPDATE
     * ========================================================
     */
    private void actualizar() {

        if (txtId.getText().isBlank()) {

            JOptionPane.showMessageDialog(
                    panelPrincipal,
                    "Debe seleccionar un dispositivo.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        try {

            int idDispositivo =
                    Integer.parseInt(
                            txtId.getText()
                    );

            Dispositivo dispositivo =
                    new Dispositivo(
                            idDispositivo,
                            txtCodigo.getText().trim(),
                            txtNombre.getText().trim(),
                            tipoSeleccionado(),
                            txtUbicacion.getText().trim(),

                            /*
                             * Conservamos el estado actual.
                             */
                            chkActivo.isSelected()
                    );

            boolean actualizado =
                    dispositivoController.actualizar(
                            dispositivo
                    );

            if (actualizado) {

                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        "Dispositivo actualizado correctamente.",
                        "Dispositivo",
                        JOptionPane.INFORMATION_MESSAGE
                );

                limpiarFormulario();

                cargarDispositivos();

            } else {

                mostrarError(
                        "No se pudo actualizar el dispositivo."
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
     * ========================================================
     */
    private void cambiarEstado() {

        if (txtId.getText().isBlank()) {

            JOptionPane.showMessageDialog(
                    panelPrincipal,
                    "Debe seleccionar un dispositivo.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        boolean estadoActual =
                chkActivo.isSelected();

        boolean nuevoEstado =
                !estadoActual;

        String accion =
                nuevoEstado
                        ? "activar"
                        : "desactivar";

        String accionTitulo =
                nuevoEstado
                        ? "Activar"
                        : "Desactivar";

        int respuesta =
                JOptionPane.showConfirmDialog(
                        panelPrincipal,
                        "¿Está seguro de "
                                + accion
                                + " el dispositivo?",
                        "Confirmar "
                                + accionTitulo,
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            int idDispositivo =
                    Integer.parseInt(
                            txtId.getText()
                    );

            Dispositivo dispositivo =
                    new Dispositivo(
                            idDispositivo,
                            txtCodigo.getText().trim(),
                            txtNombre.getText().trim(),
                            tipoSeleccionado(),
                            txtUbicacion.getText().trim(),
                            nuevoEstado
                    );

            boolean actualizado =
                    dispositivoController.actualizar(
                            dispositivo
                    );

            if (actualizado) {

                String mensaje =
                        nuevoEstado
                                ? "Dispositivo activado correctamente."
                                : "Dispositivo desactivado correctamente.";

                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        mensaje,
                        "Dispositivo",
                        JOptionPane.INFORMATION_MESSAGE
                );

                chkActivo.setSelected(
                        nuevoEstado
                );

                actualizarTextoBotonEstado(
                        nuevoEstado
                );

                cargarDispositivos();

                seleccionarFilaPorId(
                        idDispositivo
                );

            } else {

                mostrarError(
                        "No se pudo cambiar el estado "
                                + "del dispositivo."
                );
            }

        } catch (Exception e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }


    private void actualizarTextoBotonEstado(
            boolean activo
    ) {

        if (activo) {

            btnDesactivar.setText(
                    "Desactivar"
            );

        } else {

            btnDesactivar.setText(
                    "Activar"
            );
        }
    }


    /*
     * ========================================================
     * BUSCAR POR CÓDIGO
     * ========================================================
     */
    private void buscar() {

        String codigo =
                txtBuscar.getText().trim();

        if (codigo.isBlank()) {

            cargarDispositivos();

            return;
        }

        try {

            Optional<Dispositivo> resultado =
                    dispositivoController
                            .buscarPorCodigo(codigo);

            if (resultado.isPresent()) {

                Dispositivo dispositivo =
                        resultado.get();

                mostrarDispositivoEnFormulario(
                        dispositivo
                );

                seleccionarFilaPorId(
                        dispositivo.getIdDispositivo()
                );

            } else {

                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        "No se encontró ningún dispositivo "
                                + "con ese código.",
                        "Búsqueda",
                        JOptionPane.INFORMATION_MESSAGE
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
     * SELECCIÓN DESDE JTable
     * ========================================================
     */
    private void seleccionarDispositivo() {

        int fila =
                tblDispositivos.getSelectedRow();

        if (fila == -1) {
            return;
        }

        try {

            int idDispositivo =
                    Integer.parseInt(
                            tblDispositivos
                                    .getValueAt(
                                            fila,
                                            0
                                    )
                                    .toString()
                    );

            Optional<Dispositivo> resultado =
                    dispositivoController.buscar(
                            idDispositivo
                    );

            if (resultado.isPresent()) {

                mostrarDispositivoEnFormulario(
                        resultado.get()
                );
            }

        } catch (Exception e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }


    private void mostrarDispositivoEnFormulario(
            Dispositivo dispositivo
    ) {

        txtId.setText(
                String.valueOf(
                        dispositivo.getIdDispositivo()
                )
        );

        txtCodigo.setText(
                dispositivo.getCodigo()
        );

        txtNombre.setText(
                dispositivo.getNombre()
        );

        seleccionarTipoEnCombo(
                dispositivo.getTipo()
        );

        txtUbicacion.setText(
                dispositivo.getUbicacion()
        );

        chkActivo.setSelected(
                dispositivo.isActivo()
        );

        actualizarTextoBotonEstado(
                dispositivo.isActivo()
        );

        btnGuardar.setEnabled(false);

        btnActualizar.setEnabled(true);

        btnDesactivar.setEnabled(true);
    }


    /*
     * Busca visualmente en la JTable el registro
     * seleccionado por una búsqueda.
     */
    private void seleccionarFilaPorId(
            int idDispositivo
    ) {

        for (
                int fila = 0;
                fila < tblDispositivos.getRowCount();
                fila++
        ) {

            int idTabla =
                    Integer.parseInt(
                            tblDispositivos
                                    .getValueAt(
                                            fila,
                                            0
                                    )
                                    .toString()
                    );

            if (idTabla == idDispositivo) {

                tblDispositivos.setRowSelectionInterval(
                        fila,
                        fila
                );

                tblDispositivos.scrollRectToVisible(
                        tblDispositivos
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

        txtCodigo.requestFocus();
    }


    private void limpiarFormulario() {

        txtId.setText("");

        txtCodigo.setText("");

        txtNombre.setText("");

        cmbTipo.setSelectedIndex(0);

        txtUbicacion.setText("");

        txtBuscar.setText("");

        chkActivo.setSelected(true);

        tblDispositivos.clearSelection();

        btnGuardar.setEnabled(true);

        btnActualizar.setEnabled(false);

        btnDesactivar.setEnabled(false);

        btnDesactivar.setText(
                "Desactivar"
        );
    }


    /*
     * El tipo se guarda como texto (nombre del enum MedioMarcaje).
     */
    private String tipoSeleccionado() {

        MedioMarcaje tipo =
                (MedioMarcaje) cmbTipo.getSelectedItem();

        return tipo == null
                ? null
                : tipo.name();
    }

    private void seleccionarTipoEnCombo(
            String tipo
    ) {

        if (tipo == null) {
            return;
        }

        try {

            cmbTipo.setSelectedItem(
                    MedioMarcaje.valueOf(tipo)
            );

        } catch (IllegalArgumentException ignored) {
            // Si el dato en BD no coincide con el enum, dejamos el combo como está.
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
