package ru.deewend.cjava.exporter;

import ru.deewend.cjava.CompiledCode;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface Exporter {
    void load(CompiledCode instructions);
    void export(ByteBuffer stream) throws IOException;
    long addressOfFile(int idx);
    long addressOfString(int idx);
}
