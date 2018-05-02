import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.TreeSet;

import javax.activation.DataHandler;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Window extends JPanel implements MouseListener, KeyListener {
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setUndecorated(true);
		Window w = new Window(frame);
		frame.add(w);
		frame.addKeyListener(w);
		frame.addMouseListener(w);
		frame.pack();
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	private JFrame frame;
	private BufferedImage image;
	private Point click;
	private TreeSet<Integer> pressed;
	public Window(JFrame frame) {
		super();
		this.frame = frame;
		initializeImage();
		
		pressed = new TreeSet<>();
		
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension cursorArea = new Dimension(16, 16);
		Point center = new Point(cursorArea.width/2, cursorArea.height/2);
		Image image = new BufferedImage(cursorArea.width, cursorArea.height, BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.getGraphics();
		g.setColor(Color.RED);
		//g.drawRect(center.x-1, center.y-1, 2, 2);
		g.drawLine(center.x, center.y, center.x + cursorArea.width/2, center.y);
		g.drawLine(center.x, center.y, center.x, center.y + cursorArea.height);
		g.drawLine(center.x, center.y, center.x - cursorArea.width/2, center.y);
		g.drawLine(center.x, center.y, center.x, center.y - cursorArea.height);
		
		Cursor c = toolkit.createCustomCursor(image, new Point(cursorArea.width, cursorArea.height), "img");
		setCursor(c);
	}
	public void initializeImage() {
		try {
			Rectangle rect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
			image = new Robot().createScreenCapture(rect);
		} catch (AWTException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error");
			System.exit(0);
		}
	}
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		/*
		g.drawImage(image, 0, 0, null);
		g.fillRect(0, 0, getWidth(), getHeight());
		*/
		
		if(click != null) {
			int interval = pressed.contains(KeyEvent.VK_SHIFT) ? 5 : 1;
			if(pressed.contains(KeyEvent.VK_W)) {
				click.y -= interval;
				click.y = Math.max(click.y, 0);
			}
			if(pressed.contains(KeyEvent.VK_A)) {
				click.x -= interval;
				click.x = Math.max(click.x, 0);
			}
			if(pressed.contains(KeyEvent.VK_S)) {
				click.y += interval;
				click.y = Math.min(click.y, image.getHeight());
			}
			if(pressed.contains(KeyEvent.VK_D)) {
				click.x += interval;
				click.x = Math.min(click.x, image.getWidth());
			}
			/*
			//int interval = e.isShiftDown() ? 5 : 1;
			switch(e.getKeyCode()) {
			case KeyEvent.VK_W:
				click.y -= interval;
				click.y = Math.max(click.y, 0);
				break;
			case KeyEvent.VK_A:
				click.x -= interval;
				click.x = Math.max(click.x, 0);
				break;
			case KeyEvent.VK_S:
				click.y += interval;
				click.y = Math.min(click.y, image.getHeight());
				break;
			case KeyEvent.VK_D:
				click.x += interval;
				click.x = Math.min(click.x, image.getWidth());
				break;
			}
			*/
		}
		
		BufferedImage overlay = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		//Graphics gOverlay = overlay.getGraphics();
		for(int y = 0; y < image.getHeight(); y++) {
			for(int x = 0; x < image.getWidth(); x++) {
				Color c = new Color(image.getRGB(x, y));
				//int filtered = new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue(), 255).getRGB();
				int gray = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
				int filtered = new Color(gray, gray, gray, 255).getRGB();
				overlay.setRGB(x, y, filtered);
			}
		}
		g.drawImage(overlay, 0, 0, null);
		
		Rectangle rect = getCaptureRect();
		if(rect != null) {
			//Selected area is transparent
			g.drawImage(image.getSubimage(rect.x, rect.y, rect.width, rect.height), rect.x, rect.y, null);
			g.setColor(new Color(255, 0, 0, 255));
			g.drawRect(rect.x, rect.y, rect.width, rect.height);
		}
		/*
		//Area around the mouse is transparent
		Point mouse = getMousePosition();
		rect = new Rectangle(new Point(mouse.x - 16, mouse.y - 16));
		rect.add(new Point(mouse.x + 16, mouse.y + 16));
		rect.x = Math.max(rect.x, 0);
		rect.y = Math.max(rect.y, 0);
		rect.width = rect.x + rect.width > image.getWidth() ? image.getWidth() - rect.x : rect.width;
		rect.height = rect.y + rect.height > image.getHeight() ? image.getHeight() - rect.y : rect.height;
		g.drawImage(image.getSubimage(rect.x, rect.y, rect.width, rect.height), rect.x, rect.y, null);
		*/
		repaint();
	}
	private Rectangle getCaptureRect() {
		Point mouse = getMousePosition();
		if(click != null && click.x != mouse.x && click.y != mouse.y) {
			Rectangle rect = new Rectangle(click);
			rect.add(mouse);
			rect.x = Math.max(rect.x, 0);
			rect.y = Math.max(rect.y, 0);
			rect.width = rect.x + rect.width > image.getWidth() ? image.getWidth() - rect.x : rect.width;
			rect.height = rect.y + rect.height > image.getHeight() ? image.getHeight() - rect.y : rect.height;
			return rect;
		} else {
			return null;
		}
	}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {
		click = e.getPoint();
	}
	public void mouseReleased(MouseEvent e) {
		Rectangle rect = getCaptureRect();
		if(rect != null) {
			setClipboard(image.getSubimage(rect.x, rect.y, rect.width, rect.height));
		}
		click = null;
	}
	public void keyPressed(KeyEvent e) {
		pressed.add(e.getKeyCode());
		switch(e.getKeyCode()) {
		case KeyEvent.VK_CONTROL:
			frame.setVisible(false);
			initializeImage();
			frame.setVisible(true);
			break;
		case KeyEvent.VK_ENTER:
			/*
			BufferedImage original = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
			original.getGraphics().drawImage(image, 0, 0, null);
			for(int y = 0; y < image.getHeight(); y++) {
				for(int x = 0; x < image.getWidth(); x++) {
					Color c = new Color(image.getRGB(x, y));
					image.setRGB(x, y, new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue(), 255).getRGB());
				}
			}
			*/
			setClipboard(image);
			break;
		case KeyEvent.VK_ESCAPE:
			System.exit(0);
			break;
		}
	}
	public void keyReleased(KeyEvent e) {
		pressed.remove(e.getKeyCode());
	}
	public void keyTyped(KeyEvent e) {}
	private void setClipboard(BufferedImage image) {
        Transferable t = new ImageSelection(image);
        Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
        c.setContents(t, null);
	}
}
//https://stackoverflow.com/questions/3025388/how-can-i-use-java-to-place-a-jpg-file-into-the-clipboard-so-that-it-can-be-pas
class ImageSelection implements Transferable {
    private Image image;

    public ImageSelection(Image image) {
        this.image = image;
    }

    // Returns supported flavors
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DataFlavor.imageFlavor};
    }

    // Returns true if flavor is supported
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return DataFlavor.imageFlavor.equals(flavor);
    }

    // Returns image
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (!DataFlavor.imageFlavor.equals(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }
        return image;
    }
}