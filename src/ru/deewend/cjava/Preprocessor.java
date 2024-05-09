package ru.deewend.cjava;

import java.lang.reflect.Method;
import java.util.*;

public class Preprocessor {
    private static final Preprocessor INSTANCE = new Preprocessor();
    private final Map<Integer, List<Integer>> futureAddresses = new HashMap<>();
    private final Map<String, Object> context = new HashMap<>();
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
        } else {
            context.clear();

            String directive;
            if ((directive = scanDirective(true)) != null) {
                char firstCharUppercase = Character.toUpperCase(directive.charAt(0));
                String newDirective = firstCharUppercase + directive.substring(1);

                method = getClass().getDeclaredMethod("do" + newDirective);
            }
        }
        if (method != null) {
            method.setAccessible(true);

            System.err.println("Invoking " + method.getName() + " " + method.getDeclaringClass());
            return (Boolean) method.invoke(this);
        }

        return false;
    }

    // note that this method may cause TokenizedCode's cursor to move
    private boolean isDirective() {
        return tokenizedCode.hasMoreTokens() && tokenizedCode.nextToken().equals("#");
    }

    private String scanDirective(boolean strict) {
        if (isDirective()) {
            boolean hasIssue = false;
            if (!tokenizedCode.hasMoreTokens() || tokenizedCode.getNextTokenType() != TokenizedCode.TokenType.SYMBOL) {
                if (strict) tokenizedCode.issue("bad directive");
                else        hasIssue = true;
            }

            if (!hasIssue) return tokenizedCode.nextToken();
        }

        return null;
    }

    public boolean doInclude() {
        return false;
    }

    public boolean doPragma() {
        return false;
    }

    @SuppressWarnings("unchecked")
    public boolean doDefine() {
        String name;
        List<String> value;
        if (!context.isEmpty()) {
            name = (String) context.get("name");
            value = null;

            String directive;
            if ((directive = scanDirective(false)) != null && directive.startsWith("if")) {
                nextLineRequestedBy = "doDefine";

                return false;
            }
            while (tokenizedCode.hasMoreTokens()) {
                String nextToken = tokenizedCode.nextToken();
                if (nextToken.equals(name)) {
                    if (value == null) {
                        value = (List<String>) context.get("value");
                    }
                    tokenizedCode.patchToken(value.get(0));
                    for (int j = 1; j < value.size(); j++) tokenizedCode.insertToken(value.get(j));
                }
            }
            if (isLastLine()) {
                System.err.println("LAST LINE, YES!!");
                tokenizedCode.removeLine((int) context.get("idx"));

                return true;
            }
            nextLineRequestedBy = "doDefine";

            return false;
        }
        System.err.println(tokenizedCode.getLine());
        if (tokenizedCode.getNextTokenType() != TokenizedCode.TokenType.SYMBOL) tokenizedCode.issue("bad #define name");
        name = tokenizedCode.nextToken();
        value = collectRemainingTokens(true);
        context.put("name", name);
        context.put("value", value);
        context.put("idx", compiler.idx);

        defines.add(name);

        nextLineRequestedBy = "doDefine";

        return false;
    }

    public boolean doIfdef() {
        return doIfdef(false);
    }

    public boolean doIfdef(boolean not) {
        String mode;
        if (!context.isEmpty()) {
            String token;
            if ((token = scanDirective(false)) != null) {
                int stack = (int) context.get("stack");
                if (token.startsWith("if")) {
                    context.put("stack", (stack + 1));
                } else if (token.equals("endif")) {
                    if (stack == 0) {
                        mode = (String) context.get("mode");
                        int startingWith = (int) context.get("startingWith");
                        //noinspection StringEquality
                        if (mode == "clearIfAndEndIf") { // a small optimization; we don't need equals() here
                            tokenizedCode.removeLine(compiler.idx);
                            tokenizedCode.removeLine(startingWith);
                        } else { // == "clearEverything"
                            for (int i = compiler.idx; i >= startingWith; i--) {
                                tokenizedCode.removeLine(i);
                            }
                        }

                        return true;
                    } else {
                        context.put("stack", (stack - 1));
                        nextLineRequestedBy = "doIfdef";

                        return false;
                    }
                }
            }
            nextLineRequestedBy = "doIfdef";

            return false;
        }
        if (tokenizedCode.getNextTokenType() != TokenizedCode.TokenType.SYMBOL) {
            tokenizedCode.issue("it is not possible a #define with such name exists");
        }
        String name = tokenizedCode.nextToken();
        boolean defined = defines.contains(name);

        if ((not && !defined) || (!not && defined)) {
            mode = "clearIfAndEndIf";
        } else {
            mode = "clearEverything";
        }
        context.put("mode", mode);
        context.put("startingWith", compiler.idx);
        context.put("stack", 0);

        nextLineRequestedBy = "doIfdef";

        return false;
    }

    public boolean doIfndef() {
        return doIfdef(true);
    }

    public boolean doError() {
        Helper.crash(String.join(" ", collectRemainingTokens(false)));

        return false;
    }

    private boolean isLastLine() {
        return compiler.idx == tokenizedCode.linesCount() - 1;
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
