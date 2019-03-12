import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class Main {
    
    public static void main(String[] args) {
        ThreeDimensionalCanvas canvas = new ThreeDimensionalCanvas(250, 600, 600, 300,300, new Color(0,0,0), new Color(255,255,255));
        canvas.clear(true);
        MouseMotionListener dragTask = new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
                canvas.mouseDragged(mouseEvent);
            }
    
            @Override
            public void mouseMoved(MouseEvent mouseEvent) {
                canvas.mouseMoved(mouseEvent);
            }
        };
        ActionListener updateTask = new ActionListener() {
            private int counter = 0;
            private long fpsStart = System.currentTimeMillis();
            private int fpsCount = 0;
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                canvas.loop();
                if (fpsStart + 100 < System.currentTimeMillis()){
                    System.out.println("FPS: " + (1000 / (System.currentTimeMillis() - fpsStart) * fpsCount));
                    fpsCount = 0;
                    fpsStart = System.currentTimeMillis();
                }
                fpsCount++;
            }
        };
        canvas.addMouseMotionListener(dragTask);
        new Timer(10, updateTask).start();
        
    }
}
