// Author: hao-ting chang
// Title: NLP hw1
//
//
// Time: 2019/09

// import librarys
import java.util.*;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

class Ngrams{

	private static String filePath = "brown_corpus_reviews.txt";
	private static String firstSentence = "Milstein is a gifted violinist who creates all sorts of sounds and arrangements";
	private static String secondSentence = "It was a strange and emotional thing to be at the opera on a Friday night";
	

	public static void main(String[] args) {
		boolean biGram = setBiGram(args);
		boolean addOne = setAddOne(args);
		
		// read corpos
		Map<String, Integer> singleCount = new HashMap();
		Map<String, Integer> gramCount = new HashMap();
		readfile(biGram, addOne, singleCount, gramCount);
	
		// TODO: calculate probability
		String[] firstS = firstSentence.split("\\s+");
		String[] secondS = secondSentence.split("\\s+");
		firstS = toLowerCase(firstS);
		secondS = toLowerCase(secondS);

		Double firstResult = calculateProb(firstS, biGram, addOne, singleCount, gramCount);
		Double secondResult = calculateProb(secondS, biGram, addOne, singleCount, gramCount);

		// TODO: print result
		System.out.println("\nInput: "+firstSentence+"\n");
		printMatrix(firstS, biGram, addOne, singleCount, gramCount);
		System.out.println("\nInput: "+secondSentence+"\n");
		printMatrix(secondS, biGram, addOne, singleCount, gramCount);


		System.out.println("First Result: " + firstResult);
		System.out.println("Second Result: " + secondResult);
		
	}
	public static String[] toLowerCase(String[] input){
		for(int i =0; i< input.length; i++){
			input[i] = input[i].toLowerCase();
		}
		return input;
	}

	public static void printMatrix(String[] words, boolean biGram, boolean addOne, Map<String, Integer> singleCount, Map<String, Integer> gramCount){
		int offset = addOne ? 1: 0;
		int v = singleCount.size();
		if(biGram){
			System.out.println("Count Table\n");
			for(String s: words){
				String space = s.length() >= 8 ? " ":"\t";
				System.out.print(s+space);
			}
			System.out.print("\n\n");
			for(String s: words){
				for(String s2: words){
					String space = s2.length() >= 8 ? "\t ":"\t";
					System.out.print(gramCount.getOrDefault(s+" "+s2, offset) + space);
				}
				System.out.print(s+"\n\n");
			}
			System.out.println("Probability Table\n");
			for(String s: words){
				String space = s.length() >= 8 ? " ":"\t";
				System.out.print(s+space);
			}
			System.out.print("\n\n");
			for(String s: words){
				for(String s2: words){
					String space = s2.length() >= 8 ? "\t ":"\t";
					Double single = 1.0 * singleCount.getOrDefault(s2, offset);
					Double gram = 1.0 * gramCount.getOrDefault(s+ " "+ s2, offset);
					Double result = (single == 0.0 || gram == 0.0) ? 0.0 : gram/ (addOne ? single + v : single);
					System.out.print(String.format("%.4f", result) + space);
				}
				System.out.print(s+"\n\n");
			}
		}else{
			System.out.println("Count Table\n");
			for(String s: words){
				String space = s.length() >= 8 ? " ":"\t";
				System.out.print(s+space);
			}
			System.out.print("\n\n");
			String[] singleWords = buildSingle(words, 2);
			for(String sin: singleWords){
				for(String s: words){
					String space = s.length() >= 8 ? "\t ":"\t";
					System.out.print(gramCount.getOrDefault(sin+" "+s, offset) + space);
				}
				System.out.print(sin+"\n\n");
			}
			System.out.println("Probability Table\n");
			for(String s: words){
				String space = s.length() >= 8 ? " ":"\t";
				System.out.print(s+space);
			}
			System.out.print("\n\n");
			for(String sin: singleWords){
				for(String s: words){
					String space = s.length() >= 8 ? "\t ":"\t";
					Double single = 1.0 * singleCount.getOrDefault(s, offset);
					Double gram = 1.0 * gramCount.getOrDefault(sin+ " "+ s, offset);
					Double result = (single == 0.0 || gram == 0.0) ? 0.0 : gram/ (addOne ? single + v : single);
					System.out.print(String.format("%.4f", result) + space);
				}
				System.out.print(sin+"\n\n");
			}
			
		}
		
	}

	public static Double calculateProb(String[] words, Boolean biGram, Boolean addOne, Map<String, Integer> singleCount, Map<String, Integer> gramCount){
		String[] singleWords = buildSingle(words, biGram ? 1 : 2);
		String[] gramWords = buildGram(words, biGram ? 2 : 3);

		Double result = 1.0;
		
		if(addOne){
			int v = singleCount.size();
			for(int i =0; i< singleWords.length; i++){
				Double single = 1.0 * singleCount.getOrDefault(singleWords[i], 1);
				Double gram = 1.0 * gramCount.getOrDefault(gramWords[i], 1);
				result = result * gram / (single + v);
			}
		}else{
			for(int i =0; i< singleWords.length; i++){
				Double single = 1.0 * singleCount.getOrDefault(singleWords[i], 0);
				Double gram = 1.0 * gramCount.getOrDefault(gramWords[i], 0);
				if(single == 0.0 || gram == 0.0) return 0.0;
				result = result * gram / single;
			}
		}
		return result;
	}
	
	public static String[] buildSingle(String[] words, int k){
		if(k == 1){
			String[] result = new String[words.length];
			for(int i=0; i<words.length; i++){
				if(i==0){
					result[0] = "<s>";
				}else{
					result[i] = words[i-1].toLowerCase();
				}
			}
			return result;
		}
		String[] result = new String[words.length-1];
		String[] cur = new String[k];
		cur[0] = "<s>";
		for(int i =0; i< words.length-1; i++){
			cur[1] = words[i].toLowerCase();
			result[i] = buildPrev(cur, 2);
			cur[0] = cur[1];
		}
		return result;
	}

	public static String[] buildGram(String[] words, int k){
		if(k == 2){
			String[] result = new String[words.length];
			String[] cur = new String[k];
			cur[0] = "<s>";
			for(int i =0; i< words.length; i++){
				cur[1] = words[i].toLowerCase();
				result[i] = buildPrev(cur, 2);
				cur[0] = cur[1];
			}
			return result;
		}
		String[] result = new String[words.length-1];
		String[] cur = new String[k];
		cur[0] = "<s>";
		for(int i =0; i< words.length-1; i++){
			if(i<1){
				cur[1] = words[i].toLowerCase();
			}else{
				cur[2] = words[i].toLowerCase();
				result[i-1] = buildPrev(cur, 3);
				cur[0] = cur[1];
				cur[1] = cur[2];
			}
		}
		return result;
	} 

	public static boolean setBiGram(String[] args){
		if(!args[0].equals("-N")) System.out.println("first argument has to be \"-N");
		else if(args[1].equals("2")) return true;
		return false;
	}

	public static boolean setAddOne(String[] args){
		if(!args[2].equals("-b"))System.out.println("Second argument has to be \"-b");
		else if(args[3].equals("1")) return true;
		return false;
	}


	public static void readfile(boolean biGram, boolean addOne, Map<String, Integer> singleCount, Map<String, Integer> gramCount){
			File file = new File(filePath);
			try{
				Scanner sc = new Scanner(file);
				while(sc.hasNextLine()){
					String[] words = sc.nextLine().split("\\s+", -1);
					if(biGram){
						biGramCount(words, singleCount, gramCount, addOne);
					}else{
						triGramCount(words, singleCount, gramCount, addOne);
					}
				}
			}
			catch (FileNotFoundException ex){
				System.out.println("brown_corpus_reviews.txt path is wrong");
			}
			return;
	}

	public static void biGramCount(String[] words, Map<String, Integer> singleCount, Map<String, Integer> gramCount, boolean addOne){
		int offset = addOne ? 1 : 0;
		String prevWord = "<s>";
		for(String currWord: words){
			if(currWord.length() == 1 && currWord.charAt(0) == '.'){
				prevWord = "<s>";
				singleCount.put("<s>", singleCount.getOrDefault("<s>", offset)+1);
				continue;
			}
			if(currWord.length() == 1 && !((currWord.charAt(0) >= 'a' && currWord.charAt(0) <= 'z' ) || (currWord.charAt(0) >= 'A' && currWord.charAt(0) <= 'Z'))){
				continue;
			}
			currWord = currWord.toLowerCase();
			singleCount.put(currWord, singleCount.getOrDefault(currWord, offset)+1);
			StringBuilder sb = new StringBuilder();
			sb.append(prevWord);
			sb.append(" ");
			sb.append(currWord);
			String bWord = sb.toString();
			gramCount.put(bWord, gramCount.getOrDefault(bWord, offset)+1);
			prevWord = currWord;
		}
		return;
	}

	public static void triGramCount(String[] words, Map<String, Integer> singleCount, Map<String, Integer> gramCount, boolean addOne){
		int offset = addOne ? 1 : 0;
		String[] prevWords = new String[3];
		prevWords[0] = "<s>";
		prevWords[1] = null;
		prevWords[2] = null;
		for(String currWord : words){
			if(currWord.length() == 1 && currWord.charAt(0) == '.'){
				String single = buildPrev(prevWords, 2);
				singleCount.put(single, singleCount.getOrDefault(single, offset)+1);
				prevWords[0] = "<s>";
				prevWords[1] = null;
				prevWords[2] = null;
				continue;
			}
			if(currWord.length() == 1 && !((currWord.charAt(0) >= 'a' && currWord.charAt(0) <= 'z' ) || (currWord.charAt(0) >= 'A' && currWord.charAt(0) <= 'Z'))){
				continue;
			}
			currWord = currWord.toLowerCase();
			if(prevWords[1] == null){
				prevWords[1] = currWord;
				continue;
			}
			String single = buildPrev(prevWords, 2);
			singleCount.put(single, singleCount.getOrDefault(single, offset)+1);
			prevWords[2] = currWord;
			String gram = buildPrev(prevWords, 3);
			gramCount.put(gram, gramCount.getOrDefault(gram, offset)+1);
			prevWords[0] = prevWords[1];
			prevWords[1] = prevWords[2];
		}
		return;
	}

	public static String buildPrev(String[] prev, int k){
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<k; i++){
			sb.append(prev[i]);
			sb.append(" ");
		}
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
	

	
}
