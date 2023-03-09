package IntegrationTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MovecTests extends TestUtility {

    @BeforeEach
    public void init() {
        vm.reset();
    }

    @Test
    public void MOVEC_B2_test() {
        String code = "MOVEC B I -1, R0";

        vm.run(code);

        assertRegisterEquals(0, 0);
    }

    @Test
    public void MOVEC_H2_test() {
        String code = "MOVEC H I -1, R0";

        vm.run(code);

        assertRegisterEquals(0, 0);
    }

    @Test
    public void MOVEC_W2_test() {
        String code = "MOVEC W I -1, R0";

        vm.run(code);

        assertRegisterEquals(0, 0);
    }

}