package Assembler.AST_Nodes;

import java.util.List;

public interface Operand {

    public abstract byte[] generateMachineCode();

    public abstract int size();
}
