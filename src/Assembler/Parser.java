package Assembler;

import Assembler.AST_Nodes.*;

import java.util.ArrayList;
import java.util.Dictionary;

public class Parser {

    private Lexer lx;
    private ArrayList<Token> tokens;
    private ArrayList<Anweisung> anweisungen;
    private Dictionary<String, Integer> labelAdresses;

    private int currentAdress = 0;

    public Parser(Lexer lexer) {
        lx = lexer;
        anweisungen = new ArrayList<Anweisung>();
    }

    public void parse() {

    }
}
