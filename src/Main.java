import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {
    
    public static void main(String[] args) {
        ThreeDimensionalCanvas canvas = new ThreeDimensionalCanvas(250, 600, 600, 300,300, new Color(255,255,255), new Color(0,0,0, 251));
        canvas.clear(true);
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
        new Timer(10, updateTask).start();
        
    }
}
