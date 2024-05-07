package ru.deewend.cjava.exporter;

import ru.deewend.cjava.CompiledCode;
import ru.deewend.cjava.Helper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class WinI386 implements Exporter {
    @Override
    public void export(CompiledCode instructions, ByteBuffer buffer) {
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // DOS header
        Helper.putString(buffer, "MZ");
        Helper.writeNullUntil(buffer, 0x30);
        buffer.putInt(0); // padding
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.putInt(0x40); // offset of PE header

        Helper.writeNullUntil(buffer, 0x40);

        // PE header
        // PE, 0, 0
        Helper.putString(buffer, "PE");
        buffer.put((byte) 0);
        buffer.put((byte) 0);
        buffer.putShort((short) 0x14C); // Intel 386
        buffer.putShort((short) 3); // 3 sections in total
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.putShort((short) 0xE0);
        buffer.putShort((short) 0x102); // 32bit EXE

        Helper.writeNullUntil(buffer, 0x58);

        // Optional header
        buffer.putShort((short) 0x10B); // 32 bit
        buffer.putShort((short) 0); // padding
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.putInt(0x1000); // RVA of entry point
        buffer.putInt(0); // padding
        buffer.putInt(0);
        buffer.putInt(0x400000); // ImageBase
        buffer.putInt(0x1000); // offset where sections should start
        buffer.putInt(0x200); // offset where sections start in the file
        buffer.putInt(0); // padding
        buffer.putInt(0);
        buffer.putShort((short) 4); // minimal Windows version is NT4
        buffer.putShort((short) 0); // padding
        buffer.putInt(0);
        buffer.putInt(0x4000); // size of image
        buffer.putInt(0x200); // size of headers
        buffer.putInt(0);
        buffer.putShort((short) 2); // GUI
        buffer.putShort((short) 0); // padding
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.putInt(16); // count of data directories

        // Data directories
        buffer.putInt(0); // padding
        buffer.putInt(0);
        buffer.putInt(0x2000); // ImportsVA
        buffer.putInt(0); // padding
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.putInt(0);

        Helper.writeNullUntil(buffer, 0x138);

        Helper.putString(buffer, ".text");
        buffer.put((byte) 0); // padding, to make the string contain 8 bytes in total
        buffer.put((byte) 0);
        buffer.put((byte) 0);
        buffer.putInt(0x1000); // VirtualSize
        buffer.putInt(0x1000); // VirtualAddress
        buffer.putInt(0x200); // size of raw data
        buffer.putInt(0x200); // pointer to raw data
        buffer.putInt(0); // padding
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.putInt(0x60000020); // CODE_EXECUTE_READ

        Helper.putString(buffer, ".rdata");
        buffer.put((byte) 0); // padding
        buffer.put((byte) 0);
        buffer.putInt(0x1000); // VirtualSize
        buffer.putInt(0x2000); // VirtualAddress
        buffer.putInt(0x200); // size of raw data
        buffer.putInt(0x400); // pointer to raw data
        buffer.putInt(0); // padding
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.putInt(0x40000040); // INITIALIZED_READ

        Helper.putString(buffer, ".data");
        buffer.put((byte) 0); // padding, to make the string contain 8 bytes in total
        buffer.put((byte) 0);
        buffer.put((byte) 0);
        buffer.putInt(0x1000); // VirtualSize
        buffer.putInt(0x3000); // VirtualAddress
        buffer.putInt(0x200); // size of raw data
        buffer.putInt(0x600); // pointer to raw data
        buffer.putInt(0); // padding
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.putInt(0xC0000040); // DATA_READ_WRITE

        Helper.writeNullUntil(buffer, 0x200);

        // x86 assembly
        buffer.put((byte) 0x6A); // push
        buffer.put((byte) 0); // pushing 0
        buffer.put((byte) 0x68); // push address
        buffer.putInt(0x403000);
        buffer.put((byte) 0x68);
        buffer.putInt(0x403017);
        buffer.put((byte) 0x6A);
        buffer.put((byte) 0);
        buffer.putShort((short) 0x15FF); // call
        buffer.putInt(0x402070); // function address
        buffer.put((byte) 0x6A);
        buffer.put((byte) 0); // exit code (pushing)
        buffer.putShort((short) 0x15FF); // call
        buffer.putInt(0x402068); // function address

        Helper.writeNullUntil(buffer, 0x400);

        // imports
        buffer.putInt(0x203C);
        buffer.putInt(0); // padding
        buffer.putInt(0);
        buffer.putInt(0x2078);
        buffer.putInt(0x2068);

        buffer.putInt(0x2044);
        buffer.putInt(0); // padding
        buffer.putInt(0);
        buffer.putInt(0x2085);
        buffer.putInt(0x2070);

        buffer.putInt(0);
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.putInt(0);
        buffer.putInt(0);

        buffer.putLong(0x204C);
        buffer.putLong(0x205A);
        Helper.putString(buffer, "\0\0ExitProcess\0");
        Helper.putString(buffer, "\0\0MessageBoxA\0");
        buffer.putLong(0x204C);
        buffer.putLong(0x205A);
        Helper.putString(buffer, "kernel32.dll\0");
        Helper.putString(buffer, "user32.dll\0");

        // strings
        Helper.writeNullUntil(buffer, 0x600);
        Helper.putString(buffer, "a simple PE executable\0");
        Helper.putString(buffer, "Hello world!\0");
    }
}
