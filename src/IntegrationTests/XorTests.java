package IntegrationTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class XorTests extends TestUtility {

    @BeforeEach
    public void init() {
        vm.reset();
    }

    @Test
    public void XOR_B2_test() {
        String code = "MOVE B I 5, R0 XOR B I 6, R0";

        vm.run(code);

        assertRegisterEquals(3, 0);
    }

    @Test
    public void XOR_B3_test() {
        String code = "MOVE B I 5, R0 XOR B I 6, R0, R0";

        vm.run(code);

        assertRegisterEquals(3, 0);
    }

    @Test
    public void XOR_H2_test() {
        String code = "MOVE H I 5, R0 XOR H I 6, R0";

        vm.run(code);

        assertRegisterEquals(3, 0);
    }

    @Test
    public void XOR_H3_test() {
        String code = "MOVE H I 5, R0 XOR H I 6, R0, R0";

        vm.run(code);

        assertRegisterEquals(3, 0);
    }

    @Test
    public void XOR_W2_test() {
        String code = "MOVE W I 5, R0 XOR W I 6, R0";

        vm.run(code);

        assertRegisterEquals(3, 0);
    }

    @Test
    public void XOR_W3_test() {
        String code = "MOVE W I 5, R0 XOR W I 6, R0, R0";

        vm.run(code);

        assertRegisterEquals(3, 0);
    }

}