package com.skillsoft.concurrency;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class WebPageDownloaderRecurringScheduled implements Runnable{
	String[] urlsList;
	
	public WebPageDownloaderRecurringScheduled(String[] urlsList) {
		this.urlsList = urlsList;
	}
	
	@Override
	public void run() {
		String threadName = Thread.currentThread().getName();
		System.out.println(threadName+" has STARTED a run");
		try {
			for(String urlString : urlsList) {

				URL url = new URL(urlString);
				String fileName = urlString.substring(urlString.lastIndexOf("/")+1).trim() + ".html";
				BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
				BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
				
				String line;
				while((line = reader.readLine()) != null) {
					writer.write(line);
				}
				System.out.println(threadName+" has downloaded " + fileName);
				writer.close();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println(threadName+" has COMPLETED a run");
	}
	

	public static void main(String args[]) throws InterruptedException {
		String[] urls = new String[] {"https://stackoverflow.com/home",
										"https://stackoverflow.com/questions",
										"https://stackoverflow.com/tags",
										"https://stackoverflow.com/users", 
										"https://stackoverflow.com/jobs/companies",
										"https://stackoverflow.co/teams"};
		
		ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
		
		Runnable downloaderOne = new WebPageDownloaderRecurringScheduled(Arrays.copyOfRange(urls, 0, 3));//Instead of Thread use a Callable if there is a return value
		Runnable downloaderTwo = new WebPageDownloaderRecurringScheduled(Arrays.copyOfRange(urls, 3, urls.length));
		
//		ScheduledFuture fOne = executorService.scheduleWithFixedDelay(downloaderOne, 10, 60, TimeUnit.SECONDS);// first arg is the delay to start.second is the gap from the ending of one run to the beginning of the next
//		ScheduledFuture fTwo = executorService.scheduleWithFixedDelay(downloaderTwo, 15, 60, TimeUnit.SECONDS);
		
		ScheduledFuture fOne = executorService.scheduleAtFixedRate(downloaderOne, 10, 60, TimeUnit.SECONDS);//second arg is the start of the next run after the first has started
		ScheduledFuture fTwo = executorService.scheduleAtFixedRate(downloaderTwo, 15, 60, TimeUnit.SECONDS);//if the first run takes longer to complete than the second args time, then it will wait
		
		System.out.println("The jobs have been scheduled");
		
		long startTime = System.currentTimeMillis();
		
		try {
			
			System.out.println("Exec time for downloaderOne: "+fOne.get());
			System.out.println("Exec time for downloaderTwo: "+fTwo.get());
			
		}catch(ExecutionException e) {
			e.printStackTrace();
		}
		
		long endTime = System.currentTimeMillis();
		
		System.out.println("Elapsed time since scheduling:"+(endTime-startTime));
		
		executorService.shutdown();
	}

}
