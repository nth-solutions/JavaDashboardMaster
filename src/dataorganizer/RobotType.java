package dataorganizer;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

public class RobotType {

	
	public void openAndRefreshTemplate(String excelTemplateLocation) {
		
		try {
			Desktop.getDesktop().open(new File(excelTemplateLocation));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Robot robot=null;
		 
		try {
			robot = new Robot();
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		//Robot to clear flags in excel and update cells
		 robot.delay(15000);																					//Initial delay for application startup
		 robot.keyPress(KeyEvent.VK_LEFT);
		 robot.keyPress(KeyEvent.VK_ENTER);
		 robot.delay(200);
		 robot.keyRelease(KeyEvent.VK_LEFT);
		 robot.keyRelease(KeyEvent.VK_ENTER);
		 robot.delay(500);
		 robot.keyPress(KeyEvent.VK_ENTER);
		 robot.keyRelease(KeyEvent.VK_ENTER);
		 robot.delay(2000);
		 robot.keyPress(KeyEvent.VK_CONTROL);																//Press CTRL
		 robot.keyPress(KeyEvent.VK_A);																		//Press A
		 robot.delay(2500);																					//Make sure Excel has all selected
		 robot.keyRelease(KeyEvent.VK_CONTROL);																//Release CTRL
		 robot.keyRelease(KeyEvent.VK_A);																	//Release A
		 robot.keyPress(KeyEvent.VK_DELETE);																//Press Delete
		 robot.keyRelease(KeyEvent.VK_DELETE);																//Release Delete
		 robot.delay(2500);																					//Make sure Excel undoes
		 robot.keyPress(KeyEvent.VK_CONTROL);																//Press CTRL
		 robot.keyPress(KeyEvent.VK_Z);																		//Press Z
		 robot.delay(2500);																					//Make sure Excel undoes
		 robot.keyRelease(KeyEvent.VK_CONTROL);																//Release CTRL
		 robot.keyRelease(KeyEvent.VK_Z);																	//Release Z
		 try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		
		}
																								//Delete the outputFile from disk (Held in ram by Excel)
}
	
}
