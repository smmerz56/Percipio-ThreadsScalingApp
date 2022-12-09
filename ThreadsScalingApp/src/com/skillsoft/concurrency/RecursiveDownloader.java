package com.skillsoft.concurrency;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

// Fork/Join example
// Fork/Join task is similar to a Thread but it is more lightweight

@SuppressWarnings("serial")
public class RecursiveDownloader extends RecursiveAction{//if need to return something, then use ReccursiveTask instead
	
	String[] urlsList;
	private static final int THRESHOLD = 3;
	
	public RecursiveDownloader(String[] urlsList) {
		this.urlsList = urlsList;
	}

	@Override
	protected void compute() {
		if(urlsList.length > THRESHOLD) {
			ForkJoinTask.invokeAll(createSubTasks());
		}else {
			download(urlsList);
		}
	}
	
	private List<RecursiveDownloader> createSubTasks(){//Divide phase
		
		List<RecursiveDownloader> subtasks = new ArrayList<>();
		
		String[] firstSet = Arrays.copyOfRange(urlsList, 0, urlsList.length / 2);
		String[] secondSet = Arrays.copyOfRange(urlsList, urlsList.length / 2, urlsList.length);
		
		subtasks.add(new RecursiveDownloader(firstSet));//these are where the recursion happens
		subtasks.add(new RecursiveDownloader(secondSet));
		
		return subtasks;
	}
	
	public void download(String[] urlsList) {
		String threadName = Thread.currentThread().getName();
		System.out.println(threadName+" has STARTED!");
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
		System.out.println(threadName+" has FINISHED!");
	}
	
	public static void main(String args[]) throws InterruptedException {
		String[] urls = new String[] {"https://stackoverflow.com/home",
										"https://stackoverflow.com/questions",
										"https://stackoverflow.com/tags",
										"https://stackoverflow.com/users", 
										"https://stackoverflow.com/jobs/companies",
										"https://stackoverflow.co/teams"};
		
		RecursiveDownloader task = new RecursiveDownloader(urls);
		
		ForkJoinPool pool = new ForkJoinPool();//if you dont pass a value to the constructor, the degree of parallelism that can be achieved is determined by the number of available processes on your system 
		pool.invoke(task);
	}
}
