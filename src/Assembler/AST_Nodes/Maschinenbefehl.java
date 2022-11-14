package Assembler.AST_Nodes;

import Assembler.OpCode;

public class Maschinenbefehl extends Anweisung{

    public OpCode opCode;


    public Maschinenbefehl(int r, int c) {
        super(r, c);
    }

}
