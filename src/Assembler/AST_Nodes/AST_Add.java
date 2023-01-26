package Assembler.AST_Nodes;

import Assembler.OpCode;

//TODO: Currently we'll only support Integers, maybe later we'll make the Command Generic?
public class AST_Add extends Command {

    Operand a1, a2, a3;
    boolean three = false;

    public AST_Add(OpCode op, int row, int address, int beg, int end, Operand a1, Operand a2) {
        super(op, row, address, beg, end);
        this.a1 = a1;
        this.a2 = a2;
    }

    public AST_Add(OpCode op, int row, int address, int beg, int end, Operand a1, Operand a2, Operand a3) {
        this(op, row, address, beg, end, a1, a2);
        this.a3 = a3;
        three = (a3 != null);
    }

    @Override
    public byte getOpCode() {
        return (byte)0xC4; //TODO Generate the right OpCode here
    }

    @Override
    public byte[] generateMachineCode() {
        int size = 1 + a1.size() + a2.size() + (three ? a3.size() : 0);
        byte[] result = new byte[size];

        ImmediateOperand im = (ImmediateOperand) a1;

        int i = 0;
        result[i++] = getOpCode();
        for (byte b : a1.generateMachineCode()) result[i++] = b;
        for (byte b : a2.generateMachineCode()) result[i++] = b;
        if (three) for(byte b : a3.generateMachineCode()) result[i++] = b;

        return result;
    }
}
