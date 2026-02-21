/*
 * Leonardo - Media Conversion Tool
 * Copyright (c) 2026 Ross Contino
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

package com.ross.leonardo;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.io.File;
import java.net.URI;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

public class AboutDialog extends JDialog {

    private static final String THIRD_PARTY_NOTICES_FILE = "THIRD_PARTY_NOTICES.txt";
    private static final String GITHUB_REPO_URL = "https://github.com/RossContino1/Leonardo";

    public AboutDialog(JFrame parent) {
        super(parent, "About " + AppInfo.NAME + " " + AppInfo.VERSION, true);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        // Main content panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // ----- Icon -----
        URL iconURL = getClass().getResource("/com/ross/leonardo/resources/leo_icon.png");
        if (iconURL != null) {
            ImageIcon originalIcon = new ImageIcon(iconURL);
            Image scaledImage = originalIcon.getImage().getScaledInstance(140, 140, Image.SCALE_SMOOTH);

            JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
            imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            mainPanel.add(imageLabel);
            mainPanel.add(Box.createVerticalStrut(12));
        }

        // ----- Title -----
        JLabel title = new JLabel(AppInfo.NAME);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(title);

        JLabel version = new JLabel("Version " + AppInfo.VERSION);
        version.setFont(new Font("SansSerif", Font.PLAIN, 13));
        version.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(version);

        mainPanel.add(Box.createVerticalStrut(15));

        // ----- Body (wrap in scroll pane so buttons never overlap text) -----
        JLabel body = new JLabel(
                "<html><div style='text-align:center;'>" +
                        AppInfo.COPYRIGHT + "<br>" +
                        "Author: " + AppInfo.AUTHOR + "<br>" +
                        "<b>GitHub:</b> RossContino1/Leonardo<br><br>" +

                        "<b>Leonardo — Cross-Platform Media Utility</b><br><br>" +

                        "• DaVinci Resolve MP4 → MOV Compatibility<br>" +
                        "• OBS Recording Remux<br>" +
                        "• YouTube H.264 Export<br>" +
                        "• TikTok Vertical (9:16)<br><br>" +

                        "Open Source (MIT License)<br>" +
                        "Free to Use — Donations Appreciated<br><br>" +

                        "<span style='font-size:10px; color:#666666;'>" +
                        "Powered by FFmpeg (external dependency).<br>" +
                        "FFmpeg must be installed on the system." +
                        "</span>" +
                        "</div></html>"
        );

        body.setHorizontalAlignment(SwingConstants.CENTER);

        JScrollPane bodyScroll = new JScrollPane(body);
        bodyScroll.setBorder(BorderFactory.createEmptyBorder());
        bodyScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        bodyScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        bodyScroll.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Give the scroll area a sensible size so the dialog packs nicely
        bodyScroll.setPreferredSize(new Dimension(460, 240));

        mainPanel.add(bodyScroll);

        add(mainPanel, BorderLayout.CENTER);

        // ----- Buttons -----
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));

        JButton websiteButton = new JButton("Website");
        websiteButton.addActionListener(e -> openWebsite());

        JButton githubButton = new JButton("GitHub");
        githubButton.addActionListener(e -> openGitHub());

        JButton licensesButton = new JButton("Licenses");
        licensesButton.addActionListener(e -> openThirdPartyNotices());

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(websiteButton);
        buttonPanel.add(githubButton);
        buttonPanel.add(licensesButton);
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Pack AFTER everything is added (prevents clipping)
        pack();

        // Optional: enforce a minimum so it looks consistent on different DE/font sizes
        setMinimumSize(new Dimension(520, 520));

        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private void openWebsite() {
        try {
            Desktop.getDesktop().browse(new URI(AppInfo.WEBSITE));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to open website:\n" + AppInfo.WEBSITE,
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void openGitHub() {
        try {
            Desktop.getDesktop().browse(new URI(GITHUB_REPO_URL));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to open GitHub:\n" + GITHUB_REPO_URL,
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void openThirdPartyNotices() {
        File file = new File(THIRD_PARTY_NOTICES_FILE);

        if (file.exists() && file.isFile()) {
            try {
                Desktop.getDesktop().open(file);
                return;
            } catch (Exception ignored) {}
        }

        JOptionPane.showMessageDialog(
                this,
                "Leonardo uses FFmpeg.\n\n" +
                "FFmpeg is licensed under LGPL/GPL depending on build configuration.\n" +
                "Third-party notices should be included in the distribution as:\n" +
                THIRD_PARTY_NOTICES_FILE,
                "Third-Party Licenses",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
