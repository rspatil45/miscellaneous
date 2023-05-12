import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;


public class EncryptDecryptApp extends JFrame implements ActionListener {

    private final JTextArea textArea;
    private final JButton encryptButton;
    private final JButton decryptButton;
    private final JFileChooser fileChooser;

    public EncryptDecryptApp() {
        super("Encrypt/Decrypt Utility by rspatil45");
        // set window size
        setSize(500, 500);

        // set layout
        setLayout(new BorderLayout());

        // add text area
        textArea = new JTextArea();
        textArea.setEditable(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        // add encrypt button
        encryptButton = new JButton("Encrypt");
        encryptButton.addActionListener(this);
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.add(encryptButton);

        // add decrypt button
        decryptButton = new JButton("Decrypt");
        decryptButton.addActionListener(this);
        buttonPanel.add(decryptButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // set up file chooser
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == encryptButton) {
            // ask for encryption key
            String key = JOptionPane.showInputDialog(this, "Enter encryption key:");

            if (key != null) {
                try {
                    // create cipher
                    Cipher cipher = createCipher(Cipher.ENCRYPT_MODE, key);

                    // encrypt data
                    byte[] encryptedData = cipher.doFinal(textArea.getText().getBytes(StandardCharsets.UTF_8));

                    // save encrypted data to file
                    saveToFile(encryptedData);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Encryption failed: " + ex.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (e.getSource() == decryptButton) {
            // show open dialog
            int result = fileChooser.showOpenDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();

                // ask for decryption key
                String key = JOptionPane.showInputDialog(this, "Enter decryption key:");

                if (key != null) {
                    try {
                        // read encrypted data from file
                        byte[] encryptedData = readFromFile(selectedFile);

                        // create cipher
                        Cipher cipher = createCipher(Cipher.DECRYPT_MODE, key);

                        // decrypt data
                        byte[] decryptedData = cipher.doFinal(encryptedData);

                        // show decrypted data in text area
                        textArea.setText(new String(decryptedData, StandardCharsets.UTF_8));
                    } catch (BadPaddingException ex) {
                        JOptionPane.showMessageDialog(this, "Wrong decryption key!", "Error",
                                JOptionPane.ERROR_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Decryption failed: " + ex.getMessage(), "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    private Cipher createCipher(int mode, String key) throws Exception {
        // create 128-bit key from user's input
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        byte[] keyBytes = Arrays.copyOf(sha.digest(key.getBytes(StandardCharsets.UTF_8)), 16);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");

        // create initialization vector
        byte[] iv = new byte[16];
        IvParameterSpec ivParams = new IvParameterSpec(iv);

        // create cipher instance
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(mode, secretKeySpec, ivParams);

        return cipher;
    }

    private byte[] readFromFile(File file) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
        }
        return baos.toByteArray();
    }

    private void saveToFile(byte[] data) throws IOException {
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try (FileOutputStream fos = new FileOutputStream(selectedFile)) {
                fos.write(data);
            }
        }
    }

    public static void main(String[] args) {
        EncryptDecryptApp app = new EncryptDecryptApp();
        app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        app.setVisible(true);
    }
}
