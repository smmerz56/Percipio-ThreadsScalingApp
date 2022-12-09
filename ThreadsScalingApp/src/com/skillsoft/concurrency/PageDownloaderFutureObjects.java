package com.skillsoft.concurrency;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PageDownloaderFutureObjects implements Runnable{
	String[] urlsList;
	
	public PageDownloaderFutureObjects(String[] urlsList) {
		this.urlsList = urlsList;
	}
	
	@Override
	public void run() {
		String threadName = Thread.currentThread().getName();
		
		try {
			for(String urlString : urlsList) {
				
				if(Thread.currentThread().isInterrupted()) {
					throw new InterruptedException(Thread.currentThread().getName()+" interrupted");
				}
				
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
		
	}
	
	public static void main(String args[]) throws InterruptedException {
		String[] urls = new String[] {"https://stackoverflow.com/home",
										"https://stackoverflow.com/questions",
										"https://stackoverflow.com/tags",
										"https://stackoverflow.com/users", 
										"https://stackoverflow.com/jobs/companies",
										"https://stackoverflow.co/teams"};
		
		Thread downloaderOne = new Thread(new PageDownloaderFutureObjects(
						Arrays.copyOfRange(urls, 0, 3)));
		
		
		Thread downloaderTwo = new Thread(new PageDownloaderFutureObjects(
						Arrays.copyOfRange(urls, 3, urls.length)));
		
		ExecutorService executorService = Executors.newFixedThreadPool(2);
		
		long startTime = System.currentTimeMillis();
		
		Future fOne = executorService.submit(downloaderOne);//Called a Future object because it is possible for us to synchronize a thread based on the result it is expected to deliver in the future
		Future fTwo = executorService.submit(downloaderTwo);
		
		int checkCount = 0;
		
		while(true) {//this is the main thread that is checking in the while loop
			checkCount++;
			
			if(checkCount > 3) {
				fOne.cancel(true);//even if a thread is being executed, an interrupt will be issued
				fTwo.cancel(true);
				
				System.out.println("The downloaders have been cancelled!");
				break;
			}
			
			if(fOne.isDone() && fTwo.isDone()) {
				System.out.println("The downloaders are DONE!");
				break;
			}
			
			System.out.println("Check #"+ checkCount +": Downloaders are still on...");
			Thread.sleep(2000);
		}
		
		executorService.shutdown();
		while(!executorService.isTerminated()) {
			Thread.sleep(1000);
		}
		long endTime = System.currentTimeMillis();
		
		System.out.println("Total time taken: "+(endTime-startTime)/1000 + "s");
		
	}

}
