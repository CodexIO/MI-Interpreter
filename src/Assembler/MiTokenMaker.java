package Assembler;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMaker;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;

import javax.swing.text.Segment;

public class MiTokenMaker extends AbstractTokenMaker {
    @Override
    public TokenMap getWordsToHighlight() {
        TokenMap tokenMap = new TokenMap();

        for (OpCode op : OpCode.values()) {
            tokenMap.put(op.name, Token.RESERVED_WORD);
        }

        tokenMap.put("EQU", Token.RESERVED_WORD);
        tokenMap.put("DD", Token.RESERVED_WORD);
        tokenMap.put("RES", Token.RESERVED_WORD);
        tokenMap.put("SEG", Token.RESERVED_WORD);
        tokenMap.put("END", Token.RESERVED_WORD);

        tokenMap.put("B", Token.DATA_TYPE);
        tokenMap.put("H", Token.DATA_TYPE);
        tokenMap.put("W", Token.DATA_TYPE);
        tokenMap.put("F", Token.DATA_TYPE);
        tokenMap.put("D", Token.DATA_TYPE);

        tokenMap.put(",", Token.SEPARATOR);

        tokenMap.put("R0", Token.VARIABLE);
        tokenMap.put("R1", Token.VARIABLE);
        tokenMap.put("R2", Token.VARIABLE);
        tokenMap.put("R3", Token.VARIABLE);
        tokenMap.put("R4", Token.VARIABLE);
        tokenMap.put("R5", Token.VARIABLE);
        tokenMap.put("R6", Token.VARIABLE);
        tokenMap.put("R7", Token.VARIABLE);
        tokenMap.put("R8", Token.VARIABLE);
        tokenMap.put("R9", Token.VARIABLE);
        tokenMap.put("R10", Token.VARIABLE);
        tokenMap.put("R11", Token.VARIABLE);
        tokenMap.put("R12", Token.VARIABLE);
        tokenMap.put("R13", Token.VARIABLE);
        tokenMap.put("R14", Token.VARIABLE);
        tokenMap.put("R15", Token.VARIABLE);
        tokenMap.put("SP", Token.VARIABLE);
        tokenMap.put("PC", Token.VARIABLE);

        return tokenMap;
    }

    @Override
    public void addToken(Segment text, int start, int end, int tokenType, int startOffset) {
        // This assumes all keywords, etc. were parsed as "identifiers."
        if (tokenType==Token.IDENTIFIER) {
            int value = wordsToHighlight.get(text, start, end);
            if (value != -1) {
                tokenType = value;
            }
        }
        super.addToken(text, start, end, tokenType, startOffset);
    }

    /**
     * Returns a list of tokens representing the given text.
     *
     * @param text The text to break into tokens.
     * @param startTokenType The token with which to start tokenizing.
     * @param startOffset The offset at which the line of tokens begins.
     * @return A linked list of tokens representing <code>text</code>.
     */
    @Override
    public Token getTokenList(Segment text, int startTokenType, int startOffset) {

        resetTokenList();

        char[] array = text.array;
        int offset = text.offset;
        int count = text.count;
        int end = offset + count;

        // Token starting offsets are always of the form:
        // 'startOffset + (currentTokenStart-offset)', but since startOffset and
        // offset are constant, tokens' starting positions become:
        // 'newStartOffset+currentTokenStart'.
        int newStartOffset = startOffset - offset;

        int currentTokenStart = offset;
        int currentTokenType  = startTokenType;

        for (int i=offset; i<end; i++) {

            char c = array[i];

            switch (currentTokenType) {

                case Token.NULL:

                    currentTokenStart = i;   // Starting a new token here.

                    switch (c) {

                        case ' ':
                        case '\t':
                            currentTokenType = Token.WHITESPACE;
                            break;

                        case '"':
                            currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
                            break;

                        case ',':
                            currentTokenType = Token.SEPARATOR;
                            break;

                        case '-':
                            if (i + 1 < end && array[i+1] == '-') currentTokenType = Token.COMMENT_EOL;
                            else currentTokenType = Token.IDENTIFIER;
                            break;

                        default:
                            if (RSyntaxUtilities.isDigit(c)) {
                                currentTokenType = Token.LITERAL_NUMBER_DECIMAL_INT;
                                break;
                            }
                            else if (RSyntaxUtilities.isLetter(c)) {
                                currentTokenType = Token.IDENTIFIER;
                                break;
                            }

                            // Anything not currently handled - mark as an identifier
                            currentTokenType = Token.IDENTIFIER;
                            break;

                    } // End of switch (c).

                    break;

                case Token.WHITESPACE:

                    switch (c) {

                        case ' ':
                        case '\t':
                            break;   // Still whitespace.

                        case '"':
                            addToken(text, currentTokenStart,i-1, Token.WHITESPACE, newStartOffset+currentTokenStart);
                            currentTokenStart = i;
                            currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
                            break;

                        case '-':
                            addToken(text, currentTokenStart,i-1, Token.WHITESPACE, newStartOffset+currentTokenStart);
                            currentTokenStart = i;

                            if(i + 1 < end && array[i+1] == '-') currentTokenType = Token.COMMENT_EOL;
                            else currentTokenType = Token.IDENTIFIER;
                            break;

                        default:   // Add the whitespace token and start anew.

                            addToken(text, currentTokenStart,i-1, Token.WHITESPACE, newStartOffset+currentTokenStart);
                            currentTokenStart = i;

                            if (RSyntaxUtilities.isDigit(c)) {
                                currentTokenType = Token.LITERAL_NUMBER_DECIMAL_INT;
                                break;
                            }
                            else if (RSyntaxUtilities.isLetter(c) || c=='/' || c=='_') {
                                currentTokenType = Token.IDENTIFIER;
                                break;
                            }

                            // Anything not currently handled - mark as identifier
                            currentTokenType = Token.IDENTIFIER;

                    } // End of switch (c).

                    break;

                default: // Should never happen
                case Token.IDENTIFIER:

                    switch (c) {
                        case ':':
                            addToken(text, currentTokenStart, i, Token.VARIABLE, newStartOffset + currentTokenStart);
                            currentTokenStart = i + 1;
                            currentTokenType = Token.NULL;
                            break;

                        case ' ':
                        case '\t':
                            addToken(text, currentTokenStart,i-1, Token.IDENTIFIER, newStartOffset+currentTokenStart);
                            currentTokenStart = i;
                            currentTokenType = Token.WHITESPACE;
                            break;

                        case ',':
                            addToken(text, currentTokenStart, i-1, Token.IDENTIFIER, newStartOffset + currentTokenStart);
                            currentTokenStart = i;
                            currentTokenType = Token.SEPARATOR;
                            break;

                        case '"':
                            addToken(text, currentTokenStart,i-1, Token.IDENTIFIER, newStartOffset+currentTokenStart);
                            currentTokenStart = i;
                            currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
                            break;

                        default:
                            if (RSyntaxUtilities.isLetterOrDigit(c) || c=='/' || c=='_') {
                                break;   // Still an identifier of some type.
                            }
                            // Otherwise, we're still an identifier (?).

                    } // End of switch (c).

                    break;

                case Token.LITERAL_NUMBER_DECIMAL_INT:

                    switch (c) {

                        case ' ':
                        case '\t':
                            addToken(text, currentTokenStart,i-1, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset+currentTokenStart);
                            currentTokenStart = i;
                            currentTokenType = Token.WHITESPACE;
                            break;

                        case '"':
                            addToken(text, currentTokenStart,i-1, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset+currentTokenStart);
                            currentTokenStart = i;
                            currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
                            break;

                        default:

                            if (RSyntaxUtilities.isDigit(c)) {
                                break;   // Still a literal number.
                            }

                            // Otherwise, remember this was a number and start over.
                            addToken(text, currentTokenStart,i-1, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset+currentTokenStart);
                            i--;
                            currentTokenType = Token.NULL;

                    } // End of switch (c).

                    break;

                case Token.COMMENT_EOL:
                    i = end - 1;
                    addToken(text, currentTokenStart,i, currentTokenType, newStartOffset+currentTokenStart);
                    // We need to set token type to null so at the bottom we don't add one more token.
                    currentTokenType = Token.NULL;
                    break;

                case Token.LITERAL_STRING_DOUBLE_QUOTE:
                    if (c=='"') {
                        addToken(text, currentTokenStart,i, Token.LITERAL_STRING_DOUBLE_QUOTE, newStartOffset+currentTokenStart);
                        currentTokenType = Token.NULL;
                    }
                    break;

                //case Token.SEPARATOR:
                //   addToken(text, currentTokenStart, i - 1, currentTokenType, newStartOffset + currentTokenStart);
                //    currentTokenType = Token.NULL;

            } // End of switch (currentTokenType).

        } // End of for (int i=offset; i<end; i++).

        switch (currentTokenType) {

            // Remember what token type to begin the next line with.
            case Token.LITERAL_STRING_DOUBLE_QUOTE:
                addToken(text, currentTokenStart,end-1, currentTokenType, newStartOffset+currentTokenStart);
                break;

            // Do nothing if everything was okay.
            case Token.NULL:
                addNullToken();
                break;

            // All other token types don't continue to the next line...
            default:
                addToken(text, currentTokenStart,end-1, currentTokenType, newStartOffset+currentTokenStart);
                addNullToken();

        }

        // Return the first token in our linked list.
        return firstToken;
    }
}
