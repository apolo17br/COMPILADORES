//package gals;
//
//import gals.Symbol.Type;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Stack;
//import ide.IDE;
//
//public class SemanticoOld implements Constants
//{
//  
//  private enum Mode {
//    NONE,
//    VARIABLE,
//    CONSTANT,
//    DECLARING_FUNCTION,
//    DECLARING_FUNCTION_PARAMETERS,
//    ATTRIBUTE_ASSIGNMENT,
//    ATTRIBUTE_DECLARATION
//  }
//  
//  
//  //Stack escoposFuncao = new Stack(), expressao = new Stack();
//  //public List<Simbolo> TabelaSimbolos = new ArrayList();
//  //int pos = 0, indiceVariavel = 0, idEscopo = 0;
//  
//  // Symbols Table
//  public ArrayList<Symbol> symbolTable = new ArrayList<>();
//  
//  // Semantic Table
//  public Stack<Integer> semanticTable = new Stack<>();
//  
//  // Value Table
//  public Stack<String> valueTable = new Stack<>();
//  
//  // Assembler
//  private final Assembler assembler = new Assembler();
//  
//  // Current mode
//  private Mode mode = Mode.NONE;
//  
//  // Scope Stack
//  Stack<String> scopeStack = new Stack<>();
//  
//  // Temp variables
//  private String function;
//  private String variableOrConstant;
//  private Symbol.Type type;
//  private int position = 0;
//  private boolean array = false;
//  private int arraySize = 0;
//  private ArrayList<Symbol> parametersToBeAdded = new ArrayList<>();
//  private int innerScopeCount = 0;
//  private int parameterCount = 0;
//  private boolean flagExp = false;
//    
//  /**
//   * #1   =   FUNCTION
//   * #2   =   TYPE
//   * #3   =   
//   * #4   =   FUNCTION PARAMETERS (START)
//   * #5   =   FUNCTION PARAMETERS (END)
//   * #6   =   VARIABLE / CONSTANT 
//   * #7   =   COMMA
//   * #8   =   INDEX OPEN
//   * #9   =   INDEX CLOSE
//   * #10  =   SCOPE_OPEN
//   * #11  =   SCOPE_CLOSE
//   * #12  =   VARIABLE / CONSTANT (ATTRIBUTE)
//   * #13  =   COMMAND (AFTER)
//   * #14  =   ATTRIBUTES ASSIGNMENT COMMAND (END)
//   * #15  =   ARRAY INDEX
//   * #16  =   ATTRIBUTE ASSIGNMENT COMMAND (START)
//   * #17  =   ATTRIBUTE DECLARATION COMMAND
//   */
//    
//  /**
//   * Executes the current semantic action
//   * @param action Semantic action
//   * @param token Current token
//   * @throws SemanticError 
//   */
//  public void executeAction(int action, Token token) throws SemanticError
//  {
//    // System.out.println("#" + action + " - " + token.getLexeme());
//    
//    switch (action) {
//      
//      // FUNCTION
//      case 1:
//        if(this.scopeStack.empty()) {
//          this.function = token.getLexeme();
//          this.mode = Mode.DECLARING_FUNCTION;
//        }
//        break;
//      
//      // TYPE
//      case 2:
//        if (this.mode == Mode.DECLARING_FUNCTION) {
//          this.type = parseType(token.getLexeme());
//          this.addFunction();
//          this.addParameters();
//        } else if (this.mode == Mode.DECLARING_FUNCTION_PARAMETERS) {
//          this.type = parseType(token.getLexeme());
//        }
//        break;
//
//      // FUNCTION PARAMETERS (START)
//      case 4:
//        this.mode = Mode.DECLARING_FUNCTION_PARAMETERS;
//        
//        break;
//        
//      // FUNCTION PARAMETERS (END)
//      case 5:
//        if(this.parameterCount > 0)
//          this.addParameter();
//        this.mode = Mode.DECLARING_FUNCTION;
//        this.position = 0;
//        this.parameterCount = 0;
//        break;
//        
//      // VARIABLE / CONSTANT
//      case 6:
//        if (this.mode == Mode.DECLARING_FUNCTION_PARAMETERS) {
//          this.parameterCount++;
//          this.variableOrConstant = token.getLexeme();
//        }
//        break;
//        
//      // COMMA
//      case 7:
//        if (this.mode == Mode.DECLARING_FUNCTION_PARAMETERS) {
//          this.addParameter();
//          this.array = false;
//        }
//        
//        break;
//        
//      // INDEX CLOSE
//      case 9:
//        if (this.mode == Mode.DECLARING_FUNCTION_PARAMETERS) {
//          this.array = true;
//        }
//        break;
//      
//      // SCOPE OPEN
//      case 10:
//        if (scopeStack.empty()) {
//          scopeStack.push(this.function);
//        } else {
//          scopeStack.push(this.function + "-" + Integer.toString(innerScopeCount));
//          this.innerScopeCount++;
//        }
//        break;
//      
//      // SCOPE CLOSE
//      case 11:
//        String temp = scopeStack.pop();
//        if (scopeStack.empty()) {
//          this.checkForNotUsed(temp);
//          this.innerScopeCount = 0;
//          this.mode = Mode.NONE;
//        }
//        break;
//        
//      // VARIABLE / CONSTANT (ATTRIBUTE)
//      case 12:
//        this.variableOrConstant = token.getLexeme();
//        this.type = Type.UNDEFINED;
//        if(flagExp)
//          this.assembler.addToText("LD", scopeStack.peek().replace("@", "") + "_" + token.getLexeme().replace("$", ""));
//        break;
//        
//      // COMMAND (AFTER)
//      case 13:
//        if(this.mode == Mode.ATTRIBUTE_ASSIGNMENT || this.mode == Mode.ATTRIBUTE_DECLARATION) {
//          if(this.isConstant(this.variableOrConstant))
//            addConstant();
//          else
//            addVariable(); 
//        }
//   
//        // Reset temp variables
//        this.mode = Mode.NONE;
//        this.array = false;
//        this.type = Type.UNDEFINED;
//        break;
//        
//      // ATTRIBUTES ASSIGNMENT COMMAND
//      case 14:
//        this.mode = Mode.ATTRIBUTE_ASSIGNMENT;
//        this.type = this.resolveExpression();
//        break;
//        
//      // ARRAY INDEX
//      case 15:
//        if (this.mode == Mode.ATTRIBUTE_ASSIGNMENT)
//          this.array = true;
//        break;
//        
//      // ATTRIBUTE ASSIGNMENT COMMAND (START)
//      case 16:
//        this.mode = Mode.ATTRIBUTE_ASSIGNMENT;
//        flagExp = true;
//        break;
//        
//      // ATTRIBUTE DECLARATION COMMAND
//      case 17:
//        this.mode = Mode.ATTRIBUTE_DECLARATION;
//        break;
//        
//        
//      // EXPRESSION TYPES
//        
//      // INT
//      case 50:
//        if(this.mode == Mode.ATTRIBUTE_ASSIGNMENT) {
//          this.semanticTable.push(0);
//          this.valueTable.push(token.getLexeme());
//        }
//        break;
//        
//      // FLOAT
//      case 51:
//        if(this.mode == Mode.ATTRIBUTE_ASSIGNMENT)
//          this.semanticTable.push(1);
//        break;
//        
//      // CHAR
//      case 52:
//        if(this.mode == Mode.ATTRIBUTE_ASSIGNMENT)
//          this.semanticTable.push(2);
//        break;
//        
//      // STRING
//      case 53:
//        if(this.mode == Mode.ATTRIBUTE_ASSIGNMENT)
//          this.semanticTable.push(3);
//        break;
//        
//      // BOOLEAN
//      case 54:
//        if(this.mode == Mode.ATTRIBUTE_ASSIGNMENT)
//          this.semanticTable.push(4);
//        break;
//        
//      // VARIABLE / CONST
//      case 55:
//        if(this.mode == Mode.ATTRIBUTE_ASSIGNMENT) {
//          this.semanticTable.push(this.getType(token.getLexeme()));
//          this.valueTable.push(token.getLexeme());
//        }
//        break;
//        
//      // EXPRESSION OPERATORS
//      
//      // ADD
//      case 60:
//        if(this.mode == Mode.ATTRIBUTE_ASSIGNMENT)
//          this.semanticTable.push(0);
//        break;
//        
//      // SUB
//      case 61:
//        if(this.mode == Mode.ATTRIBUTE_ASSIGNMENT)
//          this.semanticTable.push(1);
//        break;
//        
//      // MULT
//      case 62:
//        if(this.mode == Mode.ATTRIBUTE_ASSIGNMENT)
//          this.semanticTable.push(2);
//        break;
//        
//      // DIV
//      case 63:
//        if(this.mode == Mode.ATTRIBUTE_ASSIGNMENT)
//          this.semanticTable.push(3);
//        break;
//        
//      // RELATIONAL
//      case 64:
//        if(this.mode == Mode.ATTRIBUTE_ASSIGNMENT)
//          this.semanticTable.push(4);
//        break;
//    }
//  }	
//  
//  
//////detecta se a variavel a ser atribuido o valor foi declarada
////    public void detectaVariavelAtribuicaoDireta(String t) throws SemanticError {
////        int in = 0, inachado = 0;
////        Simbolo sim = null;
////        for (Simbolo i : TabelaSimbolos) {
////            if (i.getId().equals(t) && i.getEscopo() == (int) escoposFuncao.peek()) {
////                sim = i;
////                expressao.push(i.getTipo());
////                inachado = in;
////                break;
////
////            } else if (i.getId().equals(t)) {
////                if (sim == null) {
////                    sim = new Simbolo(t, (int) escoposFuncao.peek());
////                    inachado = in;
////                } else {
////                    if (sim.escopo < i.getEscopo()) {
////                        sim = i;
////                        inachado = in;
////                    }
////                }
////
////            }
////            in++;
////        }
////        if (sim == null) {
////            throw new SemanticError("A variável '" + t + "' utilizada não foi declarada");
////        }
////        expressao.push(sim.getTipo());
////        TabelaSimbolos.get(inachado).setUsada(true);
////        indiceVariavel = in - 1;
////    }  
//  
//  /**
//   * Adds a function to the Symbol Table
//   * @return True if the operation was successful
//   * @throws SemanticError If the function had already been declared
//   */
//  private boolean addFunction() throws SemanticError {
//    if (functionExists(this.function))
//      throw new SemanticError("Function " + this.function + 
//              " had already been declared before");
//    
//    this.symbolTable.add(new Symbol(this.function, this.type, false,
//            "global", false, 0, false, 0, false, false));
//    
//    return true;
//  }
//  
//  /**
//   * Adds a parameter to the temporary array
//   * @return True if the operation was successful
//   * @throws SemanticError If the parameter had already been declared
//   */
//  private boolean addParameter() throws SemanticError {
//    if (parameterExists(this.variableOrConstant))
//      throw new SemanticError("Parameter " + this.variableOrConstant +
//              " had already been declared before");
//    
//    this.parametersToBeAdded.add(new Symbol(this.variableOrConstant, this.type, false,
//            this.function, true, this.position, this.array, this.arraySize, false, false));
//    
//    this.position++;
//    
//    return true;
//  }
//  
//  /**
//   * Adds all parameters to the Symbol Table
//   */
//  private void addParameters() {
//    for (Symbol symbol : this.parametersToBeAdded) {
//      this.symbolTable.add(symbol);
//    }
//    
//    this.parametersToBeAdded.clear();
//  }
//  
//  /**
//   * Adds a constant to the Symbol Table
//   */
//  private void addConstant() {
//    if (!identifierExists(this.variableOrConstant))
//      this.symbolTable.add(new Symbol(this.variableOrConstant, this.type, false,
//            this.scopeStack.peek(), false, 0, this.array, this.arraySize, false, false));
//  }
//  
//  /**
//   * Adds a variable to the Symbol Table
//   */
//  private void addVariable() {
//    if (!identifierExists(this.variableOrConstant)) {
//      this.symbolTable.add(new Symbol(this.variableOrConstant, this.type, false,
//            this.scopeStack.peek(), false, 0, this.array, this.arraySize, false, false));
//      this.assembler.addToData(this.scopeStack.peek().replace("@", "") + "_" + this.variableOrConstant.replace("$", ""), Integer.toString(0));
//    }
//  }
//  
//
//  
//  /**
//   * Checks if the given parameter had already been declared
//   * @param name Parameter name to check
//   * @return True if the parameter had already been declared
//   */
//  private boolean parameterExists(String name) {
//    for (Symbol symbol : parametersToBeAdded) {
//      if (symbol.getIdentifier().equals(name))
//        return true;
//    }
//    
//    return false;
//  }
//
//
//  
//
//  
//  /**
//   * Checks if the given identifier is a constant
//   * @param id Identifier to check
//   * @return True if the identifier is a constant
//   */
//  private boolean isConstant(String id) {
//    return id.startsWith("&");
//  }
//  
//  /**
//   * Checks if the given identifier is a variable
//   * @param id Identifier to check
//   * @return True if the identifier is a variable
//   */
//  private boolean isVariable(String id) {
//    return id.startsWith("$");
//  }
//  
//  /**
//   * Checks if the given identifier is a function
//   * @param id Identifier to check
//   * @return True if the identifier is a function
//   */
//  private boolean isFunction(String id) {
//    return id.startsWith("@");
//  }
//  
//  private int getType(String name) throws SemanticError {
//    Iterator iterator = scopeStack.iterator();
//    String scope = "";
//    while (iterator.hasNext()) {
//      scope = (String) iterator.next();
//      for (Symbol symbol : symbolTable) {
//        if (symbol.getIdentifier().equals(name) && symbol.getScope().equals(scope)) {
//          symbol.setUsed(true);
//          switch (symbol.getType()) {
//            case INT:
//              return 0;
//            case FLOAT:
//              return 1;
//            case CHAR:
//              return 2;
//            case STRING:
//              return 3;
//            case BOOLEAN:
//              return 4;
//            case UNDEFINED:
//              throw new SemanticError("Variable " + name + " was not initialized");
//          }
//        }
//      }
//    }
//    
//    throw new SemanticError("Variable " + name + " not declared in scope " + scope);
//  }
//  
//  private Type resolveExpression() throws SemanticError {
//    this.assembler.addToText("LDI", String.valueOf(valueTable.pop()));
//    
//    while (semanticTable.size() > 1) {
//      // TYPES
//      int type1 = semanticTable.pop();
//      int operator = semanticTable.pop();
//      int type2 = semanticTable.pop();
//      
//      int result = SemanticTable.resultType(type1, type2, operator);
//      
//      if (result != SemanticTable.ERR) {
//        semanticTable.push(result);
//      } else {
//        throw new SemanticError("Invalid expression");
//      }
//      
//      // VALUES
//      if (type1 == 0 && type2 == 0) {
//        if(operator == 0) {
//          this.assembler.addToText("ADDI", String.valueOf(valueTable.pop()));
//        } else if (operator == 1) {
//          this.assembler.addToText("SUBI", String.valueOf(valueTable.pop()));
//        }
//      }
//    }
//    
//    flagExp = false;
//    this.assembler.addToText("STO", this.scopeStack.peek().replace("@", "") + "_" + this.variableOrConstant.replace("$", ""));
//    
//    int lastType = semanticTable.pop();
//    
//    switch(lastType) {
//      case 0:
//        return Type.INT;
//      case 1:
//        return Type.FLOAT;
//      case 2:
//        return Type.CHAR;
//      case 3:
//        return Type.STRING;
//      case 4:
//        return Type.BOOLEAN;
//    }
//    
//    return Type.UNDEFINED;
//  }
//  
//
//}
