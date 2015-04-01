package x.lyx.setmultijoueurs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.Date;
import java.util.LinkedList;

public class CardView extends View{

    public int id;
    public static MainActivity game;
    public static long startTime = System.currentTimeMillis();

    public boolean special = false;
    public LinkedList<Card> rightSet;

    private Card card;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int[] colors = new int[] {Color.RED, Color.GREEN, Color.BLUE};
    private boolean correct;
    private boolean chosen = false;
    private boolean judged = false;
    private boolean froze = false;

    OnClickListener choose = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (((CardView) v).judged || ((CardView) v).special || ((CardView) v).froze)
                return;
            ((CardView) v).switchChoice();
            if (((CardView) v).chosen)
                game.selectCard((CardView) v);
            else
                game.removeCard((CardView) v);
            v.invalidate();
        }
    };

    public CardView (Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.setOnClickListener(choose);
    }

    public Card getCard(){
        return card;
    }

    public boolean getChosen(){
        return chosen;
    }

    public void setCard (Card c)
    {
        this.card = c;
        restart();
    }

    public void restart()
    {
        chosen = false;
        judged = false;
        froze = false;
    }

    public void setJudgment (boolean c)
    {
        judged = true;
        chosen = false;
        this.correct = c;
    }

    public void setFroze (boolean f)
    {
        froze = f;
    }

    public void switchChoice ()
    {
        chosen = !chosen;
        judged = false;
    }

    public void drawCard(Card card,Canvas canvas, RectF rect){
        float height = rect.height();
        float width = rect.width();
        float x = width / 5;
        float y = height / (2 * card.number + 3);
        float h = height / 6;
        float w = width - width / 5 * 2;
        int nb = 1;
        float delta=height/40;

        paint.setColor(colors[card.color]);
        switch (card.fill){
            case 0:
                paint.setStyle(Paint.Style.FILL);
                break;
            case 1:
                paint.setStyle(Paint.Style.STROKE);
                break;
            case 2:
                nb = (int)(h / 2 / delta) + 1;
                paint.setStyle(Paint.Style.STROKE);
                break;
        }
        for(int i = 0 ; i <= card.number ; i++){
            for(int k = 0 ; k < nb ; k++) {
                float left = rect.left+x + delta * k * (w / h);
                float top = rect.top+y * (i * 2 + 1) + y / 2 - h / 2 + delta * k;
                float right = rect.left + width - x - delta * k * (w / h);
                float bottom = top + h - 2 * delta * k;
                RectF rectf = new RectF(left, top, right, bottom);
                switch (card.shape) {
                    case 0:
                        canvas.drawRect(rectf, paint);
                        break;
                    case 1:
                        canvas.drawOval(rectf, paint);
                        break;
                    case 2:
                        drawRhombus(rectf, paint, canvas);
                        break;
                    default:
                }
            }
        }

    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        if (card != null || special)
        {
            int height = this.getHeight();
            int width = this.getWidth();
            paint.setColor(Color.WHITE);
            canvas.drawRect(width / 20, width / 20, width - width / 20, height - width / 20, paint);

            if (special)
            {
                drawScore(game.score, canvas);
                if (rightSet != null)
                {
                    int i=0;
                    for(Card card : rightSet){
                        RectF rect = new RectF(width / 3 * i + width / 30, height / 2, width / 3 * (i + 1) - width / 30, height);
                        drawCard(card, canvas, rect);
                        i++;
                    }
                    //draw 3 cards
                }
                return;
            }else{
                RectF rect = new RectF(0, 0, width, height);
                drawCard(card, canvas, rect);
            }
            if (judged) {
                if (correct)
                {
                    this.setBackgroundColor(Color.GREEN);
                    drawMask(Color.GREEN, canvas);
                }
                else
                {
                    this.setBackgroundColor(Color.RED);
                    drawMask(Color.RED, canvas);
                }
            }
            else
            {
                if (chosen)
                    this.setBackgroundColor(Color.BLUE);
                else
                    this.setBackgroundColor(Color.WHITE);
            }
        }
        else
        {
            if (chosen)
                this.setBackgroundColor(Color.BLUE);
            else
                this.setBackgroundColor(Color.WHITE);
        }

    }

    void drawRhombus(RectF rect, Paint p, Canvas c){
        float coord[][] = new float[4][2];
        coord[0][0] = rect.left;
        coord[0][1] = (rect.bottom + rect.top) / 2;
        coord[1][0] = (rect.left + rect.right) / 2;
        coord[1][1] = rect.bottom;
        coord[2][0] = rect.right;
        coord[2][1] = coord[0][1];
        coord[3][0] = coord[1][0];
        coord[3][1] = rect.top;
        Path path = new Path();
        path.moveTo(coord[3][0], coord[3][1]);
        for(int i = 0 ; i < 4 ; i++){
            path.lineTo(coord[i][0],coord[i][1]);
        }
        c.drawPath(path,p);
    }

    void drawMask (int color, Canvas canvas){
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        paint.setARGB(80, r, g, b);
        canvas.drawRect(0, 0, this.getWidth(), this.getHeight(), paint);
    }

    void drawScore (int score, Canvas canvas)
    {
        long time = System.currentTimeMillis() - startTime;
        long minuteInMillis = time / 1000 / 60;
        long secondInMillis = time / 1000 - minuteInMillis * 60;

        int h = this.getHeight();
        int w = this.getWidth();

        paint.setColor(Color.BLACK);
        paint.setTextSize(Math.min(h / 8, w / 8));
        canvas.drawText("Score: " + score, w / 8, h / 6,paint);
        canvas.drawText("Time elapsed:", w / 8, h / 6 + h / 8, paint);
        canvas.drawText(minuteInMillis + " ' " + secondInMillis + " \"", w / 3, h / 6 + h / 4, paint);
    }
}
