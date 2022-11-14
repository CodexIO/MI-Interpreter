package Assembler;

public class OpCode {
    public String name;
    public int opcode;
    public int operands;
    public DataType type;
    public int length;

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
