package AST;

import Util.position;

public class LambdaexprNode extends ExprNode {
    public LambdaexprNode(position _pos) {
        super(_pos);
    }

    @Override
    public void accept(ASTvisitor visitor) {
        visitor.visit(this);
    }
}