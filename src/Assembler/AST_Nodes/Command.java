package Assembler.AST_Nodes;

import Assembler.OpCode;

public abstract class Command {

    /**
     * Zeile des Quelltextes in der der Befehl steht
     */
    protected int line;

    /**
     * Adresse des Befehls im Speicher
     */
    protected int address;

    /**
     * Zeichenpostition - Beginn des Befehlswortes im Quelltext
     */
    protected int beg;

    /**
     * Zeichenpostition - Ende des Befehlswortes im Quelltext
     */
    protected int end;

    protected OpCode op;

    /**
     * Konstruktor für einen Befehl
     *
     * @param line   Zeile des Quelltextes in der der Befehl steht
     * @param address Adresse des Befehls im Speicher
     * @param beg    Zeichenpostition - Beginn des Befehlswortes im Quelltext
     * @param end    Zeichenpostition - Ende des Befehlswortes im Quelltext
     */
    public Command(OpCode op, int line, int address, int beg, int end) {
        this.op = op;
        this.line = line;
        this.address = address;
        this.beg = beg;
        this.end = end;
    }

    /**
     * Gibt die Adresse des Befehls zurück
     *
     * @return Adresse des Befehls
     */
    public int getAddress() {
        return address;
    }

    public abstract byte getOpCode();
    public abstract byte[] generateMachineCode();
}