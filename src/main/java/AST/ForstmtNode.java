package AST;

import Util.position;

public class ForstmtNode extends StmtNode {
    public ExprNode initstmt, forconditionexpr, stepexpr;
    public StmtNode body;

    public ForstmtNode(position _pos, ExprNode _initstmt, ExprNode _forconditionexpr, ExprNode _stepexpr, StmtNode _body) {
        super(_pos);
        initstmt = _initstmt;
        forconditionexpr = _forconditionexpr;
        stepexpr = _stepexpr;
        body = _body;
    }

    @Override
    public void accept(ASTvisitor visitor) {
        visitor.visit(this);
    }

}
