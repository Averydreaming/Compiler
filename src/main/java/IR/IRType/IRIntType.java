package IR.IRType;

public class IRIntType extends  IRType{
    private int numberOfBits = 0;
    public IRIntType(int numberOfBits) {
        if ((numberOfBits == 1)  || (numberOfBits == 8) || (numberOfBits == 32))// for bool/string/int
        {
            this.numberOfBits = numberOfBits;
        } else {
            assert false;
        }
    }
    @Override
    public String toString() {
        return "i" + numberOfBits;
    }
}
