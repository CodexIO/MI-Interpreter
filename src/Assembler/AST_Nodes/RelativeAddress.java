package Assembler.AST_Nodes;

public class RelativeAddress implements Operand {

    private int offset;
    private int reg;

    public RelativeAddress(int o, int r) {
        offset = o;
        reg = r;
    }

    @Override
    public byte[] generateMachineCode() {
        return new byte[0];
    }

    @Override
    public int size() {
        if (offset == 0) return 1;
        //TODO: @KICKOFF CONTINUE THE WORK HERE
        return 0;
    }
}
