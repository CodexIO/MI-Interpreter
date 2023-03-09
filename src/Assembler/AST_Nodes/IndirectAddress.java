package Assembler.AST_Nodes;

public class IndirectAddress implements Operand {

    private final int offset;
    private final int regX;
    private final int size;

    public IndirectAddress(RelativeAddress relativeAddress) {
        offset = relativeAddress.offset;
        regX = relativeAddress.regX;

        int relSize = relativeAddress.size();
        // This is due to the fact that indirect Addressing always saves the offset, no matter if it's zero or not
        size = (relSize == 1) ? 2 : relSize;
    }

    @Override
    public byte[] generateMachineCode() {
        if (size == 1 || size == 2) {
            byte b = (byte) (0xB0 + regX);
            return new byte[]{b, (byte)offset};
        }
        else if (size == 3) {
            byte b = (byte) (0xD0 + regX);
            byte n1 = (byte) (offset & 0xFF);
            byte n2 = (byte) ((offset >>> 8) & 0xFF);
            return new byte[]{b, n1, n2};
        }
        else {
            byte b = (byte) (0xF0 + regX);
            byte n1 = (byte) (offset & 0xFF);
            byte n2 = (byte) ((offset >>> 8) & 0xFF);
            byte n3 = (byte) ((offset >>> 16) & 0xFF);
            byte n4 = (byte) ((offset >>> 24) & 0xFF);
            return new byte[]{b, n1, n2, n3, n4};
        }
    }

    @Override
    public int size() {
        return size;
    }
}
