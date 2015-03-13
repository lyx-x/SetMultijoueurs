package x.lyx.setmultijoueurs;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.Random;


public class MainActivity extends Activity {

    class Mask implements Runnable{

        boolean judge;
        CardSet set;

        public Mask(boolean j, CardSet s)
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

    public LinkedList<CardView> selectedCard = new LinkedList<CardView>();
    public LinkedList<CardSet> rightSet = new LinkedList<CardSet>();
    public LinkedList<CardSet> wrongSet = new LinkedList<CardSet>();

    public int score = 0;

    CardView scoreboard;

    LinkedList<Integer> cards = new LinkedList<Integer>();

    TableLayout layout;
    public Handler setMask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setMask = new Handler();
        CardView.game = this;

        layout = (TableLayout)findViewById(R.id.Table);
        init();
        CardView cardView;
        System.out.println(layout.getChildCount());

        for (int i = 0 ; i < layout.getChildCount() ; i++)
        {
            TableRow row = (TableRow) layout.getChildAt(i);
            for (int j = 0 ; j < row.getChildCount() ; j++)
            {
                cardView = (CardView)row.getChildAt(j);
                cardView.setCard(nextCard());
                cardView.invalidate();
            }
        }
        cardView=(CardView)findViewById(R.id.Card16);
        cardView.special=true;
        cardView.invalidate();

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

    public void init(){
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

    boolean haveSet(){
        CardView a = selectedCard.poll();
        CardView b = selectedCard.poll();
        CardView c = selectedCard.poll();
        CardSet s = new CardSet(a, b, c);
        System.out.println(s);
        if (s.isSet()){
            score++;
            rightSet.add(s);
            setMask(true, s);
            return true;
        }
        else{
            wrongSet.add(s);
            setMask(false, s);
            return false;
        }

    }


    public void setMask(boolean judge, CardSet s)
    {
        setMask.post(new Mask(judge, s));
    }
}
