package x.lyx.setmultijoueurs;

/**
 * Created by czx on 10/03/15.
 */
public class CardSet {

    Card a,b,c;

    public CardSet(Card a,Card b,Card c){
        this.a=a;
        this.b=b;
        this.c=c;
    }

    public boolean isSet(){
        return(a.isDifferent(b) && b.isDifferent(c) && a.isDifferent(c));
    }

}
