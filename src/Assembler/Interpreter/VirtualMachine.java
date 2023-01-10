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

    public class AddressType {
        public static final int absolutAddress = 9;
        public static final int smallDirectOperand = 0;
        public static final int bigDirectOperand_Or_StackAddressingWithPlus = 8;
        public static final int registerAddressing = 5;
        public static final int relativeAddressingWithZero = 6;
        public static final int relativeAddressingWithByte = 10;
        public static final int relativeAddressingWithHalfword = 12;
        public static final int relativeAddressingWithWord = 14;
        public static final int indicatedRelativeAddressing = 4;
        public static final int indirectAddressingWithByte = 11;
        public static final int indirectAddressingWithHalfword = 13;
        public static final int indirectAddressingWithWord = 15;
        public static final int stackAddressingWithMinus = 7;
    }

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

    private int getNextByte() {
        int result = getByte(getPC());
        addOneToPC();
        return result;
    }

    private int getByte(int address) {
        byte result = memory[address];
        return result & 0xFF;
    }

    private int getNextHalfword() {
        int a = getNextByte();
        a = (a << 4) + getNextByte();
        return a;
    }

    private int getHalfword(int address) {
        int a = getByte(address);
        a = (a << 4) + getByte(address);
        return a;
    }

    private int getNextWord() {
        int a = getNextHalfword();
        a = (a << 8) + getNextHalfword();
        return a;
    }

    private int getWord(int address) {
        int a = getHalfword(address);
        a = (a << 8) + getHalfword(address);
        return a;
    }

    // Only use this for 1, 2 or 4 Bytes
    private int getMemory(int address, int size) {
        assert(size == 1 || size == 2 || size == 4);
        return switch (size) {
            case 1 -> getNextByte();
            case 2 -> getNextHalfword();
            case 4 -> getNextWord();
            default -> -1;
        };
    }

    private int getAddress(int address) {
        return getMemory(address, 4);
    }

    private int getNextOperand(int operandSize) {
        int b = getNextByte();

        int reg = (b & 0x0F);
        int addressType = b >> 4;
        if (addressType < 4) addressType = 0; // We do this so we only have to use one case statement for every direct Operand Case

        int result = 0;

        //TODO: Create enums for the switch case
        switch (addressType) {
            // Direct Operand between 0 and 63
            case AddressType.smallDirectOperand: {
                result = (b & 0x3F);
            } break;
            // Register Addressing
            case AddressType.registerAddressing: {
                result = registers[reg];
            } break;
            // Relative Addressing a + !Rx, where a = 0
            case AddressType.relativeAddressingWithZero: {
                result = memory[registers[reg]];
            } break;
            // Stack Addressing by -!Rx
            case AddressType.stackAddressingWithMinus: {
                registers[reg] -= operandSize;
                result = memory[registers[reg]];
            } break;
            case AddressType.bigDirectOperand_Or_StackAddressingWithPlus: {
                // Direct Operand bigger than 63
                if (reg == 15) {
                    for (int i = 0; i < operandSize; i++) {
                        int op = getNextByte();
                        result = (result << 4) + op;
                    }
                }
                // Stack Addressing by !Rx+
                else {
                    result = memory[registers[reg]];
                    registers[reg] += operandSize;
                } break;
            }
            // Absolute Addressing
            case AddressType.absolutAddress: {
                assert((b & 0x0F) == 15); //Not sure why the rest of the byte has to be 15

                int address = 0;
                for (int i = 0; i < operandSize; i++) {
                    int op = getNextByte();
                    address = (address << 4) + op;
                }
                result = memory[address];
            } break;
            // Relative Addressing a + !Rx, where a fits into one byte
            case AddressType.relativeAddressingWithByte: {
                int a = getNextByte();
                int address = registers[reg] + a;
                result = memory[address];
            } break;
            // Relative Addressing a + !Rx, where a fits into two byte
            case AddressType.relativeAddressingWithHalfword: {
                int a = getNextHalfword();
                int address = registers[reg] + a;
                result = memory[address];
            } break;
            case AddressType.relativeAddressingWithWord: {
                int a = getNextWord();
                int address = registers[reg] + a;
                result = memory[address];
            } break;
            case AddressType.indicatedRelativeAddressing: {
                //TODO: Implement me
            } break;
            case AddressType.indirectAddressingWithByte: {
                int a = getNextByte();
                int address1 = registers[reg] + a;
                int address2 = getAddress(address1);
                result = getMemory(address2, operandSize);
            } break;
            case AddressType.indirectAddressingWithHalfword: {
                int a = getNextHalfword();
                int address1 = registers[reg] + a;
                int address2 = getAddress(address1);
                result = getMemory(address2, operandSize);
            } break;
            case AddressType.indirectAddressingWithWord: {
                int a = getNextWord();
                int address1 = registers[reg] + a;
                int address2 = getAddress(address1);
                result = getMemory(address2, operandSize);
            } break;
            default: assert(false);
        }

        return result;
    }

    private void saveResult(int result) {
        //TODO: For now only handling Registers
        int b = getNextByte();
        int addressType = b >> 4;

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
        int opcode = getNextByte();

        //opcode & 0xFF makes the Byte unsigned
        switch(OpCode.find(opcode)) {
            case HALT: halt(); break;
            //case 0x92: cmp_b(); break;
            //case 0x93: cmp_h(); break;

            case ADD_B2: add_b2(); break;
            case ADD_B3: add_b3(); break;
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
