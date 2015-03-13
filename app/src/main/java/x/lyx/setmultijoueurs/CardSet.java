package x.lyx.setmultijoueurs;

import java.util.LinkedList;

/**
 * Created by czx on 10/03/15.
 */
public class CardSet {

    CardView a,b,c;

    public CardSet(CardView a, CardView b, CardView c){
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public boolean isSet(){
        return (a.getCard().isDifferent(b.getCard()) && b.getCard().isDifferent(c.getCard()) && a.getCard().isDifferent(c.getCard()));
    }

    public LinkedList<CardView> getCardView(){
        LinkedList<CardView> l = new LinkedList<CardView>();
        l.add(a);
        l.add(b);
        l.add(c);
        return l;
    }

}
