package x.lyx.setmultijoueurs;

import android.system.ErrnoException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

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
