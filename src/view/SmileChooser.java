package view;

import controller.Controller;

import javax.swing.*;
import java.awt.*;

public class SmileChooser extends JDialog {
    private JPanel contentPane;

    public SmileChooser() {
        this.setTitle("LocalMessenger");
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        GridLayout grid = new GridLayout(4, 5);
        contentPane.setLayout(grid);
        for (int i = 0; i < 20; i++) {
            JButton button = new JButton();

            byte id = (byte)(i + 1);
            button.setIcon(new ImageIcon(getClass().getResource("/resources/smiles/" + id + ".png")));
            button.addActionListener((e) -> {
                Controller.getInstance().sendSmile(id);
                PanelController.getInstance().addNewSmile(id);
            });

            contentPane.add(button);
        }

        setContentPane(contentPane);
    }

    public void showDialog() {
        this.setLocationRelativeTo(null);
        setMinimumSize(new Dimension(this.getWidth(), this.getHeight()));
        this.pack();
        this.setVisible(true);
    }
}
