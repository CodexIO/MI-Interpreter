package Assembler;

import static Assembler.OpCode.DataType.*;

public enum OpCode {
    HALT("HALT", 0x00, 0, NONE, 1),
    ADD_B2("ADD_B2", 0xBF, 2, BYTE, 3),
    ADD_B3("ADD_B3", 0xC4, 3, BYTE, 4);

    public final String name;
    public final int opcode;
    public final int operands;
    public final DataType type;
    public final int length; //in byte

    public static OpCode find(int code) {
        for (OpCode op : OpCode.values()) {
            if (op.opcode == code) return op;
        }
        //TODO: Throw exception
        return null;
    }

    OpCode(String n, int opC, int o, DataType t, int l) {
        name = n;
        opcode = opC;
        operands = o;
        type = t;
        length = l;
    }

    public enum DataType {
        WORT,
        HALBWORT,
        BYTE,
        FLOAT,
        DOUBLE,
        NONE
        //TODO: CONV hat Byte/Wort als datentyp, müssen wir dies unterstützen?
    }
}