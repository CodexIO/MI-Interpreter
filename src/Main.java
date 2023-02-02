import Assembler.Interpreter.VirtualMachine;
import Assembler.Lexer;
import Assembler.Parser;

public class Main {

    public static void main(String[] args) {
        String input = """
                SUB B I 5, I 10, R1
                ADD B I 1, I 2, R0
                ADD B I 10, I 0, 7 + !R0""";
        Lexer lx = new Lexer(input);

        Parser parser = new Parser(lx);

        parser.parse();
        byte[] machineCode = parser.generateMachineCode();

        StringBuilder sb = new StringBuilder();
        for(byte b : machineCode) {
            sb.append(String.format("%02X ", b & 0xFF));
        }
        System.out.println(sb);

        VirtualMachine vm = new VirtualMachine(machineCode);
        vm.run();
        System.out.println(vm);

        /*
        Token tk = lx.nextToken();

        while (tk.type != Token.Type.UNKNOWN) {
            System.out.println("Type: " + tk.type + " lexeme: \"" + tk.lexeme + "\"");
            tk = lx.nextToken();
        }*/
    }

}
