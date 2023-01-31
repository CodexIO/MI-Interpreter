package Assembler.AST_Nodes;

public class AbsoluteAddress implements Operand {

    public int address;

    public AbsoluteAddress(int address) {
        this.address = address;
    }

    @Override
    public byte[] generateMachineCode() {
        byte b1 = (byte) (address >>> 24);
        byte b2 = (byte) (address >>> 16);
        byte b3 = (byte) (address >>> 8);
        byte b4 = (byte) (address & 0xFF);

        return new byte[]{ (byte) 0x9F, b1, b2, b3, b4};
    }

    @Override
    public int size() {
        return 5;
    }
}
