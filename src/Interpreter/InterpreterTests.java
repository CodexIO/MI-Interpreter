package Interpreter;

import Assembler.OpCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static Assembler.OpCode.*;
import static org.junit.jupiter.api.Assertions.*;

public class InterpreterTests {

    public static final byte[] REGISTER_ADD = {
            (byte) ADD_B3.opcode, 0x50, 0x51, 0x52
    };

    public static final byte[] DIRECT_OPERAND_ADD = {
            (byte) ADD_B2.opcode, 0x06, 0x50,
            (byte) ADD_W2.opcode, (byte) 0x8F, 0x0F, 0x00, (byte)0xFF, 0x00, 0x51
    };

    public static final byte[] ABSOLUTE_ADDRESS_MOVE = {
            (byte) MOVE_B.opcode, (byte) 0x9F, 0x00, 0x00, 0x00, 0x00, (byte) 0x9F, 0x00, 0x00, 0x00, 0x70
    };

    public static final byte[] RELATIVE_ADDRESS_WITH_ZERO_MOVE = {
            (byte) MOVE_B.opcode, (byte) 0x8F, (byte) MOVE_B.opcode, 0x60,
            -1/* This number will be overwritten*/, 0x61, 0x52
    };

    public static final byte[] TEST1 = {
            (byte)0xC4, 0x50, 0x51, 0x52,
            (byte)0xBF, 0x52, 0x50,
            (byte)0xC9, 0x51, 0x52,
            (byte)0x9E, 0x0F, (byte)0x9F, 0x00, 0x00, 0x00, 0x40,};

    VirtualMachine vm;


    @BeforeEach
    public void init() {
        vm = new VirtualMachine(TEST1);
        vm.registers[0] = 2;
        vm.registers[1] = 3;
    }

    @Test
    public void simpleTest() {
        vm.run();

        assertEquals(7, vm.registers[0]);
        assertEquals(3, vm.registers[1]);
        assertEquals(2, vm.registers[2]);

        assertEquals(15, vm.getMemory()[0x40]);
    }

    @Test
    public void registerTest() {
        VirtualMachine vm = new VirtualMachine(REGISTER_ADD);
        int a = 1;
        int b = 4;
        vm.registers[0] = a;
        vm.registers[1] = b;

        vm.run();

        assertEquals(a + b, vm.registers[2]);
    }

    @Test
    public void absoluteAddressTest() {
        VirtualMachine vm = new VirtualMachine(ABSOLUTE_ADDRESS_MOVE);
        vm.run();

        assertEquals(OpCode.MOVE_B.opcode, vm.getMemory(0x70, 1) & 0xFF);
    }

    @Test
    public void directOperandTest() {
        VirtualMachine vm = new VirtualMachine(DIRECT_OPERAND_ADD);
        vm.run();

        assertEquals(6, vm.registers[0]);
        assertEquals(0x0F00FF00, vm.registers[1]);
    }

    @Test
    public void relativeAddressWithZeroTest() {
        VirtualMachine vm = new VirtualMachine(RELATIVE_ADDRESS_WITH_ZERO_MOVE);
        vm.registers[0] = 4;
        vm.run();

        assertEquals(MOVE_B.opcode, vm.getMemory(4, 1) & 0xFF);
        assertEquals(MOVE_B.opcode, vm.registers[2]);
    }
}
