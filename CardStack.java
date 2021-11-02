/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fivepiles;



import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ListIterator;
import java.util.Vector;

import javax.swing.JComponent;

/* This is GUI component with a embedded
 * data structure. This structure is a mixture
 * of a queue and a stack
 */
class CardStack extends JComponent
{
	protected int NUM_CARDS = 52; // Set this to any number lower than 52 for less cards. I recommend 5x + 2 cards.
	protected Vector<Card> v;
	protected boolean playStack = false;
	protected int SPREAD = 28; // Was 18 originally.
	protected int _x = 0;
	protected int _y = 0;

	public boolean isSelected = false;
	public static Image selectedImage = null;

	public CardStack(boolean isDeck)
	{
		int f = 1;
		this.setLayout(null);
		v = new Vector<Card>();
		if (isDeck)
		{
			// set deck position
			for (Card.Suit suit : Card.Suit.values())
			{
				for (Card.Value value : Card.Value.values())
				{
					if (v.size() < NUM_CARDS) {
						v.add(new Card(suit, value));
					}
				}
			}
		} else
		{
			playStack = true;
		}
	}

	public boolean empty()
	{
		if (v.isEmpty())
			return true;
		else
			return false;
	}

	public void putFirst(Card c)
	{
		v.add(0, c);
	}

	public Card getFirst()
	{
		if (!this.empty())
		{
			return v.get(0);
		} else
			return null;
	}

	// analogous to peek()
	public Card getLast()
	{
		if (!this.empty())
		{
			return v.lastElement();
		} else
			return null;
	}

	// queue-like functionality
	public Card popFirst()
	{
		if (!this.empty())
		{
			Card c = this.getFirst();
			v.remove(0);
			return c;
		} else
			return null;

	}

	public void push(Card c)
	{
		v.add(c);
	}

	public Card pop()
	{
		if (!this.empty())
		{
			Card c = v.lastElement();
			v.remove(v.size() - 1);
			return c;
		} else
			return null;
	}

	// shuffle the cards
	public void shuffle()
	{
		Vector<Card> v = new Vector<Card>();
		while (!this.empty())
		{
			v.add(this.pop());
		}
		while (!v.isEmpty())
		{
			Card c = v.elementAt((int) (Math.random() * v.size()));
			this.push(c);
			v.removeElement(c);
		}

	}

	public int showSize()
	{
		return v.size();
	}

	// reverse the order of the stack
	public CardStack reverse()
	{
		Vector<Card> v = new Vector<Card>();
		while (!this.empty())
		{
			v.add(this.pop());
		}
		while (!v.isEmpty())
		{
			Card c = v.firstElement();
			this.push(c);
			v.removeElement(c);
		}
		return this;
	}

	public void makeEmpty()
	{
		while (!this.empty())
		{
			this.popFirst();
		}
	}

	@Override
	public boolean contains(Point p)
	{
		Rectangle rect = new Rectangle(_x, _y, Card.CARD_WIDTH + 10, Card.CARD_HEIGHT * 3);
		return (rect.contains(p));
	}

	public void setXY(int x, int y)
	{
		_x = x;
		_y = y;
		// System.out.println("CardStack SET _x: " + _x + " _y: " + _y);
		setBounds(_x, _y, Card.CARD_WIDTH + 10, Card.CARD_HEIGHT * 3);
	}

	public boolean hasCard(Card c){
		for (int i=0; i<v.size(); i++){
			if (v.get(i).getID().equals(c.getID())){
				return true;
			}
		}

		return false;
	}

	public Point getXY()
	{
		// System.out.println("CardStack GET _x: " + _x + " _y: " + _y);
		return new Point(_x, _y);
	}

	/**
	 * Sets the selected state of the stack.
	 * @param b
	 */
	public void setSelected(boolean b){
		isSelected = b;
	}

	/**
     * Returns the selected state of the stack.
     * @return
     */
	public boolean isSelected(){
        return isSelected;
    }


	/**
	 * Loops through all the CardStacks in playCardStack and if a CardStack is selected, it paints a copy of the top card of that selected CardStack to bottom right of the table.
	 * cs.getFirst gets the first (top) card in a stack.
	 * cs.getFirst().getCardImage() gets the image of the first (top) card in the stack.
	 */
	public void paintSelected(){
		Graphics g = FivePiles.table.getGraphics();
		for(int i=0; i<FivePiles.playCardStack.length; i++){
			if(FivePiles.playCardStack[i].isSelected() && FivePiles.playCardStack[i].getFirst() != null && CardStack.selectedImage != null){
				// paint a copy of the top card of the CardStack to top right of the table. It should be permanent.
				g.drawImage(CardStack.selectedImage, FivePiles.table.getWidth() - CardStack.selectedImage.getWidth(null), 0, FivePiles.table);
				// Ensure that multiples of the same image are not drawn on top of each other.
				g.dispose();


			}
		}
	}

	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if (playStack)
		{
			removeAll();
			ListIterator<Card> iter = v.listIterator();
			Point prev = new Point(); // positioning relative to the container
			Point prevWhereAmI = new Point();// abs positioning on the board

			// To set the image of what is selected
			if (isSelected && this.getFirst() != null){
				CardStack.selectedImage = this.getFirst().getCardImage();
			}
			paintSelected();

			if (iter.hasNext())
			{
				Card c = iter.next();
				// this origin is point(0,0) inside the cardstack container
				prev = new Point();// c.getXY(); // starting deck pos
				if (c != null && !c.moving) {
					add(FivePiles.moveCard(c, prev.x, prev.y));
					// setting x & y position
					c.setWhereAmI(getXY());
				}
				prevWhereAmI = getXY();
			} else
			{
				removeAll();
			}

			for (; iter.hasNext();)
			{
				Card c = iter.next();
				if (c != null && !c.moving) {
					c.setXY(new Point(prev.x, prev.y + SPREAD));
					add(FivePiles.moveCard(c, prev.x, prev.y + SPREAD));
					prev = c.getXY();
					// setting x & y position
					c.setWhereAmI(new Point(prevWhereAmI.x, prevWhereAmI.y + SPREAD));
					prevWhereAmI = c.getWhereAmI();
				}
			}

		}
	}
}// END CardStack