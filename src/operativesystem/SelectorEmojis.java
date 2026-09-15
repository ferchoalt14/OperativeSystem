package operativesystem;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/** Selector de emojis reutilizable (posts, comentarios, mensajes/DMs). */
public class SelectorEmojis {

    private static final String[] EMOJIS = {
        "😀", "😁", "😂", "🤣", "😊", "😍", "😘", "😎", "🤩", "🥳",
        "😢", "😭", "😡", "😱", "🤔", "😴", "🙄", "😅", "😉", "🥺",
        "👍", "👎", "👏", "🙌", "🙏", "💪", "👋", "✌", "🤝", "❤",
        "🔥", "✨", "🎉", "🎊", "💯", "⭐", "🌟", "💜", "💙", "💚",
        "🐶", "🐱", "🎵", "⚽", "🏀", "🍕", "🍔", "☕", "🎮", "📸"
    };

    private SelectorEmojis() {}

    /** Muestra un popup con emojis; al elegir uno, invoca alElegir con el carácter. */
    public static void mostrar(Component ancla, Consumer<String> alElegir) {
        JPopupMenu popup = new JPopupMenu();
        JPanel panel = new JPanel(new GridLayout(0, 10, 2, 2));
        panel.setBackground(TemaUI.SUPERFICIE);
        for (String emoji : EMOJIS) {
            JButton btn = new JButton(emoji);
            btn.setFont(btn.getFont().deriveFont(18f));
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> {
                alElegir.accept(emoji);
                popup.setVisible(false);
            });
            panel.add(btn);
        }
        popup.add(panel);
        popup.show(ancla, 0, ancla.getHeight());
    }

    /** Crea un botón "😊" listo para abrir el selector e insertar en el JTextComponent dado. */
    public static JButton crearBoton(javax.swing.text.JTextComponent destino) {
        JButton btnEmoji = new JButton("😊");
        btnEmoji.setFont(btnEmoji.getFont().deriveFont(16f));
        btnEmoji.setContentAreaFilled(false);
        btnEmoji.setBorderPainted(false);
        btnEmoji.setFocusPainted(false);
        btnEmoji.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEmoji.addActionListener(e -> mostrar(btnEmoji, emoji -> {
            try {
                int pos = destino.getCaretPosition();
                destino.getDocument().insertString(pos, emoji, null);
                destino.setCaretPosition(pos + emoji.length());
            } catch (Exception ex) {
                destino.setText(destino.getText() + emoji);
            }
            destino.requestFocusInWindow();
        }));
        return btnEmoji;
    }
}