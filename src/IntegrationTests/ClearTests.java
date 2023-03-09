package IntegrationTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClearTests extends TestUtility {

    @BeforeEach
    public void init() {
        vm.reset();
    }

    @Test
    public void CLEAR_B2_test() {
        String code = "MOVE B I -1, R0 CLEAR B R0";

        vm.run(code);

        assertRegisterEquals(0, 0);
    }

    @Test
    public void CLEAR_H2_test() {
        String code = "MOVE H I -1, R0 CLEAR H R0";

        vm.run(code);

        assertRegisterEquals(0, 0);
    }

    @Test
    public void CLEAR_W2_test() {
        String code = "MOVE W I -1, R0 CLEAR W R0";

        vm.run(code);

        assertRegisterEquals(0, 0);
    }

    @Test
    public void CLEAR_F2_test() {
        String code = "MOVE F I -1, R0 CLEAR F R0";

        vm.run(code);

        assertRegisterEquals(0f, 0);
    }

    @Test
    public void CLEAR_D2_test() {
        String code = "MOVE D I -1, R0 CLEAR D R0";

        vm.run(code);

        assertRegisterEquals(0d, 0);
    }

}