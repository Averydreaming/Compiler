package IR.IRType;

public class IRArrayType extends IRType {
    public int dim;
    public IRType Type;

    public IRArrayType(int dim, IRType Type) {
        this.dim = dim;
        this.Type = Type;
    }

    @Override
    public String toString() {
        return "[" + dim + " x " + Type.toString() + "]";
    }

}

