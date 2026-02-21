/*
 * Leonardo - Media Conversion Tool
 * Copyright (c) 2026 Ross Contino
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */




package com.ross.leonardo;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.SwingUtilities;

public class LeonardoApp {

    public static void main(String[] args) {


        // Start GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new MainWindow();
        });
    }


}
