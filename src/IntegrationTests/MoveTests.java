package IntegrationTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MoveTests extends TestUtility {

    @BeforeEach
    public void init() {
        vm.reset();
    }

    @Test
    public void MOVE_B2_test() {
        String code = "MOVE B I -1, R0";

        vm.run(code);

        assertRegisterEquals(0xFFFF_FFFF >>> 24, 0);
    }

    @Test
    public void MOVE_H2_test() {
        String code = "MOVE H I -1, R0";

        vm.run(code);

        assertRegisterEquals(0xFFFF_FFFF >>> 16, 0);
    }

    @Test
    public void MOVE_W2_test() {
        String code = "MOVE W I -1, R0";

        vm.run(code);

        assertRegisterEquals(0xFFFF_FFFF, 0);
    }

    @Test
    public void MOVE_F2_test() {
        String code = "MOVE F I -1, R0";

        vm.run(code);

        assertRegisterEquals(-1f, 0);
    }

    @Test
    public void MOVE_D2_test() {
        String code = "MOVE D I -1, R0";

        vm.run(code);

        assertRegisterEquals(-1d, 0);
    }

}