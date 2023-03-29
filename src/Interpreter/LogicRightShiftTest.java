package Interpreter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LogicRightShiftTest {

    //@Test
    public void logicRightShiftTest() {
        byte b = -0x80;
        int c = b >>> 1;

        // This won't work
        assertEquals(64, c);
    }

}