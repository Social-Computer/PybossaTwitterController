package sociam.pybossa.twitter;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.UIManager;

public class LineBreakSample extends JApplet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void init() {
		buildUI(getContentPane());
	}

	public void buildUI(Container container) {
		try {
			String cn = UIManager.getSystemLookAndFeelClassName();
			UIManager.setLookAndFeel(cn);
		} catch (Exception cnf) {
		}
		LineBreakPanel lineBreakPanel = new LineBreakPanel();
		container.add(lineBreakPanel, BorderLayout.CENTER);
	}

	public static void main(String[] args) {

		JFrame f = new JFrame("Line Break Sample");

		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		LineBreakSample lineBreakSample = new LineBreakSample();
		lineBreakSample.buildUI(f.getContentPane());
		f.setSize(new Dimension(400, 250));
		f.setVisible(true);
	}
}
