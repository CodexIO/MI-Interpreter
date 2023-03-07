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

    public List<Command> getCommands() {
        return commands;
    }

    private void advanceToken() {
        tokenPosition += 1;
    }

    private Token nextToken() {
        Token tk = peekToken();
        advanceToken();
        return tk;
    }

    private Token peekToken() {
        if (tokenPosition >= tokens.size()) return new Token(-1, -1, "", UNKNOWN);
        return tokens.get(tokenPosition);
    }

    private Token peekPeekToken() {
        int pos = tokenPosition + 1;
        if (pos >= tokens.size()) return new Token(-1, -1, "", UNKNOWN);
        return tokens.get(pos);
    }

    private void eat(Token.Type type) {
        Token tk = nextToken();
        if (tk.type != type) {
            String rowAndCol = "[" + tk.row + ", " + tk.col + "]";
            System.err.println(rowAndCol + "ERROR: Unexpected TokenType, expected: " + type.name() + " but got: " + tk.type + " with lexeme: " + tk.lexeme);
        }
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
                case SEG -> parseSeg();
                case END -> parseEnd();
                case KEYWORD -> parseKeyword(tk);
                case IDENTIFIER -> parseLabel(tk);
                default -> System.out.println("[" + tk.row + ", " + tk.col + "] " + "Unhandled TokenType: " + tk.type + " Lexeme: " + tk.lexeme);
            }
            tk = nextToken();
        }

        patchLabels();
    }

    private void parseSeg() {
        // TODO: Maybe implement this?
        // It's not really needed since SEG doesn't do anything important
    }

    private void parseEnd() {
        // This really does nothing
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
            case "RES" -> parseReserve(command);
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

        Token tk = peekToken();
        //TODO: Check if tk is really a size indicator
        OpCode.DataType size = getDataType(tk);

        // This is a weird edge case we only have to do because of SH and ROT.
        // Maybe write a separate function for 'em
        if (size != NONE) advanceToken();
        else size = WORD;

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

        return switch(tk.type) {
            case IDENTIFIER -> parseAbsoluteAddress(tk);
            case I -> parseImmediateOperand(size);
            case REGISTER -> parseRegisterAddress(tk);
            case PLUS, CONSTANT -> parseAbsoluteAddressOrRelativeAddress(tk);
            case MINUS -> parseAbsOrRelOrStack(tk);
            case BANG -> parseOtherTypes(tk);
            default -> null; //TODO Error
        };
    }

    private Command parseReserve(Token command) {
        int address = currentAddress;

        Token tk = nextToken();
        int reserveCount = Integer.parseInt(tk.lexeme);

        ArrayList<Byte> bytes = new ArrayList<>(reserveCount);
        for (int i = 0; i < reserveCount; i++) bytes.add((byte) 0);

        currentAddress += reserveCount;

        // Reserve does almost the same thing as Data Definition, which is why we can use the same class
        // @Clean We should rename the class though to better reflect this
        return new AST_DataDefinition(null, command.row, address, command.col, -1, bytes);
    }

    private Command parseDataDefinition(Token command) {
        int address = currentAddress;

        ArrayList<Byte> bytes = parseDataGroup();

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

        } while (match(COMMA));

        return bytes;
    }

    private ArrayList<Byte> parseDataElement(OpCode.DataType size, Token tk) {
        ArrayList<Byte> bytes = new ArrayList<>();

        switch (tk.type) {
            case CONSTANT, MINUS -> {
                long number = 1;
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
                if (tk.lexeme.equals("B") || tk.lexeme.equals("H")) {
                    eat(APOSTROPHE);
                    Token num = nextToken();
                    int number = switch (tk.lexeme) {
                        case "B" -> Integer.parseInt(num.lexeme, 2);
                        case "H" -> Integer.parseInt(num.lexeme, 16);
                        default -> 0xCCCC_CCCC; //TODO: ERROR
                    };
                    eat(APOSTROPHE);
                    return getBytesFromNumber(number, size);
                } else {
                    String labelName = tk.lexeme;
                    Integer address = labelAddresses.get(labelName);

                    if (address == null) {
                        // TODO: Handle labels not being defined.
                        // Maybe we should pre parse to know all the labels ahead of time
                    } else {
                        return getBytesFromNumber(address, WORD);
                    }
                }
            }
        }

        //TODO: ERROR
        return bytes;
    }

    private OpCode.DataType getFittingSize(long number) {
        if (number <= Byte.MAX_VALUE && number >= Byte.MIN_VALUE) return BYTE;
        else if (number <= Short.MAX_VALUE && number >= Short.MIN_VALUE) return HALFWORD;
        else return WORD;
    }

    private ArrayList<Byte> getBytesFromNumber(long number, OpCode.DataType size) {
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
            case FLOAT:
                float floatNumber = (float) number;
                int bits = Float.floatToIntBits(floatNumber);
                bytes = getBytesFromNumber(bits, WORD);
                break;
            case DOUBLE:
                double doubleNumber = (double) number;
                long longBits = Double.doubleToLongBits(doubleNumber);
                bytes.addAll(getBytesFromNumber(longBits >> 32, WORD));
                bytes.addAll(getBytesFromNumber((int) longBits, WORD));
                break;
        }

        return bytes;
    }

    private Operand parseAbsoluteAddressOrRelativeAddress(Token tk) {
        switch (tk.type) {
            case PLUS, MINUS -> {
                if (peekToken().type == CONSTANT && peekPeekToken().type == PLUS)
                    return parseIndexedOrRelativeAddress(tk);
            }
            case CONSTANT -> {
                if (peekToken().type == PLUS) return parseIndexedOrRelativeAddress(tk);
            }
        }
        return parseAbsoluteAddress(tk);
    }

    private Operand parseAbsOrRelOrStack(Token tk) {
        if (peekToken().type == BANG) return parseStackAddressing(tk);
        else return parseAbsoluteAddressOrRelativeAddress(tk);
    }

    private Operand parseAbsoluteAddress(Token tk) {
        int sign = 1;

        switch (tk.type) {
            case IDENTIFIER -> {
                return parseLabelAddress(tk);
            }
            case MINUS -> sign = -1;  // I don't know why you would support this for an Address, but it's written in the Grammar so...
            case PLUS -> sign = 1;
            case CONSTANT -> {
                int address = Integer.parseInt(tk.lexeme);
                return new AbsoluteAddress(address);
            }
        }
        Token constant = nextToken();
        if (constant.type != CONSTANT) return null; //ERROR

        int address = sign * Integer.parseInt(tk.lexeme);
        return new AbsoluteAddress(address);
    }

    private Operand parseOtherTypes(Token tk) {
        Token.Type nextType = peekToken().type;
        Token.Type nextNextType = peekPeekToken().type;

        if (nextType == REGISTER && nextNextType == PLUS)
            return parseStackAddressing(tk);

        return parseRelativeOrIndirectAddress(tk);
    }

    private Operand parseStackAddressing(Token tk) {
        boolean plus = true;

        if (tk.type == MINUS) {
            plus = false;
            eat(BANG);
        }
        else assert(tk.type == BANG);

        Token regTk = nextToken();
        RegisterAddress reg = parseRegisterAddress(regTk);
        if (plus) eat(PLUS);

        return new StackAddress(reg.getReg(), plus);
    }

    private Operand parseRelativeOrIndirectAddress(Token tk) {
        Token.Type nextType = peekToken().type;

        if (nextType == REGISTER)  return parseIndexedOrRelativeAddress(tk);

        return parseIndexedOrIndirectAddress(tk);
    }

    private Operand parseIndexedAddress(Operand relOrIndAddress) {
        RegisterAddress reg = parseRegisterAddress(nextToken());
        eat(SLASH);
        return new IndexedAddress(relOrIndAddress, reg.getReg());
    }

    private Operand parseIndexedOrRelativeAddress(Token tk) {
        Operand operand = parseRelativeAddress(tk);

        if (match(SLASH)) return parseIndexedAddress(operand);
        else return operand;
    }

    private RelativeAddress parseRelativeAddress(Token tk) {
        int offset = Integer.MIN_VALUE;

        //TODO: Refactor this into it's own function
        switch (tk.type) {
            case PLUS -> {
                Token number = nextToken();
                // TODO CHECK IF CONSTANT WITH SOMETHING like Token expect(Token.Type type) returning the nextToken if it matches and if not errors
                offset = Integer.parseInt(number.lexeme);
            }
            case MINUS -> {
                Token number = nextToken();
                // TODO CHECK IF CONSTANT WITH SOMETHING like Token expect(Token.Type type) returning the nextToken if it matches and if not errors
                offset = - Integer.parseInt(number.lexeme);
            }
            case CONSTANT -> {
                offset = Integer.parseInt(tk.lexeme);
            }
        }
        if (offset != Integer.MIN_VALUE) {
            eat(PLUS);
            eat(BANG);
        }
        else offset = 0;

        Token regTk = nextToken();
        RegisterAddress reg = parseRegisterAddress(regTk);

        return new RelativeAddress(currentAddress, offset, reg.getReg());
    }

    private Operand parseIndexedOrIndirectAddress(Token tk) {
        Operand operand = parseIndirectAddress(tk);

        if (match(SLASH)) return parseIndexedAddress(operand);
        else return operand;
    }

    private IndirectAddress parseIndirectAddress(Token tk) {
        assert(tk.type == BANG);

        eat(OPEN_PAREN);
        // Relative and Indirect use the same information for generating
        // machine code.
        RelativeAddress relAddress = parseRelativeAddress(nextToken());
        eat(CLOSE_PAREN);

        return new IndirectAddress(relAddress);
    }

    private RegisterAddress parseRegisterAddress(Token tk) {
        int reg;

        if (tk.lexeme.equals("PC")) reg = VirtualMachine.PC_REGISTER;
        else if (tk.lexeme.equals("SP")) reg = VirtualMachine.SP_REGISTER;
        else {
            String s = tk.lexeme.substring(1);
            reg = Integer.parseInt(s);
        }

        return new RegisterAddress(reg);
    }

    private RelativeAddress parseLabelAddress(Token tk) {
        String labelName = tk.lexeme;
        Integer address = labelAddresses.get(labelName);
        int pc = currentAddress;
        int pcReg = 15;

        int number = 0;
        Token plusOrMinus = peekToken();
        while(match(PLUS) || match(MINUS)) {
            Token numberToken = nextToken();
            int tmp = Integer.parseInt(numberToken.lexeme);
            //TODO: Support '<Zeichen>'

            if (plusOrMinus.type == PLUS) number += tmp;
            else number -= tmp;
        }

        // In case we don't know the label yet, we have to fill the address in later
        if (address == null) {
            RelativeAddress addressToPatch = new RelativeAddress(pc, pcReg, labelName);
            labelsToPatch.add(addressToPatch);
            return addressToPatch;
        }
        int offset = address - (pc + 1) + number;


        return new RelativeAddress(pc, offset, pcReg);
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
        long number;

        switch(tk.type) {
            case MINUS -> {
                tk = nextToken();
                number = - Long.parseLong(tk.lexeme);
            }
            case CONSTANT -> number = Integer.parseInt(tk.lexeme);
            case IDENTIFIER -> {
                Token num = nextToken();
                number = switch (tk.lexeme) {
                    case "B" -> Long.parseLong(num.lexeme, 2);
                    case "H" -> Long.parseLong(num.lexeme, 16);
                    default -> 0xCCCC_CCCC; //TODO: ERROR
                };
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
            else {
                int oldSize = adr.size();

                adr.patchLabel(address);

                int newSize = adr.size();
                int offset = newSize - oldSize;
                if (offset != 0) {
                    System.out.println("Patching: " + adr.labelName);
                    patchLabelAddresses(adr.address, offset);
                    patchRelativeAddresses(adr.address, offset);
                }
            }
        }
    }

    private void patchLabelAddresses(int startingAddress, int offset) {
        for (var key: labelAddresses.keySet()) {
            int address = labelAddresses.get(key);
            if (address > startingAddress) {
                address += offset;
                labelAddresses.put(key, address);
            }
        }
    }

    private void patchRelativeAddresses(int startingAddress, int offset) {
        for (Command command : commands) {
            if (command.address > startingAddress) command.address += offset;

            for (Operand op : command.getOperands()) {
                if (op instanceof RelativeAddress rel) {
                    // TODO: What about Relative Addresses that are explicitly encoded as 5 + R15,
                    // instead of using labels. Do we need to patch those too?
                    if (rel.regX != 15) continue;

                    System.out.println("Starting Address: " + startingAddress);
                    if (rel.address > startingAddress) {
                        System.out.println("\tPatching Later with offset:" + offset);
                        rel.address += offset;
                        if (rel.address + rel.offset <= startingAddress) {
                            rel.offset -= offset;
                        }
                    }
                    if (rel.address <= startingAddress && rel.address + rel.offset > startingAddress) {
                        rel.offset += offset;
                        System.out.println("\tPatching Earlier with offset:" + offset);
                    }
                }
            }
        }
    }
}
