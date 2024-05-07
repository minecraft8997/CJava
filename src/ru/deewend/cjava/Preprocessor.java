package ru.deewend.cjava;

import java.lang.reflect.Method;

public class Preprocessor {
    private static final Preprocessor INSTANCE = new Preprocessor();

    private String nextLineRequestedBy;

    private Preprocessor() {
    }

    public static Preprocessor getInstance() {
        return INSTANCE;
    }

    /**
     * @param line The line
     * @return true if the compiler should consider this line didn't exist
     */
    public boolean handleLine(String line) throws Exception {
        Method method = null;
        line = Helper.removeBlankCharsFromStart(line);
        if (line.startsWith("#")) {
            line = line.substring(1);
            line = Helper.removeBlankCharsFromStart(line);

            String directive = Helper.readUntilBlankChar(line);
            if (!Helper.validateToken(directive)) {
                Helper.crash("Bad directive: " + directive);
            }
            char firstCharUppercase = Character.toUpperCase(directive.charAt(0));
            String newDirective = firstCharUppercase + directive.substring(1);

            line = Helper.removeBlankCharsFromStart(line);

            method = getClass().getDeclaredMethod("do" + newDirective, String.class);
        } else if (nextLineRequestedBy != null) {
            method = getClass().getDeclaredMethod(nextLineRequestedBy, String.class);
            nextLineRequestedBy = null;
        }
        if (method != null) {
            method.setAccessible(true);

            return (Boolean) method.invoke(this, line);
        }

        return false;
    }

    public boolean doInclude(String line) {
        return false;
    }

    public boolean doDefine(String line) {
        return false;
    }

    public boolean doUndef(String line) {
        return false;
    }

    public boolean doIf(String line) {
        Helper.crash("Directive #if is not currently supported");

        return false;
    }

    public boolean doIfdef(String line) {
        return false;
    }

    public boolean doIfndef(String line) {
        return false;
    }

    public boolean doError(String line) {
        Helper.crash(line);

        return false;
    }
}
