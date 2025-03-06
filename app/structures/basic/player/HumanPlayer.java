package structures.basic.player;



public class HumanPlayer extends Player {

    private int wraithlingSwarmCounter = 3;

    public HumanPlayer() {
        super();
    }

    public int getWraithlingSwarmCounter() {
        return wraithlingSwarmCounter;
    }

    public void setWraithlingSwarmCounter(int i) {
        wraithlingSwarmCounter = i;
    }

}

