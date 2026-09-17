package universidad.asistencia;

import universidad.asistencia.view.MainForm;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            MainForm mainForm =
                    new MainForm();

            JFrame frame =
                    new JFrame(
                            "Sistema de Asistencia Universitaria"
                    );

            frame.setContentPane(
                    mainForm.getPanelPrincipal()
            );

            frame.setDefaultCloseOperation(
                    JFrame.EXIT_ON_CLOSE
            );

            frame.setSize(
                    750,
                    650
            );

            frame.setLocationRelativeTo(null);

            frame.setResizable(false);

            frame.setVisible(true);
        });
    }
}