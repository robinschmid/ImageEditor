package net.rs.lamsi.multiimager.FrameModules;


import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import net.rs.lamsi.general.datamodel.image.ImageOverlay;
import net.rs.lamsi.massimager.Frames.FrameWork.modules.SettingsModuleContainer;
import net.rs.lamsi.massimager.Settings.image.SettingsImage2D;
import net.rs.lamsi.massimager.Settings.image.SettingsImageOverlay;
import net.rs.lamsi.multiimager.FrameModules.sub.ModuleBackgroundImg;
import net.rs.lamsi.multiimager.FrameModules.sub.ModulePaintscaleOverlay;
import net.rs.lamsi.multiimager.FrameModules.sub.ModuleThemes;
import net.rs.lamsi.multiimager.FrameModules.sub.ModuleZoom;
import net.rs.lamsi.multiimager.Frames.ImageEditorWindow;

public class ModuleImageOverlay extends SettingsModuleContainer<SettingsImageOverlay, ImageOverlay> {
	private ImageEditorWindow window;

	private ModuleZoom moduleZoom;
	private ModuleThemes moduleThemes;
	private ModuleBackgroundImg moduleBG;
	// 
	private ModulePaintscaleOverlay modulePaintscale;
	
	/**
	 * Create the panel.
	 */
	public ModuleImageOverlay(ImageEditorWindow wnd) {
		super("", false, SettingsImageOverlay.class, ImageOverlay.class, true);    
		window = wnd;

		JButton btnApplySettingsToAll = new JButton("apply to all");
		btnApplySettingsToAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				window.getLogicRunner().applySettingsToAllImagesInList();
			}
		});
		getPnTitleCenter().add(btnApplySettingsToAll);

		JButton btnUpdate = new JButton("update");
		btnUpdate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				window.writeAllSettingsFromModules(false);
			}
		});
		getPnTitleCenter().add(btnUpdate);

		moduleZoom = new ModuleZoom();
		addModule(moduleZoom);
		
		modulePaintscale = new ModulePaintscaleOverlay();
		addModule(modulePaintscale);

		moduleBG = new ModuleBackgroundImg();
		addModule(moduleBG);
		
		moduleThemes = new ModuleThemes();
		addModule(moduleThemes);


		// add all sub modules for settings TODO add all mods
//		listSettingsModules.addElement(moduleGeneral.getModSplitConImg());
	}
	
	//################################################################################################
	// GETTERS AND SETTERS  

	public ModuleZoom getModuleZoom() {
		return moduleZoom;
	}
	public ModulePaintscaleOverlay getModulePaintscale() {
		return modulePaintscale;
	}
	public ModuleThemes getModuleThemes() {
		return moduleThemes;
	}
	public ModuleBackgroundImg getModuleBackground() {
		return moduleBG;
	}

}
