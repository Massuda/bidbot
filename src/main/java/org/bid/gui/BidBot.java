package org.bid.gui;

import org.bid.thread.BidCounter;
import org.bid.thread.CountdownWatcher;
import org.bid.PropertiesLoader;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.util.Properties;

public class BidBot extends JFrame {
    private JPanel settingsPanel;
    private JTextField usernameField, urlField;
    private JPanel threadPanel;
    public JTextArea thread1TextArea, thread2TextArea;
    public JButton startThread1Button, stopThread1Button, startThread2Button, stopThread2Button;

    public Thread thread1, thread2;
    public boolean isSelectedManualAlertSound=false;
    public boolean isThread1Running = false, isThread2Running = false;
    public BidBot() {
        // Imposta il titolo della finestra
        super("BidBot");

        // Inizializza la finestra
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 800);

        // Crea i pannelli
        createSettingsPanel();
        createThreadPanel();

        // Crea e aggiungi le due tab alla finestra
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Impostazioni", settingsPanel);

        tabbedPane.addTab("Thread", threadPanel);

        // Aggiungi il tabbedPane alla finestra
        add(tabbedPane);

        // Mostra la finestra
        setVisible(true);
    }

    private JPanel createSection1() {
        JPanel sectionPanel =  new JPanel(new BorderLayout());

        // Titolo
        JLabel titleLabel = new JLabel("CountdownWatcher", SwingConstants.CENTER);
        sectionPanel.add(titleLabel, BorderLayout.NORTH);

        // Casella di testo non modificabile
        thread1TextArea = new JTextArea(10, 40);
        thread1TextArea.setEditable(false);
        sectionPanel.add(thread1TextArea, BorderLayout.CENTER);

        // Pannello per i bottoni a sinistra e a destra
        JPanel buttonPanel = new JPanel(new BorderLayout());

        startThread1Button = new JButton("Start");
        stopThread1Button = new JButton("Stop");
        stopThread1Button.setEnabled(false);

        buttonPanel.add(startThread1Button, BorderLayout.WEST);
        buttonPanel.add(stopThread1Button, BorderLayout.EAST);
        sectionPanel.add(new JScrollPane(thread1TextArea));
        sectionPanel.add(buttonPanel, BorderLayout.SOUTH);

        startThread1Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startThread1();
            }
        });

        stopThread1Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopThread1();
            }
        });

        return sectionPanel;
    }

    private JPanel createSection2() {
        JPanel sectionPanel = new JPanel(new BorderLayout());

        // Titolo
        JLabel titleLabel = new JLabel("BidCounter", SwingConstants.CENTER);
        sectionPanel.add(titleLabel, BorderLayout.NORTH);

        // Casella di testo non modificabile
        thread2TextArea = new JTextArea(10, 40);
        thread2TextArea.setEditable(false);
        sectionPanel.add(thread2TextArea, BorderLayout.CENTER);

        // Pannello per i bottoni a sinistra e a destra
        JPanel buttonPanel = new JPanel(new BorderLayout());

        startThread2Button = new JButton("Start");
        stopThread2Button = new JButton("Stop");
        stopThread2Button.setEnabled(false);

        buttonPanel.add(startThread2Button, BorderLayout.WEST);
        buttonPanel.add(stopThread2Button, BorderLayout.EAST);
        sectionPanel.add(new JScrollPane(thread2TextArea));
        sectionPanel.add(buttonPanel, BorderLayout.SOUTH);

        startThread2Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startThread2();
            }
        });

        stopThread2Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopThread2();
            }
        });

        return sectionPanel;
    }

    private void startThread1() {
        if (!isThread1Running) {
            CountdownWatcher countdownWatcher = new CountdownWatcher(this);
            thread1 = new Thread(countdownWatcher);
            thread1.start();
        }
    }

    private void stopThread1() {
        if (isThread1Running) {
            isThread1Running = false;
            thread1.interrupt();
        }
    }
    private void startThread2() {
        if (!isThread2Running) {
            BidCounter bidCounter = new BidCounter(this);
            thread2 = new Thread(bidCounter);
            thread2.start();
        }
    }

    private void stopThread2() {
        if (isThread2Running) {
            isThread2Running = false;
            thread2.interrupt();
        }
    }

    public void appendTextToTextArea(final JTextArea textArea, final String text) {
        SwingUtilities.invokeLater(() -> {
            textArea.append(text + "\n");
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
    }

    private void createThreadPanel() {
        threadPanel = new JPanel();
        JPanel mainPanel = new JPanel(new GridLayout(3, 1)); // 2 righe, 1 colonna

        JPanel section1 = createSection1();
        JPanel spacePanel = new JPanel();
        JPanel section2 = createSection2();
        mainPanel.add(section1);
        mainPanel.add(spacePanel);
        mainPanel.add(section2);

        threadPanel.add(mainPanel);
    }

    private void createSettingsPanel() {
        settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridLayout(5, 2));

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(20);
        JLabel urlLabel = new JLabel("URL:");
        urlField = new JTextField(20);

        settingsPanel.add(usernameLabel);
        settingsPanel.add(usernameField);
        settingsPanel.add(urlLabel);
        settingsPanel.add(urlField);

        PropertiesLoader propLoader = new PropertiesLoader();
        String user = propLoader.prop.getProperty("username");
        String url = propLoader.prop.getProperty("url");

        usernameField.setText(user);
        urlField.setText(url);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveSettings());

        JCheckBox  manualAlertSoundButton = new JCheckBox ("Manual Alert Sound");
        manualAlertSoundButton.addActionListener(e -> {
            isSelectedManualAlertSound = manualAlertSoundButton.isSelected();
        });

        settingsPanel.add(manualAlertSoundButton);

        settingsPanel.add(saveButton);
    }

    public void deleteState() {
        String currentDirectory = System.getProperty("user.dir");
        String fileName = "state.json";
        String filePath = currentDirectory + File.separator + fileName;
        File file = new File(filePath);
        if (file.delete()) {
            appendTextToTextArea(thread1TextArea, "state.json è stato eliminato con successo.");
        } else {
            JOptionPane.showMessageDialog(this, "Impossibile eliminare il file.", "Errore nell'eliminare state.json", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveSettings() {
        Properties properties = new Properties();
        properties.setProperty("username", usernameField.getText());
        properties.setProperty("url", urlField.getText());
        String currentDirectory = System.getProperty("user.dir");
        String fileName = "config.properties";
        String filePath = currentDirectory + File.separator + fileName;
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            properties.store(fos, "Impostazioni");
            JOptionPane.showMessageDialog(this, "Setting saved.", "Saved complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error during setting saving.", "Saving error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void soundAlert(String soundName){
        try {
            // Carica il suono da un file
            URL soundFile = getClass().getResource(soundName);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);

            // Configura il clip
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);

            // Riproduci il suono
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BidBot::new);
    }
}
