package IR.IRType;

public class IRVoidType extends  IRType{
    IRVoidType () {
        super("void", 0);
    }
    @Override
    public String toString() {
        return "void";
    }
}
