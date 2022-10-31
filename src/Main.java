import Assembler.Lexer;
import Assembler.Token;

public class Main {

    public static void main(String[] args) {
        Lexer lx = new Lexer("a:  DD W 1\ns:  MOVEA a, R5");

        Token tk = lx.nextToken();

        while (tk.type != Token.Type.UNKNOWN) {
            System.out.println("Type: " + tk.type + " lexeme: \"" + tk.lexeme + "\"");
            tk = lx.nextToken();
        }
    }

}
