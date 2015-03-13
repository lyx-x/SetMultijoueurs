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

    TableLayout layout;
    public static Handler setMask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setMask = new Handler();
        Game.game = this;

        layout = (TableLayout) findViewById(R.id.Table);
        Card.init();
        CardView cardView;
        System.out.println(layout.getChildCount());

        for (int i = 0 ; i < layout.getChildCount() ; i++)
        {
            TableRow row = (TableRow) layout.getChildAt(i);
            for (int j = 0 ; j < row.getChildCount() ; j++)
            {
                cardView = (CardView) row.getChildAt(j);
                cardView.setCard(Card.nextCard());
                cardView.invalidate();
            }
        }
        cardView=(CardView)findViewById(R.id.Card16);
        cardView.special=true;
        cardView.invalidate();
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

    public void setMask(boolean judge, CardSet s)
    {
        setMask.post(new Mask(judge, s));
    }
}
