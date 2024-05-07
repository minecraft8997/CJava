package ru.deewend.cjava.instruction;

import java.nio.ByteBuffer;

public interface Instruction {
    void encode(ByteBuffer buffer);
}
