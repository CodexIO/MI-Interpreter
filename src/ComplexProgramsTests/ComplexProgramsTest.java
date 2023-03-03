package ComplexProgramsTests;

import Assembler.Parser;
import Interpreter.VirtualMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import static Interpreter.VirtualMachine.WORD_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ComplexProgramsTest {
    VirtualMachine vm = new VirtualMachine();
    Parser parser;

    static final String PATH = "src/ComplexProgramsTests/programs/";

    private static String readTestFile(String filename) {
        File file = new File(PATH + filename);

        try (FileReader fr = new FileReader(file)){
            StringBuilder sb = new StringBuilder();
            String s1;

            // File reader
            BufferedReader br;

            // Buffered reader
            br = new BufferedReader(fr);

            // Initialize sl
            sb.append(br.readLine());

            // Take the input from the file
            while ((s1 = br.readLine()) != null) {
                sb.append("\n");
                sb.append(s1);
            }

            return sb.toString();
        } catch (Exception e) {
            System.err.println("File: " + file.getAbsolutePath() + " could not be found or opened.");
        }
        return "";
    }

    private void assertRegisterEquals(int expected, int reg) {
        assertEquals(expected, vm.getRegister(reg, WORD_SIZE));
    }

    private void runFile(String fileName) {
        String fileText = readTestFile(fileName);
        parser = new Parser(fileText);
        parser.parse();

        byte[] machineCode = parser.generateMachineCode();
        vm.setMemory(machineCode);
        vm.run();
    }

    @BeforeEach
    public void init() {
        vm.reset();
    }

    @Test
    public void ggtTest() {
        runFile("ggt.mi");

        assertRegisterEquals(138, 0);
    }

    @Test
    public void multTest() {
        runFile("mult.mi");

        assertRegisterEquals(0x12345000, 0);
        assertRegisterEquals(0x87654000, 1);
        assertRegisterEquals(0x09A0C962, 2);
        assertRegisterEquals(0xA4000000, 3);
        assertRegisterEquals(0x00001234, 4);
        assertRegisterEquals(0x00008765, 5);
        assertRegisterEquals(0x2A4F9000, 6);
        assertRegisterEquals(0x048D0000, 7);
        assertRegisterEquals(0x0000048D, 8);
        assertRegisterEquals(0x00004000, 9);
    }

    @Test
    public void ackTest() {
        runFile("ack.mi");

        assertRegisterEquals(65536, 14);
    }

    @Test
    public void arrayTest() {
        runFile("array.mi");

        assertRegisterEquals(65536, 14);
    }

    @Test
    public void baumTest() {
        runFile("baum.mi");

        assertRegisterEquals(1465, 0);
        assertRegisterEquals(245, 1);
        assertRegisterEquals(-999, 5);
        assertRegisterEquals(-999, 6);
        assertRegisterEquals(1, 7);
        assertRegisterEquals(65528, 14);
    }

    @Test
    public void bignatTest() {
        runFile("bignat.mi");

        assertRegisterEquals(242, 15);
    }

    @Test
    public void boolscheOperationsTest() {
        runFile("boolsche_operations.mi");

        assertRegisterEquals(0, 0);
        assertRegisterEquals(-1,1);
        assertRegisterEquals(0, 2);
        assertRegisterEquals(-20, 5);
    }

    @Test
    public void doublePiTest() {
        runFile("double_pi.mi");

        assertRegisterEquals(0x40091DE2, 0);
        assertRegisterEquals(0xC11428CB,  1);
        assertRegisterEquals(0xBF706680, 2);
        assertRegisterEquals(0xA4010668, 3);
        assertRegisterEquals(0x408F4800, 4);
        assertRegisterEquals(0x3FF00000, 6);
    }

    @Test
    public void floatPiTest() {
        runFile("float_pi.mi");

        assertEquals(0x40490FFF, 0);
    }

    @Test
    public void moveTest() {
        runFile("movetest.mi");

        assertRegisterEquals(-2147483196, 0);
    }

    @Test
    public void multOverflowTest() {
        runFile("multOverflow.mi");

        assertRegisterEquals(40, 1);
        assertTrue(vm.overflow);
    }

    @Test
    public void DoubleDataDefinitionTest() {
        runFile("doubleDataDefinition.mi");

        assertRegisterEquals(0x402E0000,0);
        assertRegisterEquals(0, 1);
    }

}
