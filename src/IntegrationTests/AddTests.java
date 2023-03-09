package IntegrationTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AddTests extends TestUtility {

    @BeforeEach
    public void init() {
        vm.reset();
    }

    @Test
    public void ADD_B2_test() {
        String code = "MOVE B I -5, R0 ADD B I 10, R0";

        vm.run(code);

        assertRegisterEquals(5, 0);
    }

    @Test
    public void ADD_B3_test() {
        String code = "MOVE B I -5, R0 ADD B I 10, R0, R0";

        vm.run(code);

        assertRegisterEquals(5, 0);
    }

    @Test
    public void ADD_H2_test() {
        String code = "MOVE H I -5, R0 ADD H I 10, R0";

        vm.run(code);

        assertRegisterEquals(5, 0);
    }

    @Test
    public void ADD_H3_test() {
        String code = "MOVE H I -5, R0 ADD H I 10, R0, R0";

        vm.run(code);

        assertRegisterEquals(5, 0);
    }

    @Test
    public void ADD_W2_test() {
        String code = "MOVE W I -5, R0 ADD W I 10, R0";

        vm.run(code);

        assertRegisterEquals(5, 0);
    }

    @Test
    public void ADD_W3_test() {
        String code = "MOVE W I -5, R0 ADD W I 10, R0, R0";

        vm.run(code);

        assertRegisterEquals(5, 0);
    }

    @Test
    public void ADD_F2_test() {
        String code = "MOVE F I -5, R0 ADD F I 10, R0";

        vm.run(code);

        assertRegisterEquals(5f, 0);
    }

    @Test
    public void ADD_F3_test() {
        String code = "MOVE F I -5, R0 ADD F I 10, R0, R0";

        vm.run(code);

        assertRegisterEquals(5f, 0);
    }

    @Test
    public void ADD_D2_test() {
        String code = "MOVE D I -5, R0 ADD D I 10, R0";

        vm.run(code);

        assertRegisterEquals(5d, 0);
    }

    @Test
    public void ADD_D3_test() {
        String code = "MOVE D I -5, R0 ADD D I 10, R0, R0";

        vm.run(code);

        assertRegisterEquals(5d, 0);
    }

    @Test
    public void ADD_FLAG_test() {
        String code = "ADD B I 120, I -5, R0";

        vm.run(code);

        assertTrue(vm.carry);
    }

}