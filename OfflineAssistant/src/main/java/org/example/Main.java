package org.example;

import org.example.features.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.ByteArrayOutputStream;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Main extends JFrame {

    // ─── Config ───────────────────────────────────────────────────────────────
    private static final String WIT_AI_TOKEN = AppConfig.WIT_AI_TOKEN;

    // ─── Modern Color Scheme ─────────────────────────────────────────────────
    // ─── Modern Color Scheme ─────────────────────────────────────────────────
    private static final Color C_BG           = new Color(6, 8, 14);
    private static final Color C_BG_2         = new Color(10, 13, 22);
    private static final Color C_PANEL        = new Color(15, 19, 31);
    private static final Color C_PANEL_2      = new Color(20, 25, 40);
    private static final Color C_SIDEBAR      = new Color(9, 12, 20);
    private static final Color C_CARD         = new Color(18, 24, 38);
    private static final Color C_CARD_HOVER   = new Color(28, 36, 56);
    private static final Color C_BORDER       = new Color(52, 64, 94);

    private static final Color C_ACCENT       = new Color(0, 229, 255);    // cyan
    private static final Color C_ACCENT_SOFT  = new Color(86, 108, 255);   // violet-blue
    private static final Color C_ACCENT_2     = new Color(124, 92, 255);   // violet

    private static final Color C_TEXT         = new Color(238, 242, 255);
    private static final Color C_TEXT_DIM     = new Color(158, 170, 196);
    private static final Color C_TEXT_FAINT   = new Color(114, 124, 148);

    private static final Color C_USER_BG      = new Color(33, 43, 75);
    private static final Color C_BOT_BG       = new Color(15, 21, 33);

    private static final Color C_SUCCESS      = new Color(35, 214, 142);
    private static final Color C_WARN         = new Color(255, 193, 79);
    private static final Color C_ERROR        = new Color(255, 91, 118);
    private static final Color C_MIC_ON       = new Color(255, 72, 104);
    // ─── Fonts ────────────────────────────────────────────────────────────────
    private static final Font F_TITLE   = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font F_SUB     = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font F_HEADER  = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font F_BODY    = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font F_SMALL   = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font F_TINY    = new Font("Segoe UI", Font.BOLD, 10);
    private static final Font F_MONO    = new Font("Consolas", Font.PLAIN, 13);
    private static final Font F_CMD     = new Font("Segoe UI", Font.PLAIN, 13);

    // ─── State ────────────────────────────────────────────────────────────────
    private JPanel chatPanel;
    private JScrollPane chatScroll;
    private JTextField inputField;
    private JButton sendButton, micButton, voiceModeButton;
    private JLabel statusLabel, micStatusLabel;
    private JPanel commandListPanel;

    private boolean isListening = false;
    private boolean voiceModeEnabled = false;
    private Thread recordingThread;
    private TargetDataLine microphone;
    private ByteArrayOutputStream audioStream;

    private final List<Feature> features = new ArrayList<>();
    private final GeminiFeature gemini = new GeminiFeature();

    private final List<String> cmdHistory = new ArrayList<>();
    private int historyIndex = -1;

    private javax.swing.Timer micPulseTimer;
    private float pulseAlpha = 1.0f;
    private boolean pulseDir = false;

    // ─────────────────────────────────────────────────────────────────────────
    public Main() {
        registerFeatures();
        TextToSpeech.setEnabled(false);
        buildUI();
        initMicrophone();
        setVisible(true);
        showWelcome();

    }

    // ─────────────────────────────────────────────────────────────────────────
    //  FEATURE REGISTRATION
    // ─────────────────────────────────────────────────────────────────────────
    private void registerFeatures() {
        features.add(new DateTimeFeature());
//        features.add(new CalculatorFeature());
        features.add(new ReminderFeature());
        features.add(new ClipboardFeature());
        features.add(new WebSearchFeature());
        features.add(new SystemInfoFeature());
        features.add(new SystemControlFeature());
        features.add(new SystemAutomationFeature());
        features.add(new AppLaunchFeature());
        features.add(new FileSearchFeature());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  UI BUILD
    // ─────────────────────────────────────────────────────────────────────────
    private void buildUI() {
        setTitle("Desktop Assistant");
        setSize(1260, 800);
        setMinimumSize(new Dimension(980, 640));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setUndecorated(false);
        getContentPane().setBackground(C_BG);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(C_BG);
        setContentPane(root);

        root.add(buildTitleBar(), BorderLayout.NORTH);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildChatArea(), BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);
    }

    // ── Title Bar ─────────────────────────────────────────────────────────────
    private JPanel buildTitleBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(12, 16, 28),
                        getWidth(), 0, new Color(20, 16, 36)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(C_BORDER);
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 68));
        bar.setBorder(new EmptyBorder(0, 20, 0, 20));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JLabel logo = new JLabel("◧");
        logo.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 27));
        logo.setForeground(C_ACCENT);

        JPanel titleStack = new JPanel();
        titleStack.setOpaque(false);
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Desktop Assistant");
        title.setFont(F_TITLE);
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Smart control panel");
        subtitle.setFont(F_SUB);
        subtitle.setForeground(C_TEXT_DIM);

        titleStack.add(title);
        titleStack.add(subtitle);

        left.add(logo);
        left.add(titleStack);

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 12));
        centerPanel.setOpaque(false);

        voiceModeButton = buildPillButton("🔈 Voice OFF", new Color(64, 76, 112), 158, 38);
        voiceModeButton.addActionListener(e -> toggleVoiceMode());
        centerPanel.add(voiceModeButton);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 15));
        right.setOpaque(false);

        JLabel liveDot = new JLabel("●");
        liveDot.setForeground(C_SUCCESS);
        liveDot.setFont(new Font("Segoe UI", Font.BOLD, 11));

        JLabel clockLabel = new JLabel();
        clockLabel.setFont(new Font("Consolas", Font.PLAIN, 14));
        clockLabel.setForeground(C_TEXT_DIM);

        javax.swing.Timer clockTimer = new javax.swing.Timer(1000, e ->
                clockLabel.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
        );
        clockTimer.start();

        right.add(liveDot);
        right.add(clockLabel);

        bar.add(left, BorderLayout.WEST);
        bar.add(centerPanel, BorderLayout.CENTER);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(C_SIDEBAR);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(34, 46, 74));
                g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
            }
        };
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setLayout(new BorderLayout());
        sidebar.setOpaque(false);

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBorder(new EmptyBorder(18, 14, 18, 14));

        inner.add(sidebarSection("QUICK COMMANDS"));
        inner.add(Box.createVerticalStrut(10));

        String[][] cmds = {
                {"🕐", "What time is it?"},
                {"📅", "What is today's date?"},
                {"💻", "System status"},
                {"📸", "Take screenshot"},
                {"🔊", "Volume up"},
                {"🔇", "Mute sound"},
                {"🔒", "Lock screen"},
//                {"📋", "Show clipboard history"},
                {"🌐", "Search for Java tutorials"},
                {"▶️", "YouTube lo-fi music"},
                {"📁", "Find any file"},
                {"⚙️", "Start coding mode"},
                {"📚", "Start study mode"},
                {"💼", "Start work mode"},
                {"🎵", "Start entertainment mode"},
                {"📊", "App opening frequency"}
        };

        for (String[] cmd : cmds) {
            inner.add(buildCommandChip(cmd[0], cmd[1]));
            inner.add(Box.createVerticalStrut(6));
        }

        inner.add(Box.createVerticalStrut(18));
        inner.add(sidebarSection("RECENT COMMANDS"));
        inner.add(Box.createVerticalStrut(10));

        commandListPanel = new JPanel();
        commandListPanel.setOpaque(false);
        commandListPanel.setLayout(new BoxLayout(commandListPanel, BoxLayout.Y_AXIS));
        inner.add(commandListPanel);

        JScrollPane sideScroll = new JScrollPane(inner);
        sideScroll.setBorder(null);
        sideScroll.setOpaque(false);
        sideScroll.getViewport().setOpaque(false);
        sideScroll.getVerticalScrollBar().setUnitIncrement(12);
        sideScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        styleScrollBar(sideScroll.getVerticalScrollBar());

        sidebar.add(sideScroll, BorderLayout.CENTER);
        return sidebar;
    }

    private JLabel sidebarSection(String title) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(F_TINY);
        lbl.setForeground(C_ACCENT);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(0, 4, 0, 0));
        return lbl;
    }

    private JPanel buildCommandChip(String icon, String text) {
        JPanel chip = new JPanel(new BorderLayout(10, 0)) {
            private boolean hovered = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(hovered ? C_CARD_HOVER : C_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));

                g2.setColor(hovered ? C_ACCENT_SOFT : C_BORDER);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 14, 14));
            }
        };
        chip.setOpaque(false);
        chip.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chip.setBorder(new EmptyBorder(8, 10, 8, 10));
        chip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        chip.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel iconLbl = new JLabel(icon, SwingConstants.CENTER);
        iconLbl.setPreferredSize(new Dimension(24, 24));
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        iconLbl.setForeground(Color.WHITE);

        JLabel textLbl = new JLabel(text);
        textLbl.setFont(F_CMD);
        textLbl.setForeground(C_TEXT);

        chip.add(iconLbl, BorderLayout.WEST);
        chip.add(textLbl, BorderLayout.CENTER);

        chip.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (text.equalsIgnoreCase("Find any file")) {
                    String fileName = JOptionPane.showInputDialog(
                            Main.this,
                            "Enter the file name to search:",
                            "Find File",
                            JOptionPane.QUESTION_MESSAGE
                    );

                    if (fileName != null && !fileName.trim().isEmpty()) {
                        inputField.setText("find file " + fileName.trim());
                        processInput();
                    }
                } else {
                    inputField.setText(text);
                    processInput();
                }
            }
        });

        return chip;
    }

    // ── Chat Area ─────────────────────────────────────────────────────────────
    private JPanel buildChatArea() {
        JPanel container = new JPanel(new BorderLayout(0, 0));
        container.setBackground(C_BG);

        chatPanel = new JPanel();
        chatPanel.setBackground(C_BG_2);
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBorder(new EmptyBorder(18, 18, 18, 18));

        chatScroll = new JScrollPane(chatPanel);
        chatScroll.setBorder(null);
        chatScroll.setBackground(C_BG_2);
        chatScroll.getViewport().setBackground(C_BG_2);
        chatScroll.getVerticalScrollBar().setUnitIncrement(18);
        chatScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        styleScrollBar(chatScroll.getVerticalScrollBar());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(C_BG_2);
        wrapper.add(chatScroll, BorderLayout.CENTER);
        wrapper.add(buildInputArea(), BorderLayout.SOUTH);

        container.add(wrapper, BorderLayout.CENTER);
        return container;
    }

    // ── Input Area ────────────────────────────────────────────────────────────
    private JPanel buildInputArea() {
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(C_PANEL);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(34, 46, 74));
                g2.drawLine(0, 0, getWidth(), 0);
            }
        };
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(16, 18, 14, 18));

        JPanel inputRow = new JPanel(new BorderLayout(12, 0));
        inputRow.setOpaque(false);

        inputField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_PANEL_2);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(C_BORDER);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 16, 16));
                super.paintComponent(g);
            }
        };
        inputField.setOpaque(false);
        inputField.setBackground(new Color(0, 0, 0, 0));
        inputField.setForeground(C_TEXT);
        inputField.setCaretColor(C_ACCENT);
        inputField.setFont(F_BODY);
        inputField.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        inputField.putClientProperty("JTextField.placeholderText", "Type a command or ask anything...");

        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP && !cmdHistory.isEmpty()) {
                    historyIndex = Math.min(historyIndex + 1, cmdHistory.size() - 1);
                    inputField.setText(cmdHistory.get(cmdHistory.size() - 1 - historyIndex));
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (historyIndex > 0) {
                        historyIndex--;
                        inputField.setText(cmdHistory.get(cmdHistory.size() - 1 - historyIndex));
                    } else {
                        historyIndex = -1;
                        inputField.setText("");
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    processInput();
                }
            }
        });

        sendButton = buildPillButton("➤ Send", C_ACCENT, 104, 46);
        sendButton.addActionListener(e -> processInput());

        micButton = buildPillButton("🎤 Mic", new Color(74, 84, 112), 96, 46);
        micButton.addActionListener(e -> toggleMic());

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(micButton);
        btnPanel.add(sendButton);

        inputRow.add(inputField, BorderLayout.CENTER);
        inputRow.add(btnPanel, BorderLayout.EAST);
        wrapper.add(inputRow, BorderLayout.CENTER);

        micStatusLabel = new JLabel("  Voice ready");
        micStatusLabel.setFont(F_SMALL);
        micStatusLabel.setForeground(C_TEXT_DIM);
        micStatusLabel.setBorder(new EmptyBorder(8, 2, 0, 0));
        wrapper.add(micStatusLabel, BorderLayout.SOUTH);

        return wrapper;
    }

    // ── Status Bar ────────────────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(9, 13, 23));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(34, 46, 74));
                g2.drawLine(0, 0, getWidth(), 0);
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 30));
        bar.setBorder(new EmptyBorder(0, 16, 0, 16));

        statusLabel = new JLabel("● Ready  |  " + features.size() + " features loaded  |  AI connected");
        statusLabel.setFont(new Font("Consolas", Font.PLAIN, 11));
        statusLabel.setForeground(C_SUCCESS);

        JLabel versionLabel = new JLabel("Desktop Assistant  |  Java " + System.getProperty("java.version"));
        versionLabel.setFont(new Font("Consolas", Font.PLAIN, 11));
        versionLabel.setForeground(C_TEXT_FAINT);

        bar.add(statusLabel, BorderLayout.WEST);
        bar.add(versionLabel, BorderLayout.EAST);
        return bar;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  CHAT BUBBLE SYSTEM
    // ─────────────────────────────────────────────────────────────────────────
    private void addMessage(String sender, String message, MessageType type) {
        SwingUtilities.invokeLater(() -> {
            JPanel bubble = createBubble(sender, message, type);
            chatPanel.add(bubble);
            chatPanel.add(Box.createVerticalStrut(10));
            chatPanel.revalidate();
            chatPanel.repaint();

            SwingUtilities.invokeLater(() -> {
                JScrollBar bar = chatScroll.getVerticalScrollBar();
                bar.setValue(bar.getMaximum());
            });

            if (voiceModeEnabled
                    && type != MessageType.USER
                    && !sender.contains("❌")
                    && !sender.contains("⚠")
                    && !sender.contains("Voice")
                    && message.length() <= 220) {
                TextToSpeech.speak(message);
            }
        });
    }

    private JPanel createBubble(String sender, String message, MessageType type) {
        boolean isUser = type == MessageType.USER;
        boolean isError = sender.contains("Error") || sender.contains("❌");
        boolean isSuccess = sender.contains("✅");
        boolean isSystem = type == MessageType.SYSTEM;

        Color bubbleBg = isUser ? new Color(30, 43, 78) :
                isError ? new Color(68, 24, 31) :
                        isSuccess ? new Color(18, 50, 36) :
                                isSystem ? new Color(28, 30, 48) : C_BOT_BG;

        Color accentBar = isUser ? C_ACCENT_SOFT :
                isError ? C_ERROR :
                        isSuccess ? C_SUCCESS :
                                isSystem ? C_WARN : C_ACCENT;

        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        outer.setAlignmentX(Component.LEFT_ALIGNMENT);
        outer.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel bubble = new JPanel(new BorderLayout(0, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bubbleBg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 18, 18));
                g2.setColor(C_BORDER);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 18, 18));
                g2.setColor(accentBar);
                g2.fillRoundRect(0, 0, 4, getHeight(), 8, 8);
            }
        };
        bubble.setOpaque(false);
        bubble.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel senderLabel = new JLabel(sender);
        senderLabel.setFont(F_HEADER);
        senderLabel.setForeground(accentBar);

        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(F_SMALL);
        timeLabel.setForeground(C_TEXT_FAINT);

        header.add(senderLabel, BorderLayout.WEST);
        header.add(timeLabel, BorderLayout.EAST);

        JTextArea msgText = new JTextArea(message) {
            @Override public boolean isFocusable() { return false; }
        };
        msgText.setOpaque(false);
        msgText.setForeground(C_TEXT);
        msgText.setFont((message.startsWith("═") || message.contains("•") || message.contains("→")) ? F_MONO : F_BODY);
        msgText.setLineWrap(true);
        msgText.setWrapStyleWord(true);
        msgText.setEditable(false);
        msgText.setMargin(new Insets(2, 0, 0, 0));
        msgText.setBackground(new Color(0, 0, 0, 0));

        bubble.add(header, BorderLayout.NORTH);
        bubble.add(msgText, BorderLayout.CENTER);
        outer.add(bubble, BorderLayout.CENTER);

        return outer;
    }

    enum MessageType { USER, BOT, SYSTEM }

    // ─────────────────────────────────────────────────────────────────────────
    //  INPUT PROCESSING
    // ─────────────────────────────────────────────────────────────────────────
    private void processInput() {
        String input = inputField.getText().trim();
        if (input.isEmpty()) return;

        inputField.setText("");
        historyIndex = -1;

        cmdHistory.add(input);
        UsageTracker.trackCommand(input);
        if (cmdHistory.size() > 50) cmdHistory.remove(0);
        addToSidebarHistory(input);

        addMessage("👤 You", input, MessageType.USER);
        setStatus("Processing: " + input, C_WARN);

        if (input.equalsIgnoreCase("clear") || input.equalsIgnoreCase("clear chat")) {
            clearChat();
            return;
        }

        if (input.equalsIgnoreCase("help")) {
            showHelp();
            return;
        }

        if (input.equalsIgnoreCase("clear history") || input.equalsIgnoreCase("clear conversation")) {
            gemini.clearHistory();
            addMessage("✅ System", "Conversation history cleared.", MessageType.SYSTEM);
            setStatus("Ready", C_SUCCESS);
            return;
        }

        if (input.equalsIgnoreCase("App opening frequency") || input.equalsIgnoreCase("app usage")) {
            addMessage("📊 Usage Tracker", UsageTracker.getTopAppsReport(), MessageType.SYSTEM);
            setStatus("Ready", C_SUCCESS);
            return;
        }

        if (input.equalsIgnoreCase("voice mode on")) {
            voiceModeEnabled = true;
            TextToSpeech.setEnabled(true);
            updateVoiceModeButton();
            addMessage("🔊 Voice Mode", "Voice response mode is now ON.", MessageType.SYSTEM);
            setStatus("Ready", C_SUCCESS);
            return;
        }

        if (input.equalsIgnoreCase("voice mode off")) {
            voiceModeEnabled = false;
            TextToSpeech.setEnabled(false);
            TextToSpeech.stop();
            updateVoiceModeButton();
            addMessage("🔇 Voice Mode", "Voice response mode is now OFF.", MessageType.SYSTEM);
            setStatus("Ready", C_SUCCESS);
            return;
        }

        new Thread(() -> {
            for (Feature f : features) {
                if (f.canHandle(input)) {
                    try {
                        f.execute(input, (sender, msg) -> {
                            addMessage(sender, msg, MessageType.BOT);
                            setStatus("Ready", C_SUCCESS);
                        });
                    } catch (Exception e) {
                        addMessage("❌ Error", "Feature crashed: " + e.getMessage(), MessageType.BOT);
                        setStatus("Ready", C_SUCCESS);
                    }
                    return;
                }
            }

            try {
                gemini.execute(input, (sender, msg) -> {
                    addMessage(sender, msg, MessageType.BOT);
                    setStatus("Ready", C_SUCCESS);
                });
            } catch (Exception e) {
                addMessage("❌ Error", "AI failed: " + e.getMessage(), MessageType.BOT);
                setStatus("Ready", C_SUCCESS);
            }
        }).start();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  MIC / VOICE
    // ─────────────────────────────────────────────────────────────────────────
    private void initMicrophone() {
        try {
            AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            microphone = (TargetDataLine) AudioSystem.getLine(info);
        } catch (Exception e) {
            micButton.setEnabled(false);
            micStatusLabel.setText("  ⚠ Microphone unavailable");
        }
    }

    private void toggleMic() {
        if (!isListening) startListening();
        else stopAndSend();
    }

    private void toggleVoiceMode() {
        voiceModeEnabled = !voiceModeEnabled;
        TextToSpeech.setEnabled(voiceModeEnabled);
        updateVoiceModeButton();

        if (voiceModeEnabled) {
            micStatusLabel.setText("  Voice replies enabled");
            addMessage("🔊 Voice Mode", "Voice response mode is now ON.", MessageType.SYSTEM);
        } else {
            micStatusLabel.setText("  Voice replies disabled");
            TextToSpeech.stop();
            addMessage("🔇 Voice Mode", "Voice response mode is now OFF.", MessageType.SYSTEM);
        }
    }

    private void updateVoiceModeButton() {
        if (voiceModeButton == null) return;

        if (voiceModeEnabled) {
            voiceModeButton.setText("🔊 Voice ON");
            voiceModeButton.setBackground(new Color(0, 180, 200));
        } else {
            voiceModeButton.setText("🔈 Voice OFF");
            voiceModeButton.setBackground(new Color(64, 76, 112));
        }
    }

    private void startListening() {
        if (microphone == null) return;
        isListening = true;
        micButton.setText("⏹ Stop");
        micButton.setBackground(C_MIC_ON);
        micStatusLabel.setText("  Recording — click Stop when done");
        audioStream = new ByteArrayOutputStream();
        setStatus("Listening...", C_MIC_ON);

        startMicPulse();

        recordingThread = new Thread(() -> {
            try {
                microphone.open(new AudioFormat(16000, 16, 1, true, false));
                microphone.start();
                byte[] buffer = new byte[4096];
                while (isListening) {
                    int n = microphone.read(buffer, 0, buffer.length);
                    if (n > 0) audioStream.write(buffer, 0, n);
                }
            } catch (Exception e) {
                addMessage("❌ Voice", "Mic error: " + e.getMessage(), MessageType.SYSTEM);
            }
        });
        recordingThread.start();
    }

    private void stopAndSend() {
        isListening = false;
        stopMicPulse();

        micButton.setText("🎤 Mic");
        micButton.setBackground(new Color(74, 84, 112));
        micStatusLabel.setText("  Processing voice...");

        if (microphone != null && microphone.isOpen()) {
            microphone.stop();
            microphone.close();
        }

        new Thread(() -> {
            try {
                byte[] audio = audioStream.toByteArray();
                CloseableHttpClient client = HttpClients.createDefault();
                HttpPost post = new HttpPost("https://api.wit.ai/speech");
                post.setHeader("Authorization", "Bearer " + WIT_AI_TOKEN);
                post.setHeader("Content-Type", "audio/raw;encoding=signed-integer;bits=16;rate=16000;endian=little");
                post.setEntity(new ByteArrayEntity(audio));

                String body = EntityUtils.toString(client.execute(post).getEntity(), "UTF-8");
                String finalText = "";

                for (String chunk : body.split("\r\n")) {
                    if (chunk.trim().startsWith("{")) {
                        JsonObject obj = JsonParser.parseString(chunk).getAsJsonObject();
                        if (obj.has("text") && !obj.get("text").isJsonNull()) {
                            finalText = obj.get("text").getAsString();
                        }
                    }
                }

                if (!finalText.isEmpty()) {
                    final String text = finalText;
                    SwingUtilities.invokeLater(() -> {
                        inputField.setText(text);
                        micStatusLabel.setText("  Heard: \"" + text + "\"");
                        processInput();
                    });
                } else {
                    micStatusLabel.setText("  Couldn't hear anything — try again");
                    addMessage("⚠️ Voice", "Could not transcribe audio. Speak clearly and try again.", MessageType.SYSTEM);
                }
            } catch (Exception e) {
                micStatusLabel.setText("  Voice error");
                addMessage("❌ Voice", "Cloud error: " + e.getMessage(), MessageType.SYSTEM);
            } finally {
                setStatus("Ready", C_SUCCESS);
                SwingUtilities.invokeLater(this::initMicrophone);
            }
        }).start();
    }

    private void startMicPulse() {
        micPulseTimer = new javax.swing.Timer(80, e -> {
            if (pulseDir) {
                pulseAlpha += 0.08f;
                if (pulseAlpha >= 1.0f) {
                    pulseAlpha = 1.0f;
                    pulseDir = false;
                }
            } else {
                pulseAlpha -= 0.08f;
                if (pulseAlpha <= 0.3f) {
                    pulseAlpha = 0.3f;
                    pulseDir = true;
                }
            }

            float r = C_MIC_ON.getRed() / 255f;
            float g = C_MIC_ON.getGreen() / 255f;
            float b = C_MIC_ON.getBlue() / 255f;
            micButton.setBackground(new Color(r, g, b, pulseAlpha));
        });
        micPulseTimer.start();
    }

    private void stopMicPulse() {
        if (micPulseTimer != null) micPulseTimer.stop();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────────────────
    private void showWelcome() {
        String welcome =
                "Hello! Welcome to Desktop Assistant 👋\n\n" +
                        "Available features:\n" +
                        "• System controls: volume, brightness, screenshot, lock, shutdown\n" +
                        "• File tools: find, open, delete, rename, create files and folders\n" +
                        "• App control: open/close Chrome, Notepad, VS Code, Spotify and more\n" +
                        "• Automation modes: coding, study, work, entertainment\n" +
                        "• Reminders and clipboard utilities\n" +
                        "• Web search and AI chat support\n" +
                        "• Voice reply mode from the top toggle\n\n" +
                        "Try a quick command from the left or type 'help' for commands.";
        addMessage("◧ Desktop Assistant", welcome, MessageType.BOT);
    }

    private void showHelp() {
        String help =
                "═══════════════════════════════\n" +
                        "  DESKTOP ASSISTANT — HELP\n" +
                        "═══════════════════════════════\n\n" +
                        "APP CONTROL\n" +
                        "  open chrome / notepad / calculator / spotify\n" +
                        "  close chrome / close notepad\n\n" +
                        "AUTOMATION\n" +
                        "  start coding mode\n" +
                        "  start study mode\n" +
                        "  start work mode\n" +
                        "  start entertainment mode\n" +
                        "  App opening frequency\n\n" +
                        "VOICE\n" +
                        "  voice mode on\n" +
                        "  voice mode off\n\n" +
                        "SYSTEM\n" +
                        "  volume up / volume down / mute / unmute\n" +
                        "  brightness up / brightness 80\n" +
                        "  take screenshot\n" +
                        "  lock screen / sleep / shutdown / restart\n" +
                        "  empty recycle bin\n\n" +
                        "FILES\n" +
                        "  find file report.pdf\n" +
                        "  open file C:\\path\\to\\file.txt\n" +
                        "  delete file myfile.txt\n" +
                        "  rename file old.txt to new.txt\n" +
                        "  create folder ProjectX\n" +
                        "  create file notes.txt\n\n" +
                        "WEB\n" +
                        "  search for Python tutorials\n" +
                        "  youtube lo-fi beats\n" +
                        "  wikipedia Elon Musk\n" +
                        "  go to github.com\n\n" +
                        "PRODUCTIVITY\n" +
                        "  what time is it?\n" +
                        "  remind me in 10 minutes to check email\n" +
                        "  show clipboard history / copy Hello World\n" +
                        "  system status / cpu / ram / battery\n\n" +
                        "CHAT\n" +
                        "  clear chat\n" +
                        "  clear history\n" +
                        "  anything else goes to AI";
        addMessage("📖 Help", help, MessageType.SYSTEM);
    }

    private void clearChat() {
        chatPanel.removeAll();
        chatPanel.revalidate();
        chatPanel.repaint();
        addMessage("✅ System", "Chat cleared.", MessageType.SYSTEM);
    }

    private void addToSidebarHistory(String cmd) {
        SwingUtilities.invokeLater(() -> {
            if (commandListPanel.getComponentCount() >= 8) {
                commandListPanel.remove(commandListPanel.getComponentCount() - 1);
            }

            JLabel lbl = new JLabel("› " + (cmd.length() > 24 ? cmd.substring(0, 24) + "…" : cmd));
            lbl.setFont(F_CMD);
            lbl.setForeground(C_TEXT_DIM);
            lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
            lbl.setBorder(new EmptyBorder(5, 8, 5, 8));

            lbl.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    inputField.setText(cmd);
                    processInput();
                }

                @Override public void mouseEntered(MouseEvent e) {
                    lbl.setForeground(C_TEXT);
                }

                @Override public void mouseExited(MouseEvent e) {
                    lbl.setForeground(C_TEXT_DIM);
                }
            });

            commandListPanel.add(lbl, 0);
            commandListPanel.revalidate();
            commandListPanel.repaint();
        });
    }

    private void setStatus(String text, Color color) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("● " + text);
            statusLabel.setForeground(color);
        });
    }

    private JButton buildPillButton(String text, Color bg, int width, int height) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(
                        0, 0, getBackground().brighter(),
                        0, getHeight(), getBackground()
                );
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));

                g2.setColor(C_BORDER);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 16, 16));

                super.paintComponent(g);
            }
        };
        btn.setPreferredSize(new Dimension(width, height));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            private Color original;

            @Override
            public void mouseEntered(MouseEvent e) {
                original = btn.getBackground();
                btn.setBackground(original.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (original != null) btn.setBackground(original);
            }
        });

        return btn;
    }

    private void styleScrollBar(JScrollBar bar) {
        bar.setBackground(C_PANEL);
        bar.setForeground(C_BORDER);
        bar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(78, 98, 145);
                this.trackColor = C_PANEL;
            }

            @Override protected JButton createDecreaseButton(int o) { return zeroButton(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroButton(); }

            private JButton zeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(Main::new);
    }
}