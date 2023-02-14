package Interpreter;

public class VMmain {

    public static void main(String[] args) {
        VirtualMachine vm = new VirtualMachine(InterpreterTests.TEST1);
        vm.registers[0] = 2;
        vm.registers[1] = 3;

        for (int i = 1; i <= 4; i++) {
            int n = i * 8;
            long maxValue = (2L << (i * 8 - 2)) - 1; // 2^(n-1) - 1
            long minValue = - (2L << (i * 8 - 2));

            System.out.println("MAX: " + maxValue + " MIN: " + minValue);
        }

    }
}
