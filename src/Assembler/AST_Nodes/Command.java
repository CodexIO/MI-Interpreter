package Assembler.AST_Nodes;

import Assembler.OpCode;

import java.util.List;

public abstract class Command {

    /**
     * Zeile des Quelltextes in der der Befehl steht
     */
    public int row;

    /**
     * Adresse des Befehls im Speicher
     */
    public int address;

    /**
     * Zeichenpostition - Beginn des Befehlswortes im Quelltext
     */
    public int beg;

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
        this.row = line;
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

    public int getLineNumber() {
        return row;
    }

    public byte getOpCode() {
        return (byte) op.code;
    }

    public abstract byte[] generateMachineCode();

    public abstract int size();

    public abstract List<Operand> getOperands();
}