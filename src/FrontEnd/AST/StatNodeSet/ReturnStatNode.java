package FrontEnd.AST.StatNodeSet;

import FrontEnd.AST.ExprNodeSet.BaseExprNode;
import FrontEnd.AST.ASTVisitor;
import Utils.Position;

public class ReturnStatNode extends BaseStatNode{
	public BaseExprNode returnexpr;
	public ReturnStatNode(BaseExprNode _returnexpr,Position _pos){
		super(_pos);
		returnexpr=_returnexpr;
	}

	@Override
	public void accept(ASTVisitor visitor) {
		visitor.visitReturnStat(this);
	}
}
