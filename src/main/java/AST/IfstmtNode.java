package AST;

import Util.position;

public class IfstmtNode extends StmtNode {
    public ExprNode conditionexpr;
    public StmtNode truestmt, falsestmt;

    public IfstmtNode(position _pos, ExprNode _conditionexpr, StmtNode _truestmt, StmtNode _falsestmt) {
        super(_pos);
        conditionexpr = _conditionexpr;
        truestmt = _truestmt;
        falsestmt = _falsestmt;
    }

    @Override
    public void accept(ASTvisitor visitor) {
        visitor.visit(this);
    }
}
