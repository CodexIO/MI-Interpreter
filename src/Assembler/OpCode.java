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
    CMP_B("CMP", 0x92, 2, BYTE),
    CMP_H("CMP", 0x93, 2, HALFWORD),
    CMP_W("CMP", 0x94, 2, WORD),
    CMP_F("CMP", 0x95, 2, FLOAT),
    CMP_D("CMP", 0x96, 2, DOUBLE),

    CLEAR_B("CLEAR", 0x99, 1, BYTE),
    CLEAR_H("CLEAR", 0x9A, 1, HALFWORD),
    CLEAR_W("CLEAR", 0x9B, 1, WORD),
    CLEAR_F("CLEAR", 0x9C, 1, FLOAT),
    CLEAR_D("CLEAR", 0x9D, 1, DOUBLE),

    MOVE_B("MOVE", 0x9E, 2, BYTE),
    MOVE_H("MOVE", 0x9F, 2, HALFWORD),
    MOVE_W("MOVE", 0xA0, 2, WORD),
    MOVE_F("MOVE", 0xA1, 2, FLOAT),
    MOVE_D("MOVE", 0xA2, 2, DOUBLE),
    MOVEN_B("MOVEN", 0xA3, 2, BYTE),
    MOVEN_H("MOVEN", 0xA4, 2, HALFWORD),
    MOVEN_W("MOVEN", 0xA5, 2, WORD),
    MOVEN_F("MOVEN", 0xA6, 2, FLOAT),
    MOVEN_D("MOVEN", 0xA7, 2, DOUBLE),
    MOVEC_B("MOVEC", 0xA8, 2, BYTE),
    MOVEC_H("MOVEC", 0xA9, 2, HALFWORD),
    MOVEC_W("MOVEC", 0xAA, 2, WORD),
    MOVEA("MOVEA", 0xAB, 2, WORD),
    //CONV TODO: implement this @Felix macht das negative propagation? Antwort: Im MI-Simulator nachgucken
    OR_B2("OR", 0xAD, 2, BYTE),
    OR_H2("OR", 0xAE, 2, HALFWORD),
    OR_W2("OR", 0xAF, 2, WORD),
    OR_B3("OR", 0xB0, 3, BYTE),
    OR_H3("OR", 0xB1, 3, HALFWORD),
    OR_W3("OR", 0xB2, 3, WORD),

    ANDNOT_B2("ANDNOT", 0xB3, 2, BYTE),
    ANDNOT_H2("ANDNOT", 0xB4, 2, HALFWORD),
    ANDNOT_W2("ANDNOT", 0xB5, 2, WORD),
    ANDNOT_B3("ANDNOT", 0xB6, 3, BYTE),
    ANDNOT_H3("ANDNOT", 0xB7, 3, HALFWORD),
    ANDNOT_W3("ANDNOT", 0xB8, 3, WORD),

    XOR_B2("XOR", 0xB9, 2, BYTE),
    XOR_H2("XOR", 0xBA, 2, HALFWORD),
    XOR_W2("XOR", 0xBB, 2, WORD),
    XOR_B3("XOR", 0xBC, 3, BYTE),
    XOR_H3("XOR", 0xBD, 3, HALFWORD),
    XOR_W3("XOR", 0xBE, 3, WORD),

    ADD_B2("ADD", 0xBF, 2, BYTE),
    ADD_H2("ADD", 0xC0, 2, HALFWORD),
    ADD_W2("ADD", 0xC1, 2, WORD),
    ADD_F2("ADD", 0xC2, 2, FLOAT),
    ADD_D2("ADD", 0xC3, 2, DOUBLE),
    ADD_B3("ADD", 0xC4, 3, BYTE),
    ADD_H3("ADD", 0xC5, 3, HALFWORD),
    ADD_W3("ADD", 0xC6, 3, WORD),
    ADD_F3("ADD", 0xC7, 3, FLOAT),
    ADD_D3("ADD", 0xC8, 3, DOUBLE),

    SUB_B2("SUB", 0xC9, 2, BYTE),
    SUB_H2("SUB", 0xCA, 2, HALFWORD),
    SUB_W2("SUB", 0xCB, 2, WORD),
    SUB_F2("SUB", 0xCC, 2, FLOAT),
    SUB_D2("SUB", 0xCD, 2, DOUBLE),
    SUB_B3("SUB", 0xCE, 3, BYTE),
    SUB_H3("SUB", 0xCF, 3, HALFWORD),
    SUB_W3("SUB", 0xD0, 3, WORD),
    SUB_F3("SUB", 0xD1, 3, FLOAT),
    SUB_D3("SUB", 0xD2, 3, DOUBLE),

    MULT_B2("MULT", 0xD3, 2, BYTE),
    MULT_H2("MULT", 0xD4, 2, HALFWORD),
    MULT_W2("MULT", 0xD5, 2, WORD),
    MULT_F2("MULT", 0xD6, 2, FLOAT),
    MULT_D2("MULT", 0xD7, 2, DOUBLE),
    MULT_B3("MULT", 0xD8, 3, BYTE),
    MULT_H3("MULT", 0xD9, 3, HALFWORD),
    MULT_W3("MULT", 0xDA, 3, WORD),
    MULT_F3("MULT", 0xDB, 3, FLOAT),
    MULT_D3("MULT", 0xDC, 3, DOUBLE),

    DIV_B2("DIV", 0xDD, 2, BYTE),
    DIV_H2("DIV", 0xDE, 2, HALFWORD),
    DIV_W2("DIV", 0xDF, 2, WORD),
    DIV_F2("DIV", 0xE0, 2, FLOAT),
    DIV_D2("DIV", 0xE1, 2, DOUBLE),
    DIV_B3("DIV", 0xE2, 3, BYTE),
    DIV_H3("DIV", 0xE3, 3, HALFWORD),
    DIV_W3("DIV", 0xE4, 3, WORD),
    DIV_F3("DIV", 0xE5, 3, FLOAT),
    DIV_D3("DIV", 0xE6, 3, DOUBLE),
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

    public String fullName() {
        return name + "_" + type.toString().charAt(0) + operands;
    }

    //TODO: @Speed This is slow, maybe implement some kind of HashTable
    public static OpCode getOpCode(String command, OpCode.DataType operandSize, int operandCount) {
        for (OpCode op : OpCode.values()) {
            if (op.name.equals(command) &&
                    op.type == operandSize &&
                    op.operands == operandCount) return op;
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