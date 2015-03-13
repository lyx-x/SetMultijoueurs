package x.lyx.setmultijoueurs;

import android.app.Notification;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by lyx on 26/02/15.
 */
public class CardView extends View{

    public boolean special=false;
    private Card card;
    private Paint paint;
    private int[] colors = new int[] {Color.RED, Color.GREEN, Color.BLUE};
    private boolean correct;
    private boolean chosen = false;
    private boolean judged = false;

    private OnClickListener chose = new OnClickListener() {
        @Override
        public void onClick(View v) {
            ((CardView)v).switchChoice();
            Game.selectCard((CardView)v);
            if(Game.selectedCard.size()==3){
                Game.haveSet();
            }
            v.invalidate();
        }
    };

    public Card getCard(){
        return card;
    }

    public CardView (Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.setOnClickListener(chose);
    }

    public void setCard (Card c)
    {
        this.card = c;
    }

    public void setJudgment (boolean c)
    {
        judged = true;
        this.correct = c;
    }

    public void switchChoice ()
    {
        chosen = !chosen;
    }

    void drawRhombus(RectF rect, Paint p, Canvas c){
        int h = c.getHeight();
        int w = c.getWidth();
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

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        if (card != null)
        {
            int y = this.getHeight() / (2 * card.number + 3);
            int x = this.getWidth() / 5;
            int h = this.getHeight() / 6;
            int w = this.getWidth() - x * 2;
            int height = this.getHeight();
            int width = this.getWidth();
            int delta = height / 40;
            int nb = 1;
            paint.setColor(Color.WHITE);
            canvas.drawRect(x / 4, x / 4, width - x / 4, height - x / 4, paint);
            if(special){
                paint.setColor(Color.BLACK);
                paint.setTextSize(15);
                canvas.drawText("Score: "+Game.score,10,20,paint);
                canvas.drawText("Time: ",10,40,paint);
                return;
            }
            paint.setColor(colors[card.color]);
            switch (card.fill){
                case 0:
                    paint.setStyle(Paint.Style.FILL);
                    break;
                case 1:
                    paint.setStyle(Paint.Style.STROKE);
                    break;
                case 2:
                    nb = h / 2 / delta + 1;
                    paint.setStyle(Paint.Style.STROKE);
                    break;
            }
            for(int i = 0 ; i <= card.number ; i++){
                for(int k = 0 ; k < nb ; k++) {
                    int left = x + delta * k * (w / h);
                    int top = y * (i * 2 + 1) + y / 2 - h / 2 + delta * k;
                    int right = width - x - delta * k * (w / h);
                    int bottom = top + h - 2 * delta * k;
                    RectF rect = new RectF(left, top, right, bottom);
                    switch (card.shape) {
                        case 0:
                            canvas.drawRect(rect, paint);
                            break;
                        case 1:
                            canvas.drawOval(rect, paint);
                            break;
                        case 2:
                            drawRhombus(rect, paint, canvas);
                            break;
                        default:
                    }
                }
            }
            if (judged) {
                if (correct)
                {
                    this.setBackgroundColor(Color.GREEN);
                    mask(Color.GREEN, canvas);
                }
                else
                {
                    this.setBackgroundColor(Color.RED);
                    mask(Color.RED, canvas);
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
            paint.setColor(Color.YELLOW);
            canvas.drawRect(10, 10, this.getWidth() - 10, this.getHeight() - 10, paint);
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(this.getWidth() / 2, this.getHeight() / 2, this.getWidth() / 2 - 10, paint);
        }

    }

    void mask (int color, Canvas canvas){
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = (color >> 0) & 0xFF;
        paint.setARGB(80, r, g, b);
        canvas.drawRect(0, 0, this.getWidth(), this.getHeight(), paint);
    }


}
