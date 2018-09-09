package minigolfcom.cs478project04microgolf;


//
// Stores the information that are send between threads
//
public class PlayerMessage {

    private int turn;
    private int holeNum;
    private int resultHole;
    private int gameStatus;

    public PlayerMessage(int turn, int holeNum, int result, int gameStatus) {
        this.turn = turn;
        this.holeNum = holeNum;
        this.resultHole = result;
        this.gameStatus = gameStatus;
    }

    public int getGameStatus() {
        return gameStatus;
    }

    public int getTurn() {
        return turn;
    }

    public int getHoleNum() {
        return holeNum;
    }

    public int getResultHole() {
        return resultHole;
    }

}   // PlayerMessage
