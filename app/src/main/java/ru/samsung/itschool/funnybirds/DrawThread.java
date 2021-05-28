package ru.samsung.itschool.funnybirds;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Toast;

public class DrawThread extends Thread {

    private Bitmap backgroundBitmap;
    private Bitmap pauseBitmap;
    private boolean isPaused;
    private SurfaceHolder surfaceHolder;
    private volatile boolean running = true;

    private Sprite playerBird;
    private Sprite enemyBird;
    private Sprite touchBird;
    private Sprite Bonus;

    private DrawThread.Timer timer;

    private final int timerInterval = 30;

    private int points = 0;
    private int level = 1;

    private int pauseX;
    private int pauseY;

    private int viewWidth;
    private int viewHeight;

    private Context context;

    class Timer extends CountDownTimer {
        public Timer() {
            super(Integer.MAX_VALUE, timerInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            update ();
        }

        @Override
        public void onFinish() {
        }
    }

    public DrawThread(Context context, SurfaceHolder surfaceHolder) {
        this.context = context;
        //создаем сурфейс холдер
        this.surfaceHolder = surfaceHolder;

        //Создание первых фреймов птичек и их действий
        Bitmap playerBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.player);
        int playerWidth = playerBitmap.getWidth()/5;
        int playerHeight = playerBitmap.getHeight()/3;
        Rect firstFrame = new Rect(
                0,
                0,
                playerWidth,
                playerHeight);
        playerBird = new Sprite(10, 0, 0, 400, firstFrame, playerBitmap);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }
                if (i == 2 && j == 3) {
                    continue;
                }
                playerBird.addFrame(new Rect(j * playerWidth,
                        i * playerHeight,
                        j* playerWidth + playerWidth,
                        i * playerWidth + playerWidth));
            }
        }
        Bitmap enemyBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.enemy);
        int enemyWidth = enemyBitmap.getWidth() / 5;
        int enemyHeight = enemyBitmap.getHeight() / 3;
        firstFrame = new Rect(
                4 * enemyWidth,
                0,
                5 * enemyWidth,
                enemyHeight);

        enemyBird = new Sprite(2000, 250, -300, 0, firstFrame, enemyBitmap);
        for (int i = 0; i < 3; i++) {
            for (int j = 4; j >= 0; j--) {
                if (i ==0 && j == 4) {
                    continue;
                }
                if (i ==2 && j == 0) {
                    continue;
                }
                enemyBird.addFrame(new Rect(j * enemyWidth, i * enemyHeight, j * enemyWidth + enemyWidth, i * enemyWidth + enemyWidth));
            }
        }

        Bitmap touchBirdBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.touchbird);
        int touchBirdWidth = touchBirdBitmap.getWidth()/5;
        int touchBirdHeight = touchBirdBitmap.getHeight()/3;
        firstFrame = new Rect(0, 0, touchBirdWidth, touchBirdHeight);
        touchBird = new Sprite(2000, 400, -300, 0, firstFrame, touchBirdBitmap);
        for (int i = 0; i < 3; i++) {
            for (int j = 4; j >= 0; j--) {
                if (i ==0 && j == 4) {
                    continue; }
                if (i ==2 && j == 0) {
                    continue; }
                touchBird.addFrame(new Rect(
                        j * touchBirdWidth + 10,
                        i * touchBirdHeight +10,
                        j * touchBirdWidth + touchBirdWidth + 10,
                        i * touchBirdWidth + touchBirdWidth));
            }
        }

        //бонус
        Bitmap bonusBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.bonus);
        bonusBitmap = Bitmap.createScaledBitmap(bonusBitmap,140,140,true);
        int bonusWidth = bonusBitmap.getWidth();
        int bonusHeight = bonusBitmap.getHeight();
        firstFrame = new Rect(0,0, bonusWidth, bonusHeight );
        Bonus = new Sprite(2000,500,-150,0, firstFrame, bonusBitmap);
        //пауза
        isPaused = false;
        pauseBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pause);
        pauseBitmap = Bitmap.createScaledBitmap(pauseBitmap,130,130,true);
        backgroundBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.bg);
        
        timer = new DrawThread.Timer();
        timer.start();
    }



    //смена флага остановки треда
    public void requestStop() {
        running = false;
    }

    // метод onDraw, только в фоновом треде
    @Override
    public void run() {
        Canvas canvas;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setTextSize(45.0f);
        p.setColor(Color.BLACK);

        while (running) {
            canvas = surfaceHolder.lockCanvas();
            viewWidth = canvas.getWidth();
            viewHeight = canvas.getHeight();
            try {
                //прорисовка фона и созданных объектов
                canvas.drawBitmap(backgroundBitmap, 0, 0, paint);
                playerBird.draw(canvas);
                enemyBird.draw(canvas);
                touchBird.draw(canvas);
                Bonus.draw(canvas);
                pauseX = canvas.getWidth()-150;
                pauseY = canvas.getHeight()-150;
                canvas.drawBitmap(pauseBitmap, pauseX, pauseY, p);
                canvas.drawText("Уровень: "+level, viewWidth - 250, 120, p);
                canvas.drawText("Очки: "+points, viewWidth - 250, 70, p);
                //Если в паузе, то пишем Pause
                if(isPaused){
                    p.setTextSize(100);
                    canvas.drawText("Пауза",viewWidth/2-120,viewHeight/2,p);
                    p.setTextSize(45.0f);
                }
            } finally {
                //аналок сурфейса после перерисовки
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    protected void update () {
        playerBird.updateSprite(timerInterval);
        enemyBird.updateSprite(timerInterval);
        touchBird.updateSprite(timerInterval);
        Bonus.updateSprite(timerInterval);
        //касания стенки
        if (playerBird.getY() + playerBird.getFrameHeight() > viewHeight) {
            playerBird.setY(viewHeight - playerBird.getFrameHeight());
            playerBird.setVy(-playerBird.getVy());
            points--;
        }
        //касания стенки
        else if (playerBird.getY() < 0) {
            playerBird.setY(0);
            playerBird.setVy(-playerBird.getVy());
        }
        //телепорт противника при касании края
        if (enemyBird.getX() < - enemyBird.getFrameWidth()) {
            teleportAnySprite(enemyBird);
            points +=10;
        }
        //телепорт противника при касании игрока
        if (enemyBird.intersect(playerBird)) {
            teleportAnySprite(enemyBird);
            points -= 20;
        }
        //телепорт тачптички при касании края
        if (touchBird.getX() < - touchBird.getFrameWidth()) {
            teleportAnySprite(touchBird);
            points -=10;
        }
        //телепорт тачптички при касании игрока
        if (touchBird.intersect(playerBird)) {
            teleportAnySprite(touchBird);
            points -= 20;
        }
        //телепорт бонуса при пересечении границы
        if (Bonus.getX() < - Bonus.getFrameWidth()) {
            teleportAnySprite(Bonus);
        }
        //телепорт боунса при касании игрока
        if (Bonus.intersect(playerBird)) {
            teleportAnySprite(Bonus);
            points += 10;
        }
        //меняем уровень
        if (points>=30){
            NextLevel();
        }
        //конец игры
        if(points<=-300){
            EndGame();
        }
    }
    public boolean onTouchEvent(MotionEvent event){

        int eventAction = event.getAction();
        if (eventAction == MotionEvent.ACTION_DOWN)  {
            //обработка касаний (пауза)
            if (event.getX() >= pauseX-100 && event.getY() >= pauseY-100){
                //если пауза не стоит, посатвить и надоборот
                if(!isPaused){
                    isPaused = true;
                    timer.cancel();
                }
                else if (isPaused){
                    isPaused = false;
                    timer.start();
                }
            }
            //обработка касаний игрока
            else {
                //если касанеие попало в спрайт птички, телепортируем ее и получаем очки
                if(event.getX() <= touchBird.getX()+touchBird.getFrameWidth() && event.getX() >= touchBird.getX()
                        && event.getY() <= touchBird.getY()+touchBird.getFrameHeight() && event.getY() >= touchBird.getY()){
                    teleportAnySprite(touchBird);
                    points+=15;
                }
                //если касание ниже/выше нашей птички, ее скорость изменяется
                else if (event.getY() < playerBird.getBoundingBoxRect().top) {
                    playerBird.setVy(-400);
                    points--;
                } else if (event.getY() > (playerBird.getBoundingBoxRect().bottom)) {
                    playerBird.setVy(400);
                    points--;
                }
            }
        }
        return true;
    }
    //телепортация спрайтов
    private void teleportAnySprite (Sprite sprite) {
        sprite.setX(viewWidth + Math.random() * 300);
        sprite.setY(Math.random() * (viewHeight - sprite.getFrameHeight()));
    }
    //переход на слуд. уровень
    private void NextLevel(){
        level++;
        //добавляем скорость врагов и обнуляем очки
        enemyBird.setVx(enemyBird.getVx()-80);
        touchBird.setVx(touchBird.getVx()-80);
        points = 0;
        Toast.makeText(context,"Следующий уровень!",Toast.LENGTH_SHORT);
    }

    private void EndGame(){
        timer.cancel();
        LayoutInflater li = LayoutInflater.from(context);
        View diView = li.inflate(R.layout.restartdialog, null);
        AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(context);
        mDialogBuilder.setView(diView);
        mDialogBuilder.setCancelable(false);
        final AlertDialog alertDialog = mDialogBuilder.create();
        alertDialog.show();
        diView.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                level = 1;
                points = 0;
                playerBird.setY(0);
                enemyBird.setVx(-300);
                touchBird.setVx(-300);
                teleportAnySprite(enemyBird);
                teleportAnySprite(touchBird);
                teleportAnySprite(Bonus);
                timer.start();
                alertDialog.cancel();
            }
        });
    }

}
