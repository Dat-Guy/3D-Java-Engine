import javax.swing.*;
import java.awt.*;

public class ThreeDimensionalCanvas {
	private final JFrame frame;
	private final Canvas canvas;
	private final Graphics2D graphics;
	public ThreeDimensionalCanvas(int width, int height){
		frame = new JFrame();
		frame.setSize(width, height);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(width, height);
		
		canvas = new Canvas();
		canvas.setSize(width, height);
		
		frame.add(canvas);
		frame.setVisible(true);
		
		graphics = (Graphics2D) canvas.getGraphics();
		graphics.setColor(new Color(255, 0, 0));
		
	}
	public void drawLine(int x1, int y1, int x2, int y2){
		graphics.drawLine(x1,y1,x2,y2);
	}
	public void update(){
		canvas.update(graphics);
	}
}
