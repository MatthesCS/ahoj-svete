import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FillerApp {
	BufferedImage image;
	private static final int COLOR_DRAWN = 0xFFFFFFFF;
	private static final int COLOR_FILL = 0xFF00FF00;
	private static final int COLOR_LINE = 0xFFFF0000;
	private static final int COLOR_SCREEN = 0xFF000000;

	public FillerApp() {
		image = new BufferedImage(Filler.WIDTH, Filler.HEIGHT,
				BufferedImage.TYPE_INT_RGB);
	}

	public void showImage(Graphics g) {  //metoda pro vykreslení plátna
		g.drawImage(image, 0, 0, null);
	}

	public void clearImage() {  // metoda pro vyèištìní plátna
		for (int x = 0; x < Filler.WIDTH; x++) {
			for (int y = 0; y < Filler.HEIGHT; y++) {
				drawPixel(x, y, "clear");
			}
		}
	}

	public void drawPixel(int x, int y, String type) {  //metoda pro vykreslení pixelu, kontroluje pøepisování již nakreslených
		if (x >= 0 && y >= 0 && x < Filler.WIDTH && y < Filler.HEIGHT) {
			switch (type) {
			case "clear":
				image.setRGB(x, y, COLOR_SCREEN);
				break;
			case "erase":
				if (image.getRGB(x, y) == COLOR_DRAWN) {
					image.setRGB(x, y, COLOR_SCREEN);
				}
				break;
			case "line":
				image.setRGB(x, y, COLOR_LINE);
				break;
			case "drawn":
				if (image.getRGB(x, y) != COLOR_LINE
						&& image.getRGB(x, y) != COLOR_FILL) {
					image.setRGB(x, y, COLOR_DRAWN);
				}
				break;
			case "fill":
				image.setRGB(x, y, COLOR_FILL);
				break;

			default:
				break;
			}
		}
	}

	public void drawLine(int x1, int y1, int x2, int y2, String type) {  // metoda pro nakreslení úseèky
		if ((x1 == x2) && (y1 == y2)) {
			drawPixel(x1, y1, type);
		} else {
			int dx = Math.abs(x2 - x1);
			int dy = Math.abs(y2 - y1);
			int d = dx - dy;

			int mx, my;

			if (x1 < x2) {
				mx = 1;
			} else {
				mx = -1;
			}
			if (y1 < y2) {
				my = 1;
			} else {
				my = -1;
			}

			while ((x1 != x2) || (y1 != y2)) {
				int p = 2 * d;

				if (p > -dy) {
					d -= dy;
					x1 += mx;
				}
				if (p < dx) {
					d += dx;
					y1 += my;
				}
				drawPixel(x1, y1, type);
			}
		}
	}

	public void scanLine(List<Integer> points, Graphics g) {  //metoda pro vyplnìn oblasti scanline algoritmem
		class SCLine {
			private int x1, y1, x2, y2;
			private float k, q;

			public SCLine(int x1, int y1, int x2, int y2) {
				if (y1 > y2) {
					this.x1 = x2;
					this.y1 = y2;
					this.x2 = x1;
					this.y2 = y1;
				} else {
					this.x1 = x1;
					this.y1 = y1;
					this.x2 = x2;
					this.y2 = y2;
				}
				if (!this.isVertical()) {
					k = (y2 - y1) / (x2 - x1);
					q = ((k * x1) - y1) * (-1);
				}
			}

			public boolean isHorizontal() {
				return (y1 == y2);
			}

			public boolean isVertical() {
				return (x1 == x2);
			}

			public int yMin(int yMin) {
				return (y1 < yMin) ? y1 : yMin;
			}

			public int yMax(int yMax) {
				return (y2 > yMax) ? y2 : yMax;
			}

			public boolean intersects(int y) {
				return (y >= y1 && y < y2);
			}

			public int intersection(float y) {
				if (this.isVertical()) {
					return x1;
				}
				return (int) ((y - q) / k);
			}

		}

		List<SCLine> toDraw = new ArrayList<>();
		List<Integer> intersections = new ArrayList<>();
		int yMin = Filler.HEIGHT;
		int yMax = 0;

		for (int i = 0; i < points.size() - 1; i += 2) {
			SCLine line = new SCLine(points.get(i), points.get(i + 1),
					points.get((i + 2) % points.size()), points.get((i + 3)
							% points.size()));
			if (!line.isHorizontal()) {
				yMin = line.yMin(yMin);
				yMax = line.yMax(yMax);
				toDraw.add(line);
			}
		}

		for (int y = yMin; y <= yMax; y++) {
			intersections.clear();
			for (int j = 0; j < toDraw.size(); j++) {
				if (toDraw.get(j).intersects(y)) {
					intersections.add(toDraw.get(j).intersection((float) y));
				}
			}
			Collections.sort(intersections);
			for (int i = 1; i < intersections.size(); i++) {
				if (intersections.get(i) == intersections.get(i - 1)) {
					intersections.remove(i);
					intersections.remove(i - 1);
					i -= 2;
				}
			}
			if (intersections.size() % 2 == 1) {
				intersections.add(Filler.WIDTH);
			}
			for (int j = 0; j < intersections.size(); j = j + 2) {
				for (int x = intersections.get(j); x <= intersections
						.get(j + 1); x++) {
					drawPixel(x, y, "fill");
					//showImage(g);   // pøekreslení po každém zaneseném pixelu pro ukázání jak algoritmus pracuje, zpomaluje chod
				}
			}
		}

		for (int i = 0; i < points.size() - 1; i += 2) {
			drawLine(points.get(i), points.get(i + 1),
					points.get((i + 2) % points.size()),
					points.get((i + 3) % points.size()), "line");
			showImage(g);
		}
	}

	public void seedFillList(int x, int y, Graphics g) {  //metoda pro vyplnìní oblasti pomocí øádkového semínkového algoritmu
		if (x > 0 && x < Filler.WIDTH && y > 0 && y < Filler.HEIGHT) {
			if (COLOR_SCREEN == image.getRGB(x, y)) {
				List<Integer> points = new ArrayList<Integer>();
				int w, e;
				points.add(x);
				points.add(y);

				for (int i = 0; i < points.size() - 1; i += 2) {
					if (COLOR_SCREEN == image.getRGB(points.get(i),
							points.get(i + 1))) {
						w = points.get(i);
						e = points.get(i);
						while (w > 0
								&& COLOR_SCREEN == image.getRGB(w,
										points.get(i + 1))) {
							w--;
						}
						while (e < Filler.WIDTH
								&& COLOR_SCREEN == image.getRGB(e,
										points.get(i + 1))) {
							e++;
						}
						for (int j = w + 1; j < e; j++) {
							drawPixel(j, points.get(i + 1), "fill");
							//showImage(g);  // pøekreslení po každém zaneseném pixelu pro ukázání jak algoritmus pracuje, zpomaluje chod
							if (points.get(i + 1) + 1 < Filler.HEIGHT
									&& COLOR_SCREEN == image.getRGB(j,
											points.get(i + 1) + 1)) {
								points.add(j);
								points.add(points.get(i + 1) + 1);
							}
							if (points.get(i + 1) - 1 > 0
									&& COLOR_SCREEN == image.getRGB(j,
											points.get(i + 1) - 1)) {
								points.add(j);
								points.add(points.get(i + 1) - 1);
							}
						}
					}
				}
			}
		}
	}

	public boolean seedFill(int x, int y, Graphics g) {  //metoda pro vyplnìní oblasti pomocí semínkového algoritmu
		if (x > 0 && x < Filler.WIDTH && y > 0 && y < Filler.HEIGHT) {  // vrací true, pokud pøehltí stack
			if (COLOR_SCREEN == image.getRGB(x, y)) {
				drawPixel(x, y, "fill"); 
				//showImage(g); // pøekreslení po každém zaneseném pixelu pro ukázání jak algoritmus pracuje, zpomaluje chod
				try {
					if (seedFill(x, y - 1, g) || seedFill(x, y + 1, g)
							|| seedFill(x + 1, y, g) || seedFill(x - 1, y, g)) {
						return true;
					}
				} catch (StackOverflowError e) {
					return true;  // ukonèí vyplòování pøi pøehlcení stacku
				}
			}
		}
		return false;
	}
}