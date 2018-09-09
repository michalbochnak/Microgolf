package minigolfcom.cs478project04microgolf.Player;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import minigolfcom.cs478project04microgolf.MainActivity;
import minigolfcom.cs478project04microgolf.PlayerMessage;


//
// This thread is always picking best available hole, based on data that was collected
//
public class PlayerTwo extends Player {

    private static final String TAG = "PlayerTwo";
    public Handler playerTwoHandler;

    public PlayerTwo(MainActivity act) {
        super(act);
    }

    public void run() {
        Looper.prepare();

        //
        // handle actions for player one
        //
        playerTwoHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (((PlayerMessage)msg.obj).getGameStatus() == MainActivity.GAME_START) {
                    if (((PlayerMessage)msg.obj).getTurn() == 2) {
                        makeMove();
                    }
                    else if (((PlayerMessage) msg.obj).getResultHole() == MainActivity.JACKPOT) {
                        // WON
                        getHoles().setJackpot(((PlayerMessage) msg.obj).getResultHole());
                    }
                    else {
                        getHoles().markNewTry(((PlayerMessage)msg.obj).getHoleNum(), ((PlayerMessage)
                                msg.obj).getResultHole());
                    }
                }
                else if (((PlayerMessage)msg.obj).getGameStatus() == MainActivity.GAME_END) {
                    //Looper.myLooper().quitSafely();
                    Thread.currentThread().interrupt();
                }
            }
        };

        Looper.loop();
    }

    //
    // Makes move on the manner of picking most efficient move
    // based on last feedback from the UI thread
    //
    protected void makeMove() {
        sleep(1000);
        Message msg = getUiThread().UiHandler.obtainMessage();
        // player 2
        msg.arg1 = 2;
        // FIXME: update holes status
        int nextTarget = getHoles().getTargetTry();
        msg.arg2 = nextTarget;
        getUiThread().UiHandler.sendMessage(msg);
    }
}   // PlayerTwo class
