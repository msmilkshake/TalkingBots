import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Documented;
import java.util.Scanner;

public class ChatWindow {
    
    private int lines;
    private int chars;
    
    private Scanner scanner;
    
    private volatile boolean blinkingCaret;
    
    private volatile String initial;
    private volatile StyledDocument doc;
    private volatile Style style;
    
    private JTextPane text;
    private JFrame frame;
    
    public ChatWindow() {
        lines = 20;
        chars = 33;
        int frameWidth = 28 * chars + 38;
        int frameHeight = 46 * lines + 83;
        
        scanner = new Scanner(System.in);
        blinkingCaret = false;
        
        initial = "M";
        
        initializeWindow(frameWidth, frameHeight);
        asyncConsole();
        callBlinkingCarret();
    }
    
    public static void main(String[] args) {
        new ChatWindow();
    }
    
    
    private Font createFont() {
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new File("src/main/resources/font/Monaco.ttf")).deriveFont(46f);
            GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
            genv.registerFont(font);
            return font;
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private void callBlinkingCarret() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                
                blinkingCaret = true;
                
                int len = doc.getLength() - 1;
                text.setCaretPosition(len);
                try {
                    while (blinkingCaret) {
                        
                        doc.remove(len, 1);
                        for (int i = 0; i < 5; ++i) {
                            if (!blinkingCaret) {
                                break;
                            }
                            Thread.sleep(100);
                        }
                        doc.insertString(len, "▯", style);
                        for (int i = 0; i < 5; ++i) {
                            if (!blinkingCaret) {
                                break;
                            }
                            Thread.sleep(100);
                        }
                    }
                    
                    if (doc.getLength() == len) {
                        doc.insertString(len, "▯", style);
                    }
                    text.setCaretPosition(len);
                } catch (InterruptedException | BadLocationException e) {
                    e.printStackTrace();
                }
                
            }
        });
        t.start();
    }
    
    public void initializeWindow(int w, int h) {
        
        Font monaco = createFont();
        monaco.deriveFont(Font.BOLD);
        
        frame = new JFrame();
        frame.getContentPane().setBackground(Color.decode("#002344"));
        frame.setSize(w, h);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setTitle("ChatWindow");
        
        text = new JTextPane();
        text.setCaretColor(Color.decode("#002344"));
        text.setBackground(Color.decode("#002344"));
        text.setBorder(BorderFactory.createEmptyBorder());
        
        text.setFont(monaco);
        text.setForeground(Color.decode("#ffffff"));
        style = text.addStyle("Style1", null);
        StyleConstants.setForeground(style, Color.decode("#1cff1c"));
        
        JScrollPane scroll = new JScrollPane(text);
        
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        frame.add(scroll);
    
        doc = text.getStyledDocument();
    
        try {
            doc.insertString(0, initial + ":▯", style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        
        int len = doc.getLength();
        text.setCaretPosition(len);
        
        frame.setVisible(true);
    }
    
    private void asyncConsole() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("in console");
                while (true) {
                    String s = scanner.nextLine();
                    System.out.println("entered msg");
                    blinkingCaret = false;
                    try {
                        Thread.sleep(150);
                        
                        String response = "";
                        int length = s.length();
                        boolean lastLine = false;
                        for (int i = 0; i < length / 30 + 1; ++i) {
                            int lineEnd = 30;
                            if (i * 30 + lineEnd > length) {
                                lineEnd = length % 30;
                                lastLine = true;
                            }
                            response += s.substring(i * 30, i * 30 + lineEnd);
                            if (!lastLine) {
                                response += "\n  ";
                            }
                        }
                        
                        for (char c : response.toCharArray()) {
                            doc.insertString(text.getCaretPosition(), String.valueOf(c), style);
                            text.setCaretPosition(text.getCaretPosition() + 1);
                            Thread.sleep(40);
                        }
                        if (initial.equals("M")) {
                            //text.setForeground(Color.decode("#ff5e19"));
                            StyleConstants.setForeground(style, Color.decode("#ff5e19"));
                            initial = "F";
                        } else {
                            //text.setForeground(Color.decode("#0fa03a"));
                            StyleConstants.setForeground(style, Color.decode("#1cff1c"));
                            initial = "M";
                        }
                        doc.insertString(text.getCaretPosition(), "\n" + initial + ":", style);
                    } catch (InterruptedException | BadLocationException e) {
                        e.printStackTrace();
                    }
                    callBlinkingCarret();
                }
            }
        });
        t.start();
    }
}
