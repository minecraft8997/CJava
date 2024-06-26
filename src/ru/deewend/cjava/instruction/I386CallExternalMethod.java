package ru.deewend.cjava.instruction;

import ru.deewend.cjava.exporter.Exporter;

import java.nio.ByteBuffer;

public class I386CallExternalMethod implements Instruction {
    private final Exporter exporter;
    private final String methodName;

    public I386CallExternalMethod(Exporter exporter, String methodName) {
        this.exporter = exporter;
        this.methodName = methodName;
    }

    @Override
    public void encode(ByteBuffer buffer) {
        buffer.putShort((short) 0x15FF);
        buffer.putInt(exporter.getExternalMethodVirtualAddress(methodName));
    }
}
