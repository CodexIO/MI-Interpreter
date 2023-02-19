package Assembler;

import Assembler.AST_Nodes.*;
import Interpreter.VirtualMachine;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static Assembler.OpCode.DataType.*;
import static Assembler.Token.Type.*;

public class Parser {

    private final Lexer lx;
    private final List<Token> tokens;
    private int tokenPosition;

    private final List<Command> commands = new ArrayList<>();
    private final List<RelativeAddress> labelsToPatch = new ArrayList<>();
    private final Map<String, Integer> labelAddresses = new HashMap<>();

    private int currentAddress = 0;

    public Parser(Lexer lexer) {
        lx = lexer;
        tokens = lx.getTokens();
    }

    public Parser(String input) {
        this(new Lexer(input));
    }

    private Token nextToken() {
        Token tk = peekToken();
        tokenPosition += 1;
        return tk;
    }

    private Token peekToken() {
        if (tokenPosition >= tokens.size()) return new Token(-1, -1, "", UNKNOWN);
        return tokens.get(tokenPosition);
    }

    private void eat(Token.Type type) {
        Token tk = nextToken();
        if (tk.type != type) assert(false);//TODO: ERROR
    }

    private boolean match(Token.Type type) {
        Token tk = peekToken();
        if (tk.type == type) {
            nextToken();
            return true;
        }
        return false;
    }

    public byte[] generateMachineCode() {
        ArrayList<Byte> code = new ArrayList<>();
        for(Command cmd : commands) {
            //TODO: Do i want to use ArrayList instead of byte[] everywhere?
            //TODO: Probably not, i think i can figure out the size beforehand and just use byte[]
            byte[] bytes = cmd.generateMachineCode();
            for(byte b : bytes) {
                code.add(b);
            }
        }

        byte[] bytes = new byte[code.size()];
        int i = 0;
        for (Byte b : code) {
            bytes[i++] = b;
        }
        return bytes;
    }

    public void parse() {
        Token tk = nextToken();

        while (tk.type != UNKNOWN) {
            switch (tk.type) {
                case KEYWORD -> parseKeyword(tk);
                case IDENTIFIER -> parseLabel(tk);
                default -> System.out.println("Unhandled TokenType: " + tk.type + " Lexeme: " + tk.lexeme);
            }
            tk = nextToken();
        }

        patchLabels();
    }

    private void parseLabel(Token name) {
        eat(COLON);
        //TODO: Check if a label was already defined.
        labelAddresses.put(name.lexeme, currentAddress);
    }

    private void parseKeyword(Token command) {

        //TODO @Speed This String comparisons are probably slow.
        Command cmd = switch (command.lexeme) {
            case "HALT" -> parseZeroOpCommand(command);
            case "DD" -> parseDataDefinition(command);
            case "CMP", "MOVE", "MOVEN", "MOVEC" -> parseTwoOpCommand(command);
            case "MOVEA" -> parseMOVEAorCONV(command, true);
            case "CONV" -> parseMOVEAorCONV(command, false);
            case "OR", "ANDNOT", "XOR", "ADD", "SUB", "MULT", "DIV"->
                    parseTwoOrThreeOpCommand(command);
            case "SH", "ROT" -> parseThreeOpCommand(command);
            case "CLEAR" -> parseCLEAR(command);
            case "JEQ", "JNE", "JGT",
                    "JGE", "JLT", "JLE",
                    "JC", "JNC", "JV", "JNV" -> parseSingleOpCommand(command);
            case "JUMP", "CALL" -> parseSingleOpCommand(command);
            case "RET", "PUSHR", "POPR" -> parseZeroOpCommand(command);
            default -> null;
        };

        commands.add(cmd);
    }

    private OpCode.DataType getDataType(Token tk) {
        return switch (tk.lexeme) {
            case "B" -> OpCode.DataType.BYTE;
            case "H" -> OpCode.DataType.HALFWORD;
            case "W" -> OpCode.DataType.WORD;
            case "F" -> OpCode.DataType.FLOAT;
            case "D" -> OpCode.DataType.DOUBLE;
            default -> NONE;
        };
    }

    private Command parseTwoOrThreeOpCommand(Token command) {
        //It doesn't matter what we pass as twoOperandsNeeded, when fixedOperandCount is false
        return parseCommand(command, false, false);
    }

    private Command parseTwoOpCommand(Token command) {
        return parseCommand(command, true, true);
    }

    private Command parseThreeOpCommand(Token command) {
        return parseCommand(command, true, false);
    }

    private Command parseCommand(Token command, boolean fixedOperandCount, boolean twoOperandsNeeded) {
        // Incrementing the currentAddress because of the OpCode Byte
        int address = currentAddress++;

        Token tk = nextToken();
        //TODO: Check if tk is really a size indicator
        OpCode.DataType size = getDataType(tk);

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

        if (fixedOperandCount) {
            if (twoOperandsNeeded) {
                if (a3 != null) {
                    //TODO: ERROR
                }
            } else {
                if (a3 == null) {
                    //TODO: ERROR
                }
            }
        }

        OpCode op = OpCode.getOpCode(command.lexeme, size, operands);

        //TODO: Think of a convenient way to keep track of current line and row
        //TODO:                                      |
        return new AST_Add(op, command.row, address, command.col, -1, a1, a2, a3);
    }

    private Command parseMOVEAorCONV(Token command, boolean movea) {
        int address = currentAddress++;
        int operands = 2;

        Operand a1 = parseOperand(movea ? WORD : BYTE);
        currentAddress += a1.size();
        eat(COMMA);

        Operand a2 = parseOperand(WORD);
        currentAddress += a2.size();

        OpCode op = OpCode.getOpCode(command.lexeme, WORD, operands);


        //TODO: Think of a convenient way to keep track of current line and row
        //TODO:                                      |
        return new AST_Add(op, command.row, address, command.col, -1, a1, a2, null);
    }

    // @Clean This is almost the same as parseSingleOp
    // except that here we need to figure out the size first
    // Maybe factor together
    private Command parseCLEAR(Token command) {
        int address = currentAddress++;
        int operandCount = 1;

        Token tk = nextToken();
        //TODO: Check if tk is really a size indicator
        OpCode.DataType size = getDataType(tk);

        Operand a1 = parseOperand(WORD);
        currentAddress += a1.size();

        OpCode op = OpCode.getOpCode(command.lexeme, size, operandCount);

        return new AST_SingleOp(op, command.row, address, command.col, -1, a1);
    }

    private Command parseZeroOpCommand(Token command) {
        int address = currentAddress++;

        OpCode op = OpCode.getOpCode(command.lexeme, NONE, 0);

        return new AST_ZeroOp(op, command.row, address, command.col, -1);
    }

    private Command parseSingleOpCommand(Token command) {
        int address = currentAddress++;

        Operand a1 = parseOperand(WORD);
        currentAddress += a1.size();

        OpCode op = OpCode.getOpCode(command.lexeme, WORD, 1);

        return new AST_SingleOp(op, command.row, address, command.col, -1, a1);
    }

    private Operand parseOperand(OpCode.DataType size) {
        Token tk = nextToken();

        if (tk.lexeme.equals("I")) {
            return parseImmediateOperand(size);
        }
        else if (tk.type == CONSTANT) {
            if (match(PLUS))
                return parseRelativeAddress(tk);
            else
                return parseAbsoluteAddress(tk);
        }
        else if (tk.lexeme.startsWith("R")) {
            return parseRegisterAddress(tk);
        }
        else if (tk.type == KEYWORD) {
            if (tk.lexeme.equals("PC")) return new RegisterAddress(VirtualMachine.PC_REGISTER);
            if (tk.lexeme.equals("SP")) return new RegisterAddress(VirtualMachine.SP_REGISTER);
        }
        else if (tk.type == IDENTIFIER) {
            return parseLabelAddress(tk);
        }
        else if (tk.type == BANG) {
            //TODO: Handle the other types of Addressing that start with !
            return parseStackAddressing();
        }
        else {
            //TODO: ERROR
        }

        return null; //TODO: remove this
    }

    private Command parseDataDefinition(Token command) {
        int address = currentAddress;

        ArrayList<Byte> bytes = new ArrayList<>(parseDataGroup());

        currentAddress += bytes.size();
        return new AST_DataDefinition(null, command.row, address, command.col, -1, bytes);
    }

    private ArrayList<Byte> parseDataGroup() {
        ArrayList<Byte> bytes = new ArrayList<>();

        do {
            Token tk = nextToken();
            OpCode.DataType size = getDataType(tk);

            if (size != NONE) tk = nextToken();

            bytes.addAll(parseDataElement(size, tk));

        } while (peekToken().type == COMMA);

        return bytes;
    }

    private ArrayList<Byte> parseDataElement(OpCode.DataType size, Token tk) {
        ArrayList<Byte> bytes = new ArrayList<>();

        switch (tk.type) {
            case CONSTANT, MINUS -> {
                int number = 1;
                if (tk.type == MINUS) {
                    number = -1;
                    tk = nextToken();
                }
                number *= Integer.parseInt(tk.lexeme);
                return getBytesFromNumber(number, size);
            }
            case OPEN_PAREN -> {
                //TODO: Implement this
            }
            case APOSTROPHE -> {
                tk = nextToken();
                for (byte b : tk.lexeme.getBytes(StandardCharsets.US_ASCII)) {
                    bytes.add(b);
                }
                eat(APOSTROPHE);
                return bytes;
            }
            case IDENTIFIER -> {
                eat(APOSTROPHE);
                Token num = nextToken();
                int number = switch (tk.lexeme) {
                    case "B" -> Integer.parseInt(num.lexeme, 2);
                    case "H" -> Integer.parseInt(num.lexeme, 16);
                    default -> 0xCCCC_CCCC; //TODO: ERROR
                };
                eat(APOSTROPHE);
                return getBytesFromNumber(number, size);
            }
        }

        //TODO: ERROR
        return bytes;
    }

    private OpCode.DataType getFittingSize(int number) {
        if (number <= Byte.MAX_VALUE && number >= Byte.MIN_VALUE) return BYTE;
        else if (number <= Short.MAX_VALUE && number >= Short.MIN_VALUE) return HALFWORD;
        else return WORD;
    }

    private ArrayList<Byte> getBytesFromNumber(int number, OpCode.DataType size) {
        ArrayList<Byte> bytes = new ArrayList<>();

        if (size == NONE) size = getFittingSize(number);
        switch (size) {
            case WORD: {
                bytes.add((byte) (number >>> 24));
                bytes.add((byte) (number >>> 16));
            }
            case HALFWORD:
                bytes.add((byte) (number >>> 8));
            case BYTE:
                bytes.add((byte) number);
                break;
                // TODO: Add support for FLOAT and Double here.
        }

        return bytes;
    }

    //TODO: For now this only handles !Rx+. @Cleanup
    private StackAddress parseStackAddressing() {
        Token regTk = nextToken();
        RegisterAddress reg = parseRegisterAddress(regTk);
        eat(PLUS);

        return new StackAddress(reg.getReg(), true);
    }

    private RelativeAddress parseRelativeAddress(Token tk) {
        int offset = Integer.parseInt(tk.lexeme);

        eat(BANG);

        Token regTk = nextToken();
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
        Integer address = labelAddresses.get(labelName);
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
        if (size == FLOAT || size == DOUBLE) {
            return parseFloat(size);
        }

        //TODO: Maybe put parseFloat and parseInteger together
        return parseInteger(size);
    }

    //TODO: Here and in parseInteger, check if provided number fits into the specified size
    private ImmediateOperand parseFloat(OpCode.DataType size) {
        // @Note: We simply parse everything as a Double first
        // and later cast it to a float if a float is specified
        // TODO: @Felix ist das okay so?

        Token tk = nextToken();
        double number = 0;


        switch(tk.type) {
            case MINUS -> {
                tk = nextToken();
                String floatToParse = tk.lexeme;
                if (match(POINT)) {
                    String afterDecimalPoint = peekToken().type == CONSTANT ? nextToken().lexeme : "";
                    floatToParse = tk.lexeme + "." + afterDecimalPoint;
                }
                number = - Float.parseFloat(floatToParse);
            }
            case CONSTANT -> {
                String floatToParse = tk.lexeme;
                if (match(POINT)) {
                    String afterDecimalPoint = peekToken().type == CONSTANT ? nextToken().lexeme : "";
                    floatToParse = tk.lexeme + "." + afterDecimalPoint;
                }
                number = Float.parseFloat(floatToParse);
            }
            case IDENTIFIER -> {
                eat(APOSTROPHE);
                Token num = nextToken();
                long floatSavedInLong = switch (tk.lexeme) {
                    case "B" -> Long.parseLong(num.lexeme, 2);
                    case "H" -> Long.parseLong(num.lexeme, 16);
                    default -> 0xCCCC_CCCC; //TODO: ERROR
                };
                eat(APOSTROPHE);
                return new ImmediateOperand(floatSavedInLong, size);
            }
            default -> {
                //TODO: ERROR
            }
        }

        if (size == FLOAT) return new ImmediateOperand((float) number);
        else return new ImmediateOperand(number);
    }

    private ImmediateOperand parseInteger(OpCode.DataType size) {
        Token tk = nextToken();
        int number;

        switch(tk.type) {
            case MINUS -> {
                tk = nextToken();
                number = - Integer.parseInt(tk.lexeme);
            }
            case CONSTANT -> number = Integer.parseInt(tk.lexeme);
            case IDENTIFIER -> {
                eat(APOSTROPHE);
                Token num = nextToken();
                number = switch (tk.lexeme) {
                    case "B" -> Integer.parseInt(num.lexeme, 2);
                    case "H" -> Integer.parseInt(num.lexeme, 16);
                    default -> 0xCCCC_CCCC; //TODO: ERROR
                };
                eat(APOSTROPHE);
            }
            default -> {
                number = 0xCCCC_CCCC;
                //TODO: ERROR
            }
        }
        return new ImmediateOperand(number, size);
    }

    private void patchLabels() {
        for (RelativeAddress adr : labelsToPatch) {
            Integer address = labelAddresses.get(adr.labelName);

            //TODO: ERROR Unknown label;
            if (address == null) System.out.println("Unknown Label " + adr.labelName);
            else adr.patchLabel(address);
        }
    }
}
