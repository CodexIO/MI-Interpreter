package CLI;

import Assembler.Interpreter.VirtualMachine;

public class Main {

    static VirtualMachine vm;
    static byte[] memory = new byte[VirtualMachine.MEMORY_LENGTH];
    static int[] registers = new int[16];

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please specify input state for the memory.");
            return;
        }
        for (int i = 0; i < args.length; i++) {
            String s = args[i];

            if (s.equals("-m")) {
                String mem = args[++i];
                String[] bytes = mem.split(" ");

                int j = 0;
                for (String b : bytes) {
                    memory[j++] = (byte) (Short.parseShort(b, 16));
                }
            }
            else if (s.equals("-r")) {
                String regList = args[++i];
                String[] regs = regList.split(" ");

                int j = 0;
                for (String r : regs) {
                    registers[j++] = Integer.parseInt(r, 16);
                }
            }
        }

        vm = new VirtualMachine(memory, registers);

        System.out.println("Initial State: \n" + vm);

        vm.run();

        System.out.println("Final State: \n" + vm);
    }
}
