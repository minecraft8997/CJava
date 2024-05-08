package ru.deewend.cjava;

import java.util.List;

public class TokenizedCode {
    public interface Detector {
        default boolean detect(String token) {
            if (token.isEmpty()) throw new IllegalArgumentException("Empty token");

            return detect0(token, token.charAt(0));
        }

        boolean detect0(String token, char firstChar);
    }

    public enum TokenType {
        SYMBOL((token, firstChar) -> !Character.isDigit(firstChar) && Character.isLetter(firstChar)),
        LITERAL_INTEGER((token, firstChar) -> Character.isDigit(firstChar)),
        LITERAL_STRING((token, firstChar) -> firstChar == '"'),
        LITERAL_CHARACTER((token, firstChar) -> firstChar == '\''),
        OPERATOR_MATH(((token, firstChar) -> "+-/*".indexOf(firstChar) != -1)),
        OTHER((token, firstChar) -> "#(){},".indexOf(firstChar) != -1),
        STATEMENT_END((token, firstChar) -> firstChar == ';');

        private final Detector detector;

        TokenType(Detector detector) {
            this.detector = detector;
        }

        boolean detect(String token) {
            return detector.detect(token);
        }
    }

    private final List<List<String>> tokenizedLines;
    private int lineIdx;
    private int nextTokenIdx;

    private TokenizedCode(List<List<String>> tokenizedLines) {
        this.tokenizedLines = tokenizedLines;
    }

    public static TokenizedCode of(List<List<String>> tokenizedLines) {
        return new TokenizedCode(tokenizedLines);
    }

    public void switchToLine(int lineIdx) {
        if (lineIdx < 0 || lineIdx >= tokenizedLines.size()) issue("lineIdx out of bounds");

        this.lineIdx = lineIdx;
        nextTokenIdx = 0;
    }

    public boolean hasMoreTokens() {
        return nextTokenIdx < tokenizedLines.get(lineIdx).size();
    }

    public TokenType getNextTokenType() {
        String nextToken = nextToken(false);
        for (TokenType possibleType : TokenType.values()) {
            if (possibleType.detect(nextToken)) return possibleType;
        }
        issue("could not determine the next token type"); // could probably return OTHER instead?

        return null; // unreachable statement
    }

    public String nextToken() {
        return nextToken(true);
    }

    public String nextToken(boolean moveCursor) {
        List<String> tokens = tokenizedLines.get(lineIdx);
        if (nextTokenIdx >= tokens.size()) issue("nextTokenIdx out of bounds");

        String result = tokens.get(nextTokenIdx);
        if (moveCursor) nextTokenIdx++;

        return result;
    }

    private void issue(String message) {
        throw new IllegalArgumentException("Line " + (lineIdx + 1) + ": " + message);
    }
}
