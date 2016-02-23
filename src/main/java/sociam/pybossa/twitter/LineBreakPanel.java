package sociam.pybossa.twitter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Hashtable;

import javax.swing.JPanel;

public class LineBreakPanel extends JPanel{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// The LineBreakMeasurer used to line-break the paragraph.
    private LineBreakMeasurer lineMeasurer;
 
    // index of the first character in the paragraph.
    private int paragraphStart;
 
    // index of the first character after the end of the paragraph.
    private int paragraphEnd;
 
    private static final
        Hashtable<TextAttribute, Object> map =
           new Hashtable<TextAttribute, Object>();
 
    static {
        map.put(TextAttribute.FAMILY, "Serif");
        map.put(TextAttribute.SIZE, new Float(18.0));
    }  
 
    private static AttributedString vanGogh = new AttributedString(
        "Many people believe that Vincent van Gogh painted his best works " +
        "during the two-year period he spent in Provence. Here is where he " +
        "painted The Starry Night--which some consider to be his greatest " +
        "work of all. However, as his artistic brilliance reached new " +
        "heights in Provence, his physical and mental health plummeted. ",
        map);
 
    public void paintComponent(Graphics g) {
 
    super.paintComponent(g);
        setBackground(Color.white);
 
        Graphics2D g2d = (Graphics2D)g;
 
        // Create a new LineBreakMeasurer from the paragraph.
        // It will be cached and re-used.
        if (lineMeasurer == null) {
            AttributedCharacterIterator paragraph = vanGogh.getIterator();
            paragraphStart = paragraph.getBeginIndex();
            paragraphEnd = paragraph.getEndIndex();
            FontRenderContext frc = g2d.getFontRenderContext();
            lineMeasurer = new LineBreakMeasurer(paragraph, frc);
        }
 
        // Set break width to width of Component.
        float breakWidth = (float)getSize().width;
        float drawPosY = 0;
        // Set position to the index of the first character in the paragraph.
        lineMeasurer.setPosition(paragraphStart);
 
        // Get lines until the entire paragraph has been displayed.
        while (lineMeasurer.getPosition() < paragraphEnd) {
 
            // Retrieve next layout. A cleverer program would also cache
            // these layouts until the component is re-sized.
			TextLayout layout = lineMeasurer.nextLayout(breakWidth);
 
            // Compute pen x position. If the paragraph is right-to-left we
            // will align the TextLayouts to the right edge of the panel.
            // Note: this won't occur for the English text in this sample.
            // Note: drawPosX is always where the LEFT of the text is placed.
            float drawPosX = layout.isLeftToRight()
                ? 0 : breakWidth - layout.getAdvance();
 
            // Move y-coordinate by the ascent of the layout.
            drawPosY += layout.getAscent();
 
            // Draw the TextLayout at (drawPosX, drawPosY).
            layout.draw(g2d, drawPosX, drawPosY);
 
            // Move y-coordinate in preparation for next layout.
            drawPosY += layout.getDescent() + layout.getLeading();
        }
    }
 
}
 
