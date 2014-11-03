package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class Display extends JFrame {
	private JPanel contentPane;
	private DefaultTableModel model;

	// This lock protects the array flyPosition because I'm not entirely sure whether Swing can interfere. Safe is safe.
	private Lock lock;
	private int[] flyPosition;
	private Image img;

	// To notify the client of events, could have also been solved by an event-queue, but this is faster.
	private Client client;

	// Just so the array is not always recreated in updateTable()
	private String[] columnIdentifiers = new String[] { "Playername", "Score" };

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					Display frame = new Display();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * 
	 * @throws IOException
	 */
	public Display() throws IOException {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		this.contentPane = new JPanel();
		this.contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(this.contentPane);

		this.lock = new ReentrantLock();
		this.flyPosition = new int[2];
		this.img = ImageIO.read(new File("." + File.pathSeparator + "fliege-t20678.jpg"));

		add(generateLeftPane(), BorderLayout.WEST);
		add(generateMainPane(), BorderLayout.CENTER);
	}

	private Component generateLeftPane() {
		JTable table = new JTable(new DefaultTableModel());
		this.model = (DefaultTableModel) table.getModel();

		return new JScrollPane(table);
	}

	private Component generateMainPane() {
		JPanel drawingCanvas = new JPanel() {

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				// setting to white because someone didn't supplied a picture with transparent background...
				setBackground(Color.WHITE);

				Display.this.lock.lock();
				try {
					g.drawImage(Display.this.img, Display.this.flyPosition[0], Display.this.flyPosition[1], this);
				} finally {
					Display.this.lock.unlock();
				}
			}
		};

		drawingCanvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getX() > Display.this.flyPosition[0] && e.getX() < Display.this.flyPosition[0] + Display.this.img.getWidth(null)
						&& e.getY() > Display.this.flyPosition[1] && e.getY() < Display.this.flyPosition[1] + Display.this.img.getHeight(null)) {
					client.
				}
			}
		});

		return drawingCanvas;
	}

	/**
	 * Sets the displayed data for the JTable.
	 * 
	 * @param dataVector
	 */
	public void updateTable(Object[][] dataVector) {
		/*
		 * Yes, this is a convenience method. I want to keep track of the playerlist only at one place, and that is not here. This class *should* only show
		 * stuff, so I simply update the TableModels contents every time I change something. I mean, this is definitly NOT task of this assignment, right?
		 */
		this.model.setDataVector(dataVector, this.columnIdentifiers);
	}

	public void setFlyPosition(int x, int y) {
		this.lock.lock();
		try {
			this.flyPosition[0] = x;
			this.flyPosition[1] = y;
		} finally {
			this.lock.unlock();
		}
	}
}
