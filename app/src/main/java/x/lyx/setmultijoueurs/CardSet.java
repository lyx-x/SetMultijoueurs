package x.lyx.setmultijoueurs;

import java.lang.reflect.Field;
import java.util.LinkedList;

public class CardSet {

    CardView a, b, c;
    LinkedList<CardView> set;

    public CardSet(CardView a, CardView b, CardView c){
        this.a = a;
        this.b = b;
        this.c = c;
        set = new LinkedList<CardView>();
        set.add(a);
        set.add(b);
        set.add(c);
    }

    public boolean isSet(){
        try
        {
            Class<?> cls = Class.forName("x.lyx.setmultijoueurs.Card");
            Field[] fields = cls.getDeclaredFields();  //Find all 4 members of Card
            for (Field f : fields)
            {
                if (!isSetOneTest(f))  //Compare every member
                    return false;
            }
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isSetOneTest(Field f)
    {
        try{
            int x ,y ,z;
            x = f.getInt(a.getCard());
            y = f.getInt(b.getCard());
            z = f.getInt(c.getCard());
            return (x == y && y == z && z == x) || (x != y && y != z && z != x);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public LinkedList<CardView> getCardView(){
        return set;
    }

    @Override
    public String toString()
    {
        String ans = "";
        for (CardView c : set)
            ans += c.getCard().toString();
        return ans;
    }

}
