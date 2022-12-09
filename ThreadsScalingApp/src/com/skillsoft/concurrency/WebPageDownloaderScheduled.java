package com.skillsoft.concurrency;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WebPageDownloaderScheduled implements Callable<Long>{
	String[] urlsList;
	
	public WebPageDownloaderScheduled(String[] urlsList) {
		this.urlsList = urlsList;
	}
	@Override
	public Long call() throws Exception {
		long startTime = System.currentTimeMillis();
		String threadName = Thread.currentThread().getName();
		
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
		long endTime = System.currentTimeMillis();
		
		return endTime - startTime;
	}
	

	public static void main(String args[]) throws InterruptedException {
		String[] urls = new String[] {"https://stackoverflow.com/home",
										"https://stackoverflow.com/questions",
										"https://stackoverflow.com/tags",
										"https://stackoverflow.com/users", 
										"https://stackoverflow.com/jobs/companies",
										"https://stackoverflow.co/teams"};
		
		ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
		
		Callable<Long> downloaderOne = new WebPageDownloaderCallable(Arrays.copyOfRange(urls, 0, 3));//Instead of Thread use a Callable if there is a return value
		Callable<Long> downloaderTwo = new WebPageDownloaderCallable(Arrays.copyOfRange(urls, 3, urls.length));
		
		Future<Long> fOne = executorService.schedule(downloaderOne, 30, TimeUnit.SECONDS);
		Future<Long> fTwo = executorService.schedule(downloaderTwo, 40, TimeUnit.SECONDS);
		
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
