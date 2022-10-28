package AST;

import Util.position;

public class WhilestmtNode extends StmtNode {
    public ExprNode whileconditionexpr;
    public StmtNode body;

    public WhilestmtNode(position _pos, ExprNode _whileconditionexpr, StmtNode _body) {
        super(_pos);
        whileconditionexpr = _whileconditionexpr;
        body = _body;
    }

    @Override
    public void accept(ASTvisitor visitor) {
        visitor.visit(this);
    }
}
