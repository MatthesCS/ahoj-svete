import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Filler extends JFrame {
	private FillerApp fillerApp;
	private int x0 = 0;
	private int y0 = 0;
	private int x1 = 0;
	private int y1 = 0;
	private int x2 = 0;
	private int y2 = 0;
	public static final int WIDTH = 800;
	public static final int HEIGHT = 600;
	private int type = 1;  // prom�n� pro ur�en� typu vypl�ov�n� nebo kreslen�
	private List<Integer> body;  //pomocn� prom�nn� pro scanline algoritmus
	private boolean drawing = false;

	private JButton btLine;
	private JButton btSeed;
	private JButton btSeedLine;
	private JButton btScan;
	private JButton btClear;
	private JLabel lbOverflow;
	private JLabel lbScan;

	protected class MouseEventHandler extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			if (type == 1) {  // pokud se kresl�
				x1 = e.getX();
				y1 = e.getY();
				x2 = e.getX();
				y2 = e.getY();
			} else if (type == 2) { // pokud se vypl�uje sem�nkov�m algoritmem
				if (fillerApp.seedFill(e.getX(), e.getY(), getGraphics())) {
					lbOverflow.setText("Vybr�na moc velk� oblast pro sem�nko!");  // pokud po�et rekurz� p�es�hne limit,
				}														// zastav� se algoritmus a u�ivatel bude upozorn�n
			} else if (type == 3) {  // pokud se vypl�uje ��dkov�m sem�nkov�m algoritmem
				fillerApp.seedFillList(e.getX(), e.getY(), getGraphics());
			} else if (type == 4) {  // pokud se vyp�uje scanline algoritmem, nejprve se kresl� n-�heln�k
				if (!drawing && e.getButton() != 3) {
					drawing = true;
					x0 = e.getX();
					y0 = e.getY();
					x1 = e.getX();
					y1 = e.getY();
					x2 = e.getX();
					y2 = e.getY();
					btLine.setEnabled(false);
					btSeed.setEnabled(false);
					btSeedLine.setEnabled(false);
					btClear.setEnabled(false);
					lbScan.setText("Kresl�te n-�heln�k pro scanline vypln�n�, pro dokon�en� kreslen�, stiskn�te prav� tla��tko.");

					body.add(x0);
					body.add(y0);
					fillerApp.drawPixel(x0, y0, "line");
					fillerApp.showImage(getGraphics());
				} else if (drawing && e.getButton() != 3) {
					x2 = e.getX();
					y2 = e.getY();
					body.add(x2);
					body.add(y2);
					fillerApp.drawLine(x1, y1, x2, y2, "line");
					fillerApp.drawLine(x0, y0, x2, y2, "drawn");
					fillerApp.showImage(getGraphics());
					fillerApp.drawLine(x0, y0, x2, y2, "erase");
					x1 = x2;
					y1 = y2;
				} else if (drawing && e.getButton() == 3) {
					x2 = e.getX();
					y2 = e.getY();
					body.add(x2);
					body.add(y2);
					fillerApp.drawLine(x1, y1, x2, y2, "line");
					fillerApp.drawLine(x2, y2, x0, y0, "line");
					fillerApp.showImage(getGraphics());

					fillerApp.scanLine(body, getGraphics());

					lbScan.setText("    U ScanLine se nejd��ve kresl� n-�heln�k, kter� bude vypln�n.");
					drawing = false;
					body.clear();
					btLine.setEnabled(true);
					btSeed.setEnabled(true);
					btSeedLine.setEnabled(true);
					btClear.setEnabled(true);
				}
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			if (type == 4 && drawing && body.size() < 4) {
				x2 = e.getX();
				y2 = e.getY();
				fillerApp.drawLine(x1, y1, x2, y2, "drawn");
				fillerApp.showImage(getGraphics());
				fillerApp.drawLine(x1, y1, x2, y2, "erase");
			} else if (type == 4 && drawing) {
				x2 = e.getX();
				y2 = e.getY();
				fillerApp.drawLine(x1, y1, x2, y2, "drawn");
				fillerApp.drawLine(x0, y0, x2, y2, "drawn");
				fillerApp.showImage(getGraphics());
				fillerApp.drawLine(x1, y1, x2, y2, "erase");
				fillerApp.drawLine(x0, y0, x2, y2, "erase");
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (type == 1) {
				x2 = e.getX();
				y2 = e.getY();
				fillerApp.drawLine(x1, y1, x2, y2, "drawn");
				fillerApp.showImage(getGraphics());
				fillerApp.drawLine(x1, y1, x2, y2, "erase");
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (type == 1) {
				x2 = e.getX();
				y2 = e.getY();
				fillerApp.drawLine(x1, y1, x2, y2, "line");
				fillerApp.showImage(getGraphics());
			}
		}
	}

	protected void initialize() {
		fillerApp = new FillerApp();
		setVisible(true);
		MouseEventHandler handler = new MouseEventHandler();
		addMouseListener(handler);
		addMouseMotionListener(handler);

		btLine = new JButton("Kreslen�");
		btLine.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				type = 1;
			}
		});
		btSeed = new JButton("Vypln�n� (Sem�nko)");
		btSeed.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				type = 2;
			}
		});
		btSeedLine = new JButton("Vypln�n� (��dkov� sem�nko)");
		btSeedLine.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				type = 3;
			}
		});
		btScan = new JButton("Vypln�n� (Scanline)");
		btScan.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				type = 4;
			}
		});
		btClear = new JButton("Vymazat");
		btClear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fillerApp.clearImage();
				fillerApp.showImage(getGraphics());
			}
		});
		lbOverflow = new JLabel("Sem�nko pouze pro mal� oblasti!     ");
		lbScan = new JLabel(
				"    U ScanLine se nejd��ve kresl� n-�heln�k, kter� bude vypln�n.");

		JPanel btPanel = new JPanel(new FlowLayout());
		btPanel.add(btLine);
		btPanel.add(btSeed);
		btPanel.add(btSeedLine);
		btPanel.add(btScan);
		btPanel.add(btClear);

		JPanel lbPanel = new JPanel(new FlowLayout());
		lbPanel.add(lbOverflow);
		lbPanel.add(lbScan);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(btPanel, BorderLayout.NORTH);
		panel.add(lbPanel, BorderLayout.SOUTH);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(panel, "South");
		pack();
		setSize(WIDTH, HEIGHT + 100);
		fillerApp.clearImage();
		fillerApp.showImage(getGraphics());
		body = new ArrayList<>();
	}

	public static void main(String[] args) {
		Filler filler = new Filler();
		filler.initialize();
	}
}
