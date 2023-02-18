package Assembler.AST_Nodes;

import Assembler.OpCode;

public class AST_ZeroOp extends Command {

    public AST_ZeroOp(OpCode op, int line, int address, int beg, int end) {
        super(op, line, address, beg, end);
    }

    @Override
    public byte[] generateMachineCode() {
        byte opCode = getOpCode();
        return new byte[]{opCode};
    }
}