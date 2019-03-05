import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {
    
    public static void main(String[] args) {
        ThreeDimensionalCanvas canvas = new ThreeDimensionalCanvas(600, 600, 300,300, new Color(0,0,0));
        canvas.clear();
        ActionListener updateTask = new ActionListener() {
            private int counter = 0;
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                canvas.loop();
            }
        };
        new Timer(10, updateTask).start();
    }
}
