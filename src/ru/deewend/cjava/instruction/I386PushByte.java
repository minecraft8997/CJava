package ru.deewend.cjava.instruction;

import java.nio.ByteBuffer;

public class I386PushByte implements Instruction {
    private final byte value;

    private I386PushByte(byte value) {
        this.value = value;
    }

    public static I386PushByte of(byte value) {
        return new I386PushByte(value);
    }

    @Override
    public void encode(ByteBuffer buffer) {
        buffer.put((byte) 0x6A);
        buffer.put(value);
    }
}
