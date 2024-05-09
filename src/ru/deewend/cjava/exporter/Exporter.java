package ru.deewend.cjava.exporter;

import ru.deewend.cjava.CompiledCode;

import java.nio.ByteBuffer;

public interface Exporter {
    void load(CompiledCode instructions);
    void export(ByteBuffer stream);
    long addressOfFile(int idx);
    long addressOfString(int idx);
}
