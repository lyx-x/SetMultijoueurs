package x.lyx.setmultijoueurs;

import java.util.LinkedList;

/**
 * Created by czx on 10/03/15.
 */
public class Game {

    public static LinkedList<CardView> selectedCard=new LinkedList<CardView>();
    public static LinkedList<CardSet> rightSet=new LinkedList<CardSet>();
    public static LinkedList<CardSet> wrongSet=new LinkedList<CardSet>();
    static int score=0;

    public static void selectCard(CardView c){
        selectedCard.add(c);
    }
    public static boolean haveSet(){
        selectedCard.getFirst().switchChoice();
        selectedCard.getFirst().invalidate();
        Card a=selectedCard.getFirst().getCard();
        selectedCard.pop();
        selectedCard.getFirst().switchChoice();
        selectedCard.getFirst().invalidate();
        Card b=selectedCard.getFirst().getCard();
        selectedCard.pop();
        selectedCard.getFirst().switchChoice();
        selectedCard.getFirst().invalidate();
        Card c=selectedCard.getFirst().getCard();
        selectedCard.pop();
        CardSet s=new CardSet(a,b,c);
        if (s.isSet()){
            score++;
            rightSet.add(s);
            return true;
        }else{
            wrongSet.add(s);
            return false;
        }
    }
}
