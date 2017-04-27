package cn.edu.sjtu.seiee.songrb.catchcrazycat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by Song Rb on 4/27/2017.
 */

public class Playground extends SurfaceView implements View.OnTouchListener {

    private static final int ROW = 10;
    private static final int COL = 10;
    private static final int BLOCKS = 15;//默认添加的路障数量
    private static int WIDTH = 40;
    int startingX = 0;
    int startingY = 40;


    private Dot matrix[][];
    private Dot cat;

    private ImageView CatImg;

    private Bitmap icon;
    SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceDestroyed(SurfaceHolder arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void surfaceCreated(SurfaceHolder arg0) {
            // TODO Auto-generated method stub
            redraw();
        }

        @Override
        public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub
            WIDTH = arg2 / (COL + 1);
            startingY = arg3 - (ROW + 1) * WIDTH;
            startingX = (arg2 - (ROW) * WIDTH) / 2;
            redraw();
        }
    };

    public Playground(Context context) {
        super(context);
        CatImg = new ImageView(context);
        CatImg.setImageResource(R.drawable.cat);

        icon = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.cat), (int) 3.8 * WIDTH, (int) 3.8 * WIDTH, false);

        getHolder().addCallback(callback);
        matrix = new Dot[ROW][COL];
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COL; j++) {
                matrix[i][j] = new Dot(j, i);
            }
        }
        setOnTouchListener(this);
        initGame();
    }

    private Dot getDot(int x, int y) {
        return matrix[y][x];
    }

    private boolean isAtEdge(Dot d) {
        return d.getX() * d.getY() == 0 || d.getX() + 1 == COL || d.getY() + 1 == ROW;
    }

    private Dot getNeighbour(Dot dot, int dir) {
        switch (dir) {
            case 1:
                return getDot(dot.getX() - 1, dot.getY());
            case 2:
                if (dot.getY() % 2 == 0) {
                    return getDot(dot.getX() - 1, dot.getY() - 1);
                } else {
                    return getDot(dot.getX(), dot.getY() - 1);
                }
            case 3:
                if (dot.getY() % 2 == 0) {
                    return getDot(dot.getX(), dot.getY() - 1);
                } else {
                    return getDot(dot.getX() + 1, dot.getY() - 1);
                }
            case 4:
                return getDot(dot.getX() + 1, dot.getY());
            case 5:
                if (dot.getY() % 2 == 0) {
                    return getDot(dot.getX(), dot.getY() + 1);
                } else {
                    return getDot(dot.getX() + 1, dot.getY() + 1);
                }
            case 6:
                if (dot.getY() % 2 == 0) {
                    return getDot(dot.getX() - 1, dot.getY() + 1);
                } else {
                    return getDot(dot.getX(), dot.getY() + 1);
                }

            default:
                break;
        }
        return null;
    }

    // 遇到路障 则返回负数
    // 到达边沿，返回正数
    private int getDistance(Dot one, int dir) {
//		System.out.println("X:"+one.getX()+" Y:"+one.getY()+" Dir:"+dir);
        int distance = 0;
        if (isAtEdge(one)) {
            return 1;
        }
        Dot ori = one, next;
        while (true) {
            next = getNeighbour(ori, dir);
            assert next != null;
            if (next.getStatus() == Dot.STATUS_ON) {
                return distance * -1;
            }
            if (isAtEdge(next)) {
                distance++;
                return distance;
            }
            distance++;
            ori = next;
        }
    }

    private void MoveTo(Dot one) {
        one.setStatus(Dot.STATUS_IN);
        getDot(cat.getX(), cat.getY()).setStatus(Dot.STATUS_OFF);
        cat.setXY(one.getX(), one.getY());
    }

    private void move() {
        if (isAtEdge(cat)) {
            lose();
            return;
        }
        // 这个方向上的点为空
        Vector<Dot> emptyDots = new Vector<>();
        // 从这个方向可以出去
        Vector<Dot> positive = new Vector<>();
        // 为空的点和对应的方向
        HashMap<Dot, Integer> al = new HashMap<>();
        for (int i = 1; i < 7; i++) {
            Dot n = getNeighbour(cat, i);
            assert n != null;
            if (n.getStatus() == Dot.STATUS_OFF) {
                emptyDots.add(n);
                al.put(n, i);

                if (getDistance(n, i) > 0) {
                    positive.add(n);

                }
            }
        }
        if (emptyDots.size() == 0) {
            win();
        } else if (emptyDots.size() == 1) {
            MoveTo(emptyDots.get(0));
        } else {
            Dot best = null;

            //存在可以直接到达屏幕边缘的走向
            if (positive.size() != 0) {
                System.out.println("向前进");
                int min = 999;
                for (int i = 0; i < positive.size(); i++) {

                    // 下一步可以走的点的方向上距离屏幕边缘的距离
                    int a = getDistance(positive.get(i), al.get(positive.get(i)));
                    if (a < min) {
                        min = a;
                        best = positive.get(i);
                    }
                }
                MoveTo(best);
            } else {
                //所有方向都存在路障
                // 选取距离路障最远的点
                System.out.println("躲路障");
                int max = 0;
                for (int i = 0; i < emptyDots.size(); i++) {
                    int k = getDistance(emptyDots.get(i), al.get(emptyDots.get(i)));
                    if (k <= max) {
                        max = k;
                        best = emptyDots.get(i);
                    }
                }
                MoveTo(best);
            }
        }
    }

    private void lose() {
        Toast.makeText(getContext(), "Lose", Toast.LENGTH_SHORT).show();
        initGame();
    }

    private void win() {
        Toast.makeText(getContext(), "You Win!", Toast.LENGTH_SHORT).show();

    }

    private void redraw() {
        Canvas c = getHolder().lockCanvas();
        c.drawColor(Color.LTGRAY);
        Paint paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        for (int i = 0; i < ROW; i++) {
            int offset = 0;
            if (i % 2 != 0) {
                offset = WIDTH / 2;
            }
            for (int j = 0; j < COL; j++) {
                Dot one = getDot(j, i);
                switch (one.getStatus()) {
                    case Dot.STATUS_OFF:
                        paint.setColor(0xFFEEEEEE);
                        break;
                    case Dot.STATUS_ON:
                        paint.setColor(0xFFFFAA00);
                        break;
                    case Dot.STATUS_IN:
                        paint.setColor(0xFFFF0000);
                        break;

                    default:
                        break;
                }

                if (one.getStatus() == Dot.STATUS_IN) {
                    c.drawBitmap(icon, startingX + one.getX() * WIDTH + offset, startingY + one.getY() * WIDTH, new Paint());
                } else {
                    c.drawOval(new RectF(startingX + one.getX() * WIDTH + offset, startingY + one.getY() * WIDTH,
                            startingX + (one.getX() + 1) * WIDTH + offset, startingY + (one.getY() + 1) * WIDTH), paint);
                }
            }

        }
        getHolder().unlockCanvasAndPost(c);
    }

    private void initGame() {
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COL; j++) {
                matrix[i][j].setStatus(Dot.STATUS_OFF);
            }
        }
        cat = new Dot(4, 5);
        getDot(4, 5).setStatus(Dot.STATUS_IN);
        for (int i = 0; i < BLOCKS; ) {
            int x = (int) ((Math.random() * 1000) % COL);
            int y = (int) ((Math.random() * 1000) % ROW);
            if (getDot(x, y).getStatus() == Dot.STATUS_OFF) {
                getDot(x, y).setStatus(Dot.STATUS_ON);
                i++;
            }
        }
    }

    @Override
    public boolean onTouch(View arg0, MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_UP) {
            int x, y;
            y = (int) ((e.getY() - startingY) / WIDTH);
            if (y % 2 == 0) {
                x = (int) ((e.getX() - startingX) / WIDTH);
            } else {
                x = (int) (((e.getX() - startingX) - WIDTH / 2) / WIDTH);
            }

            // 触及边界，执行初始化操作
            if (x + 1 > COL || y + 1 > ROW || x < 0 || y < 0) {
                initGame();
            } else if (getDot(x, y).getStatus() == Dot.STATUS_OFF) {
                getDot(x, y).setStatus(Dot.STATUS_ON);
                move();
            }
            redraw();
        }
        return true;
    }
}