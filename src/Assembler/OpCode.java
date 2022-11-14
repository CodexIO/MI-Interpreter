package Assembler;

import static Assembler.OpCode.DataType.*;

public class OpCode {
    public String name;
    public int opcode;
    public int operands;
    public DataType type;
    public int length; //in byte

    public OpCode(String n, int opC, int o, DataType t, int l) {
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

    public static OpCode HALT = new OpCode("HALT", 0x00, 0, NONE, 1);
    public static OpCode REI = new  OpCode("REI", 0x01, 0, NONE, 1);
}
