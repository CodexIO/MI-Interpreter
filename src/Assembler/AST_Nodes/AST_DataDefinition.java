package Assembler.AST_Nodes;

import Assembler.OpCode;

import java.util.ArrayList;
import java.util.List;

public class AST_DataDefinition extends Command {

    List<Byte> bytes;
    List<String> labelsToPatch;

    public AST_DataDefinition(OpCode op, int line, int address, int beg, int end, List<Byte> bytes) {
        super(op, line, address, beg, end);
        this.bytes = bytes;
    }

    public AST_DataDefinition(OpCode op, int line, int address, int beg, int end, List<Byte> bytes, List<String> labelsToPatch) {
        this(op, line, address, beg, end, bytes);
        this.labelsToPatch = labelsToPatch;
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
