package view;

import controller.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class LocalMessenger extends JFrame {
    public static void main(String[] args) { new LocalMessenger(); }

    LocalMessenger() {
        PanelController panelSwitcher = new PanelController();
        panelSwitcher.setMainFrame(this);
        panelSwitcher.setStartPanel();

        this.setLocationRelativeTo(null);
        this.setTitle("LocalMessenger");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(this.getWidth(), this.getHeight()));
        setVisible(true);

        this.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            public void windowClosing(WindowEvent event) {
                Controller.getInstance().appClosing();
            }

            @Override
            public void windowClosed(WindowEvent e) {

            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });
    }
}