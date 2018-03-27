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
            String suffix = ".so";
            String h3LibName = "libh3-java.so";
            // Switch to Mac dylib if running on OS X
            if (System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0) {
                h3LibName = "libh3-java.dylib";
                suffix = ".dylib";
            }

            File newLibraryFile = File.createTempFile("libh3-java", suffix);

            newLibraryFile.deleteOnExit();

            copyResource("/" + h3LibName, newLibraryFile);

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
}