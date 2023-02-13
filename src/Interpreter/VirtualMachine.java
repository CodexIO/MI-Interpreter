package Interpreter;

import Assembler.OpCode;

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

    private byte[] memory = new byte[MEMORY_LENGTH];
    private final boolean[] changedMemory = new boolean[MEMORY_LENGTH];

    public int[] registers = new int[NUMBER_OF_REGISTERS]; //TODO: Make registers private again with proper testing
    public final boolean[] changedRegisters = new boolean[NUMBER_OF_REGISTERS];

    private boolean programHaltet;
    private int operationResult;

    public VirtualMachine(int begin, byte[] memory) {
        //TODO: Handle bad input

        for (int i = 0;  i < memory.length; i++) {
            int address = begin + i;
            this.memory[address] = memory[i];
            changedMemory[address] = true;
        }
    }

    public VirtualMachine(byte[] memory) {
        this(0, memory);
    }

    public VirtualMachine() {
        this(0, new byte[]{});
    }

    public VirtualMachine(byte[] memory, int[] registers) {
        this(memory);
        System.arraycopy(registers, 0, this.registers, 0, registers.length);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int lastNonZeroByteIndex = memory.length -1;
        while (memory[lastNonZeroByteIndex] == 0 && lastNonZeroByteIndex != 0) lastNonZeroByteIndex--;
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
                "  memory: {" + sb + "\n}\n" +
                "  registers=" + Arrays.toString(registers) + "\n" +
                '}';
    }

    private void checkSize(int size) {
        assert(size == BYTE_SIZE || size == HALFWORD_SIZE || size == WORD_SIZE);
    }

    // Resets the State of the VM, so it can run again
    public void reset() {
        V = N = Z = C = false;
        Arrays.fill(registers, 0);


        Arrays.fill(changedRegisters, false);
        Arrays.fill(changedMemory, false);
        programHaltet = false;
    }

    //region Getter and Setter
    public byte[] getMemory() {
        return memory;
    }

    public void setMemory(byte[] mem) {
        Arrays.fill(memory, (byte) 0);
        System.arraycopy(mem, 0, memory, 0, mem.length);
    }

    //@Cleanup Check if this even gets called from other than getPC()
    public int getPC(int size) {
        return getRegister(PC_REGISTER, size);
    }

    public int getPC() {
        return getPC(WORD_SIZE);
    }

    public void setPC(int value, int size) {
        setRegister(PC_REGISTER, size, value);
    }

    public void setPC(int value) {
        setPC(value, WORD_SIZE);
    }

    private void decPC() {
        setRegister(PC_REGISTER, WORD_SIZE, registers[PC_REGISTER] - 1);
    }

    private void incPC() {
        setRegister(PC_REGISTER, WORD_SIZE, registers[PC_REGISTER] + 1);
    }

    private int getSP(int size) {
        return getRegister(SP_REGISTER, size);
    }

    private int getSP() {
        return getSP(WORD_SIZE);
    }

    private void setSP(int value, int size) {
        setRegister(SP_REGISTER, size, value);
    }

    private void setSP(int value) {
        setSP(value, WORD_SIZE);
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
        int result = getByte(getPC());
        incPC();
        return result;
    }

    private int getByte(int address) {
        if (address >= memory.length) return 0;
        byte result = memory[address];
        return (result & 0xFF);
    }

    private void setByte(int address, int number) {
        changedMemory[address] = true;
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
        int result = value & mask;

        //TODO: @Felix I'm not sure we want this behaviour in the registers.
        /*result = switch(size) {
            case 1 -> (byte)  result;
            case 2 -> (short) result;
            case 4 -> (int)   result;
            default -> result;
        };*/
        changedRegisters[reg] = true;
        registers[reg] = result;
    }

    private int getNextHalfword() {
        int a = getNextByte();
        a = (a << 8) + getNextByte();
        return a;
    }

    private int getHalfword(int address) {
        int a = getByte(address);
        a = (a << 8) + getByte(address + 1);
        return a;
    }

    private void setHalfword(int address, int number) {
        setByte(address, number >>> 8);
        setByte(address + 1, number);
    }

    private int getNextWord() {
        int a = getNextHalfword();
        a = (a << 16) + getNextHalfword();
        return a;
    }

    private int getWord(int address) {
        int a = getHalfword(address);
        a = (a << 16) + getHalfword(address + 2);
        return a;
    }

    private void setWord(int address, int number) {
        setHalfword(address, number >>> 16);
        setHalfword(address + 2, number);
    }

    // Only use this for 1, 2 or 4 Bytes
    public int getMemory(int address, int size) {
        checkSize(size);
        int result = switch (size) {
            case 1 -> (byte)  getByte(address);
            case 2 -> (short) getHalfword(address);
            case 4 -> (int)   getWord(address);
            default -> -1;
        };
        return result;
    }

    private int getNextMemory(int size) {
        int result = getMemory(getPC(size), size);
        setPC(getPC(size) + size, size);
        return result;
    }

    //TODO: CHECK IF getMemory and setMemory use the correct Endianness.
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
        // It's important that we get the regValue before we call getNextMemory,
        // because getNextMemory changes the PC which we don't want here.
        int regValue = getRegister(reg);
        int a = getNextMemory(size_of_a);
        return regValue + a;
    }

    private int computeIndirectAddressing(int reg, int size_of_a) {
        int a = getNextMemory(size_of_a);
        int address1 = getRegister(reg) + a;
        return getAddress(address1);
    }

    private int computeNextAddress(int operandSize) {
        //TODO: COPY PASTED FROM getNextOperand()
        //      can we factor this together?
        //      Maybe make 'em part of the VM
        int b = getNextByte();
        int reg = (b & 0x0F);
        int addressType = b >>> 4;
        if (addressType < 4) addressType = 0;

        return computeAddress(b, addressType, reg, operandSize);
    }


    // TODO: Maybe Factor computeAddress and getOperand together.
    private int computeAddress(int b, int addressType, int reg, int operandSize) {
        switch (addressType) {
            // Direct Operand between 0 and 63
            case AddressType.SMALL_DIRECT_OPERAND -> {
                return (b & 0x3F);
            }
            // Register Addressing || Relative Addressing a + !Rx, where a = 0
            case AddressType.REGISTER_ADDRESSING, AddressType.RELATIVE_ADDRESSING_WITH_ZERO -> {
                return getRegister(reg, operandSize);
            }

            case AddressType.STACK_ADDRESSING_WITH_MINUS -> {
                return computeStackAddressingWithMinus(reg, operandSize);
            }

            case AddressType.BIG_DIRECT_OPERAND_OR_STACK_ADDRESSING_WITH_PLUS -> {
                // Direct Operand bigger than 63
                if (reg == 15) {
                    int operand = 0;
                    for (int i = 0; i < operandSize; i++) {
                        int op = getNextByte();
                        operand = (operand << 8) + op;
                    }
                    return operand;
                }
                // Stack Addressing by !Rx+
                else {
                    int tmpAddress = getRegister(reg);
                    int address = getMemory(tmpAddress, operandSize);
                    setRegister(reg, WORD_SIZE, tmpAddress + operandSize);
                    return address;
                }
            }

            // Absolute Addressing
            case AddressType.ABSOLUT_ADDRESS -> {
                assert((b & 0x0F) == 15); //Not sure why the rest of the byte has to be 15

                return getNextWord();
            }

            // Relative Addressing a + !Rx, where a fits into one byte
            case AddressType.RELATIVE_ADDRESSING_WITH_BYTE -> {
                return computeRelativeAddressing(reg, BYTE_SIZE);
            }

            // Relative Addressing a + !Rx, where a fits into two byte
            case AddressType.RELATIVE_ADDRESSING_WITH_HALFWORD -> {
                return computeRelativeAddressing(reg, HALFWORD_SIZE);
            }
            case AddressType.RELATIVE_ADDRESSING_WITH_WORD -> {
                return computeRelativeAddressing(reg, WORD_SIZE);
            }
            case AddressType.INDICATED_RELATIVE_ADDRESSING -> {
                //TODO: Implement me
            }
            case AddressType.INDIRECT_ADDRESSING_WITH_BYTE -> {
                return computeIndirectAddressing(reg, BYTE_SIZE);
            }
            case AddressType.INDIRECT_ADDRESSING_WITH_HALFWORD -> {
                return computeIndirectAddressing(reg, HALFWORD_SIZE);
            }
            case AddressType.INDIRECT_ADDRESSING_WITH_WORD -> {
                return computeIndirectAddressing(reg, WORD_SIZE);
            }

            //default -> assert(false);
        }
        //TODO: ERROR HERE
        return -1;
    }

    private int getNextOperand(int operandSize) {
        int b = getNextByte();

        int reg = (b & 0x0F);
        int addressType = b >>> 4;
        if (addressType < 4) addressType = 0; // We do this so we only have to use one case statement for every direct Operand Case

        int address = -1;

        switch (addressType) {
            // Direct Operand between 0 and 63
            case AddressType.SMALL_DIRECT_OPERAND: {
                return (b & 0x3F);
            }
            // Register Addressing
            case AddressType.REGISTER_ADDRESSING: {
                return getRegister(reg, operandSize);
            }
            case AddressType.RELATIVE_ADDRESSING_WITH_ZERO, AddressType.STACK_ADDRESSING_WITH_MINUS: {
                address = computeAddress(b, addressType, reg, operandSize);
            } break;
            case AddressType.BIG_DIRECT_OPERAND_OR_STACK_ADDRESSING_WITH_PLUS: {
                // Direct Operand bigger than 63
                if (reg == 15) {
                    int operand = 0;
                    for (int i = 0; i < operandSize; i++) {
                        int op = getNextByte();
                        operand = (operand << 8) + op;
                    }
                    return operand;
                }
                // Stack Addressing by !Rx+
                else {
                    address = getRegister(reg);
                    int result = getMemory(address, operandSize);
                    setRegister(reg, WORD_SIZE, address + operandSize);

                    return result;
                }
            }
            // Absolute Addressing
            case AddressType.ABSOLUT_ADDRESS: {
                assert((b & 0x0F) == 15); //Not sure why the rest of the byte has to be 15
                address = computeAddress(b, addressType, reg, operandSize);
            } break;
            case AddressType.RELATIVE_ADDRESSING_WITH_BYTE:
            case AddressType.INDIRECT_ADDRESSING_WITH_BYTE: {
                address = computeAddress(b, addressType, reg, BYTE_SIZE);
            } break;
            case AddressType.RELATIVE_ADDRESSING_WITH_HALFWORD:
            case AddressType.INDIRECT_ADDRESSING_WITH_HALFWORD: {
                address = computeAddress(b, addressType, reg, HALFWORD_SIZE);
            } break;
            case AddressType.RELATIVE_ADDRESSING_WITH_WORD:
            case AddressType.INDIRECT_ADDRESSING_WITH_WORD: {
                address = computeAddress(b, addressType, reg, WORD_SIZE);
            } break;
            case AddressType.INDICATED_RELATIVE_ADDRESSING: {
                //TODO: Implement me
            } break;
            default: assert(false);
        }

        return getMemory(address, operandSize);
    }

    private void saveResult(int result, int operandSize) {
        this.operationResult = result;
        int b = getNextByte();

        int reg = (b & 0x0F);
        int regValue = getRegister(reg);

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

        OpCode op = OpCode.find(opcode);
        switch(op) {
            case HALT -> {
                decPC(); //We do this because getNextByte() automatically increments the PC
                halt();
            }

            case CMP_B -> cmp_I(BYTE_SIZE);
            case CMP_H -> cmp_I(HALFWORD_SIZE);
            case CMP_W -> cmp_I(WORD_SIZE);

            case CMP_F -> {
            }
            case CMP_D -> {
            }
            case CLEAR_B -> clear(BYTE_SIZE);
            case CLEAR_H -> clear(HALFWORD_SIZE);
            case CLEAR_W -> clear(WORD_SIZE);
            case CLEAR_F -> clear(FLOAT_SIZE);
            case CLEAR_D -> clear(DOUBLE_SIZE);//TODO: Check if 8 Byte work

            case MOVE_B -> move(BYTE_SIZE);
            case MOVE_H -> move(HALFWORD_SIZE);
            case MOVE_W -> move(WORD_SIZE);

            case MOVE_F -> {
            }
            case MOVE_D -> {
            }
            case MOVEN_B -> moven_I(BYTE_SIZE);
            case MOVEN_H -> moven_I(HALFWORD_SIZE);
            case MOVEN_W -> moven_I(WORD_SIZE);

            case MOVEN_F -> {
            }
            case MOVEN_D -> {
            }
            case MOVEC_B -> movec(BYTE_SIZE);
            case MOVEC_H -> movec(HALFWORD_SIZE);
            case MOVEC_W -> movec(WORD_SIZE);

            case MOVEA -> movea();
            case OR_B2 -> or_2(BYTE_SIZE);
            case OR_H2 -> or_2(HALFWORD_SIZE);
            case OR_W2 -> or_2(WORD_SIZE);
            case OR_B3 -> or_3(BYTE_SIZE);
            case OR_H3 -> or_3(HALFWORD_SIZE);
            case OR_W3 -> or_3(WORD_SIZE);

            case ANDNOT_B2 -> andnot_2(BYTE_SIZE);
            case ANDNOT_H2 -> HOW_TO_NAME_THIS(HALFWORD_SIZE, Operation.ANDNOT, true);
            case ANDNOT_W2 -> HOW_TO_NAME_THIS(WORD_SIZE, Operation.ANDNOT, true);

            case ANDNOT_B3 -> HOW_TO_NAME_THIS(BYTE_SIZE, Operation.ANDNOT, false);
            case ANDNOT_H3 -> HOW_TO_NAME_THIS(HALFWORD_SIZE, Operation.ANDNOT, false);
            case ANDNOT_W3 -> HOW_TO_NAME_THIS(WORD_SIZE, Operation.ANDNOT, false);

            case XOR_B2 -> HOW_TO_NAME_THIS(BYTE_SIZE, Operation.XOR, true);
            case XOR_H2 -> HOW_TO_NAME_THIS(HALFWORD_SIZE, Operation.XOR, true);
            case XOR_W2 -> HOW_TO_NAME_THIS(WORD_SIZE, Operation.XOR, true);
            case XOR_B3 -> HOW_TO_NAME_THIS(BYTE_SIZE, Operation.XOR, false);
            case XOR_H3 -> HOW_TO_NAME_THIS(HALFWORD_SIZE, Operation.XOR, false);
            case XOR_W3 -> HOW_TO_NAME_THIS(WORD_SIZE, Operation.XOR, false);

            case ADD_B2 -> HOW_TO_NAME_THIS(BYTE_SIZE, Operation.ADD, true);
            case ADD_H2 -> add_h2(); //TODO: @Cleanup make everything use the same Function
            case ADD_W2 -> add_w2();
            case ADD_F2 -> {
            }
            case ADD_D2 -> {
            }
            case ADD_B3 -> add_b3();
            case ADD_H3 -> HOW_TO_NAME_THIS(HALFWORD_SIZE, Operation.ADD, false);
            case ADD_W3 -> HOW_TO_NAME_THIS(WORD_SIZE, Operation.ADD, false);
            case ADD_F3 -> {
            }
            case ADD_D3 -> {
            }
            case SUB_B2 -> sub_b2();
            case SUB_H2 -> HOW_TO_NAME_THIS(HALFWORD_SIZE, Operation.SUB, true);
            case SUB_W2 -> HOW_TO_NAME_THIS(WORD_SIZE, Operation.SUB, true);
            case SUB_F2 -> {
            }
            case SUB_D2 -> {
            }
            case SUB_B3 -> HOW_TO_NAME_THIS(BYTE_SIZE, Operation.SUB, false);
            case SUB_H3 -> HOW_TO_NAME_THIS(HALFWORD_SIZE, Operation.SUB, false);
            case SUB_W3 -> HOW_TO_NAME_THIS(WORD_SIZE, Operation.SUB, false);
            case SUB_F3 -> {
            }
            case SUB_D3 -> {
            }

            case MULT_B2 -> HOW_TO_NAME_THIS(BYTE_SIZE, Operation.MULT, true);
            case MULT_H2 -> HOW_TO_NAME_THIS(HALFWORD_SIZE, Operation.MULT, true);
            case MULT_W2 -> HOW_TO_NAME_THIS(WORD_SIZE, Operation.MULT, true);
            case MULT_F2 -> {
            }
            case MULT_D2 -> {
            }
            case MULT_B3 -> HOW_TO_NAME_THIS(BYTE_SIZE, Operation.MULT, false);
            case MULT_H3 -> HOW_TO_NAME_THIS(HALFWORD_SIZE, Operation.MULT, false);
            case MULT_W3 -> HOW_TO_NAME_THIS(WORD_SIZE, Operation.MULT, false);
            case MULT_F3 -> {
            }
            case MULT_D3 -> {
            }
            case DIV_B2 -> HOW_TO_NAME_THIS(BYTE_SIZE, Operation.DIV, true);
            case DIV_H2 -> HOW_TO_NAME_THIS(HALFWORD_SIZE, Operation.DIV, true);
            case DIV_W2 -> HOW_TO_NAME_THIS(WORD_SIZE, Operation.DIV, true);
            case DIV_F2 -> {
            }
            case DIV_D2 -> {
            }
            case DIV_B3 -> HOW_TO_NAME_THIS(BYTE_SIZE, Operation.DIV, false);
            case DIV_H3 -> HOW_TO_NAME_THIS(HALFWORD_SIZE, Operation.DIV, false);
            case DIV_W3 -> HOW_TO_NAME_THIS(WORD_SIZE, Operation.DIV, false);
            case DIV_F3 -> {
            }
            case DIV_D3 -> {
            }
            case JEQ, JNE, JGT, JGE, JLT, JLE -> jumpOnCondition(op);
            case JC -> {
            }
            case JNC -> {
            }
            case JUMP -> jump();
            case CALL -> call();
            case RET -> ret();
            case PUSHR -> {
            }
            case POPR -> {
            }
        }
    }

    public void run() {
        // Since we increase the PC before we get the Value of the Memory
        // we have to start at -1
        //setPC(-1, WORD_SIZE);

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
        int address = computeNextAddress(WORD_SIZE);
        saveResult(address, WORD_SIZE);
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

    private void jump() {
        int address = computeNextAddress(WORD_SIZE);
        setPC(address);
    }

    private void call() {
        int address = computeNextAddress(WORD_SIZE);

        setSP(getSP() - 4);
        setMemory(getSP(), WORD_SIZE, getPC());

        setPC(address);
    }

    private void ret() {
        int address = getMemory(getSP(), WORD_SIZE);
        setPC(address);
        setSP(getSP() + 4);
    }

    private void jumpOnCondition(OpCode op) {
        boolean cond = switch (op) {
            case JEQ -> operationResult == 0;
            case JNE -> operationResult != 0;
            case JGT -> operationResult >  0;
            case JGE -> operationResult >= 0;
            case JLT -> operationResult <  0;
            case JLE -> operationResult <= 0;
            default -> false;
        };

        int address = computeNextAddress(WORD_SIZE);
        if (cond) setPC(address, WORD_SIZE);
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