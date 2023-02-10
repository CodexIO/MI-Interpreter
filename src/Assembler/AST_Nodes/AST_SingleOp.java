package Assembler.AST_Nodes;

import Assembler.OpCode;

public class AST_SingleOp extends Command {

    Operand a1;

    public AST_SingleOp(OpCode op, int row, int address, int beg, int end, Operand a1) {
        super(op, row, address, beg, end);
        this.a1 = a1;
    }

    @Override
    public byte[] generateMachineCode() {
        int size = 1 + a1.size();
        byte[] result = new byte[size];

        int i = 0;
        result[i++] = getOpCode();
        for (byte b : a1.generateMachineCode()) result[i++] = b;

        return result;
    }
}
