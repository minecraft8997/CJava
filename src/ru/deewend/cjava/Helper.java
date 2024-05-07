package ru.deewend.cjava;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Helper {
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

    public static void putString(ByteBuffer buffer, String str) {
        buffer.put(str.getBytes(StandardCharsets.US_ASCII));
    }

    public static void writeNullUntil(ByteBuffer buffer, int offset) {
        int written = buffer.position();
        for (int i = 0; i < offset - written; i++) buffer.put((byte) 0x00);
    }
}
