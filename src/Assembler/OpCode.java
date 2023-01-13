package Assembler;

import static Assembler.OpCode.DataType.*;

public enum OpCode {
    HALT("HALT", 0x00, 0, NONE),
    MOVE_B("MOVE_B", 0x9E, 2, BYTE),
    ADD_B2("ADD_B2", 0xBF, 2, BYTE),
    ADD_B3("ADD_B3", 0xC4, 3, BYTE),
    SUB_B2("SUB_B2", 0xC9, 2, BYTE);

    public final String name;
    public final int opcode;
    public final int operands;
    public final DataType type;

    public static OpCode find(int code) {
        for (OpCode op : OpCode.values()) {
            if (op.opcode == code) return op;
        }
        //TODO: Throw exception
        return null;
    }

    OpCode(String n, int opC, int o, DataType t) {
        name = n;
        opcode = opC;
        operands = o;
        type = t;
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