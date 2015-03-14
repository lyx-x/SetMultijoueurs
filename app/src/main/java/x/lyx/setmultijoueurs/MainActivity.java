package x.lyx.setmultijoueurs;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
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

        LinkedList<Card> cardSet=new LinkedList<Card>();

        public UpdateScore(LinkedList<Card> c)
        {
            this.cardSet = c;
        }

        public void run()
        {
            scoreboard.rightSet = cardSet;
            scoreboard.invalidate();
        }
    }

    class ReplaceCards implements Runnable{

        LinkedList<CardView> views;
        LinkedList<Card> cards;

        public ReplaceCards(LinkedList<CardView> v, LinkedList<Card> c)
        {
            this.cards = c;
            this.views = v;
        }

        public ReplaceCards(LinkedList<CardView> v){
            this.views = v;
            this.cards = new LinkedList<Card>();
        }

        public void run()
        {
            int i=0;
            for (CardView v : views)
            {
                if(cards.size() != 0){
                    v.setCard(cards.get(i));
                    i++;
                }
                else{
                    v.setCard(nextCard());
                }
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

    class LooperSocket extends Thread{

        public Handler handler = new Handler();

        public void run(){
            Looper.prepare();
            handler = new Handler();
            Looper.loop();
        }
    }

    class ClientSubmission implements Runnable{

        CardSet set;
        Socket client;

        public ClientSubmission(CardSet set, Socket s){
            this.set = set;
            this.client = s;
        }

        public void run(){
            StringBuilder s = new StringBuilder();
            s.append('S');
            for (CardView v:set.getCardView()){
                s.append(' ');
                s.append(v.id);
                s.append(' ');
                s.append(v.getCard().hashCode());
            }
            try
            {
                System.out.println(s);
                PrintWriter output = new PrintWriter(client.getOutputStream(), true);
                output.println(s);
            }
            catch (IOException e)
            {
                System.out.println(e);
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

    class ClientReceive extends Thread{

        Socket client;
        LinkedList<CardView> views;
        LinkedList<Card> cards;

        public ClientReceive(Socket s){
            this.client = s;
        }

        @Override
        public void run() {
            try {
                client = new Socket("192.168.1.1", 8888);
                socket = client;
            } catch (IOException e) {
                e.printStackTrace();
            }
            BufferedReader input;
            boolean active = true;
            while (active){
                try{
                    Thread.sleep(100);
                    input = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    while(input.ready() && active){
                        char task = (char)input.read();
                        System.out.println(task);
                        input.read();
                        String[] s;
                        switch (task){
                            case 'V':
                                views = new LinkedList<CardView>();
                                cards = new LinkedList<Card>();
                                int view = 0, cardCode = 0;
                                String msg = input.readLine();
                                System.out.println(msg);
                                s = msg.split(" ");
                                for (int i = 0 ; i < s.length / 2 ; i++){
                                    view = Integer.parseInt(s[i * 2]);
                                    cardCode = Integer.parseInt(s[i * 2 + 1]);
                                    cards.add(new Card(cardCode));
                                    views.add(allViews.get(view));
                                }
                                if (s.length == 6)
                                {
                                    replaceCards(views,cards, greenTime);
                                }
                                else
                                {
                                    replaceCards(views,cards);
                                }
                                viewChange.post(meltViews);
                                break;
                            case 'S':
                                score += greenScore;
                                cards = new LinkedList<Card>();
                                s = input.readLine().split(" ");
                                for (int i = 0 ; i < 3 ; i++) {
                                    cards.add(new Card(Integer.parseInt(s[i])));
                                }
                                updateScore(cards);
                                break;
                            case 'E':
                                active = false;
                                client.close();
                                break;
                        }
                    }
                }
                catch (Exception e){
                    System.out.println(e);
                }
            }
        }
    }

    Runnable meltViews = new Runnable() {
        @Override
        public void run() {
            for (CardView v : allViews) {
                v.restart();
                v.invalidate();
            }
        }
    };

    public int score = 0;
    Handler viewChange;  //Handler for all calls from other threads
    Socket socket = new Socket();
    LooperSocket looper=new LooperSocket();

    LinkedList<CardView> selectedCard = new LinkedList<CardView>();
    LinkedList<CardView> allViews = new LinkedList<CardView>();
    LinkedList<Integer> cards = new LinkedList<Integer>();  //All 81 cards


    int numberViews = 15;  //Number of cards displayed
    long greenTime = 500;  //Duration after a set is found
    long redTime = 2000;  //Duration after a wrong set is found
    int greenScore = 10;  //Score for a right set
    int redScore = -2;  //Score for a wrong set
    boolean netMode = true; //false for one player, true for multi players

    CardView scoreboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewChange = new Handler();
        CardView.game = this;  //Pass some methods and the score to all CardViews

        TableLayout layout = (TableLayout)findViewById(R.id.Table);
        CardView cardView;
        int count = 0;
        for (int i = 0 ; i < layout.getChildCount() ; i++)
        {
            if (count >= numberViews)
                break;
            TableRow row = (TableRow)layout.getChildAt(i);
            for (int j = 0 ; j < row.getChildCount() ; j++)
            {
                if (count >= numberViews)
                    break;
                count++;
                cardView = (CardView)row.getChildAt(j);
                cardView.id=i*row.getChildCount()+j;

                allViews.add(cardView);
            }
        }

        if (netMode)
        {
            try
            {
                new ClientReceive(socket).start();
            }
            catch (Exception e){
                System.out.println(e);
                e.printStackTrace();
            }
        }
        else
        {
            initCards();  //Create all 81 cards
            replaceCards(allViews);  //Now we have a full list of CardView
        }

        cardView = (CardView)findViewById(R.id.Card16);
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
        LinkedList<Card> cardSet = new LinkedList<Card>();
        cardSet.add(a.getCard());
        cardSet.add(b.getCard());
        cardSet.add(c.getCard());
        CardSet s = new CardSet(a, b, c);
        if (s.isSet()){
            if (!netMode){
                score += greenScore;
                setMask(true, s);
                replaceCards(s.getCardView(), greenTime);
                updateScore(cardSet);
            }
            else
            {
                setMask(true, s);
                looper.handler.post(new ClientSubmission(s, socket));
            }
        }
        else{
            score += redScore;
            updateScore(cardSet);
            setMask(false,s);
            if (netMode){
                for(CardView cv : allViews){
                    cv.setFroze(true);
                }

            }else {
                undoMask(s, redTime);
            }
        }
    }

    public void setMask(boolean judge, CardSet s)
    {
        viewChange.post(new DoMask(judge, s));
    }

    public void updateScore(LinkedList<Card> cs)
    {
        viewChange.post(new UpdateScore(cs));
    }

    public void replaceCards(LinkedList<CardView> cards)
    {
        viewChange.post(new ReplaceCards(cards));
    }

    public void replaceCards(LinkedList<CardView> views, LinkedList<Card> cards)
    {
        viewChange.post(new ReplaceCards(views, cards));
    }

    public void replaceCards(LinkedList<CardView> cards, long time)
    {
        new DelayThread(new ReplaceCards(cards), time).start();
    }

    public void replaceCards(LinkedList<CardView> views, LinkedList<Card> cards, long time)
    {
        new DelayThread(new ReplaceCards(views, cards), time).start();
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
