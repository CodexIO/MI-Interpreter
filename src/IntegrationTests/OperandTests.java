package IntegrationTests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

public class OperandTests extends TestUtility {

    @BeforeEach
    public void init() {
        vm.reset();
    }

    @Test
    public void operandTest() {
        String code = readTestFile("everyOperand.mi");

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
