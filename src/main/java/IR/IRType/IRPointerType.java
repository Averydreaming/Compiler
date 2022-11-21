package IR.IRType;
//The pointer type ptr is used to specify memory locations.
public class IRPointerType extends IRType{
    public IRType  Type;

    public IRPointerType(IRType type) {
        this.Type = type;
    }

    @Override
    public String toString() {
        return Type.toString() + "*";
    }
}
