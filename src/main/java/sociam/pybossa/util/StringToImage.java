package sociam.pybossa.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

public class StringToImage {
	final static Logger logger = Logger.getLogger(StringToImage.class);

	// public static void main(String[] args) {
	//
	// convertToImage(
	// "Because font metrics is based on a graphics context, we need to Because
	// font metrics is based on a graphics context, we need to Because font",
	// "Can you translate the below text? (yes/no/not valid)");
	// }

	public static File convertStringToImage(String text) {

		/*
		 * Because font metrics is based on a graphics context, we need to
		 * create a small, temporary image so we can ascertain the width and
		 * height of the final image
		 */
		try {
			BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_BINARY);
			Graphics2D g2d = img.createGraphics();
			int width = 650;
			int height = 140;
			Font font = new Font("Arial", Font.PLAIN, 20);
			g2d.setFont(font);
			FontMetrics fm = g2d.getFontMetrics();
			g2d.dispose();
			img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			g2d = img.createGraphics();
			g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
					RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
			g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
			// g2d.setColor(Color.BLUE);
			// g2d.fillRect(0, 0, width, height);
			g2d.setFont(font);
			fm = g2d.getFontMetrics();
			g2d.setColor(Color.BLACK);

			int lineWidth = 60;
			int x = 20;
			int y = 30;
			// g2d.drawString(question, x, y);
			// y += 40;
			if (fm.stringWidth(text) < lineWidth) {
				g2d.drawString(text, x, y);
			} else {
				String[] words = text.split("\\s+");
				String currentLine = words[0];
				for (int i = 1; i < words.length; i++) {
					String tmp = currentLine + " " + words[i];
					if (tmp.length() < lineWidth) {
						currentLine = currentLine + " " + words[i];
					} else {
						g2d.drawString(currentLine, x, y);
						y += fm.getHeight();
						currentLine = words[i];
					}
				}
				if (currentLine.trim().length() > 0) {
					g2d.drawString(currentLine, x, y);
				}
			}
			g2d.dispose();
			// try {
			// ImageIO.write(img, "png", new File("Text.png"));
			// } catch (IOException ex) {
			// ex.printStackTrace();
			// }
			File outputfile = new File("textToImage.jpg");
			ImageIO.write(img, "jpg", outputfile);
			// ImageIO.write(img, "jpg", new File("image.jpg"));
			return outputfile.getAbsoluteFile();
		} catch (Exception e) {
			logger.error("Error ", e);
			return null;
		}
	}
}
