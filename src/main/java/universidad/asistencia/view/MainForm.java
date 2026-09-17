package universidad.asistencia.view;

import javax.swing.*;

public class MainForm {

    private JPanel panelPrincipal;

    private JLabel lblTitulo;

    private JButton btnEstudiantes;
    private JButton btnDocentes;
    private JButton btnCursos;
    private JButton btnPeriodos;
    private JButton btnSecciones;
    private JButton btnHorarios;
    private JButton btnInscripciones;
    private JButton btnSesiones;
    private JButton btnDispositivos;
    private JButton btnAsistencia;
    private JButton btnJustificaciones;
    private JButton btnUsuarios;

    private JButton btnSalir;


    public MainForm() {

        configurarFormulario();
        configurarEventos();
    }


    /*
     * Configuración general del menú.
     */
    private void configurarFormulario() {

       /* lblTitulo.setText(
                "Sistema de Asistencia Universitaria"
        );*/
    }


    /*
     * Aquí centralizamos todos los eventos
     * del menú principal.
     */
    private void configurarEventos() {

        this.btnEstudiantes.addActionListener(
                e -> abrirEstudiantes()
        );

        btnDocentes.addActionListener(
                e -> abrirDocentes()
        );

        btnCursos.addActionListener(
                e -> abrirCursos()
        );

        btnPeriodos.addActionListener(
                e -> moduloPendiente(
                        "Períodos Académicos"
                )
        );

        btnSecciones.addActionListener(
                e -> moduloPendiente("Secciones")
        );

        btnHorarios.addActionListener(
                e -> moduloPendiente("Horarios")
        );

        btnInscripciones.addActionListener(
                e -> moduloPendiente("Inscripciones")
        );

        btnSesiones.addActionListener(
                e -> moduloPendiente("Sesiones")
        );

        btnDispositivos.addActionListener(
                e -> moduloPendiente("Dispositivos")
        );

        btnAsistencia.addActionListener(
                e -> moduloPendiente("Asistencia")
        );

        btnJustificaciones.addActionListener(
                e -> moduloPendiente(
                        "Justificaciones"
                )
        );

        btnUsuarios.addActionListener(
                e -> moduloPendiente("Usuarios")
        );

        btnSalir.addActionListener(
                e -> salir()
        );
    }


    /*
     * ======================================================
     * ESTUDIANTES
     * ======================================================
     */
    private void abrirEstudiantes() {

        EstudianteView estudianteView =
                new EstudianteView();

        abrirVentana(
                "Administración de Estudiantes",
                estudianteView.getPanelPrincipal(),
                950,
                650
        );
    }
    /*
     * ======================================================
     * DOCENTES
     * ======================================================
     */
    private void abrirDocentes() {

        DocenteView docenteView =
                new DocenteView();

        abrirVentana(
                "Administración de Docentes",
                docenteView.getPanelPrincipal(),
                950,
                650
        );
    }
    /*
     * ======================================================
     * CURSOS
     * ======================================================
     */
    private void abrirCursos() {

        CursoView cursoView =
                new CursoView();

        abrirVentana(
                "Administración de Estudiantes",
                cursoView.getPanelPrincipal(),
                950,
                650
        );
    }


    /*
     * Método reutilizable para abrir cualquier módulo.
     *
     * Como nuestras Views están construidas como JPanel,
     * simplemente colocamos el panel dentro de un JFrame.
     */
    private void abrirVentana(
            String titulo,
            JPanel panel,
            int ancho,
            int alto
    ) {

        JFrame ventana =
                new JFrame(titulo);

        ventana.setContentPane(panel);

        ventana.setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        ventana.setSize(
                ancho,
                alto
        );

        ventana.setLocationRelativeTo(null);

        ventana.setVisible(true);
    }


    /*
     * Mientras vamos construyendo los módulos,
     * los botones ya pueden existir en el MainForm.
     */
    private void moduloPendiente(
            String modulo
    ) {

        JOptionPane.showMessageDialog(
                panelPrincipal,
                "El módulo de "
                        + modulo
                        + " todavía no ha sido construido.",
                "Módulo pendiente",
                JOptionPane.INFORMATION_MESSAGE
        );
    }


    /*
     * Cierra completamente la aplicación.
     */
    private void salir() {

        int respuesta =
                JOptionPane.showConfirmDialog(
                        panelPrincipal,
                        "¿Desea salir del sistema?",
                        "Confirmar salida",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

        if (
                respuesta ==
                        JOptionPane.YES_OPTION
        ) {

            System.exit(0);
        }
    }


    public JPanel getPanelPrincipal() {

        return panelPrincipal;
    }
}