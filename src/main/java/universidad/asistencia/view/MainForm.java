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
                e -> abrirPeriodos()
        );

        btnSecciones.addActionListener(
                e -> abrirSecciones()
        );

        btnHorarios.addActionListener(
                e -> abrirHorarios()
        );

        btnInscripciones.addActionListener(
                e -> abrirInscripciones()
        );

        btnSesiones.addActionListener(
                e -> abrirSesiones()
        );

        btnDispositivos.addActionListener(
                e -> abrirDispositivos()
        );

        btnAsistencia.addActionListener(
                e -> abrirAsistencia()
        );

        btnJustificaciones.addActionListener(
                e -> abrirJustificaciones()
        );

        btnUsuarios.addActionListener(
                e -> abrirUsuarios()
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
     * INSCRIPCIONES
     * ======================================================
     */
    private void abrirInscripciones() {

        InscripcionesView inscripcionesView =
                new InscripcionesView();

        abrirVentana(
                "Administración de Inscripciones",
                inscripcionesView.getPanelPrincipal(),
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
                "Administración de Cursos",
                cursoView.getPanelPrincipal(),
                950,
                650
        );
    }


    /*
     * ======================================================
     * SECCIONES
     * ======================================================
     */
    private void abrirSecciones() {

        SeccionView seccionView =
                new SeccionView();

        abrirVentana(
                "Administración de Secciones",
                seccionView.getPanelPrincipal(),
                950,
                650
        );
    }


    /*
     * ======================================================
     * HORARIOS
     * ======================================================
     */
    private void abrirHorarios() {

        HorarioView horarioView =
                new HorarioView();

        abrirVentana(
                "Administración de Horarios",
                horarioView.getPanelPrincipal(),
                950,
                650
        );
    }


    /*
     * ======================================================
     * DISPOSITIVOS
     * ======================================================
     */
    private void abrirDispositivos() {

        DispositivoView dispositivoView =
                new DispositivoView();

        abrirVentana(
                "Administración de Dispositivos",
                dispositivoView.getPanelPrincipal(),
                950,
                650
        );
    }


    /*
     * ======================================================
     * SESIONES DE CLASE
     * ======================================================
     */
    private void abrirSesiones() {

        SesionClaseView sesionClaseView =
                new SesionClaseView();

        abrirVentana(
                "Administración de Sesiones de Clase",
                sesionClaseView.getPanelPrincipal(),
                950,
                650
        );
    }


    /*
     * ======================================================
     * ASISTENCIA Y MARCAJES
     * ======================================================
     */
    private void abrirAsistencia() {

        AsistenciaView asistenciaView =
                new AsistenciaView();

        abrirVentana(
                "Asistencia y Marcajes",
                asistenciaView.getPanelPrincipal(),
                950,
                650
        );
    }


    /*
     * ======================================================
     * JUSTIFICACIONES
     * ======================================================
     */
    private void abrirJustificaciones() {

        JustificacionView justificacionView =
                new JustificacionView();

        abrirVentana(
                "Administración de Justificaciones",
                justificacionView.getPanelPrincipal(),
                950,
                650
        );
    }


    /*
     * ======================================================
     * USUARIOS
     * ======================================================
     */
    private void abrirUsuarios() {

        UsuarioView usuarioView =
                new UsuarioView();

        abrirVentana(
                "Administración de Usuarios",
                usuarioView.getPanelPrincipal(),
                950,
                650
        );
    }


    /*
     * ======================================================
     * PERIODOS ACADÉMICOS
     * ======================================================
     */
    private void abrirPeriodos() {

        PeriodoAcademicoView periodoAcademicoView =
                new PeriodoAcademicoView();

        abrirVentana(
                "Administración de Periodos Académicos",
                periodoAcademicoView.getPanelPrincipal(),
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