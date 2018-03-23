package application;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javafx.scene.control.CheckBox;

public class TaskEntry {

	private String date;
	private String title;
	private String percent;
	private String description;

	
	public TaskEntry(String date, String title, String percent, String description){
		this.date = date;
		this.title = title;
		this.percent = percent;
		this.description = description;
	}
	
	public CheckBox getTaskFinished(){
		CheckBox cb = new CheckBox();
		// With this lines, we set the checkbox as no modifiable
		cb.setOnAction(value->{
			cb.setSelected(Integer.parseInt(percent) == 100);
		});
		
		// Return true if the task is completed
		cb.setSelected(Integer.parseInt(percent) == 100);
		return cb;
	}
	
	public String getDate(){
		return date;
	}
	
	public String getTitle(){
		return title;
	}
	
	public String getPercent(){
		return percent;
	}
	
	public String getDescription(){
		return description;
	}
	
	/** Check if the task is overdue */
	public boolean isOverdue() {
		DateFormat dateFormat = new SimpleDateFormat(Main.DATE_FORMAT);
		Calendar taskDate = new GregorianCalendar();
		try {
			taskDate.setTime(dateFormat.parse(date));
		} catch (ParseException e) {
			return false;
		}
		Calendar today  = new GregorianCalendar();
		
		if(today.get(Calendar.YEAR) > taskDate.get(Calendar.YEAR)) {
			return true;
		}else if(today.get(Calendar.YEAR) == taskDate.get(Calendar.YEAR)) {
			if(today.get(Calendar.MONTH) > taskDate.get(Calendar.MONTH)) {
				return true;
			}else if(today.get(Calendar.MONTH) == taskDate.get(Calendar.MONTH)) {
				if(today.get(Calendar.DAY_OF_MONTH) > taskDate.get(Calendar.DAY_OF_MONTH)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	/** Check if the task date is today */
	public boolean isForToday() {
		DateFormat dateFormat = new SimpleDateFormat(Main.DATE_FORMAT);
		Calendar taskDate = new GregorianCalendar();
		try {
			taskDate.setTime(dateFormat.parse(date));
		} catch (ParseException e) {
			return false;
		}
		Calendar today  = new GregorianCalendar();
		
		return (today.get(Calendar.YEAR) == taskDate.get(Calendar.YEAR) &&
				today.get(Calendar.MONTH) == taskDate.get(Calendar.MONTH) &&
				today.get(Calendar.DAY_OF_MONTH) == taskDate.get(Calendar.DAY_OF_MONTH));
		
	}
	
	/** Check if the task date is in the same week as today */
	public boolean isForThisWeek() {
		DateFormat dateFormat = new SimpleDateFormat(Main.DATE_FORMAT);
		Calendar taskDate = new GregorianCalendar();
		try {
			taskDate.setTime(dateFormat.parse(date));
		} catch (ParseException e) {
			return false;
		}
		Calendar today  = new GregorianCalendar();
		return (today.get(Calendar.YEAR) == taskDate.get(Calendar.YEAR) &&
				today.get(Calendar.MONTH) == taskDate.get(Calendar.MONTH) &&
				today.get(Calendar.WEEK_OF_MONTH) == taskDate.get(Calendar.WEEK_OF_MONTH));
	}
	
}
