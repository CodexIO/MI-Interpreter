package IntegrationTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OrTests extends TestUtility {

    @BeforeEach
    public void init() {
        vm.reset();
    }

@Test
public void OR_B2_test() {
    String code = "MOVE B I 5, R0 OR B I 6, R0";

    vm.run(code);

    assertRegisterEquals(7, 0);
}

@Test
public void OR_B3_test() {
    String code = "MOVE B I 5, R0 OR B I 6, R0, R0";

    vm.run(code);

    assertRegisterEquals(7, 0);
}

@Test
public void OR_H2_test() {
    String code = "MOVE H I 5, R0 OR H I 6, R0";

    vm.run(code);

    assertRegisterEquals(7, 0);
}

@Test
public void OR_H3_test() {
    String code = "MOVE H I 5, R0 OR H I 6, R0, R0";

    vm.run(code);

    assertRegisterEquals(7, 0);
}

@Test
public void OR_W2_test() {
    String code = "MOVE W I 5, R0 OR W I 6, R0";

    vm.run(code);

    assertRegisterEquals(7, 0);
}

@Test
public void OR_W3_test() {
    String code = "MOVE W I 5, R0 OR W I 6, R0, R0";

    vm.run(code);

    assertRegisterEquals(7, 0);
}

}