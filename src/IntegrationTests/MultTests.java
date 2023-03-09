package IntegrationTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MultTests extends TestUtility {

    @BeforeEach
    public void init() {
        vm.reset();
    }

    @Test
    public void MULT_B2_test() {
        String code = "MOVE B I 5, R0 MULT B I 4, R0";

        vm.run(code);

        assertRegisterEquals(20, 0);
    }

    @Test
    public void MULT_B3_test() {
        String code = "MOVE B I 5, R0 MULT B I 4, R0, R0";

        vm.run(code);

        assertRegisterEquals(20, 0);
    }

    @Test
    public void MULT_H2_test() {
        String code = "MOVE H I 5, R0 MULT H I 4, R0";

        vm.run(code);

        assertRegisterEquals(20, 0);
    }

    @Test
    public void MULT_H3_test() {
        String code = "MOVE H I 5, R0 MULT H I 4, R0, R0";

        vm.run(code);

        assertRegisterEquals(20, 0);
    }

    @Test
    public void MULT_W2_test() {
        String code = "MOVE W I 5, R0 MULT W I 4, R0";

        vm.run(code);

        assertRegisterEquals(20, 0);
    }

    @Test
    public void MULT_W3_test() {
        String code = "MOVE W I 5, R0 MULT W I 4, R0, R0";

        vm.run(code);

        assertRegisterEquals(20, 0);
    }

    @Test
    public void MULT_F2_test() {
        String code = "MOVE F I 5, R0 MULT F I 4, R0";

        vm.run(code);

        assertRegisterEquals(20f, 0);
    }

    @Test
    public void MULT_F3_test() {
        String code = "MOVE F I 5, R0 MULT F I 4, R0, R0";

        vm.run(code);

        assertRegisterEquals(20f, 0);
    }

    @Test
    public void MULT_D2_test() {
        String code = "MOVE D I 5, R0 MULT D I 4, R0";

        vm.run(code);

        assertRegisterEquals(20d, 0);
    }

    @Test
    public void MULT_D3_test() {
        String code = "MOVE D I 5, R0 MULT D I 4, R0, R0";

        vm.run(code);

        assertRegisterEquals(20d, 0);
    }

}