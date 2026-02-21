/*
 * Leonardo - Media Conversion Tool
 * Copyright (c) 2026 Ross Contino
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */



package com.ross.leonardo;

import javax.swing.*;
import java.io.*;
import java.util.*;

public class VideoConverter extends SwingWorker<Void, Integer> {

    private final String input;
    private final String output;
    private final JProgressBar progressBar;
    private final JButton convertButton;
    private final JFrame parent;
    private Exception conversionError = null;
    private Process ffmpegProcess;



    public VideoConverter(String input, String output,
                          JProgressBar progressBar,
                          JButton convertButton,
                          JFrame parent) {

        this.input = input;
        this.output = output;
        this.progressBar = progressBar;
        this.convertButton = convertButton;
        this.parent = parent;
    }

    @Override
    protected Void doInBackground() {

        try {

            double duration = FFmpegUtil.getDurationSeconds(input);

            java.util.List<String> command = new java.util.ArrayList<>();
            command.add("ffmpeg");
            command.add("-i");
            command.add(input);

            if (input.toLowerCase().endsWith(".mp4")) {
                command.add("-vcodec"); command.add("mjpeg");
                command.add("-q:v"); command.add("2");
                command.add("-acodec"); command.add("pcm_s16be");
                command.add("-q:a"); command.add("0");
                command.add("-f"); command.add("mov");
            } else {
                command.add("-c:v"); command.add("libx264");
                command.add("-c:a"); command.add("aac");
            }

            command.add(output);

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);

            ffmpegProcess = pb.start();
            Process process = ffmpegProcess;


            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;

            while (!isCancelled() && (line = reader.readLine()) != null) {


                if (line.contains("time=")) {
                    double current = FFmpegUtil.extractTimeInSeconds(line);
                    int percent = (int)((current / duration) * 100);
                    publish(Math.min(percent, 100));
                }
            }

            process.waitFor();

        } catch (Exception e) {
            conversionError = e;
        }

        return null;
    }

    @Override
    protected void process(List<Integer> chunks) {
        int value = chunks.get(chunks.size() - 1);
        progressBar.setValue(value);
    }

    @Override
    protected void done() {

        convertButton.setEnabled(true);

        // Re-enable preset & reset cancel in MainWindow
        if (parent instanceof MainWindow) {
            ((MainWindow) parent).conversionFinished();
        }

        if (isCancelled()) {

            if (ffmpegProcess != null) {
                ffmpegProcess.destroyForcibly();
            }

            progressBar.setValue(0);

            JOptionPane.showMessageDialog(parent,
                    "Conversion Cancelled.",
                    "Leonardo 10",
                    JOptionPane.INFORMATION_MESSAGE);

            return;
        }

        if (conversionError != null) {
            JOptionPane.showMessageDialog(parent,
                    "Conversion failed:\n" + conversionError.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        progressBar.setValue(100);

        JOptionPane.showMessageDialog(parent,
                "Conversion Complete!",
                "Leonardo 10",
                JOptionPane.INFORMATION_MESSAGE);
    }

}
