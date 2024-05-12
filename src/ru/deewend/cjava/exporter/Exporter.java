package ru.deewend.cjava.exporter;

import ru.deewend.cjava.Metadata;

import java.nio.ByteBuffer;

public interface Exporter {
    void load(Metadata instructions);
    void export(ByteBuffer stream);
    int mountString(String str);
    void putInstruction(String name, Object parameter);
    int getExternalMethodVirtualAddress(String name);
    int imageSize();
}
