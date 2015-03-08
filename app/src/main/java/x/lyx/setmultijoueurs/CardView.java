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
import android.view.View;

/**
 * Created by lyx on 26/02/15.
 */
public class CardView extends View {

    private Card card;
    private Paint paint;
    private int[] colors = new int[] {Color.RED, Color.GREEN, Color.BLUE};

    public CardView (Context context, AttributeSet attrs, Card c)
    {
        super(context, attrs);
        this.card = c;
    }

    public CardView (Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public void setCard (Card c)
    {
        this.card = c;
    }


    void drawRhombus(RectF rect,Paint p,Canvas c){
        p.setColor(colors[card.color]);
        int h=c.getHeight();
        int w=c.getWidth();
        float coor[][]=new float[4][2];
        coor[0][0]=rect.left;
        coor[0][1]=(h-rect.bottom+rect.top)/2;
        coor[1][0]=w/2;
        coor[1][1]=h-rect.bottom;
        coor[2][0]=w-rect.right;
        coor[2][1]=(h-rect.bottom+rect.top)/2;
        coor[3][0]=w/2;
        coor[3][1]=rect.top;
        Path path=new Path();
        path.moveTo(coor[3][0],coor[3][1]);
        for(int i=0;i<4;i++){
            path.lineTo(coor[i][0],coor[i][1]);
        }
        c.drawPath(path,p);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        paint = new Paint();
        if (card != null)
        {
            int y=canvas.getHeight()/(2*card.number+1);
            int x=canvas.getWidth()/10;
            int height=canvas.getHeight();
            int width=canvas.getWidth();
            int little=height/20;
            paint.setColor(colors[card.color]);
            int j=1;
            switch (card.fill){
                case 1:
                    paint.setStrokeWidth(1);
                    break;
                case 2:
                    j=10;
                    paint.setStrokeWidth(1);
                    break;
            }
            for(int i=0;i<card.number;i++){
                for(int k=0;k<j;k++) {
                    RectF rect = new RectF(x+little*k, y * (i * 2 + 1)+little*k, width - x+little*k, y * (i * 2 + 1) + y+little*k);
                    switch (card.shape) {
                        case 0:
                            canvas.drawRect(rect, paint);
                            break;
                        case 1:
                            canvas.drawOval(rect, paint);
                            break;
                        case 2:
                            drawRhombus(rect,paint,canvas);
                            break;
                        default:
                    }
                }
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

}
