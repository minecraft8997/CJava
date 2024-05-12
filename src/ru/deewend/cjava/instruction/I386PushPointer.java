package ru.deewend.cjava.instruction;

import ru.deewend.cjava.exporter.Exporter;

import java.nio.ByteBuffer;

public class I386PushPointer implements Instruction {
    private final int address;

    public I386PushPointer(Exporter exporter, Integer address) {
        this.address = address;
    }

    @Override
    public void encode(ByteBuffer buffer) {
        buffer.put((byte) 0x68);
        buffer.putInt(address);
    }
}
