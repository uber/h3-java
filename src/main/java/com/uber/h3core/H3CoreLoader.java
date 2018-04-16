/*
 * Copyright 2017-2018 Uber Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.uber.h3core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Extracts the native H3 core library to the local filesystem and loads it.
 */
final class H3CoreLoader {
    H3CoreLoader() {
        // Prevent instantiation
    }

    private static volatile File libraryFile = null;

    /**
     * Read all bytes from <code>in</code> and write them to <code>out</code>.
     */
    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[4096];

        int read;
        while ((read = in.read(buf)) != -1) {
            out.write(buf, 0, read);
        }
    }

    /**
     * Copy the resource at the given path to the file. File will be made readable,
     * writable, and executable.
     *
     * @param resourcePath Resource to copy
     * @param newH3LibFile File to write
     */
    private static void copyResource(String resourcePath, File newH3LibFile) throws IOException {
        // Set the permissions
        newH3LibFile.setReadable(true);
        newH3LibFile.setWritable(true, true);
        newH3LibFile.setExecutable(true, true);

        // Shove the resource into the file and close it
        try (InputStream resource = H3CoreLoader.class.getResourceAsStream(resourcePath)) {
            try (FileOutputStream outFile = new FileOutputStream(newH3LibFile)) {
                copyStream(resource, outFile);
            }
        }
    }

    /**
     * For use when the H3 library should be unpacked from the JAR and loaded.
     *
     * @throws SecurityException Loading the library was not allowed by the
     *                           SecurityManager.
     * @throws UnsatisfiedLinkError The library could not be loaded
     * @throws IOException Failed to unpack the library
     */
    public synchronized static NativeMethods loadNatives() throws IOException {
        // This is synchronized because if multiple threads were writing and
        // loading the shared object at the same time, bad things could happen.

        if (libraryFile == null) {
            OperatingSystem os = detectOs();
            String arch = detectArch();

            String dirName = String.format("%s-%s", os.getDirName(), arch);
            String libName = String.format("libh3-java%s", os.getSuffix());

            File newLibraryFile = File.createTempFile("libh3-java", os.getSuffix());

            newLibraryFile.deleteOnExit();

            copyResource(String.format("/%s/%s", dirName, libName), newLibraryFile);

            libraryFile = newLibraryFile;
        }

        System.load(libraryFile.getCanonicalPath());

        return new NativeMethods();
    }

    /**
     * For use when the H3 library is installed system-wide and Java is able to locate it.
     *
     * @throws SecurityException Loading the library was not allowed by the
     *                           SecurityManager.
     * @throws UnsatisfiedLinkError The library could not be loaded
     */
    public static NativeMethods loadSystemNatives() {
        System.loadLibrary("h3-java");

        return new NativeMethods();
    }

    /**
     * Operating systems supported by H3-Java.
     */
    private enum OperatingSystem {
        ANDROID(".so"),
        DARWIN(".dylib"),
        WINDOWS(".dll"),
        LINUX(".so");

        private final String suffix;

        OperatingSystem(String suffix) {
            this.suffix = suffix;
        }

        /**
         * Suffix for native libraries.
         */
        public String getSuffix() {
            return suffix;
        }

        /**
         * How this operating system's name is rendered when extracting the native library.
         */
        public String getDirName() {
            return name().toLowerCase();
        }
    }

    /**
     * Detect the current operating system.
     */
    private static final OperatingSystem detectOs() {
        // Detecting Android using the properties from:
        // https://developer.android.com/reference/java/lang/System.html
        if (System.getProperty("java.vendor").toLowerCase().contains("android")) {
            return OperatingSystem.ANDROID;
        }

        String javaOs = System.getProperty("os.name").toLowerCase();
        if (javaOs.contains("mac")) {
            return OperatingSystem.DARWIN;
        } else if (javaOs.contains("win")) {
            return OperatingSystem.WINDOWS;
        } else {
            // Only other supported platform
            return OperatingSystem.LINUX;
        }
    }

    /**
     * Detect the system architecture.
     */
    private static final String detectArch() {
        String javaArch = System.getProperty("os.arch");
        if (javaArch.equals("amd64") || javaArch.equals("x86_64")) {
            return "x64";
        } else {
            return javaArch;
        }
    }
}