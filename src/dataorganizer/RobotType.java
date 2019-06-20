package dataorganizer;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

/**
 * This class is responsible for the emulation of keystrokes, ultimately to write data onto a spreadsheet.
 */

public class RobotType {

	private Robot robot;
	
	public RobotType() {
		try {
			robot = new Robot();
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	public void openAndRefreshTemplate(String excelTemplateLocation, boolean hideWindow) {
		openWorkbook(excelTemplateLocation);
		robot.delay(20000);	//Delay for opening the excel workbook
		goToFirstSheet();
		refreshSheet();		
		nextDataSheet();
		refreshSheet();
		nextDataSheet();	
		if(hideWindow) {
			minimizeWindow();
		}
	}
	

	public void openAndRefreshTwoModuleTemplate(String excelTemplateLocation, boolean hideWindow) {
		openWorkbook(excelTemplateLocation);
		robot.delay(20000);	//Delay for opening the excel workbook
		goToFirstSheet();
		refreshSheet();
		nextDataSheet();
		refreshSheet();
		nextDataSheet();
		refreshSheet();
		nextDataSheet();
		refreshSheet();
		goToMomentum();
		if(hideWindow) {
			minimizeWindow();
		}
	}
	
	public void goToMomentum() {
		goToFirstSheet();
		for(int i = 0 ; i < 10; i++) {
			nextDataSheet();
		}
	}
	
	public void openWorkbook(String excelTemplateLocation) {
		try {
			Desktop.getDesktop().open(new File(excelTemplateLocation));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public void goToFirstSheet() {
		for(int i = 0; i < 14; i++) {
			 robot.keyPress(KeyEvent.VK_CONTROL);
			 robot.keyPress(KeyEvent.VK_PAGE_UP);
			 robot.keyRelease(KeyEvent.VK_CONTROL);
			 robot.keyRelease(KeyEvent.VK_PAGE_UP);
		}
	}
	
	public void minimizeWindow() {
		robot.keyPress(KeyEvent.VK_ALT);
		robot.keyPress(KeyEvent.VK_SPACE);
		robot.keyPress(KeyEvent.VK_N);
		robot.keyRelease(KeyEvent.VK_ALT);
		robot.keyRelease(KeyEvent.VK_SPACE);
		robot.keyRelease(KeyEvent.VK_N);
	}
	
	public void nextDataSheet() {
		 robot.keyPress(KeyEvent.VK_CONTROL);
		 robot.keyPress(KeyEvent.VK_PAGE_DOWN);
		 robot.keyRelease(KeyEvent.VK_CONTROL);
		 robot.keyRelease(KeyEvent.VK_PAGE_DOWN);
	}
	
	public void refreshSheet() {																				//Initial delay for application startup
		 robot.keyPress(KeyEvent.VK_CONTROL);																//Press CTRL
		 robot.keyPress(KeyEvent.VK_A);																		//Press A
		 robot.delay(100);																					//Make sure Excel has all selected
		 robot.keyRelease(KeyEvent.VK_CONTROL);																//Release CTRL
		 robot.keyRelease(KeyEvent.VK_A);																	//Release A
		 robot.keyPress(KeyEvent.VK_DELETE);																//Press Delete
		 robot.keyRelease(KeyEvent.VK_DELETE);																//Release Delete
		 robot.delay(100);																					//Make sure Excel undoes
		 robot.keyPress(KeyEvent.VK_CONTROL);																//Press CTRL
		 robot.keyPress(KeyEvent.VK_Z);																		//Press Z
		 robot.delay(100);																					//Make sure Excel undoes
		 robot.keyRelease(KeyEvent.VK_CONTROL);																//Release CTRL
		 robot.keyRelease(KeyEvent.VK_Z);																	//Release Z
		 try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
