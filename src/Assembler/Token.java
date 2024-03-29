package Assembler;

public class Token {

    public int row;
    public int col;
    public Type type;
    public String lexeme;

    public enum Type {
        I,
        B,
        H,
        REGISTER,
        COMMA,
        POINT,
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
        SEG,
        END,
        UNKNOWN, //@Cleanup right now this is used for End of File and unknown Tokens, separate these
        REPLACED_BY_EQUAL,
    }

    public Token(int r, int c, String l, Type t) {
        row = r;
        col = c - l.length();
        type = t;
        lexeme = l;
    }

    public int colEnd() {
        return row + lexeme.length();
    }
}
