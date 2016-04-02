package com.org.news;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class InvertedIndex {

	private String[] myDocs;
	private File[] fileList;
	ArrayList<String> stopWords = new ArrayList<String>();
	ArrayList<String> termList = new ArrayList<String>();
	ArrayList<ArrayList<Integer>> docLists = new ArrayList<ArrayList<Integer>>();

	public InvertedIndex(String directory) {
		File folder = new File(directory);
		fileList = folder.listFiles();
		// get the string List
		myDocs = new String[fileList.length];
		for (int i = 0; i < fileList.length; i++) {
			myDocs[i] = fileList[i].getName();
			System.out.println("File: " + fileList[i].getName() + " has docID number as " + (i + 1));
		}
	}

	public void getStopWords() throws FileNotFoundException {
		File temp = new File("src/stopwords.txt");
		Scanner scn = new Scanner(temp);

		// Adding all stopwords to a data structure (Arraylist)
		while (scn.hasNextLine()) {
			stopWords.add(scn.nextLine());
		}
		scn.close();
		// get the stop words and sort them
		Collections.sort(stopWords);
	}

	public void createInvertedIndex(ArrayList<String> stemmed, File file, int m) {
		for (String word : stemmed) {
			ArrayList<Integer> docList;
			if (!termList.contains(word)) {// a new term
				termList.add(word);
				docList = new ArrayList<Integer>();
				docList.add(m);
				docLists.add(docList);
			} else { // an existing term
				int index = termList.indexOf(word);
				docList = docLists.get(index);
				if (!docList.contains(m)) {
					docList.add(m);
				}
			}
		}
	}

	public File getFile(int i) {

		return fileList[i];
	}

	public ArrayList<String> readFile(File file) throws IOException, FileNotFoundException {

		ArrayList<String> tokenList = new ArrayList<String>();
		ArrayList<String> stemming = new ArrayList<String>();
		String[] tokens;

		// Read all the lines in the current file and convert it to lower case
		Scanner scan = new Scanner(file);
		String allLines = new String();
		while (scan.hasNextLine()) {
			allLines += scan.nextLine().toLowerCase(); // converting to
														// lowercase
		}
		scan.close();
		System.out.println(allLines + "\n");
		// define tokens
		tokens = allLines.split("[ '&.;,:$#+?%!()\\-\"\\*]+");

		// Loop over the tokens and add the words to a tokenList
		for (String token : tokens) {
			if (searchStopWord(token) < 0) {
				tokenList.add(token);
			}
		}

		// Invoking Porter's stemmer to perform stemming
		Stemmer stem = new Stemmer();
		for (String token : tokenList) {
			stem.add(token.toCharArray(), token.length());
			stem.stem();
			stemming.add(stem.toString());
			stem = new Stemmer();
		}

		return stemming;
	}

	public String toString() {
		String outString = new String();
		for (int i = 0; i < termList.size(); i++) {
			outString += String.format("%-15s", termList.get(i));
			ArrayList<Integer> docList = docLists.get(i);
			for (int j = 0; j < docList.size(); j++) {
				outString += docList.get(j) + "\t";
			}
			outString += "\n";
		}
		return outString;
	}

	public int searchStopWord(String key) {
		// defining data structure to search the stopwords
		return Collections.binarySearch(stopWords, key);
		
	}

	public ArrayList<Integer> search(String query) {
		int index = termList.indexOf(query);
		if ((index >= 0) && (index <= termList.size())) {
			return docLists.get(index);
		} else
			return null;
	}

	public ArrayList<Integer> search(String[] query) {
		ArrayList<Integer> result = search(query[0]);
		
		for(int t=1; t< query.length;t++){
			ArrayList<Integer> result1 = search(query[t]);
			result = union(result, result1);
		}
		return result;
	}

	public ArrayList<Integer> searchForCombinedWords(String[] words) {
		int x = words.length;
		String temp;
		String[] combined = words;

		for (int i = 0; i < x; i++) {
			for (int j = 1; j < (x - i); j++) {
				String y = combined[j - 1];
				String z = combined[j];
				// System.out.println("**************" + termList + "\n");
				if (docLists.get(termList.indexOf(y)).size() > docLists.get(termList.indexOf(z)).size()) {
					temp = combined[j - 1];
					combined[j - 1] = combined[j];
					combined[j] = temp;
				}
			}
		}

		System.out.println(x + " keywords are combined in the order of");
		for (int i = 0; i < combined.length; i++) {
			System.out.println((i + 1) + " " + combined[i]);
		}

		ArrayList<Integer> result = search(combined[0]);
		
		
		for(int t=1; t<words.length;t++){
			ArrayList<Integer> result1 = search(combined[t]);
			result = merge(result, result1);
		}
		return result;
	}

	public ArrayList<Integer> merge(ArrayList<Integer> l1, ArrayList<Integer> l2) {
		ArrayList<Integer> mergeList = new ArrayList<Integer>();
		int id1 = 0, id2 = 0;
		while (id1 < l1.size() && id2 < l2.size()) {
			if (l1.get(id1).intValue() == l2.get(id2).intValue()) {
				mergeList.add(l1.get(id1));
				id1++;
				id2++;
			} else if (l1.get(id1).intValue() < l2.get(id2).intValue())
				id1++;
			else
				id2++;
		}
		return mergeList;
	}

	public ArrayList<Integer> union(ArrayList<Integer> l1, ArrayList<Integer> l2) {
		ArrayList<Integer> unionList = new ArrayList<Integer>();
		int id1 = 0;
		for (int i = 0; i < l1.size(); i++) {
			unionList.add(l1.get(id1));
		}
		return unionList;
	}

	public ArrayList<Integer> searchForUnionWords(String[] word) {
		ArrayList<Integer> searchResult = search(word[0]);
		int temp = 1;
		while (temp < word.length) {
			ArrayList<Integer> result1 = search(word[temp]);
			searchResult = union(searchResult, result1);
			temp++;
		}
		return searchResult;
	}

	public static void main(String[] args) {
		InvertedIndex inv = new InvertedIndex("src/NewsFiles");

		try {
			inv.getStopWords();
			for (int i = 0; i < 5; i++) {
				File file = inv.getFile(i);
				// System.out.println(file.getName());

				ArrayList<String> stemmed = inv.readFile(file);

				inv.createInvertedIndex(stemmed, file, i);
			}
			System.out.println(inv);

			/*
			 * Query 1 : 1. Implement a search algorithm that can handle a query
			 * with a single keyword. Design two test cases and show the
			 * document names as the search result.
			 */
			// TEST CASE 1:

			String searchWord1 = "sweet";
			ArrayList<Integer> s1 = inv.search(searchWord1);
			System.out
					.println("Test case 1 : Word being searched '" + searchWord1 + "' can be found in document " + s1);

			for (int i = 0; i < s1.size(); i++) {
				System.out.println(s1.get(i) + "\t" + inv.fileList[i].getName() + "\n");

			}

			// TEST CASE 2:
			String searchWord2 = "natur";
			s1 = inv.search(searchWord2);
			System.out
					.println("Test Case 1 : Word being searched '" + searchWord2 + "' can be found in document " + s1);

			for (int i = 0; i < s1.size(); i++) {
				System.out.println(s1.get(i) + "\t" + inv.fileList[i].getName() + "\n");

			}

			/*
			 * 2.Implement a search algorithm that can handle a query with two
			 * keywords. Assume that query terms are connected using the AND
			 * operator.
			 */
			// TEST CASE 1:
			String[] set1 = { "danger", "live" };
			String[] set2 = { "film", "type" };
			ArrayList<Integer> s3 = inv.searchForCombinedWords(set1);

			System.out.println("Intersection output:" + set1[0] + " AND  " + set1[1] + "\n");
			for (int i = 0; i < s3.size(); i++) {
				System.out.println("\t" + s3.get(i) + "\t" + inv.fileList[i].getName() + "\n");

			}
			// TEST CASE 2:
			ArrayList<Integer> s4 = inv.searchForCombinedWords(set2);

			System.out.println("Intersection output:" + set2[0] + " AND  " + set2[1] + "\n");
			for (int i = 0; i < s4.size(); i++) {
				// System.out.println(s4);
				System.out.println("\t" + s4.get(i) + "\t" + inv.fileList[i].getName() + "\n");

			}

			/*
			 * 3. Implement a search algorithm that can handle a query with two
			 * keywords. Assume that query terms are connected using the OR
			 * operator.
			 */
			// TEST CASE 1:
			String[] set13 = { "danger", "deriv" };
			String[] set14 = { "cast", "valid" };
			ArrayList<Integer> s5 = inv.searchForUnionWords(set13);

			System.out.println("Output:" + set13[0] + " OR  " + set13[1] + "\n");
			for (int i = 0; i < s5.size(); i++) {

				System.out.println(s5.get(i) + "\t" + inv.fileList[i].getName() + "\n");

			}

			// TEST CASE 2:
			s5 = inv.searchForUnionWords(set14);

			System.out.println("Output:" + set14[0] + " OR  " + set14[1] + "\n");
			for (int i = 0; i < s5.size(); i++) {

				System.out.println(s5.get(i) + "\t" + inv.fileList[i].getName() + "\n");

			}
			
			/*
			 * 
			 */
			 String[] s115 = {"norm","talent","appear"}; 
		       
		      ArrayList<Integer> result = inv.searchForCombinedWords(s115);
		       System.out.println("Search Query for : " +s115[0] +" AND "+ s115[1]+" AND "+s115[2]+ "\n");
		       for( int i = 0;i<result.size();i++){	
		    	   System.out.println(result.get(i)+ "\t"+inv.fileList[i].getName()+"\n");  
		    	   
		       }
		       
		       
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

}
