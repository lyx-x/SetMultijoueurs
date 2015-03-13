package x.lyx.setmultijoueurs;

public class Card {

    int number;
    int color;
    int fill;  // 0 : FILL ; 1 : STROKE ; 2 : HATCH
    int shape;  // 0 : RECT ; 1 : OVAL ; 2 : RHOMBUS

    public Card (int n, int c, int f, int s)
    {
        this.number = n;
        this.color = c;
        this.fill = f;
        this.shape = s;
    }

    public int hashCode()
    {
        return (((number * 3 + color) * 3 + fill) * 3 + shape);
    }

    @Override
    public String toString()
    {
        return "N " + number + " | C " + color + " | F " + fill + " | S " + shape + "\n";
    }
}
