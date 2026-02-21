/*
 * Leonardo - Media Conversion Tool
 * Copyright (c) 2026 Ross Contino
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */




package com.ross.leonardo;

public final class AppInfo {

    public static final String NAME = "Leonardo";
    public static final String VERSION = System.getProperty("leonardo.version", "DEV");
    public static final String AUTHOR = "Ross Contino";
    public static final String WEBSITE = "https://bytesbreadbbq.com";
    public static final String COPYRIGHT = "© 2026 Ross Contino";



    private AppInfo() {
        // Prevent instantiation
    }
}
