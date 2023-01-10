package Assembler.Interpreter;

public class VMmain {

    public static void main(String[] args) {
        VirtualMachine vm = new VirtualMachine(VirtualMachine.TEST1);
        vm.registers[0] = 2;
        vm.registers[1] = 3;

        vm.run();

        System.out.println("Register R0 = " + vm.registers[0]);
        System.out.println("Register R1 = " + vm.registers[1]);
        System.out.println("Register R2 = " + vm.registers[2]);
    }
}
