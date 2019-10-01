package Tag;

import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
// import java.lang.StringBuilder;

public class tagger{
	Map<String, Double> initProb;
	Map<String, Map<String, Double>> tranProb;
	Map<String, Map<String, Double>> emisProb;
	public tagger(){
		initProb = new HashMap();
		tranProb = new HashMap();
		emisProb = new HashMap();
	}
	public List<List<String>> load_corpus(String path){
		List<List<String>> result = new ArrayList();
		File dir = new File(path);
		if(dir.exists() && dir.isDirectory()){
			File[] files = dir.listFiles();
			for(File f : files){
				try{
					Scanner sc = new Scanner(f);
					while(sc.hasNextLine()){
						String[] words = sc.nextLine().split(" ", -1);
						for(String word: words){
							List<String> list = new ArrayList();
							String[] tag = word.split("/", -1);
							for(String s: tag){
								list.add(s);
								if(list.size() ==2){
									result.add(list);
									list = new ArrayList();
								}
							}
						}		
					}
				}catch(FileNotFoundException ex){
					System.out.println("file doesn't exists");
				}
				
			}
		}else{
			System.out.println("It is not a dir!");
		}

		return result;
	}

	public void initialize_probabilities(List<List<String>> words){
		Map<String, Integer> initCount = new HashMap();
		Map<String, Map<String, Integer>> tranCount = new HashMap();
		Map<String, Map<String, Integer>> emisCount = new HashMap();
		Set<String> set = new HashSet();
		int initTotal=0;
		Boolean start = true;
		String prev = "";
		for(List<String> word : words){
			// change the word to lowerCase
			String lower = word.get(0).toLowerCase();
			word.remove(0);
			word.add(0, lower);
			if(start){
				initCount.put(word.get(1), initCount.getOrDefault(word.get(1), 1)+1);
				start = false;
				prev = word.get(1);
				initTotal++;
			}else{
				Map<String, Integer> tran = tranCount.getOrDefault(prev, new HashMap());
				tran.put(word.get(1), tran.getOrDefault(word.get(1), 1)+1); 
				tranCount.put(prev, tran);
				if(word.get(0).equals(".")){
					start = true;
					prev = "";
				}else{
					prev = word.get(1);
				}
			}

			Map<String, Integer> tmp = emisCount.getOrDefault(word.get(1), new HashMap());
			tmp.put(word.get(0), tmp.getOrDefault(word.get(0), 1)+1);
			emisCount.put(word.get(1), tmp);

			// for add-one smoothing V
			set.add(word.get(1));
		}

		// initProb
		System.out.println("initTotal: "+initTotal);

		initProb.put("@", 1.0/initTotal);  // for default
		for(String word: initCount.keySet()){
			initProb.put(word, 1.0 * initCount.get(word) / initTotal);
		}

		// tranProb
		int rt = 0;
		int rv = 0;
		for(String word: tranCount.keySet()){			
			Map<String, Integer> wordCount = tranCount.get(word);
			int total =0;
			int v= wordCount.size();
			for(String next: wordCount.keySet()){
				total+=wordCount.get(next);
			}
			rt+=total;
			rv+=v;
			Map<String, Double> wordProb = new HashMap();
			wordProb.put("@", 1.0/(total+v));  // for default
			for(String next: wordCount.keySet()){
				wordProb.put(next, 1.0*wordCount.get(next)/(total+v));
			}
			tranProb.put(word, wordProb);
		}
		Map<String, Double> noPrevCase = new HashMap();
		noPrevCase.put("@", 1.0/(rt+rv));
		tranProb.put("@", noPrevCase);

		// emisProb
		for(String term: emisCount.keySet()){
			Map<String, Integer> wordCount = emisCount.get(term);
			int total =0;
			int v= wordCount.size();
			for(String word: wordCount.keySet()){
				total+=wordCount.get(word);
			}
			Map<String, Double> wordProb = new HashMap();
			wordProb.put("@", 1.0/(total+v));  // for default
			for(String word: wordCount.keySet()){
				wordProb.put(word, 1.0*wordCount.get(word)/(total+v));
			}
			emisProb.put(term, wordProb);
		}
	}

	public String[] viterbi_decode(String s){
		String[] words = s.split(" ",-1);
		for(int i=0; i< words.length; i++){
			words[i] = words[i].toLowerCase();
		}
		String[] result = new String[words.length];
		Double[][] v = new Double[emisProb.size()][words.length];
		List<String>[][] vs = new ArrayList[emisProb.size()][words.length];
		String[] terms = new String[emisProb.size()];
		int n=0;
		for(String term : emisProb.keySet()){
			terms[n++] = term;
		}
		for(int i =0; i< words.length; i++){
			if(i==0){
				for(int j=0; j<terms.length; j++){
					// v[j][0] = init(term) * emis(word|term)
					v[j][i] = initProb.getOrDefault(terms[j], initProb.get("@"))*emisProb.get(terms[j]).getOrDefault(words[i], emisProb.get(terms[j]).get("@"));
					List<String> t = new ArrayList();
					t.add(terms[j]);
					vs[j][i] = t;
				}
			}else{
				for(int j=0; j<terms.length; j++){
					String prev = "";
					String cur = terms[j];
					Double max = 0.0;
					for(int k=0; k<terms.length; k++){
						prev = terms[k];
						Map<String, Double> tran = tranProb.getOrDefault(prev, tranProb.get("@"));
						double tmp = v[k][i-1] * tran.getOrDefault(cur, tran.get("@")) * emisProb.get(terms[j]).getOrDefault(words[i], emisProb.get(terms[j]).get("@"));
						if(tmp > max){
							List<String> t = new ArrayList(vs[k][i-1]);
							t.add(terms[j]);
							vs[j][i] = t;
							max = tmp;
						}
					}
					v[j][i] = max;
				}
			}
		}
		int idx =0;
		double max =0;
		for(int i=0; i< terms.length; i++){
			if(v[i][result.length-1] > max){
				idx = i;
				max = v[i][result.length-1];
			}
		}
		List<String> maxString = vs[idx][result.length-1];
		for(int i=0; i< result.length; i++){
			result[i] = maxString.get(i);
		}
		return result;
	}

	public void printStringArray(String[] result){
		System.out.print("['");
		for(int i=0; i< result.length; i++){
			System.out.print(result[i]);
			if(i != result.length-1){
				System.out.print("', '");	
			}
		}
		System.out.println("']");
	}

	public void printInitProb(){
		// System.out.println("initProb: ");
		// for(String word : initProb.keySet()){
		// 	System.out.println(word+" : "+initProb.get(word));
		// }
		System.out.println("tranProb: ");
		for(String word : tranProb.keySet()){
			System.out.println(word+":");
			Map<String, Double> tmp = tranProb.get(word);
			for(String next : tmp.keySet()){
				System.out.print("\t");
				System.out.println(next + " : " + tmp.get(next));
			}
		}
		// System.out.println("emisProb: ");
		// 	String term = "VERB";
		// 	String word = "race";
		// 	System.out.println(term+":");
		// 	Map<String, Double> tmp = emisProb.get(term);
		// 	for(String next : tmp.keySet()){
		// 		System.out.print(word+"\t>\t");
		// 		System.out.println(next + " : " + tmp.get(next));
		// 	}

		// 	System.out.println(term+" > "+tmp.get(word));
		// 	term = "NOUN";
		// 	tmp = emisProb.get(term);
		// 	System.out.println(term+" > "+tmp.get(word));
	}

	public void printList(List<List<String>> result){
		for(List<String> l1 : result){
			for(String s: l1){
				System.out.print(s+"\t");
			}
			System.out.println();
		}
	}
}
