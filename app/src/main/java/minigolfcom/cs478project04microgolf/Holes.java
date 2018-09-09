package minigolfcom.cs478project04microgolf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


//
// Store the current state of the holes for the player
// Performs operations such as finding the whole in same
// group, close group, random etc.
//
public class Holes {

    private final int MISS = 4, NONE = 5, LAST_TRY = 6, JACKPOT = 101,
            NEAR_MISS = 102, NEAR_GROUP = 103;
    private int closeGroup;
    private int sameGroup;
    private int lastTry;
    private int lastTryType;
    private HashMap<Integer, Integer> holes;
    private ArrayList<Integer> freeHoles;


    public Holes() {
        holes = new HashMap<Integer, Integer>();
        freeHoles = new ArrayList<>();
        closeGroup = -1;
        sameGroup = -1;
        lastTry = -1;
        lastTryType = NONE;

        // initialize all holes to empty
        for (int i = 1; i <= 50; ++i) {
            holes.put(i, NONE);
            freeHoles.add(i);
        }
    }

    public void setLastTry(int holeNumber) {
        clearLastTry();
        holes.put(holeNumber, LAST_TRY);
        lastTry = holeNumber;
    }

    public void setMissed(int holeNumber) {
        holes.put(holeNumber, MISS);
    }

    //
    // returns random hole that was not hit yet
    //
    public int getRandomAvailableHole() {
        int index = -1;
        if (freeHoles.size() > 1)
            index = new Random().nextInt(freeHoles.size() - 1);
        else if (freeHoles.size() == 1)
            index = 0;

        return freeHoles.get(index);
    }

    //
    // returns random hole that was not tried yet in the group
    // that last hit and adjacent groups
    //
    public int getCloseGroupTarget() {
        // collect close group targets
        ArrayList<Integer> closeGroup = new ArrayList<>();
        for (Integer hole : freeHoles) {
            if (isInCloseGroup(hole)) {
                closeGroup.add(hole);
            }
        }

        Integer target = -1;
        if (closeGroup.size() > 1)
            target = closeGroup.get(new Random().nextInt(closeGroup.size() - 1) + 1);
        else if (closeGroup.size() == 1)
            target = closeGroup.get(0);

        freeHoles.remove(target);
        return target;
    }

    //
    // returns random hole in the same group as last hit
    //
    public int getSameGroupTarget() {
        // collect same group targets
        ArrayList<Integer> sameGroup = new ArrayList<>();
        for (Integer hole : freeHoles) {
            if (isInSameGroup(hole)) {
                sameGroup.add(hole);
            }
        }

        Integer target = -1;
        if (sameGroup.size() > 1) {
            target =  sameGroup.get(new Random().nextInt(sameGroup.size() - 1) + 1);
        }
        else if (sameGroup.size() == 1)
            target = sameGroup.get(0);

        freeHoles.remove(target);
        return target;
    }

    //
    // returns best possible target hole based on last try
    //
    public int getTargetTry() {
        int bestTarget = -1;

        if (lastTryType == NEAR_MISS)
            bestTarget = getSameGroupTarget();
        else if (lastTryType == NEAR_GROUP)
            bestTarget = getCloseGroupTarget();
        else
            bestTarget = getRandomAvailableHole();

        return bestTarget;
    }

    //
    // returns true if hole is in the same group or in adjacent group
    //
    private boolean isInCloseGroup(int hole) {
        if (lastTry == -1)
            return false;

        int lastTryGroupId = (lastTry / 10) + 1;
        int currHoleGroupId = (hole / 10) + 1;

        int distance = lastTryGroupId - currHoleGroupId;

        return distance == 1 || distance == 0 || distance == -1;
    }

    //
    // returns true if hole is in the same group
    //
    private boolean isInSameGroup(int hole) {
        if (lastTry == -1)
            return false;

        int lastTryGroupId = ((lastTry - 1) / 10) + 1;
        int currHoleGroupId = ((hole - 1) / 10) + 1;

        int distance = lastTryGroupId - currHoleGroupId;

        return distance == 0;
    }

    //
    // clear hole that was last hit
    //
    private void clearLastTry() {
        for (int i = 1; i <= 50; ++i)
            if (holes.get(i) == LAST_TRY)
                setMissed(i);
    }

    public void setJackpot(int holeNum) {
        holes.put(holeNum, JACKPOT);
    }

    //
    // add new try and mark the type it was
    //
    public void markNewTry(Integer holeNum, int res) {
        freeHoles.remove(holeNum);
        setLastTry(holeNum);
        switch (res) {
            case JACKPOT:
                setJackpot(holeNum);
                lastTryType = JACKPOT;
                break;
            case NEAR_MISS:
                setMissed(holeNum);
                sameGroup = (holeNum / 10) + 1;
                lastTryType = NEAR_MISS;
                break;
            case NEAR_GROUP:
                setMissed(holeNum);
                closeGroup = (holeNum / 10) + 1;
                lastTryType = NEAR_GROUP;
                break;
        }
    }

}   // Holes class
