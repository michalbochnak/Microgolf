package minigolfcom.cs478project04microgolf;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Random;
import minigolfcom.cs478project04microgolf.Player.PlayerOne;
import minigolfcom.cs478project04microgolf.Player.PlayerTwo;


//
// Hosts the game. Performs UI realted operations and coordinates
// the player's turns etc
//
public class MainActivity extends AppCompatActivity {

    private int WIN_HOLE;
    private static final String TAG = "MainActivity";
    public static final int NONE = 0, P_1 = 1, P_2 = 2, JACKPOT = 101,
        NEAR_MISS = 102, NEAR_GROUP = 103, BIG_MISS = 104, CATASTROPHE = 105,
        P_1_LAST_TRY = 501, P_2_LAST_TRY = 502, P_1_JACKPOT = 601, P_2_JACKPOT = 602,
        GAME_START = 201, GAME_END = 202;
    private int winner, turn;
    private PlayerOne playerOne;
    private PlayerTwo playerTwo;
    private int playerOneLastTry, playerTwoLastTry;
    private ArrayList<Integer> misses;
    private TextView infoTextView;
    private int catastropheHole;

    //
    // Handle message
    //
    public Handler UiHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.arg1 == P_1) {
                infoTextView.setText("Now is player Two turn");
                handlePlayerOne(msg.arg2);
            }
            else if (msg.arg1 == P_2) {
                infoTextView.setText("Now is player One turn");
                handlePlayerTwo(msg.arg2);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addButtonListener();
    }

    private void addButtonListener() {
        findViewById(R.id.startGameButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupGame();
                startGame();
            }
        });

    }

    private void setupGame() {
        clearBoard();
        catastropheHole = -1;
        WIN_HOLE = new Random().nextInt(49) + 1;
        setHoleImage(WIN_HOLE, JACKPOT);
        winner = NONE;
        turn = P_1;
        playerOneLastTry = -1;
        playerTwoLastTry = -1;
        infoTextView = findViewById(R.id.currentActionTextView);
        misses = new ArrayList<Integer>();
        playerOne = new PlayerOne(this);
        playerTwo = new PlayerTwo(this);
        new Thread(playerOne).start();
        new Thread(playerTwo).start();
    }

    public void clearBoard() {
        for (int i = 1; i <= 50; ++i)
            setHoleImage(i, NONE);
    }

    private void startGame() {
        sleep(500);

        Message msg = playerOne.playerOneHandler.obtainMessage();
        msg.obj = new PlayerMessage(turn, -1, -1, GAME_START);
        playerOne.playerOneHandler.sendMessage(msg);
    }

    //
    // Set the hole image based on the hole type
    //
    private void setHoleImage(int holeNum, int holeType) {
        int viewId = getResources().getIdentifier("hole_"
                + Integer.toString(holeNum),"id", getPackageName());
        int rscId = getImageId(holeType);
        findViewById(viewId).setBackgroundResource(rscId);
    }

    //
    // Returns the image id for the given hole type
    //
    private int getImageId(int holeType) {
        switch (holeType) {
            case NONE:
                return  getResources().getIdentifier("hole_white",
                        "drawable", getPackageName());
            case JACKPOT:
                return  getResources().getIdentifier("hole_blue",
                        "drawable", getPackageName());
            case NEAR_MISS:
                return  getResources().getIdentifier("hole_light_gray",
                        "drawable", getPackageName());
            case NEAR_GROUP:
                return  getResources().getIdentifier("hole_light_gray",
                        "drawable", getPackageName());
            case BIG_MISS:
                return  getResources().getIdentifier("hole_light_gray",
                        "drawable", getPackageName());
            case CATASTROPHE:
                return  getResources().getIdentifier("hole_red",
                        "drawable", getPackageName());
            case P_1_LAST_TRY:
                return  getResources().getIdentifier("hole_p1",
                        "drawable", getPackageName());
            case P_2_LAST_TRY:
                return  getResources().getIdentifier("hole_p2",
                        "drawable", getPackageName());
            case P_1_JACKPOT:
                return  getResources().getIdentifier("hole_p1_jack",
                        "drawable", getPackageName());
            case P_2_JACKPOT:
                return  getResources().getIdentifier("hole_p2_jack",
                        "drawable", getPackageName());
            default:
                return  getResources().getIdentifier("hole_white",
                        "drawable", getPackageName());
        }
    }

    //
    // player one move
    //
    private void handlePlayerOne(int holeNum) {

        turn = P_2;

        // Note: Omitted
        // Mark the used hole
        if (holeNum > 0)
            //misses.add(holeNum);
        if (holeNum !=  -1)
            playerOneLastTry = holeNum;
        Handler mHandler = playerOne.playerOneHandler;
        // winner
        if (holeNum == WIN_HOLE) {
            Message msg = mHandler.obtainMessage();
            msg.obj = new PlayerMessage(turn, holeNum, JACKPOT, GAME_END);
            mHandler.sendMessage(msg);

            // answer other player
            mHandler = playerTwo.playerTwoHandler;
            msg = mHandler.obtainMessage();
            msg.obj = new PlayerMessage(turn, holeNum, -1, GAME_END);
            mHandler.sendMessage(msg);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    setWinner(P_1);
                }
            });
        }
        // not a winner, game continues
        else {
            Message msg = mHandler.obtainMessage();
            int res = determineResult(holeNum);
            // catastrophe
            if (playerOneLastTry == playerTwoLastTry) {
                playerTwoWins();
                catastropheHole = holeNum;
                setHoleImage(holeNum, CATASTROPHE);
            }
            else {
                msg.obj = new PlayerMessage(turn, holeNum, res, GAME_START);
                mHandler.sendMessage(msg);

                // answer other player
                mHandler = playerTwo.playerTwoHandler;
                msg = mHandler.obtainMessage();
                msg.obj = new PlayerMessage(turn, holeNum, -1, GAME_START);
                mHandler.sendMessage(msg);
            }
        }

        redrawBoard();
    }

    //
    // player two move
    //
    private void handlePlayerTwo(int holeNum) {

        turn = P_1;
        // Note: Omitted
        // Mark the used hole
        if (holeNum > 0)
            //misses.add(holeNum);
        if (holeNum !=  -1)
            playerTwoLastTry = holeNum;
        Handler mHandler = playerTwo.playerTwoHandler;
        // winner
        if (holeNum == WIN_HOLE) {
            Message msg = mHandler.obtainMessage();
            msg.obj = new PlayerMessage(turn, holeNum, JACKPOT, GAME_END);
            mHandler.sendMessage(msg);

            // answer other player
            mHandler = playerOne.playerOneHandler;
            msg = mHandler.obtainMessage();
            msg.obj = new PlayerMessage(turn, holeNum, -1, GAME_END);
            mHandler.sendMessage(msg);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    setWinner(P_2);
                }
            });
        }
        // not a winner, game continues
        else {
            Message msg = mHandler.obtainMessage();
            int res = determineResult(holeNum);
            Log.i(TAG, "");
            // catastrophe
            if (playerOneLastTry == playerTwoLastTry) {
                playerOneWins();
                catastropheHole = holeNum;
                setHoleImage(holeNum, CATASTROPHE);
            }
            else {
                msg.obj = new PlayerMessage(turn, holeNum, res, GAME_START);
                mHandler.sendMessage(msg);

                // answer other player
                mHandler = playerOne.playerOneHandler;
                msg = mHandler.obtainMessage();
                msg.obj = new PlayerMessage(turn, holeNum, -1, GAME_START);
                mHandler.sendMessage(msg);
            }
        }

        redrawBoard();
    }

    //
    // refreshes UI state
    //
    public void redrawBoard() {
        // set all to white
        clearBoard();

        // jackpot
        setHoleImage(WIN_HOLE, JACKPOT);

        // redraw player moves
        if (playerOneLastTry != -1)
            setHoleImage(playerOneLastTry, P_1_LAST_TRY);
        if (playerTwoLastTry != -1)
            setHoleImage(playerTwoLastTry, P_2_LAST_TRY);

        if (winner == P_1) {
            infoTextView.setText("Player One WON!");
            Log.i(TAG, "p1: " + playerOneLastTry + "p2: " + playerTwoLastTry);
            if (catastropheHole == -1)
                setHoleImage(WIN_HOLE, P_1_JACKPOT);
        }
        else if (winner == P_2) {
            infoTextView.setText("Player Two WON!");
            Log.i(TAG, "p1: " + playerOneLastTry + "p2: " + playerTwoLastTry);
            if (catastropheHole == -1)
                setHoleImage(WIN_HOLE, P_2_JACKPOT);
        }

        if (catastropheHole != -1)
            setHoleImage(catastropheHole, CATASTROPHE);

    }

    private synchronized void setWinner(int playerNum) {
        winner = playerNum;
    }

    //
    // handles actions related to case when player one wins the game
    //
    private void playerOneWins() {
        winner = P_1;
        Handler oneHandler = playerOne.playerOneHandler;
        Message msg =oneHandler.obtainMessage();
        msg.obj = new PlayerMessage(turn, -1, JACKPOT, GAME_END);
        oneHandler.sendMessage(msg);

        // answer other player
        Handler twoHandler = playerTwo.playerTwoHandler;
        msg = twoHandler.obtainMessage();
        msg.obj = new PlayerMessage(turn, -1, -1, GAME_END);
        twoHandler.sendMessage(msg);
    }

    //
    // handles actions related to case when player two wins the game
    //
    private void playerTwoWins() {
        winner = P_2;
        Handler oneHandler = playerOne.playerOneHandler;
        Message msg =oneHandler.obtainMessage();
        msg.obj = new PlayerMessage(turn, -1, -1, GAME_END);
        oneHandler.sendMessage(msg);

        // answer other player
        Handler twoHandler = playerTwo.playerTwoHandler;
        msg = twoHandler.obtainMessage();
        msg.obj = new PlayerMessage(turn, -1, JACKPOT, GAME_END);
        twoHandler.sendMessage(msg);
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    //
    // returns the outcome of the try base on the hole number
    //
    private int determineResult(int holeNum) {
        if (isNearMiss(WIN_HOLE, holeNum))
            return NEAR_MISS;
        else if (isNearGroup(WIN_HOLE, holeNum))
            return NEAR_GROUP;
        else if (playerOneLastTry == playerTwoLastTry)
            return CATASTROPHE;
        else
            return BIG_MISS;
    }

    //
    // true if try was in the same group as jackpot
    //
    private boolean isNearMiss(int a, int b) {
        int winHoleGroup = (a / 10) + 1;
        int currTryGroup = (b / 10) + 1;

        return winHoleGroup == currTryGroup;
    }

    //
    // true if try was in the adjacent group to the jackpot
    //
    private boolean isNearGroup(int a, int b) {
        int winHoleGroup = (a / 10) + 1;
        int currTryGroup = (b / 10) + 1;
        int distance = winHoleGroup - currTryGroup;

        // difference is not more than 1 (absolute value)
        return distance == 1 || distance == -1;
    }


}   // MainActivity class



