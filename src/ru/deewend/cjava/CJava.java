package ru.deewend.cjava;

import java.io.*;
import java.util.*;

public class CJava {
    //private static CJava instance;
    final Set<String> defines;
    private final InputStream sourceStream;
    List<String> sourceLines;
    TokenizedCode tokenizedLines;
    int idx;
    private final boolean debugPreprocessingResult;

    public CJava(Set<String> defines, InputStream sourceStream, boolean debugPreprocessingResult) {
        //instance = this;
        this.defines = new HashSet<>(defines);
        this.sourceStream = sourceStream;
        this.debugPreprocessingResult = debugPreprocessingResult;
    }

    public static void main(String[] args) {
        CJava compiler;
        try (FileInputStream stream = new FileInputStream("source.c")) {
            compiler = new CJava(new HashSet<>(), stream, true);
            compiler.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        compiler.preprocess();
    }

    /*
    public static CJava getInstance() {
        return instance;
    }
     */

    public void load() {
        try {
            // assuming sourceStream will be closed by the caller
            // both BufferedStream and InputStreamReader themselves don't hold any native resources, not closing them
            BufferedReader reader = new BufferedReader(new InputStreamReader(sourceStream));
            String line;
            while ((line = reader.readLine()) != null) sourceLines.add(line);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<List<String>> tokenizedLines = new ArrayList<>();
        for (idx = 0; idx < sourceLines.size(); idx++) {
            tokenizedLines.add(Tokenizer.getInstance().tokenizeLine());
        }
        this.tokenizedLines = TokenizedCode.of(tokenizedLines);

        sourceLines = null;
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public void preprocess() {
        Preprocessor.getInstance().linkCompiler(this);

        while (idx != sourceLines.size()) {
            boolean shouldReset = false;
            try {
                shouldReset = Preprocessor.getInstance().handleLine();
            } catch (Exception e) {
                System.err.println("Preprocessor has thrown an Exception:");
                e.printStackTrace();

                Helper.crash(e.getMessage());
            }
            idx = (shouldReset ? 0 : idx + 1);
        }
        if (debugPreprocessingResult) {
            for (idx = 0; idx < sourceLines.size(); idx++) {
                String line = sourceLines.get(idx);
                System.out.println(line);
                if (Preprocessor.getInstance().hasFutureAddresses()) {
                    // assuming the list is sorted in ascending order
                    List<Integer> futureAddresses = Preprocessor.getInstance().listFutureAddresses();
                    for (int i = 0; i < line.length(); i++) {
                        char c;
                        if (futureAddresses.contains(i)) {
                            c = '^';
                        } else {
                            c = ' ';
                        }
                        System.out.print(c);
                    }
                    System.out.println();
                    int firstIdx = futureAddresses.get(0);
                    for (int i = 0; i < firstIdx; i++) System.out.println(' ');

                    System.out.println("CJava will paste an address here");
                }
            }
        }
    }
}
