package IntegrationTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ConvTests extends TestUtility {

    @BeforeEach
    public void init() {
        vm.reset();
    }

    @Test
    public void CONV_test() {
        String code = "CONV I -5, R0 CONV I -128, R1";

        vm.run(code);

        assertRegisterEquals(-5, 0);
        assertRegisterEquals(-128, 1);
    }

}
