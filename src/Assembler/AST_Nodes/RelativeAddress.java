package Assembler.AST_Nodes;

public class RelativeAddress implements Operand {

    public int offset;
    private int size;
    public final int regX;
    public int address;

    public String labelName;

    public RelativeAddress(int a, int o, int r) {
        address = a;
        offset = o;
        regX = r;

        computeSize();
    }

    public RelativeAddress(int a, int r, String l) {
        //TODO: Right now when we don't know a label position yet to compute it's relative address,
        //      we call this constructor. But the offset(int o) passed in here is not the right offset
        //      yet, so the size computation could be wrong. FIX THIS.
        address = a;
        offset = 0;
        regX = r;
        labelName = l;
        size = 2;
    }

    private void computeSize() {
        if (offset == 0) size = 1;
        else if (offset <= Byte.MAX_VALUE && offset >= Byte.MIN_VALUE) size = 2;
        else if (offset <= Short.MAX_VALUE && offset >= Short.MIN_VALUE) size = 3;
        else size = 5;
    }

    public void patchLabel(int address) {
        //TODO: Explain this for later me.
        offset += address - (this.address + 1);

        computeSize();
    }

    @Override
    public byte[] generateMachineCode() {
        if (size == 1) {
            byte b = (byte) (0x60 + regX);
            return new byte[]{b};
        }
        else if (size == 2) {
            byte b = (byte) (0xA0 + regX);
            return new byte[]{b, (byte)offset};
        }
        else if (size == 3) {
            byte b = (byte) (0xC0 + regX);
            byte n1 = (byte) ((offset >>> 8) & 0xFF);
            byte n2 = (byte) (offset & 0xFF);
            return new byte[]{b, n1, n2};
        }
        else {
            byte b = (byte) (0xE0 + regX);
            byte n1 = (byte) ((offset >>> 24) & 0xFF);
            byte n2 = (byte) ((offset >>> 16) & 0xFF);
            byte n3 = (byte) ((offset >>> 8) & 0xFF);
            byte n4 = (byte) (offset & 0xFF);
            return new byte[]{b, n1, n2, n3, n4};
        }
    }

    @Override
    public int size() {
        return size;
    }
}
