package Assembler;

import Assembler.AST_Nodes.*;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

import static Assembler.OpCode.DataType.*;
import static Assembler.Token.Type.*;

public class Parser {

    private Lexer lx;
    private ArrayList<Token> tokens;
    private ArrayList<Command> commands = new ArrayList<>();
    private ArrayList<RelativeAddress> labelsToPatch = new ArrayList<>();
    private Dictionary<String, Integer> labelAdresses = new Hashtable<>();

    private int currentAddress = 0;

    public Parser(String input) {
        lx = new Lexer(input);
    }

    public Parser(Lexer lexer) {
        lx = lexer;
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

    public byte[] generateMachineCode() {
        System.out.println("Generating MachineCode for the input:\n" + lx.source);
        ArrayList<Byte> code = new ArrayList<>();
        for(Command cmd : commands) {
            //TODO: Do i want to use ArrayList instead of byte[] everywhere?
            //TODO: Probably not, i think i can figure out the size beforehand and just use byte[]
            byte[] bytes = cmd.generateMachineCode();
            for(byte b : bytes) {
                code.add(b);
            }
        }

        StringBuilder sb = new StringBuilder();
        for(byte b : code) {
            sb.append(String.format("%02X ", b & 0xFF));
        }
        System.out.println(sb);

        byte[] bytes = new byte[code.size()];
        int i = 0;
        for (Byte b : code) {
            bytes[i++] = b;
        }
        return bytes;
    }

    public void parse() {
        Token tk = lx.nextToken();

        while (tk.type != UNKNOWN) {
            switch (tk.type) {
                case KEYWORD -> parseKeyword(tk);
                case IDENTIFIER -> parseLabel(tk);
                default -> System.out.println("Unhandled TokenType: " + tk.type + " Lexeme: " + tk.lexeme);
            }
            tk = lx.nextToken();
        }

        patchLabels();
    }

    private void parseLabel(Token name) {
        eat(COLON);
        labelAdresses.put(name.lexeme, currentAddress);
    }

    private void parseKeyword(Token command) {

        //TODO @Speed This String comparisons are probably slow.
        Command cmd = switch (command.lexeme) {
            case "MOVE" -> parseCommand(command);
            case "MOVEA" -> parseMOVEA(command);
            case "ADD" -> parseCommand(command);
            case "SUB" -> parseCommand(command);
            case "MULT" -> parseCommand(command);
            case "DIV" -> parseCommand(command);

            default -> null;
        };

        commands.add(cmd);
    }

    private Command parseCommand(Token command) {
        // Incrementing the currentAddress because of the OpCode Byte
        int address = currentAddress++;

        Token tk = lx.nextToken();
        //TODO: Check if tk is really a size indicator
        OpCode.DataType size = switch (tk.lexeme) {
            case "B" -> OpCode.DataType.BYTE;
            case "H" -> OpCode.DataType.HALFWORD;
            case "W" -> OpCode.DataType.WORD;
            case "F" -> OpCode.DataType.FLOAT;
            case "D" -> OpCode.DataType.DOUBLE;
            default -> NONE;
        };

        //TODO: Check if operands are really always
        //TODO  2 or 3... Probably not
        int operands = 2;

        Operand a1 = parseOperand(size);
        currentAddress += a1.size();
        eat(COMMA);

        Operand a2 = parseOperand(size);
        currentAddress += a2.size();

        Operand a3 = null;

        if (match(COMMA)) {
            a3 = parseOperand(size);
            currentAddress += a3.size();
            operands = 3;
        }

        OpCode op = OpCode.getOpCode(command.lexeme, size, operands);

        //TODO: Think of a convenient way to keep track of current line and row
        //TODO:                                      |
        return new AST_Add(op, command.row, address, command.col, -1, a1, a2, a3);
    }

    private Command parseMOVEA(Token command) {
        int address = currentAddress++;

        //TODO: Check if operands are really always
        //TODO  2 or 3... Probably not
        int operands = 2;

        Operand a1 = parseOperand(WORD);
        currentAddress += a1.size();
        eat(COMMA);

        Operand a2 = parseOperand(WORD);
        currentAddress += a2.size();

        OpCode op = OpCode.getOpCode(command.lexeme, WORD, operands);


        //TODO: Think of a convenient way to keep track of current line and row
        //TODO:                                      |
        return new AST_Add(op, command.row, address, command.col, -1, a1, a2, null);
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
                return parseAbsoluteAddress(tk);
        }
        else if (tk.lexeme.startsWith("R")) {
            return parseRegisterAddress(tk);
        }
        else if (tk.type == IDENTIFIER) {
            return parseLabelAddress(tk);
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

    private AbsoluteAddress parseAbsoluteAddress(Token tk) {
        int address = Integer.parseInt(tk.lexeme);

        return new AbsoluteAddress(address);
    }

    //TODO: Labels are apparently relative to the PC and not AbsoluteAddresses. Fix this.
    private RelativeAddress parseLabelAddress(Token tk) {
        String labelName = tk.lexeme;
        Integer address = labelAdresses.get(labelName);
        int pc = currentAddress + 1;
        int pcReg = 15;

        // In case we don't know the label yet, we have to fill the address in later
        if (address == null) {
            RelativeAddress addressToPatch = new RelativeAddress(pc, pcReg, labelName);
            labelsToPatch.add(addressToPatch);
            return addressToPatch;
        }

        int offset = address - pc;
        return new RelativeAddress(offset, pcReg);
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

    private void patchLabels() {
        for (RelativeAddress adr : labelsToPatch) {
            Integer address = labelAdresses.get(adr.labelName);

            //TODO: ERROR Unknown label;
            if (address == null) System.out.println("Unknown Label " + adr.labelName);
            else adr.patchLabel(address);
        }
    }
}
