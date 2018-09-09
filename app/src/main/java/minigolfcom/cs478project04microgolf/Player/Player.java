package minigolfcom.cs478project04microgolf.Player;

import minigolfcom.cs478project04microgolf.Holes;
import minigolfcom.cs478project04microgolf.MainActivity;


//
// Superclass for the players
//
public abstract class Player implements Runnable{

    private MainActivity uiThread;
    private Holes holes;

    public Player(MainActivity act) {
        uiThread = act;
        holes = new Holes();
    }

    public void run() {
        // deferred to subclass
    }

    protected void makeMove() {
        // deferred to subclass
    }

    protected void sleep(int millis) {
        try {
            Thread.sleep(millis);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MainActivity getUiThread() {
        return uiThread;
    }

    public Holes getHoles() {
        return holes;
    }

}   // Player class
