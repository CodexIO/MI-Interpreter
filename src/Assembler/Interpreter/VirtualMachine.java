package Assembler.Interpreter;

import java.util.ArrayList;

public class VirtualMachine {

    // Konstanten für die Virtuelle Maschine
    public static final int MEMORY_LENGTH = 1048576; // 1MByte Speicher
    public static final int SP_REGISTER = 14; // Stackpointer
    public static final int PC_REGISTER = 15; // Programcounter
    public static final int NUMBER_OF_REGISTERS = 16;
    public static final int WORD_SIZE = 4;

    // Condition Codes / Flags
    private boolean C, Z, N, V;

    private final Byte[] memory = new Byte[MEMORY_LENGTH];
    private final ArrayList<Integer> changedMemory = new ArrayList<>();

    private final int[] registers = new int[NUMBER_OF_REGISTERS];
    private final boolean[] registersChanged = new boolean[NUMBER_OF_REGISTERS];

    private boolean programHaltet;

    public VirtualMachine(int begin, Byte[] content) {
        //TODO: Handle bad input

        for (int i = 0;  i < content.length; i++) {
            int address = begin + i;
            memory[address] = content[i];
            changedMemory.add(address);
        }
    }

    public VirtualMachine(Byte[] content) {
        this(0, content);
    }

    //region Getter and Setter
    private int getPC() {
        return registers[PC_REGISTER];
    }

    private void setPC(int value) {
        registers[PC_REGISTER] = value;
    }

    private void addOneToPC() {
        registers[PC_REGISTER] += 1;
    }

    private int getSP() {
        return registers[SP_REGISTER];
    }

    private void setSP(int value) {
        registers[SP_REGISTER] = value;
    }

    private Byte getNextByte() {
        Byte result = memory[registers[PC_REGISTER]];
        addOneToPC();
        return result;
    }
    //endregion

    // Executes one instruction
    public void step() {
        Byte opcode = getNextByte();

        //opcode & 0xFF makes the Byte unsigned
        switch(opcode & 0xFF) {
            case 0x00: halt();
            case 0x92: cmp_b();
            case 0x93: cmp_h();
        }
    }

    public void run() {
        //TODO: Do we assume that a halt is at the end of the Program
        // and simply run until we find it or do we have another way
        // to know that we are finished?
        while (!programHaltet) {
            step();
        }
    }

    public void halt() {

        programHaltet = true;
    }

    public void cmp_b() {

    }

    public void cmp_h() {

    }

}
