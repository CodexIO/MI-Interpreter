package Assembler.AST_Nodes;

public class AbsoluteAddress extends Operand {


    @Override
    public byte[] generateMachineCode() {
        return new byte[0];
    }

    @Override
    public int size() {
        return 0;
    }
}
