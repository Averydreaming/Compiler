package FrontEnd;

import AST.*;
import Util.*;
import Util.error.semanticError;

import java.util.ArrayList;

public class SemanticChecker implements ASTvisitor {
    public Scope scope, now;
    public Type res_type;
    public Classtype in_class;
    public boolean fl;
    public int dep;

    public SemanticChecker(Scope _scope) {
        scope = _scope;
        dep = 0;
    }

    @Override
    public void visit(RootNode it) {
        Funcsymbol func_main = scope.get_function("main", false, it.pos);
        now = scope;
        if (!func_main.type.is_int()) throw new semanticError("Invalid main", it.pos);
        if (func_main.para_list.size() > 0) throw new semanticError("Invalid main", it.pos);
        it.body.forEach(x -> x.accept(this));
    }

    @Override public void visit(TypeNode it) {}

    @Override
    public void visit(ClassdefNode it) {
        now = new Scope(now);
        in_class = (Classtype) scope.type_map.get(it.id);
        in_class.varmap.forEach((key, value) -> now.insert_variable(key, value, it.pos));
        in_class.funcmap.forEach((key, value) -> now.insert_function(key, value, it.pos));
        it.funclist.forEach(x -> x.accept(this));
        if (it.constructor != null) {
            if (!it.constructor.id.equals(it.id)) throw new semanticError("Invalid constructor!", it.pos);
            it.constructor.accept(this);
        }
        now = now.parent_scope;
        in_class = null;
    }

    @Override
    public void visit(FunctiondefNode it) {
        now = new Scope(now);
        if (it.paralist != null)
            it.paralist.forEach(x ->
            {
                x.varsymbol = new Varsymbol(x.id, scope.get_type(x.type));
                now.insert_variable(x.id, x.varsymbol, x.pos);
            });
        if (in_class != null) {
            it.funcsymbol.id2 = in_class.id + "_function_in_class_" + it.funcsymbol.id;
            it.funcsymbol.flag_class = true;
        } else it.funcsymbol.id2=it.funcsymbol.id;
        fl = false;
        if (it.type == null) res_type = new Literaltype("void");
        else res_type = scope.get_type(it.type);
        it.part.accept(this);
        it.flag_return = fl;
        if (it.id.equals("main")) fl = true;
        if (!fl && it.type != null && !it.type.type.equals("void")) throw new semanticError("Invalid return!", it.pos);
        now = now.parent_scope;
    }
    @Override public void visit(VardefstmtNode it) {it.varlist.forEach(x -> x.accept(this));}
    @Override public void visit(VardefsubstmtNode it) {
        Type tmp = scope.get_type(it.type);
        if (tmp.is_void()) throw new semanticError("Invalid Vardefsubstmt!", it.pos);
        if (it.expr  != null) {
            it.expr .accept(this);
            if (!tmp.equal(it.expr .type)) throw new semanticError("Invalid Vardefsubstmt!", it.pos);
        }
        it.varsymbol = new Varsymbol(it.id, tmp);
        if (now == scope) it.varsymbol.is_global = true;
        now.insert_variable(it.id, it.varsymbol, it.pos);
    }

    //
    @Override
    public void visit(PartstmtNode  it) {
        it.stmtlist.forEach(x ->
        {
            if (x instanceof PartstmtNode ) {
                now = new Scope(now);
                x.accept(this);
                now = now.parent_scope;
            } else x.accept(this);
        });
    }
    @Override public void visit(IfstmtNode it) {
        it.conditionexpr.accept(this);
        if (!it.conditionexpr.type.is_boolean()) throw new semanticError("Invalid If!", it.pos);
        {
            now = new Scope(now);
            it.truestmt.accept(this);
            now = now.parent_scope;
        }
        if (it.falsestmt != null) {
            now = new Scope(now);
            it.falsestmt.accept(this);
            now = now.parent_scope;
        }
    }
    @Override public void visit(WhilestmtNode it) {
        it.whileconditionexpr.accept(this);
        if (!it.whileconditionexpr.type.is_boolean()) throw new semanticError("Invalid While!", it.pos);
        now = new Scope(now);
        ++dep;
        it.body.accept(this);
        now = now.parent_scope;
        --dep;
    }
    @Override public void visit(ForstmtNode it) {
        if (it.initstmt != null) it.initstmt.accept(this);
        if (it.forconditionexpr!= null)
        {
            it.forconditionexpr.accept(this);
            if (!it.forconditionexpr.type.is_boolean()) throw new semanticError("Invalid For", it.pos);
        }
        if (it.stepexpr != null) it.stepexpr.accept(this);
        now = new Scope(now);
        ++dep;
        it.body.accept(this);
        now = now.parent_scope;
        --dep;
    }
    @Override
    public void visit(ReturnstmtNode it) {
        fl = true;
        if (it.res == null) {
            if (!res_type.is_void()) throw new semanticError("Invalid return!", it.pos);
        } else {
            it.res.accept(this);
            if (!res_type.equal(it.res.type)) throw new semanticError("Invalid return!", it.pos);
        }
    }
    @Override public void visit(BreakstmtNode it) {if (dep == 0) throw new semanticError("Invalid Break!", it.pos);}
    @Override public void visit(ContinuestmtNode it) { if (dep == 0) throw new semanticError("Invalid Continue!", it.pos);}
    @Override public void visit(EmptystmtNode it) {}
    @Override public void visit(ExprstmtNode it) {it.expr.accept(this);}
/*
   : '(' expr ')' #subexpr
    | constant #constexpr
    | This #thisexpr class 中函数调用
    | ID #idexpr
    | expr '(' exprList? ')' #funcexpr
    | expr '.' ID #classexpr// class 中函数调用 或者是已定义的内建函数 比如string.size/length/parseint/ord //
    | expr1=expr '[' expr2=expr ']' #arrexpr
     | <assoc=right> New newformat #newexpr
    | expr op=('++'|'--') #suffixexpr
    | <assoc=right> op=('++'|'--') expr #prefixexpr
    | <assoc=right> op=('+'|'-') expr #prefixexpr
    | <assoc=right> op=('!'|'~') expr #prefixexpr
    | src1=expr op=('*'|'/'|'%'|'+'|'-') src2=expr #binaryexpr
    | src1=expr op=('<<'|'>>') src2=expr #binaryexpr
    | src1=expr op=('<'|'>'|'<='|'>=') src2=expr #binaryexpr
    | src1=expr op=('=='|'!=') src2=expr #binaryexpr
    | src1=expr op=('&'|'^'|'|'|'&&'|'||') src2=expr #binaryexpr
    | <assoc=right> src1=expr op='=' src2=expr #binaryexpr
*/

    @Override public void visit(NullexprNode  it) {
        it.type = new Literaltype("null");
    }
    @Override public void visit(BoolexprNode  it) {
        it.type = new Literaltype("bool");
    }
    @Override public void visit(StringexprNode  it) {
        it.type = new Literaltype("string");
    }
    @Override public void visit(IntexprNode  it) {
        it.type = new Literaltype("int");
    }
    @Override public void visit(ThisexprNode  it) { if (in_class == null) throw new semanticError("Not in class!", it.pos); it.type = in_class;}

    @Override public void visit(FuncexprNode  it) {
        if (it.name instanceof VarexprNode ) it.name.type = now.get_function(((VarexprNode ) it.name).id, true, it.pos);
        else it.name.accept(this); //The function belongs to a class.
        if (!(it.name.type instanceof Funcsymbol)) throw new semanticError("Undefined function!", it.pos);
        Funcsymbol tmp = (Funcsymbol) it.name.type;
        it.exprlist.exprlist.forEach(x -> x.accept(this));
        if (it.exprlist.exprlist.size() != tmp.para_list.size()) throw new semanticError("Invalid parameters!", it.pos);
        for (int i = 0; i < tmp.para_list.size(); i++)
            if (!tmp.para_list.get(i).type.equal(it.exprlist.exprlist.get(i).type))
                throw new semanticError("Invalid parameter type!", it.pos);
        it.type = tmp.type;
    }

    @Override public void visit(ClassexprNode  it) {
        it.name.accept(this);
       if (it.name.type instanceof Arraytype && it.flag_func && it.id.equals("size")) {
            Funcsymbol tmp = new Funcsymbol("size");
            tmp.type = new Literaltype("int");
            it.type = tmp;
            return;
        }
        if (it.name.type.is_string() && it.flag_func && it.id.equals("length")) {
            Funcsymbol tmp = new Funcsymbol("length");
            tmp.type = new Literaltype("int");
            tmp.id2="__std_str_length";
            it.type = tmp;
            return;
        }
        if (it.name.type.is_string() && it.flag_func && it.id.equals("substring")) {
            Funcsymbol tmp = new Funcsymbol("substring");
            tmp.type = new Literaltype("string");
            tmp.id2="__std_str_substring";
            tmp.para_list.add(new Varsymbol("left", new Literaltype("int")));
            tmp.para_list.add(new Varsymbol("right", new Literaltype("int")));
            it.type = tmp;
            return;
        }
        if (it.name.type.is_string() && it.flag_func && it.id.equals("parseInt")) {
            Funcsymbol tmp = new Funcsymbol("parseInt");
            tmp.type = new Literaltype("int");
            tmp.id2="__std_str_parseInt";
            it.type = tmp;
            return;
        }
        if (it.name.type.is_string() && it.flag_func && it.id.equals("ord")) {
            Funcsymbol tmp = new Funcsymbol("ord");
            tmp.type = new Literaltype("int");
            tmp.id2="__std_str_ord";
            tmp.para_list.add(new Varsymbol("pos", new Literaltype("int")));
            it.type = tmp;
            return;
        }
        if (!(it.name.type instanceof Classtype)) throw new semanticError("Invalid Classexpr!", it.pos);
        Classtype tmp = (Classtype) it.name.type;
        if (it.flag_func) {
            if (tmp.funcmap.containsKey(it.id)) it.type = tmp.funcmap.get(it.id);
            else throw new semanticError("Invalid Classexpr!", it.pos);
        } else {
            if (tmp.varmap.containsKey(it.id)) {
                it.varsymbol = tmp.varmap.get(it.id);
                it.type = it.varsymbol.type;
            } else throw new semanticError("Invalid Classexpr!", it.pos);
        }
    }

    @Override
    public void visit(NewexprNode  it) {
        if (it.exprlist != null) {
            it.exprlist.forEach(x ->
            {
                x.accept(this);
                if (!x.type.is_int()) throw new semanticError("Invalid new", it.pos);
            });
        }
        it.type = scope.get_type(it.typenode);
    }

    @Override
    public void visit(ArrexprNode  it) {
        it.base.accept(this);
        it.offset.accept(this);
        if (!(it.base.type instanceof Arraytype)) throw new semanticError("Undefined array!", it.pos);
        if (!(it.offset.type.is_int())) throw new semanticError("The parameter must be int!", it.pos);
        Arraytype tmp = (Arraytype) it.base.type;
        if (tmp.dim == 1) it.type = tmp.type;
        else it.type = new Arraytype(tmp.type, tmp.dim - 1);
    }


    @Override
    public void visit(VarexprNode  it) {
        it.type = now.get_variable(it.id, true, it.pos).type;
        it.varsymbol = now.get_variable(it.id, true, it.pos);
    }

//
    @Override
    public void visit(SuffixexprNode  it) {
        it.src.accept(this);
        if (!it.src.type.is_int()) throw new semanticError("Invalid suffix", it.pos);
        if (!it.src.assign) throw new semanticError("Invalid suffix", it.pos);
        it.type = it.src.type;
    }
    /*
    @Override
    public void visit(LambdaExprNode node) {
        currentScope = new LambdaScope(currentScope, node.isGlobe);
        if (node.functionParameterList != null) node.functionParameterList.accept(this);
        if (node.functionParameterValue != null) node.functionParameterValue.accept(this);
        if (node.functionParameterList != null && node.functionParameterValue != null) {
            ArrayList<SingleVarDefNode> parameterList = node.functionParameterList.parameterList;
            ArrayList<ExprNode> parameterValue = node.functionParameterValue.parameters;
            if (parameterValue.size() != parameterList.size())
                throw new SemanticError("parameter list not match", node.pos);
            for (int i = 0; i < parameterValue.size(); i++) {
                if (!parameterList.get(i).typeNode.sameType(parameterValue.get(i).type)) {
                    int b=1;
                    throw new SemanticError("parameter list not match", node.pos);
                }
            }
            node.funcBody.accept(this);
        } else if (node.functionParameterValue == null && node.functionParameterList == null) {
            node.funcBody.accept(this);
        } else throw new SemanticError("parameter list not match", node.pos);
        if (((LambdaScope) currentScope).returnType == null) {
            node.type = new TypeNode(node.pos, gScope.getType("void"), false);
        } else node.type = ((LambdaScope) currentScope).returnType;
        currentScope = currentScope.parentScope;
    }
     */
    @Override public void visit(LambdaexprNode it){
        Scope tmp=now;
        now = new Scope(it.has_and? now:null);
        if (!it.paralist.isEmpty()) {it.paralist.forEach(x -> {x.accept(this);});}//throw new semanticError("1", it.pos);}
        if (it.exprlist != null) {it.exprlist.accept(this);}//throw new semanticError("2", it.pos);}
        if (!it.paralist.isEmpty() && it.exprlist != null) {
            ArrayList<VardefsubstmtNode> paraList = it.paralist;
            ArrayList<ExprNode> paravalue = it.exprlist.exprlist;
            if (paraList.size() != paravalue.size()) throw new semanticError("parameter list not match1", it.pos);
            if (paraList.size() ==1 && paravalue.size()==1 && paraList.get(0).type.type.equals("int") && paravalue.get(0).type.is_int()){ }
            else {
                for (int i = 0; i < paraList.size(); i++) {
                    //paraList.get(i).varsymbol=new Varsymbol(paraList.get(i).id,paraList.get(i).typenode);
                    if (!now.get_type(paraList.get(i).type).equal(paravalue.get(i).type)) {
                        String d = "0";
                        if (paraList.get(i).varsymbol.type.is_int() && paravalue.get(i).type.is_int()) d = "11";
                        if (!paraList.get(i).varsymbol.type.is_int() && paravalue.get(i).type.is_int()) d = "01";
                        if (paraList.get(i).varsymbol.type.is_int() && !paravalue.get(i).type.is_int()) d = "10";
                        if (!paraList.get(i).varsymbol.type.is_int() && !paravalue.get(i).type.is_int()) d = "00";
                        throw new semanticError(d, it.pos);
                    }
                }
            }
            it.body.accept(this);
        }
        else if (it.paralist.isEmpty()  && it.exprlist == null) {
            it.body.accept(this);
        }
        else throw new semanticError("parameter list not match3", it.pos);
        it.type=new Literaltype("null");
        for(int i=0;i<it.body.stmtlist.size();i++){
            StmtNode stmt=it.body.stmtlist.get(i);
            if(stmt instanceof ReturnstmtNode){it.type=((ReturnstmtNode)stmt).res.type;}
        }
        now=tmp;
    };
    @Override
    public void visit(PrefixexprNode  it) {
        it.src.accept(this);
        if (it.op.equals("++") || it.op.equals("--")) {
            if (!it.src.type.is_int()) throw new semanticError("Invalid Prefix", it.pos);
            if (!it.src.assign) throw new semanticError("Invalid Prefix", it.pos);
            it.assign = true;
            it.type = new Literaltype("int");
        }
        if (it.op.equals("+") || it.op.equals("-") || it.op.equals("~")) {
            if (!it.src.type.is_int()) throw new semanticError("Invalid Prefix", it.pos);
            it.type = new Literaltype("int");
        }
        if (it.op.equals("!")) {
            if (!it.src.type.is_boolean()) throw new semanticError("Invalid Prefix", it.pos);
            it.type = new Literaltype("bool");
        }
        if (!it.op.equals("++") && !it.op.equals("--") && !it.op.equals("+") && !it.op.equals("-") && !it.op.equals("~") && !it.op.equals("!"))  throw new semanticError("Invalid Prifix", it.pos);
    }
    @Override
    public void visit(BinaryexprNode  it) {
        it.src1.accept(this);
        it.src2.accept(this);
        boolean fl=false;
        if (it.op.equals("*") || it.op.equals("/") || it.op.equals("%") || it.op.equals("-") || it.op.equals("<<") || it.op.equals(">>") || it.op.equals("&") || it.op.equals("|") || it.op.equals("^")) {
            if (it.src1.type.is_int() && it.src2.type.is_int()) it.type = new Literaltype("int");
            else throw new semanticError("Invalid Binary", it.pos);
            fl=true;
        }
        if (it.op.equals("+")) {
            if (it.src1.type.is_int() && it.src2.type.is_int()) it.type = new Literaltype("int");
            else if (it.src1.type.is_string() && it.src2.type.is_string()) it.type = new Literaltype("string");
            else throw new semanticError("Invalid Binary", it.pos);
            fl=true;
        }
        if (it.op.equals("<") || it.op.equals(">") || it.op.equals("<=") || it.op.equals(">=")) {
            if (it.src1.type.is_int() && it.src2.type.is_int()) it.type = new Literaltype("bool");
            else if (it.src1.type.is_string() && it.src2.type.is_string()) it.type = new Literaltype("bool");
            else throw new semanticError("Invalid Binary", it.pos);
            fl=true;
        }
        if (it.op.equals("&&") || it.op.equals("||")) {
            if (it.src1.type.is_boolean() && it.src2.type.is_boolean()) it.type = new Literaltype("bool");
            else throw new semanticError("Invalid Binary", it.pos);
            fl=true;
        }
        if (it.op.equals("==") || it.op.equals("!=")) {
            if (it.src1.type.equal(it.src2.type)) it.type = new Literaltype("bool");
            else throw new semanticError("Invalid Binary", it.pos);
            fl=true;
        }
        if (it.op.equals("=")) {
            if (!it.src1.type.equal(it.src2.type)) throw new semanticError("Invalid Binary", it.pos);
            if (!it.src1.assign) throw new semanticError("Invalid Binary", it.pos);
            it.type = it.src1.type;
            it.assign = true;
            fl=true;
        }
        if (!fl) throw new semanticError("Invalid Binary!", it.pos);
    }
    @Override public void visit(ExprlistexprNode  it) {
        it.exprlist.forEach(x-> x.accept(this));
    }
}
