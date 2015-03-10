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
    public static LinkedList<Integer> CardSet=new LinkedList<Integer>();
    int number;
    int color;
    int fill;  // 0 : FILL ; 1 : STROKE ; 2 : HATCH
    int shape;  // 0 : RECT ; 1 : OVAL ; 2 : RHOMBUS
    public static void init(){
        int[][] temp=new int[81][2];
        Random rand=new Random();
        for(int i=0;i<81;i++){
            temp[i][0]=i;
            temp[i][1]=rand.nextInt(100);
        }
        for(int i=0;i<81;i++){
            for(int j=i+1;j<81;j++){
                if (temp[j][1]>temp[i][1]){
                    int k=temp[i][0];temp[i][0]=temp[j][0];temp[j][0]=k;
                    k=temp[i][1];temp[i][1]=temp[j][1];temp[j][1]=k;
                }
            }
        }
        for(int i=0;i<81;i++){
            CardSet.add(temp[i][0]);
        }

    }
    public static Card nextCard(){
        if (!CardSet.isEmpty()){
            int c=CardSet.getFirst();
            CardSet.remove();
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
    public int hashCode(){
        return(((number*3+color)*3+fill)*3+shape);
    }
    public boolean isDifferent(Card a){
        return (number!=a.number && color!=a.color && shape!=a.shape && fill!=a.fill);
    }

}
