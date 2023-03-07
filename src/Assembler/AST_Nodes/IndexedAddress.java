package Assembler.AST_Nodes;

public class IndexedAddress implements Operand {

    private final int regY;
    private final Operand relativeOrIndirectAddress;

    public IndexedAddress(Operand relOrIndAddress, int r) {
        regY = r;
        relativeOrIndirectAddress = relOrIndAddress;
    }

    @Override
    public byte[] generateMachineCode() {
        byte[] code = new byte[size()];
        byte[] relAddressCode = relativeOrIndirectAddress.generateMachineCode();
        code[0] = (byte) (0x40 + regY);

        System.arraycopy(relAddressCode, 0, code, 1, size() - 1);
        return code;
    }

    @Override
    public int size() {
        return relativeOrIndirectAddress.size() + 1;
    }
}
