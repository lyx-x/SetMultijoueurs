package x.lyx.setmultijoueurs;

import android.app.Notification;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
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

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (card != null)
        {
            paint.setColor(Color.WHITE);
            canvas.drawRect(10, 10, this.getWidth() - 10, this.getHeight() - 10, paint);
            paint.setColor(card.color);

            Path path = new Path();
            paint.setStyle(Paint.Style.STROKE);
            path.moveTo(50, 50);
            path.lineTo(100,100);
            path.lineTo(0,100);
            canvas.drawPath(path, paint);
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
