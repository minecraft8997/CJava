package ru.deewend.cjava.instruction;

import java.nio.ByteBuffer;

public class I386Call implements Instruction {
    private final int address;

    private I386Call(int address) {
        this.address = address;
    }

    public static I386Call of(int address) {
        return new I386Call(address);
    }

    @Override
    public void encode(ByteBuffer buffer) {
        buffer.putShort((short) 0x15FF);
        buffer.putInt(address);
    }
}
