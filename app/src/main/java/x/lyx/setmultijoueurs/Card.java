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

    public static LinkedList<Integer> CardSet;

    int number;
    int color;
    int fill;  // 0 : FILL ; 1 : STROKE ; 2 : HATCH
    int shape;  // 0 : RECT ; 1 : OVAL ; 2 : RHOMBUS

    public static void init(){
        CardSet.clear();
        HashMap<Integer,Integer> temp = new HashMap<Integer, Integer>();
        Random rand = new Random();
        for(int i = 0 ; i < 81 ; i++){
            temp.put(rand.nextInt(), i);
        }
        Map<Integer,Integer> sort=new TreeMap<Integer, Integer>(temp);
        Set set=sort.entrySet();
        Iterator iter=set.iterator();
        while(iter.hasNext()){
          Map.Entry me=(Map.Entry)iter.next();
          CardSet.push((int)me.getValue());
        }

    }
    public Card nextCard(){
        if (!CardSet.isEmpty()){
            int c=CardSet.getFirst();
            CardSet.pop();
            return (new Card(c%3,(c/3)%3,(c/9)%3,(c/27)%3));
        }else{
            return null;
        }
    }

    public Card (int n, int c, int f, int s)
    {
        this.number = n;
        this.color = c;
        this.fill = f;
        this.shape = s;
    }

}
