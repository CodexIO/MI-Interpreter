package Assembler.AST_Nodes;

public class RegisterAddress implements Operand {

    private int reg;

    public RegisterAddress(int r) {
        assert(r >= 0 && r <= 15);
        reg = r;
    }

    public int getReg() {
        return reg;
    }

    @Override
    public byte[] generateMachineCode() {
        byte b = (byte) (0x50 + reg);

        return new byte[]{b};
    }

    @Override
    public int size() {
        return 1;
    }
}
