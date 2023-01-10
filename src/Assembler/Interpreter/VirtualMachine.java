package Assembler.Interpreter;

import Assembler.OpCode;

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

    private final byte[] memory = new byte[MEMORY_LENGTH];
    private final ArrayList<Integer> changedMemory = new ArrayList<>();

    public final int[] registers = new int[NUMBER_OF_REGISTERS]; //TODO: Make registers private again with proper testing
    private final boolean[] registersChanged = new boolean[NUMBER_OF_REGISTERS];

    private boolean programHaltet;

    public static final byte[] TEST1 = {(byte)0xC4, 0x50, 0x51, 0x52, (byte)0xBF, 0x52, 0x50};

    public VirtualMachine(int begin, byte[] content) {
        //TODO: Handle bad input

        for (int i = 0;  i < content.length; i++) {
            int address = begin + i;
            memory[address] = content[i];
            changedMemory.add(address);
        }
    }

    public VirtualMachine(byte[] content) {
        this(0, content);
    }

    //region Getter and Setter
    private int getPC() {
        return registers[PC_REGISTER];
    }

    private void setPC(int value) {
        registers[PC_REGISTER] = value;
    }

    private void decPC() {
        registers[PC_REGISTER] -= 1;
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

    private byte getNextByte() {
        byte result = memory[getPC()];
        addOneToPC();
        return result;
    }

    private int getNextOperand(int OperandSize) {
        byte b = getNextByte();

        int reg = (b & 0x0F);
        int addressType = (b & 0xFF) >> 4;
        if (addressType < 4) addressType = 0; // We do this so we only have to use one case statement for every direct Operand Case

        int result = 0;

        switch (addressType) {
            // Direct Operand between 0 and 63
            case 0: {
                result = (b & 0x3F);
            } break;
            // Stack Addressing by -!Rx
            case 7: {
                registers[reg] -= OperandSize;
                result = memory[registers[reg]];
            } break;
            case 8: {
                // Direct Operand bigger than 63
                if (reg == 15) {
                    for (int i = 0; i < OperandSize; i++) {
                        byte op = getNextByte();
                        result = (result << 4) + (op & 0xFF);
                    }
                }
                // Stack Addressing by !Rx+
                else {
                    result = memory[registers[reg]];
                    registers[reg] += OperandSize;
                }
            }
            // Absolute Addressing
            case 9: {
                assert((b & 0x0F) == 15); //Not sure why the rest of the byte has to be 15

                int address = 0;
                for (int i = 0; i < OperandSize; i++) {
                    byte op = getNextByte();
                    address = (address << 4) + (op & 0xFF);
                }
                result = memory[address];
            } break;
            // Register Addressing
            case 5: {
                result = registers[reg];
            } break;
            //TODO: Other cases
        }

        return result;
    }

    private void saveResult(int result) {
        //TODO: For now only handling Registers
        byte b = getNextByte();
        int addressType = (b & 0xFF) >> 4;

        switch (addressType) {
            case 5: {
                int reg = (b & 0x0F);
                registers[reg] = result;
            } break;
            //TODO: Other cases
        }
    }

    //endregion

    public void executeOneInstruction() {
        byte opcode = getNextByte();

        //opcode & 0xFF makes the Byte unsigned
        switch(opcode & 0xFF) {
            case 0x00: halt(); break;
            case 0x92: cmp_b(); break;
            case 0x93: cmp_h(); break;

            case 0xBF: add_b2(); break;
            case OpCode.ADD_B3_Code: add_b3(); break;
        }
    }

    public void run() {
        //TODO: Do we assume that a halt is at the end of the Program
        // and simply run until we find it or do we have another way
        // to know that we are finished?
        while (!programHaltet) {
            executeOneInstruction();
        }
    }

    public void halt() {

        programHaltet = true;
    }

    public void cmp_b() {

    }

    public void cmp_h() {

    }

    public void add_b2() {
        int a1 = getNextOperand(1);
        int a2 = getNextOperand(1);
        int result = a1 + a2;

        decPC();
        saveResult(result);
    }

    public void add_b3() {
        //TODO: Zum testen gehen wir jetzt kurzzeitig von Registern aus

        int a1 = getNextOperand(1);
        int a2 = getNextOperand(1);
        int result = a1 + a2;

        saveResult(result);
    }

}
