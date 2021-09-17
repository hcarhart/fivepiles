/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fivepiles;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.RoundRectangle2D;
/*
 * Adapts the CardStack to be used as the final
* (foundation) stack
 */
class FinalStack extends CardStack
{
	public FinalStack()
	{
		super(false);
	}

	@Override
	public void setXY(int x, int y)
	{
		_x = x;
		_y = y;
		setBounds(_x, _y, Card.CARD_WIDTH + 10, Card.CARD_HEIGHT + 10);
	}

	@Override
	public boolean contains(Point p)
	{
		Rectangle rect = new Rectangle(_x, _y, Card.CARD_WIDTH + 10, Card.CARD_HEIGHT + 10);
		return (rect.contains(p));
	}

	/*
	 * We draw this stack one card on top of the other
	 */
	@Override
	protected void paintComponent(Graphics g)
	{
		removeAll();
		if (!empty())
		{
			add(FivePiles.moveCard(this.getLast(), 1, 1));
		} else
		{
			// draw back of card if empty
			Graphics2D g2d = (Graphics2D) g;
			RoundRectangle2D rect = new RoundRectangle2D.Double(0, 0, Card.CARD_WIDTH, Card.CARD_HEIGHT,
					Card.CORNER_ANGLE, Card.CORNER_ANGLE);
			g2d.setColor(Color.LIGHT_GRAY);
			g2d.fill(rect);
			g2d.setColor(Color.black);
			g2d.draw(rect);
		}

	}
}
