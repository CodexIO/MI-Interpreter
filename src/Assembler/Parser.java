package Assembler;

import Assembler.AST_Nodes.*;
import Interpreter.VirtualMachine;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static Assembler.OpCode.DataType.*;
import static Assembler.Token.Type.*;

public class Parser {

    private List<Token> tokens;
    private int tokenIndex;
    private int currentAddress = 0;

    private final List<Command> commands = new ArrayList<>();
    private final List<RelativeAddress> labelsToPatch = new ArrayList<>();
    private final Map<String, Integer> labelAddresses = new HashMap<>();

    private final List<AST_DataDefinition> dataDefinitionsToPatch = new ArrayList<>();
    private List<String> labelsToPatchInDD = new ArrayList<>();

    private PrintStream out;

    public Parser(PrintStream out) {
        this.out = out;
    }

    private void reset() {
        tokenIndex = 0;
        commands.clear();
        labelsToPatch.clear();
        labelAddresses.clear();
        dataDefinitionsToPatch.clear();
        labelsToPatchInDD.clear();
    }

    public List<Command> getCommands() {
        return commands;
    }

    private void advanceToken() {
        tokenIndex += 1;
    }

    private Token nextToken() {
        Token tk = peekToken();
        advanceToken();
        return tk;
    }

    private Token prevToken() {
        assert(tokenIndex >= 0);
        return tokens.get(tokenIndex - 1);
    }

    private Token peekToken() {
        if (tokenIndex >= tokens.size()) return new Token(-1, -1, "", UNKNOWN);
        return tokens.get(tokenIndex);
    }

    private Token peekPeekToken() {
        int pos = tokenIndex + 1;
        if (pos >= tokens.size()) return new Token(-1, -1, "", UNKNOWN);
        return tokens.get(pos);
    }

    private void eat(Token.Type type) {
        Token tk = nextToken();
        if (tk.type != type) {
            error(tk, "Unexpected Token, expected: % but got: % with lexeme: %", type.name(), tk.type.name(), tk.lexeme);
        }
    }

    private Token expect(Token.Type expected, String message) {
        Token tk = nextToken();
        if (tk.type != expected) errorUnexpectedToken(tk, message, expected);
        return tk;
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
        int size = 0;
        for (Command cmd : commands) size += cmd.size();

        byte[] machineCode = new byte[size];
        int i = 0;

        for(Command cmd : commands) {
            byte[] bytes = cmd.generateMachineCode();
            for(byte b : bytes) {
                machineCode[i++] = b;
            }
        }

        return machineCode;
    }

    public void parse(String input) {
        reset();
        Lexer lx = new Lexer(input);
        tokens = lx.getTokens();

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
        // It's not really needed since SEG doesn't do anything important
    }

    private void parseEnd() {
        // This really does nothing
    }

    private void parseLabel(Token name) {
        eat(COLON);

        boolean labelExists = labelAddresses.containsKey(name.lexeme);
        if (labelExists) error(name, "Label % has already been defined.", name.lexeme);
        else labelAddresses.put(name.lexeme, currentAddress);
    }

    private void parseKeyword(Token command) {

        //@Speed These String comparisons are probably slow.
        Command cmd = switch (command.lexeme) {
            case "HALT", "RET", "PUSHR", "POPR" -> parseZeroOpCommand(command);

            case "DD" -> parseDataDefinition(command);

            case "RES" -> parseReserve(command);

            case "CMP", "MOVE", "MOVEN", "MOVEC" -> parseTwoOpCommand(command);

            case "MOVEA" -> parseMOVEAorCONV(command, true);

            case "CONV" -> parseMOVEAorCONV(command, false);

            case "OR", "ANDNOT", "XOR", "ADD", "SUB", "MULT", "DIV"->
                    parseTwoOrThreeOpCommand(command);

            case "SH", "ROT" -> parseThreeOpCommand(command);

            case "CLEAR" -> parseCLEAR(command);

            case "JEQ", "JNE", "JGT", "JGE", "JLT", "JLE",
                    "JC", "JNC", "JV", "JNV", "JUMP", "CALL" ->
                    parseSingleOpCommand(command);
            default -> null;
        };

        if (cmd == null) error(command, "Unknown Command: %", command.lexeme);
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
            if (twoOperandsNeeded && a3 != null) error(a3, "Expected two arguments for % but got three", command.lexeme);
            if (!twoOperandsNeeded && a3 == null) error(a2, "Expected three arguments for % but only got two", command.lexeme);
        }

        OpCode op = OpCode.getOpCode(command.lexeme, size, operands);

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



        return new AST_Add(op, command.row, address, command.col, command.colEnd(), a1, a2, null);
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

        Operand op = switch(tk.type) {
            /*
            FIRST Mengen:
                <absolute>  = +, -, CONSTANT, ident
                <immediate> = I
                <register>  = Rx, SP, PC
                <relative>  = !Rx, !SP, !PC, +, -, CONSTANT
                <indirekt>  = !!Rx, !!SP, !!PC, !<relativ>
                <indz rela> = <relativ> /Rx/
                <indz indr> = <indirekt> /Rx/
                <keller>    = -!Rx, !Rx+
             */
            case IDENTIFIER -> parseAbsoluteAddress(tk);
            case I -> parseImmediateOperand(size);
            case REGISTER -> parseRegisterAddress(tk);
            case PLUS, CONSTANT -> parseAbsoluteAddressOrRelativeAddress(tk);
            case MINUS -> parseAbsOrRelOrStack(tk);
            case BANG -> parseOtherTypes(tk);
            default -> null; //TODO Error
        };

        if (op == null) errorUnexpectedToken(tk, "trying to parse an Operand", IDENTIFIER, I, REGISTER, PLUS, MINUS, CONSTANT, BANG);
        return op;
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
        var dataDefinition = new AST_DataDefinition(null, command.row, address, command.col, -1, bytes, labelsToPatchInDD);

        if(!labelsToPatchInDD.isEmpty()) dataDefinitionsToPatch.add(dataDefinition);
        labelsToPatchInDD = new ArrayList<>();


        return dataDefinition;
    }

    private ArrayList<Byte> parseDataGroup() {
        ArrayList<Byte> bytes = new ArrayList<>();

        Token tk = nextToken();
        OpCode.DataType size = getDataType(tk);
        if (size != NONE) tk = nextToken();

        while (true) {
            bytes.addAll(parseDataElement(size, tk));
            if (!match(COMMA)) break;
            tk = nextToken();
        }

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
            case B -> {
                Token num = nextToken();
                long number = Integer.parseInt(num.lexeme, 2);
                return getBytesFromNumber(number, size);
            }
            case H -> {
                Token num = nextToken();
                long number = Integer.parseInt(num.lexeme, 16);
                return getBytesFromNumber(number, size);
            }
            case IDENTIFIER -> {
                String labelName = tk.lexeme;

                labelsToPatchInDD.add(labelName);
                for (int i = 0; i < 4; i++) bytes.add((byte) 0xDD);
                return bytes;
            }
            default -> errorUnexpectedToken(tk, "parsing Data Definition", CONSTANT, MINUS, OPEN_PAREN, B, H, IDENTIFIER);
        }

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
                int bits = Float.floatToIntBits(number);
                bytes = getBytesFromNumber(bits, WORD);
                break;
            case DOUBLE:
                long longBits = Double.doubleToLongBits(number);
                bytes.addAll(getBytesFromNumber(longBits >> 32, WORD));
                bytes.addAll(getBytesFromNumber((int) longBits, WORD));
                break;
        }

        return bytes;
    }

    private Operand parseAbsoluteAddressOrRelativeAddress(Token tk) {
        if (tk.type == PLUS || tk.type == MINUS) {
            if (peekToken().type == CONSTANT && peekPeekToken().type == PLUS)
                return parseIndexedOrRelativeAddress(tk);
        }
        else if (tk.type == CONSTANT && peekToken().type == PLUS)
            return parseIndexedOrRelativeAddress(tk);

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
            case PLUS -> {}
            case CONSTANT -> {
                int address = Integer.parseInt(tk.lexeme);
                return new AbsoluteAddress(address);
            }
        }
        Token constant = expect(CONSTANT, "parsing Absolute Address");

        int address = sign * Integer.parseInt(constant.lexeme);
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
        else if(tk.type != BANG) {
            errorUnexpectedToken(tk, "while parsing Stack Addressing", MINUS, BANG);
            return null;
        }

        Token regTk = nextToken();
        RegisterAddress reg = parseRegisterAddress(regTk);
        if (plus) eat(PLUS);

        return new StackAddress(reg.getReg(), plus);
    }

    private Operand parseRelativeOrIndirectAddress(Token tk) {
        Token.Type nextType = peekToken().type;

        if (nextType == REGISTER) return parseIndexedOrRelativeAddress(tk);

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
        // or maybe add PLUS and MINUS to the Constant that comes after them in the Lexer
        switch (tk.type) {
            case PLUS -> {
                Token number = expect(CONSTANT, "parsing Constant for Relative Address");
                offset = Integer.parseInt(number.lexeme);
            }
            case MINUS -> {
                Token number = expect(CONSTANT, "parsing Constant for Relative Address");
                offset = - Integer.parseInt(number.lexeme);
            }
            case CONSTANT -> offset = Integer.parseInt(tk.lexeme);
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

        RelativeAddress relAddress;
        if (match(OPEN_PAREN)) {
            relAddress = parseRelativeAddress(nextToken());
            eat(CLOSE_PAREN);
        }
        else if (match(BANG)) {
            relAddress = parseRelativeAddress(prevToken());
        }
        else {
            errorUnexpectedToken(peekToken(), "parsing IndirectAddress", OPEN_PAREN, BANG);
            return null;
        }

        // Relative and Indirect use the same information for generating
        // machine code.
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

        return parseInteger(size);
    }

    //TODO: Here and in parseInteger, check if provided number fits into the specified size
    private ImmediateOperand parseFloat(OpCode.DataType size) {
        // @Note: We simply parse everything as a Double first
        // and later cast it to a float if a float is specified

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
                number = - Double.parseDouble(floatToParse);
            }
            case CONSTANT -> {
                String floatToParse = tk.lexeme;
                if (match(POINT)) {
                    String afterDecimalPoint = peekToken().type == CONSTANT ? nextToken().lexeme : "";
                    floatToParse = tk.lexeme + "." + afterDecimalPoint;
                }
                else {
                    Token maybeE = peekToken();
                    if (maybeE.type == IDENTIFIER && (maybeE.lexeme.equals("E") || maybeE.lexeme.equals("e"))) {
                        advanceToken();
                        double base = Double.parseDouble(floatToParse);

                        int sign = 1;
                        if (match(MINUS)) sign = -1;
                        else match(PLUS);

                        Token numTk = expect(CONSTANT, "parsing a float, you need to specify a number after using e/E");

                        int exp = sign * Integer.parseInt(numTk.lexeme);
                        number = base * Math.pow(10, exp);
                        break;
                    }
                }
                number = Double.parseDouble(floatToParse);
            }
            case B -> {
                Token num = nextToken();
                long floatSavedInLong = Long.parseLong(num.lexeme, 2);
                return new ImmediateOperand(floatSavedInLong, size);
            }
            case H -> {
                Token num = nextToken();
                long floatSavedInLong = Long.parseLong(num.lexeme, 16);
                return new ImmediateOperand(floatSavedInLong, size);
            }
            default -> errorUnexpectedToken(tk, "parsing Float", MINUS, CONSTANT, B, H);
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
            case B -> {
                Token num = nextToken();
                number = Long.parseLong(num.lexeme, 2);
            }
            case H -> {
                Token num = nextToken();
                number = Long.parseLong(num.lexeme, 16);
            }
            default -> {
                number = 0xCCCC_CCCC;
                errorUnexpectedToken(tk, "parsing Integer", MINUS, CONSTANT, B, H);
            }
        }
        return new ImmediateOperand(number, size);
    }

    private void patchLabels() {
        for (RelativeAddress adr : labelsToPatch) {
            Integer address = labelAddresses.get(adr.labelName);

            if (address == null) {
                error(adr, "Undefined Label: % ", adr.labelName);
                continue;
            }

            int oldSize = adr.size();
            adr.patchLabel(address);
            int newSize = adr.size();

            int offset = newSize - oldSize;
            if (offset != 0) {
                patchLabelAddresses(adr.address, offset);
                patchRelativeAddresses(adr.address, offset);
            }
        }

        for (AST_DataDefinition dataDefinition : dataDefinitionsToPatch) {
            for (String labelName : dataDefinition.labelsToPatch) {
                Integer address = labelAddresses.get(labelName);

                if (address == null) error(dataDefinition, "Undefined Label: % ", labelName);
                else dataDefinition.patchAddress(address);
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
                    // instead of using labels. Do we need to patch those too? Or not?
                    if (rel.regX != 15) continue;

                    if (rel.address > startingAddress) {
                        rel.address += offset;
                        if (rel.address + rel.offset <= startingAddress) {
                            rel.offset -= offset;
                        }
                    }
                    if (rel.address <= startingAddress && rel.address + rel.offset > startingAddress) {
                        rel.offset += offset;
                    }
                }
            }
        }
    }

    private void error(int row, int col, String message) {
        String prefix = "ERROR while Parsing:\n";
        String info = "[" + row + ", " + col + "] ";
        out.println(prefix + info + message);
    }

    private void error(int row, int col, String format, String... args) {
        assert(format.chars().filter(ch -> ch == '%').count() == args.length);

        StringBuilder sb = new StringBuilder();

        int argsIndex = 0;
        for (int i = 0; i < format.length(); i++) {
            char c = format.charAt(i);

            if (c == '%') sb.append(args[argsIndex++]);
            else sb.append(c);
        }

        error(row, col, sb.toString());
    }

    private void error(Token tk, String format, String... args) {
        error(tk.row, tk.col, format,args);
    }

    private void error(Command cmd, String format, String... args) {
        error(cmd.row, cmd.beg, format, args);
    }

    private void error(Operand op, String format, String... args) {
        error(-1, -1, format, args);
    }

    private void errorUnexpectedToken(Token got, String infix, Token.Type... expected) {
        StringBuilder format = new StringBuilder("Unexpected Token while " + infix + "\nGot: " + got.type + " with lexeme: " + got.lexeme + "\nExpected: (");
        for (int i = 0; i < expected.length; i++) {
            if (i != 0) format.append(", ");

            Token.Type type = expected[i];
            format.append(type);
        }
        format.append(")");

        error(got.row, got.col, format.toString());
    }

}
