package universidad.asistencia.view;

import universidad.asistencia.controller.SeccionController;
import universidad.asistencia.model.Curso;
import universidad.asistencia.model.Docente;
import universidad.asistencia.model.PeriodoAcademico;
import universidad.asistencia.model.Seccion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.Optional;

public class SeccionView {

    // Componentes vinculados desde SeccionView.form
    private JPanel panelPrincipal;

    private JTextField txtId;
    private JTextField txtCodigo;
    private JTextField txtIdCurso;
    private JTextField txtIdPeriodo;
    private JTextField txtIdDocente;
    private JTextField txtAulaAsignada;

    private JCheckBox chkActivo;

    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnDesactivar;

    private JTextField txtBuscar;
    private JButton btnBuscar;

    private JTable tblSecciones;
    private JButton btnSalir;

    // La View solamente conoce al Controller.
    private final SeccionController seccionController;


    /*
     * Constructor normal.
     */
    public SeccionView() {

        this(new SeccionController());
    }


    /*
     * También permitimos recibir el Controller desde afuera.
     */
    public SeccionView(
            SeccionController seccionController
    ) {

        this.seccionController = seccionController;

        configurarFormulario();
        configurarEventos();
        cargarSecciones();
    }


    /*
     * Configuración inicial.
     */
    private void configurarFormulario() {

        // El ID lo genera SQL Server.
        txtId.setEditable(false);

        // Toda sección nueva inicia activa.
        chkActivo.setSelected(true);

        /*
         * El estado no se cambia directamente desde el checkbox.
         * Se cambia utilizando el botón Activar/Desactivar.
         */
        chkActivo.setEnabled(false);

        btnGuardar.setEnabled(true);

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
                                "Curso",
                                "Periodo",
                                "Docente",
                                "Aula",
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

        tblSecciones.setModel(modelo);

        tblSecciones.setSelectionMode(
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
         * recuperamos la sección utilizando su ID.
         */
        tblSecciones
                .getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {
                        seleccionarSeccion();
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
                    new Seccion(
                            txtCodigo.getText().trim(),
                            leerCurso(),
                            leerPeriodo(),
                            leerDocente(),
                            txtAulaAsignada.getText().trim()
                    );

            boolean guardado =
                    seccionController.guardar(
                            seccion
                    );

            if (guardado) {

                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        "Sección guardada correctamente.",
                        "Sección",
                        JOptionPane.INFORMATION_MESSAGE
                );

                limpiarFormulario();

                cargarSecciones();

            } else {

                mostrarError(
                        "No se pudo guardar la sección."
                );
            }

        } catch (NumberFormatException e) {

            mostrarError(
                    "Los ID de curso, periodo y docente "
                            + "deben ser valores numéricos."
            );

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
    private void cargarSecciones() {

        try {

            List<Seccion> secciones =
                    seccionController.listar();

            DefaultTableModel modelo =
                    (DefaultTableModel)
                            tblSecciones.getModel();

            modelo.setRowCount(0);

            for (Seccion seccion : secciones) {

                modelo.addRow(
                        new Object[]{
                                seccion.getIdSeccion(),
                                seccion.getCodigo(),
                                seccion.getCurso().getCodigo(),
                                seccion.getPeriodoAcademico().getNombre(),
                                seccion.getDocente().getCodigoEmpleado(),
                                seccion.getAulaAsignada(),

                                seccion.isActivo()
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
                    "Debe seleccionar una sección.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        try {

            int idSeccion =
                    Integer.parseInt(
                            txtId.getText()
                    );

            Seccion seccion =
                    new Seccion(
                            idSeccion,
                            txtCodigo.getText().trim(),
                            leerCurso(),
                            leerPeriodo(),
                            leerDocente(),
                            txtAulaAsignada.getText().trim(),

                            /*
                             * Conservamos el estado actual.
                             */
                            chkActivo.isSelected()
                    );

            boolean actualizado =
                    seccionController.actualizar(
                            seccion
                    );

            if (actualizado) {

                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        "Sección actualizada correctamente.",
                        "Sección",
                        JOptionPane.INFORMATION_MESSAGE
                );

                limpiarFormulario();

                cargarSecciones();

            } else {

                mostrarError(
                        "No se pudo actualizar la sección."
                );
            }

        } catch (NumberFormatException e) {

            mostrarError(
                    "Los ID de curso, periodo y docente "
                            + "deben ser valores numéricos."
            );

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

        if (txtId.getText().isBlank()) {

            JOptionPane.showMessageDialog(
                    panelPrincipal,
                    "Debe seleccionar una sección.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        /*
         * El checkbox refleja el estado actual
         * de la sección seleccionada.
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
                                + " la sección?",
                        "Confirmar "
                                + accionTitulo,
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            int idSeccion =
                    Integer.parseInt(
                            txtId.getText()
                    );

            /*
             * Construimos la sección con todos sus datos,
             * cambiando únicamente el atributo activo.
             */
            Seccion seccion =
                    new Seccion(
                            idSeccion,
                            txtCodigo.getText().trim(),
                            leerCurso(),
                            leerPeriodo(),
                            leerDocente(),
                            txtAulaAsignada.getText().trim(),
                            nuevoEstado
                    );

            /*
             * No necesitamos crear un nuevo método en el
             * Controller porque actualizar() ya persiste
             * el campo activo.
             */
            boolean actualizado =
                    seccionController.actualizar(
                            seccion
                    );

            if (actualizado) {

                String mensaje =
                        nuevoEstado
                                ? "Sección activada correctamente."
                                : "Sección desactivada correctamente.";

                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        mensaje,
                        "Sección",
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

                cargarSecciones();

                /*
                 * Volvemos a seleccionar visualmente
                 * la misma sección.
                 */
                seleccionarFilaPorId(
                        idSeccion
                );

            } else {

                mostrarError(
                        "No se pudo cambiar el estado "
                                + "de la sección."
                );
            }

        } catch (NumberFormatException e) {

            mostrarError(
                    "Los ID de curso, periodo y docente "
                            + "deben ser valores numéricos."
            );

        } catch (Exception e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }


    /*
     * Cambia el texto del botón dependiendo
     * del estado actual de la sección.
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
     * BUSCAR POR CÓDIGO
     * ========================================================
     */
    private void buscar() {

        String codigo =
                txtBuscar.getText().trim();

        /*
         * Si no escribió nada, mostramos nuevamente
         * todas las secciones.
         */
        if (codigo.isBlank()) {

            cargarSecciones();

            return;
        }

        try {

            Optional<Seccion> resultado =
                    seccionController
                            .buscarPorCodigo(codigo);

            if (resultado.isPresent()) {

                Seccion seccion =
                        resultado.get();

                mostrarSeccionEnFormulario(
                        seccion
                );

                seleccionarFilaPorId(
                        seccion.getIdSeccion()
                );

            } else {

                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        "No se encontró ninguna sección "
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
    private void seleccionarSeccion() {

        int fila =
                tblSecciones.getSelectedRow();

        if (fila == -1) {
            return;
        }

        try {

            int idSeccion =
                    Integer.parseInt(
                            tblSecciones
                                    .getValueAt(
                                            fila,
                                            0
                                    )
                                    .toString()
                    );

            Optional<Seccion> resultado =
                    seccionController.buscar(
                            idSeccion
                    );

            if (resultado.isPresent()) {

                mostrarSeccionEnFormulario(
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
    private void mostrarSeccionEnFormulario(
            Seccion seccion
    ) {

        txtId.setText(
                String.valueOf(
                        seccion.getIdSeccion()
                )
        );

        txtCodigo.setText(
                seccion.getCodigo()
        );

        txtIdCurso.setText(
                String.valueOf(
                        seccion.getCurso().getIdCurso()
                )
        );

        txtIdPeriodo.setText(
                String.valueOf(
                        seccion.getPeriodoAcademico().getIdPeriodo()
                )
        );

        txtIdDocente.setText(
                String.valueOf(
                        seccion.getDocente().getIdDocente()
                )
        );

        txtAulaAsignada.setText(
                seccion.getAulaAsignada()
        );

        /*
         * Reflejamos el estado real.
         */
        chkActivo.setSelected(
                seccion.isActivo()
        );

        /*
         * Cambiamos automáticamente el texto del botón.
         */
        actualizarTextoBotonEstado(
                seccion.isActivo()
        );

        /*
         * Estamos trabajando con un registro existente.
         */
        btnGuardar.setEnabled(false);

        btnActualizar.setEnabled(true);

        /*
         * El botón debe estar habilitado tanto para
         * secciones activas como inactivas.
         */
        btnDesactivar.setEnabled(true);
    }


    /*
     * Busca visualmente en la JTable el registro
     * seleccionado por una búsqueda.
     */
    private void seleccionarFilaPorId(
            int idSeccion
    ) {

        for (
                int fila = 0;
                fila < tblSecciones.getRowCount();
                fila++
        ) {

            int idTabla =
                    Integer.parseInt(
                            tblSecciones
                                    .getValueAt(
                                            fila,
                                            0
                                    )
                                    .toString()
                    );

            if (idTabla == idSeccion) {

                tblSecciones.setRowSelectionInterval(
                        fila,
                        fila
                );

                tblSecciones.scrollRectToVisible(
                        tblSecciones
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


    /*
     * Limpia la pantalla para ingresar una nueva sección.
     */
    private void limpiarFormulario() {

        txtId.setText("");

        txtCodigo.setText("");

        txtIdCurso.setText("");

        txtIdPeriodo.setText("");

        txtIdDocente.setText("");

        txtAulaAsignada.setText("");

        txtBuscar.setText("");

        chkActivo.setSelected(true);

        tblSecciones.clearSelection();

        btnGuardar.setEnabled(true);

        btnActualizar.setEnabled(false);

        btnDesactivar.setEnabled(false);

        /*
         * Para una nueva sección el estado inicial
         * siempre es activo.
         */
        btnDesactivar.setText(
                "Desactivar"
        );
    }


    /*
     * ========================================================
     * LECTURA DE CLAVES FORÁNEAS
     *
     * El formulario solo pide el ID del curso, del periodo
     * y del docente. El Repository únicamente necesita ese
     * ID para guardar/actualizar la sección.
     * ========================================================
     */
    private Curso leerCurso() {

        Curso curso = new Curso();

        curso.setIdCurso(
                Integer.parseInt(
                        txtIdCurso.getText().trim()
                )
        );

        return curso;
    }

    private PeriodoAcademico leerPeriodo() {

        PeriodoAcademico periodo = new PeriodoAcademico();

        periodo.setIdPeriodo(
                Integer.parseInt(
                        txtIdPeriodo.getText().trim()
                )
        );

        return periodo;
    }

    private Docente leerDocente() {

        Docente docente = new Docente();

        docente.setIdDocente(
                Integer.parseInt(
                        txtIdDocente.getText().trim()
                )
        );

        return docente;
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
     * Conservamos exactamente la lógica utilizada:
     * salir de este catálogo significa regresar al MainForm.
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
