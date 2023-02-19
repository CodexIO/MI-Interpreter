package IntegrationTests;

import Assembler.Parser;
import Interpreter.VirtualMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static Interpreter.VirtualMachine.*;
import static org.junit.jupiter.api.Assertions.*;



public class CmpTests {
    VirtualMachine vm = new VirtualMachine();
    Parser parser;

    @BeforeEach
    public void init() {
        vm.reset();
    }

    @Test
    public void cmp_B2_test() {
        String code = "CMP B I 5, I 5";

        vm.run(code);
        assertTrue(vm.zero);

        vm.run(" CMP B I -5, I 5");
        assertTrue(vm.negative);
    }

}
