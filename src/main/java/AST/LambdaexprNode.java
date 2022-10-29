package AST;

import Util.position;

import java.util.ArrayList;

/*

    |('[' op='&' ']'|'[' ']') ( '(' paraList? ')' )? '->' part '(' exprList ')'	#lambdaexpr	//仅在Semantic Check阶段考察
 */
public class LambdaexprNode extends ExprNode {
    public boolean has_and;
    public ArrayList<VardefsubstmtNode> paralist = new ArrayList<>();
    public PartstmtNode body;
    public ExprlistexprNode exprlist;
    public LambdaexprNode(position _pos) {
        super(_pos);
    }

    @Override
    public void accept(ASTvisitor visitor) {
        visitor.visit(this);
    }
}