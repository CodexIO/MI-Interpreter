package Interpreter;

import Assembler.AST_Nodes.Command;
import Assembler.OpCode;
import Assembler.Parser;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        public static final int INDIRECT_ADDRESSING_WITH_BYTE = 11;
        public static final int INDIRECT_ADDRESSING_WITH_HALFWORD = 13;
        public static final int INDIRECT_ADDRESSING_WITH_WORD = 15;
        public static final int INDEXED_ADDRESSING = 4;
        public static final int STACK_ADDRESSING_WITH_MINUS = 7;

        private AddressType() {}
    }

    public enum Operation {
        OR, ANDNOT, XOR, ADD, SUB, MULT, DIV
    }

    // Condition Codes / Flags
    public boolean carry;
    public boolean zero;
    public boolean negative;
    public boolean overflow;

    private final byte[] memory = new byte[MEMORY_LENGTH];
    private final boolean[] changedMemory = new boolean[MEMORY_LENGTH];

    private final int[] registers = new int[NUMBER_OF_REGISTERS];
    public final boolean[] changedRegisters = new boolean[NUMBER_OF_REGISTERS];

    private final Map<Integer, Integer> lineNumberToAddress = new HashMap<>();
    private final Map<Integer, Integer> addressToLineNumber = new HashMap<>();
    private int[] breakpointAddresses = new int[0];

    private boolean justBreaked;
    public boolean programHaltet;
    public boolean memoryHasChanged;

    private PrintStream out = System.out;
    private Parser parser = new Parser(out);

    public VirtualMachine(int begin, byte[] memory) {
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

    public VirtualMachine(PrintStream out) {
        this(0, new byte[]{});
        this.out = out;
    }

    public VirtualMachine(byte[] memory, int[] registers) {
        this(memory);
        System.arraycopy(registers, 0, this.registers, 0, registers.length);
    }

    public VirtualMachine(String program) {
        parser.parse(program);
        byte[] machineCode = parser.generateMachineCode();
        setLineNumberToAddress(parser.getCommands());
        setMemory(machineCode);
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
                "  C=" + carry +
                ", Z=" + zero +
                ", N=" + negative +
                ", V=" + overflow + "\n" +
                "  memory: {" + sb + "\n}\n" +
                "  registers=" + Arrays.toString(registers) + "\n" +
                '}';
    }

    public void run(String input) {
        parser.parse(input);
        reset();
        setMemory(parser.generateMachineCode());
        setLineNumberToAddress(parser.getCommands());

        run();
    }

    private void checkSize(int size) {
        assert(size == BYTE_SIZE || size == HALFWORD_SIZE || size == WORD_SIZE);
    }

    // Resets the State of the VM, so it can run again
    public void reset() {
        overflow = negative = zero = carry = false;
        Arrays.fill(registers, 0);


        Arrays.fill(changedRegisters, false);
        Arrays.fill(changedMemory, false);
        breakpointAddresses = new int[0];
        programHaltet = false;
    }

    //region Getter and Setter
    public byte[] getMemory() {
        return memory;
    }

    public void setMemory(byte[] mem) {
        Arrays.fill(memory, (byte) 0);
        System.arraycopy(mem, 0, memory, 0, mem.length);
        memoryHasChanged = true;
    }

    public void setLineNumberToAddress(List<Command> commands) {
        for (var command : commands) {
            int lineNumber = command.getLineNumber();
            int address = command.getAddress();

            //TODO: Check if no lineNumber gets set multiple times (normally this shouldn't happen)
            lineNumberToAddress.put(lineNumber, address);
            addressToLineNumber.put(address, lineNumber);
        }
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

    private int getSP() {
        return getRegister(SP_REGISTER, WORD_SIZE);
    }

    private void setSP(int value) {
        setRegister(SP_REGISTER, WORD_SIZE, value);
    }

    public void setCarry(long result, int size) {
        long bIndexMinusOne = result & 1L << (size * 8);
        carry = bIndexMinusOne != 0;
    }

    public void setOverflow(long result, int size) {
        int n = size * 8;
        long maxValue = (1L << (n - 1)) - 1; // 2^(n-1) - 1
        long minValue = - (1L << (n - 1));   // - 2^(n-1)

        overflow = (result > maxValue || result < minValue);
    }

    public void setOverflow(double result) {
        overflow = Double.isInfinite(result);
    }

    public void setZero(long result, int size) {
        result = normaliseResult((int) result, size);
        zero = (result == 0);
    }

    public void setZero(double result) {
        zero = (result == 0);
    }

    public void setNegative(long result, int size) {
        long indexOfNegativeBit = (0x80L << (size - 1) * 8);
        negative = ((result & indexOfNegativeBit) != 0);
    }

    public void setNegative(double result) {
        negative = result < 0;
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

    private void setByte(int address, long number) {
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

    public float getRegisterAsFloat(int reg) {
        int bits = registers[reg];
        return Float.intBitsToFloat(bits);
    }

    public double getRegisterAsDouble(int reg) {
        long bits = getWideRegister(reg);
        return Double.longBitsToDouble(bits);
    }

    public int getRegisterWithSign(int reg, int size) {
        int result = getRegister(reg, size);
        return switch(size) {
            case BYTE_SIZE -> (byte) result;
            case HALFWORD_SIZE -> (short) result;
            default -> result;
        };
    }

    public int getRegister(int reg, int size) {
        int mask = getMask(size);
        return registers[reg] & mask;
    }

    private long getWideRegister(int reg) {
        long result = getRegister(reg, WORD_SIZE);
        //I hate Java for making me do this useless &
        long second = getRegister((reg + 1) % 16) & 0x00000000_FFFF_FFFFL;
        result = result << 32 | second;
        return result;
    }

    private int getRegister(int reg) {
        return getRegister(reg, WORD_SIZE);
    }

    private void setRegister(int reg, long value) {
        setRegister(reg, WORD_SIZE, (int) (value >>> 32));
        setRegister((reg + 1) % 16, WORD_SIZE, (int) value);
    }

    public void setRegister(int reg, int size, int value) {
        checkSize(size);
        int mask = getMask(size);
        int maskNegated = ~mask;
        int result = value & mask;

        //@Feature I'm not sure we want this behaviour in the registers.
        //       Antwort: Eventuell später in seperaten Registern anzeigen
        /*result = switch(size) {
            case 1 -> (byte)  result;
            case 2 -> (short) result;
            case 4 -> (int)   result;
            default -> result;
        };*/
        changedRegisters[reg] = true;
        int regValue = (registers[reg] & maskNegated) + result;
        registers[reg] = regValue;
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

    private void setHalfword(int address, long number) {
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

    private void setWord(int address, long number) {
        setHalfword(address, number >>> 16);
        setHalfword(address + 2, number);
    }

    // This should only be used for Doubles
    private long getWideWord(int address) {
        long a = getWord(address);
        a = (a << 32) + getWord(address + 4);
        return a;
    }

    private void setWideWord(int address, long number) {
        setWord(address, number >>> 32);
        setWord(address + 4, number);
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


    //TODO: It seems that we always want the memory with the Sign here, test this
    private int getNextMemory(int size) {
        //@Note: This function should never be called with size 8 which is only for Doubles
        int result = getMemory(getPC(), size);
        setPC(getPC(size) + size, size);

        return (int) doSignExtension(result, size);
    }

    private void setMemory(int address, int size, long result) {
        checkSize(size);
        memoryHasChanged = true;
        switch (size) {
            case 1 -> setByte(address, result);
            case 2 -> setHalfword(address, result);
            case 4 -> setWord(address, result);
            case 8 -> setWideWord(address, result);
        }
    }

    private int getAddress(int address) {
        return getMemory(address, WORD_SIZE);
    }

    private int computeStackAddressingWithMinus(int reg, int operandSize) {
        int address = getRegister(reg);
        address -= operandSize;
        setRegister(reg, WORD_SIZE, address);
        return address;
    }

    private int computeRelativeAddressing(int reg, int sizeOfA) {
        // It's important that we get the regValue before we call getNextMemory,
        // because getNextMemory changes the PC which we don't want here.
        int regValue = getRegister(reg);
        int a = getNextMemory(sizeOfA);
        return regValue + a;
    }

    private int computeIndirectAddressing(int reg, int sizeOfA) {
        int a = getNextMemory(sizeOfA);
        int address1 = getRegister(reg) + a;
        return getAddress(address1);
    }

    private int computeIndexedAddressing(int reg, int operandSize) {
        // Check if the next address is relative or indirekt
        int address = computeNextAddress(operandSize);
        int regValue = getRegister(reg);
        return address + regValue * operandSize;
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
            case AddressType.ABSOLUT_ADDRESS -> {
                assert((b & 0x0F) == 15); //Not sure why the rest of the byte has to be 15

                return getNextWord();
            }
            case AddressType.RELATIVE_ADDRESSING_WITH_BYTE -> {
                return computeRelativeAddressing(reg, BYTE_SIZE);
            }
            case AddressType.RELATIVE_ADDRESSING_WITH_HALFWORD -> {
                return computeRelativeAddressing(reg, HALFWORD_SIZE);
            }
            case AddressType.RELATIVE_ADDRESSING_WITH_WORD -> {
                return computeRelativeAddressing(reg, WORD_SIZE);
            }
            case AddressType.INDEXED_ADDRESSING -> {
                return computeIndexedAddressing(reg, operandSize);
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
        }
        //TODO: ERROR HERE
        return -1;
    }

    //TODO: @Cleanup: Do we always wanna do Signextension when we return numbers from here?
    private long getNextOperandWithSign(int operandSize) {
        long numberWithoutSign = getNextOperand(operandSize);
        return doSignExtension(numberWithoutSign, operandSize);
    }

    private long getNextOperand(int operandSize) {
        int b = getNextByte();

        int reg = (b & 0x0F);
        int addressType = b >>> 4;
        if (addressType < 4) addressType = 0; // We do this so we only have to use one case statement for every direct Operand Case

        int address = -1;

        switch (addressType) {
            // Direct Operand between 0 and 63
            case AddressType.SMALL_DIRECT_OPERAND -> {
                return (b & 0x3F);
            }
            case AddressType.REGISTER_ADDRESSING -> {
                if (operandSize == DOUBLE_SIZE) return getWideRegister(reg);
                return getRegister(reg, operandSize);
            }
            case AddressType.RELATIVE_ADDRESSING_WITH_ZERO, AddressType.STACK_ADDRESSING_WITH_MINUS ->
                address = computeAddress(b, addressType, reg, operandSize);
            case AddressType.BIG_DIRECT_OPERAND_OR_STACK_ADDRESSING_WITH_PLUS -> {
                // Direct Operand bigger than 63
                if (reg == 15) {
                    long operand = 0;
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
            case AddressType.ABSOLUT_ADDRESS -> {
                assert ((b & 0x0F) == 15); //Not sure why the rest of the byte has to be 15
                address = computeAddress(b, addressType, reg, operandSize);
            }
            case AddressType.RELATIVE_ADDRESSING_WITH_BYTE, AddressType.INDIRECT_ADDRESSING_WITH_BYTE ->
                address = computeAddress(b, addressType, reg, BYTE_SIZE);
            case AddressType.RELATIVE_ADDRESSING_WITH_HALFWORD, AddressType.INDIRECT_ADDRESSING_WITH_HALFWORD ->
                address = computeAddress(b, addressType, reg, HALFWORD_SIZE);
            case AddressType.RELATIVE_ADDRESSING_WITH_WORD, AddressType.INDIRECT_ADDRESSING_WITH_WORD ->
                address = computeAddress(b, addressType, reg, WORD_SIZE);
            case AddressType.INDEXED_ADDRESSING -> {
                address = computeAddress(b, addressType, reg, operandSize);
            }
        }

        if (operandSize == DOUBLE_SIZE) return getWideWord(address);
        else return getMemory(address, operandSize);
    }

    private double getNextOperandAsDouble() {
        long bits = getNextOperand(DOUBLE_SIZE);
        return Double.longBitsToDouble(bits);
    }

    private float getNextOperandAsFloat() {
        int bits = (int) getNextOperand(FLOAT_SIZE);
        return Float.intBitsToFloat(bits);
    }

    private void saveResult(float result) {
        int bits = Float.floatToIntBits(result);
        saveResult(bits, FLOAT_SIZE);
    }

    private void saveResult(double result) {
        long bits = Double.doubleToLongBits(result);
        saveResult(bits, DOUBLE_SIZE);
    }

    private long doSignExtension(long number, int size) {
        return switch (size) {
            case 1 -> (byte) number;
            case 2 -> (short)number;
            case 4 -> (int)  number;
            default -> number;
        };
    }

    private void saveResult(long result, int operandSize) {
        int b = getNextByte();

        int reg = (b & 0x0F);
        int regValue = getRegister(reg);

        byte addressType = (byte) (b >>> 4);

        switch (addressType) {
            case AddressType.REGISTER_ADDRESSING -> {
                if (operandSize == DOUBLE_SIZE) setRegister(reg, result);
                else setRegister(reg, operandSize, (int) result);
            }
            case AddressType.RELATIVE_ADDRESSING_WITH_ZERO ->
                setMemory(regValue, operandSize, result);
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
            case AddressType.RELATIVE_ADDRESSING_WITH_BYTE -> {
                int a = getNextMemory(BYTE_SIZE);
                setMemory(a + regValue, operandSize, result);
            }
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
            case AddressType.INDEXED_ADDRESSING -> {
                int address = computeIndexedAddressing(reg, operandSize);
                setMemory(address, operandSize, result);
            }
            default -> {
                //TODO: ERROR here
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
            case CMP_F -> cmp_F();
            case CMP_D -> cmp_D();

            case CLEAR_B -> clear(BYTE_SIZE);
            case CLEAR_H -> clear(HALFWORD_SIZE);
            case CLEAR_W -> clear(WORD_SIZE);
            case CLEAR_F -> clear(FLOAT_SIZE);
            case CLEAR_D -> clear(DOUBLE_SIZE);

            case MOVE_B -> move(BYTE_SIZE);
            case MOVE_H -> move(HALFWORD_SIZE);
            case MOVE_W, MOVE_F -> move(WORD_SIZE);
            case MOVE_D -> move(DOUBLE_SIZE);

            case MOVEN_B -> moven_I(BYTE_SIZE);
            case MOVEN_H -> moven_I(HALFWORD_SIZE);
            case MOVEN_W -> moven_I(WORD_SIZE);
            case MOVEN_F -> moven_F();
            case MOVEN_D -> moven_D();
            case MOVEC_B -> movec(BYTE_SIZE);
            case MOVEC_H -> movec(HALFWORD_SIZE);
            case MOVEC_W -> movec(WORD_SIZE);

            case MOVEA -> movea();
            case CONV -> conv();

            case OR_B2 -> arithmeticOperation(BYTE_SIZE, Operation.OR, true);
            case OR_H2 -> arithmeticOperation(HALFWORD_SIZE, Operation.OR, true);
            case OR_W2 -> arithmeticOperation(WORD_SIZE, Operation.OR, true);
            case OR_B3 -> arithmeticOperation(BYTE_SIZE, Operation.OR, false);
            case OR_H3 -> arithmeticOperation(HALFWORD_SIZE, Operation.OR, false);
            case OR_W3 -> arithmeticOperation(WORD_SIZE, Operation.OR, false);

            case ANDNOT_B2 -> arithmeticOperation(BYTE_SIZE, Operation.ANDNOT, true);
            case ANDNOT_H2 -> arithmeticOperation(HALFWORD_SIZE, Operation.ANDNOT, true);
            case ANDNOT_W2 -> arithmeticOperation(WORD_SIZE, Operation.ANDNOT, true);

            case ANDNOT_B3 -> arithmeticOperation(BYTE_SIZE, Operation.ANDNOT, false);
            case ANDNOT_H3 -> arithmeticOperation(HALFWORD_SIZE, Operation.ANDNOT, false);
            case ANDNOT_W3 -> arithmeticOperation(WORD_SIZE, Operation.ANDNOT, false);

            case XOR_B2 -> arithmeticOperation(BYTE_SIZE, Operation.XOR, true);
            case XOR_H2 -> arithmeticOperation(HALFWORD_SIZE, Operation.XOR, true);
            case XOR_W2 -> arithmeticOperation(WORD_SIZE, Operation.XOR, true);
            case XOR_B3 -> arithmeticOperation(BYTE_SIZE, Operation.XOR, false);
            case XOR_H3 -> arithmeticOperation(HALFWORD_SIZE, Operation.XOR, false);
            case XOR_W3 -> arithmeticOperation(WORD_SIZE, Operation.XOR, false);

            case ADD_B2 -> arithmeticOperation(BYTE_SIZE, Operation.ADD, true);
            case ADD_H2 -> arithmeticOperation(HALFWORD_SIZE, Operation.ADD, true);
            case ADD_W2 -> arithmeticOperation(WORD_SIZE, Operation.ADD, true);
            case ADD_F2 -> arithmeticOperationOnFloat(Operation.ADD, true);
            case ADD_D2 -> arithmeticOperationOnDouble(Operation.ADD, true);

            case ADD_B3 -> arithmeticOperation(BYTE_SIZE, Operation.ADD, false);
            case ADD_H3 -> arithmeticOperation(HALFWORD_SIZE, Operation.ADD, false);
            case ADD_W3 -> arithmeticOperation(WORD_SIZE, Operation.ADD, false);
            case ADD_F3 -> arithmeticOperationOnFloat(Operation.ADD, false);
            case ADD_D3 -> arithmeticOperationOnDouble(Operation.ADD, false);

            case SUB_B2 -> arithmeticOperation(BYTE_SIZE, Operation.SUB, true);
            case SUB_H2 -> arithmeticOperation(HALFWORD_SIZE, Operation.SUB, true);
            case SUB_W2 -> arithmeticOperation(WORD_SIZE, Operation.SUB, true);
            case SUB_F2 -> arithmeticOperationOnFloat(Operation.SUB, true);
            case SUB_D2 -> arithmeticOperationOnDouble(Operation.SUB, true);

            case SUB_B3 -> arithmeticOperation(BYTE_SIZE, Operation.SUB, false);
            case SUB_H3 -> arithmeticOperation(HALFWORD_SIZE, Operation.SUB, false);
            case SUB_W3 -> arithmeticOperation(WORD_SIZE, Operation.SUB, false);
            case SUB_F3 -> arithmeticOperationOnFloat(Operation.SUB, false);
            case SUB_D3 -> arithmeticOperationOnDouble(Operation.SUB, false);

            case MULT_B2 -> arithmeticOperation(BYTE_SIZE, Operation.MULT, true);
            case MULT_H2 -> arithmeticOperation(HALFWORD_SIZE, Operation.MULT, true);
            case MULT_W2 -> arithmeticOperation(WORD_SIZE, Operation.MULT, true);
            case MULT_F2 -> arithmeticOperationOnFloat(Operation.MULT, true);
            case MULT_D2 -> arithmeticOperationOnDouble(Operation.MULT, true);

            case MULT_B3 -> arithmeticOperation(BYTE_SIZE, Operation.MULT, false);
            case MULT_H3 -> arithmeticOperation(HALFWORD_SIZE, Operation.MULT, false);
            case MULT_W3 -> arithmeticOperation(WORD_SIZE, Operation.MULT, false);
            case MULT_F3 -> arithmeticOperationOnFloat(Operation.MULT, false);
            case MULT_D3 -> arithmeticOperationOnDouble(Operation.MULT, false);

            case DIV_B2 -> arithmeticOperation(BYTE_SIZE, Operation.DIV, true);
            case DIV_H2 -> arithmeticOperation(HALFWORD_SIZE, Operation.DIV, true);
            case DIV_W2 -> arithmeticOperation(WORD_SIZE, Operation.DIV, true);
            case DIV_F2 -> arithmeticOperationOnFloat(Operation.DIV, true);
            case DIV_D2 -> arithmeticOperationOnDouble(Operation.DIV, true);

            case DIV_B3 -> arithmeticOperation(BYTE_SIZE, Operation.DIV, false);
            case DIV_H3 -> arithmeticOperation(HALFWORD_SIZE, Operation.DIV, false);
            case DIV_W3 -> arithmeticOperation(WORD_SIZE, Operation.DIV, false);
            case DIV_F3 -> arithmeticOperationOnFloat(Operation.DIV, false);
            case DIV_D3 -> arithmeticOperationOnDouble(Operation.DIV, false);

            case SH -> sh();
            case ROT -> rot();

            case JEQ, JNE, JGT, JGE, JLT, JLE,
                    JC, JNC, JV, JNV -> jumpOnCondition(op);

            case JUMP -> jump();
            case CALL -> call();
            case RET -> ret();
            case PUSHR -> pushr();
            case POPR -> popr();
        }
    }

    public void setBreakpoints(int[] breakpointLines) {
        this.breakpointAddresses = new int[breakpointLines.length];
        int i = 0;
        for (int breakpointLine : breakpointLines) {
            //TODO: Currently this would break if a breakpoint is set on something other than a Command.
            //      To prevent this, we should prevent setting Breakpoints on empty lines or EQU lines.
            this.breakpointAddresses[i++] = lineNumberToAddress.get(breakpointLine);
        }
    }

    public int getLineNumberOfNextCommand() {
        //In case the Program gets modified mid run, this won't find anything.
        Integer lineNumber = addressToLineNumber.get(getPC());

        if (lineNumber == null) return 1;
        else return lineNumber;
    }

    public void run() {
        while (!programHaltet) {
            if (justBreaked) justBreaked = false;
            else {
                // Note: binarySearch expects the array to be sorted, which should always be the case here
                boolean breakPointFound = Arrays.binarySearch(breakpointAddresses, getPC()) >= 0;
                if (breakPointFound) {
                    justBreaked = true;
                    return;
                }
            }
            executeOneInstruction();
        }
    }

    public void step() {
        justBreaked = false;
        executeOneInstruction();
    }

    public void halt() {
        programHaltet = true;
    }

    public void move(int size) {
        long a1 = getNextOperand(size);

        overflow = false;
        setZero(a1, size);
        setNegative(a1, size);

        saveResult(a1, size);
    }

    private void cmp_I(int size) {
        long a1 = getNextOperandWithSign(size);
        long a2 = getNextOperandWithSign(size);

        zero = (a1 == a2);
        negative = (a1 < a2);
    }

    private void cmp_F() {
        float a1 = getNextOperandAsFloat();
        float a2 = getNextOperandAsFloat();

        zero = (a1 == a2);
        negative = (a1 < a2);
    }

    private void cmp_D() {
        double a1 = getNextOperandAsDouble();
        double a2 = getNextOperandAsDouble();

        zero = (a1 == a2);
        negative = (a1 < a2);
    }

    private void clear(int size) {
        overflow = false;
        zero = true;
        negative = false;
        saveResult(0, size);
    }

    private void moven_I(int size) {
        long result = - getNextOperandWithSign(size);

        carry = false;
        setOverflow(result, size);
        setZero(result, size);
        setNegative(result, size);

        saveResult(result, size);
    }

    private void moven_F() {
        float result = - getNextOperandAsFloat();

        carry = false;
        overflow = false;
        setZero(result);
        setNegative(result);

        saveResult(result);
    }

    private void moven_D() {
        double result = - getNextOperandAsDouble();

        carry = false;
        overflow = false;
        setZero(result);
        setNegative(result);

        saveResult(result);
    }

    private void movec(int size) {
        long a1 = ~ getNextOperand(size);

        overflow = false;
        setZero(a1, size);
        setNegative(a1, size);

        saveResult(a1, size);
    }

    private void movea() {
        int address = computeNextAddress(WORD_SIZE);

        overflow = false;
        setZero(address, WORD_SIZE);
        setNegative(address, WORD_SIZE);

        saveResult(address, WORD_SIZE);
    }

    private void conv() {
        int a1 = (int) getNextOperandWithSign(BYTE_SIZE);

        carry = false;
        overflow = false;
        setZero(a1, WORD_SIZE);
        setNegative(a1, WORD_SIZE);

        saveResult(a1, WORD_SIZE);
    }

    private void arithmeticOperation(int size, Operation op, boolean twoOperands) {
        //TODO: getNextOperand currently returns the number sign extended,
        // but i think we need to use the raw bytes of the numbers here

        long a1 = getNextOperand(size);

        int addressOfSecondOperand = getPC();
        long a2 = getNextOperand(size);

        long result = switch (op) {
            case OR -> a1 | a2;
            case ANDNOT -> ~ a1 & a2;
            case XOR -> a1 ^ a2;
            case ADD -> a1 + a2;
            case SUB -> a2 - a1;
            case MULT -> a1 * a2;
            case DIV -> a2 / a1;
        };

        switch (op) {
            case OR, ANDNOT, XOR -> setOrAndnotXorFlags(result, size);
            case ADD, SUB -> setAddSubFlags(result, size);
            case MULT, DIV -> setMultDivFlags(result, size);
        }

        if (twoOperands) setPC(addressOfSecondOperand);
        saveResult((int) result, size);
    }

    private void arithmeticOperationOnFloat(Operation op, boolean twoOperands) {
        //TODO: Check if op is only add, sub, mult or div

        float a1 = getNextOperandAsFloat();

        int addressOfSecondOperand = getPC();
        float a2 = getNextOperandAsFloat();

        float result = switch (op) {
            case ADD -> a1 + a2;
            case SUB -> a2 - a1;
            case MULT -> a1 * a2;
            case DIV -> a2 / a1;
            default -> 0;
        };
        setFlags(result);

        if (twoOperands) setPC(addressOfSecondOperand);
        saveResult(result);
    }

    private void arithmeticOperationOnDouble(Operation op, boolean twoOperands) {
        //TODO: Check if op is only add, sub, mult or div

        double a1 = getNextOperandAsDouble();

        int addressOfSecondOperand = getPC();
        double a2 = getNextOperandAsDouble();

        double result = switch (op) {
            case ADD -> a1 + a2;
            case SUB -> a2 - a1;
            case MULT -> a1 * a2;
            case DIV -> a2 / a1;
            default -> 0;
        };
        setFlags(result);

        if (twoOperands) setPC(addressOfSecondOperand);
        saveResult(result);
    }

    private void sh() {
        int a1 = (int) getNextOperand(WORD_SIZE);
        int a2 = (int) getNextOperand(WORD_SIZE);
        int result;

        if (a1 >= 0) result = a2 << a1;
        else result = a2 >> -a1;

        carry = false;
        //TODO: Set overflow with IOVL (integer overflow)
        // for that, i yet have to figure out how IOVL works
        setZero(result);
        setNegative(result);

        saveResult(result, WORD_SIZE);
    }

    private void rot() {
        int a1 = (int) getNextOperand(WORD_SIZE);
        int a2 = (int) getNextOperand(WORD_SIZE);
        int result;

        if (a1 >= 0) result = Integer.rotateLeft(a2, a1);
        else result = Integer.rotateRight(a2, -a1);


        overflow = false;
        setZero(result);
        setNegative(result);

        saveResult(result, WORD_SIZE);
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

    private void pushr() {
        for (int i = 14; i >= 0; i--) {
            setSP(getSP() - 4);
            setMemory(getSP(), 4, getRegister(i));
        }
    }

    private void popr() {
        for (int i = 0; i <= 14; i++) {
            int regValue = getMemory(getSP(), WORD_SIZE);
            setRegister(i, WORD_SIZE, regValue);

            setSP(getSP() + 4);
        }
    }

    private void jumpOnCondition(OpCode op) {
        boolean cond = switch (op) {
            case JEQ -> zero;
            case JNE -> !zero;
            case JGT -> !(negative | zero);
            case JGE -> !negative;
            case JLT -> negative;
            case JLE -> (negative | zero);
            case JC -> carry;
            case JNC -> !carry;
            case JV -> overflow;
            case JNV -> !overflow;
            default -> false;
        };

        int address = computeNextAddress(WORD_SIZE);
        if (cond) setPC(address, WORD_SIZE);
    }

    private void setOrAndnotXorFlags(long result, int size) {
        overflow = false;
        setZero(result, size);
        setNegative(result, size);
    }

    private void setAddSubFlags(long result, int size) {
        setCarry(result, size);
        setOverflow(result, size);
        setZero(result, size);
        setNegative(result, size);
    }

    private void setFlags(double result) {
        carry = false;
        setOverflow(result);
        setZero(result);
        setNegative(result);
    }

    private void setMultDivFlags(long result, int size) {
        carry = false;
        setOverflow(result, size);
        setZero(result, size);
        setNegative(result, size);
    }
}