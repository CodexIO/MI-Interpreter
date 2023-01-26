import Assembler.Interpreter.VirtualMachine;
import Assembler.Lexer;
import Assembler.Parser;
import Assembler.Token;

public class Main {

    public static void main(String[] args) {
        Lexer lx = new Lexer("ADD B I 1, I 2, R0\n ADD B I 10, I 0, 7 + !R0");

        Parser parser = new Parser(lx);

        parser.parse();
        var machineCode = parser.generateMachineCode();

        System.out.println(machineCode);

        byte[] bytes = new byte[machineCode.size()];
        int i = 0;
        for (Byte b : machineCode) {
            bytes[i++] = b;
        }

        VirtualMachine vm = new VirtualMachine(bytes);
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
