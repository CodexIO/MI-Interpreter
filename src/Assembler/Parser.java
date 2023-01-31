package Assembler;

import Assembler.AST_Nodes.*;

import java.util.ArrayList;
import java.util.Dictionary;

import static Assembler.Token.Type.*;

public class Parser {

    private Lexer lx;
    private ArrayList<Token> tokens;
    private ArrayList<Command> commands;
    private Dictionary<String, Integer> labelAdresses;

    private int currentAddress = 0;

    public Parser(Lexer lexer) {
        lx = lexer;
        commands = new ArrayList<Command>();
    }

    private void eat(Token.Type type) {
        Token tk = lx.nextToken();
        if (tk.type != type) assert(false);//TODO: ERROR
    }

    private boolean match(Token.Type type) {
        //TODO: Figure out how to peekToken. Maybe Lex all the Tokens upfront
        Token tk = lx.peekToken();
        if (tk.type == type) {
            lx.nextToken();
            return true;
        }
        return false;
    }

    public ArrayList<Byte> generateMachineCode() {
        System.out.println("Generating MachineCode for the input:\n" + lx.source);
        ArrayList<Byte> code = new ArrayList<>();
        for(Command cmd : commands) {
            //TODO: Do i want to use ArrayList instead of byte[] everywhere?
            byte[] bytes = cmd.generateMachineCode();
            for(byte b : bytes) {
                code.add(b);
            }
        }
        return code;
    }

    public void parse() {
        Token tk = lx.nextToken();

        while (tk.type != UNKNOWN) {
            switch (tk.type) {
                case KEYWORD -> parseKeyword(tk);
                default -> System.out.println("Unhandled TokenType: " + tk.type + " Lexeme: " + tk.lexeme);
            }
            tk = lx.nextToken();
        }
    }

    private void parseKeyword(Token command) {

        //TODO @Speed This String comparisons are probably slow.
        Command cmd = switch (command.lexeme) {
            case "ADD" -> parseCommand(command);

            default -> null;
        };

        commands.add(cmd);
    }

    private Command parseCommand(Token command) {
        int address = currentAddress;
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

        //TODO: Check if operands are really always
        //TODO  2 or 3... Probably not
        int operands = 2;

        Operand a1 = parseOperand(size);
        eat(COMMA);

        Operand a2 = parseOperand(size);

        Operand a3 = null;

        if (match(COMMA)) {
            a3 = parseOperand(size);
            operands = 3;
        }

        OpCode op = OpCode.getOpCode(command.lexeme, size, operands);


        //TODO: Think of a convenient way to keep track of current line and row
        //TODO:                                      |
        return new AST_Add(op, command.row, address, command.col, -1, a1, a2, a3);
    }

    private Operand parseOperand(OpCode.DataType size) {
        Token tk = lx.nextToken();

        if (tk.lexeme.equals("I")) {
            return parseImmediateOperand(size);
        }
        else if (tk.type == CONSTANT) {
            if (lx.peekToken().type == PLUS)
                return parseRelativeAddress(tk);
            else
                return parseAbsoluteAddress(tk, size);
        }
        else if (tk.lexeme.startsWith("R")) {
            return parseRegisterAddress(tk);
        }
        else {
            //TODO: ERROR
        }

        return null; //TODO: remove this
    }

    private RelativeAddress parseRelativeAddress(Token tk) {
        int offset = Integer.parseInt(tk.lexeme);

        eat(PLUS);
        eat(BANG);

        Token regTk = lx.nextToken();
        RegisterAddress reg = parseRegisterAddress(regTk);

        return new RelativeAddress(offset, reg.getReg());
    }

    private RegisterAddress parseRegisterAddress(Token tk) {
        String s = tk.lexeme.substring(1);
        int reg = Integer.parseInt(s);

        return new RegisterAddress(reg);
    }

    private AbsoluteAddress parseAbsoluteAddress(Token tk, OpCode.DataType size) {
        int address = Integer.parseInt(tk.lexeme);

        return new AbsoluteAddress(address);
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
        return null; //TODO: Remove this
    }
}
