package ru.deewend.cjava.instruction;

import ru.deewend.cjava.exporter.Exporter;

import java.nio.ByteBuffer;

public class I386PushByte implements Instruction {
    private final byte value;

    public I386PushByte(Exporter exporter, Integer value) {
        this.value = value.byteValue();
    }

    @Override
    public void encode(ByteBuffer buffer) {
        buffer.put((byte) 0x6A);
        buffer.put(value);
    }
}
