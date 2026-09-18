package universidad.asistencia.view;

import universidad.asistencia.controller.CursoController;
import universidad.asistencia.controller.DocenteController;
import universidad.asistencia.controller.PeriodoAcademicoController;
import universidad.asistencia.controller.SeccionController;
import universidad.asistencia.model.Curso;
import universidad.asistencia.model.Docente;
import universidad.asistencia.model.PeriodoAcademico;
import universidad.asistencia.model.Seccion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;
import java.util.List;
import java.util.Optional;

public class SeccionView {

    // Componentes vinculados desde SeccionView.form
    private JPanel panelPrincipal;

    private JTextField txtId;
    private JTextField txtCodigo;
    private JComboBox cmbCurso;
    private JComboBox cmbPeriodo;
    private JComboBox cmbDocente;
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

    // La View solamente conoce a los Controllers.
    private final SeccionController seccionController;
    private final CursoController cursoController;
    private final PeriodoAcademicoController periodoController;
    private final DocenteController docenteController;


    /*
     * Constructor normal.
     */
    public SeccionView() {

        this(
                new SeccionController(),
                new CursoController(),
                new PeriodoAcademicoController(),
                new DocenteController()
        );
    }


    /*
     * También permitimos recibir los Controllers desde afuera.
     */
    public SeccionView(
            SeccionController seccionController,
            CursoController cursoController,
            PeriodoAcademicoController periodoController,
            DocenteController docenteController
    ) {

        this.seccionController = seccionController;
        this.cursoController = cursoController;
        this.periodoController = periodoController;
        this.docenteController = docenteController;

        configurarFormulario();
        configurarEventos();
        cargarCombos();
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

        configurarCombos();

        configurarTabla();
    }


    /*
     * Los combos muestran texto legible en vez del toString()
     * por defecto de Curso/PeriodoAcademico/Docente.
     */
    @SuppressWarnings("unchecked")
    private void configurarCombos() {

        cmbCurso.setRenderer(new DefaultListCellRenderer() {

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

                if (value instanceof Curso curso) {

                    setText(
                            curso.getCodigo()
                                    + " - "
                                    + curso.getNombre()
                    );
                }

                return this;
            }
        });

        cmbPeriodo.setRenderer(new DefaultListCellRenderer() {

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

                if (value instanceof PeriodoAcademico periodo) {

                    setText(
                            periodo.getNombre()
                    );
                }

                return this;
            }
        });

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
    }


    /*
     * Carga la lista de cursos, periodos y docentes en los combos.
     *
     * Usamos listar() (no listarActivos()) para que, al editar una
     * sección existente, el combo pueda seguir mostrando un curso,
     * periodo o docente que ya se haya desactivado.
     */
    @SuppressWarnings("unchecked")
    private void cargarCombos() {

        try {

            DefaultComboBoxModel<Curso> modeloCursos =
                    new DefaultComboBoxModel<>();

            for (Curso curso : cursoController.listar()) {
                modeloCursos.addElement(curso);
            }

            cmbCurso.setModel(modeloCursos);

            DefaultComboBoxModel<PeriodoAcademico> modeloPeriodos =
                    new DefaultComboBoxModel<>();

            for (PeriodoAcademico periodo : periodoController.listar()) {
                modeloPeriodos.addElement(periodo);
            }

            cmbPeriodo.setModel(modeloPeriodos);

            DefaultComboBoxModel<Docente> modeloDocentes =
                    new DefaultComboBoxModel<>();

            for (Docente docente : docenteController.listar()) {
                modeloDocentes.addElement(docente);
            }

            cmbDocente.setModel(modeloDocentes);

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

            Curso curso =
                    (Curso) cmbCurso.getSelectedItem();

            PeriodoAcademico periodo =
                    (PeriodoAcademico) cmbPeriodo.getSelectedItem();

            Docente docente =
                    (Docente) cmbDocente.getSelectedItem();

            if (curso == null || periodo == null || docente == null) {

                mostrarError(
                        "Debe seleccionar un curso, un periodo "
                                + "y un docente."
                );

                return;
            }

            Seccion seccion =
                    new Seccion(
                            txtCodigo.getText().trim(),
                            curso,
                            periodo,
                            docente,
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

            Curso curso =
                    (Curso) cmbCurso.getSelectedItem();

            PeriodoAcademico periodo =
                    (PeriodoAcademico) cmbPeriodo.getSelectedItem();

            Docente docente =
                    (Docente) cmbDocente.getSelectedItem();

            if (curso == null || periodo == null || docente == null) {

                mostrarError(
                        "Debe seleccionar un curso, un periodo "
                                + "y un docente."
                );

                return;
            }

            Seccion seccion =
                    new Seccion(
                            idSeccion,
                            txtCodigo.getText().trim(),
                            curso,
                            periodo,
                            docente,
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

            Curso curso =
                    (Curso) cmbCurso.getSelectedItem();

            PeriodoAcademico periodo =
                    (PeriodoAcademico) cmbPeriodo.getSelectedItem();

            Docente docente =
                    (Docente) cmbDocente.getSelectedItem();

            /*
             * Construimos la sección con todos sus datos,
             * cambiando únicamente el atributo activo.
             */
            Seccion seccion =
                    new Seccion(
                            idSeccion,
                            txtCodigo.getText().trim(),
                            curso,
                            periodo,
                            docente,
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

        seleccionarCursoEnCombo(
                seccion.getCurso().getIdCurso()
        );

        seleccionarPeriodoEnCombo(
                seccion.getPeriodoAcademico().getIdPeriodo()
        );

        seleccionarDocenteEnCombo(
                seccion.getDocente().getIdDocente()
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
     * Ubica en el combo de cursos el que tiene el ID indicado.
     */
    private void seleccionarCursoEnCombo(
            int idCurso
    ) {

        for (
                int i = 0;
                i < cmbCurso.getItemCount();
                i++
        ) {

            Curso curso =
                    (Curso) cmbCurso.getItemAt(i);

            if (curso.getIdCurso() == idCurso) {

                cmbCurso.setSelectedIndex(i);

                return;
            }
        }
    }


    /*
     * Ubica en el combo de periodos el que tiene el ID indicado.
     */
    private void seleccionarPeriodoEnCombo(
            int idPeriodo
    ) {

        for (
                int i = 0;
                i < cmbPeriodo.getItemCount();
                i++
        ) {

            PeriodoAcademico periodo =
                    (PeriodoAcademico) cmbPeriodo.getItemAt(i);

            if (periodo.getIdPeriodo() == idPeriodo) {

                cmbPeriodo.setSelectedIndex(i);

                return;
            }
        }
    }


    /*
     * Ubica en el combo de docentes el que tiene el ID indicado.
     */
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

        if (cmbCurso.getItemCount() > 0) {
            cmbCurso.setSelectedIndex(0);
        }

        if (cmbPeriodo.getItemCount() > 0) {
            cmbPeriodo.setSelectedIndex(0);
        }

        if (cmbDocente.getItemCount() > 0) {
            cmbDocente.setSelectedIndex(0);
        }

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
