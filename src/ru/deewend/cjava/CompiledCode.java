package ru.deewend.cjava;

import ru.deewend.cjava.instruction.Instruction;

import java.util.*;

public class CompiledCode {
    private List<Instruction> instructions = new ArrayList<>();
    private boolean finishedConstructingInstructions;
    private Map<LibraryName, Set<ExternalMethod>> imports = new HashMap<>();
    private boolean finishedConstructingImports;
    private Map<String, Integer> stringsSI = new HashMap<>();
    private Map<Integer, String> stringsIS = new HashMap<>();
    private boolean finishedConstructingStrings;
    private int fileCount;
    private int minNTVersion;
    private int subsystem;

    public void addInstruction(Instruction instruction) {
        instructions.add(instruction);
    }

    public void finishConstructingInstructions() {
        instructions = Collections.unmodifiableList(instructions);
        finishedConstructingInstructions = true;
    }

    public List<Instruction> getInstructions() {
        if (!finishedConstructingInstructions) throw new IllegalStateException();

        return instructions;
    }

    public void addImport(String library, String functionName, List<String> parameterTypes) {
        LibraryName libraryName = LibraryName.of(library);
        if (!imports.containsKey(libraryName)) {
            imports.put(libraryName, new HashSet<>());
        }
        imports.get(libraryName).add(ExternalMethod.of(functionName, parameterTypes));
    }

    public void finishConstructingImports() {
        imports = Collections.unmodifiableMap(imports);
        finishedConstructingImports = true;
    }

    public boolean hasFinishedConstructingImports() {
        return finishedConstructingImports;
    }

    public Set<Map.Entry<LibraryName, Set<ExternalMethod>>> importsSet() {
        if (!finishedConstructingImports) throw new IllegalStateException();

        return imports.entrySet();
    }

    public void getStringTemporaryIdx(String str) {

    }

    public void setMinNTVersion(int minNTVersion) {
        this.minNTVersion = minNTVersion;
    }

    public void setSubsystem(int subsystem) {
        this.subsystem = subsystem;
    }

    public int getMinNTVersion() {
        return minNTVersion;
    }

    public int getSubsystem() {
        return subsystem;
    }
}
