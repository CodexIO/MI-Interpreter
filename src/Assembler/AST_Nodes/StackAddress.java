package Assembler.AST_Nodes;

public class StackAddress implements Operand {

    private int reg;
    private boolean plus;

    public StackAddress(int r, boolean plus) {
        assert(r >= 0 && r <= 15);
        reg = r;
    }

    public int getReg() {
        return reg;
    }

    @Override
    public byte[] generateMachineCode() {
        byte b = plus ? (byte) (0x70 + reg) : (byte) (0x80 + reg);

        return new byte[]{b};
    }

    @Override
    public int size() {
        return 1;
    }
}
