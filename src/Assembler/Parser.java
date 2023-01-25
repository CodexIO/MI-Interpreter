package Assembler;

import Assembler.AST_Nodes.*;

import java.util.ArrayList;
import java.util.Dictionary;

import static Assembler.Token.Type.COMMA;

public class Parser {

    private Lexer lx;
    private ArrayList<Token> tokens;
    private ArrayList<Command> commands;
    private Dictionary<String, Integer> labelAdresses;

    private int currentAdress = 0;

    public Parser(Lexer lexer) {
        lx = lexer;
        commands = new ArrayList<Command>();
    }

    private void check(Token.Type type) {
        Token tk = lx.nextToken();
        if (tk.type != type) ;//TODO: ERROR
    }

    private boolean match(Token.Type type) {
        //TODO: Figure out how to peekToken. Maybe Lex all the Tokens upfront
        Token tk = lx.peekToken();
        if (tk.type == type) {
            lx.advance();
            return true;
        }
        return false;
    }

    public void parse() {
        Token tk = lx.nextToken();

        switch (tk.type) {
            case KEYWORD -> parseKeyword(tk);
        }
    }

    private void parseKeyword(Token command) {

        //TODO @Speed This String comparisons are probably slow.
        switch (command.lexeme) {
            case "ADD" -> parseCommand(command);

            default ->
        }
    }

    private Command parseCommand(Token command) {
        Token tk = lx.nextToken();
        //TODO: Check if tk is really a size indicator
        OpCode.DataType size = switch (tk.lexeme) {
            case "B" -> OpCode.DataType.BYTE;
            case "H" -> OpCode.DataType.HALFWORD;
            case "W" -> OpCode.DataType.WORD;
            case "F" -> OpCode.DataType.FLOAT;
            case "D" -> OpCode.DataType.DOUBLE;
            default -> OpCode.DataType.NONE;
        };

        OpCode op = OpCode.getOpCode(command.lexeme, size);

        Operand a1 = parseOperand();
        check(COMMA);

        Operand a2 = parseOperand();

        if (match(COMMA)) {

        }
    }

    private Operand parseOperand() {
        Token tk = lx.nextToken();

        return switch (tk.lexeme) {
            case "I" -> parseImmediateOperand();
        };
    }

    private ImmediateOperand parseImmediateOperand(OpCode.DataType size) {
        //TODO: Support Floating Point
        Token tk = lx.nextToken();

        switch(tk.type) {
            case CONSTANT -> {
                int number = Integer.parseInt(tk.lexeme);
                return new ImmediateOperand(number, size);
            }
            case IDENTIFIER -> {
                Token num = lx.nextToken();
                switch (tk.lexeme) {
                    case "B" -> {
                        int number = Integer.parseInt(num.lexeme, 2);
                        return new ImmediateOperand(number, size);
                    }
                    case "H" -> {
                        int number = Integer.parseInt(num.lexeme, 16);
                        return new ImmediateOperand(number, size);
                    }
                    default -> {
                        //TODO: ERROR
                    }
                }
            }
            default -> {
                //TODO: ERROR
            }
        }
    }
}
