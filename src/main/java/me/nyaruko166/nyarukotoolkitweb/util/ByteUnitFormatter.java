package me.nyaruko166.nyarukotoolkitweb.util;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ByteUnitFormatter {

    private static final int THRESHOLD = 1024;

    private static final String[] UNITS = new String[]{"B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};

    public String format(double size) {
        size = size < 0 ? 0 : size;
        int u;
        for (u = 0; u < UNITS.length - 1 && size >= THRESHOLD; ++u) {
            size /= 1024;
        }
        return String.format("%.0f %s", size, UNITS[u]);
    }

}
