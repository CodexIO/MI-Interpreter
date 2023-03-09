package IntegrationTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ShTests extends TestUtility {

    @BeforeEach
    public void init() {
        vm.reset();
    }

    @Test
    public void SH_test() {
        String code = "SH I 5, I 2, R0 SH I -4, I -32, R1";

        vm.run(code);

        assertRegisterEquals(2 << 5, 0);
        assertRegisterEquals(-2, 1);
    }

}
