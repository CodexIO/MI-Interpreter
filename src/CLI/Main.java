package CLI;

import Interpreter.VirtualMachine;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    static VirtualMachine vm;
    static byte[] memory = new byte[VirtualMachine.MEMORY_LENGTH];
    static int[] registers = new int[16];

    private static String readFile(String filename) {
        File file = new File(filename);

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

    //TODO: Add printing after each executed Instruction
    public static void main(String[] args) {
        boolean fileSpecified = false;
        String fileInput = "";

        if (args.length == 0) {
            System.out.println("Please specify input state for the memory.");
            return;
        }
        for (int i = 0; i < args.length; i++) {
            String s = args[i];

            switch (s) {
                case "-m" -> {
                    String mem = args[++i];
                    String[] bytes = mem.split(" ");

                    int j = 0;
                    for (String b : bytes) {
                        memory[j++] = (byte) (Short.parseShort(b, 16));
                    }
                }
                case "-r" -> {
                    String regList = args[++i];
                    String[] regs = regList.split(" ");

                    int j = 0;
                    for (String r : regs) {
                        registers[j++] = Integer.parseInt(r, 16);
                    }
                }
                case "-f" -> {
                    fileInput = readFile(args[++i]);
                    fileSpecified = true;
                }
            }
        }

        if (fileSpecified) {
            System.out.println(fileInput);

            vm = new VirtualMachine(fileInput);
            vm.run();
            System.out.println("Final State: \n" + vm);
        }
        else {
            vm = new VirtualMachine(memory, registers);

            System.out.println("Initial State: \n" + vm);

            vm.run();

            System.out.println("Final State: \n" + vm);
        }
    }
}