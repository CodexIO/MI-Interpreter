package IntegrationTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AndnotTests extends TestUtility {

    @BeforeEach
    public void init() {
        vm.reset();
    }

@Test
public void ANDNOT_B2_test() {
    String code = "MOVE B I -1, R0 ANDNOT B I H'0F', R0";

    vm.run(code);

    assertRegisterEquals((0xFFFF_FFFF >>> 24) & 0xFFFF_FFF0, 0);
}

@Test
public void ANDNOT_B3_test() {
    String code = "MOVE B I -1, R0 ANDNOT B I H'0F', R0, R0";

    vm.run(code);

    assertRegisterEquals((0xFFFF_FFFF >>> 24) & 0xFFFF_FFF0, 0);
}

@Test
public void ANDNOT_H2_test() {
    String code = "MOVE H I -1, R0 ANDNOT H I H'0F', R0";

    vm.run(code);

    assertRegisterEquals((0xFFFF_FFFF >>> 16) & 0xFFFF_FFF0, 0);
}

@Test
public void ANDNOT_H3_test() {
    String code = "MOVE H I -1, R0 ANDNOT H I H'0F', R0, R0";

    vm.run(code);

    assertRegisterEquals((0xFFFF_FFFF >>> 16) & 0xFFFF_FFF0, 0);
}

@Test
public void ANDNOT_W2_test() {
    String code = "MOVE W I -1, R0 ANDNOT W I H'0F', R0";

    vm.run(code);

    assertRegisterEquals(0xFFFF_FFF0, 0);
}

@Test
public void ANDNOT_W3_test() {
    String code = "MOVE W I -1, R0 ANDNOT W I H'0F', R0, R0";

    vm.run(code);

    assertRegisterEquals(0xFFFF_FFF0, 0);
}

}