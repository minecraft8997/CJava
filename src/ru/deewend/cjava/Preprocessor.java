package ru.deewend.cjava;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Preprocessor {
    private static final Preprocessor INSTANCE = new Preprocessor();
    private final Map<Integer, List<Integer>> futureAddresses = new HashMap<>();
    private CJava compiler;

    private String nextLineRequestedBy;

    private Preprocessor() {
    }

    public static Preprocessor getInstance() {
        return INSTANCE;
    }

    public void linkCompiler(CJava compiler) {
        this.compiler = compiler;
        futureAddresses.clear();
    }

    public boolean hasFutureAddresses() {
        return futureAddresses.containsKey(compiler.idx);
    }

    public List<Integer> listFutureAddresses() {
        return new ArrayList<>(futureAddresses.get(compiler.idx));
    }

    /**
     * @return true if the compiler should start analyzing source file again.
     */
    public boolean handleLine() throws Exception {
        Method method = null;
        List<String> tokenizedLine = compiler.tokenizedLines.get(compiler.idx);


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
        String name = Helper.readUntilBlankChar(line);
        line = line.substring(name.length());
        line = Helper.removeBlankCharsFromStart()

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
