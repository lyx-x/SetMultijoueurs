package x.lyx.setmultijoueurs;

/**
 * Created by lyx on 26/02/15.
 */

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

}
