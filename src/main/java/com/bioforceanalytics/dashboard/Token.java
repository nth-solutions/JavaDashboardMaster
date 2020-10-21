package com.bioforceanalytics.dashboard;
import java.util.List;
import java.util.ArrayList;

enum TokenType{
    OPERATOR,AXIS,VARIABLE, CONST;
}
enum Operation{
    
    ADD,SUBTRACT,MULTIPLY,DIVIDE,EXP,OPEN_PAREN,CLOSE_PAREN;
}

public class Token {

    public static List<AxisData> intermediateAxes = new ArrayList<AxisData>();
    String stringVal;
    TokenType type;
    Operation operation;
    AxisData axis;
    double constant;
    public Token(AxisData axis){
        this.axis = axis;
        type = TokenType.AXIS;
    }
    public Token(double constant){
        this.constant = constant;
        type = TokenType.CONST;
    }
    public boolean isInOrder(int order){
        switch(operation){
            case EXP:
                if(order>0) return true;
                return false;
            case MULTIPLY:
                if(order>1) return true;
                return false;
            case DIVIDE:
                if(order>1) return true;
                return false;
            case ADD:
                if(order>2) return true;
                return false;
            case SUBTRACT:
                if(order>2) return true;
                return false;
            default:
            return false;
        }
    }
    public String toString(){
        switch(type){
            case OPERATOR:
                return operation.toString();
            case AXIS:
                return axis.toString();
            case CONST:
                return "" + constant;
            case VARIABLE:
                return stringVal;
            default:
                return "";
        }
    }
    public Token(String value){
        stringVal = value;
        type = TokenType.OPERATOR;
        switch(stringVal){
            case "(":
                operation = Operation.OPEN_PAREN;
                break;
            case ")":
                operation = Operation.CLOSE_PAREN;
                break;
            case "^":
                operation = Operation.EXP;
                break;
            case "*":
                operation = Operation.MULTIPLY;
                break;
            case "/":
                operation = Operation.DIVIDE;
                break;
            case "+":
                operation = Operation.ADD;
                break;
            case "-":
                operation = Operation.SUBTRACT;
                break;
            default:
                if((axis=AxisData.getAxisData(stringVal)) != null){
                    type = TokenType.AXIS;
                    break;
                }else if (AxisData.nameAxisDataMap.containsKey(stringVal) == true){
                    axis = AxisData.nameAxisDataMap.get(stringVal);
                    type = TokenType.AXIS;
                    break;
                }
                else{
                    try{
                        type=TokenType.CONST;
                        constant = Double.parseDouble(stringVal);
                    } catch(Exception e){
                        type=TokenType.VARIABLE;
                    }
                }
        }
    }
    public Token operate(Token a, Token b){
        if(type==TokenType.OPERATOR);
            switch(operation){
                case EXP:
                    return exp(a,b);
                case MULTIPLY:
                    return multiply(a,b);
                case DIVIDE:
                    return divide(a,b);
                case ADD:
                    return add(a,b);
                case SUBTRACT:
                    return subtract(a,b);
                default:
                return a;
            }
        }

    public static Token add(Token a, Token b){
        AxisData result;
        
        if(a.type == TokenType.AXIS || b.type == TokenType.AXIS){
            if(a.type == TokenType.AXIS && b.type == TokenType.AXIS){
                result = a.axis.add(b.axis,"$" + (intermediateAxes.size()));
            }else if (a.type == TokenType.AXIS){
                result = a.axis.add(b.constant,"$" + (intermediateAxes.size()));
            }else{
                result = b.axis.add(a.constant,"$" + (intermediateAxes.size()));
            }
            intermediateAxes.add(result);
            return new Token(result);
        }
        else{
            return new Token(a.constant + b.constant);
        }
    }
    public static Token subtract(Token a, Token b){
        AxisData result;
        
        if(a.type == TokenType.AXIS || b.type == TokenType.AXIS){
            if(a.type == TokenType.AXIS && b.type == TokenType.AXIS){
                result = a.axis.subtract(b.axis,"$" + (intermediateAxes.size()));
            }else if (a.type == TokenType.AXIS){
                result = a.axis.subtract(b.constant,"$" + (intermediateAxes.size()));
            }else{
                result = b.axis.subtract(a.constant,"$" + (intermediateAxes.size()));
            }
            intermediateAxes.add(result);
            return new Token(result);
        }
        else{
            return new Token(a.constant - b.constant);
        }
    }
    public static Token multiply(Token a, Token b){
        AxisData result;
        if(a.type == TokenType.AXIS || b.type == TokenType.AXIS){
            if(a.type == TokenType.AXIS && b.type == TokenType.AXIS){
                result = a.axis.multiply(b.axis,"$" + (intermediateAxes.size()));
            }else if (a.type == TokenType.AXIS){
                result = a.axis.multiply(b.constant,"$" + (intermediateAxes.size()));
            }else{
                result = b.axis.multiply(a.constant,"$" + (intermediateAxes.size()));
            }
            intermediateAxes.add(result);
            return new Token(result);
        }
        else{
            return new Token(a.constant * b.constant);
        }
    }
    public static Token divide(Token a, Token b){
        AxisData result;
        if(a.type == TokenType.AXIS || b.type == TokenType.AXIS){
            if(a.type == TokenType.AXIS && b.type == TokenType.AXIS){
                result = a.axis.divide(b.axis,"$" + (intermediateAxes.size()));
            }else if (a.type == TokenType.AXIS){
                result = a.axis.divide(b.constant,"$" + (intermediateAxes.size()));
            }else{
                result = b.axis.divide(a.constant,"$" + (intermediateAxes.size()));
            }
            intermediateAxes.add(result);
            return new Token(result);
        }
        else{
            return new Token(a.constant / b.constant);
        }
    }
    public static Token exp(Token a, Token b){
        AxisData result;
        if(a.type == TokenType.AXIS || b.type == TokenType.AXIS){
            if(a.type == TokenType.AXIS && b.type == TokenType.AXIS){
                result = a.axis;
            }else if (a.type == TokenType.AXIS){
                result = a.axis.exp(b.constant,"$" + (intermediateAxes.size()));
            }else{
                result = b.axis.exp(a.constant,"$" + (intermediateAxes.size()));
            }
            intermediateAxes.add(result);
            return new Token(result);
        }
        else{
            return new Token(Math.pow(a.constant,  b.constant));
        }
    }
}