package Assembler;

public class Token {

    public int row;
    public int col;
    public Type type;
    public String lexeme;

    public enum Type {
        COMMA,
        MINUS,
        PLUS,
        SLASH,
        BANG,
        COLON,
        APOSTROPHE,
        OPEN_PAREN,
        CLOSE_PAREN,
        KEYWORD,
        CONSTANT,
        IDENTIFIER,
        UNKNOWN
    }

    public Token(int r, int c, String l, Type t) {
        row = r;
        col = c;
        type = t;
        lexeme = l;
    }
}
