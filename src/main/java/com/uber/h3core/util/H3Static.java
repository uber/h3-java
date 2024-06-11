package com.uber.h3core.util;

import java.io.File;
import java.util.Locale;

public class H3Static {

    private H3Static() {
    }

    // These constants are from h3api.h and h3Index.h
    /** Maximum number of vertices for an H3 index */
    public static final int MAX_CELL_BNDRY_VERTS = 10;

    public static final int NUM_BASE_CELLS = 122;
    public static final int NUM_PENTAGONS = 12;

    // Constants for the resolution bits in an H3 index.
    public static final long H3_RES_OFFSET = 52L;
    public static final long H3_RES_MASK = 0xfL << H3_RES_OFFSET;
    public static final long H3_RES_MASK_NEGATIVE = ~H3_RES_MASK;
    /**
     * Mask for the indexing digits in an H3 index.
     *
     * <p>The digits are offset by 0, so no shift is needed in the constant.
     */
    public static final long H3_DIGIT_MASK = 0x1fffffffffffL;

    public static final long INVALID_INDEX = 0L;

    /**
     * Locale used when handling system strings that need to be mapped to resource names. English is
     * used since that locale was used when building the library.
     */
    public static final Locale H3_CORE_LOCALE = Locale.ENGLISH;

    // Supported H3 architectures
    public static final String ARCH_X64 = "x64";
    public static final String ARCH_X86 = "x86";
    public static final String ARCH_ARM64 = "arm64";

    public static volatile File libraryFile = null;


}
