package fivepiles;

///////////////////////////////////////// Imports /////////////////////////////////////////
import java.awt.event.WindowEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.*;
///////////////////////////////////////// Imports /////////////////////////////////////////

public class FivePiles
{


    // CONSTANTS
    public static final int TABLE_HEIGHT = Card.CARD_HEIGHT * 4; //150*4 = 600
    public static final int TABLE_WIDTH = (Card.CARD_WIDTH * 7) + 100; //100 * 7 = 700
    public static final int NUM_PLAY_DECKS = 7; // 5 playing piles, 2 extra for the last 2 cards.
    public static final Point DECK_POS = new Point(5, 5);
    public static final Point SHOW_POS = new Point(DECK_POS.x + Card.CARD_WIDTH + 5, DECK_POS.y);
    public static final Point FINAL_POS = new Point(SHOW_POS.x + Card.CARD_WIDTH + 150, DECK_POS.y);
    public static final Point PLAY_POS = new Point(DECK_POS.x, FINAL_POS.y + Card.CARD_HEIGHT + 30);

    // GAMEPLAY STRUCTURES
    private static CardStack[] playCardStack; // Tableau stacks
    private static CardStack deck; // populated with standard 52 card deck

    // GUI COMPONENTS (top level)
    private static final JFrame frame = new JFrame("Five Piles");
    protected static final JPanel table = new JPanel();
    // other components
    private static JEditorPane gameTitle = new JEditorPane("text/html", ""); //
    private static JButton showRulesButton = new JButton("Show Rules");
    private static JButton newGameButton = new JButton("New Game");
    private static JButton menuButton = new JButton("Menu"); // Returns player to main platform/menu.
    private static JButton menuSureButton = new JButton("Are you sure? You will lose any progress.");
    private static JButton toggleTimerButton = new JButton("Pause Timer");
    private static JTextField scoreBox = new JTextField();// displays the score
    private static JTextField timeBox = new JTextField();// displays the time
    private static JTextField statusBox = new JTextField();// status messages
    private static final Card newCardButton = new Card();// reveal waste card

    private static String inputName = null; //for validating playerName

    private static JButton selectGameButton = new JButton("Select Game");
    private static JButton statisticsButton = new JButton("Statistics");
    private static JButton exitMenuButton = new JButton("Exit");
    private static JButton fivePilesButton = new JButton("Five Piles");

    // TIMER UTILITIES
    private static Timer timer = new Timer();
    private static ScoreClock scoreClock = new ScoreClock();

    // MISC TRACKING VARIABLES
    private static boolean timeRunning = false;// timer running?
    private static boolean gameLive = false; //is the game currently running?
    private static int score = 0;// keep track of the score
    private static int time = 0;// keep track of seconds elapsed
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
                        System.out.println("Is top");
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

        public Player(){


            FivePiles.inputName = playerName;
            FivePiles.score = playerScore;
            FivePiles.time = playerTime;
        }

        public static String getPlayerName() {
            return playerName;
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
            return playerScoreList.size();
        }

        public static void setPlayerName(String inputName) {
            playerName = FivePiles.inputName;

            try {
                loadFile(playerName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
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

        public static void resetPlayer(){
            playerName = null;
            playerScore = 0;
            playerTime = 0;
        }

        public static String displayInfo(){
            String display = "";
            System.out.println(playerNameList.size());
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
        scoreClock = new ScoreClock();
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

    private static class ScoreClock extends TimerTask {

        @Override
        public void run() {
            updateTimer();
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
        public void actionPerformed(ActionEvent ae) {

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
            System.out.println(menuSureButton.isVisible());
            System.out.println(menuButton.getActionListeners().length);
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
            startProgram(); // And startProgram() which returns us to the main menu.
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

    private static class ShowRulesListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            JDialog ruleFrame = new JDialog(frame, true); // Create a new dialog box for our frame.
            ruleFrame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Set what happens when the window closes.
            ruleFrame.setSize(TABLE_HEIGHT, TABLE_WIDTH); // Set the dimensions of the dialog box.
            JScrollPane scroll;
            JEditorPane rulesTextPane = new JEditorPane("text/html", ""); // The actual text/content.
            rulesTextPane.setEditable(false); // Make the text not editable.
            String rulesText = "<b>File Piles Solitaire</b>" // The rules for Five Piles.
                    + "<br>(From bvssolitaire.com/rules/five-piles.htm) 1 deck. Easy. No redeal.<br> "+
                    "" +
                    "<br>Five Piles Solitaire uses one deck (52 cards). You have 5 tableau piles.\n<br>" +
                    "" +
                    "<br>The object of the game:\n" +
                    "<br>To discard pairs of cards whose ranks add up to 13.\n <br>" +
                    "<br>Here is a list of valid pairs:" +
                    "Queen-Ace, Jack-Two, 10-3, 9-4, etc." +
                    "<br>Kings are discarded singularly, To discard a King, just click it.\n<br>" +
                    "<br>The rules:\n" +
                    "<br>Only the top card of each tableau pile is available for play on the foundations.\n<br>" +
                    "\n" +
                    "<br>When you have made all the moves initially available, click on the stock pile to deal one card on each tableau pile. You cannot move cards from one tableau pile to another. The last 2 cards in the stock are dealt separately from the tableau and can be discarded in a pair with cards from any of the 5 tableau piles.\n<br>" +
                    "\n" +
                    "<br>Wins are rare.\n<br>" +
                    "\n" +
                    "<br>There is no redeal.<br>";
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
                        card = c;
                        stopSearch = true;
                        System.out.println("Transfer Size: " + transferStack.showSize());
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
                    System.out.println("Added value: " + value);
                }

                Card old = null; // Initialize a new variable to hold the card that we selected before.

                if (!card.isKing()){ // If the card we clicked ISN'T a king. This method is custom and doesn't exist for other types.
                    System.out.println("Not king");
                    old = ChosenCards.getFirst(); // Store the old card in a variable
                }
                ChosenCards.putFirst(card); // Put the clicked card on top of the temporary card stack.

                System.out.println(isTop(card));

                if (isValidPair(card, old) || card.isKing() && isTop(card)) { // If the combined value of 2 selected cards is 13, a king, and is on top.
                    System.out.println("20 points added for matching to 13!");
                    score += 20; // Add 20 points. This is just temporary and can be changed.
                    for (int x = 0; x < NUM_PLAY_DECKS; x++) { // Loop through all existing play decks.
                        if (playCardStack[x].getFirst() != null) { // If the first card in the play deck exists
                            if (playCardStack[x].getFirst().getXY().equals(card.getXY()) && playCardStack[x].getFirst().getID().equals(card.getID())) { // Check if it is the same card as the one we clicked on. Basically, if the position (xy) and ID are the same, it's the same card.
                                System.out.println("Popped and removed/repainted.");

                                Card c = playCardStack[x].popFirst(); // We pop the card from the play deck, since it added to 13.

                                if (c != null) {
                                    table.remove(FivePiles.moveCard(c, SHOW_POS.x, SHOW_POS.y)); // We remove the card from the table.
                                    System.out.println("Removed: " + card.getID());
                                    c.repaint(); // Repaint to visualize changes.
                                }
                                table.repaint(); // Repaint to visualize changes.
                            }
                        }
                        if (old != null && playCardStack[x].getFirst() != null) { // If the first card in the play deck exists and the previously clicked card exists
                            if (playCardStack[x].getFirst().getXY().equals(old.getXY()) && playCardStack[x].getFirst().getID().equals(old.getID())) { // Check if it is the same card as the one we PREVIOUSLY clicked on.
                                System.out.println("Popped and removed/repainted.");
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
            if (newCardButton.contains(start) && deck.showSize() > 0) { // If the mouse is within the bounds of newCardButton and the deck has more than 0 cards.

                ChosenCards = new CardStack(false); // Added this to ensure we can't pick between cards from different layers.


                for (int x = 0; x < NUM_PLAY_DECKS-2; x++) { // We want to add new cards to our 5 piles, so we for loop through the total number of play decks.
                    if (deck.showSize() > 2) { // Added this condition to account for the deck running out / last 2 cards.
                        Card c = deck.pop().setFaceup(); // We get our card from the deck and set it faceup.
                        if (c != null) { // We make sure the "card" we just got actually exists.
                            playCardStack[x].putFirst(c); // We put this newly obtained card into the pile we're iterating over.
                            table.add(FivePiles.moveCard(c, SHOW_POS.x, SHOW_POS.y)); // We add the card visual to the table.
                            c.repaint(); // We repaint our card so it shows in the card stack (pile).
                        }
                        table.repaint(); // We repaint the table now so it shows the new card stack (pile).
                    }else { // This part is to handle the last 2 cards. We do this if we have only 2 cards left in the deck.
                        Card c1 = deck.pop(); // We pop the first card.
                        Card c2 = deck.pop(); // We pop the second card.


                        if (c1 != null && c2 != null){ // We make sure the cards we popped actually exist.
                            c1 = c1.setFaceup(); // Set them faceup.
                            c2 = c2.setFaceup(); // Set them faceup.
                            playCardStack[NUM_PLAY_DECKS-2].putFirst(c1); // Put first card in the first extra pile.
                            table.add(FivePiles.moveCard(c1, SHOW_POS.x, SHOW_POS.y)); // We add the card 1's (c1) visual to our table.
                            playCardStack[NUM_PLAY_DECKS-1].putFirst(c2); // Put the second card in the second extra pile.
                            table.add(FivePiles.moveCard(c2, SHOW_POS.x, SHOW_POS.y)); // We add the card 2's (c2) visual to our table
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
            int result;

            table.repaint(); // Repaint table to refresh visual elements.

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
                System.out.println("Empty piles: " + emptyPiles);
                if (emptyPiles == 7){
                    gameOver = true;
                }
            }

            if (checkForWin && gameOver) { // If we checked for win and gameOver is true then we won!
                toggleTimer(); // Pause the timer since we won.
                JOptionPane.showMessageDialog(table, "Congratulations! You've Won!"); // Shows a message with this text.
                statusBox.setText("Game Over!"); // Updates our statusBox (but I don't think the GUI for statusBox is visible.)
                Player.setPlayerScore(score);//grab score and time and assign to player
                Player.setPlayerTime(time);
                Player.setPlayerWin(1);
                Player.updateLists(Player.getPlayerName(), Player.getPlayerScore(), Player.getPlayerTime(), Player.getPlayerWin());
                try
                {
                    saveGame();
                } catch (FileNotFoundException ex)
                {
                    //System.out.println("Something went wrong.")
                }
                updateGameState(false);//this is to show that the game has ended.
            }

            else if(deck.empty() && gameLive) // Since we didn't win, we check if we lost instead. Firstly, we ensure the deck is empty and the game is actually running.
            {

                for(int i = 0; i < NUM_PLAY_DECKS; i++) // For every pile we have.
                {
                    for(int j = i + 1; j < NUM_PLAY_DECKS; j++) // We loop through all other piles.
                    {
                        if(i != NUM_PLAY_DECKS - 1)
                        {
                        } else {
                            j = 0;
                        }
                        // If any card adds up to 13, we have more moves.
                        if(!playCardStack[i].empty() && !playCardStack[j].empty() && playCardStack[i].getFirst().getNumericalValue() + playCardStack[j].getFirst().getNumericalValue() == 13 || !playCardStack[i].empty() && !playCardStack[j].empty() && playCardStack[i].getFirst().getNumericalValue() == 13)
                        {
                            i = NUM_PLAY_DECKS;
                            j = NUM_PLAY_DECKS;
                        }

                    }

                    if(i == NUM_PLAY_DECKS - 2)
                    {
                        toggleTimer(); // Since we lost, we toggle timer.
                        System.out.println("Game Over!");
                        updateGameState(false); //this is to show that the game has ended.
                        Player.setPlayerScore(score);//grab score and time and assign to player
                        Player.setPlayerTime(time);
                        Player.setPlayerWin(0);
                        Player.updateLists(Player.getPlayerName(), Player.getPlayerScore(), Player.getPlayerTime(), Player.getPlayerWin());
                        try
                        {
                            saveGame();
                        } catch (FileNotFoundException ex)
                        {

                        }
                        result = JOptionPane.showOptionDialog(table, "You Lost.", "Game State", 2, 1, null, options, null); // Show a message saying you lost.
                        statusBox.setText("Game Over!"); // Put in the status box you lost.
                        System.out.println("Player score and time for "+ Player.getPlayerName()+ ": "+ Player.getPlayerScore() +" points in "+  Player.getPlayerTime() + " seconds");
                        System.out.println("result: " + result); // Print the result of the options from our optionsDialog.


                        switch(result) // Switch statement to go to the correct option. It depends on the result.
                        {
                            case 0: playNewGame(); // If result = 0, we playNewGame() meaning user pressed new game.
                                Player.resetPlayer();
                                break;
                            case 1: startProgram(); // If result = 1, we startProgram() meaning user pressed main menu.
                                break;
                            case 2: // If result = 2, the user pressed to exit game.
                                break;
                            default:
                                break;
                        }
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
        if(Player.playerName != null)
        {
            validateName = false;
        }
        while(validateName){
            inputName = (String)JOptionPane.showInputDialog("Enter player name: ");
            if(inputName.isEmpty()){
                JOptionPane.showMessageDialog(frame, "Please enter name");
            }
            else{
                validateName = false;
            }
        }
        if (Player.playerName != inputName) {
            Player.setPlayerName(inputName);
            System.out.println("Player: " + Player.getPlayerName());
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

    }

    public static void startProgram() // This is the method for our main menu.
    {


        updateGameState(false);//this is to show that the game has ended.

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


        table.add(selectGameButton);
        table.add(statisticsButton);
        table.add(exitMenuButton);


    }

    public static void saveGame() throws FileNotFoundException{
        try (PrintWriter out = new PrintWriter("users\\" + Player.getPlayerName())) {
            out.println("FivePiles");
            out.print(Player.displayInfo());

        }
    }

    public static void loadFile(String filePath) throws FileNotFoundException{

        int loop = 0;

        File usersFile = new File("users");
        if(!usersFile.exists())
        {
            usersFile.mkdir();
        }
        File saveFile = new File("users\\" + filePath);


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


        Container contentPane;

        frame.setSize(TABLE_WIDTH, TABLE_HEIGHT); // The dimensions of our gameplay area.

        table.setLayout(null);
        table.setBackground(new Color(0, 180, 0)); // The color of our "table."

        contentPane = frame.getContentPane();
        contentPane.add(table);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        startProgram();

        frame.setResizable(false); // We don't want the user to be able to resize the window.
        frame.setVisible(true); // We want the window visible.

    }
}