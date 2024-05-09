package ru.deewend.cjava;

import ru.deewend.cjava.exporter.Exporter;
import ru.deewend.cjava.exporter.WinI386;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

public class CJava {
    public static final int CONTEXT_EXPECTING_SOURCE_FILE = 1;
    public static final int CONTEXT_EXPECTING_DEFINES = 2;
    public static final int CONTEXT_EXPECTING_EXPORTER = 3;
    public static final int CONTEXT_EXPECTING_BUFFER_CAPACITY = 4;
    public static final int CONTEXT_EXPECTING_OUTPUT_FILE = 5;

    final Set<String> defines;
    private final InputStream sourceStream;
    List<String> sourceLines;
    TokenizedCode tokenizedLines;
    int idx;
    private final boolean debugPreprocessingResult;

    public CJava(Set<String> defines, InputStream sourceStream, boolean debugPreprocessingResult) {
        this.defines = new HashSet<>(defines);
        this.sourceStream = sourceStream;
        this.debugPreprocessingResult = debugPreprocessingResult;
    }

    @SuppressWarnings("IOStreamConstructor")
    public static void main(String[] args) {
        String sourceFile = null;
        Set<String> defines = new HashSet<>();
        String exporter = "WinI386";
        int bufferCapacity = 65536;
        String outputFile = null;
        int context = 0;
        for (int i = 0; i < args.length; i++) {
            String argument = args[i];
            boolean shouldClearContextLater = (context != 0 && context != CONTEXT_EXPECTING_DEFINES);
            switch (context) {
                case CONTEXT_EXPECTING_SOURCE_FILE: {
                    sourceFile = argument;

                    break;
                }
                case CONTEXT_EXPECTING_DEFINES: {
                    if (argument.startsWith("-")) {
                        context = 0;
                        i--;

                        break;
                    }
                    defines.add(argument);

                    break;
                }
                case CONTEXT_EXPECTING_EXPORTER: {
                    exporter = argument;

                    break;
                }
                case CONTEXT_EXPECTING_BUFFER_CAPACITY: {
                    bufferCapacity = Integer.parseInt(argument);

                    break;
                }
                case CONTEXT_EXPECTING_OUTPUT_FILE: {
                    outputFile = argument;

                    break;
                }
                default: {
                    if (argument.equalsIgnoreCase("-s")) {
                        context = CONTEXT_EXPECTING_SOURCE_FILE;
                    } else if (argument.equalsIgnoreCase("-d")) {
                        context = CONTEXT_EXPECTING_DEFINES;
                    } else if (argument.equalsIgnoreCase("-e")) {
                        context = CONTEXT_EXPECTING_EXPORTER;
                    } else if (argument.equalsIgnoreCase("-bc")) {
                        context = CONTEXT_EXPECTING_BUFFER_CAPACITY;
                    } else if (argument.equalsIgnoreCase("-o")) {
                        context = CONTEXT_EXPECTING_OUTPUT_FILE;
                    } else {
                        System.err.println("Unrecognized option (at i=" + i + "): " + argument);
                    }

                    break;
                }
            }
            if (shouldClearContextLater) context = 0;
        }
        boolean hasIssues = false;
        if (sourceFile == null) {
            System.err.println("Source file is not specified");

            hasIssues = true;
        }
        if (Helper.validateToken(exporter)) {
            System.err.println("Bad exporter name");

            hasIssues = true;
        }
        if (outputFile == null) {
            System.err.println("Output file is not specified");

            hasIssues = true;
        }
        if (hasIssues) {
            System.err.println("Usage: java -jar CJava.jar " +
                    "-s <sourceFile> [-d [define1] [define2] ...] [-e <exporter>] [-bc <bufferCapacity>] -o <outputFile>");

            System.exit(-1);
        }
        System.out.println("Source file: " + sourceFile);
        System.out.println("Defines (might be listed in a different order): " + String.join(" ", defines));
        System.out.println("Exporter: " + exporter);
        System.out.println("Buffer capacity: " + bufferCapacity + " bytes");
        System.out.println("Output file: " + outputFile);

        Exporter exporterObj;
        try {
            Class<?> clazz = Class.forName("ru.deewend.cjava.exporter." + exporter);
            exporterObj = (Exporter) clazz.newInstance();
        } catch (ClassNotFoundException e) {
            System.err.println("Unknown exporter");
            System.exit(-1);

            return;
        } catch (Exception e) {
            System.err.println("Failed to instantiate the exporter");
            System.exit(-1);

            return;
        }

        CJava compiler;
        try (InputStream stream = new FileInputStream(sourceFile)) {
            compiler = new CJava(defines, stream, true);
            compiler.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        compiler.preprocess();

        CompiledCode compiledCode = compiler.compile();
        exporterObj.load(compiledCode);

        ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);
        exporterObj.export(buffer);

        int position = buffer.position();
        try (OutputStream stream = new FileOutputStream(outputFile)) {
            buffer.flip();
            stream.write(buffer.array(), 0, position);
            for (; position < 16384; position++) stream.write(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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

                    System.out.println("CJava will put an address here");
                }
            }
        }
    }

    public CompiledCode compile() {
        return null;
    }
}
