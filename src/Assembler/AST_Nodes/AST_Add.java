package Assembler.AST_Nodes;

import Assembler.OpCode;

import java.util.ArrayList;
import java.util.List;

//TODO: Currently we'll only support Integers, maybe later we'll make the Command Generic?
//TODO: We'll also use this class for every command at the moment,
//TODO: because there doesn'T seem to be an obvious difference while generating code.

public class AST_Add extends Command {

    Operand a1;
    Operand a2;
    Operand a3;
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
    public byte[] generateMachineCode() {
        int size = size();
        byte[] result = new byte[size];

        int i = 0;
        result[i++] = getOpCode();
        for (byte b : a1.generateMachineCode()) result[i++] = b;
        for (byte b : a2.generateMachineCode()) result[i++] = b;
        if (three) for(byte b : a3.generateMachineCode()) result[i++] = b;

        return result;
    }

    @Override
    public int size() {
        return 1 + a1.size() + a2.size() + (three ? a3.size() : 0);
    }

    @Override
    public List<Operand> getOperands() {
        List<Operand> operands = new ArrayList<>();
        operands.add(a1);
        operands.add(a2);
        if (a3 != null) operands.add(a3);

        return operands;
    }
}
