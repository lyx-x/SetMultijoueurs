package x.lyx.setmultijoueurs;

import android.app.Activity;

import java.util.LinkedList;

/**
 * Created by czx on 10/03/15.
 */
public class Game {

    public static LinkedList<CardView> selectedCard = new LinkedList<CardView>();
    public static LinkedList<CardSet> rightSet = new LinkedList<CardSet>();
    public static LinkedList<CardSet> wrongSet = new LinkedList<CardSet>();

    static int score = 0;

    public static MainActivity game;

    public static void selectCard(CardView c){
        selectedCard.add(c);
    }

    public static boolean haveSet(){
        selectedCard.getFirst().switchChoice();
        selectedCard.getFirst().invalidate();
        CardView a=selectedCard.getFirst();
        selectedCard.pop();
        selectedCard.getFirst().switchChoice();
        selectedCard.getFirst().invalidate();
        CardView b=selectedCard.getFirst();
        selectedCard.pop();
        selectedCard.getFirst().switchChoice();
        selectedCard.getFirst().invalidate();
        CardView c=selectedCard.getFirst();
        selectedCard.pop();
        CardSet s=new CardSet(a,b,c);
        if (s.isSet()){
            score++;
            rightSet.add(s);
            game.setMask(true, s);
            return true;
        }
        else{
            wrongSet.add(s);
            game.setMask(false, s);
            return false;
        }
    }
}
