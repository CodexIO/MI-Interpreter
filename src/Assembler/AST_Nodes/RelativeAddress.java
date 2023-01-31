package Assembler.AST_Nodes;

public class RelativeAddress implements Operand {

    private int offset;
    private int reg;
    private int size;

    public RelativeAddress(int o, int r) {
        offset = o;
        reg = r;

        if (offset == 0) size = 1;
        else if (offset <= Byte.MAX_VALUE && offset >= Byte.MIN_VALUE) size = 2;
        else if (offset <= Short.MAX_VALUE && offset >= Short.MIN_VALUE) size = 3;
        else size = 5;
    }

    @Override
    public byte[] generateMachineCode() {
        if (size == 1) {
            byte b = (byte) (0x60 + reg);
            return new byte[]{b};
        }
        else if (size == 2) {
            byte b = (byte) (0xA0 + reg);
            return new byte[]{b, (byte)offset};
        }
        else if (size == 3) {
            byte b = (byte) (0xC0 + reg);
            byte n1 = (byte) (offset & 0xFF);
            byte n2 = (byte) ((offset >>> 8) & 0xFF);
            return new byte[]{b, n1, n2};
        }
        else {
            byte b = (byte) (0xE0 + reg);
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
