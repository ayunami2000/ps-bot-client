package me.ayunami2000.psbot;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// based on https://stackoverflow.com/a/24476755/6917520

public class ResizableFrame {
	private JFrame frame = new JFrame("ps-bot -- Change screen capture size");

	class MainPanel extends JPanel {

		private BorderPanel bp;

		public MainPanel(BorderPanel bp) {
			this.bp = bp;
			setBackground(Color.white);
		}

		@Override
		public Dimension getPreferredSize() {
			Dimension res;
			if (PsBot.screenSize[0] == 0 && PsBot.screenSize[1] == 0 && PsBot.screenSize[2] == 0 && PsBot.screenSize[3] == 0) {
				res = Toolkit.getDefaultToolkit().getScreenSize();
			} else {
				res =  new Dimension(PsBot.screenSize[2], PsBot.screenSize[3]);
			}
			res.setSize(res.width - 10, res.height - (10 + 26)); // mehhh hardcode 26
			return res;
		}
	}

	class BorderPanel extends JPanel {

		private JLabel label;
		int pX, pY;

		public BorderPanel() {
			label = new JLabel(" X ");
			label.setOpaque(true);
			label.setBackground(Color.RED);
			label.setForeground(Color.WHITE);

			setBackground(Color.black);
			setLayout(new FlowLayout(FlowLayout.RIGHT));

			add(label);

			label.addMouseListener(new MouseAdapter() {
				public void mouseReleased(MouseEvent e) {
					Point loc = frame.getLocation();
					Dimension dim = frame.getSize();
					PsBot.screenSize = new int[] { loc.x, loc.y, dim.width, dim.height };
					PsBot.chatMsg("Changed screen capture size to " + PsBot.screenSizeString());
					frame.dispose();
				}
			});
			addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent me) {
					// Get x,y and store them
					pX = me.getX();
					pY = me.getY();

				}

				public void mouseDragged(MouseEvent me) {

					frame.setLocation(frame.getLocation().x + me.getX() - pX,
							frame.getLocation().y + me.getY() - pY);
				}
			});

			addMouseMotionListener(new MouseMotionAdapter() {
				public void mouseDragged(MouseEvent me) {

					frame.setLocation(frame.getLocation().x + me.getX() - pX,
							frame.getLocation().y + me.getY() - pY);
				}
			});
		}
	}

	class OutsidePanel extends JPanel {

		public OutsidePanel() {
			setLayout(new BorderLayout());
			BorderPanel bp = new BorderPanel();
			add(new MainPanel(bp), BorderLayout.CENTER);
			add(bp, BorderLayout.PAGE_START);
			setBorder(new LineBorder(Color.BLACK, 5));
		}
	}

	public void createAndShowGui() {
		ComponentResizer cr = new ComponentResizer();
		cr.registerComponent(frame);
		cr.setSnapSize(new Dimension(1, 1));
		frame.setUndecorated(true);
		frame.add(new OutsidePanel());
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setOpacity(0.7f);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				Point loc = frame.getLocation();
				Dimension dim = frame.getSize();
				PsBot.screenSize = new int[] { loc.x, loc.y, dim.width, dim.height };
				PsBot.chatMsg("Changed screen capture size to " + PsBot.screenSizeString());
			}
		});
		if (PsBot.screenSize[0] == 0 && PsBot.screenSize[1] == 0 && PsBot.screenSize[2] == 0 && PsBot.screenSize[3] == 0) {
			frame.setLocation(0, 0);
		} else {
			frame.setLocation(PsBot.screenSize[0], PsBot.screenSize[1]);
		}
		frame.pack();
		frame.setVisible(true);
		frame.toFront();
	}
}
