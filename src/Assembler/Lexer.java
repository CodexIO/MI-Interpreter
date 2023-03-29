package Assembler;

import Assembler.Token.Type;

import java.util.*;

public class Lexer {

    private int row = 1;
    private int col = 1;

    public final String source;    // Input String to Lex
    private int index = 0;       // Indexes the current position in the String
    private int start = 0;       // Marks the start position of the current Token

    private final List<Token> tokens = new ArrayList<>();
    private final Map<String, ArrayList<Token>> equalsDefinitions = new HashMap<>();

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
        if (index >= source.length()) return '\0';

        return source.charAt(index);
    }

    private char peekNext() {
        if (index + 1 >= source.length()) return '\0';
        return source.charAt(index + 1);
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
        while (true) {
            if (peek() == '-' && peekNext() == '-') {

                char c = nextChar();
                while (c != '\n' && c != '\0' && c != ';') c = nextChar();

                if (c == '\0') return;
            }
            else break;

            eatWhitespace();
        }
    }

    private Token newToken(Type type) {
        String lexeme = source.substring(start, index);
        return new Token(row, col, lexeme, type);
    }

    private boolean isRegister(String s) {
        if (s.equals("SP") || s.equals("PC")) return true;
        if (s.charAt(0) == 'R') {
            //@Slow
            String num = s.substring(1);

            try {
                int reg = Integer.parseInt(num);
                return reg >= 0 && reg <= 15;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    // @Speed This is horribly slow
    private boolean isKeyword(String s) {
        for (OpCode op : OpCode.values()) {
            String name = op.name;
            if (s.equals(name)) return true;
        }
        if (s.equals("DD")) return true;
        if (s.equals("RES"))return true;

        return false;
    }

    // Returns the next normal Token after all relevant Tokens for EQU are lexed
    // @Note all these TODOS show that this function should probably be handled by the Parser
    private Token lexEquals() {
        eatWhitespace();

        start = index;

        Token name = lexKeywordOrIdentifier();

        if (name.type != Type.IDENTIFIER) System.exit(-1);//to do: ERROR

        eatWhitespace();

        //to do: ERROR
        if (nextChar() != '=') System.exit(-1);

        int lineNumber = row;
        ArrayList<Token> replacementTokens = new ArrayList<>();

        Token tk = nextToken();
        while (tk.row == lineNumber) {
            replacementTokens.add(tk);
            tk = nextToken();

        }

        //TO DO: Check if equal label is already defined
        equalsDefinitions.put(name.lexeme, replacementTokens);

        return tk;
    }

    private Token maybeReplaceIdentifier(Token identifier) {
        for (String key : equalsDefinitions.keySet()) {
            if (key.equals(identifier.lexeme)) {
                identifier.type = Type.REPLACED_BY_EQUAL;

                //@Robustness:  This only works if the EQU has been seen before,
                // I'm unsure if EQU use before it's definition is allowed.
                ArrayList<Token> replacementTokens = equalsDefinitions.get(key);
                tokens.addAll(replacementTokens);

                return identifier;
            }
        }
        return identifier;
    }

    private Token lexKeywordOrIdentifier() {
        while (Character.isAlphabetic(peek()) || Character.isDigit(peek()) || peek() == '_') advance();

        Token tk = newToken(Type.IDENTIFIER);

        // When we encounter a line beginning with EQU we parse it directly
        if (tk.lexeme.equals("EQU") || tk.lexeme.equals("EQUALS")) {
            return lexEquals();
        }

        if (tk.lexeme.equals("I")) tk.type = Type.I;
        else if (tk.lexeme.equals("B")) tk.type = Type.B;
        else if (tk.lexeme.equals("H")) tk.type = Type.H;
        else if (isRegister(tk.lexeme)) tk.type = Type.REGISTER;
        else if (tk.lexeme.equals("SEG")) tk.type = Type.SEG;
        else if (tk.lexeme.equals("END")) tk.type = Type.END;
        else if (isKeyword(tk.lexeme)) tk.type = Type.KEYWORD;

        return maybeReplaceIdentifier(tk);
    }

    private Token lexNumberConstant() {
        while (Character.isDigit(peek())) advance();

        return newToken(Type.CONSTANT);
    }

    private Token lexEncapsulatedConstant() {
        start = index;

        while (peek() != '\'' && peek() != '\0' && peek() != '\n') advance();

        Token tk = newToken(Type.CONSTANT);

        // Eat the second APOSTROPHE
        advance();

        return tk;
    }

    public List<Token> getTokens() {
        Token tk = nextToken();
        while (tk.type != Type.UNKNOWN) {
            // @Cleanup: Maybe we later want to create a Token that encapsulates
            // this information, for better Error Messages.
            if (tk.type != Type.REPLACED_BY_EQUAL) tokens.add(tk);

            tk = nextToken();
        }

        return tokens;
    }

    public Token nextToken() {
        eatWhitespace();
        eatComments();

        start = index;
        char c = nextChar();

        if (Character.isAlphabetic(c)) {
            return lexKeywordOrIdentifier();
        } else if (c == '\'') {
            return lexEncapsulatedConstant();
        } else if (Character.isDigit(c)) {
            return lexNumberConstant();
        }

        return switch (c) {
            case '+' -> newToken(Type.PLUS);
            case '-' -> newToken(Type.MINUS);
            case ',' -> newToken(Type.COMMA);
            case '.' -> newToken(Type.POINT);
            case '!' -> newToken(Type.BANG);
            case '/' -> newToken(Type.SLASH);
            case ':' -> newToken(Type.COLON);
            case '(' -> newToken(Type.OPEN_PAREN);
            case ')' -> newToken(Type.CLOSE_PAREN);
            default -> newToken(Type.UNKNOWN);
        };
    }
}
