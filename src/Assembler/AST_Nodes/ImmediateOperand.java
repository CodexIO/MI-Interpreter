package Assembler.AST_Nodes;

import Assembler.OpCode;

import static Assembler.OpCode.DataType.DOUBLE;
import static Assembler.OpCode.DataType.FLOAT;

public class ImmediateOperand implements Operand {

    long number;
    OpCode.DataType size;

    public ImmediateOperand(long n, OpCode.DataType s) {
        number = n;
        size = s;
    }

    public ImmediateOperand(float n) {
        this(Float.floatToIntBits(n), FLOAT);
    }

    public ImmediateOperand(double n) {
        this(Double.doubleToLongBits(n), DOUBLE);
    }

    @Override
    public byte[] generateMachineCode() {
        if (number >= 0 && number <= 63 && size != FLOAT && size != DOUBLE) return new byte[]{ (byte) number };

        byte op = (byte) 0x8F;
        byte b1 = (byte) (number >>> 7 * 8);
        byte b2 = (byte) (number >>> 6 * 8);
        byte b3 = (byte) (number >>> 5 * 8);
        byte b4 = (byte) (number >>> 4 * 8);
        byte b5 = (byte) (number >>> 3 * 8);
        byte b6 = (byte) (number >>> 2 * 8);
        byte b7 = (byte) (number >>> 8);
        byte b8 = (byte) (number & 0xFF);

        return switch (size) {
            case BYTE -> new byte[]{ op, b8};
            case HALFWORD -> new byte[]{ op, b7, b8};
            case WORD, FLOAT -> new byte[]{op, b5, b6, b7, b8};
            case DOUBLE, NONE -> new byte[]{op, b1, b2, b3, b4, b5, b6, b7, b8};
        };
    }

    @Override
    public int size() {
        if (number >= 0 && number <= 63 && size != FLOAT && size != DOUBLE) return 1;
        return switch(size) {
            case BYTE -> 2;
            case HALFWORD -> 3;
            case WORD, FLOAT -> 5;
            case DOUBLE -> 9;
            default -> -1;
        };
    }
}
