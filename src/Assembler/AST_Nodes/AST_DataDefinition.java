package Assembler.AST_Nodes;

import Assembler.OpCode;

import java.util.ArrayList;
import java.util.List;

public class AST_DataDefinition extends Command {

    private List<Byte> bytes;
    public List<String> labelsToPatch;

    public AST_DataDefinition(OpCode op, int line, int address, int beg, int end, List<Byte> bytes) {
        super(op, line, address, beg, end);
        this.bytes = bytes;
    }

    public AST_DataDefinition(OpCode op, int line, int address, int beg, int end, List<Byte> bytes, List<String> labelsToPatch) {
        this(op, line, address, beg, end, bytes);
        this.labelsToPatch = labelsToPatch;
    }

    public void patchAddress(int address) {
        int[] addressBytes = new int[]{address >> 24, address >> 16, address >> 8, address};
        int i = 0;
        while (i < bytes.size()) {
            if (bytes.get(i) == (byte)0xDD) break;
            i++;
        }
        for (int j = 0; j < 4; j++) {
            bytes.set(i + j, (byte) addressBytes[j]);
        }
    }

    @Override
    public byte[] generateMachineCode() {
        byte[] bytesArray = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            bytesArray[i] = bytes.get(i);
        }
        return bytesArray;
    }

    @Override
    public List<Operand> getOperands() {
        return new ArrayList<>();
    }
}
