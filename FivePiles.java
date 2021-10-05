package fivepiles;

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
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;

public class FivePiles // Test. Did it work?
{
    // CONSTANTS

    public static final int TABLE_HEIGHT = Card.CARD_HEIGHT * 4; //150*4 = 600
    public static final int TABLE_WIDTH = (Card.CARD_WIDTH * 7) + 100; //100 * 7 = 700
    public static final int NUM_FINAL_DECKS = 1; //was 4 in solitaire
    public static final int NUM_PLAY_DECKS = 7; //five playing piles
    public static final Point DECK_POS = new Point(5, 5);
    public static final Point SHOW_POS = new Point(DECK_POS.x + Card.CARD_WIDTH + 5, DECK_POS.y);
    public static final Point FINAL_POS = new Point(SHOW_POS.x + Card.CARD_WIDTH + 150, DECK_POS.y);
    public static final Point PLAY_POS = new Point(DECK_POS.x, FINAL_POS.y + Card.CARD_HEIGHT + 30);

    // GAMEPLAY STRUCTURES
    private static FinalStack[] final_cards;// Foundation Stacks
    private static CardStack[] playCardStack; // Tableau stacks
    private static final Card newCardPlace = new Card();// waste card spot
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


    private static JButton selectGameButton = new JButton("Select Game");
    private static JButton statisticsButton = new JButton("Statistics");
    private static JButton exitMenuButton = new JButton("Exit");
    private static JButton fivePilesButton = new JButton("Five Piles");

    // TIMER UTILITIES
    private static Timer timer = new Timer();
    private static ScoreClock scoreClock = new ScoreClock();

    // MISC TRACKING VARIABLES
    private static boolean timeRunning = false;// timer running?
    private static int score = 0;// keep track of the score
    private static int time = 0;// keep track of seconds elapsed

    // moves a card to abs location within a component
    protected static Card moveCard(Card c, int x, int y) {
        c.setBounds(new Rectangle(new Point(x, y), new Dimension(Card.CARD_WIDTH + 10, Card.CARD_HEIGHT + 10)));
        c.setXY(new Point(x, y));
        return c;
    }

    // add/subtract points based on gameplay actions
    protected static void setScore(int deltaScore) {
        FivePiles.score += deltaScore;
        String newScore = "Score: " + FivePiles.score;
        scoreBox.setText(newScore);
        scoreBox.repaint();
    }

    // GAME TIMER UTILITIES
    protected static void updateTimer() {
        FivePiles.time += 1;
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
            table.remove(selectGameButton);
            table.remove(statisticsButton);
            table.remove(exitMenuButton);
            table.repaint();

            if (fivePilesButton.getActionListeners().length < 1) { // This condition is to ensure the same action happens only once per click.
                fivePilesButton.addActionListener(new NewGameListener());
            }
            fivePilesButton.setBounds((TABLE_WIDTH/2)-75, (TABLE_HEIGHT)/2-135, 150, 50);

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

    private static class openMenu implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println(menuSureButton.isVisible());
            System.out.println(menuButton.getActionListeners().length);
            if (menuSureButton.isVisible()){
                menuSureButton.hide();
            }else {
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

            if (menuSureButton.isVisible()) {
                menuSureButton.hide();
            }

        }
        @Override
        public void actionPerformed(ActionEvent e) {
            menuSureButton.hide();
            startProgram();
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
            JDialog ruleFrame = new JDialog(frame, true);
            ruleFrame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            ruleFrame.setSize(TABLE_HEIGHT, TABLE_WIDTH);
            JScrollPane scroll;
            JEditorPane rulesTextPane = new JEditorPane("text/html", "");
            rulesTextPane.setEditable(false);
            String rulesText = "<b>File Piles Solitaire</b>"
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
            rulesTextPane.setText(rulesText);
            ruleFrame.add(scroll = new JScrollPane(rulesTextPane));

            ruleFrame.setVisible(true);
        }
    }

    /*
     * This class handles all of the logic of moving the Card components as well
     * as the game logic. This determines where Cards can be moved according to
     * the rules of Klondike solitiaire
     */
    private static class CardMovementManager extends MouseAdapter {

        private Card prevCard = null;// tracking card for waste stack
        private Card movedCard = null;// card moved from waste stack
        private boolean putBackOnDeck = true;// used for waste card recycling
        private boolean checkForWin = false;// should we check if game is over?
        private boolean gameOver = true;// easier to negate this than affirm it
        private Point start = null;// where mouse was clicked
        private Point stop = null;// where mouse was released
        private Card card = null; // card to be moved
        // used for moving single cards
        private CardStack source = null;
        private CardStack dest = null;
        // used for moving a stack of cards
        private CardStack transferStack = new CardStack(false);

        // Testing
        CardStack ChosenCards = new CardStack(false);
        // Testing


        @Override
        public void mousePressed(MouseEvent e) {
            start = e.getPoint();
            boolean stopSearch = false;
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
            if (card != null) {
                int value = card.getNumericalValue(); // Gets the value of the card clicked on.
                //System.out.println("Card value of " + card.getValue() + ": " + value);
                if (ChosenCards.getFirst() != null) { // If the first card in our temporary pile isn't null, it means we selected 2 cards and should combine their values.
                    value += ChosenCards.getFirst().getNumericalValue(); // Combine the values of our two selected cards.
                    System.out.println("Added value: " + value);
                }

                Card old = ChosenCards.getFirst(); // Store the old card in a variable
                ChosenCards.putFirst(card); // Put the clicked card on top of the temporary card stack.

                if (value == 13) { // If the combined value of 2 selected cards is 13.
                    System.out.println("20 points added for matching to 13!");
                    score += 20; // Add 20 points. This is just temporary and can be changed.
                    for (int x = 0; x < NUM_PLAY_DECKS; x++) { // Loop through all existing play decks.
                        if (playCardStack[x].getFirst() != null) { // If the first card in the play deck exists
                            if (playCardStack[x].getFirst().getXY().equals(card.getXY()) && playCardStack[x].getFirst().getValue().equals(card.getValue())) { // Check if it is the same card as the one we clicked on.
                                System.out.println("Popped and removed/repainted.");
                                Card c = playCardStack[x].popFirst(); // We pop the card from the play deck, since it added to 13.
                                if (c != null) {
                                    table.remove(FivePiles.moveCard(c, SHOW_POS.x, SHOW_POS.y)); // We remove the card from the table.

                                    c.repaint(); // Repaint to visualize changes.
                                }
                                table.repaint(); // Repaint to visualize changes.
                            }
                        }
                        if (old != null && playCardStack[x].getFirst() != null) { // If the first card in the play deck exists and the previously clicked card exists
                            if (playCardStack[x].getFirst().getXY().equals(old.getXY()) && playCardStack[x].getFirst().getValue().equals(old.getValue())) { // Check if it is the same card as the one we PREVIOUSLY clicked on.
                                System.out.println("Popped and removed/repainted.");
                                Card c = playCardStack[x].popFirst(); // We pop the card from the play deck, since it added to 13.
                                if (c != null) {
                                    table.remove(FivePiles.moveCard(c, SHOW_POS.x, SHOW_POS.y)); // We remove the card from the table.

                                    c.repaint(); // Repaint to visualize changes.
                                }
                                table.repaint(); // Repaint to visualize changes.
                            }
                        }


                        ChosenCards = new CardStack(false);

                    }

                }
            }
            // SHOW (WASTE) CARD OPERATIONS
            // display new show card
            // dealing from the deck: still need to handle last two cards here

            if (newCardButton.contains(start) && deck.showSize() > 0) {

                ChosenCards = new CardStack(false); // Added this to ensure we can't pick between cards from different layers.
                if (putBackOnDeck && prevCard != null) {
                    System.out.println("Putting back on show stack: ");
                    prevCard.getValue();
                    prevCard.getSuit();
                    deck.putFirst(prevCard);
                }

                if (prevCard != null) {
                    table.remove(prevCard);
                }
                for (int x = 0; x < NUM_PLAY_DECKS-2; x++) {
                    if (deck.showSize() > 2) { // Added this condition to account for the deck running out / last 2 cards.
                        Card c = deck.pop().setFaceup();
                        if (c != null) {
                            playCardStack[x].putFirst(c);
                            table.add(FivePiles.moveCard(c, SHOW_POS.x, SHOW_POS.y));
                            c.repaint();
                        }
                        table.repaint();
                    }else { // This part is to handle the last 2 cards.
                        Card c1 = deck.pop(); // We pop the first card.
                        Card c2 = deck.pop(); // We pop the second card.


                        if (c1 != null && c2 != null){ //
                            c1 = c1.setFaceup(); // Set them faceup.
                            c2 = c2.setFaceup(); // Set them faceup.
                            playCardStack[NUM_PLAY_DECKS-2].putFirst(c1); // Put first card in the first extra pile.
                            table.add(FivePiles.moveCard(c1, SHOW_POS.x, SHOW_POS.y));
                            playCardStack[NUM_PLAY_DECKS-1].putFirst(c2); // Put the second card in the second extra pile.
                            table.add(FivePiles.moveCard(c2, SHOW_POS.x, SHOW_POS.y));
                            c1.repaint();
                            c2.repaint();
                        }
                        table.repaint();
                    }
                }
                deck.showSize();

            }

            if (newCardPlace.contains(start) && prevCard != null) {
                movedCard = prevCard;
            }

            // FINAL (FOUNDATION) CARD OPERATIONS
            for (int x = 0; x < NUM_FINAL_DECKS; x++) {

                if (final_cards[x].contains(start)) {
                    source = final_cards[x];
                    card = source.getLast();
                    transferStack.putFirst(card);
                    break;
                }
            }
            putBackOnDeck = true;

        }

        @Override
        public void mouseReleased(MouseEvent e) {
            stop = e.getPoint();
            // used for status bar updates
            boolean validMoveMade = false;

            // PLAY STACK OPERATIONS
            if (card != null && source != null) { // Moving from PLAY TO PLAY
                for (int x = 0; x < NUM_PLAY_DECKS; x++) {
                    dest = playCardStack[x];
                    // MOVING TO POPULATED STACK
                    if (card.getFaceStatus() == true && dest.contains(stop) && source != dest && !dest.empty()
                            && transferStack.showSize() == 1) {
                        Card c = source.popFirst();

                        if (c != null) {
                            c.repaint();
                        }

                        // if playstack, turn next card up
                        if (source.getFirst() != null) {
                            Card temp = source.getFirst().setFaceup();
                            temp.repaint();
                            source.repaint();
                        }

                        dest.setXY(dest.getXY().x, dest.getXY().y);
                        dest.putFirst(c);

                        dest.repaint();

                        table.repaint();

                        System.out.print("Destination ");
                        dest.showSize();

                        validMoveMade = true;
                        break;
                    } else if (dest.empty() && card.getValue() == Card.Value.KING && transferStack.showSize() == 1) {// MOVING TO EMPTY STACK, ONLY KING ALLOWED
                        Card c = source.popFirst();

                        if (c != null) {
                            c.repaint();
                        }

                        // if playstack, turn next card up
                        if (source.getFirst() != null) {
                            Card temp = source.getFirst().setFaceup();
                            temp.repaint();
                            source.repaint();
                        }

                        dest.setXY(dest.getXY().x, dest.getXY().y);
                        dest.putFirst(c);

                        dest.repaint();

                        table.repaint();

                        System.out.print("Destination ");
                        dest.showSize();
                        setScore(5);
                        validMoveMade = true;
                        break;
                    }
                }

            }// end cycle through play decks

            // SHOWING STATUS MESSAGE IF MOVE INVALID
            if (!validMoveMade && dest != null && card != null) {
                statusBox.setText("That Is Not A Valid Move");
            }
            // CHECKING FOR WIN
            if (checkForWin) {
                boolean gameNotOver = false;
                int emptyPiles = 0;
                // cycle through play decks, if they are all empty, then you beat five piles.
                for (int x = 0; x < NUM_PLAY_DECKS; x++) {

                    if (playCardStack[x].showSize() == 0) {
                        emptyPiles += 1;
                        if (emptyPiles < 5){
                            gameNotOver = true;
                            break;
                        }
                    }
                }
                if (!gameNotOver) {
                    gameOver = true;
                }
            }

            if (checkForWin && gameOver) {
                JOptionPane.showMessageDialog(table, "Congratulations! You've Won!");
                statusBox.setText("Game Over!");
            }
            // RESET VARIABLES FOR NEXT EVENT
            start = null;
            stop = null;
            source = null;
            dest = null;
            card = null;
            checkForWin = false;
            gameOver = false;
        }// end mousePressed()
    }//end card movement manager class


    private static void playNewGame() {

        if (table.getMouseListeners().length < 1) {
            table.addMouseListener(new CardMovementManager());
        }
        if (table.getMouseMotionListeners().length < 1) {
            table.addMouseMotionListener(new CardMovementManager());
        }

        deck = new CardStack(true); // deal 52 cards
        deck.shuffle();
        table.removeAll();
        // reset stacks if user starts a new game in the middle of one
        if (playCardStack != null && final_cards != null) {
            for (int x = 0; x < NUM_PLAY_DECKS; x++) {
                playCardStack[x].makeEmpty();
            }
            for (int x = 0; x < NUM_FINAL_DECKS; x++) {
                final_cards[x].makeEmpty();
            }
        }
        // initialize & place final (foundation) decks/stacks
        final_cards = new FinalStack[NUM_FINAL_DECKS];
        for (int x = 0; x < NUM_FINAL_DECKS; x++) {
            final_cards[x] = new FinalStack();

            final_cards[x].setXY((FINAL_POS.x + (x * Card.CARD_WIDTH)) + 10, FINAL_POS.y);
            table.add(final_cards[x]);

        }
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
        for (int x = 0; x < NUM_PLAY_DECKS; x++) {
            int hld = 0;
            Card c = deck.pop().setFaceup();
            if (x < NUM_PLAY_DECKS-2) {
                playCardStack[x].putFirst(c);
            }

//			for (int y = x + 1; y < NUM_PLAY_DECKS; y++)
//			{
//				playCardStack[y].putFirst(c = deck.pop());
//			}
        }
        // reset time
        time = 0;

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
        newGameButton.setBounds(0, TABLE_HEIGHT - 70, 120, 30);

        if (showRulesButton.getActionListeners().length < 1) { // This condition is to ensure the same action happens only once per click.
            showRulesButton.addActionListener(new ShowRulesListener());
        }
        showRulesButton.setBounds(120, TABLE_HEIGHT - 70, 120, 30);

        gameTitle.setText(" ");//text
        gameTitle.setEditable(false);
        gameTitle.setOpaque(false);
        gameTitle.setBounds(245, 20, 100, 100);

        scoreBox.setBounds(240, TABLE_HEIGHT - 70, 120, 30);
        scoreBox.setText("Score: 0");
        scoreBox.setEditable(false);
        scoreBox.setOpaque(false);

        timeBox.setBounds(360, TABLE_HEIGHT - 70, 120, 30);
        timeBox.setText("Seconds: 0");
        timeBox.setEditable(false);
        timeBox.setOpaque(false);

        startTimer();

        toggleTimerButton.setBounds(480, TABLE_HEIGHT - 70, 125, 30);
        if (toggleTimerButton.getActionListeners().length < 1) { // This condition is to ensure the same action happens only once per click.
            toggleTimerButton.addActionListener(new ToggleTimerListener());
        }

        statusBox.setBounds(605, TABLE_HEIGHT - 70, 180, 30);
        statusBox.setEditable(false);
        statusBox.setOpaque(false);

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
    }

    public static void startProgram()
    {

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
    public static void main(String[] args) {

        Container contentPane;

        frame.setSize(TABLE_WIDTH, TABLE_HEIGHT);

        table.setLayout(null);
        table.setBackground(new Color(0, 180, 0));

        contentPane = frame.getContentPane();
        contentPane.add(table);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        startProgram();

        //playNewGame();

//        table.addMouseListener(new CardMovementManager());
//        table.addMouseMotionListener(new CardMovementManager());

        frame.setResizable(false);
        frame.setVisible(true);

    }
}