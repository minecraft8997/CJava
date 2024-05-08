package ru.deewend.cjava;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {
    private static final Tokenizer INSTANCE = new Tokenizer();

    private CJava compiler;
    private String line;
    private String token;
    private int i;

    private Tokenizer() {
    }

    public static Tokenizer getInstance() {
        return INSTANCE;
    }

    public void linkCompiler(CJava compiler) {
        this.compiler = compiler;
    }

    public List<String> tokenizeLine() {
        line = compiler.sourceLines.get(compiler.idx);

        List<String> result = new ArrayList<>();
        while (!line.isEmpty()) {
            // removing blank symbols in the start
            line = line.replaceFirst("^\\s*", "");
            if (line.isEmpty()) continue;

            token = null;
            char firstSymbol = line.charAt(0);
            if (Character.isLetterOrDigit(firstSymbol)) {
                for (i = 1; i < line.length(); i++) {
                    if (!Character.isLetterOrDigit(line.charAt(i))) break;
                }
                i--;
                token();
            } else if (firstSymbol == '"') {
                for (i = 1; i < line.length(); i++) {
                    char currentChar = line.charAt(i);
                    if (currentChar == '"' && (i == 1 || line.charAt(i - 1) != '\\')) {
                        token();

                        break;
                    }
                }
                if (token == null) issue("could not found the end of a string");
            } else if (firstSymbol == '\'') {
                String badEnding = "could not find the end of a character";
                if (line.length() <= 2) issue(badEnding);
                i = 1;
                char nextSymbol = line.charAt(i++);
                if (nextSymbol == '\\') {
                    if (line.length() == 3) issue(badEnding);
                    i++;
                }
                char closingSymbol = line.charAt(i);
                if (closingSymbol != '\'') issue(badEnding);

                token();
            } else if ("#(){}+-/*,;".indexOf(firstSymbol) != -1) {
                i = 0;
                token(); // instead of token = String.valueOf(firstSymbol); line = line.substring(1);
            } else {
                issue("Found a token that starts with '" + firstSymbol + "': could not parse it");
            }
            result.add(token);
        }
        line = null;
        token = null;

        return result;
    }

    private void token() {
        token = line.substring(0, i + 1);
        line = line.substring(i + 1);
    }

    private void issue(String message) {
        throw new IllegalArgumentException("Line " + (compiler.idx + 1) + ": " + message);
    }
}
