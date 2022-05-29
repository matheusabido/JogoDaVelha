import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.Random;

public class JogoDaVelha {
    private static boolean umJogador = false, vezX = true;
    private static int[] umJogadorTextInfo = new int[4], doisJogadoresTextInfo = new int[4];

    private static byte[][] game = new byte[3][3];
    private static final int GAME_SIZE = 300, SQUARE_SIZE = GAME_SIZE / 3, SHAPE_SIZE = 60, FONT_SIZE = 16;
    private static final JFrame frame = new JFrame("Jogo da Velha");
    public static void main(String[] args) {
        configureFrame();
        registerMouseEvents();

        while (true) {
            render();
        }
    }

    private static void render() {
        BufferedImage image = new BufferedImage(frame.getContentPane().getWidth(), frame.getContentPane().getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(Color.white);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());

        {
            Font font = new Font("Arial", Font.BOLD, FONT_SIZE);
            g.setFont(font);
        }
        drawGameModes(g);

        {
            Font font = new Font("Arial", Font.BOLD, SHAPE_SIZE);
            g.setFont(font);
        }

        highlightHoveredSquare(g);
        drawLines(g);
        drawGame(g);

        {
            byte vencedor = getWinner();
            if (vencedor > 0) {
                String venceuText = (vencedor == 1 ? "X" : "O") + " VENCEU";
                int size = g.getFontMetrics().stringWidth(venceuText);
                int x = frame.getContentPane().getWidth() / 2 - size / 2;
                int y = frame.getContentPane().getHeight() / 2 + SHAPE_SIZE / 2;
                g.setColor(new Color(0, 0, 0, 255/2));
                g.fillRect(x - 5, y - SHAPE_SIZE, size + 10, SHAPE_SIZE + 15);
                g.setColor(Color.red);
                g.drawString(venceuText, x, y);
            } else if (vencedor == 0) {

            }
        }

        frame.getGraphics().drawImage(image, 8, 31, null);
    }

    private static void configureFrame() {
        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    private static void drawGameModes(Graphics2D g) {
        String modosText = "MODOS DE JOGO";
        String umJogadorText = "UM JOGADOR";
        String doisJogadoresText = "DOIS JOGADORES";
        int gapBetween = 30;
        int umJogadorWidth = g.getFontMetrics().stringWidth(umJogadorText);
        int doisJogadoresWidth = g.getFontMetrics().stringWidth(doisJogadoresText);
        int fullWidth = umJogadorWidth + doisJogadoresWidth + gapBetween;

        g.setColor(Color.black);
        g.drawString(modosText, frame.getContentPane().getWidth() / 2 - g.getFontMetrics().stringWidth(modosText) / 2, 10 + FONT_SIZE);

        {
            int x = frame.getContentPane().getWidth() / 2 - fullWidth / 2;
            int y = 20 + 2 * FONT_SIZE;
            umJogadorTextInfo = new int[] {x, y - FONT_SIZE, x + umJogadorWidth, y};
        }
        {
            int x = frame.getContentPane().getWidth() / 2 - fullWidth / 2 + umJogadorWidth + gapBetween;
            int y = 20 + 2 * FONT_SIZE;
            doisJogadoresTextInfo = new int[] {x, y - FONT_SIZE, x + doisJogadoresWidth, y};
        }

        Color hoveredColor = new Color(100, 100, 100);

        if (isHovering(umJogadorTextInfo)) g.setColor(hoveredColor);
        if (umJogador) g.setColor(Color.red);
        g.drawString(umJogadorText, umJogadorTextInfo[0], umJogadorTextInfo[1] + FONT_SIZE);
        g.setColor(umJogador ? (isHovering(doisJogadoresTextInfo) ? hoveredColor : Color.black) : Color.red);
        g.drawString(doisJogadoresText, doisJogadoresTextInfo[0], doisJogadoresTextInfo[1] + FONT_SIZE);
    }

    private static boolean isHovering(int[] info) {
        Point mousePoint = getRelativeMousePoint();
        mousePoint.setLocation(mousePoint.x - 8, mousePoint.y - 19);
        return mousePoint.x >= info[0] && mousePoint.y >= info[1] && mousePoint.x <= info[2] && mousePoint.y <= info[3];
    }

    private static void drawGame(Graphics2D g) {
        Point drawingPoint = getGameDrawingPoint();
        g.setColor(Color.black);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int jogada = game[i][j];
                if (jogada != 0) {
                    String shape = jogada == 1 ? "X" : "O";
                    int x = drawingPoint.x + i * SQUARE_SIZE + (SQUARE_SIZE / 2 - g.getFontMetrics().stringWidth(shape) / 2);
                    int y = drawingPoint.y + j * SQUARE_SIZE + SQUARE_SIZE - SHAPE_SIZE / 2;
                    g.drawString(shape, x, y);
                }
            }
        }
    }

    private static void drawLines(Graphics2D g) {
        Point drawingPoint = getGameDrawingPoint();
        int x = drawingPoint.x;
        int y = drawingPoint.y;
        g.setColor(Color.black);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                g.fillRect(x + i * SQUARE_SIZE, y + j * SQUARE_SIZE, 1, SQUARE_SIZE);
                g.fillRect(x + i * SQUARE_SIZE, y + j * SQUARE_SIZE, SQUARE_SIZE, 1);
                g.fillRect(x + SQUARE_SIZE + i * SQUARE_SIZE, y + j * SQUARE_SIZE, 1, SQUARE_SIZE);
                g.fillRect(x + i * SQUARE_SIZE, y + SQUARE_SIZE + j * SQUARE_SIZE, SQUARE_SIZE, 1);
            }
        }
    }

    private static void highlightHoveredSquare(Graphics2D g) {
        int[] hoveredSquare = getHoveredSquare();
        if (hoveredSquare[0] == -1 || hoveredSquare[1] == -1) return;

        Point drawingPoint = getGameDrawingPoint();

        g.setColor(Color.lightGray);
        g.fillRect(drawingPoint.x + SQUARE_SIZE * hoveredSquare[0], drawingPoint.y + SQUARE_SIZE * hoveredSquare[1], SQUARE_SIZE, SQUARE_SIZE);
    }

    private static void registerMouseEvents() {
        frame.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {
                int[] hoveredSquare = getHoveredSquare();
                if (hoveredSquare[0] != -1 && hoveredSquare[1] != -1) {
                    if (getWinner() == -1) {
                        if (game[hoveredSquare[0]][hoveredSquare[1]] == 0) {
                            game[hoveredSquare[0]][hoveredSquare[1]] = (byte) (vezX ? 1 : 2);
                            vezX = !vezX;
                            if (umJogador && getWinner() == -1) {
                                jogarAleatoriamente();
                            }
                        }
                    }
                } else if (isHovering(umJogadorTextInfo)) {
                    umJogador = true;
                    resetGame();
                } else if (isHovering(doisJogadoresTextInfo)) {
                    umJogador = false;
                    resetGame();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
        });
    }

    private static void jogarAleatoriamente() {
        if (umJogador && getWinner() == -1) {
            int random = new Random().nextInt(9);
            for (int k = 0; k < 10 && game[random / 3][random % 3] != 0; k++) random = new Random().nextInt(9);
            if (game[random / 3][random % 3] == 0) {
                game[random / 3][random % 3] = 2;
            } else {
                boolean jogou = false;
                for (int i = 0; i < 3 && !jogou; i++) {
                    for (int j = 0; j < 3 && !jogou; j++) {
                        if (game[i][j] == 0) {
                            game[i][j] = 2;
                            jogou = true;
                        }
                    }
                }
            }
            vezX = true;
        }
    }

    private static byte getWinner() {
        byte winnerHorizontally, winnerVertically, winnerDiagonally;
        if ((winnerHorizontally = getWinnerHorizontally()) != -1) return winnerHorizontally;
        if ((winnerVertically = getWinnerVertically()) != -1) return winnerVertically;
        if ((winnerDiagonally = getWinnerDiagonally()) != -1) return winnerDiagonally;
        if (verifyDraw()) return 0;
        return -1;
    }

    private static boolean verifyDraw() {
        boolean empate = true;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                empate = empate && game[i][j] != 0;
            }
        }
        return empate;
    }

    private static byte getWinnerHorizontally() {
        for (int i = 0; i < 3; i++) {
            boolean won = true;
            byte last = game[0][i];
            if (last == 0) continue;
            for (int j = 1; j < 3; j++) {
                if (game[j][i] == 0) {
                    won = false;
                    break;
                }
                won = won && last == game[j][i];
                last = game[j][i];
            }
            if (won) return last;
        }
        return -1;
    }

    private static byte getWinnerVertically() {
        for (int i = 0; i < 3; i++) {
            boolean won = true;
            byte last = game[i][0];
            if (last == 0) continue;
            for (int j = 1; j < 3; j++) {
                if (game[i][j] == 0) {
                    won = false;
                    break;
                }
                won = won && last == game[i][j];
                last = game[i][j];
            }
            if (won) return last;
        }
        return -1;
    }

    private static byte getWinnerDiagonally() {
        byte lastA = game[0][0], lastB = game[0][2];
        boolean wonA = true, wonB = true;
        if (lastA == 0) wonA = false;
        if (lastB == 0) wonB = false;
        for (int i = 1; i < 3; i++) {
            if (game[i][i] == 0) wonA = false;
            if (game[i][2 - i] == 0) wonB = false;
            wonA = wonA && lastA == game[i][i];
            wonB = wonB && lastB == game[i][2 - i];
            lastA = game[i][i];
            lastB = game[i][2 - i];
        }
        if (wonA) return lastA;
        if (wonB) return lastB;
        return -1;
    }

    private static void resetGame() {
        game = new byte[3][3];
        vezX = true;
    }

    private static int[] getHoveredSquare() {
        int[] hoveredSquare = {-1, -1};
        if (isMouseInside()) {
            Point mousePoint = getRelativeMousePoint();
            int i = mousePoint.x / 100 - 1;
            int j = mousePoint.y / 100 - 1;
            if ((i >= 0 && i <= 2) && (j >= 0 && j <= 2)) {
                hoveredSquare[0] = i;
                hoveredSquare[1] = j;
            }
        }
        return hoveredSquare;
    }

    private static boolean isMouseInside() {
        Point mousePoint = getRelativeMousePoint();
        return mousePoint.x >= 0 && mousePoint.y >= 0;
    }

    private static Point getRelativeMousePoint() {
        Point mousePoint = MouseInfo.getPointerInfo().getLocation();
        Point framePoint = new Point(frame.getLocation().x, frame.getLocation().y);
        return new Point(mousePoint.x - framePoint.x, mousePoint.y - framePoint.y - 12);
    }

    private static Point getGameDrawingPoint() {
        Container content = frame.getContentPane();
        return new Point(content.getWidth() / 2 - GAME_SIZE / 2, content.getHeight() / 2 - GAME_SIZE / 2);
    }
}