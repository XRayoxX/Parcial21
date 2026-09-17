package universidad.asistencia.view;

import universidad.asistencia.controller.DocenteController;
import universidad.asistencia.model.Docente;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.Optional;

public class DocenteView {

    // Componentes vinculados desde DocenteView.form
    private JPanel panelPrincipal;

    private JTextField txtId_docente;
    private JTextField txtCodigo_empleado;
    private JTextField txtNombres;
    private JTextField txtApellidos;
    private JTextField txtCorreo;

    private JCheckBox chkActivo;

    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnDesactivar;

    private JTextField txtBuscar;
    private JButton btnBuscar;

    private JTable tblEstudiantes;
    private JButton btnSalir;

    // La View solamente conoce al Controller.
    private final DocenteController docenteController;


    /*
     * Constructor normal.
     */
    public DocenteView() {

        this(new DocenteController());
    }


    /*
     * También permitimos recibir el Controller desde afuera.
     */
    public DocenteView(
            DocenteController docenteController
    ) {

        this.docenteController = docenteController;

        configurarFormulario();
        configurarEventos();
        cargarDocentes();
    }


    /*
     * Configuración inicial.
     */
    private void configurarFormulario() {

        // El ID lo genera SQL Server.
        txtId_docente.setEditable(false);

        // Todo docente nuevo inicia activo.
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
                                "Nombres",
                                "Apellidos",
                                "Correo",
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

        tblEstudiantes.setModel(modelo);

        tblEstudiantes.setSelectionMode(
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
         * recuperamos el docente utilizando su ID.
         */
        tblEstudiantes
                .getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {
                        seleccionarDocente();
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

            Docente docente =
                    new Docente(
                            txtCodigo_empleado.getText().trim(),
                            txtNombres.getText().trim(),
                            txtApellidos.getText().trim(),
                            txtCorreo.getText().trim()
                    );

            boolean guardado =
                    docenteController.guardar(
                            docente
                    );

            if (guardado) {

                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        "Docente guardado correctamente.\n"
                                + "ID generado: "
                                + docente.getIdDocente(),
                        "Docente",
                        JOptionPane.INFORMATION_MESSAGE
                );

                limpiarFormulario();

                cargarDocentes();

            } else {

                mostrarError(
                        "No se pudo guardar el docente."
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
    private void cargarDocentes() {

        try {

            List<Docente> docentes =
                    docenteController.listar();

            DefaultTableModel modelo =
                    (DefaultTableModel)
                            tblEstudiantes.getModel();

            modelo.setRowCount(0);

            for (Docente docente : docentes) {

                modelo.addRow(
                        new Object[]{
                                docente.getIdDocente(),
                                docente.getCodigoEmpleado(),
                                docente.getNombres(),
                                docente.getApellidos(),
                                docente.getCorreo(),

                                docente.isActivo()
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

        if (txtId_docente.getText().isBlank()) {

            JOptionPane.showMessageDialog(
                    panelPrincipal,
                    "Debe seleccionar un docente.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        try {

            int idDocente =
                    Integer.parseInt(
                            txtId_docente.getText()
                    );

            Docente docente =
                    new Docente(
                            idDocente,
                            txtCodigo_empleado.getText().trim(),
                            txtNombres.getText().trim(),
                            txtApellidos.getText().trim(),
                            txtCorreo.getText().trim(),

                            /*
                             * Conservamos el estado actual.
                             */
                            chkActivo.isSelected()
                    );

            boolean actualizado =
                    docenteController.actualizar(
                            docente
                    );

            if (actualizado) {

                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        "Docente actualizado correctamente.",
                        "Docente",
                        JOptionPane.INFORMATION_MESSAGE
                );

                limpiarFormulario();

                cargarDocentes();

            } else {

                mostrarError(
                        "No se pudo actualizar el docente."
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
     * ACTIVO   -> INACTIVO
     * INACTIVO -> ACTIVO
     *
     * Utilizamos actualizar() porque el Repository ya guarda
     * el valor booleano del atributo activo.
     * ========================================================
     */
    private void cambiarEstado() {

        if (txtId_docente.getText().isBlank()) {

            JOptionPane.showMessageDialog(
                    panelPrincipal,
                    "Debe seleccionar un docente.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        /*
         * El checkbox refleja el estado actual
         * del docente seleccionado.
         */
        boolean estadoActual =
                chkActivo.isSelected();

        boolean nuevoEstado =
                !estadoActual;


        /*
         * Los textos cambian dependiendo del estado actual.
         */
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
                                + " al docente?",
                        "Confirmar "
                                + accionTitulo,
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }


        try {

            int idDocente =
                    Integer.parseInt(
                            txtId_docente.getText()
                    );


            /*
             * Construimos el docente con todos sus datos,
             * cambiando únicamente el atributo activo.
             */
            Docente docente =
                    new Docente(
                            idDocente,
                            txtCodigo_empleado.getText().trim(),
                            txtNombres.getText().trim(),
                            txtApellidos.getText().trim(),
                            txtCorreo.getText().trim(),
                            nuevoEstado
                    );


            /*
             * No necesitamos crear un nuevo método en el
             * Controller porque actualizar() ya persiste
             * el campo activo.
             */
            boolean actualizado =
                    docenteController.actualizar(
                            docente
                    );


            if (actualizado) {

                String mensaje =
                        nuevoEstado
                                ? "Docente activado correctamente."
                                : "Docente desactivado correctamente.";


                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        mensaje,
                        "Docente",
                        JOptionPane.INFORMATION_MESSAGE
                );


                /*
                 * Reflejamos inmediatamente el nuevo estado.
                 */
                chkActivo.setSelected(
                        nuevoEstado
                );


                actualizarTextoBotonEstado(
                        nuevoEstado
                );


                cargarDocentes();


                /*
                 * Volvemos a seleccionar visualmente
                 * el mismo docente.
                 */
                seleccionarFilaPorId(
                        idDocente
                );

            } else {

                mostrarError(
                        "No se pudo cambiar el estado "
                                + "del docente."
                );
            }

        } catch (Exception e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }


    /*
     * Cambia el texto del botón dependiendo
     * del estado actual del docente.
     *
     * Activo:
     *      botón = Desactivar
     *
     * Inactivo:
     *      botón = Activar
     */
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
     * BUSCAR POR CÓDIGO DE EMPLEADO
     * ========================================================
     */
    private void buscar() {

        String codigo =
                txtBuscar.getText().trim();

        /*
         * Si no escribió nada, mostramos nuevamente
         * todos los docentes.
         */
        if (codigo.isBlank()) {

            cargarDocentes();

            return;
        }

        try {

            Optional<Docente> resultado =
                    docenteController
                            .buscarPorCodigo(codigo);

            if (resultado.isPresent()) {

                Docente docente =
                        resultado.get();

                mostrarDocenteEnFormulario(
                        docente
                );

                seleccionarFilaPorId(
                        docente.getIdDocente()
                );

            } else {

                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        "No se encontró ningún docente "
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
    private void seleccionarDocente() {

        int fila =
                tblEstudiantes.getSelectedRow();

        if (fila == -1) {
            return;
        }

        try {

            int idDocente =
                    Integer.parseInt(
                            tblEstudiantes
                                    .getValueAt(
                                            fila,
                                            0
                                    )
                                    .toString()
                    );

            Optional<Docente> resultado =
                    docenteController.buscar(
                            idDocente
                    );

            if (resultado.isPresent()) {

                mostrarDocenteEnFormulario(
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
    private void mostrarDocenteEnFormulario(
            Docente docente
    ) {

        txtId_docente.setText(
                String.valueOf(
                        docente.getIdDocente()
                )
        );

        txtCodigo_empleado.setText(
                docente.getCodigoEmpleado()
        );

        txtNombres.setText(
                docente.getNombres()
        );

        txtApellidos.setText(
                docente.getApellidos()
        );

        txtCorreo.setText(
                docente.getCorreo()
        );


        /*
         * Reflejamos el estado real.
         */
        chkActivo.setSelected(
                docente.isActivo()
        );


        /*
         * Cambiamos automáticamente el texto del botón.
         */
        actualizarTextoBotonEstado(
                docente.isActivo()
        );


        /*
         * Estamos trabajando con un registro existente.
         */
        btnGuardar.setEnabled(false);

        btnActualizar.setEnabled(true);


        /*
         * IMPORTANTE:
         *
         * El botón debe estar habilitado tanto para
         * docentes activos como inactivos.
         *
         * Si está activo permitirá Desactivar.
         * Si está inactivo permitirá Activar.
         */
        btnDesactivar.setEnabled(true);
    }


    /*
     * Busca visualmente en la JTable el registro
     * seleccionado por una búsqueda.
     */
    private void seleccionarFilaPorId(
            int idDocente
    ) {

        for (
                int fila = 0;
                fila < tblEstudiantes.getRowCount();
                fila++
        ) {

            int idTabla =
                    Integer.parseInt(
                            tblEstudiantes
                                    .getValueAt(
                                            fila,
                                            0
                                    )
                                    .toString()
                    );

            if (idTabla == idDocente) {

                tblEstudiantes.setRowSelectionInterval(
                        fila,
                        fila
                );

                tblEstudiantes.scrollRectToVisible(
                        tblEstudiantes
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

        txtCodigo_empleado.requestFocus();
    }


    /*
     * Limpia la pantalla para ingresar un nuevo docente.
     */
    private void limpiarFormulario() {

        txtId_docente.setText("");

        txtCodigo_empleado.setText("");

        txtNombres.setText("");

        txtApellidos.setText("");

        txtCorreo.setText("");

        txtBuscar.setText("");

        chkActivo.setSelected(true);

        tblEstudiantes.clearSelection();

        btnGuardar.setEnabled(true);

        btnActualizar.setEnabled(false);

        btnDesactivar.setEnabled(false);

        /*
         * Para un nuevo docente el estado inicial
         * siempre es activo.
         */
        btnDesactivar.setText(
                "Desactivar"
        );
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

            // Obtiene la ventana JFrame que contiene este JPanel
            // y cierra únicamente el catálogo actual.
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
