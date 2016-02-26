package sociam.pybossa.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
/**
 * 
 * @author user Saud Aljaloud
 * @author email sza1g10@ecs.soton.ac.uk
 *
 */
public class StringToImage {
	final static Logger logger = Logger.getLogger(StringToImage.class);

	// the hight of the text
	static int height = 140;

	public static File combineTextWithImage(String text, String ImageURL) {

		try {
			URL url = new URL(ImageURL);
			BufferedImage img1 = ImageIO.read(url);
			BufferedImage img2 = convertStringToImage(text, img1.getWidth());
			File combinedImage = draw(img1, img2);
			return combinedImage;
		} catch (IOException e) {
			logger.error("Error", e);
			return null;
		}

	}

	private static File draw(BufferedImage img1, BufferedImage img2) {
		try {
			int widthImg1 = img1.getWidth();
			int heightImg1 = img1.getHeight();
			int heightImg2 = img2.getHeight();
			BufferedImage img = new BufferedImage(widthImg1, heightImg1 + heightImg2, BufferedImage.TYPE_INT_ARGB);
			img.createGraphics().setColor(Color.RED);
			boolean image1Drawn = img.createGraphics().drawImage(img1, 0, 20 + img2.getHeight(), null);
			if (!image1Drawn) {
				logger.error("Problems drawing first image");
			}
			boolean image2Drawn = img.createGraphics().drawImage(img2, 0, 0, null);
			if (!image2Drawn) {
				logger.error("Problems drawing second image");
			}

			File final_image = new File("Final.jpg");
			if (ImageIO.write(img, "jpeg", final_image)) {
				return final_image;
			} else {
				return null;
			}
		} catch (IOException e) {
			logger.error("Error", e);
			return null;
		}

	}

	private static BufferedImage convertStringToImage(String text, int width) {

		try {
			BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = img.createGraphics();

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
			g2d.setColor(Color.WHITE);
			// g2d.fillRect(0, 0, width, height);
			fm = g2d.getFontMetrics();

			int x = 20;
			int y = 30;
			if (fm.stringWidth(text) < width) {
				g2d.drawString(text, x, height / 2);
			} else {
				String[] words = text.split("\\s+");
				String currentLine = words[0];
				for (int i = 1; i < words.length; i++) {
					String tmp = currentLine + " " + words[i];
					if (fm.stringWidth(tmp) < width) {
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
			return img;
		} catch (Exception e) {
			logger.error("Error", e);
			return null;
		}

	}

	public static File convertStringToImage(String text) {

		/*
		 * Because font metrics is based on a graphics context, we need to
		 * create a small, temporary image so we can ascertain the width and
		 * height of the final image
		 */
		try {
			BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
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
