package universidad.asistencia.view;

import universidad.asistencia.controller.DocenteController;
import universidad.asistencia.controller.UsuarioController;
import universidad.asistencia.enums.RolUsuario;
import universidad.asistencia.model.Docente;
import universidad.asistencia.model.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class UsuarioView {

    // Componentes vinculados desde UsuarioView.form
    private JPanel panelPrincipal;

    private JTextField txtId;
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JComboBox cmbRol;
    private JComboBox cmbDocente;

    private JCheckBox chkActivo;

    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnDesactivar;

    private JTextField txtBuscar;
    private JButton btnBuscar;

    private JTable tblUsuarios;
    private JButton btnSalir;

    // La View solamente conoce a los Controllers.
    private final UsuarioController usuarioController;
    private final DocenteController docenteController;

    /*
     * El passwordHash y la fecha de creación del usuario
     * seleccionado no se editan desde este formulario: se
     * conservan aquí para reenviarlos tal cual en Actualizar.
     */
    private String passwordHashSeleccionado;
    private LocalDateTime fechaCreacionSeleccionada;


    /*
     * Constructor normal.
     */
    public UsuarioView() {

        this(
                new UsuarioController(),
                new DocenteController()
        );
    }


    /*
     * También permitimos recibir los Controllers desde afuera.
     */
    public UsuarioView(
            UsuarioController usuarioController,
            DocenteController docenteController
    ) {

        this.usuarioController = usuarioController;
        this.docenteController = docenteController;

        configurarFormulario();
        configurarEventos();
        cargarCombos();
        cargarUsuarios();
    }


    /*
     * Configuración inicial.
     */
    private void configurarFormulario() {

        // El ID lo genera SQL Server.
        txtId.setEditable(false);

        cmbRol.setModel(
                new DefaultComboBoxModel<>(
                        RolUsuario.values()
                )
        );

        chkActivo.setSelected(true);
        chkActivo.setEnabled(false);

        btnActualizar.setEnabled(false);

        btnDesactivar.setEnabled(false);
        btnDesactivar.setText("Desactivar");

        configurarCombos();

        configurarTabla();
    }


    /*
     * El combo de docentes muestra texto legible, y el de
     * docente se habilita/deshabilita según el rol elegido:
     * un ADMIN no debe tener docente relacionado.
     */
    @SuppressWarnings("unchecked")
    private void configurarCombos() {

        cmbDocente.setRenderer(new DefaultListCellRenderer() {

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
        });

        cmbRol.addItemListener(e -> {

            if (e.getStateChange() != java.awt.event.ItemEvent.SELECTED) {
                return;
            }

            actualizarDisponibilidadDocente();
        });
    }


    /*
     * Habilita el combo de docente solo cuando el rol es DOCENTE.
     */
    private void actualizarDisponibilidadDocente() {

        boolean esDocente =
                cmbRol.getSelectedItem() == RolUsuario.DOCENTE;

        cmbDocente.setEnabled(esDocente);

        if (!esDocente) {
            cmbDocente.setSelectedIndex(-1);
        }
    }


    /*
     * Carga la lista de docentes en el combo. Usamos listar()
     * (no listarActivos()) para poder seguir mostrando el
     * docente de un usuario existente aunque ya esté inactivo.
     */
    @SuppressWarnings("unchecked")
    private void cargarCombos() {

        try {

            DefaultComboBoxModel<Docente> modeloDocentes =
                    new DefaultComboBoxModel<>();

            for (Docente docente : docenteController.listar()) {
                modeloDocentes.addElement(docente);
            }

            cmbDocente.setModel(modeloDocentes);

            actualizarDisponibilidadDocente();

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
                                "Usuario",
                                "Rol",
                                "Docente",
                                "Activo",
                                "Fecha Creación"
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

        tblUsuarios.setModel(modelo);

        tblUsuarios.setSelectionMode(
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
                e -> cambiarEstado()
        );

        btnBuscar.addActionListener(
                e -> buscar()
        );

        btnSalir.addActionListener(
                e -> salir()
        );

        tblUsuarios
                .getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {
                        seleccionarUsuario();
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

            RolUsuario rol =
                    (RolUsuario) cmbRol.getSelectedItem();

            Docente docente =
                    rol == RolUsuario.DOCENTE
                            ? (Docente) cmbDocente.getSelectedItem()
                            : null;

            char[] passwordChars =
                    txtPassword.getPassword();

            String password =
                    new String(passwordChars);

            Usuario creado;

            try {

                creado =
                        usuarioController.crearUsuario(
                                txtUsuario.getText().trim(),
                                password,
                                rol,
                                docente
                        );

            } finally {

                /*
                 * Limpiamos la contraseña de memoria tan pronto
                 * como termina de usarse.
                 */
                java.util.Arrays.fill(passwordChars, ' ');
            }

            JOptionPane.showMessageDialog(
                    panelPrincipal,
                    "Usuario guardado correctamente.\n"
                            + "ID generado: "
                            + creado.getIdUsuario(),
                    "Usuario",
                    JOptionPane.INFORMATION_MESSAGE
            );

            limpiarFormulario();

            cargarUsuarios();

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
    private void cargarUsuarios() {

        try {

            List<Usuario> usuarios =
                    usuarioController.listar();

            DefaultTableModel modelo =
                    (DefaultTableModel)
                            tblUsuarios.getModel();

            modelo.setRowCount(0);

            for (Usuario usuario : usuarios) {

                modelo.addRow(
                        new Object[]{
                                usuario.getIdUsuario(),
                                usuario.getUsuario(),
                                usuario.getRol(),

                                usuario.getDocente() == null
                                        ? "-"
                                        : usuario.getDocente().getCodigoEmpleado(),

                                usuario.isActivo()
                                        ? "Sí"
                                        : "No",

                                usuario.getFechaCreacion()
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
     *
     * Nunca toca la contraseña: siempre reenvía el passwordHash
     * y la fechaCreacion del usuario que se cargó al seleccionar.
     * ========================================================
     */
    private void actualizar() {

        if (txtId.getText().isBlank()) {

            JOptionPane.showMessageDialog(
                    panelPrincipal,
                    "Debe seleccionar un usuario.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        try {

            int idUsuario =
                    Integer.parseInt(
                            txtId.getText()
                    );

            RolUsuario rol =
                    (RolUsuario) cmbRol.getSelectedItem();

            Docente docente =
                    rol == RolUsuario.DOCENTE
                            ? (Docente) cmbDocente.getSelectedItem()
                            : null;

            Usuario usuario =
                    new Usuario(
                            idUsuario,
                            txtUsuario.getText().trim(),
                            passwordHashSeleccionado,
                            rol,
                            docente,

                            /*
                             * Conservamos el estado actual.
                             */
                            chkActivo.isSelected(),

                            fechaCreacionSeleccionada
                    );

            boolean actualizado =
                    usuarioController.actualizar(
                            usuario
                    );

            if (actualizado) {

                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        "Usuario actualizado correctamente.",
                        "Usuario",
                        JOptionPane.INFORMATION_MESSAGE
                );

                limpiarFormulario();

                cargarUsuarios();

            } else {

                mostrarError(
                        "No se pudo actualizar el usuario."
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
                    "Debe seleccionar un usuario.",
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
                                + " el usuario?",
                        "Confirmar "
                                + accionTitulo,
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            int idUsuario =
                    Integer.parseInt(
                            txtId.getText()
                    );

            RolUsuario rol =
                    (RolUsuario) cmbRol.getSelectedItem();

            Docente docente =
                    rol == RolUsuario.DOCENTE
                            ? (Docente) cmbDocente.getSelectedItem()
                            : null;

            Usuario usuario =
                    new Usuario(
                            idUsuario,
                            txtUsuario.getText().trim(),
                            passwordHashSeleccionado,
                            rol,
                            docente,
                            nuevoEstado,
                            fechaCreacionSeleccionada
                    );

            boolean actualizado =
                    usuarioController.actualizar(
                            usuario
                    );

            if (actualizado) {

                String mensaje =
                        nuevoEstado
                                ? "Usuario activado correctamente."
                                : "Usuario desactivado correctamente.";

                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        mensaje,
                        "Usuario",
                        JOptionPane.INFORMATION_MESSAGE
                );

                chkActivo.setSelected(
                        nuevoEstado
                );

                actualizarTextoBotonEstado(
                        nuevoEstado
                );

                cargarUsuarios();

                seleccionarFilaPorId(
                        idUsuario
                );

            } else {

                mostrarError(
                        "No se pudo cambiar el estado "
                                + "del usuario."
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

        btnDesactivar.setText(
                activo
                        ? "Desactivar"
                        : "Activar"
        );
    }


    /*
     * ========================================================
     * BUSCAR POR NOMBRE DE USUARIO
     * ========================================================
     */
    private void buscar() {

        String nombre =
                txtBuscar.getText().trim();

        if (nombre.isBlank()) {

            cargarUsuarios();

            return;
        }

        try {

            Optional<Usuario> resultado =
                    usuarioController
                            .buscarPorUsuario(nombre);

            if (resultado.isPresent()) {

                Usuario usuario =
                        resultado.get();

                mostrarUsuarioEnFormulario(
                        usuario
                );

                seleccionarFilaPorId(
                        usuario.getIdUsuario()
                );

            } else {

                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        "No se encontró ningún usuario "
                                + "con ese nombre.",
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
    private void seleccionarUsuario() {

        int fila =
                tblUsuarios.getSelectedRow();

        if (fila == -1) {
            return;
        }

        try {

            int idUsuario =
                    Integer.parseInt(
                            tblUsuarios
                                    .getValueAt(
                                            fila,
                                            0
                                    )
                                    .toString()
                    );

            Optional<Usuario> resultado =
                    usuarioController.buscar(
                            idUsuario
                    );

            if (resultado.isPresent()) {

                mostrarUsuarioEnFormulario(
                        resultado.get()
                );
            }

        } catch (Exception e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }


    private void mostrarUsuarioEnFormulario(
            Usuario usuario
    ) {

        txtId.setText(
                String.valueOf(
                        usuario.getIdUsuario()
                )
        );

        txtUsuario.setText(
                usuario.getUsuario()
        );

        /*
         * Nunca se muestra ni se edita la contraseña existente.
         */
        txtPassword.setText("");
        txtPassword.setEditable(false);

        passwordHashSeleccionado =
                usuario.getPasswordHash();

        fechaCreacionSeleccionada =
                usuario.getFechaCreacion();

        cmbRol.setSelectedItem(
                usuario.getRol()
        );

        actualizarDisponibilidadDocente();

        if (usuario.getDocente() != null) {

            seleccionarDocenteEnCombo(
                    usuario.getDocente().getIdDocente()
            );
        }

        chkActivo.setSelected(
                usuario.isActivo()
        );

        actualizarTextoBotonEstado(
                usuario.isActivo()
        );

        btnGuardar.setEnabled(false);

        btnActualizar.setEnabled(true);

        btnDesactivar.setEnabled(true);
    }


    private void seleccionarDocenteEnCombo(
            int idDocente
    ) {

        for (
                int i = 0;
                i < cmbDocente.getItemCount();
                i++
        ) {

            Docente docente =
                    (Docente) cmbDocente.getItemAt(i);

            if (docente.getIdDocente() == idDocente) {

                cmbDocente.setSelectedIndex(i);

                return;
            }
        }
    }


    private void seleccionarFilaPorId(
            int idUsuario
    ) {

        for (
                int fila = 0;
                fila < tblUsuarios.getRowCount();
                fila++
        ) {

            int idTabla =
                    Integer.parseInt(
                            tblUsuarios
                                    .getValueAt(
                                            fila,
                                            0
                                    )
                                    .toString()
                    );

            if (idTabla == idUsuario) {

                tblUsuarios.setRowSelectionInterval(
                        fila,
                        fila
                );

                tblUsuarios.scrollRectToVisible(
                        tblUsuarios
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

        txtUsuario.requestFocus();
    }


    private void limpiarFormulario() {

        txtId.setText("");

        txtUsuario.setText("");

        txtPassword.setEditable(true);
        txtPassword.setText("");

        passwordHashSeleccionado = null;
        fechaCreacionSeleccionada = null;

        cmbRol.setSelectedIndex(0);

        actualizarDisponibilidadDocente();

        if (cmbDocente.getItemCount() > 0) {
            cmbDocente.setSelectedIndex(0);
        }

        txtBuscar.setText("");

        chkActivo.setSelected(true);

        tblUsuarios.clearSelection();

        btnGuardar.setEnabled(true);

        btnActualizar.setEnabled(false);

        btnDesactivar.setEnabled(false);

        btnDesactivar.setText(
                "Desactivar"
        );
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
