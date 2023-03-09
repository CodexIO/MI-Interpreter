package IntegrationTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MovenTests extends TestUtility {

    @BeforeEach
    public void init() {
        vm.reset();
    }

    @Test
    public void MOVEN_B2_test() {
        String code = "MOVEN B I -1, R0";

        vm.run(code);

        assertRegisterEquals(1, 0);
    }

    @Test
    public void MOVEN_H2_test() {
        String code = "MOVEN H I -1, R0";

        vm.run(code);

        assertRegisterEquals(1, 0);
    }

    @Test
    public void MOVEN_W2_test() {
        String code = "MOVEN W I -1, R0";

        vm.run(code);

        assertRegisterEquals(1, 0);
    }

    @Test
    public void MOVEN_F2_test() {
        String code = "MOVEN F I -1, R0";

        vm.run(code);

        assertRegisterEquals(1f, 0);
    }

    @Test
    public void MOVEN_D2_test() {
        String code = "MOVEN D I -1, R0";

        vm.run(code);

        assertRegisterEquals(1d, 0);
    }

}