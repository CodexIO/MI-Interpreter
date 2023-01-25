package Assembler.AST_Nodes;

import Assembler.OpCode;

public class ImmediateOperand extends Operand {

    int number;
    OpCode.DataType size;

    public ImmediateOperand(int n, OpCode.DataType s) {
        number = n;
        size = s;
    }

    @Override
    public byte[] generateMachineCode() {
        return new byte[0];
    }

    @Override
    public int size() {
        return 0;
    }
}
