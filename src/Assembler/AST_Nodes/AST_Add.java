package Assembler.AST_Nodes;

import Assembler.OpCode;

//TODO: Currently we'll only support Integers, maybe later we'll make the Command Generic?
public class AST_Add extends Command {

    Operand a1, a2, a3;
    boolean three = false;

    public AST_Add(OpCode op, int line, int address, int beg, int end, Operand a1, Operand a2) {
        super(op, line, address, beg, end);
        this.a1 = a1;
        this.a2 = a2;
    }

    public AST_Add(OpCode op, int line, int address, int beg, int end, Operand a1, Operand a2, Operand a3) {
        this(op, line, address, beg, end, a1, a2);
        this.a3 = a3;
        three = true;
    }

    @Override
    public byte getOpCode() {
        return 0;
    }

    @Override
    public byte[] generateMachineCode() {
        int size = 1 + a1.size() + a2.size() + (three ? a3.size() : 0);
        byte[] result = new byte[size];

        int i = 0;
        result[i++] = getOpCode();
        for (byte b : a1.generateMachineCode()) result[i++] = b;
        for (byte b : a2.generateMachineCode()) result[i++] = b;
        if (three) for(byte b : a3.generateMachineCode()) result[i++] = b;

        return result;
    }
}
