package IntegrationTests;

import Interpreter.VirtualMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;



public class CmpTests {
    VirtualMachine vm = new VirtualMachine();

    @BeforeEach
    public void init() {
        vm.reset();
    }

    @Test
    public void cmp_B2_test() {
        String code = "CMP B I 5, I 5";

        vm.run(code);
        assertTrue(vm.zero);

        vm.run(" CMP B I -5, I 5");
        assertTrue(vm.negative);
    }

    @Test
    public void cmp_F_test() {
        String code = "CMP F I 4.5, I 4.5";

        vm.run(code);
        assertTrue(vm.zero);

        vm.run("CMP F I 3.4, I 4.0");
        assertTrue(vm.negative);
    }

    @Test
    public void cmp_D_test() {
        String code = "CMP D I 4.5, I 4.5";

        vm.run(code);
        assertTrue(vm.zero);

        vm.run("CMP D I 3.4, I 4.0");
        assertTrue(vm.negative);
    }

}
