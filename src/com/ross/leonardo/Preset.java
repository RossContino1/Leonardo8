/*
 * Leonardo - Media Conversion Tool
 * Copyright (c) 2026 Ross Contino
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */




package com.ross.leonardo;

import java.util.List;

public class Preset {

    private final String name;
    private final String outputExtension;
    private final List<String> ffmpegArgs;

    public Preset(String name, String outputExtension, List<String> ffmpegArgs) {
        this.name = name;
        this.outputExtension = outputExtension;
        this.ffmpegArgs = ffmpegArgs;
    }

    public String getName() {
        return name;
    }

    public String getOutputExtension() {
        return outputExtension;
    }

    public List<String> getFfmpegArgs() {
        return ffmpegArgs;
    }

    @Override
    public String toString() {
        return name;
    }
}
