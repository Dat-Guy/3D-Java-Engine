

public class Main {

    public static void main(String[] args) {
        ThreeDimensionalCanvas canvas = new ThreeDimensionalCanvas(600, 600);
        for (var i = 1; i < 600; i++) {
            canvas.drawLine(0, i,600, i);
            if (i % 100 == 0) {
                try {
                    Thread.sleep(1000);
                    canvas.update();
                } catch (InterruptedException e) {
                    System.out.println("Inturrepted Mid-Sleep!");
                }
            }
        }
    }
}
