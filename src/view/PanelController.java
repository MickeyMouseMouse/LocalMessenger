package view;

public class PanelController { // singleton
    private static PanelController instance;
    public static synchronized PanelController getInstance() {
        if (instance == null) instance = new PanelController();
        return instance;
    }

    private static LocalMessenger mainFrame;
    private static final StartPanel startPanel = new StartPanel();
    private static final MessagingPanel messagingPanel = new MessagingPanel();
    public void setMainFrame(LocalMessenger mainFrame) {
        PanelController.mainFrame = mainFrame;
    }

    public void setStartPanel() {
        mainFrame.setContentPane(startPanel.getPanel());
        mainFrame.pack();

        messagingPanel.emptyChatBox();
    }

    public void setMessagingPanel(String nickname) {
        messagingPanel.emptyChatBox();
        messagingPanel.setInterlocutorNickname(nickname);
        mainFrame.setContentPane(messagingPanel.getPanel());
        mainFrame.setSize(500, 400);
    }

    public void addNewMessage(String text) {
        messagingPanel.insertText(text, true);
    }

    public void addNewMessageFromInterlocutor(String text) {
        messagingPanel.insertText(text, false);
    }

    public void addNewSmile(byte id) { messagingPanel.addSmile(id); }
}