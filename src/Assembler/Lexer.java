package Assembler;

import Assembler.Token.Type;

public class Lexer {

    public int row;
    public int col;

    public String input;
    public int index;

    private char nextChar() {
        if (index >= input.length()) return '\0';

        char c = input.charAt(index++);
        if (c == '\n') {
            row++;
            col = 0;
        } else {
            col++;
        }
        return c;
    }

    private Token newToken(Type type) {
        return new Token(row, col, type);
    }

    public Token nextToken() {
        char c = nextChar();

        //TODO is underscore allowed at the beginning of an Identifier
        if (Character.isAlphabetic(c)) {

        } else if (Character.isDigit(c)) {

        }

        switch (c) {
            case '+': return newToken(Type.PLUS);
            case '-': return newToken(Type.MINUS);
        }

        return null;
    }
}
