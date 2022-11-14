package Assembler;

import Assembler.Token.Type;

public class Lexer {

    private int row = 1;
    private int col = 1;

    private final String source;    // Input String to Lex
    private int index = 0;       // Indexes the current position in the String
    private int start = 0;       // Marks the start position of the current Token

    public Lexer(String i) {
        source = i + "\0";
    }

    private void advance() {
        if (source.charAt(index) == '\n') {
            row++;
            col = 1;
        } else {
            col++;
        }
        index++;
    }

    private char nextChar() {
        if (index >= source.length()) return '\0';

        advance();
        return source.charAt(index - 1);
    }

    private char peek() {
        return source.charAt(index);
    }

    private char peekNext() {
        return source.charAt(index - 1);
    }

    private boolean match(char expected) {
        if (peek() == expected) {
            advance();
            return true;
        }
        return false;
    }

    private void eatWhitespace() {
        char c;
        while (true) {
            c = peek();
            if (c != '\n' && c != ' ' && c != '\t') break;
            advance();
        }
    }

    private void eatComments() {
        // TODO: Implement me!
    }

    private Token newToken(Type type) {
        String lexeme = source.substring(start, index);
        return new Token(row, col, lexeme, type);
    }

    private Token lexKeywordOrIdentifier() {
        while (Character.isAlphabetic(peek()) || Character.isDigit(peek())) advance();

        Token tk = newToken(Type.IDENTIFIER);

        // TODO: Check if the Token is a Keyword.

        return tk;
    }

    private Token lexNumberConstant() {
        while (Character.isDigit(peek())) advance();

        return newToken(Type.CONSTANT);
    }

    public Token nextToken() {
        eatWhitespace();
        eatComments();

        start = index;
        char c = nextChar();

        //TODO is underscore allowed at the beginning of an Identifier
        if (Character.isAlphabetic(c)) {
            return lexKeywordOrIdentifier();
        } else if (Character.isDigit(c)) {
            return lexNumberConstant();
        }

        switch (c) {
            case '+': return newToken(Type.PLUS);
            case '-': return newToken(Type.MINUS);
            case ',': return newToken(Type.COMMA);
            case '!': return newToken(Type.BANG);
            case '/': return newToken(Type.SLASH);
            case ':': return newToken(Type.COLON);
            default: return newToken(Type.UNKNOWN);
        }
    }
}
