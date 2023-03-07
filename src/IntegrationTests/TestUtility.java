package IntegrationTests;

import Interpreter.VirtualMachine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import static Interpreter.VirtualMachine.WORD_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestUtility {
    VirtualMachine vm = new VirtualMachine();

    static final String PATH = "src/IntegrationTests/programs/";

    public static String readTestFile(String filename) {
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

    protected void assertRegisterEquals(int expected, int reg) {
        assertEquals(expected, vm.getRegister(reg, WORD_SIZE));
    }

    protected void assertRegisterEquals(float expected, int reg) {
        assertEquals(expected, vm.getRegisterAsFloat(reg));
    }

    protected void assertRegisterEquals(double expected, int reg) {
        assertEquals(expected, vm.getRegisterAsDouble(reg));
    }


}
