package Assembler;

import static Assembler.OpCode.DataType.*;

public enum OpCode {
    HALT("HALT", 0x00, 0, NONE),
    //REI
    //SPPCB
    //LPCB
    //SPSB
    //SPSL
    //SPSCBADR
    //SPPCBADR
    //SPIPL
    //SPRKMAP
    //SPMAPEN
    //LSB
    //LSL
    //LSCBADR
    //LPCBADR
    //LIPL
    //LRKMAP
    //LMAPEN
    //RKALARM
    CMP_B("CMP_B", 0x92, 2, BYTE),
    CMP_H("CMP_H", 0x93, 2, HALFWORD),
    CMP_W("CMP_W", 0x94, 2, WORD),
    CMP_F("CMP_F", 0x95, 2, FLOAT),
    CMP_D("CMP_D", 0x96, 2, DOUBLE),

    CLEAR_B("CLEAR_B", 0x99, 1, BYTE),
    CLEAR_H("CLEAR_H", 0x9A, 1, HALFWORD),
    CLEAR_W("CLEAR_W", 0x9B, 1, WORD),
    CLEAR_F("CLEAR_F", 0x9C, 1, FLOAT),
    CLEAR_D("CLEAR_D", 0x9D, 1, DOUBLE),

    MOVE_B("MOVE_B", 0x9E, 2, BYTE),
    MOVE_H("MOVE_H", 0x9F, 2, HALFWORD),
    MOVE_W("MOVE_W", 0xA0, 2, WORD),
    MOVE_F("MOVE_F", 0xA1, 2, FLOAT),
    MOVE_D("MOVE_D", 0xA2, 2, DOUBLE),
    MOVEN_B("MOVEN_B", 0xA3, 2, BYTE),
    MOVEN_H("MOVEN_H", 0xA4, 2, HALFWORD),
    MOVEN_W("MOVEN_W", 0xA5, 2, WORD),
    MOVEN_F("MOVEN_F", 0xA6, 2, FLOAT),
    MOVEN_D("MOVEN_D", 0xA7, 2, DOUBLE),
    MOVEC_B("MOVEC_B", 0xA8, 2, BYTE),
    MOVEC_H("MOVEC_H", 0xA9, 2, HALFWORD),
    MOVEC_W("MOVEC_W", 0xAA, 2, WORD),
    MOVEA("MOVEA", 0xAB, 2, WORD),
    //CONV TODO: implement this @Felix macht das negative propagation? Antwort: Im MI-Simulator nachgucken
    OR_B2("OR_B2", 0xAD, 2, BYTE),
    OR_H2("OR_H2", 0xAE, 2, HALFWORD),
    OR_W2("OR_W2", 0xAF, 2, WORD),
    OR_B3("OR_B3", 0xB0, 3, BYTE),
    OR_H3("OR_H3", 0xB1, 3, HALFWORD),
    OR_W3("OR_W3", 0xB2, 3, WORD),

    ANDNOT_B2("ANDNOT_B2", 0xB3, 2, BYTE),
    ANDNOT_H2("ANDNOT_H2", 0xB4, 2, HALFWORD),
    ANDNOT_W2("ANDNOT_W2", 0xB5, 2, WORD),
    ANDNOT_B3("ANDNOT_B3", 0xB6, 3, BYTE),
    ANDNOT_H3("ANDNOT_H3", 0xB7, 3, HALFWORD),
    ANDNOT_W3("ANDNOT_W3", 0xB8, 3, WORD),

    XOR_B2("XOR_B2", 0xB9, 2, BYTE),
    XOR_H2("XOR_H2", 0xBA, 2, HALFWORD),
    XOR_W2("XOR_W2", 0xBB, 2, WORD),
    XOR_B3("XOR_B3", 0xBC, 3, BYTE),
    XOR_H3("XOR_H3", 0xBD, 3, HALFWORD),
    XOR_W3("XOR_W3", 0xBE, 3, WORD),

    ADD_B2("ADD_B2", 0xBF, 2, BYTE),
    ADD_H2("ADD_H2", 0xC0, 2, HALFWORD),
    ADD_W2("ADD_W2", 0xC1, 2, WORD),
    ADD_F2("ADD_F2", 0xC2, 2, FLOAT),
    ADD_D2("ADD_D2", 0xC3, 2, DOUBLE),
    ADD_B3("ADD_B3", 0xC4, 3, BYTE),
    ADD_H3("ADD_H3", 0xC5, 3, HALFWORD),
    ADD_W3("ADD_W3", 0xC6, 3, WORD),
    ADD_F3("ADD_F3", 0xC7, 3, FLOAT),
    ADD_D3("ADD_D3", 0xC8, 3, DOUBLE),

    SUB_B2("SUB_B2", 0xC9, 2, BYTE),
    SUB_H2("SUB_H2", 0xCA, 2, HALFWORD),
    SUB_W2("SUB_W2", 0xCB, 2, WORD),
    SUB_F2("SUB_F2", 0xCC, 2, FLOAT),
    SUB_D2("SUB_D2", 0xCD, 2, DOUBLE),
    SUB_B3("SUB_B3", 0xCE, 3, BYTE),
    SUB_H3("SUB_H3", 0xCF, 3, HALFWORD),
    SUB_W3("SUB_W3", 0xD0, 3, WORD),
    SUB_F3("SUB_F3", 0xD1, 3, FLOAT),
    SUB_D3("SUB_D3", 0xD2, 3, DOUBLE),

    MULT_B2("MULT_B2", 0xD3, 2, BYTE),
    MULT_H2("MULT_H2", 0xD4, 2, HALFWORD),
    MULT_W2("MULT_W2", 0xD5, 2, WORD),
    MULT_F2("MULT_F2", 0xD6, 2, FLOAT),
    MULT_D2("MULT_D2", 0xD7, 2, DOUBLE),
    MULT_B3("MULT_B3", 0xD8, 3, BYTE),
    MULT_H3("MULT_H3", 0xD9, 3, HALFWORD),
    MULT_W3("MULT_W3", 0xDA, 3, WORD),
    MULT_F3("MULT_F3", 0xDB, 3, FLOAT),
    MULT_D3("MULT_D3", 0xDC, 3, DOUBLE),

    DIV_B2("DIV_B2", 0xDD, 2, BYTE),
    DIV_H2("DIV_H2", 0xDE, 2, HALFWORD),
    DIV_W2("DIV_W2", 0xDF, 2, WORD),
    DIV_F2("DIV_F2", 0xE0, 2, FLOAT),
    DIV_D2("DIV_D2", 0xE1, 2, DOUBLE),
    DIV_B3("DIV_B3", 0xE2, 3, BYTE),
    DIV_H3("DIV_H3", 0xE3, 3, HALFWORD),
    DIV_W3("DIV_W3", 0xE4, 3, WORD),
    DIV_F3("DIV_F3", 0xE5, 3, FLOAT),
    DIV_D3("DIV_D3", 0xE6, 3, DOUBLE),
    //SH
    //ROT
    JEQ("JEQ", 0xE9, 1, WORD),
    JNE("JNE", 0xEA, 1, WORD),
    JGT("JGT", 0xEB, 1, WORD),
    JGE("JGE", 0xEC, 1, WORD),
    JLT("JLT", 0xED, 1, WORD),
    JLE("JLE", 0xEE, 1, WORD),
    JC("JC", 0xEF, 1, WORD),
    JNC("JNC", 0xF0, 1, WORD),

    JUMP("JUMP", 0xF1, 1, WORD),
    CALL("CALL", 0xF2, 1, WORD),
    RET("RET", 0xF3, 0, NONE),
    PUSHR("PUSHR", 0xF4, 0, NONE),
    POPR("POPR", 0xF5, 0, NONE);

    public final String name;
    public final int opcode;
    public final int operands;
    public final DataType type;

    //TODO @Speed make a hashtable or array for this.
    public static OpCode find(int code) {
        for (OpCode op : OpCode.values()) {
            if (op.opcode == code) return op;
        }
        //TODO: Throw exception
        return null;
    }

    OpCode(String n, int opC, int o, DataType t) {
        name = n;
        opcode = (opC & 0xFF);
        operands = o;
        type = t;
    }

    public static OpCode getOpCode(String command, OpCode.DataType operandSize, int operandCount) {
        char dataType = switch (operandSize) {
            case BYTE -> 'B';
            case HALFWORD -> 'H';
            case WORD -> 'W';
            case FLOAT -> 'F';
            case DOUBLE -> 'D';
            case NONE -> '0';
        };
        String name = command + "_" + dataType + operandCount;

        for (OpCode op : OpCode.values()) {
            if (op.name.equals(name)) return op;
        }

        return null;
    }

    public enum DataType {
        WORD,
        HALFWORD,
        BYTE,
        FLOAT,
        DOUBLE,
        NONE
        //TODO: CONV hat Byte/Wort als datentyp, müssen wir dies unterstützen?
    }
}