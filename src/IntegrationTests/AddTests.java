package IntegrationTests;

import Assembler.Parser;
import Interpreter.VirtualMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static Interpreter.VirtualMachine.*;
import static org.junit.jupiter.api.Assertions.*;


public class AddTests {
    VirtualMachine vm = new VirtualMachine();
    Parser parser;

    @BeforeEach
    public void init() {
        vm.reset();
    }

    @Test
    public void add_B2_test() {
        String code = "ADD B I 5, R0" + " ADD B I -5, R1";

        vm.run(code);

        assertEquals(5, vm.getRegister(0, BYTE_SIZE));
        assertEquals(-5, vm.getRegisterWithSign(1, BYTE_SIZE));
    }

    @Test
    public void add_B3_test() {
        String code = "ADD B I 5, I 5, R0" + " ADD B I -5, R0, R1";

        vm.run(code);

        assertEquals(10, vm.getRegister(0, BYTE_SIZE));
        assertEquals(5, vm.getRegister(1, BYTE_SIZE));
    }

    @Test
    public void add_flag_test() {
        String code = "JUMP start\n" +
                "\n" +
                "number: DD B 130\n" +
                "\n" +
                "start:\n" +
                "ADD B number, R0\n" +
                "ADD B I 127, number, R1";

        vm.run(code);

        assertTrue(vm.carry);
    }
}
