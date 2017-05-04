package net.rs.lamsi.massimager.Frames.FrameWork;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

public class JFontBox extends JComboBox<String> {

	public JFontBox() {
		super(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());

		setSelectedItem("Arial");
		setRenderer(new FontBoxRenderer(this));
		addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    final String fontName = getSelectedItem().toString();
                    setFont(new Font(fontName, Font.PLAIN, 14));
                }
            }
        });
	}

	
	
}
