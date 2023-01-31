package Assembler.Interpreter;

import Assembler.OpCode;

import java.util.ArrayList;
import java.util.Arrays;

public class VirtualMachine {

    // Konstanten für die Virtuelle Maschine
    public static final int MEMORY_LENGTH = 1048576; // 1MByte Speicher
    public static final int SP_REGISTER = 14; // Stackpointer
    public static final int PC_REGISTER = 15; // Programcounter
    public static final int NUMBER_OF_REGISTERS = 16;
    public static final int BYTE_SIZE = 1;
    public static final int HALFWORD_SIZE = 2;
    public static final int WORD_SIZE = 4;
    public static final int FLOAT_SIZE = 4;
    public static final int DOUBLE_SIZE = 8;

    public static class AddressType {
        public static final int ABSOLUT_ADDRESS = 9;
        public static final int SMALL_DIRECT_OPERAND = 0;
        public static final int BIG_DIRECT_OPERAND_OR_STACK_ADDRESSING_WITH_PLUS = 8;
        public static final int REGISTER_ADDRESSING = 5;
        public static final int RELATIVE_ADDRESSING_WITH_ZERO = 6;
        public static final int RELATIVE_ADDRESSING_WITH_BYTE = 10;
        public static final int RELATIVE_ADDRESSING_WITH_HALFWORD = 12;
        public static final int RELATIVE_ADDRESSING_WITH_WORD = 14;
        public static final int INDICATED_RELATIVE_ADDRESSING = 4;
        public static final int INDIRECT_ADDRESSING_WITH_BYTE = 11;
        public static final int INDIRECT_ADDRESSING_WITH_HALFWORD = 13;
        public static final int INDIRECT_ADDRESSING_WITH_WORD = 15;
        public static final int STACK_ADDRESSING_WITH_MINUS = 7;

        private AddressType() {}
    }

    public enum Operation {
        OR, ANDNOT, XOR, ADD, SUB, MULT, DIV
    }

    // Condition Codes / Flags
    private boolean C, Z, N, V;

    public final byte[] memory = new byte[MEMORY_LENGTH];
    private final ArrayList<Integer> changedMemory = new ArrayList<>();

    public final int[] registers = new int[NUMBER_OF_REGISTERS]; //TODO: Make registers private again with proper testing
    private final boolean[] registersChanged = new boolean[NUMBER_OF_REGISTERS];

    private boolean programHaltet;

    public VirtualMachine(int begin, byte[] memory) {
        //TODO: Handle bad input

        for (int i = 0;  i < memory.length; i++) {
            int address = begin + i;
            this.memory[address] = memory[i];
            changedMemory.add(address);
        }
    }

    public VirtualMachine(byte[] memory) {
        this(0, memory);
    }

    public VirtualMachine(byte[] memory, int[] registers) {
        this(memory);
        System.arraycopy(registers, 0, this.registers, 0, registers.length);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int lastNonZeroByteIndex = memory.length -1;
        while (memory[lastNonZeroByteIndex] == 0) lastNonZeroByteIndex--;
        for (int i = 0; i <= lastNonZeroByteIndex; i++) {
            if (i % 8 == 0) {
                sb.append("\n|");
                sb.append(String.format("%010X", i));
                sb.append("| ");
            }
            sb.append(String.format("%02X", memory[i] & 0xFF));
            sb.append(" ");
        }
        return "VirtualMachine {\n" +
                "  C=" + C +
                ", Z=" + Z +
                ", N=" + N +
                ", V=" + V + "\n" +
                "  memory: {" + sb.toString() + "\n}\n" +
                "  registers=" + Arrays.toString(registers) + "\n" +
                '}';
    }

    private void checkSize(int size) {
        assert(size == BYTE_SIZE || size == HALFWORD_SIZE || size == WORD_SIZE);
    }

    //region Getter and Setter
    public int getPC(int size) {
        return getRegister(PC_REGISTER, size);
    }

    public void setPC(int value, int size) {
        setRegister(PC_REGISTER, size, value);
    }

    private void decPC() {
        registers[PC_REGISTER] -= 1;
    }

    private void incPC() {
        registers[PC_REGISTER] += 1;
    }

    private int getSP(int size) {
        return getRegister(SP_REGISTER, size);
    }

    private void setSP(int value, int size) {
        setRegister(SP_REGISTER, size, value);
    }

    public void setV(int result, int size) {
        //TODO: @Felix fragen wie genau das gemeint ist.
    }

    public void setZ(int result, int size) {
        result = normaliseResult(result, size);
        Z = (result == 0);
    }

    public void setN(int result, int size) {
        N = ((result & (0x80 << size * 8)) != 0);
    }

    public int normaliseResult(int result, int size) {
        return result & (0xFFFF_FFFF >>> (4 - size) * 8);
    }

    private int getNextByte() {
        int result = getByte(getPC(WORD_SIZE));
        incPC();
        return result;
    }

    private int getByte(int address) {
        byte result = memory[address];
        return (result & 0xFF);
    }

    private void setByte(int address, int number) {
        memory[address] = (byte) (number & 0x000000FF);
    }

    private int getMask(int size) {
        checkSize(size);
        return switch(size) {
            case 1 -> 0xFF;
            case 2 -> 0xFFFF;
            case 4 -> 0xFFFF_FFFF;
            default -> 0;
        };
    }

    private int getRegister(int reg, int size) {
        int mask = getMask(size);
        return registers[reg] & mask;
    }

    private int getRegister(int reg) {
        return getRegister(reg, WORD_SIZE);
    }

    private void setRegister(int reg, int size, int value) {
        checkSize(size);
        int mask = getMask(size);
        registers[reg] = value & mask;
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

    private void setHalfword(int address, int number) {
        setByte(address++, number);
        setByte(address, number >>> 4);
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

    private void setWord(int address, int number) {
        setHalfword(address, number);
        setHalfword(address, number >>> 8);
    }

    // Only use this for 1, 2 or 4 Bytes
    public int getMemory(int address, int size) {
        checkSize(size);
        return switch (size) {
            case 1 -> getByte(address);
            case 2 -> getHalfword(address);
            case 4 -> getWord(address);
            default -> -1;
        };
    }

    private int getNextMemory(int size) {
        int result = getMemory(getPC(size), size);
        setPC(getPC(size) + size, size);
        return result;
    }

    // Only use this for 1, 2 or 4 Bytes
    private void setMemory(int address, int size, int result) {
        checkSize(size);
        switch (size) {
            case 1 -> setByte(address, result);
            case 2 -> setHalfword(address, result);
            case 4 -> setWord(address, result);
            //TODO: Maybe need to support 8 here for DOUBLES
        }
    }

    private int getAddress(int address) {
        return getMemory(address, 4);
    }

    private int computeStackAddressingWithMinus(int reg, int operandSize) {
        int address = getRegister(reg);
        address -= operandSize;
        setRegister(reg, WORD_SIZE, address);
        return address;
    }

    private int computeRelativeAddressing(int reg, int size_of_a) {
        int a = getNextMemory(size_of_a);
        return getRegister(reg) + a;
    }

    private int computeIndirectAddressing(int reg, int size_of_a) {
        int a = getNextMemory(size_of_a);
        int address1 = getRegister(reg) + a;
        return getAddress(address1);
    }

    private int getNextOperand(int operandSize) {
        int b = getNextByte();

        int reg = (b & 0x0F);
        int addressType = b >>> 4;
        if (addressType < 4) addressType = 0; // We do this so we only have to use one case statement for every direct Operand Case

        int result = 0;

        switch (addressType) {
            // Direct Operand between 0 and 63
            case AddressType.SMALL_DIRECT_OPERAND: {
                result = (b & 0x3F);
            } break;
            // Register Addressing
            case AddressType.REGISTER_ADDRESSING: {
                result = getRegister(reg, operandSize);
            } break;
            // Relative Addressing a + !Rx, where a = 0
            case AddressType.RELATIVE_ADDRESSING_WITH_ZERO: {
                int reg_value = getRegister(reg, operandSize);
                result = getMemory(reg_value, operandSize);
            } break;
            // Stack Addressing by -!Rx
            case AddressType.STACK_ADDRESSING_WITH_MINUS: {
                int address = computeStackAddressingWithMinus(reg, operandSize);
                result = getMemory(address, operandSize);
            } break;
            case AddressType.BIG_DIRECT_OPERAND_OR_STACK_ADDRESSING_WITH_PLUS: {
                // Direct Operand bigger than 63
                if (reg == 15) {
                    for (int i = 0; i < operandSize; i++) {
                        int op = getNextByte();
                        result = (result << 8) + op;
                    }
                }
                // Stack Addressing by !Rx+
                else {
                    int address = getRegister(reg);
                    result = getMemory(address, operandSize);
                    setRegister(reg, WORD_SIZE, address + operandSize);
                } break;
            }
            // Absolute Addressing
            case AddressType.ABSOLUT_ADDRESS: {
                assert((b & 0x0F) == 15); //Not sure why the rest of the byte has to be 15

                int address = getNextWord();
                result = getMemory(address, operandSize);
            } break;
            // Relative Addressing a + !Rx, where a fits into one byte
            case AddressType.RELATIVE_ADDRESSING_WITH_BYTE: {
                int address = computeRelativeAddressing(reg, BYTE_SIZE);
                result = getMemory(address, operandSize);
            } break;
            // Relative Addressing a + !Rx, where a fits into two byte
            case AddressType.RELATIVE_ADDRESSING_WITH_HALFWORD: {
                int address = computeRelativeAddressing(reg, HALFWORD_SIZE);
                result = getMemory(address, operandSize);
            } break;
            case AddressType.RELATIVE_ADDRESSING_WITH_WORD: {
                int address = computeRelativeAddressing(reg, WORD_SIZE);
                result = getMemory(address, operandSize);
            } break;
            case AddressType.INDICATED_RELATIVE_ADDRESSING: {
                //TODO: Implement me
            } break;
            case AddressType.INDIRECT_ADDRESSING_WITH_BYTE: {
                int address = computeIndirectAddressing(reg, BYTE_SIZE);
                result = getMemory(address, operandSize);
            } break;
            case AddressType.INDIRECT_ADDRESSING_WITH_HALFWORD: {
                int address = computeIndirectAddressing(reg, HALFWORD_SIZE);
                result = getMemory(address, operandSize);
            } break;
            case AddressType.INDIRECT_ADDRESSING_WITH_WORD: {
                int address = computeIndirectAddressing(reg, WORD_SIZE);
                result = getMemory(address, operandSize);
            } break;
            default: assert(false);
        }

        return result;
    }

    private void saveResult(int result, int operandSize) {
        int b = getNextByte();

        int reg = (b & 0x0F);
        int regValue = getRegister(reg);

        //TODO: @Felix Funktioniert der >>> operator richtig? Soll er nicht mit 0en auffüllen?
        byte addressType = (byte) (b >>> 4);

        switch (addressType) {
            case AddressType.REGISTER_ADDRESSING -> {
                setRegister(reg, operandSize, result);
            }
            case AddressType.RELATIVE_ADDRESSING_WITH_ZERO -> {
                setMemory(regValue, operandSize, result);
            }
            case AddressType.STACK_ADDRESSING_WITH_MINUS -> {
                int address = computeStackAddressingWithMinus(reg, operandSize);
                setMemory(address, operandSize, result);
            }
            case AddressType.BIG_DIRECT_OPERAND_OR_STACK_ADDRESSING_WITH_PLUS -> {
                assert (reg != 15); // Note: Direct Operands can't save a result
                int address = getRegister(reg);
                setMemory(address, operandSize, result);
                setRegister(reg, WORD_SIZE, address + operandSize);
            }
            case AddressType.ABSOLUT_ADDRESS -> {
                assert ((b & 0x0F) == 15); //Not sure why the rest of the byte has to be 15

                int address = getNextWord();
                setMemory(address, operandSize, result);
            }

            // Relative Addressing a + !Rx, where a fits into one byte
            case AddressType.RELATIVE_ADDRESSING_WITH_BYTE -> {
                int a = getNextMemory(BYTE_SIZE);
                setMemory(a + regValue, operandSize, result);
            }

            // Relative Addressing a + !Rx, where a fits into two byte
            case AddressType.RELATIVE_ADDRESSING_WITH_HALFWORD -> {
                int a = getNextMemory(HALFWORD_SIZE);
                setMemory(a + regValue, operandSize, result);
            }
            case AddressType.RELATIVE_ADDRESSING_WITH_WORD -> {
                int a = getNextMemory(WORD_SIZE);
                setMemory(a + regValue, operandSize, result);
            }
            case AddressType.INDIRECT_ADDRESSING_WITH_BYTE -> {
                int address = computeIndirectAddressing(reg, BYTE_SIZE);
                setMemory(address, operandSize, result);
            }
            case AddressType.INDIRECT_ADDRESSING_WITH_HALFWORD -> {
                int address = computeIndirectAddressing(reg, HALFWORD_SIZE);
                setMemory(address, operandSize, result);
            }
            case AddressType.INDIRECT_ADDRESSING_WITH_WORD -> {
                int address = computeIndirectAddressing(reg, WORD_SIZE);
                setMemory(address, operandSize, result);
            }
        }
    }

    //endregion

    public void executeOneInstruction() {
        int opcode = getNextByte();

        switch(OpCode.find(opcode)) {
            case HALT: halt(); break;

            case CMP_B: cmp_I(BYTE_SIZE); break;
            case CMP_H: cmp_I(HALFWORD_SIZE); break;
            case CMP_W: cmp_I(WORD_SIZE); break;

            case CLEAR_B: clear(BYTE_SIZE); break;
            case CLEAR_H: clear(HALFWORD_SIZE); break;
            case CLEAR_W: clear(WORD_SIZE); break;
            case CLEAR_F: clear(FLOAT_SIZE); break;
            case CLEAR_D: clear(DOUBLE_SIZE); break;//TODO: Check if 8 Byte work

            case MOVE_B: move(BYTE_SIZE); break;
            case MOVE_H: move(HALFWORD_SIZE); break;
            case MOVE_W: move(WORD_SIZE); break;

            case MOVEN_B: moven_I(BYTE_SIZE); break;
            case MOVEN_H: moven_I(HALFWORD_SIZE); break;
            case MOVEN_W: moven_I(WORD_SIZE); break;

            case MOVEC_B: movec(BYTE_SIZE); break;
            case MOVEC_H: movec(HALFWORD_SIZE); break;
            case MOVEC_W: movec(WORD_SIZE); break;

            case OR_B2: or_2(BYTE_SIZE); break;
            case OR_H2: or_2(HALFWORD_SIZE); break;
            case OR_W2: or_2(WORD_SIZE); break;
            case OR_B3: or_3(BYTE_SIZE); break;
            case OR_H3: or_3(HALFWORD_SIZE); break;
            case OR_W3: or_3(WORD_SIZE); break;

            case ANDNOT_B2: andnot_2(BYTE_SIZE); break;

            case ADD_B2: HOW_TO_NAME_THIS(BYTE_SIZE, Operation.ADD, true); break;
            case ADD_H2: add_h2(); break;
            case ADD_W2: add_w2(); break;
            case ADD_B3: add_b3(); break;

            case SUB_B2: sub_b2();
        }
    }

    public void run() {
        //TODO: Do we assume that a halt is at the end of the Program
        // and simply run until we find it or do we have another way
        // to know that we are finished? @Alex: There should always be a zero byte at the end.
        while (!programHaltet) {
            executeOneInstruction();
        }
    }

    public void halt() {

        programHaltet = true;
    }

    public void move(int size) {
        int a1 = getNextOperand(size);

        V = false;
        setZ(a1, size);
        setN(a1, size);

        saveResult(a1, size);
    }

    public void cmp_I(int size) {
        int a1 = getNextOperand(size);
        int a2 = getNextOperand(size);

        Z = (a1 == a2);
        N = (a1 < a2);
    }

    public void clear(int size) {
        V = false;
        Z = true;
        N = false;
        saveResult(0, size);
    }

    public void moven_I(int size) {
        int result = -getNextOperand(size);

        C = false;
        setV(result, size);
        setZ(result, size);
        setN(result, size);

        saveResult(result, size);
    }

    public void movec(int size) {
        int a1 = ~ getNextOperand(size);

        V = false;
        setZ(a1, size);
        setN(a1, size);

        saveResult(a1, size);
    }

    public void movea() {
        //TODO: This is not the same as move. Figure out how to do this
        //int a1 = getNextOperand(WORD_SIZE);
        //saveResult(a1, WORD_SIZE);
    }

    public void HOW_TO_NAME_THIS(int size, Operation op, boolean twoOperands) {
        int a1 = getNextOperand(size);
        int a2 = getNextOperand(size);
        int result = switch (op) {
            case OR -> a1 | a2;
            case ANDNOT -> a1 & ~ a2;
            case XOR -> a1 ^ a2;
            case ADD -> a1 + a2;
            case SUB -> a2 - a1;
            case MULT -> a1 * a2;
            case DIV -> a2 / a1;
        };

        switch (op) {
            case OR, ANDNOT, XOR -> setOrAndnotXorFlags(result, size);
            case ADD, SUB -> {
                //TODO: Set Carry @Felix klären wie Carry sich verhält
                setV(result, size);
                setZ(result, size);
                setN(result, size);
            }
            case MULT, DIV -> {
                C = false;
                setV(result, size);
                setZ(result, size);
                setN(result, size);
            }

        }

        if (twoOperands) decPC();
        saveResult(result, size);
    }

    private void setOrAndnotXorFlags(int result, int size) {
        V = false;
        setZ(result, size);
        setN(result, size);
    }

    public void or_2(int size) {
        int a1 = getNextOperand(size);
        int a2 = getNextOperand(size);
        int result = a1 | a2;

        setOrAndnotXorFlags(result, size);

        decPC();
        saveResult(result, size);
    }

    public void or_3(int size) {
        int a1 = getNextOperand(size);
        int a2 = getNextOperand(size);
        int result = a1 | a2;

        setOrAndnotXorFlags(result, size);

        saveResult(result, size);
    }

    public void andnot_2(int size) {
        int a1 = getNextOperand(size);
        int a2 = getNextOperand(size);
        int result = (a1 & ~a2);

        setOrAndnotXorFlags(result, size);

        saveResult(result, size);
    }

    //TODO: GROUP THE ADDS TOGETHER MAYBE?
    public void add_b2() {
        int a1 = getNextOperand(BYTE_SIZE);
        int a2 = getNextOperand(BYTE_SIZE);
        int result = a1 + a2;

        decPC();
        saveResult(result, BYTE_SIZE);
    }

    public void add_h2() {
        int a1 = getNextOperand(HALFWORD_SIZE);
        int a2 = getNextOperand(HALFWORD_SIZE);
        int result = a1 + a2;

        decPC();
        saveResult(result, HALFWORD_SIZE);
    }

    public void add_w2() {
        int a1 = getNextOperand(WORD_SIZE);
        int a2 = getNextOperand(WORD_SIZE);
        int result = a1 + a2;

        decPC();
        saveResult(result, WORD_SIZE);
    }

    public void add_b3() {
        //TODO: Zum testen gehen wir jetzt kurzzeitig von Registern aus

        int a1 = getNextOperand(1);
        int a2 = getNextOperand(1);
        int result = a1 + a2;

        saveResult(result, 1);
    }

    public void sub_b2() {
        int a1 = getNextOperand(1);
        int a2 = getNextOperand(1);
        int result = a2 - a1;

        decPC();
        saveResult(result, 1);
    }

}
