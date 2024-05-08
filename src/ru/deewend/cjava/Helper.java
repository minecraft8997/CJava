package ru.deewend.cjava;

import ru.deewend.cjava.exporter.Exporter;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Helper {
    private static final Map<String, Exporter> EXPORTER_CACHE = new HashMap<>();

    private Helper() {
    }

    public static String removeBlankCharsFromStart(String str) {
        throw new UnsupportedOperationException();
    }

    public static String removeBlankCharsFromEnd(String str) {
        throw new UnsupportedOperationException();
    }

    public static String readUntilBlankChar(String str) {
        throw new UnsupportedOperationException();
    }

    public static Pair<String, String> readLettersDigits(String str) {
        throw new UnsupportedOperationException();
    }

    public static boolean validateToken(String token) {
        if (token.isEmpty()) {
            return false;
        }
        for (int i = 0; i < token.length(); i++) {
            char current = token.charAt(i);
            if (current == '_') {
                continue; // this is allowed
            }
            if (current >= '0' && current <= '9') {
                continue;
            }
            if ((current >= 'a' && current <= 'z') || (current >= 'A' && current <= 'Z')) {
                continue;
            }

            return false;
        }

        return true;
    }

    public static void crash(String message) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public static Exporter getExporter(String name) {
        Objects.requireNonNull(name);

        if (EXPORTER_CACHE.containsKey(name)) {
            return EXPORTER_CACHE.get(name);
        }

        Exporter instance;
        try {
            Class<?> clazz = Class.forName("ru.deewend.cjava.exporter." + name);
            instance = (Exporter) clazz.newInstance();
        } catch (Exception e) {
            System.err.println("Could not instantiate an Exporter:");
            e.printStackTrace();
            System.err.println("getExporter(\"" + name + "\") will return null");

            return null;
        }
        EXPORTER_CACHE.put(name, instance);

        return instance;
    }

    public static void putString(ByteBuffer buffer, String str) {
        buffer.put(str.getBytes(StandardCharsets.US_ASCII));
    }

    public static void writeNullUntil(ByteBuffer buffer, int offset) {
        int written = buffer.position();
        for (int i = 0; i < offset - written; i++) buffer.put((byte) 0x00);
    }
}
