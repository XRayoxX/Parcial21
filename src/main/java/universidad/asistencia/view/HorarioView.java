package universidad.asistencia.view;

import universidad.asistencia.controller.HorarioSemanalController;
import universidad.asistencia.model.HorarioSemanal;
import universidad.asistencia.model.Seccion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

public class HorarioView {

    // Componentes vinculados desde HorarioView.form
    private JPanel panelPrincipal;

    private JTextField txtId;
    private JTextField txtIdSeccion;
    private JTextField txtDiaSemana;
    private JTextField txtHoraInicio;
    private JTextField txtHoraFin;

    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnActualizar;

    /*
     * HorarioSemanal no tiene columna "activo" en la base de datos,
     * así que este botón realiza una eliminación real (no un toggle).
     */
    private JButton btnDesactivar;

    private JTextField txtBuscar;
    private JButton btnBuscar;

    private JTable tblHorarios;
    private JButton btnSalir;

    // La View solamente conoce al Controller.
    private final HorarioSemanalController horarioController;


    /*
     * Constructor normal.
     */
    public HorarioView() {

        this(new HorarioSemanalController());
    }


    /*
     * También permitimos recibir el Controller desde afuera.
     */
    public HorarioView(
            HorarioSemanalController horarioController
    ) {

        this.horarioController = horarioController;

        configurarFormulario();
        configurarEventos();
        cargarHorarios();
    }


    /*
     * Configuración inicial.
     */
    private void configurarFormulario() {

        // El ID lo genera SQL Server.
        txtId.setEditable(false);

        btnGuardar.setEnabled(true);

        btnActualizar.setEnabled(false);

        btnDesactivar.setEnabled(false);
        btnDesactivar.setText("Eliminar");

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
                                "Sección",
                                "Día",
                                "Hora inicio",
                                "Hora fin"
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

        tblHorarios.setModel(modelo);

        tblHorarios.setSelectionMode(
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
                e -> eliminar()
        );

        btnBuscar.addActionListener(
                e -> buscar()
        );

        btnSalir.addActionListener(
                e -> salir()
        );

        /*
         * Cuando seleccionamos una fila,
         * recuperamos el horario utilizando su ID.
         */
        tblHorarios
                .getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {
                        seleccionarHorario();
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

            HorarioSemanal horario =
                    new HorarioSemanal(
                            leerSeccion(),
                            leerDiaSemana(),
                            leerHora(txtHoraInicio.getText()),
                            leerHora(txtHoraFin.getText())
                    );

            boolean guardado =
                    horarioController.guardar(
                            horario
                    );

            if (guardado) {

                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        "Horario guardado correctamente.",
                        "Horario",
                        JOptionPane.INFORMATION_MESSAGE
                );

                limpiarFormulario();

                cargarHorarios();

            } else {

                mostrarError(
                        "No se pudo guardar el horario."
                );
            }

        } catch (NumberFormatException e) {

            mostrarError(
                    "El ID de sección debe ser un valor numérico."
            );

        } catch (DateTimeParseException e) {

            mostrarError(
                    "Las horas deben tener el formato HH:mm."
            );

        } catch (IllegalArgumentException e) {

            mostrarError(
                    e.getMessage()
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
    private void cargarHorarios() {

        try {

            List<HorarioSemanal> horarios =
                    horarioController.listar();

            llenarTabla(horarios);

        } catch (Exception e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }


    /*
     * Vuelca una lista de horarios dentro de la JTable.
     * La reutilizamos tanto para listar todo como
     * para filtrar por sección.
     */
    private void llenarTabla(
            List<HorarioSemanal> horarios
    ) {

        DefaultTableModel modelo =
                (DefaultTableModel)
                        tblHorarios.getModel();

        modelo.setRowCount(0);

        for (HorarioSemanal horario : horarios) {

            modelo.addRow(
                    new Object[]{
                            horario.getIdHorario(),
                            horario.getSeccion().getCodigo(),
                            formatDiaSemana(horario.getDiaSemana()),
                            horario.getHoraInicio(),
                            horario.getHoraFin()
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
                    "Debe seleccionar un horario.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        try {

            int idHorario =
                    Integer.parseInt(
                            txtId.getText()
                    );

            HorarioSemanal horario =
                    new HorarioSemanal(
                            idHorario,
                            leerSeccion(),
                            leerDiaSemana(),
                            leerHora(txtHoraInicio.getText()),
                            leerHora(txtHoraFin.getText())
                    );

            boolean actualizado =
                    horarioController.actualizar(
                            horario
                    );

            if (actualizado) {

                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        "Horario actualizado correctamente.",
                        "Horario",
                        JOptionPane.INFORMATION_MESSAGE
                );

                limpiarFormulario();

                cargarHorarios();

            } else {

                mostrarError(
                        "No se pudo actualizar el horario."
                );
            }

        } catch (NumberFormatException e) {

            mostrarError(
                    "El ID de sección debe ser un valor numérico."
            );

        } catch (DateTimeParseException e) {

            mostrarError(
                    "Las horas deben tener el formato HH:mm."
            );

        } catch (IllegalArgumentException e) {

            mostrarError(
                    e.getMessage()
            );

        } catch (Exception e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }


    /*
     * ========================================================
     * ELIMINAR
     *
     * HorarioSemanal no maneja un atributo "activo", por lo
     * que aquí no hay un estado que alternar: el botón
     * elimina definitivamente el registro seleccionado.
     * ========================================================
     */
    private void eliminar() {

        if (txtId.getText().isBlank()) {

            JOptionPane.showMessageDialog(
                    panelPrincipal,
                    "Debe seleccionar un horario.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int respuesta =
                JOptionPane.showConfirmDialog(
                        panelPrincipal,
                        "¿Está seguro de eliminar este horario?",
                        "Confirmar eliminación",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            int idHorario =
                    Integer.parseInt(
                            txtId.getText()
                    );

            boolean eliminado =
                    horarioController.eliminar(
                            idHorario
                    );

            if (eliminado) {

                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        "Horario eliminado correctamente.",
                        "Horario",
                        JOptionPane.INFORMATION_MESSAGE
                );

                limpiarFormulario();

                cargarHorarios();

            } else {

                mostrarError(
                        "No se pudo eliminar el horario."
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
     * BUSCAR POR SECCIÓN
     * ========================================================
     */
    private void buscar() {

        String texto =
                txtBuscar.getText().trim();

        /*
         * Si no escribió nada, mostramos nuevamente
         * todos los horarios.
         */
        if (texto.isBlank()) {

            cargarHorarios();

            return;
        }

        try {

            int idSeccion =
                    Integer.parseInt(texto);

            List<HorarioSemanal> resultado =
                    horarioController
                            .listarPorSeccion(idSeccion);

            llenarTabla(resultado);

            if (resultado.isEmpty()) {

                JOptionPane.showMessageDialog(
                        panelPrincipal,
                        "Esa sección no tiene horarios "
                                + "registrados.",
                        "Búsqueda",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

        } catch (NumberFormatException e) {

            mostrarError(
                    "El ID de sección debe ser un valor numérico."
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
    private void seleccionarHorario() {

        int fila =
                tblHorarios.getSelectedRow();

        if (fila == -1) {
            return;
        }

        try {

            int idHorario =
                    Integer.parseInt(
                            tblHorarios
                                    .getValueAt(
                                            fila,
                                            0
                                    )
                                    .toString()
                    );

            Optional<HorarioSemanal> resultado =
                    horarioController.buscar(
                            idHorario
                    );

            if (resultado.isPresent()) {

                mostrarHorarioEnFormulario(
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
    private void mostrarHorarioEnFormulario(
            HorarioSemanal horario
    ) {

        txtId.setText(
                String.valueOf(
                        horario.getIdHorario()
                )
        );

        txtIdSeccion.setText(
                String.valueOf(
                        horario.getSeccion().getIdSeccion()
                )
        );

        txtDiaSemana.setText(
                formatDiaSemana(
                        horario.getDiaSemana()
                )
        );

        txtHoraInicio.setText(
                horario.getHoraInicio().toString()
        );

        txtHoraFin.setText(
                horario.getHoraFin().toString()
        );

        /*
         * Estamos trabajando con un registro existente.
         */
        btnGuardar.setEnabled(false);

        btnActualizar.setEnabled(true);

        btnDesactivar.setEnabled(true);
    }


    /*
     * ========================================================
     * NUEVO
     * ========================================================
     */
    private void nuevo() {

        limpiarFormulario();

        txtIdSeccion.requestFocus();
    }


    /*
     * Limpia la pantalla para ingresar un nuevo horario.
     */
    private void limpiarFormulario() {

        txtId.setText("");

        txtIdSeccion.setText("");

        txtDiaSemana.setText("");

        txtHoraInicio.setText("");

        txtHoraFin.setText("");

        txtBuscar.setText("");

        tblHorarios.clearSelection();

        btnGuardar.setEnabled(true);

        btnActualizar.setEnabled(false);

        btnDesactivar.setEnabled(false);
    }


    /*
     * ========================================================
     * LECTURA / FORMATO DE CAMPOS
     * ========================================================
     */
    private Seccion leerSeccion() {

        Seccion seccion = new Seccion();

        seccion.setIdSeccion(
                Integer.parseInt(
                        txtIdSeccion.getText().trim()
                )
        );

        return seccion;
    }

    private LocalTime leerHora(String texto) {

        return LocalTime.parse(
                texto.trim()
        );
    }

    /*
     * Acepta el nombre del día en español (con o sin
     * tildes) y lo convierte al DayOfWeek equivalente.
     */
    private DayOfWeek leerDiaSemana() {

        String normalizado =
                Normalizer.normalize(
                                txtDiaSemana.getText().trim(),
                                Normalizer.Form.NFD
                        )
                        .replaceAll("\\p{M}", "")
                        .toUpperCase();

        return switch (normalizado) {

            case "LUNES" -> DayOfWeek.MONDAY;
            case "MARTES" -> DayOfWeek.TUESDAY;
            case "MIERCOLES" -> DayOfWeek.WEDNESDAY;
            case "JUEVES" -> DayOfWeek.THURSDAY;
            case "VIERNES" -> DayOfWeek.FRIDAY;
            case "SABADO" -> DayOfWeek.SATURDAY;
            case "DOMINGO" -> DayOfWeek.SUNDAY;

            default -> throw new IllegalArgumentException(
                    "El día debe ser: Lunes, Martes, Miércoles, "
                            + "Jueves, Viernes, Sábado o Domingo."
            );
        };
    }

    /*
     * Convierte el DayOfWeek a su nombre en español
     * para mostrarlo en el formulario y en la tabla.
     */
    private String formatDiaSemana(DayOfWeek dia) {

        return switch (dia) {

            case MONDAY -> "Lunes";
            case TUESDAY -> "Martes";
            case WEDNESDAY -> "Miércoles";
            case THURSDAY -> "Jueves";
            case FRIDAY -> "Viernes";
            case SATURDAY -> "Sábado";
            case SUNDAY -> "Domingo";
        };
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
