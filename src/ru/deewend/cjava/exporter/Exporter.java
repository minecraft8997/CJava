package ru.deewend.cjava.exporter;

import ru.deewend.cjava.CompiledCode;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface Exporter {
    void export(CompiledCode instructions, ByteBuffer stream) throws IOException;
}
