package Assembler.AST_Nodes;

public interface Operand {

    public abstract byte[] generateMachineCode();

    public abstract int size();
}
