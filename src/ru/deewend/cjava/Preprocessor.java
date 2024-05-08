package ru.deewend.cjava;

import java.lang.reflect.Method;
import java.util.*;

public class Preprocessor {
    public interface LineProcessor {
        void process();
    }

    private static final Preprocessor INSTANCE = new Preprocessor();
    private final Map<Integer, List<Integer>> futureAddresses = new HashMap<>();
    private Set<String> defines;
    private CJava compiler;
    private TokenizedCode tokenizedCode;

    private String nextLineRequestedBy;

    private Preprocessor() {
    }

    public static Preprocessor getInstance() {
        return INSTANCE;
    }

    public void linkCompiler(CJava compiler) {
        this.compiler = compiler;
        tokenizedCode = compiler.tokenizedLines;
        futureAddresses.clear();
        defines = new HashSet<>(compiler.defines);
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
        tokenizedCode.switchToLine(compiler.idx);

        if (nextLineRequestedBy != null) {
            method = getClass().getDeclaredMethod(nextLineRequestedBy);
            nextLineRequestedBy = null;
        } else if (isDirective()) {
            if (tokenizedCode.getNextTokenType() != TokenizedCode.TokenType.SYMBOL) tokenizedCode.issue("bad directive");
            String directive = tokenizedCode.nextToken();

            char firstCharUppercase = Character.toUpperCase(directive.charAt(0));
            String newDirective = firstCharUppercase + directive.substring(1);

            method = getClass().getDeclaredMethod("do" + newDirective);
        }
        if (method != null) {
            method.setAccessible(true);

            return (Boolean) method.invoke(this, tokenizedCode);
        }

        return false;
    }

    // note that this method causes TokenizedCode's cursor to move
    private boolean isDirective() {
        return tokenizedCode.hasMoreTokens() && tokenizedCode.nextToken().equals("#");
    }

    public boolean doInclude() {
        return false;
    }

    public boolean doDefine() {
        if (tokenizedCode.getNextTokenType() != TokenizedCode.TokenType.SYMBOL) tokenizedCode.issue("bad #define name");
        String name = tokenizedCode.nextToken();
        List<String> value = collectRemainingTokens(true);
        goThroughLines(() -> { // TODO rewrite, why would I need a loop when I can just nextLineRequestedBy = "doDefine" or something
            while (tokenizedCode.hasMoreTokens()) {
                String nextToken = tokenizedCode.nextToken();
                if (nextToken.equals(name)) {
                    tokenizedCode.patchToken(value.get(0));
                    for (int j = 1; j < value.size(); j++) tokenizedCode.insertToken(value.get(j));
                }
            }
        });
        tokenizedCode.removeLine(compiler.idx);
        defines.add(name);

        return false;
    }

    public boolean doIfdef() {
        return doIfdef(false);
    }

    public boolean doIfdef(boolean not) {
        if (tokenizedCode.getNextTokenType() != TokenizedCode.TokenType.SYMBOL) {
            tokenizedCode.issue("it is not possible a #define with such name exists");
        }
        String name = tokenizedCode.nextToken();
        boolean defined = defines.contains(name);
        if ((not && !defined) || (!not && defined)) {
            goThroughLines(() -> {

            });
        }

        return false;
    }

    public boolean doIfndef() {
        return doIfdef(true);
    }

    public boolean doError() {
        Helper.crash(String.join(" ", collectRemainingTokens(false)));

        return false;
    }

    private void goThroughLines(LineProcessor processor) {
        for (int i = 1; compiler.idx + i < tokenizedCode.linesCount(); i++) {
            tokenizedCode.switchToLine(compiler.idx + i);
            processor.process();
        }
    }

    private List<String> collectRemainingTokens(boolean isDefine) {
        List<String> value = new ArrayList<>();
        while (tokenizedCode.hasMoreTokens()) {
            String nextToken = tokenizedCode.nextToken();
            if (isDefine && !tokenizedCode.hasMoreTokens() && nextToken.equals("\\")) {
                tokenizedCode.issue("multi-line #defines are not supported");
            }
            value.add(nextToken);
        }
        if (value.isEmpty()) value.add("");

        return value;
    }
}
