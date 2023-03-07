package IntegrationTests;

import ComplexProgramsTests.ComplexProgramsTest;
import Interpreter.VirtualMachine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static Interpreter.VirtualMachine.WORD_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OperandTests {
    VirtualMachine vm = new VirtualMachine();

    //TODO: Copied from ComplexProgramsTest, factor these together into a Parent Class
    private void assertRegisterEquals(int expected, int reg) {
        assertEquals(expected, vm.getRegister(reg, WORD_SIZE));
    }


    @BeforeEach
    public void init() {
        vm.reset();
    }

    @Test
    public void operandTest() {
        String code = ComplexProgramsTest.readTestFile("everyOperand.mi");

        vm.run(code);

        assertRegisterEquals(5, 0);
        assertRegisterEquals(6, 1);
        assertRegisterEquals(5, 2);
        assertRegisterEquals(5, 3);
        assertRegisterEquals(5, 4);
        assertRegisterEquals(3, 5);
        assertRegisterEquals(3, 6);
        assertRegisterEquals(8, 7);
        assertRegisterEquals(10000, 14);
    }

}
