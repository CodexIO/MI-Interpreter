package Interpreter;

import Assembler.OpCode;
import IntegrationTests.TestUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static Assembler.OpCode.*;
import static org.junit.jupiter.api.Assertions.*;

public class InterpreterTests extends TestUtility {

    private static final byte[] REGISTER_ADD = {
            (byte) ADD_B3.code, 0x50, 0x51, 0x52
    };

    private static final byte[] DIRECT_OPERAND_ADD = {
            (byte) ADD_B2.code, 0x06, 0x50,
            (byte) ADD_W2.code, (byte) 0x8F, 0x0F, 0x00, (byte)0xFF, 0x00, 0x51
    };

    private static final byte[] ABSOLUTE_ADDRESS_MOVE = {
            (byte) MOVE_B.code, (byte) 0x9F, 0x00, 0x00, 0x00, 0x00, (byte) 0x9F, 0x00, 0x00, 0x00, 0x70
    };

    private static final byte[] RELATIVE_ADDRESS_WITH_ZERO_MOVE = {
            (byte) MOVE_B.code, (byte) 0x8F, (byte) MOVE_B.code, 0x60,
            -1/* This number will be overwritten*/, 0x61, 0x52
    };

    private static final byte[] TEST1 = {
            (byte)0xC4, 0x50, 0x51, 0x52,
            (byte)0xBF, 0x52, 0x50,
            (byte)0xC9, 0x51, 0x52,
            (byte)0x9E, 0x0F, (byte)0x9F, 0x00, 0x00, 0x00, 0x40,};


    @BeforeEach
    public void init() {
        vm.reset();
    }

    @Test
    public void simpleTest() {
        vm.setMemory(TEST1);

        vm.setRegister(0, 4, 2);
        vm.setRegister(1, 4, 3);

        vm.run();

        assertRegisterEquals(7, 0);
        assertRegisterEquals(3, 1);
        assertRegisterEquals(2, 2);

        assertEquals(15, vm.getMemory()[0x40]);
    }

    @Test
    public void registerTest() {
        vm = new VirtualMachine(REGISTER_ADD);
        int a = 1;
        int b = 4;
        vm.setRegister(0, 4, a);
        vm.setRegister(1, 4, b);

        vm.run();

        assertRegisterEquals(a + b, 2);
    }

    @Test
    public void absoluteAddressTest() {
        vm.setMemory(ABSOLUTE_ADDRESS_MOVE);
        vm.run();

        assertEquals(OpCode.MOVE_B.code, vm.getMemory(0x70, 1) & 0xFF);
    }

    @Test
    public void directOperandTest() {
        vm.setMemory(DIRECT_OPERAND_ADD);
        vm.run();

        assertRegisterEquals(6, 0);
        assertRegisterEquals(0x0F00FF00, 1);
    }

    @Test
    public void relativeAddressWithZeroTest() {
        vm.setMemory(RELATIVE_ADDRESS_WITH_ZERO_MOVE);
        vm.setRegister(0, 4, 4);

        vm.run();

        assertEquals(MOVE_B.code, vm.getMemory(4, 1) & 0xFF);
        assertRegisterEquals(MOVE_B.code, 2);
    }
}
