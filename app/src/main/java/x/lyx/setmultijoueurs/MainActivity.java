package x.lyx.setmultijoueurs;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TableRow;
import java.util.LinkedList;
import java.util.Random;


public class MainActivity extends Activity {

    class DoMask implements Runnable{

        boolean judge;
        CardSet set;

        public DoMask(boolean j, CardSet s)
        {
            this.judge = j;
            this.set = s;
        }

        public void run()
        {
            for (CardView view : set.getCardView())
            {
                view.setJudgment(judge);
                view.invalidate();
            }
        }
    }

    class UpdateScore implements Runnable{

        CardSet set;

        public UpdateScore(CardSet set)
        {
            this.set = set;
        }

        public void run()
        {
            scoreboard.rightSet = set;
            scoreboard.invalidate();
        }
    }

    class ReplaceCards implements Runnable{

        LinkedList<CardView> cards;

        public ReplaceCards(LinkedList<CardView> c)
        {
            this.cards = c;
        }

        public void run()
        {
            for (CardView v : cards)
            {
                v.setCard(nextCard());
                v.invalidate();
            }
        }
    }

    class UndoMask implements Runnable{

        CardSet set;

        public UndoMask(CardSet s)
        {
            this.set = s;
        }

        public void run()
        {
            for (CardView v : set.getCardView())
            {
                v.restart();
                v.invalidate();
            }
        }
    }

    class DelayThread extends Thread{

        Runnable change;
        long time;

        public DelayThread(Runnable r, long t)
        {
            this.change = r;
            this.time = t;
        }

        public void run()
        {
            try
            {
                Thread.sleep(time);
            }
            catch (Exception e)
            {
                System.out.println(e);
            }
            viewChange.post(change);
        }

    }

    class LoopThread extends Thread{

        Runnable change;
        long time;

        public LoopThread(Runnable r, long t)
        {
            this.change = r;
            this.time = t;
        }

        public void run()
        {
            while (true) {
                try {
                    Thread.sleep(time);
                } catch (Exception e) {
                    System.out.println(e);
                }
                viewChange.post(change);
            }
        }
    }

    public int score = 0;
    Handler viewChange;  //Handler for all calls from other threads

    LinkedList<CardView> selectedCard = new LinkedList<CardView>();
    LinkedList<CardView> allViews = new LinkedList<CardView>();
    LinkedList<Integer> cards = new LinkedList<Integer>();  //All 81 cards

    int numberViews = 15;  //Number of cards displayed
    long greenTime = 500;  //Duration after a set is found
    long redTime = 2000;  //Duration after a wrong set is found
    int greenScore = 10;  //Score for a right set
    int redScore = -2;  //Score for a wrong set

    CardView scoreboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initCards();  //Create all 81 cards
        viewChange = new Handler();
        CardView.game = this;  //Pass some methods and the score to all CardViews

        TableLayout layout = (TableLayout)findViewById(R.id.Table);

        int count = 0;
        for (int i = 0 ; i < layout.getChildCount() ; i++)
        {
            if (count >= numberViews)
                break;
            TableRow row = (TableRow) layout.getChildAt(i);
            for (int j = 0 ; j < row.getChildCount() ; j++)
            {
                if (count >= numberViews)
                    break;
                count++;
                allViews.add((CardView)row.getChildAt(j));
            }
        }
        replaceCards(allViews);  //Now we have a full list of CardView

        CardView cardView;
        cardView=(CardView)findViewById(R.id.Card16);
        cardView.special=true;
        scoreboard = cardView;
        new LoopThread(new UpdateScore(null), 500).start();

        //Test case
//        cardView = (CardView)findViewById(R.id.Card01);
//        cardView.setCard(new Card(0, 0, 0, 0));
//        cardView.invalidate();
//        cardView = (CardView)findViewById(R.id.Card02);
//        cardView.setCard(new Card(0, 0, 0, 0));
//        cardView.invalidate();
//        cardView = (CardView)findViewById(R.id.Card03);
//        cardView.setCard(new Card(0, 0, 0, 0));
//        cardView.invalidate();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void initCards(){
        int[][] temp = new int[81][2];
        Random rand = new Random();
        for(int i = 0 ; i < 81 ; i++){
            temp[i][0] = i;
            temp[i][1] = rand.nextInt(100);
        }
        for(int i = 0 ; i < 81 ; i++){
            for(int j = i + 1 ; j < 81 ; j++){
                if (temp[j][1] > temp[i][1]){
                    int k = temp[i][0];
                    temp[i][0] = temp[j][0];
                    temp[j][0] = k;
                    k = temp[i][1];
                    temp[i][1] = temp[j][1];
                    temp[j][1] = k;
                }
            }
        }
        for(int i = 0 ; i < 81 ; i++){
            cards.add(temp[i][0]);
        }
    }

    Card nextCard(){
        if (!cards.isEmpty()){
            int c = cards.getFirst();
            cards.remove();
            return (new Card(c % 3, (c / 3) % 3, (c / 9) % 3, (c / 27) % 3));
        }
        else{
            return null;
        }
    }

    public void selectCard(CardView c){
        selectedCard.add(c);
        if (selectedCard.size() == 3)
            haveSet();
    }

    public void removeCard(CardView c){
        selectedCard.remove(c);
    }

    void haveSet(){
        CardView a = selectedCard.poll();
        CardView b = selectedCard.poll();
        CardView c = selectedCard.poll();
        CardSet s = new CardSet(a, b, c);
        if (s.isSet()){
            score += greenScore;
            setMask(true, s);
            replaceCards(s.getCardView(), greenTime);
        }
        else{
            score += redScore;
            setMask(false, s);
            undoMask(s, redTime);
        }
        updateScore(s);
    }

    public void setMask(boolean judge, CardSet s)
    {
        viewChange.post(new DoMask(judge, s));
    }

    public void updateScore(CardSet set)
    {
        viewChange.post(new UpdateScore(set));
    }

    public void replaceCards(LinkedList<CardView> cards)
    {
        viewChange.post(new ReplaceCards(cards));
    }

    public void replaceCards(LinkedList<CardView> cards, long time)
    {
        new DelayThread(new ReplaceCards(cards), time).start();
    }

    public void undoMask(CardSet set)
    {
        viewChange.post(new UndoMask(set));
    }

    public void undoMask(CardSet set, long time)
    {
        new DelayThread(new UndoMask(set), time).start();
    }
}
