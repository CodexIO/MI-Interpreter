package IntegrationTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RotTests extends TestUtility {

    @BeforeEach
    public void init() {
        vm.reset();
    }

    @Test
    public void ROT_test() {
        String code = "ROT I 4, I H'80000001', R0\n" +
                      "ROT I -4, I H'80000001', R1";

        vm.run(code);

        assertRegisterEquals(24, 0);
        assertRegisterEquals(0x1800_0000, 1);
    }

}
