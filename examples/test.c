#include "dummy.h"

#pragma cjava subsystem gui
#pragma cjava minNTVersion 4
#pragma cjava uses("MessageBoxA", "user32.dll")

// just for demonstration purposes, currently this feature without variables isn't really useful
#pragma cjava bundleFile("./myImage.png", 0)

int main() {
    MessageBoxA(0, "Hello World!", "CJava test", 0);
}
