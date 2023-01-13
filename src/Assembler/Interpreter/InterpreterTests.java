package Assembler.Interpreter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class InterpreterTests {

    public static final byte[] TEST1 = {
            (byte)0xC4, 0x50, 0x51, 0x52,
            (byte)0xBF, 0x52, 0x50,
            (byte)0xC9, 0x51, 0x52,
            (byte)0x9E, 0x0F, (byte)0x9F, 0x00, 0x00, 0x00, 0x40,};

    VirtualMachine vm;


    @BeforeEach
    public void init() {
        vm = new VirtualMachine(TEST1);
        vm.registers[0] = 2;
        vm.registers[1] = 3;
    }

    @Test
    public void simpleTest() {
        vm.run();

        assertEquals(7, vm.registers[0]);
        assertEquals(3, vm.registers[1]);
        assertEquals(2, vm.registers[2]);

        assertEquals(15, vm.memory[0x40]);
    }
}
