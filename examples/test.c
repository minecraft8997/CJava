#include "dummy.h"

#pragma cjava subsystem gui
#pragma cjava minNTVersion 4
#pragma cjava import("MessageBoxA", "user32.dll", byte, pointer, pointer, byte)

// #pragma cjava bundleFile("./myImage.png", 0) // to be implemented

#define HELLO_WORLD "Hello World!"
#ifndef HELLO_WORLD
#ifdef HELLO_WORLD
#error "This should never happen"
#endif
#endif

int main() {
    MessageBoxA(0, HELLO_WORLD, "CJava test", 0);
}
