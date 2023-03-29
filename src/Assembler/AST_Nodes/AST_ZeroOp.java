package Assembler.AST_Nodes;

import Assembler.OpCode;

import java.util.ArrayList;
import java.util.List;

public class AST_ZeroOp extends Command {

    public AST_ZeroOp(OpCode op, int line, int address, int beg, int end) {
        super(op, line, address, beg, end);
    }

    @Override
    public byte[] generateMachineCode() {
        byte opCode = getOpCode();
        return new byte[]{opCode};
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public List<Operand> getOperands() {
        return new ArrayList<>();
    }
}
