package fivepiles;

///////////////////////////////////////// Imports /////////////////////////////////////////
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;
///////////////////////////////////////// Imports /////////////////////////////////////////

public class FivePiles
{


    // CONSTANTS
    public static final int TABLE_HEIGHT = Card.CARD_HEIGHT * 4 + 100; //150*4 = 600
    public static final int TABLE_WIDTH = (Card.CARD_WIDTH * 7) + 100; //100 * 7 = 700
    public static final int NUM_PLAY_DECKS = 7; // 5 playing piles, 2 extra for the last 2 cards.
    public static final Point DECK_POS = new Point(5, 5);
    public static final Point SHOW_POS = new Point(DECK_POS.x + Card.CARD_WIDTH + 5, DECK_POS.y);
    public static final Point FINAL_POS = new Point(SHOW_POS.x + Card.CARD_WIDTH + 150, DECK_POS.y);
    public static final Point PLAY_POS = new Point(DECK_POS.x, FINAL_POS.y + Card.CARD_HEIGHT + 30);

    // GAMEPLAY STRUCTURES
    public static CardStack[] playCardStack; // Tableau stacks
    private static CardStack deck; // populated with standard 52 card deck

    // GUI COMPONENTS (top level)
    private static final JFrame frame = new JFrame("Five Piles");
    public static JLabel table = new JLabel();
    // other components
    private static JEditorPane gameTitle = new JEditorPane("text/html", ""); //
    private static JButton showRulesButton = new JButton("Show Rules");
    private static JButton newGameButton = new JButton("New Game");
    private static JButton menuButton = new JButton("Menu"); // Returns player to main platform/menu.
    private static JButton menuSureButton = new JButton("Are you sure? You will lose any progress.");
    private static JButton toggleTimerButton = new JButton("Pause Timer");
    private static JButton exitStatistics = new JButton("Return to Menu");
    private static JButton resetStatistics = new JButton("Reset Score File");
    private static JTextField scoreBox = new JTextField();// displays the score
    private static JTextField timeBox = new JTextField();// displays the time
    private static JTextField statusBox = new JTextField();// status messages
    private static JTextArea statisticsTextDisplay = new JTextArea(); //formats the statistics text
    private static JTextArea statisticsNumbersDisplay = new JTextArea(); //formats the statistics values
    private static JTextArea topStatisticsTextDisplay = new JTextArea(); //formats the top statistics text
    private static JTextArea topStatisticsNamesDisplay = new JTextArea(); //formats the top statistics names
    private static JTextArea topStatisticsScoresDisplay = new JTextArea(); //formats the top statistics scores
    private static JTextArea topStatisticsTimesDisplay = new JTextArea(); //formats the top statistics times
    private static JTextArea topStatisticsWinsDisplay = new JTextArea(); //formats the top statistics wins
    private static final Card newCardButton = new Card();// reveal waste card

    private static String inputName = null; //for validating playerName

    private static JButton selectGameButton = new JButton("Select Game");
    private static JButton statisticsButton = new JButton("Statistics");
    private static JButton exitMenuButton = new JButton("Exit");
    private static JButton fivePilesButton = new JButton("Five Piles");
    private static JComboBox<String> profileButton;

    // TIMER UTILITIES
    private static Timer timer = new Timer();
    private static Timer movementTimer = new Timer();
    private static ScoreClock scoreClock = new ScoreClock();
    private static MovementClock movementClock = new MovementClock();

    private static ArrayList<CardStack> movementFromList = new ArrayList();
    private static ArrayList<CardStack> movementToList = new ArrayList();
    private static ArrayList<Card> movementCardList = new ArrayList();
    private static double previousTime = 0;

    // ANIMATION
    private static double animationUpdateFrequency = 10; // This means we update every x milliseconds.
    private static int animationMovementIncrement = 25; // We move by this much every animation update.


    private static int index = -1; // keep track of which element is selected for the profile button.

    // MISC TRACKING VARIABLES
    private static boolean timeRunning = false;// timer running?
    private static boolean gameLive = false; //is the game currently running?
    private static int score = 0;// keep track of the score
    private static int time = 0;// keep track of seconds elapsed
    private static double movementTime = 0;
    private static int win = 0;// keep track of whether the game was won or lost

    // moves a card to absolute location within a component
    protected static Card moveCard(Card c, int x, int y) {
        c.setBounds(new Rectangle(new Point(x, y), new Dimension(Card.CARD_WIDTH + 10, Card.CARD_HEIGHT + 10)));
        c.setXY(new Point(x, y));
        return c;
    }

    /**
     * Checks whether a card is on top of the card stack.
     *
     * @param c The card to see if is on the top
     * @return The truth value
     */
    protected static boolean isTop(Card c){
        boolean valid = false; // Start by setting a boolean to store our decision.

        if (c != null) { // This is to make sure our card definitely exists (isn't null).
            for (int x = 0; x < NUM_PLAY_DECKS; x++) { // NUM_PLAY_DECKS refers to the number of decks we have in play, we for loop through that.
                if (playCardStack[x].getFirst() != null) { // x is the index corresponding to the playCardStack of x. The first "pile" is playCardStack[1] of type CardStack.
                    if (playCardStack[x].getFirst().getID().equals(c.getID())) { // If the first card in a pile is the SAME card as what we're checking if is in top, then it is on top.
                        valid = true; // The card c is on top of a pile, so it is valid (on top).
                    }
                }
            }
        }

        return valid; // Return true if our card c is on top of a pile, false otherwise.
    }

    /**
     * Returns true if the pair of cards, a and b, are both on top and add to 13.
     * @param a The first card in the pair.
     * @param b The second card in the pair.
     * @return Whether the pair adds up to 13 and is on the top of their respective card stack.
     */
    protected static boolean isValidPair(Card a, Card b){

        int combinedValue = 0; // This is the combined value of two cards paired up.

        if (a != null && b != null){ // If a isn't null and b isn't null then we can add their values.
            combinedValue = a.getNumericalValue() + b.getNumericalValue(); // Add both cards' values.
        }

        return (combinedValue==13 && isTop(a) && isTop(b)); // If both cards add to 13 and they're both on top of a pile, they're a valid pair.
    }

    // Adds/subtracts points based on gameplay actions. This also updates the score visually.
    protected static void setScore(int deltaScore) { // deltaScore is an integer of how much score should change.
        FivePiles.score += deltaScore; // We add to our game score, the deltaScore.
        String newScore = "Score: " + FivePiles.score; // We change our score box message to be our new value.
        scoreBox.setText(newScore); // Update the score box with our new message.
        scoreBox.repaint(); // We call repaint on the GUI element scoreBox to refresh it with our new information we gave it.
    }

    /**
     * This method sets isSelected to true for CardStack c and then loops through all the CardStacks in playCardStack and sets their isSelected to false
     * @param c The CardStack that is being selected.
     */
    public static void select(CardStack c){
        for(int i=0; i<NUM_PLAY_DECKS; i++){
            if (playCardStack[i] != c && playCardStack[i].isSelected()) {
                playCardStack[i].setSelected(false);
            }
        }
        c.setSelected(true);
    }

    /**
     * This method deselects all CardStacks in playCardStack.
     */
    public static void deselectAll(){
        for(int i=0; i<NUM_PLAY_DECKS; i++){
            playCardStack[i].setSelected(false);
        }
    }



    //player class
    private static class Player{

        public static String playerName;
        public static int playerScore = 0;
        public static int playerTime = 0;
        public static int playerWin = 0;

        static ArrayList<String> playerNameList = new ArrayList<>();
        static ArrayList<Integer> playerScoreList = new ArrayList<>();
        static ArrayList<Integer> playerTimeList = new ArrayList<>();
        static ArrayList<Integer> playerWinList = new ArrayList<>();

        static ArrayList<String> topPlayerNameList = new ArrayList<>();
        static ArrayList<Integer> topPlayerScoreList = new ArrayList<>();
        static ArrayList<Integer> topPlayerTimeList = new ArrayList<>();
        static ArrayList<Integer> topPlayerWinList = new ArrayList<>();

        //static ArrayList<Player> topPlayers = new ArrayList<>();
        //static Player[] topPlayers = new Player[5];

        public Player(){

            playerName = playerName.substring(0, 1).toUpperCase() + playerName.substring(1);
            playerName = playerName.replace(" ", "_");
            FivePiles.inputName = playerName;
            FivePiles.score = playerScore;
            FivePiles.time = playerTime;
            FivePiles.win = playerWin;
        }

        public Player(String playerName, int playerScore, int playerTime, int playerWin) {
            playerName = playerName.substring(0, 1).toUpperCase() + playerName.substring(1);
            playerName = playerName.replace(" ", "_");
            Player.playerName = playerName;
            Player.playerScore = playerScore;
            Player.playerTime = playerTime;
            Player.playerWin = playerWin;

            FivePiles.inputName = playerName;
            FivePiles.score = playerScore;
            FivePiles.time = playerTime;
            FivePiles.win = playerWin;
        }

        public static String getPlayerName() {
            return FivePiles.inputName;
        }

        public static int getPlayerScore() {
            return FivePiles.score;
        }

        public static int getPlayerTime(){
            return FivePiles.time;
        }

        public static int getPlayerWin(){
            return FivePiles.win;
        }

        public static int getNumberOfGamesPlayed(){
            if (playerScoreList != null) {
                return playerScoreList.size();
            }
            else {
                return 0;
            }
        }

        public static int getNumberOfGamesWon(){
            int wins = 0;
            if (playerWinList != null && playerWinList.size() > 0) {
                for (int i = 0; i < playerWinList.size(); i++) {
                    if (playerWinList.get(i) == 1) {
                        wins++;
                    }
                }

                return wins;
            }else {
                return 0;
            }
        }

        public static double getWinRatio(){
            if (getNumberOfGamesPlayed() > 0) {
                return getNumberOfGamesWon() / getNumberOfGamesPlayed();
            }else {
                return 0;
            }
        }

        public static int getLastGameScore(){
            if (playerScoreList != null && playerScoreList.size() > 0) {
                return playerScoreList.get(playerScoreList.size() - 1);
            }else {
                return 0;
            }
        }

        public static int getHighestGameScore(){
            int current = 0;
            if (playerScoreList != null && playerScoreList.size() > 0) {
                for (int i = 0; i < playerScoreList.size(); i++) {
                    current = Integer.max(current, playerScoreList.get(i));
                }

                return current;
            }else {
                return 0;
            }
        }

        public static int getLastElapsedTime(){
            if (playerTimeList != null && playerTimeList.size() > 0) {
                return playerTimeList.get(playerTimeList.size() - 1);
            }else {
                return 0;
            }
        }

        public static int getShortestElapsedTime(){
            int current = 999999;

            if (playerTimeList != null && playerTimeList.size() > 0) {
                for (int i = 0; i < playerTimeList.size(); i++) {

                    current = Integer.min(current, playerTimeList.get(i));

                }

                return current;
            }else {
                return 0;
            }
        }

        public static String outputStatsInfo(){


            String returnedStats = "Stats for player " + playerName + "\n"
                    + "Number of games played:     " + getNumberOfGamesPlayed() + " \n"
                    + "Number of games won:         " + getNumberOfGamesWon() + " \n"
                    + "Highest game score:           " + getHighestGameScore() + " \n"
                    + "Last elapsed time:            " + getLastElapsedTime() + " \n"
                    + "Last game score:              " + getLastGameScore() + " \n"
                    + "Win/Loss ratio:               " + getWinRatio() + " \n"
                    + "Shortest elapsed time:        " + getShortestElapsedTime();


            return returnedStats;

        }

        public static String outputStatsNumbers() throws FileNotFoundException {

            boolean filesExist = true;
            if(Player.playerName == null) {
                File userFolder = new File("users");
                File[] numberOfFiles = userFolder.listFiles();
                if(numberOfFiles.length == 0) {
                    filesExist = false;
                } else {
                    loadFile(numberOfFiles[0].toString());
                }
            }
            String returnedStats = "\n"
                    + getNumberOfGamesPlayed() + " \n"
                    + getNumberOfGamesWon() + " \n"
                    + getHighestGameScore() + " \n"
                    + getLastElapsedTime() + " \n"
                    + getLastGameScore() + " \n"
                    + getWinRatio() + " \n"
                    + getShortestElapsedTime();

            if(!filesExist) {
                returnedStats = "";
            }

            return returnedStats;

        }

        public static String outputStatsText() throws FileNotFoundException {
            boolean filesExist = true;
            if(Player.playerName == null) {
                File userFolder = new File("users");
                File[] numberOfFiles = userFolder.listFiles();
                if(numberOfFiles.length == 0) {
                    filesExist = false;
                } else {
                    loadFile(numberOfFiles[0].toString());
                }
            }

            if (playerNameList.size() > 0) {
                playerName = playerNameList.get(0);
            }
            String returnedText = "Stats for player " + playerName + ":\n"
                    + "Number of games played:\n"
                    + "Number of games won:\n"
                    + "Highest game score:\n"
                    + "Last elapsed time:\n"
                    + "Last game score:\n"
                    + "Win/Loss ratio:\n"
                    + "Shortest elapsed time:";
            if(!filesExist) {
                returnedText = "There is no player data to load.";
            }

            return returnedText;
        }

        public static String outputTopStatsInfo() {

            String returnedStats = "Top Statistics \n";

            for(int i = 0; i < topPlayerScoreList.size(); i++) {
                switch(i)
                {
                    case 0: returnedStats += "First place: ";
                        break;
                    case 1: returnedStats += "Second place: ";
                        break;
                    case 2: returnedStats += "Third place: ";
                        break;
                    case 3: returnedStats += "Fourth place: ";
                        break;
                    case 4: returnedStats += "Fifth place: ";
                        break;
                }
                returnedStats += topPlayerNameList.get(i) + " " + topPlayerScoreList.get(i) + " " + topPlayerTimeList.get(i) + " " + topPlayerWinList.get(i) + "\n";
            }

            return returnedStats;
        }

        public static String outputTopStatsText() {

            String returnedStats = "Top Statistics \n"
                    + "First place: \n"
                    + "Second place: \n"
                    + "Third place: \n"
                    + "Fourth place: \n"
                    + "Fifth place: ";

            return returnedStats;
        }

        public static String outputTopStatsNames() {
            String returnedStats = "";
            if (topPlayerNameList.size() > 0) {
                for (int i = 0; i < topPlayerNameList.size(); i++) {
                    String topPlayerName = topPlayerNameList.get(i).substring(0, 1).toUpperCase() + topPlayerNameList.get(i).substring(1);
                    returnedStats += topPlayerName;
                    if (i != topPlayerNameList.size() - 1) {
                        returnedStats += "\n";
                    }
                }
            }
            return returnedStats;
        }


        public static String outputTopStatsScores() {
            String returnedStats = "";
            for(int i = 0; i < topPlayerScoreList.size(); i++) {
                returnedStats += topPlayerScoreList.get(i);
                if(i != topPlayerScoreList.size() - 1) {
                    returnedStats += "\n";
                }
            }
            return returnedStats;
        }

        public static String outputTopStatsTimes() {
            String returnedStats = "";
            for(int i = 0; i < topPlayerTimeList.size(); i++) {
                returnedStats += topPlayerTimeList.get(i);
                if(i != topPlayerTimeList.size() - 1) {
                    returnedStats += "\n";
                }
            }
            return returnedStats;
        }

        public static String outputTopStatsWins() {
            String returnedStats = "";
            for(int i = 0; i < topPlayerWinList.size(); i++) {
                returnedStats += topPlayerWinList.get(i);
                if(i != topPlayerWinList.size() - 1) {
                    returnedStats += "\n";
                }
            }
            return returnedStats;
        }

        public static void setPlayerName() {
            playerName = FivePiles.inputName.toLowerCase();
            playerName = playerName.replace(" ", "_");
            playerName = playerName.substring(0, 1).toUpperCase() + playerName.substring(1);

            try {
                loadFile(playerName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            outputStatsInfo();
        }

        public static void setPlayerScore(int score) {
            playerScore = FivePiles.score;
        }

        public static void setPlayerTime(int time){
            playerTime = FivePiles.time;
        }

        public static void setPlayerWin(int win){
            playerWin = win;
        }
        public static void updateLists(String fileName, int fileScore, int fileTime, int fileWin)
        {
            playerNameList.add(fileName);
            playerScoreList.add(fileScore);
            playerTimeList.add(fileTime);
            playerWinList.add(fileWin);
        }

        /**
         * This method updates the Player class with the top players from the scores file.
         * @return
         * @throws FileNotFoundException
         */
        public static void updateTopPlayers() throws FileNotFoundException {
            int loop = 0;
            File topScoresFile = new File("scores");
            topPlayerNameList.removeAll(topPlayerNameList);
            topPlayerScoreList.removeAll(topPlayerScoreList);
            topPlayerTimeList.removeAll(topPlayerTimeList);
            topPlayerWinList.removeAll(topPlayerWinList);

            if (topScoresFile.exists()) {
                Scanner in = new Scanner(topScoresFile);
                while (in.hasNextLine()) {
                    if(loop == 0)
                    {
                        in.next();
                        loop++;
                    }

                    else {
                        String fileName = "default";
                        int fileScore = 0;
                        int fileTime = 0;
                        int fileWin = 0;

                        try {
                            fileName = in.next();
                            fileScore = in.nextInt();
                            fileTime = in.nextInt();
                            fileWin = in.nextInt();
                        } catch (Exception e){
                            System.out.println("Cannot read scores file.");
                        }

                        if (fileName != "default") {
                            topPlayerNameList.add(fileName);
                            topPlayerScoreList.add(fileScore);
                            topPlayerTimeList.add(fileTime);
                            topPlayerWinList.add(fileWin);
                        }

                    }

                }
            }else {
                try {
                    topScoresFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        public static void insertTopPlayer(int currentGameScore) throws FileNotFoundException {

            boolean wasEmpty = false;

            if(topPlayerScoreList.isEmpty()) {
                wasEmpty = true;
                topPlayerNameList.add(getPlayerName());
                topPlayerScoreList.add(getPlayerScore());
                topPlayerTimeList.add(getPlayerTime());
                topPlayerWinList.add(getPlayerWin());
            }

            for(int i = 0; i < topPlayerScoreList.size(); i++) {

                if(currentGameScore >= topPlayerScoreList.get(i) && !wasEmpty)
                {

                    //These next four insert the values into the correct spot
                    topPlayerNameList.add(i, getPlayerName());
                    topPlayerScoreList.add(i, getPlayerScore());
                    topPlayerTimeList.add(i, getPlayerTime());
                    topPlayerWinList.add(i, getPlayerWin());

                    if(topPlayerScoreList.size() > 5) {
                        topPlayerNameList.remove(5);
                        topPlayerScoreList.remove(5);
                        topPlayerTimeList.remove(5);
                        topPlayerWinList.remove(5);
                    }
                    i = topPlayerScoreList.size();

                }

            }
            updateTopScoresFile();
        }

        public static void resetPlayer(){
            playerName = null;
            playerScore = 0;
            playerTime = 0;
        }

        public static void resetPlayerStats(){
            playerScore = 0;
            playerTime = 0;
        }

        public static String displayInfo(){
            String display = "";
            for(int i = 0; i < playerNameList.size(); i++) {
                display += playerNameList.get(i) + " " + playerScoreList.get(i) + " " + playerTimeList.get(i) + " " + playerWinList.get(i);
                if(i < playerNameList.size() - 1)
                {
                    display += "\n";
                }
            }

            return display;
        }

    }



    public static void updateGameState(boolean newGameState)
    {
        gameLive = newGameState;
    }

    protected static void updateMovementTimer() {
        FivePiles.movementTime += 1;

        // Run animatedMoveCard through a list of to[x] and from[x].
        if (movementTime >= previousTime + animationUpdateFrequency && movementFromList.size() > 0) {
            for (int i = 0; i < movementFromList.size(); i++) {
                animatedMoveCard(movementFromList.get(i), movementToList.get(i), movementCardList.get(i), animationMovementIncrement);
            }
            previousTime = (double)movementTime;
        }
    }


    public static void animatedMoveCard(CardStack from, CardStack to, Card c, int movementIncrement){

        if (!to.hasCard(c)) {
            c.moving = true;
            to.putFirst(c);
            table.add(moveCard(c, from.getX(), from.getY()));
            c.repaint();
            table.repaint();
        }

        int xDiff = to.getX() - from.getX();
        int yDiff = to.getY() - from.getY();
        int xSign = xDiff / (Math.abs(xDiff));
        int ySign = yDiff / (Math.abs(yDiff));

        if (Math.abs(to.getX() - c.getX()) + Math.abs(to.getY() - c.getY()) > movementIncrement){
            xDiff = to.getX() - c.getX();
            yDiff = to.getY() - c.getY();
            xSign = xDiff == 0 ? 1 : (xDiff / (Math.abs(xDiff)));
            ySign = yDiff == 0 ? 1 : (yDiff / (Math.abs(yDiff)));
            moveCard(c, c.getX() + xSign*(movementIncrement > xDiff ? xDiff : movementIncrement), c.getY() + ySign*(movementIncrement > yDiff ? yDiff : movementIncrement));
            table.repaint();
            c.repaint();
        }else {
            int index = movementCardList.indexOf(c);
            movementFromList.remove(index);
            movementToList.remove(index);
            movementCardList.remove(index);
            c.moving = false;
            table.repaint();
            c.repaint();

        }

    }

    /**
     * Adds a cardstack to the from list, a cardstack to the to list, and a card to the card list.
     *
     * @param from An ArrayList containing CardStacks from which cards in the card ArrayList are moving.
     * @param to An ArrayList containing CardStacks to which cards in the card ArrayList are moving.
     * @param c An ArrayList of cards that are moving from one card stack to another.
     */
    public static void animMoveCard(CardStack from, CardStack to, Card c){
        movementFromList.add(from);
        movementToList.add(to);
        movementCardList.add(c);
        c.moving = true;
    }

    // GAME TIMER UTILITIES
    protected static void updateTimer() {
        FivePiles.time += 1;
        setScore(0); // Literally just for updating score every second.
        // every 10 seconds elapsed we take away 2 points
        if (FivePiles.time % 10 == 0) {
            setScore(-2);
        }
        String time = "Seconds: " + FivePiles.time;
        timeBox.setText(time);
        timeBox.repaint();
    }

    protected static void startTimer() {
        if (scoreClock != null){ // He was creating several timers, incrementing all at once.
            scoreClock.cancel(); // Timer fix for acceleration.
        }

        if (movementClock != null){
            movementClock.cancel();
        }

        scoreClock = new ScoreClock();
        movementClock = new MovementClock();
        movementTimer.scheduleAtFixedRate(movementClock, 1, 1);
        // set the timer to update every second
        timer.scheduleAtFixedRate(scoreClock, 1000, 1000);
        timeRunning = true;
    }

    // the pause timer button uses this
    protected static void toggleTimer() {
        if (timeRunning && scoreClock != null) {
            scoreClock.cancel();
            timeRunning = false;
        } else {
            startTimer();
        }
    }

    protected static void updateStatisticsVisuals(){
        try {
            statisticsTextDisplay.setText(Player.outputStatsText());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        statisticsTextDisplay.setFont(new Font("Courier", Font.BOLD, 20));
        statisticsTextDisplay.setBounds(50, (TABLE_HEIGHT/2)-250, 250, 300);
        statisticsTextDisplay.setOpaque(false);
        statisticsTextDisplay.setVisible(true);
        statisticsTextDisplay.setEditable(false);
        table.remove(statisticsTextDisplay);
        table.add(statisticsTextDisplay);

        try {
            statisticsNumbersDisplay.setText(Player.outputStatsNumbers());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        statisticsNumbersDisplay.setFont(new Font("Courier", Font.BOLD, 20));
        statisticsNumbersDisplay.setBounds(300, (TABLE_HEIGHT/2)-249, 50, 300);
        statisticsNumbersDisplay.setOpaque(false);
        statisticsNumbersDisplay.setVisible(true);
        statisticsNumbersDisplay.setEditable(false);
        table.remove(statisticsNumbersDisplay);
        table.add(statisticsNumbersDisplay);

        topStatisticsTextDisplay.setText(Player.outputTopStatsText());
        topStatisticsTextDisplay.setFont(new Font("Courier", Font.BOLD, 20));
        topStatisticsTextDisplay.setBounds(400, (TABLE_HEIGHT/2)-250, 150, 300);
        topStatisticsTextDisplay.setOpaque(false);
        topStatisticsTextDisplay.setVisible(true);
        topStatisticsTextDisplay.setEditable(false);

        topStatisticsNamesDisplay.setText(Player.outputTopStatsNames());
        topStatisticsNamesDisplay.setFont(new Font("Courier", Font.BOLD, 20));
        topStatisticsNamesDisplay.setBounds(550, (TABLE_HEIGHT/2)-225, 100, 300);
        topStatisticsNamesDisplay.setOpaque(false);
        topStatisticsNamesDisplay.setVisible(true);
        topStatisticsNamesDisplay.setEditable(false);

        topStatisticsScoresDisplay.setText(Player.outputTopStatsScores());
        topStatisticsScoresDisplay.setFont(new Font("Courier", Font.BOLD, 20));
        topStatisticsScoresDisplay.setBounds(650, (TABLE_HEIGHT/2)-225, 50, 300);
        topStatisticsScoresDisplay.setOpaque(false);
        topStatisticsScoresDisplay.setVisible(true);
        topStatisticsScoresDisplay.setEditable(false);

        topStatisticsTimesDisplay.setText(Player.outputTopStatsTimes());
        topStatisticsTimesDisplay.setFont(new Font("Courier", Font.BOLD, 20));
        topStatisticsTimesDisplay.setBounds(700, (TABLE_HEIGHT/2)-225, 50, 300);
        topStatisticsTimesDisplay.setOpaque(false);
        topStatisticsTimesDisplay.setVisible(true);
        topStatisticsTimesDisplay.setEditable(false);

        topStatisticsWinsDisplay.setText(Player.outputTopStatsWins());
        topStatisticsWinsDisplay.setFont(new Font("Courier", Font.BOLD, 20));
        topStatisticsWinsDisplay.setBounds(750, (TABLE_HEIGHT/2)-225, 50, 300);
        topStatisticsWinsDisplay.setOpaque(false);
        topStatisticsWinsDisplay.setVisible(true);
        topStatisticsWinsDisplay.setEditable(false);

        table.repaint();
    }

    private static class ComboBoxTitle extends JLabel implements ListCellRenderer
    {
        private String _title;

        public ComboBoxTitle(String title)
        {
            _title = title;
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean hasFocus) {
            if (index == -1 && value == null) setText(_title);
            else setText(value.toString());
            return this;
        }
    }

    private static class ScoreClock extends TimerTask {

        @Override
        public void run() {
            updateTimer();
        }
    }

    private static class MovementClock extends TimerTask {

        @Override
        public void run() {
            updateMovementTimer();
        }
    }

    // BUTTON LISTENERS

    private static class SelectGameListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent ae) {
            // We remove the several buttons since we selected game, and are therefore changing visuals.
            table.remove(selectGameButton);
            table.remove(statisticsButton);
            table.remove(exitMenuButton);
            table.repaint(); // This is to refresh our table (the primary GUI element that holds all our others).
            //


            if (fivePilesButton.getActionListeners().length < 1) { // This condition is to ensure the same action happens only once per click. Otherwise we stack listeners that do the same things, causing repeats.
                fivePilesButton.addActionListener(new NewGameListener());
            }

            // This line defines the bounds of our five piles button, its position and size.
            fivePilesButton.setBounds((TABLE_WIDTH/2)-75, (TABLE_HEIGHT)/2-135, 150, 50);

            // Since we defined our button's listener and bounds, we add it to the table to actually exist.
            table.add(fivePilesButton);
        }

    }

    private static class ExitMenuListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent ae) {
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        }

    }

    private static class StatisticsListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent e) {

            try {
                Player.updateTopPlayers(); // Update the top scores info for our player class every time we open statistics.
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }


            // We remove the several buttons since we selected game, and are therefore changing visuals.
            table.remove(selectGameButton);
            table.remove(statisticsButton);
            table.remove(exitMenuButton);
            table.repaint(); // This is to refresh our table (the primary GUI element that holds all our others).
            //

            try {
                statisticsTextDisplay.setText(Player.outputStatsText());
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
            statisticsTextDisplay.setFont(new Font("Courier", Font.BOLD, 20));
            statisticsTextDisplay.setBounds(50, (TABLE_HEIGHT/2)-250, 250, 300);
            statisticsTextDisplay.setOpaque(false);
            statisticsTextDisplay.setVisible(true);
            statisticsTextDisplay.setEditable(false);

            try {
                statisticsNumbersDisplay.setText(Player.outputStatsNumbers());
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
            statisticsNumbersDisplay.setFont(new Font("Courier", Font.BOLD, 20));
            statisticsNumbersDisplay.setBounds(300, (TABLE_HEIGHT/2)-249, 50, 300);
            statisticsNumbersDisplay.setOpaque(false);
            statisticsNumbersDisplay.setVisible(true);
            statisticsNumbersDisplay.setEditable(false);

            topStatisticsTextDisplay.setText(Player.outputTopStatsText());
            topStatisticsTextDisplay.setFont(new Font("Courier", Font.BOLD, 20));
            topStatisticsTextDisplay.setBounds(400, (TABLE_HEIGHT/2)-250, 150, 300);
            topStatisticsTextDisplay.setOpaque(false);
            topStatisticsTextDisplay.setVisible(true);
            topStatisticsTextDisplay.setEditable(false);

            topStatisticsNamesDisplay.setText(Player.outputTopStatsNames());
            topStatisticsNamesDisplay.setFont(new Font("Courier", Font.BOLD, 20));
            topStatisticsNamesDisplay.setBounds(550, (TABLE_HEIGHT/2)-225, 100, 300);
            topStatisticsNamesDisplay.setOpaque(false);
            topStatisticsNamesDisplay.setVisible(true);
            topStatisticsNamesDisplay.setEditable(false);

            topStatisticsScoresDisplay.setText(Player.outputTopStatsScores());
            topStatisticsScoresDisplay.setFont(new Font("Courier", Font.BOLD, 20));
            topStatisticsScoresDisplay.setBounds(650, (TABLE_HEIGHT/2)-225, 50, 300);
            topStatisticsScoresDisplay.setOpaque(false);
            topStatisticsScoresDisplay.setVisible(true);
            topStatisticsScoresDisplay.setEditable(false);

            topStatisticsTimesDisplay.setText(Player.outputTopStatsTimes());
            topStatisticsTimesDisplay.setFont(new Font("Courier", Font.BOLD, 20));
            topStatisticsTimesDisplay.setBounds(700, (TABLE_HEIGHT/2)-225, 50, 300);
            topStatisticsTimesDisplay.setOpaque(false);
            topStatisticsTimesDisplay.setVisible(true);
            topStatisticsTimesDisplay.setEditable(false);

            topStatisticsWinsDisplay.setText(Player.outputTopStatsWins());
            topStatisticsWinsDisplay.setFont(new Font("Courier", Font.BOLD, 20));
            topStatisticsWinsDisplay.setBounds(750, (TABLE_HEIGHT/2)-225, 50, 300);
            topStatisticsWinsDisplay.setOpaque(false);
            topStatisticsWinsDisplay.setVisible(true);
            topStatisticsWinsDisplay.setEditable(false);

            if (exitStatistics.getActionListeners().length < 1) { // This condition is to ensure the same action happens only once per click.
                exitStatistics.addActionListener(new menuReturnConfirmation());
            }
            exitStatistics.setBounds((TABLE_WIDTH/2)-75, (TABLE_HEIGHT/2)+65, 150, 50);
            exitStatistics.setVisible(true);

            if (resetStatistics.getActionListeners().length < 1){
                resetStatistics.addActionListener(new resetPlayerStatistics());
            }
            resetStatistics.setBounds((TABLE_WIDTH)-175, (TABLE_HEIGHT)-96, 150, 50);

            table.add(exitStatistics);
            table.add(resetStatistics);
            table.add(statisticsTextDisplay);
            table.add(statisticsNumbersDisplay);
            table.add(topStatisticsTextDisplay);
            table.add(topStatisticsNamesDisplay);
            table.add(topStatisticsScoresDisplay);
            table.add(topStatisticsTimesDisplay);
            table.add(topStatisticsWinsDisplay);
            table.repaint();
        }

    }

    private static class ProfileButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            if(profileButton.getSelectedIndex() != index) {
                index = profileButton.getSelectedIndex();
                FivePiles.inputName = profileButton.getSelectedItem().toString();
                Player.setPlayerName();

                try {
                    statisticsTextDisplay.setText(Player.outputStatsText());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                try {
                    statisticsNumbersDisplay.setText(Player.outputStatsNumbers());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                if(exitStatistics.isVisible()) {
                    table.add(statisticsTextDisplay);
                    statisticsTextDisplay.setVisible(true);
                    table.add(statisticsNumbersDisplay);
                    statisticsNumbersDisplay.setVisible(true);
                    table.repaint();
                }
            }
        }

    }

    private static class NewGameListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            playNewGame();
        }

    }

    private static class openMenu implements ActionListener { // The listener for the confirmation return to menu button.

        @Override
        public void actionPerformed(ActionEvent e) {
            if (menuSureButton.isVisible()){ // If the menu is visible, we hide it.
                menuSureButton.hide();
            }else { // Otherwise it isn't visible, so we show it. Basically a toggle.
                menuSureButton.show();
            }
        }

    }

    private static class menuReturnConfirmation implements ActionListener {

        private static boolean fontset = false;

        public menuReturnConfirmation(){
            if (!fontset) { // I changed the font size to be smaller to fit the button.
                menuSureButton.setFont(new Font("Arial", Font.PLAIN, 8));
                fontset = true;
            }

            if (menuSureButton.isVisible()) { // We want to ensure the button isn't visible when created. It pops up when we click return to menu button.
                menuSureButton.hide();
            }

        }
        @Override
        public void actionPerformed(ActionEvent e) { // This is what happens if we click the button.
            menuSureButton.hide(); // We hide ethe menu confirmation button.
            Player.resetPlayerStats();
            exitStatistics.setVisible(false);
            startProgram(); // And startProgram() which returns us to the main menu.
        }

    }

    private static class resetPlayerStatistics implements ActionListener {

        private static boolean fontset = false;

        public resetPlayerStatistics(){
            if (!fontset) { // I changed the font size to be smaller to fit the button.
                menuSureButton.setFont(new Font("Arial", Font.PLAIN, 8));
                fontset = true;
            }

            if (menuSureButton.isVisible()) { // We want to ensure the button isn't visible when created. It pops up when we click return to menu button.
                menuSureButton.hide();
            }

        }
        @Override
        public void actionPerformed(ActionEvent e) { // This is what happens if we click the button.
            File scoreFile = new File("users\\" + Player.getPlayerName()); // Our player's score file.
            File topScoreFile = new File("scores"); // Our top score file.
            String temp = ""; // A temporary string to hold the top score info.

            try {
                if (topScoreFile.exists() && topScoreFile != null){ // If the top score file exists then
                    Scanner s = new Scanner(topScoreFile); // We start a scanner for the top score file


                    while (s.hasNextLine()){ // We loop through every line
                        String comparedWith = s.nextLine(); // Store the line
                        temp = temp + (comparedWith.contains(Player.getPlayerName() + " ") ? "" : comparedWith + (s.hasNextLine() ? "\n" : "")); // If the line contains the player's name with a space after it, then do not include it in the temp string.
                    }

                    s.close(); // Close our scanner so other processes can read/write.

                    PrintWriter out = new PrintWriter(topScoreFile); // Create a printwriter for our top score file so we can update its contents.
                    if (temp.length() > 0) { // Ensure the temporary strings length is more than one, otherwise no need to update.
                        out.print(temp); // Print the contents of our temp string to our top scores file.
                    }

                    out.close(); // Close our print writer so other processes can read/write.
                    Player.updateTopPlayers(); // Update the top scores info for our player class.
                    updateStatisticsVisuals(); // Update the statistics visuals.
                }

                if (scoreFile.exists()) { // If our player's score file exists then
                    scoreFile.delete(); // we just delete it.
                }else { // otherwise
                    System.out.println("Score file for player (" + Player.getPlayerName() + ") does not exist and therefore cannot be deleted/reset.");
                }
            } catch (FileNotFoundException ex) { // We put it in a try/catch to ensure we can handle file not found exceptions.
                ex.printStackTrace();
            }
        }

    }

    private static class ToggleTimerListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            toggleTimer();
            if (!timeRunning) {
                toggleTimerButton.setText("Start Timer");
            } else {
                toggleTimerButton.setText("Pause Timer");
            }
        }

    }

    private static boolean containsInvalidCharacter(String c) {
        String[] invalidCharacters = {"!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "[", "]", "{", "}", ":", ";", "'", "<", ">", ",", ".", "?", "/", "\\", "~", "`", "|", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
        for (int i = 0; i < invalidCharacters.length; i++) { // Loop through every invalid character
            if (c.contains(invalidCharacters[i]) || c.length() > 10){ // If the string contains any of the invalid characters or if the string is longer than 10 characters then
                return true; // return true.
            }
        }
        return false;
    }

    private static class ShowRulesListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            JDialog ruleFrame = new JDialog(frame, true); // Create a new dialog box for our frame.
            ruleFrame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Set what happens when the window closes.
            ruleFrame.setSize(TABLE_HEIGHT, TABLE_WIDTH); // Set the dimensions of the dialog box.
            JScrollPane scroll;
            JEditorPane rulesTextPane = new JEditorPane("text/html", ""); // The actual text/content.
            rulesTextPane.setEditable(false); // Make the text not editable.
            String rulesText = "<b><h2>File Piles Solitaire</h2></b>" // The rules for Five Piles.
                    +  "<br>(From bvssolitaire.com/rules/five-piles.htm) \n 1 deck. Easy. No redeal.<br> "+
                    "" +
                    "<br>Five Piles Solitaire uses one deck (52 cards). You have 5 tableau piles.\n<br>" +
                    "" +
                    "<br><h3>The object of the game:\n</h3>" +
                    "<br>To discard pairs of cards whose ranks add up to 13.\n <br>" +
                    "<br>Here is a list of valid pairs:" +
                    "<ul><li>Queen+Ace</li> <li>Jack+2</li> <li>10+3</li> <li>9+4</li> <li>8+5</li> <li>7+6</li> <li>6+7</li> <li>5+8</li> <li>4+9</li>  <li>3+10</li>  <li>2+Jack</li> <li>Ace+Queen</li> </ul>" +
                    "<br>\n Kings are discarded singularly, To discard a King, just click it.\n<br>" +
                    "<br><h3>The rules:\n</h3>" +
                    "<br><ol> <li>Only the top card of each tableau pile is available for play on the foundations.\n</li> <br>" +
                    "\n" +
                    "<br><li>When you have made all the moves initially available, click on the stock pile to deal one card on each tableau pile. \n</li><<br>" + "<br><li>You cannot move cards from one tableau pile to another. \n</li><br>"   + "<br><li>\n The last 2 cards in the stock are dealt separately from the tableau and can be discarded in a pair with cards from any of the 5 tableau piles.\n</li> <br>" +
                    "\n" +
                    "<br><li>Wins are rare.\n</li><br>" +
                    "\n" +
                    "<br><li>There is no redeal. </li> </ol><br>";
            rulesTextPane.setText(rulesText); // Sets the text for our text GUI element to the rules string above.
            ruleFrame.add(scroll = new JScrollPane(rulesTextPane));

            ruleFrame.setVisible(true); // Set the window of rules to be visible.
        }
    }

    private static class CardMovementManager extends MouseAdapter {

        private boolean checkForWin = false;// should we check if game is over?
        private boolean gameOver = false;// We want to start this as false, since the game isn't over immediately.
        private Point start = null;// where mouse was clicked
        private Card card = null; // card being clicked
        // used for moving single cards
        private CardStack source = null;
        private CardStack dest = null;
        // used for moving a stack of cards
        private CardStack transferStack = new CardStack(false);

        // Testing
        CardStack ChosenCards = new CardStack(false);
        // Testing


        @Override
        public void mousePressed(MouseEvent e) { // Stuff in this method happen when left mouse button is pressed down.
            start = e.getPoint(); // The point where the mouse was when it was clicked.
            boolean stopSearch = false; // Used for searching for what card was pressed.
            statusBox.setText("");
            transferStack.makeEmpty();



            /*
             * Here we use transferStack to temporarily hold all the cards above
             * the selected card in case player wants to move a stack rather
             * than a single card
             */
            for (int x = 0; x < NUM_PLAY_DECKS; x++) {
                if (stopSearch) {
                    break;
                }
                source = playCardStack[x];
                // pinpointing exact card pressed
                for (Component ca : source.getComponents()) {
                    Card c = (Card) ca;
                    if (c.getFaceStatus() && source.contains(start)) {
                        transferStack.putFirst(c);
                    }
                    if (c.contains(start) && source.contains(start) && c.getFaceStatus()) {
                        System.out.println(x);

                        select(playCardStack[x]); // Selects the CardStack that the card is in.
                        card = c;
                        stopSearch = true;
                        break;
                    }
                }




            }

            // If card isn't null (exists / is a card) then add it to an invisible card stack.
            // if that card stack is even, then two cards have been selected.
            // check the value of those two cards added.
            // if that is 13, add twenty points.
            if (card != null) { // When we use a card, it's important to check that it exists first.
                int value = card.getNumericalValue(); // Gets the value of the card clicked on.
                if (ChosenCards.getFirst() != null) { // If the first card in our temporary pile isn't null, it means we selected 2 cards and should combine their values.
                    value += ChosenCards.getFirst().getNumericalValue(); // Combine the values of our two selected cards.
                }

                Card old = null; // Initialize a new variable to hold the card that we selected before.

                if (!card.isKing()){ // If the card we clicked ISN'T a king. This method is custom and doesn't exist for other types.
                    old = ChosenCards.getFirst(); // Store the old card in a variable
                }
                ChosenCards.putFirst(card); // Put the clicked card on top of the temporary card stack.

                if (isValidPair(card, old) || card.isKing() && isTop(card)) { // If the combined value of 2 selected cards is 13, a king, and is on top.
                    deselectAll(); // Deselect all cards since we just made a valid match.
                    score += 20; // Add 20 points. This is just temporary and can be changed.
                    for (int x = 0; x < NUM_PLAY_DECKS; x++) { // Loop through all existing play decks.
                        if (playCardStack[x].getFirst() != null) { // If the first card in the play deck exists
                            if (playCardStack[x].getFirst().getXY().equals(card.getXY()) && playCardStack[x].getFirst().getID().equals(card.getID())) { // Check if it is the same card as the one we clicked on. Basically, if the position (xy) and ID are the same, it's the same card.


                                Card c = playCardStack[x].popFirst(); // We pop the card from the play deck, since it added to 13.

                                if (c != null) {
                                    table.remove(FivePiles.moveCard(c, SHOW_POS.x, SHOW_POS.y)); // We remove the card from the table.

                                    c.repaint(); // Repaint to visualize changes.
                                }
                                table.repaint(); // Repaint to visualize changes.
                            }
                        }
                        if (old != null && playCardStack[x].getFirst() != null) { // If the first card in the play deck exists and the previously clicked card exists
                            if (playCardStack[x].getFirst().getXY().equals(old.getXY()) && playCardStack[x].getFirst().getID().equals(old.getID())) { // Check if it is the same card as the one we PREVIOUSLY clicked on.


                                Card c = playCardStack[x].popFirst(); // We pop the card from the play deck, since it added to 13.

                                if (c != null) {
                                    table.remove(FivePiles.moveCard(c, SHOW_POS.x, SHOW_POS.y)); // We remove the card from the table.

                                    c.repaint(); // Repaint to visualize changes.
                                }
                                table.repaint(); // Repaint to visualize changes.
                            }
                        }


                        ChosenCards = new CardStack(false); // We recreate our temporary cards holder object of type CardStack, ChosenCards.

                    }

                }
            }
            // SHOW (WASTE) CARD OPERATIONS
            // display new show card
            // dealing from the deck
            if (newCardButton.contains(start) && deck.showSize() > 0 && gameLive) { // If the mouse is within the bounds of newCardButton and the deck has more than 0 cards.

                ChosenCards = new CardStack(false); // Added this to ensure we can't pick between cards from different layers.

                deselectAll(); // Deselect all cards since we are dealing new cards.
                for (int x = 0; x < NUM_PLAY_DECKS-2; x++) { // We want to add new cards to our 5 piles, so we for loop through the total number of play decks.
                    if (deck.showSize() > 2) { // Added this condition to account for the deck running out / last 2 cards.
                        Card c = deck.pop().setFaceup(); // We get our card from the deck and set it faceup.
                        if (c != null) { // We make sure the "card" we just got actually exists.
                            //playCardStack[x].putFirst(c); // We put this newly obtained card into the pile we're iterating over.
                            animMoveCard(deck, playCardStack[x], c);
                            //table.add(FivePiles.moveCard(c, SHOW_POS.x, SHOW_POS.y)); // We add the card visual to the table.
                            c.repaint(); // We repaint our card so it shows in the card stack (pile).
                        }
                        table.repaint(); // We repaint the table now so it shows the new card stack (pile).
                    }else { // This part is to handle the last 2 cards. We do this if we have only 2 cards left in the deck.
                        Card c1 = deck.pop(); // We pop the first card.
                        Card c2 = deck.pop(); // We pop the second card.


                        if (c1 != null && c2 != null){ // We make sure the cards we popped actually exist.
                            c1 = c1.setFaceup(); // Set them faceup.
                            c2 = c2.setFaceup(); // Set them faceup.
                            animMoveCard(deck, playCardStack[NUM_PLAY_DECKS-2], c1);
                            animMoveCard(deck, playCardStack[NUM_PLAY_DECKS-1], c2);
                            c1.repaint(); // We repaint so they show up in the pile.
                            c2.repaint(); // We repaint so they show up in the pile.
                        }
                        table.repaint(); // We repaint the table to show updated card stacks (piles).
                    }
                }
                deck.showSize(); // Just to print our decks size after dealing out cards to our piles.

            }
        }



        @Override
        public void mouseReleased(MouseEvent e) { // These things happen when the left mouse button is released.
            checkForWin = true; // This is set to true to check if our move we made won the game.
            gameOver = false; // We set this to false to ensure we don't keep the gameOver value from a previous game.


            // used for status bar updates
            boolean validMoveMade = false;
            String[] options = {"New Game", "Return to Menu", "Return to Game"};
            String[] winOptions = {"New Game", "Return to Menu"};
            int result;

            table.repaint(); // Repaint table to refresh visual elements.
            setScore(0); // Update score.

            // SHOWING STATUS MESSAGE IF MOVE INVALID
            if (!validMoveMade && dest != null && card != null) {
                statusBox.setText("That Is Not A Valid Move");
            }
            // CHECKING FOR WIN
            if (checkForWin) {
                int emptyPiles = 0;
                // cycle through play decks, if they are all empty, then you beat five piles.
                for (int x = 0; x < NUM_PLAY_DECKS; x++) {

                    if (playCardStack[x].empty()) {
                        emptyPiles += 1;
                    }
                }

                if (emptyPiles == 7){
                    gameOver = true;
                }
            }

            if (checkForWin && gameOver && gameLive) { // If we checked for win and gameOver is true then we won!
                toggleTimer(); // Pause the timer since we won.
                statusBox.setText("Game Over!"); // Updates our statusBox (but I don't think the GUI for statusBox is visible.)
                Player.setPlayerScore(score);//grab score and time and assign to player
                Player.setPlayerTime(time);
                Player.setPlayerWin(1);
                Player.updateLists(Player.getPlayerName(), Player.getPlayerScore(), Player.getPlayerTime(), Player.getPlayerWin());
                try {
                    Player.insertTopPlayer(Player.getPlayerScore());
                } catch (FileNotFoundException ex) {
                    System.out.println("insert top player didn't work");
                }
                try
                {
                    saveGame();
                } catch (FileNotFoundException ex)
                {

                }
                updateGameState(false);//this is to show that the game has ended.
                result = JOptionPane.showOptionDialog(table, "Congratulations! You've Won!", "Game State", 2, 1, null, winOptions, null); // Shows a message saying you won.
                switch(result) // Switch statement to go to the correct option. It depends on the result.
                {
                    case 0: playNewGame(); // If result = 0, we playNewGame() meaning user pressed new game.
                        Player.resetPlayerStats();
                        break;
                    case 1: startProgram(); // If result = 1, we startProgram() meaning user pressed main menu.
                        break;
                    default:
                        break;
                }
            }

            else if(deck.empty() && gameLive) // Since we didn't win, we check if we lost instead. Firstly, we ensure the deck is empty and the game is actually running.
            {
                boolean noValidMoves = true; // This starts as true because we're assuming we have no valid moves.

                for (int i=0; i<NUM_PLAY_DECKS;  i++){ // Loop through all piles.
                    for (int j=0; j<NUM_PLAY_DECKS; j++){ // For each loop through all piles, loop again.
                        if (playCardStack[i].getFirst() != null && playCardStack[j].getFirst() != null) { // If neither top card is null then continue.
                            if (isValidPair(playCardStack[i].getFirst(), playCardStack[j].getFirst()) || playCardStack[i].getFirst().getNumericalValue() == 13 || playCardStack[j].getFirst().getNumericalValue() == 13) { // If the pair of cards looped through i, j is valid (13 and on top) or we have a king in either i or j then...
                                noValidMoves = false; // therefore, valid moves exist.
                            }
                        }
                    }
                }

                if (noValidMoves){ // If no valid moves exist, as previously determined, we lose.
                    toggleTimer(); // Since we lost, we toggle timer.
                    updateGameState(false); //this is to show that the game has ended.
                    Player.setPlayerScore(score);//grab score and time and assign to player
                    Player.setPlayerTime(time);
                    Player.setPlayerWin(0);
                    Player.updateLists(Player.getPlayerName(), Player.getPlayerScore(), Player.getPlayerTime(), Player.getPlayerWin());
                    try {
                        Player.insertTopPlayer(Player.getPlayerScore());
                    } catch (FileNotFoundException ex) {
                        System.out.println("insert top player didn't work for game loss.");
                    }
                    try
                    {
                        saveGame();
                    } catch (FileNotFoundException ex)
                    {
                        System.out.println("save game didn't work from game lose");
                    }
                    result = JOptionPane.showOptionDialog(table, "You Lost.", "Game State", 2, 1, null, options, null); // Show a message saying you lost.
                    statusBox.setText("Game Over!"); // Put in the status box you lost.

                    switch(result) // Switch statement to go to the correct option. It depends on the result.
                    {
                        case 0: playNewGame(); // If result = 0, we playNewGame() meaning user pressed new game.
                            Player.resetPlayerStats();
                            break;
                        case 1: startProgram(); // If result = 1, we startProgram() meaning user pressed main menu.
                            //Player.resetPlayer();
                            break;
                        case 2: // If result = 2, the user pressed to exit game.
                            break;
                        default:
                            break;
                    }
                }
            }


            // RESET VARIABLES FOR NEXT EVENT

            start = null;
            source = null;
            dest = null;
            card = null;
            checkForWin = false;
            gameOver = false;

        }// end mousePressed()
    }//end card movement manager class



    private static void playNewGame() {

        updateGameState(true);

        //checks name is not whitespace or blank
        boolean validateName = true;
        if(FivePiles.inputName != null)
        {
            validateName = false;
            Player.setPlayerName(); // Possibly an issue duplicating player scores.
        }
        while(validateName){
            inputName = (String)JOptionPane.showInputDialog("Enter player name: ");
            if (inputName != null) {
                if (inputName.isEmpty() || containsInvalidCharacter(inputName)) {
                    JOptionPane.showMessageDialog(frame, "Please enter name");
                } else {
                    validateName = false;
                }
            }
        }
        if (Player.playerName != inputName) {
            inputName = inputName.replace(" ", "_");
            Player.setPlayerName();
        }


        if (table.getMouseListeners().length < 1) { // If we have 0 listeners, we add a new one. This is to avoid duplicates.
            table.addMouseListener(new CardMovementManager());
        }
        if (table.getMouseMotionListeners().length < 1) { // If we have 0 listeners, we add a new one. This is to avoid duplicates.
            table.addMouseMotionListener(new CardMovementManager());
        }

        deck = new CardStack(true); // Create a deck of 52 cards to deal from.
        deck.shuffle(); // Shuffle our new deck.
        table.removeAll(); // Clean up all the entities on our table.


        // place new card distribution button
        table.add(moveCard(newCardButton, DECK_POS.x, DECK_POS.y));
        // initialize & place play (tableau) decks/stacks
        playCardStack = new CardStack[NUM_PLAY_DECKS];
        for (int x = 0; x < NUM_PLAY_DECKS; x++) {
            playCardStack[x] = new CardStack(false);
            playCardStack[x].setXY((DECK_POS.x + (x * (Card.CARD_WIDTH + 10))), PLAY_POS.y);

            table.add(playCardStack[x]);
        }

        // Dealing new game
        for (int x = 0; x < NUM_PLAY_DECKS-2; x++) {
            int hld = 0;
            Card c = deck.pop().setFaceup();
            if (x < NUM_PLAY_DECKS) {
                playCardStack[x].putFirst(c);
            }
        }

        // reset time since we are starting a new game.
        time = 0;

        // reset score since we are starting a new game.
        FivePiles.score = 0;

        if (menuButton.getActionListeners().length < 1) { // This condition is to ensure the same action happens only once per click.
            menuButton.addActionListener(new openMenu());
        }
        menuButton.setBounds(605, TABLE_HEIGHT - 70, 194, 30);

        if (menuSureButton.getActionListeners().length < 1) { // This condition is to ensure the same action happens only once per click.
            menuSureButton.addActionListener(new menuReturnConfirmation());
        }
        menuSureButton.setBounds(605, TABLE_HEIGHT - 100, 194, 30);

        if (newGameButton.getActionListeners().length < 1) { // This condition is to ensure the same action happens only once per click.
            newGameButton.addActionListener(new NewGameListener());
        }
        newGameButton.setBounds(0, TABLE_HEIGHT - 70, 120, 30); // Bounds for new game button, consisting of position and size.

        if (showRulesButton.getActionListeners().length < 1) { // This condition is to ensure the same action happens only once per click.
            showRulesButton.addActionListener(new ShowRulesListener());
        }
        showRulesButton.setBounds(120, TABLE_HEIGHT - 70, 120, 30); // Bounds for show rules button, consisting of position and size.

        gameTitle.setText(" ");
        gameTitle.setEditable(false);
        gameTitle.setOpaque(false);
        gameTitle.setBounds(245, 20, 100, 100); // Bounds for game title (?), consisting of position and size.

        scoreBox.setBounds(240, TABLE_HEIGHT - 70, 120, 30); // Bounds for score box, consisting of position and size.
        scoreBox.setText("Score: 0");
        scoreBox.setEditable(false);
        scoreBox.setOpaque(false);

        timeBox.setBounds(360, TABLE_HEIGHT - 70, 120, 30); // Bounds for time box, consisting of position and size.
        timeBox.setText("Seconds: 0");
        timeBox.setEditable(false);
        timeBox.setOpaque(false);

        startTimer();

        toggleTimerButton.setBounds(480, TABLE_HEIGHT - 70, 125, 30); // Bounds for toggle timer button, consisting of position and size.
        if (toggleTimerButton.getActionListeners().length < 1) { // This condition is to ensure the same action happens only once per click.
            toggleTimerButton.addActionListener(new ToggleTimerListener());
        }

        statusBox.setBounds(605, TABLE_HEIGHT - 70, 180, 30); // Bounds for status box, consisting of position and size.
        statusBox.setEditable(false);
        statusBox.setOpaque(false);

        //// Adding all of our UI elements to the table for gameplay. ////
        //table.add(statusBox); // Removed as it was in the way of new UI elements.
        table.add(toggleTimerButton);
        table.add(gameTitle);
        table.add(timeBox);
        table.add(newGameButton);
        table.add(menuButton); // Return to main menu
        table.add(menuSureButton); // Confirm return to main menu.
        table.add(showRulesButton);
        table.add(scoreBox);
        table.repaint();
        //// Adding all of our UI elements to the table for gameplay. ////

        if(profileButton.getSelectedIndex() == -1) {
            for(int i = 0; i < profileButton.getItemCount(); i++) {
                if(Player.playerName.equals(profileButton.getItemAt(i))) {
                    try {
                        profileButton.setSelectedItem(Player.playerName);
                    } catch(IndexOutOfBoundsException e) {}
                    i = profileButton.getItemCount();
                }
                else if(i == profileButton.getItemCount() - 1 && profileButton.getSelectedIndex() == -1) {
                    profileButton.addItem(Player.playerName);
                }
            }
        }
    }

    public static void startProgram() // This is the method for our main menu.
    {
        gameLive = false; // To ensure we never do anything that happens in-game.

        updateGameState(false);//this is to show that the game has ended.
        Player.resetPlayerStats();

        if (menuButton.getActionListeners().length > 1) {
            for (int i=0; i<menuButton.getActionListeners().length; i++) {
                menuButton.removeActionListener(menuButton.getActionListeners()[i]);
            }
        }

        if (menuSureButton.getActionListeners().length > 1) {
            for (int i=0; i<menuSureButton.getActionListeners().length; i++) {
                menuSureButton.removeActionListener(menuSureButton.getActionListeners()[i]);
            }
        }
        table.removeAll();
        table.repaint();

        if (selectGameButton.getActionListeners().length < 1) { // This condition is to ensure the same action happens only once per click.
            selectGameButton.addActionListener(new SelectGameListener());
        }
        selectGameButton.setBounds((TABLE_WIDTH/2)-75, (TABLE_HEIGHT)/2-135, 150, 50);

        if (statisticsButton.getActionListeners().length < 1) { // This condition is to ensure the same action happens only once per click.
            statisticsButton.addActionListener(new StatisticsListener());
        }
        statisticsButton.setBounds((TABLE_WIDTH/2)-75, (TABLE_HEIGHT/2)-35, 150, 50);

        if (exitMenuButton.getActionListeners().length < 1) { // This condition is to ensure the same action happens only once per click.
            exitMenuButton.addActionListener(new ExitMenuListener());
        }
        exitMenuButton.setBounds((TABLE_WIDTH/2)-75, (TABLE_HEIGHT/2)+65, 150, 50);

        if(profileButton.getActionListeners().length < 1) {
            profileButton.addActionListener(new ProfileButtonListener());
        }
        profileButton.setBounds(15, 15, 100, 35);
        profileButton.setRenderer(new ComboBoxTitle("Select Profile"));
        profileButton.setSelectedIndex(index);
        table.add(profileButton);

        table.add(selectGameButton);
        table.add(statisticsButton);
        table.add(exitMenuButton);


    }


    public static void setupProfileButton() throws FileNotFoundException{
        String[] returnedNames;
        File usersFile = new File("users");
        if(!usersFile.exists()) {
            usersFile.mkdir();
        }
        returnedNames = usersFile.list();
        profileButton = new JComboBox(returnedNames);
    }

    /**
     * This method updates the file as the program is closed to save the top players.
     * @throws FileNotFoundException
     */

    public static void updateTopScoresFile() throws FileNotFoundException{
        try (PrintWriter out = new PrintWriter("scores")) {
            out.println("Players:");
            for(int i = 0; i < Player.topPlayerScoreList.size(); i++) {
                if(i == Player.topPlayerScoreList.size() - 1) {
                    out.print(Player.topPlayerNameList.get(i) + " " + Player.topPlayerScoreList.get(i) + " " + Player.topPlayerTimeList.get(i) + " " + Player.topPlayerWinList.get(i));
                }
                else {
                    out.println(Player.topPlayerNameList.get(i) + " " + Player.topPlayerScoreList.get(i) + " " + Player.topPlayerTimeList.get(i) + " " + Player.topPlayerWinList.get(i));
                }
            }
        }
    }

    public static void saveGame() throws FileNotFoundException{
        try (PrintWriter out = new PrintWriter("users\\" + Player.getPlayerName())) {
            out.println("FivePiles");
            out.print(Player.displayInfo());

        }
    }

    public static void loadFile(String filePath) throws FileNotFoundException{
        Player.playerNameList.clear();
        Player.playerScoreList.clear();
        Player.playerTimeList.clear();
        Player.playerWinList.clear();
        // These lines are to ensure we start with a clean score list when loading a new player's info.

        int loop = 0;

        File usersFile = new File("users");
        File saveFile;
        if(!usersFile.exists())
        {
            usersFile.mkdir();
        }
        if(filePath.startsWith("users\\")) {
            saveFile = new File(filePath);
        } else {
            saveFile = new File("users\\" + filePath);
        }



        if (saveFile.exists()) {
            Scanner in = new Scanner(saveFile);
            while (in.hasNextLine()) {
                if(loop == 0)
                {
                    String waste = in.next();
                    loop++;
                }

                else {
                    String fileName = in.next();
                    int fileScore = in.nextInt();
                    int fileTime = in.nextInt();
                    int fileWin = in.nextInt();
                    Player.updateLists(fileName, fileScore, fileTime, fileWin);
                }

            }
        }else {
            try {
                saveFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        Image tableTexture = null;

        try { // We try to load our table's texture image.
            tableTexture = ImageIO.read(new File("textures\\TableTexture.png")).getScaledInstance(TABLE_WIDTH, TABLE_HEIGHT, Image.SCALE_SMOOTH);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Container contentPane;

        Player.updateTopPlayers();
        setupProfileButton();

        frame.setSize(TABLE_WIDTH, TABLE_HEIGHT); // The dimensions of our gameplay area.

        table.setLayout(null);

        table.setBackground(new Color(0, 180, 0)); // The color of our "table" if the image doesn't load.
        if (tableTexture != null){
            table = new JLabel(new ImageIcon(tableTexture));
        }

        contentPane = frame.getContentPane();
        contentPane.add(table);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        startProgram();

        Card.loadCardImages();

        frame.setResizable(false); // We don't want the user to be able to resize the window.
        frame.setVisible(true); // We want the window visible.

    }
}