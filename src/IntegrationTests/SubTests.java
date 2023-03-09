package IntegrationTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SubTests extends TestUtility {

    @BeforeEach
    public void init() {
        vm.reset();
    }

    @Test
    public void SUB_B2_test() {
        String code = "SUB B I -1, R0";

        vm.run(code);

        assertRegisterEquals(1, 0);
    }

    @Test
    public void SUB_B3_test() {
        String code = "SUB B I -1, R0, R0";

        vm.run(code);

        assertRegisterEquals(1, 0);
    }

    @Test
    public void SUB_H2_test() {
        String code = "SUB H I -1, R0";

        vm.run(code);

        assertRegisterEquals(1, 0);
    }

    @Test
    public void SUB_H3_test() {
        String code = "SUB H I -1, R0, R0";

        vm.run(code);

        assertRegisterEquals(1, 0);
    }

    @Test
    public void SUB_W2_test() {
        String code = "SUB W I -1, R0";

        vm.run(code);

        assertRegisterEquals(1, 0);
    }

    @Test
    public void SUB_W3_test() {
        String code = "SUB W I -1, R0, R0";

        vm.run(code);

        assertRegisterEquals(1, 0);
    }

    @Test
    public void SUB_F2_test() {
        String code = "SUB F I -3.141, R0";

        vm.run(code);

        assertRegisterEquals(3.141f, 0);
    }

    @Test
    public void SUB_F3_test() {
        String code = "SUB F I -3.141, R0, R0";

        vm.run(code);

        assertRegisterEquals(3.141f, 0);
    }

    @Test
    public void SUB_D2_test() {
        String code = "SUB D I -3.141, R0";

        vm.run(code);

        assertRegisterEquals(3.141d, 0);
    }

    @Test
    public void SUB_D3_test() {
        String code = "SUB D I -3.141, R0, R0";

        vm.run(code);

        assertRegisterEquals(3.141d, 0);
    }

}