package Assembler.AST_Nodes;

import Assembler.OpCode;

public class ImmediateOperand implements Operand {

    int number;
    OpCode.DataType size;

    public ImmediateOperand(int n, OpCode.DataType s) {
        number = n;
        size = s;
    }

    public ImmediateOperand(float n, OpCode.DataType s) {
        this(Float.floatToIntBits(n), s);
    }

    @Override
    public byte[] generateMachineCode() {
        if (number >= 0 && number <= 63) return new byte[]{ (byte) number };

        byte op = (byte) 0x8F;
        byte b1 = (byte) (number >>> 24);
        byte b2 = (byte) (number >>> 16);
        byte b3 = (byte) (number >>> 8);
        byte b4 = (byte) (number & 0xFF);

        return switch (size) {
            case BYTE -> new byte[]{ op, b4};
            case HALFWORD -> new byte[]{ op, b3, b4};
            case WORD, FLOAT -> new byte[]{op, b1, b2, b3, b4};

            //TODO: Implement Double
            case DOUBLE, NONE -> throw new UnsupportedOperationException();
        };
    }

    @Override
    public int size() {
        if (number >= 0 && number <= 63) return 1;
        return switch(size) {
            case BYTE -> 2;
            case HALFWORD -> 3;
            case WORD, FLOAT -> 5;
            case DOUBLE -> 9;
            default -> -1;
        };
    }
}
