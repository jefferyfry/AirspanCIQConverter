package com.sprint.airspan.AirspanCIQConverter.xls;

public interface CIQConverterListener {
	
	public void start();
	
	public void progress(int progress, int total);
	
	public void finished();
	
	public void exception(Exception e);

}
