package Assembler.AST_Nodes;

public class StackAddress implements Operand {

    private int reg;
    private boolean plus;

    public StackAddress(int reg, boolean plus) {
        assert(reg >= 0 && reg <= 15);
        this.reg = reg;
        this.plus = plus;
    }

    public int getReg() {
        return reg;
    }

    @Override
    public byte[] generateMachineCode() {
        byte b = plus ? (byte) (0x80 + reg) : (byte) (0x70 + reg);

        return new byte[]{b};
    }

    @Override
    public int size() {
        return 1;
    }
}
