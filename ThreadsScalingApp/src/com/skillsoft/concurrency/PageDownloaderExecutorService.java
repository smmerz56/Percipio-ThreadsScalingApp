package com.skillsoft.concurrency;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PageDownloaderExecutorService implements Runnable{
	String[] urlsList;
	CountDownLatch latch;
	
	public PageDownloaderExecutorService(String[] urlsList, CountDownLatch latch) {
		this.urlsList = urlsList;
		this.latch = latch;
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
		latch.countDown();
	}
	
	public static void main(String args[]) throws InterruptedException {
		String[] urls = new String[] {"https://stackoverflow.com/home",
										"https://stackoverflow.com/questions",
										"https://stackoverflow.com/tags",
										"https://stackoverflow.com/users", 
										"https://stackoverflow.com/jobs/companies",
										"https://stackoverflow.co/teams"};
		
		int maxThreads = 3;//wait for this many tasks to complete their execution. Only this many threads can run concurrently until control goes back to the main thread
		CountDownLatch latch = new CountDownLatch(maxThreads);
		ExecutorService executorService = Executors.newFixedThreadPool(maxThreads);//argument is # of workers in the pool
//		ExecutorService executorSerice = Executors.newSingleThreadExecutor();
		
		long startTime = System.currentTimeMillis();
		
		for(String url : urls) {
			
			Thread downloader = new Thread(new PageDownloaderExecutorService(new String[] {url}, latch));
			executorService.submit(downloader);//argument is runnable or callable task

		}
		
		latch.await();
//		executorSerice.awaitTermination(15, TimeUnit.SECONDS);//
		executorService.shutdownNow();//terminates everything immediately, if thread hasnt started yet
//		executorService.shutdown();//Graceful shutdown. Initiate the shutdown but doesnt terminate until the last remaining thread has been processed
		while(!executorService.isTerminated()) {//will not return to main thread until all threads have completed
			Thread.sleep(1000);
		}
		
		long endTime = System.currentTimeMillis();
		
		System.out.println("Total time taken: "+(endTime-startTime)/1000 + "s");
			
	}
}
 