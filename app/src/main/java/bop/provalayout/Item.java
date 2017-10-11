package bop.provalayout;

public class Item implements Comparable<Item>{
	private String name;
	private String data;
	private String date;
	private String path;
	private String file_image;
	private boolean m_isFile=true, isBack=false, isUp=false;
	
	public Item(String n,String d, String dt, String p, Boolean isFile, String file_img, String function_descr)
	{
		name = n;
		data = d;
		date = dt;
		path = p; 
		file_image = file_img;
		m_isFile = isFile;
		if(function_descr.toUpperCase().equals("BACK")) isBack = true;
		else isBack = false;
		if(function_descr.toUpperCase().equals("UP")) isUp = true;
		else isUp = false;
		
	}
	public String getName()
	{
		return name;
	}
	public String getData()
	{
		return data;
	}
	public String getDate()
	{
		return date;
	}
	public String getPath()
	{
		return path;
	}
	public String getFileImage() {
		return file_image;
	}
	public boolean isFile() {
		return m_isFile;
	}
	public boolean isBack() {
		return isBack;
	}
	public boolean isUp() {
		return isUp;
	}
	
	public int compareTo(Item o) {
		if(this.name != null)
			return this.name.toLowerCase().compareTo(o.getName().toLowerCase());
		else
			throw new IllegalArgumentException();
	}
}
