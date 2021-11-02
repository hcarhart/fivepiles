/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fivepiles;
//goodbye

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

import javax.swing.*;

class Card extends JPanel
{
	public static enum Value
	{
		ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING
	}

	public static enum Suit
	{
		SPADES, CLUBS, DIAMONDS, HEARTS
	}

	protected static Image[][] cardImage = new Image[4][13]; // What you see when the card is face up.
	protected static Image cardBackImage; // What you see when the card is face down.

	private Suit _suit;

	private Value _value;

	private Boolean _faceup;

	private int _id;

	private Point _location; // location relative to container

	private Point whereAmI; // used to create abs postion rectangle for contains
	// functions

	private int x; // used for relative positioning within CardStack Container
	private int y;

	private final int x_offset = 10;
	private final int y_offset = 20;
	private final int new_x_offset = x_offset + (CARD_WIDTH - 30);
	final static public int CARD_HEIGHT = 150;

	final static public int CARD_WIDTH = 100;

	final static public int CORNER_ANGLE = 25;

	public boolean moving;

	Card(Suit suit, Value value)
	{
		_suit = suit;
		_value = value;
		_faceup = false;
		_id = (int)(Math.random()*10000000f);
		_location = new Point();
		x = 0;
		y = 0;
		_location.x = x;
		_location.y = y;
		whereAmI = new Point();
		moving = false;
	}

	Card()
	{
		_suit = Card.Suit.CLUBS;
		_value = Card.Value.ACE;
		_faceup = false;
		_id = (int)(Math.random()*100000f);
		_location = new Point();
		x = 0;
		y = 0;
		_location.x = x;
		_location.y = y;
		whereAmI = new Point();
	}

	public static void loadCardImages() {
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 13; j++) {
				int math = (i * 13) + j;
				try {
					cardImage[i][j] = ImageIO.read(new File("textures\\" + Integer.toString(math) + ".png"));
				} catch (IOException ex) {}
			}
		}
	}

	public Image getCardImage(){
		// Next we make a switch statement such that a variable s is assigned 0 if it is a heart, 1 if it is a spade, 2 if it is a diamond, and 3 if it is a club.
		// This is done to make the card image array easier to read.
		int s = 0;
		switch (_suit) {
			case SPADES:
				s = 1;
				break;
			case CLUBS:
				s = 3;
				break;
			case DIAMONDS:
				s = 2;
				break;
			case HEARTS:
				s = 0;
				break;
		}

		return cardImage[s][getNumericalValue()-1];
	}

	public Suit getSuit()
	{
		return _suit;
	}

	/**
	 * Returns the card's ID. The cards ID is a random number generated when the card object is constructed.
	 * @return Returns the card's ID.
	 */
	public String getID(){
		return Integer.toString(_id);
	}

	public Value getValue()
	{
		return _value;
	}

	/**
	 * This method is meant to be used to see if the numerical values of two cards add up to 13, to achieve a score.
	 * @param value Possible choices are ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING.
	 * @return Returns a numerical value corresponding to the Value provided.
	 */
	private int numericalFromValue(Value value){
		switch (value) {
			case ACE:
				return 1;
			case TWO:
				return 2;
			case THREE:
				return 3;
			case FOUR:
				return 4;
			case FIVE:
				return 5;
			case SIX:
				return 6;
			case SEVEN:
				return 7;
			case EIGHT:
				return 8;
			case NINE:
				return 9;
			case TEN:
				return 10;
			case JACK:
				return 11;
			case QUEEN:
				return 12;
			case KING:
				return 13;
			default:
				return 0;
		}
	}

	/**
	 * This method is necessary to calculate whether the value of two cards added, results in 13.
	 * @return Returns the NUMERICAL value of the Card object.
	 */
	public int getNumericalValue(){
		return numericalFromValue(this.getValue());
	}

	public void setWhereAmI(Point p)
	{
		whereAmI = p;
	}

	public Point getWhereAmI()
	{
		return whereAmI;
	}

	public Point getXY()
	{
		return new Point(x, y);
	}

	public Boolean getFaceStatus()
	{
		return _faceup;
	}

	public void setXY(Point p)
	{
		x = p.x;
		y = p.y;

	}

	public void setSuit(Suit suit)
	{
		_suit = suit;
	}

	public void setValue(Value value)
	{
		_value = value;
	}

	public boolean isKing(){
		return this.getNumericalValue() == 13;
	}

	public Card setFaceup()
	{
		_faceup = true;
		return this;
	}

	public Card setFacedown()
	{
		_faceup = false;
		return this;
	}

	@Override
	public boolean contains(Point p)
	{
		Rectangle rect = new Rectangle(whereAmI.x, whereAmI.y, Card.CARD_WIDTH, Card.CARD_HEIGHT);
		return (rect.contains(p));
	}

	// No longer needed
	private void drawSuit(Graphics2D g, String suit, Color color)
	{
		g.setFont(new Font("Aerial", Font.PLAIN, 30 ));
		g.setColor(color);
		g.drawString(suit, _location.x + x_offset-6, _location.y + y_offset + 28);
		g.drawString(suit, _location.x + new_x_offset-6, _location.y + CARD_HEIGHT - 5 - 23);
	}

	// No longer needed.
	private void drawValue(Graphics2D g, String value)
	{
		g.setFont(new Font("Algerian", Font.PLAIN, 25 )); // This is only necessary until we add card images to replace these.
		g.drawString(value, _location.x + ((this.getNumericalValue() == 10) ? x_offset-10 : x_offset-5), _location.y + y_offset);
		g.drawString(value, _location.x + ((this.getNumericalValue() == 10) ? new_x_offset-12 : new_x_offset), _location.y + y_offset + CARD_HEIGHT - 25);
	}

	@Override
	public void paintComponent(Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g;
		RoundRectangle2D rect2 = new RoundRectangle2D.Double(_location.x, _location.y, CARD_WIDTH, CARD_HEIGHT,
				CORNER_ANGLE, CORNER_ANGLE);
		g2d.setColor(Color.WHITE);
		g2d.fill(rect2);
		g2d.setColor(Color.black);
		g2d.draw(rect2);
		//g2d.drawImage(cardImage, _location.x, _location.y, this);

		// DRAW THE CARD SUIT AND VALUE IF FACEUP
		if (_faceup)
		{

			switch (_suit)
			{
				case HEARTS:
					g2d.drawImage(cardImage[0][getNumericalValue()-1], _location.x, _location.y, this);
					break;
				case SPADES:
					g2d.drawImage(cardImage[1][getNumericalValue()-1], _location.x, _location.y, this);
					break;
				case DIAMONDS:
					g2d.drawImage(cardImage[2][getNumericalValue()-1], _location.x, _location.y, this);
					break;
				case CLUBS:
					g2d.drawImage(cardImage[3][getNumericalValue()-1], _location.x, _location.y, this);
					break;
			}
			int new_x_offset = x_offset + (CARD_WIDTH - 30);

		} else
		{
			// DRAW THE BACK OF THE CARD IF FACEDOWN
			RoundRectangle2D rect = new RoundRectangle2D.Double(_location.x, _location.y, CARD_WIDTH, CARD_HEIGHT,
					CORNER_ANGLE, CORNER_ANGLE);
			g2d.setColor(Color.LIGHT_GRAY);
			g2d.fill(rect);
			g2d.setColor(Color.black);
			g2d.draw(rect);
			g2d.drawImage(cardBackImage, _location.x, _location.y, this); // Include this line for a card back image.
		}

	}

}// END Card