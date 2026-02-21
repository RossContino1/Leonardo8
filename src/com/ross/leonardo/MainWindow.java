/*
 * Leonardo - Media Conversion Tool
 * Copyright (c) 2026 Ross Contino
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */




package com.ross.leonardo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;



public class MainWindow extends JFrame {

    private JTextField inputField;
    private JTextField outputField;
    private JButton convertButton;
    private JButton cancelButton;
    private JProgressBar progressBar;
    private VideoConverter currentConverter;
    private JComboBox<Preset> presetComboBox;
    private javax.swing.Timer statusTimer;
    private java.util.Properties config;
    private java.io.File configFile;
   

    




    public MainWindow() {

    	initializeConfig();

    	
    	setTitle(AppInfo.NAME + "  •  v" + AppInfo.VERSION);
    	pack();
    	setMinimumSize(new Dimension(780, 430));  // tweak if you want
    	setLocationRelativeTo(null);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10,10));

        add(createMainPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
        setJMenuBar(createMenu());

        enableDragAndDrop();
        setAppIcon();

        checkFFmpegOnStartup();  // AFTER UI exists
        
        setVisible(true);

    }

    private JPanel createMainPanel() {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        inputField = new JTextField();
        outputField = new JTextField();
        outputField.setEditable(false);

        JButton browseBtn = new JButton("Browse");
        browseBtn.addActionListener(e -> chooseFile());

        convertButton = new JButton("Convert");
        convertButton.addActionListener(e -> startConversion());

        cancelButton = new JButton("Cancel");
        cancelButton.setEnabled(false);
        cancelButton.addActionListener(e -> cancelConversion());


     // ----- Preset Label -----
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        panel.add(new JLabel("Preset:"), gbc);

        // ----- Preset Dropdown -----
        presetComboBox = new JComboBox<>();
        initializePresets();
        restoreLastPreset();

        gbc.gridy = 1;
        panel.add(presetComboBox, gbc);
        presetComboBox.addActionListener(e -> {
            String inputPath = inputField.getText();
            if (!inputPath.isEmpty()) {
                setOutputFromPreset(new File(inputPath));
            }
        });

        // Reset gridwidth for normal 2-column layout
        gbc.gridwidth = 1;

        // ----- Input Label -----
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1;
        panel.add(new JLabel("Input File:"), gbc);

        // ----- Input Field -----
        gbc.gridy = 3;
        panel.add(inputField, gbc);

        // ----- Browse Button -----
        gbc.gridx = 1;
        gbc.weightx = 0;
        panel.add(browseBtn, gbc);

        // ----- Output Label -----
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        panel.add(new JLabel("Output File:"), gbc);

        // ----- Output Field -----
        gbc.gridy = 5;
        panel.add(outputField, gbc);

        // Reset gridwidth
        gbc.gridwidth = 1;

        // ----- Button Panel (Centered) -----
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.add(convertButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        // Reset gridwidth
        gbc.gridwidth = 1;

        return panel;
    }

    private JLabel statusLabel;

    private JPanel createBottomPanel() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));

        progressBar = new JProgressBar(0,100);
        progressBar.setStringPainted(true);

        statusLabel = new JLabel(" ");
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.ITALIC, 12f));

        panel.add(progressBar);
        panel.add(Box.createVerticalStrut(4));
        panel.add(statusLabel);

        return panel;
    }



    private JMenuBar createMenu() {

        JMenuBar menuBar = new JMenuBar();

        // ===== FILE MENU =====
        JMenu fileMenu = new JMenu("File");

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(exitItem);


        // ===== VIEW MENU =====
        JMenu viewMenu = new JMenu("View");

        JMenuItem systemTheme = new JMenuItem("System Theme");
        JMenuItem lightTheme = new JMenuItem("Light Theme");
        JMenuItem darkTheme = new JMenuItem("Dark Theme");

        systemTheme.addActionListener(e -> changeTheme("system"));
        lightTheme.addActionListener(e -> changeTheme("light"));
        darkTheme.addActionListener(e -> changeTheme("dark"));

        viewMenu.add(systemTheme);
        viewMenu.add(lightTheme);
        viewMenu.add(darkTheme);


        // ===== HELP MENU =====
        JMenu helpMenu = new JMenu("Help");

        JMenuItem userGuideItem = new JMenuItem("User Guide");
        userGuideItem.addActionListener(e -> showUserGuide());

        JMenuItem docsItem = new JMenuItem("Online Documentation");
        docsItem.addActionListener(e ->
                openWebsite(AppInfo.WEBSITE + "/leonardo")
        );

        JMenuItem supportItem = new JMenuItem("Support Leonardo");
        supportItem.addActionListener(e ->
                openWebsite(AppInfo.WEBSITE + "/support-leonardo")
        );

        JMenuItem githubItem = new JMenuItem("GitHub Repository");
        githubItem.addActionListener(e ->
                openWebsite("https://github.com/RossContino1/Leonardo")
        );

        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> new AboutDialog(this));

        helpMenu.add(userGuideItem);
        helpMenu.addSeparator();
        helpMenu.add(docsItem);
        helpMenu.add(supportItem);
        helpMenu.add(githubItem);
        helpMenu.addSeparator();
        helpMenu.add(aboutItem);


        // ===== ADD MENUS TO BAR =====
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }


    private void chooseFile() {

        JFileChooser chooser = new JFileChooser();

        // Restore last used directory
        String lastDir = config.getProperty("lastFolder");
        if (lastDir != null) {
            chooser.setCurrentDirectory(new File(lastDir));
        }

        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {

            File file = chooser.getSelectedFile();

            // Set input field
            inputField.setText(file.getAbsolutePath());

            // Save last folder
            config.setProperty("lastFolder", file.getParent());
            saveConfig();

            // Auto-set output based on selected preset
            setOutputFromPreset(file);
        }
    }


    private void startConversion() {

        String input = inputField.getText();
        String output = outputField.getText();

        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select a file first.");
            return;
        }

        presetComboBox.setEnabled(false);
        convertButton.setEnabled(false);
        progressBar.setValue(0);

        currentConverter = new VideoConverter(
                input,
                output,
                progressBar,
                convertButton,
                this
        );

        convertButton.setEnabled(false);
        cancelButton.setEnabled(true);
        
        Preset selectedPreset = (Preset) presetComboBox.getSelectedItem();
        config.setProperty("lastPreset", selectedPreset.getName());
        saveConfig();


        currentConverter.execute();

    }

    private void enableDragAndDrop() {

        setTransferHandler(new TransferHandler() {

            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(
                        DataFlavor.javaFileListFlavor);
            }

            public boolean importData(TransferSupport support) {
                try {
                    List<File> files = (List<File>)
                            support.getTransferable()
                                    .getTransferData(
                                            DataFlavor.javaFileListFlavor);

                    File file = files.get(0);
                    inputField.setText(file.getAbsolutePath());

                    String name = file.getAbsolutePath().toLowerCase();
                    if (name.endsWith(".mp4")) {
                        outputField.setText(file.getAbsolutePath()
                                .replaceAll("(?i)\\.mp4$", ".mov"));
                    }
                    else if (name.endsWith(".mov")) {
                        outputField.setText(file.getAbsolutePath()
                                .replaceAll("(?i)\\.mov$", ".mp4"));
                    }

                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        });
    }

    private void cancelConversion() {

        if (currentConverter != null) {
            currentConverter.cancel(true);
        }

        cancelButton.setEnabled(false);
        convertButton.setEnabled(true);
        progressBar.setValue(0);
    }
    
    private void initializePresets() {

        // --- DaVinci (LOCKED PARAMETERS) ---
        Preset davinciPreset = new Preset(
                "DaVinci Resolve (Linux Free Compatible)",
                ".mov",
                java.util.List.of(
                        "-vcodec", "mjpeg",
                        "-q:v", "2",
                        "-acodec", "pcm_s16be",
                        "-q:a", "0",
                        "-f", "mov"
                )
        );

        // --- OBS Remux (Fast, No Re-Encode) ---
        Preset remuxPreset = new Preset(
                "OBS Remux (Fast, No Re-Encode)",
                ".mp4",
                java.util.List.of(
                        "-c", "copy"
                )
        );

        // --- YouTube Export ---
        Preset youtubePreset = new Preset(
                "YouTube H.264",
                ".mp4",
                java.util.List.of(
                        "-c:v", "libx264",
                        "-preset", "slow",
                        "-crf", "18",
                        "-c:a", "aac",
                        "-b:a", "192k"
                )
        );

     // --- TikTok Vertical (9:16 Auto Crop) ---
        Preset tiktokPreset = new Preset(
                "TikTok Vertical 9:16 (Auto Crop)",
                ".mp4",
                java.util.List.of(
                        "-vf", "crop=in_h*9/16:in_h,scale=1080:1920",
                        "-c:v", "libx264",
                        "-preset", "medium",
                        "-crf", "20",
                        "-pix_fmt", "yuv420p",
                        "-profile:v", "high",
                        "-level", "4.1",
                        "-c:a", "aac",
                        "-b:a", "160k",
                        "-movflags", "+faststart"
                )
        );
        
        presetComboBox.addItem(davinciPreset);
        presetComboBox.addItem(remuxPreset);
        presetComboBox.addItem(youtubePreset);
        presetComboBox.addItem(tiktokPreset);


        presetComboBox.setSelectedIndex(0); // Default to DaVinci
    }

    public void conversionFinished() {
        presetComboBox.setEnabled(true);
        cancelButton.setEnabled(false);
    }

    private void setOutputFromPreset(File file) {

        Preset selectedPreset = (Preset) presetComboBox.getSelectedItem();
        if (selectedPreset == null) return;

        String baseName = file.getAbsolutePath()
                .replaceAll("\\.[^.]+$", "");

        String newOutput = baseName + selectedPreset.getOutputExtension();

        boolean overwritePrevented = false;

        if (newOutput.equals(file.getAbsolutePath())) {
            newOutput = baseName + "_" +
                    selectedPreset.getName().replaceAll("\\s+", "_")
                    + selectedPreset.getOutputExtension();
            overwritePrevented = true;
        }

        outputField.setText(newOutput);

        if (overwritePrevented) {
            flashOutputField();
            showStatusMessage("Output filename adjusted to prevent overwrite");
        }

    }


    private void flashOutputField() {

        Color originalColor = outputField.getBackground();
        outputField.setBackground(new Color(255, 255, 180)); // light yellow

        javax.swing.Timer timer = new javax.swing.Timer(800, e -> {
            outputField.setBackground(originalColor);
        });

        timer.setRepeats(false);
        timer.start();
    }

    private void showStatusMessage(String message) {

        statusLabel.setText(message);

        if (statusTimer != null && statusTimer.isRunning()) {
            statusTimer.stop();
        }

        statusTimer = new javax.swing.Timer(4000, e -> {
            statusLabel.setText(" ");
        });

        statusTimer.setRepeats(false);
        statusTimer.start();
    }

    private void initializeConfig() {

        try {
            String home = System.getProperty("user.home");
            java.io.File dir = new java.io.File(home, ".leonardo");

            if (!dir.exists()) {
                dir.mkdir();
            }

            configFile = new java.io.File(dir, "config.properties");

            config = new java.util.Properties();

            if (configFile.exists()) {
                try (java.io.FileInputStream fis = new java.io.FileInputStream(configFile)) {
                    config.load(fis);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        applySavedTheme();
    }

    private void saveConfig() {

        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(configFile)) {
            config.store(fos, "Leonardo Configuration");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void restoreLastPreset() {

        String lastPreset = config.getProperty("lastPreset");
        if (lastPreset == null) return;

        for (int i = 0; i < presetComboBox.getItemCount(); i++) {
            if (presetComboBox.getItemAt(i).getName().equals(lastPreset)) {
                presetComboBox.setSelectedIndex(i);
                break;
            }
        }
    }

    private void applySavedTheme() {

        String theme = config.getProperty("theme", "system");

        try {

            switch (theme) {
                case "dark":
                    FlatDarkLaf.setup();
                    break;

                case "light":
                    FlatLightLaf.setup();
                    break;

                default:
                    // System Look and Feel
                    javax.swing.UIManager.setLookAndFeel(
                            javax.swing.UIManager.getSystemLookAndFeelClassName()
                    );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void changeTheme(String theme) {

        config.setProperty("theme", theme);
        saveConfig();

        try {

            switch (theme) {
                case "dark":
                    FlatDarkLaf.setup();
                    break;

                case "light":
                    FlatLightLaf.setup();
                    break;

                default:
                    javax.swing.UIManager.setLookAndFeel(
                            javax.swing.UIManager.getSystemLookAndFeelClassName()
                    );
            }

            SwingUtilities.updateComponentTreeUI(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isFFmpegAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-version");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Give it a short timeout so it never hangs
            if (!process.waitFor(2, java.util.concurrent.TimeUnit.SECONDS)) {
                process.destroy();
                return false;
            }

            return process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }


    private void checkFFmpegOnStartup() {

        if (!isFFmpegAvailable()) {

        	JOptionPane.showMessageDialog(
        	        this,
        	        "FFmpeg was not found on your system.\n\n" +
        	        "Leonardo requires FFmpeg to convert media files.\n\n" +
        	        "Install FFmpeg using your package manager:\n" +
        	        "Arch / Garuda: sudo pacman -S ffmpeg\n" +
        	        "Ubuntu / Debian: sudo apt install ffmpeg\n" +
        	        "Fedora: sudo dnf install ffmpeg\n\n" +
        	        "Then restart Leonardo.",
        	        "FFmpeg Not Found",
        	        JOptionPane.WARNING_MESSAGE
        	);


            // Disable conversion functionality
            convertButton.setEnabled(false);
            convertButton.setToolTipText("Disabled: FFmpeg is not installed");

            // Optional: Show persistent status message
            statusLabel.setText("FFmpeg not detected — conversion disabled");
            
            
        }
    }

    private void showUserGuide() {
        openHelpPage("index.html");
    }

    private void openHelpPage(String page) {
        String resourcePath = "/com/ross/leonardo/resources/help/" + page;

        try {
            URL resource = getClass().getResource(resourcePath);
            if (resource == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Help file not found:\n" + resourcePath,
                        "User Guide",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            // If running unpackaged and it’s a normal file URL, open directly
            if ("file".equalsIgnoreCase(resource.getProtocol())) {
                Desktop.getDesktop().browse(resource.toURI());
                return;
            }

            // If running from JAR/jpackage (jar: URL), extract to temp file first
            try (java.io.InputStream in = getClass().getResourceAsStream(resourcePath)) {
                if (in == null) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Unable to read help resource:\n" + resourcePath,
                            "User Guide",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                java.nio.file.Path tempDir = java.nio.file.Files.createTempDirectory("leonardo-help-");
                java.nio.file.Path outFile = tempDir.resolve(page);

                // Copy the requested page
                java.nio.file.Files.copy(in, outFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                // Optional: also copy the other help pages so links work when clicked
                copyHelpResourceTo(tempDir, "modes.html");
                copyHelpResourceTo(tempDir, "davinci.html");
                copyHelpResourceTo(tempDir, "obs-remux.html");
                copyHelpResourceTo(tempDir, "youtube-h264.html");
                copyHelpResourceTo(tempDir, "tiktok-vertical.html");
                copyHelpResourceTo(tempDir, "troubleshooting.html");
                copyHelpResourceTo(tempDir, "faq.html");

                // Open the extracted file
                Desktop.getDesktop().browse(outFile.toUri());
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to open the User Guide.",
                    "User Guide",
                    JOptionPane.ERROR_MESSAGE
            );
            ex.printStackTrace();
        }
    }

    private void copyHelpResourceTo(java.nio.file.Path tempDir, String filename) {
        String p = "/com/ross/leonardo/resources/help/" + filename;

        try (java.io.InputStream in = getClass().getResourceAsStream(p)) {
            if (in == null) return;
            java.nio.file.Path out = tempDir.resolve(filename);
            java.nio.file.Files.copy(in, out, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ignored) {
            // If a page is missing, don't break help completely
        }
    }

    private void openWebsite(String url) {

        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to open browser.\nPlease visit:\n" + url,
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void setAppIcon() {
        try {
            URL iconURL = getClass().getResource("/com/ross/leonardo/resources/leo_icon.png");
            if (iconURL == null) {
                System.out.println("Icon not found: /com/ross/leonardo/resources/leo_icon.png");
                return;
            }

            ImageIcon base = new ImageIcon(iconURL);
            Image img = base.getImage();

            java.util.List<Image> icons = new java.util.ArrayList<>();
            int[] sizes = {16, 32, 48, 64, 128, 256};

            for (int s : sizes) {
                icons.add(img.getScaledInstance(s, s, Image.SCALE_SMOOTH));
            }

            setIconImages(icons);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
