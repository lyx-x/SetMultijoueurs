package x.lyx.setmultijoueurs;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;


public class MainActivity extends Activity {

    class DoMask implements Runnable{

    //Maquer les cartes selectionnées
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

        //renouveller le score et crée la carte spéciale

        LinkedList<Card> cardSet = new LinkedList<Card>();

        public UpdateScore(LinkedList<Card> c)
        {
            this.cardSet = c;
        }

        public void run()
        {
            if (cardSet != null)
                scoreboard.rightSet = cardSet;
            scoreboard.invalidate();
        }
    }

    class ReplaceCards implements Runnable{

        //changer les cartes dans la listes views par les cartes dans la liste cards(multi mode) ou par nextcard dans le tas local
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
            if(cards.size() != 0){
                for (CardView v : views){
                    v.setCard(cards.poll());
                    v.invalidate();
                }
            }
            else{
                for(CardView v : views){
                    v.setCard(nextCard());
                    v.invalidate();
                }
            }
            if(!netMode){
                haveCardSet();
            }
        }
    }

    class UndoMask implements Runnable{
        //redémarrer un set de card

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
        //handler pour traiter les communication avec le serveur

        public Handler handler = new Handler();

        public void run(){
            Looper.prepare();
            handler = new Handler();
            Looper.loop();
        }
    }

    class Dialog implements Runnable{
        //le scoreboard

        AlertDialog builder;
        String message;         //message à affichier
        Activity a;
        LinkedList<String> l;   //une liste de message
        Boolean score = false;  //true en multi mode, false en sole mode

        public Dialog(Activity a, String s){
            this.message = s;
            this.a = a;
            this.score = false;
        }


        public Dialog(Activity a, LinkedList<String> l){
            this.a = a;
            this.l = l;
            this.score = true;
        }

        public void run(){
            builder= new AlertDialog.Builder(a).create();
            if(score){
                builder.setTitle("Scoreboard");
                StringBuffer str=new StringBuffer();
                for (String s : l) {
                    str.append(s);
                    str.append('\n');
                }
                builder.setMessage(str);
            }
            else{
                builder.setTitle("Alert");
                builder.setMessage(message);
            }
            builder.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });
            builder.show();
        }
    }

    class ClientSubmission implements Runnable{

        CardSet set;    //le bon set trouvé
        Socket client;
        String message;

        public ClientSubmission(CardSet set, Socket s){
            this.set = set;
            this.client = s;
            this.message = "N"; //pas de message, mais un set
        }

        public ClientSubmission(String str, Socket s){
            this.message = str;
            this.client = s;
        }
        public void run(){
            StringBuilder s = new StringBuilder();
            if(message != "N"){
                s.append(message);  //si il y a un message, submet le message
                try
                {
                    System.out.println(s);
                    PrintWriter output = new PrintWriter(client.getOutputStream(), true);
                    output.println(s);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                return;
            }
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
                e.printStackTrace();
            }
        }

    }

    class DelayThread extends Thread{
        //change certain cardviews
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
                e.printStackTrace();
            }
            viewChange.post(change);
        }

    }

    class TimeThread extends Thread{

        Runnable change;
        long time;

        public TimeThread(Runnable r, long t)
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
                    e.printStackTrace();
                }
                viewChange.post(change);
            }
        }
    }

    class ClientReceive extends Thread{

        Socket client;
        LinkedList<CardView> views;     //les Views à changer
        LinkedList<Card> cards;         //les cartes à remplacer
        Activity a;
        public ClientReceive(Activity a, Socket s){
            this.client = s;
            this.a=a;
        }

        @Override
        public void run() {
            try {
                client = new Socket("10.0.2.2", 8888);
                socket = client;
            } catch (IOException e) {
                e.printStackTrace();
                viewChange.post(new Dialog(a, "Connection failed!"));
                netMode = false;
                viewChange.post(meltViews);
                if(numberViews == 15){
                    numberViews = 12;
                    CardView a = allViews.removeLast();
                    CardView b = allViews.removeLast();
                    CardView c = allViews.removeLast();
                    a.setCard(null);
                    b.setCard(null);
                    c.setCard(null);
                    a.invalidate();
                    b.invalidate();
                    c.invalidate();
                }
                replaceCards(allViews);
                invalidateOptionsMenu();
                score = 0;
                scoreboard.rightSet = null;
                CardView.startTime = System.currentTimeMillis();
                return;
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
                            case 'M':
                                viewChange.post(meltViews);
                                break;
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
                                looper.handler.post(new ClientSubmission("N " + score, socket));
                                break;
                            case 'E':
                                active = false;
                                input.close();
                                client.close();
                                netMode = false;
                                viewChange.post(meltViews);
                                if(numberViews == 15){
                                    numberViews = 12;
                                    CardView a = allViews.removeLast();
                                    CardView b = allViews.removeLast();
                                    CardView c = allViews.removeLast();
                                    a.setCard(null);
                                    b.setCard(null);
                                    c.setCard(null);
                                    a.invalidate();
                                    b.invalidate();
                                    c.invalidate();
                                }
                                replaceCards(allViews);
                                invalidateOptionsMenu();
                                score = 0;
                                scoreboard.rightSet = null;
                                CardView.startTime = System.currentTimeMillis();
                                return;
                            case 'B':
                                s = input.readLine().split("b");
                                scores = new LinkedList<String>();
                                for(int i = 0 ; i < s.length ; i++){
                                    System.out.println(s[i]);
                                    scores.add(s[i]);
                                }
                                viewChange.post(new Dialog(a, scores));
                                break;
                        }
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                    active = false;
                    netMode = false;
                    viewChange.post(meltViews);
                    if(numberViews == 15){
                        numberViews = 12;
                        allViews.removeLast();
                        allViews.removeLast();
                        allViews.removeLast();
                    }
                    replaceCards(allViews);
                    invalidateOptionsMenu();
                    score = 0;
                    scoreboard.rightSet = null;
                    CardView.startTime = System.currentTimeMillis();
                    return;
                }
            }
        }
    }

    Runnable meltViews = new Runnable() {
        @Override
        public void run() {
            for (CardView v : allViews) {
                if (v.getChosen())
                    removeCard(v);      //faire sortir cette carte dans le liste selectedCard
                v.restart();
                v.invalidate();
            }
        }
    };

    public int score = 0;
    Handler viewChange;  //Handler for all calls from other threads en mode solo
    Socket socket = new Socket();
    LooperSocket looper = new LooperSocket();   //handler for all calls from other threads en mode multiple

    LinkedList<CardView> selectedCard = new LinkedList<CardView>(); //les cartes selectionées
    LinkedList<CardView> allViews = new LinkedList<CardView>(); //les cartesviews (12 ou 15 selon l'existance du set)
    LinkedList<Integer> cards = new LinkedList<Integer>();  //All 81 cards
    LinkedList<String> scores=new LinkedList<String>();

    int numberViews = 12;  //Number of cards displayed
    long greenTime = 500;  //Duration after a set is found
    long redTime = 2000;  //Duration after a wrong set is found
    int greenScore = 10;  //Score for a right set
    int redScore = -2;  //Score for a wrong set
    boolean netMode = false; //false for one player, true for multi players
    TableLayout layout;

    CardView scoreboard;    //la carte spéciale

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewChange = new Handler();
        CardView.game = this;  //Pass some methods and the score to all CardViews

        layout = (TableLayout)findViewById(R.id.Table);
        CardView cardView;
        int count = 0;
        for (int i = 0 ; i < layout.getChildCount(); i++)
        {
            TableRow row = (TableRow)layout.getChildAt(i);
            for (int j = 0 ; j < row.getChildCount() ; j++)
            {
                count++;
                cardView = (CardView)row.getChildAt(j);
                cardView.id = i * row.getChildCount() + j;
                allViews.add(cardView);
            }
        }

        if (netMode)
        {
            try
            {
                new ClientReceive(this,socket).start();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        else
        {
            initCards();  //Create all 81 cards
            System.out.println(numberViews);
            allViews.removeLast();
            allViews.removeLast();
            allViews.removeLast();
            allViews.removeLast();
            replaceCards(allViews);  //Now we have a full list of CardView
        }

        cardView = (CardView)findViewById(R.id.Card16);
        cardView.special = true;
        scoreboard = cardView;
        new TimeThread(new UpdateScore(null), 500).start();

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
    protected void onDestroy(){
        //exit en laissant le message 'End' à serveur

        super.onDestroy();
        if(netMode)
            looper.handler.post(new ClientSubmission("E", socket));
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.getItem(0);
        item.setTitle(R.string.multiplayer);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.multimode) {
            if(numberViews == 15){
                numberViews = 12;
                CardView a = allViews.removeLast();
                CardView b = allViews.removeLast();
                CardView c = allViews.removeLast();
                a.setCard(null);
                b.setCard(null);
                c.setCard(null);
                a.invalidate();
                b.invalidate();
                c.invalidate();
            }
            score = 0;
            scoreboard.rightSet = null;
            CardView.startTime = System.currentTimeMillis();
            viewChange.post(meltViews);
            netMode = !netMode;
            if(netMode){
                item.setTitle(R.string.solo);   //changer le settings
                try
                {
                    new ClientReceive(this,socket).start();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            else{
                item.setTitle(R.string.multiplayer);
                replaceCards(allViews);
                looper.handler.post(new ClientSubmission("E", socket));
            }
            return true;
        }
        if(id == R.id.restart){
            if(netMode) {
                looper.handler.post(new ClientSubmission("E", socket));
            }else{
                if(numberViews == 15){
                    numberViews = 12;
                    allViews.removeLast();
                    allViews.removeLast();
                    allViews.removeLast();
                }
                viewChange.post(meltViews);
                replaceCards(allViews);
                score = 0;
                scoreboard.rightSet = null;
                CardView.startTime = System.currentTimeMillis();
            }
        }
        if(id == R.id.scoreboard){
            if(netMode){
                looper.handler.post(new ClientSubmission("B", socket));
            }else{
                Dialog d = new Dialog(this,"You are in solo mode!");
                viewChange.post(d);
            }
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
    //prendre la cartes suivante, et réinitialiser la liste de card s'il n'y a plus de cartes dans la liste
        if (!cards.isEmpty()){
            int c = cards.getFirst();
            cards.remove();
            return (new Card(c));
        }
        else{
            System.out.print("NEW");
            initCards();
            for (int i = 0 ; i < numberViews ; i++) {
                int c = allViews.get(i).getCard().hashCode();
                int j = 0;
                while(j < cards.size() && cards.get(j) != c){
                    j++;
                }
                if(j < cards.size())
                    cards.remove(j);
            }
            int c = cards.getFirst();
            cards.remove();
            return(new Card(c));
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
                if(numberViews == 12){
                    replaceCards(s.getCardView(), greenTime);
                }else{
                    numberViews = 12;
                    LinkedList<CardView> cardView = new LinkedList<CardView>(); //les views à remplacer
                    LinkedList<Card> replaceCard = new LinkedList<Card>();  //les nouvelles cartes
                    if (a.id < 12)
                        cardView.add(a);
                    if (b.id < 12)
                        cardView.add(b);
                    if (c.id < 12)
                        cardView.add(c);
                    CardView d = allViews.removeLast();
                    CardView e = allViews.removeLast();
                    CardView f = allViews.removeLast();
                    if(!d.getChosen())
                        replaceCard.add(d.getCard());
                    if(!e.getChosen())
                        replaceCard.add(e.getCard());
                    if(!f.getChosen())
                        replaceCard.add(f.getCard());
                    d.setCard(null);
                    e.setCard(null);
                    f.setCard(null);
                    d.invalidate();
                    e.invalidate();
                    f.invalidate();
                    replaceCards(cardView, replaceCard, greenTime);
                }
                updateScore(cardSet);
            }
            else
            {
                setMask(true, s);
                looper.handler.post(new ClientSubmission(s, socket));
                looper.handler.post(new ClientSubmission("N " + score, socket));
            }
        }
        else{
            score += redScore;
            updateScore(null);
            setMask(false, s);
            if (netMode){
                for(CardView cv : allViews){
                    cv.setFroze(true);
                }
                looper.handler.post(new ClientSubmission("F", socket));
                looper.handler.post(new ClientSubmission("N " + score, socket));
            }else {
                undoMask(s, redTime);
            }
        }
    }

    public void haveCardSet(){
        CardSet s;
        boolean findSet = false;
        for (int i = 0 ; i < numberViews ; i++) {
            for (int j = i + 1 ; j < numberViews ; j++) {
                for (int k = j + 1 ; k < numberViews ; k++) {
                    s = new CardSet(allViews.get(i), allViews.get(j), allViews.get(k));
                    if (s.isSet()){
                        findSet = true;
                        System.out.format("%d %d %d\n", i, j, k);
                        break;
                    }
                }
                if(findSet)
                    break;
            }
            if(findSet)
                break;
        }
        if(numberViews == 12 && !findSet){
            CardView cardView;
            TableRow row = (TableRow)layout.getChildAt(layout.getChildCount() - 1);
            for (int j = 0 ; j < row.getChildCount() - 1 ; j++)
            {
                cardView = (CardView)row.getChildAt(j);
                cardView.id = 12 + j;
                allViews.add(cardView);
                cardView.setCard(nextCard());
                cardView.invalidate();
                numberViews++;
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
