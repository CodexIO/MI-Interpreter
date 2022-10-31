package Assembler;

public class Token {

    public int row;
    public int col;
    public Type type;

    public static enum Type {
        COMMA,
        SEMICOLON,
        MINUS,
        PLUS,
        SLASH,
        BANG,
        COLON,
        KEYWORD,
        CONSTANT,
        Identifier
    }

    public Token(int r, int c, Type t) {
        row = r;
        col = c;
        type = t;
    }
}
