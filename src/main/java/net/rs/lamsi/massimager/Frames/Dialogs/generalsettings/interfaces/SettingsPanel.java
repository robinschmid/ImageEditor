package net.rs.lamsi.massimager.Frames.Dialogs.generalsettings.interfaces;
 
import net.rs.lamsi.massimager.Settings.Settings;
import net.rs.lamsi.massimager.Settings.SettingsHolder;

public interface SettingsPanel {

	/*
	 * Save all settings to SettingsObject
	 */
	public void setAllSettings(SettingsHolder settings);
	/*
	 * Apply all settings to all panels
	 */
	public void setAllSettingsOnPanel(SettingsHolder settings);
	
	/*
	 * Returns the used settings object
	 */
	public Settings getSettings(SettingsHolder settings);
}
