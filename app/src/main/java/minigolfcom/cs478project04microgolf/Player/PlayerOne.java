package minigolfcom.cs478project04microgolf.Player;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import minigolfcom.cs478project04microgolf.MainActivity;
import minigolfcom.cs478project04microgolf.PlayerMessage;


//
// This thread is picking next hole by switching between
// possible shots in sequential matter
//
public class PlayerOne extends Player {

    private static final String TAG = "PlayerOne";
    public Handler playerOneHandler;
    private int shotType;

    public PlayerOne(MainActivity act) {
        super(act);
        shotType = 0;
    }

    @Override
    public void run() {
        Looper.prepare();

        //
        // handle actions for player one
        //
        playerOneHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (((PlayerMessage)msg.obj).getGameStatus() == MainActivity.GAME_START) {
                    if (((PlayerMessage)msg.obj).getTurn() == 1) {
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
    // Makes move in sequential manner, each move type is performed
    // every 4 moves
    //
    protected void makeMove() {
        sleep(1000);

        int nextTarget = -1;
        if (shotType % 4 == 0) {
            nextTarget = getHoles().getCloseGroupTarget();
        }
        else if (shotType % 4 == 1) {
            nextTarget = getHoles().getSameGroupTarget();
        }
        else if (shotType % 4 == 2) {
            nextTarget = getHoles().getTargetTry();
        }
        else {
            nextTarget = getHoles().getRandomAvailableHole();
        }

        if (nextTarget == -1)
            nextTarget = getHoles().getRandomAvailableHole();

        Message msg = getUiThread().UiHandler.obtainMessage();
        // player one
        msg.arg1 = 1;
        msg.arg2 = nextTarget;
        getUiThread().UiHandler.sendMessage(msg);
        shotType++;
    }

}   // PlayerOne class
