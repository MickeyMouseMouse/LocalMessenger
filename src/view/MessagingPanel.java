package view;

import controller.Controller;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class MessagingPanel extends JPanel {
    private JPanel messagingPanel;
    private JButton buttonTerminate;
    private JButton buttonSound;
    private JButton buttonFile;
    private JButton buttonSmile;
    private JTextField textMessage;
    private JButton buttonSend;
    private JLabel labelInterlocutorNickname;
    private JTextPane chatBox;
    private JButton buttonBrightness;

    private boolean brightness = true; // true == light, false == dark

    private final SmileChooser smileChooser = new SmileChooser();

    public MessagingPanel() {
        buttonTerminate.addActionListener(e -> Controller.getInstance().terminateSession());

        buttonBrightness.addActionListener(e -> changeBrightness());

        buttonSound.addActionListener(e -> {
            if (Controller.getInstance().soundSwitcher())
                buttonSound.setIcon(new ImageIcon(getClass().getResource("/resources/soundOn.png")));
            else
                buttonSound.setIcon(new ImageIcon(getClass().getResource("/resources/soundOff.png")));
        });

        // make chatBox read-only
        chatBox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                e.consume();
            }

            @Override
            public void keyPressed(KeyEvent e) {
                e.consume();
            }
        });

        // make chatBox auto-scrolling
        DefaultCaret caret = (DefaultCaret)chatBox.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        buttonFile.addActionListener(e -> selectFile());

        buttonSmile.addActionListener(e -> smileChooser.showDialog() );

        // send message on ENTER button
        textMessage.addActionListener(
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        buttonSend.doClick();
                    }
                }
        );

        buttonSend.addActionListener(e -> {
            if (Controller.getInstance().sendString(textMessage.getText())) {
                insertText(textMessage.getText(), true);
                textMessage.setText("");
                textMessage.requestFocus();
            }
        });
    }

    public JPanel getPanel() { return messagingPanel; }

    public void setInterlocutorNickname(String nickname) {
        labelInterlocutorNickname.setText(nickname);
    }

    public void insertText(String text, boolean sender) {
        Color color;
        if (sender)
            color = Color.BLACK;
        else
            color = Color.GREEN;

        AttributeSet attributeSet = StyleContext
                .getDefaultStyleContext()
                .addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color);

        int pos = chatBox.getDocument().getLength();
        try {
            chatBox.getDocument().insertString(pos, text + "\n", attributeSet);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void addSmile(byte id) {
        int pos = chatBox.getDocument().getLength();

        chatBox.setCaretPosition(pos);
        chatBox.insertIcon(new ImageIcon(getClass()
                .getResource("/resources/smiles/" + id + ".png")));

        pos = chatBox.getDocument().getLength();
        try {
            chatBox.getDocument().insertString(pos, "\n", null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void selectFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select File");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            Controller.getInstance().sendFile(fileChooser.getSelectedFile());
        }
    }

    public void changeBrightness() {
        if (brightness) {
            buttonBrightness.setIcon(new ImageIcon(getClass().getResource("/resources/sun.png")));
            chatBox.setBackground(Color.GRAY);
            brightness = false;
        } else {
            buttonBrightness.setIcon(new ImageIcon(getClass().getResource("/resources/moon.png")));
            chatBox.setBackground(Color.WHITE);
            brightness = true;
        }
    }

    public void emptyChatBox() { chatBox.setText(""); }
}
