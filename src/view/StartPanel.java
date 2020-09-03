package view;

import controller.Controller;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class StartPanel extends JPanel {
    private JPanel startPanel;
    private JTextField textFieldNickname;
    private JTextField textFieldDownloads;
    private JButton buttonClient;
    private JButton buttonServer;
    private JTextField textFieldServerIP;
    private JTextField textFieldPort;
    private JButton buttonGitHub;

    public StartPanel() {
        textFieldDownloads.setText(System.getProperty("user.dir"));
        buttonGitHub.setBorderPainted(false);

        buttonClient.addActionListener(e -> Controller.getInstance().createClient(textFieldNickname.getText(), textFieldDownloads.getText(),
                 textFieldServerIP.getText(), textFieldPort.getText()));

        buttonServer.addActionListener(e -> {
            if (buttonServer.getText().equals("Server")) {
                Controller.getInstance()
                        .createServer(textFieldNickname.getText(),
                                textFieldDownloads.getText(), textFieldPort.getText());
                buttonServer.setText("Destroy Server");
            } else {
                Controller.getInstance().destroyServer();
                buttonServer.setText("Server");
            }
        });

        buttonGitHub.addActionListener(e -> {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(new URI("https://github.com/MickeyMouseMouse/LocalMessenger"));
                } catch (IOException | URISyntaxException exp) {
                    exp.printStackTrace();
                }
            }
        });
    }

    public JPanel getPanel() {
        buttonServer.setText("Server");
        return startPanel;
    }
}
