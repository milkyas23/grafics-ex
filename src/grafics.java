import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class grafics extends Canvas implements Runnable{
    int x, y;
    int x1 = 400;
    int y1 = 300;
    double angle = 0;
    BufferStrategy bs;
    int width = 800;
    int height = 600;
    BufferedImage img = null;
    // Genom att lägga uppdaterinegn av skärmen i en egen tråd kan vi styra exakt med vilken hastighet den ska
    // renderas
    private Thread thread;
    private boolean running = false;

    public grafics() {
        try {
            img = ImageIO.read(new File("supermario.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        setSize(width, height);
        JFrame frame = new JFrame("grafic version 4");
        frame.add(this);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public synchronized void start() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public synchronized void stop() {
        running = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /* Eftersom vi vill ta kontroll över hur snabbt vår animering updateras bestämmer vi en tid mellan varje uppdatering
       Tiden mellan två uppdateringar blir 1 /30 sekund (30 ups eller fps). Delta anger i hur nära vi är en ny uppdatering.
       När delta blir 1 är det dags att rita igen. delta nollställs inte eftersom det kan hända att något tagit lång tid
       och att vi måste göra flera uppdateringar efter varandra.

       Här ligger update och render i samma tidssteg. Det går att separera dessa. Egentligen kan vi rita ut hur fort som
       helst (lägga render utanför while(delta>1)) Det viktiga är att update anropas med konstant hastighet eftersom det
       är den som simulerar tiden i animeringar.
     */
    public void run() {
        double ns = 1000000000.0 / 1024.0;
        double delta = 0;
        long lastTime = System.nanoTime();

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;

            while(delta >= 1) {
                // Uppdatera koordinaterna
                update();
                // Rita ut bilden med updaterad data
                render();
                delta--;
            }
        }
        stop();
    }


    public void render() {
        bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }
        Graphics g = bs.getDrawGraphics();
        // Rita ut den nya bilden
        draw(g);
        g.dispose();
        bs.show();
    }

    public void draw(Graphics g) {

        drawHouse(400, 400, g);
        drawHouse(460, 400, g);
        drawHouse(520, 400, g);
        drawHouse(x, y, g);
        g.drawImage(img,600,50,null);
        g.setColor(new Color(0x00FF00));
        g.fillOval(x1, y1, 40, 40);
    }

    // Uppdatera x och y-koordinaterna. Ger en cirkel
    private void update() {
        x = 200 + (int) (100 * Math.cos(angle));
        y = 200 + (int) (100 * Math.sin(angle));
        angle += 2 * Math.PI / 720;
    }

    // Rita ett litet hus på koordinaterna (x,y)
    private void drawHouse(int x, int y, Graphics g) {
        g.setColor(new Color(0xAA1111));
        g.fillRect(x, y, 50, 50);
        g.setColor(new Color(0x444444));
        int[] xcoords = {x, x + 25, x + 50};
        int[] ycoords = {y, y - 50, y};
        g.fillPolygon(xcoords, ycoords, 3);
    }

    public static void main(String[] args) {
        grafics mingrafic = new grafics();
        mingrafic.start();
    }
}

