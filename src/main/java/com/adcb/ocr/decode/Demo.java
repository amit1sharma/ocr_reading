package com.adcb.ocr.decode;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

/**
 * Demo Swing application, demonstrates simple MRZ parsing.
 */
public class Demo {

    private static int toPos(int col, int row, String text) {
        int currentRow = 0;
        int currentCol = 0;
        int pos = 0;
        while (text.length() > pos) {
            if (row == currentRow && currentCol == col) {
                return pos;
            }
            if (text.charAt(pos) == '\n') {
                currentRow++;
                currentCol = 0;
            } else {
                currentCol++;
            }
            pos++;
        }
        return -1;
    }

    /**
     * MRZ demo.
     * @param args 
     */
    public static void main(String[] args) {
        final JFrame frame = new JFrame("MRZDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final JTextArea mrz = new JTextArea(5, 44);
        final JButton parse = new JButton("Parse");
        parse.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                final String m = mrz.getText();
                try {
                    final MrzRecord record = MrzParser.parse(m);
                    JOptionPane.showMessageDialog(frame, "Parse successfull: " + record);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Parse failed: " + ex);
                    if (ex instanceof MrzParseException) {
                        final MrzParseException mpe = (MrzParseException) ex;
                        final MrzRange r = mpe.range;
                        mrz.select(toPos(r.column, r.row, m), toPos(r.columnTo, r.row, m));
                    }
                }
            }
        });
        frame.getContentPane().add(mrz, BorderLayout.CENTER);
        frame.getContentPane().add(parse, BorderLayout.SOUTH);
        frame.pack();
        frame.setVisible(true);
    }
}
