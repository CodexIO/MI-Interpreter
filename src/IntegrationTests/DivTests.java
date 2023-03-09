package IntegrationTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DivTests extends TestUtility {

    @BeforeEach
    public void init() {
        vm.reset();
    }

@Test
public void DIV_B2_test() {
    String code = "MOVE B I 21, R0 DIV B I 3, R0";

    vm.run(code);

    assertRegisterEquals(7, 0);
}

@Test
public void DIV_B3_test() {
    String code = "MOVE B I 21, R0 DIV B I 3, R0, R0";

    vm.run(code);

    assertRegisterEquals(7, 0);
}

@Test
public void DIV_H2_test() {
    String code = "MOVE H I 21, R0 DIV H I 3, R0";

    vm.run(code);

    assertRegisterEquals(7, 0);
}

@Test
public void DIV_H3_test() {
    String code = "MOVE H I 21, R0 DIV H I 3, R0, R0";

    vm.run(code);

    assertRegisterEquals(7, 0);
}

@Test
public void DIV_W2_test() {
    String code = "MOVE W I 21, R0 DIV W I 3, R0";

    vm.run(code);

    assertRegisterEquals(7, 0);
}

@Test
public void DIV_W3_test() {
    String code = "MOVE W I 21, R0 DIV W I 3, R0, R0";

    vm.run(code);

    assertRegisterEquals(7, 0);
}

@Test
public void DIV_F2_test() {
    String code = "MOVE F I 21, R0 DIV F I 3, R0";

    vm.run(code);

    assertRegisterEquals(7f, 0);
}

@Test
public void DIV_F3_test() {
    String code = "MOVE F I 21, R0 DIV F I 3, R0, R0";

    vm.run(code);

    assertRegisterEquals(7f, 0);
}

@Test
public void DIV_D2_test() {
    String code = "MOVE D I 21, R0 DIV D I 3, R0";

    vm.run(code);

    assertRegisterEquals(7d, 0);
}

@Test
public void DIV_D3_test() {
    String code = "MOVE D I 21, R0 DIV D I 3, R0, R0";

    vm.run(code);

    assertRegisterEquals(7d, 0);
}

}