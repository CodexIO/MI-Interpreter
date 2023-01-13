package Assembler.Interpreter;

public class VMmain {

    public static void printMemory(VirtualMachine vm) {
        for (int i = 0; i <= 100; i++) {
            System.out.println("[" + i + "] = " + Integer.toHexString(vm.memory[i] & 0xFF));
        }
    }

    public static void main(String[] args) {
        VirtualMachine vm = new VirtualMachine(InterpreterTests.TEST1);
        vm.registers[0] = 2;
        vm.registers[1] = 3;

        vm.run();

        System.out.println("Register R0 = " + vm.registers[0]);
        System.out.println("Register R1 = " + vm.registers[1]);
        System.out.println("Register R2 = " + vm.registers[2]);

        printMemory(vm);

    }
}
