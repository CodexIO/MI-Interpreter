package IntegrationTests;

import Interpreter.VirtualMachine;
import org.junit.jupiter.api.Test;

import static Interpreter.VirtualMachine.*;
import static org.junit.Assert.*;

public class JumpTests {
    VirtualMachine vm = new VirtualMachine();

    @Test
    public void JC_Test() {
        String code = "ADD B I -1, I -1, R0 " +
                "JC a2 a1: MOVE B I 1, R0 a2: MOVE B I 1, R1";
        vm.run(code);

        assertNotEquals(1, vm.getRegisterWithSign(0, BYTE_SIZE));
        assertEquals(1, vm.getRegisterWithSign(1, BYTE_SIZE));
    }

    @Test
    public void JNC_Test() {
        String code = "JNC a2 a1: MOVE B I 1, R0 a2: MOVE B I 1, R1";
        vm.run(code);

        assertNotEquals(1, vm.getRegisterWithSign(0, BYTE_SIZE));
        assertEquals(1, vm.getRegisterWithSign(1, BYTE_SIZE));
    }

    @Test
    public void JV_Test() {
        String code = "ADD B I -128, I -128, R0 " +
                "JV a2 a1: MOVE B I 1, R0 a2: MOVE B I 1, R1";
        vm.run(code);

        assertNotEquals(1, vm.getRegisterWithSign(0, BYTE_SIZE));
        assertEquals(1, vm.getRegisterWithSign(1, BYTE_SIZE));
    }

    @Test
    public void JNV_Test() {
        String code = "JNV a2 a1: MOVE B I 1, R0 a2: MOVE B I 1, R1";
        vm.run(code);

        assertNotEquals(1, vm.getRegisterWithSign(0, BYTE_SIZE));
        assertEquals(1, vm.getRegisterWithSign(1, BYTE_SIZE));
    }

    @Test
    public void JEQ_Test() {
        String code = "MOVE B R0, R1 JEQ a2 MOVE B I 1, R0 a2: MOVE B I 1, R1";

        vm.run(code);

        assertNotEquals(1, vm.getRegisterWithSign(0, BYTE_SIZE));
        assertEquals(1, vm.getRegisterWithSign(1, BYTE_SIZE));
    }

    @Test
    public void JNE_Test() {
        String code = "MOVE B I 1, R1 JNE a2 MOVE B I 1, R0 a2: MOVE B I 1, R1";

        vm.run(code);

        assertNotEquals(1, vm.getRegisterWithSign(0, BYTE_SIZE));
        assertEquals(1, vm.getRegisterWithSign(1, BYTE_SIZE));
    }

    @Test
    public void JGT_Test() {
        String code = "MOVE B I 1, R1 JGT a2 MOVE B I 1, R0 a2: MOVE B I 1, R1";
        vm.run(code);

        assertNotEquals(1, vm.getRegisterWithSign(0, BYTE_SIZE));
        assertEquals(1, vm.getRegisterWithSign(1, BYTE_SIZE));

        code = "MOVE B I -1, R1 JGT a2 MOVE B I 1, R0 a2: MOVE B I 1, R1";
        vm.run(code);

        assertEquals(1, vm.getRegisterWithSign(0, BYTE_SIZE));
        assertEquals(1, vm.getRegisterWithSign(1, BYTE_SIZE));
    }

    @Test
    public void JGE_Test() {
        String code = "MOVE B I 1, R1 JGE a2 MOVE B I 1, R0 a2: MOVE B I 1, R1";
        vm.run(code);

        assertNotEquals(1, vm.getRegisterWithSign(0, BYTE_SIZE));
        assertEquals(1, vm.getRegisterWithSign(1, BYTE_SIZE));

        code = "MOVE B I 0, R1 JGE a2 MOVE B I 1, R0 a2: MOVE B I 1, R1";
        vm.run(code);

        assertNotEquals(1, vm.getRegisterWithSign(0, BYTE_SIZE));
        assertEquals(1, vm.getRegisterWithSign(1, BYTE_SIZE));
    }
}
