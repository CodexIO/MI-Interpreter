package Assembler.AST_Nodes;

public abstract class Operand {

    public int address;

    public abstract byte[] generateMachineCode();

    public abstract int size();
}
