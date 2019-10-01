import Tag.*;
import java.util.*;


class main {
	public static void main(String[] args) {
		String path = "brown_modified";
		String s0 = "People race tomorrow .";
		String s1 = "The Secretariat is expected to race tomorrow .";
		String s2 = "People continue to enquire the reason for the race for outer space .";
		tagger tg = new tagger();
		List<List<String>> words = tg.load_corpus(path);
		tg.initialize_probabilities(words);
		// tg.printInitProb();

		String[] result0 = tg.viterbi_decode(s0);
		System.out.println("\n"+s0);
		tg.printStringArray(result0);

		String[] result1 = tg.viterbi_decode(s1);
		System.out.println("\n"+s1);
		tg.printStringArray(result1);

		String[] result2 = tg.viterbi_decode(s2);
		System.out.println("\n"+s2);
		tg.printStringArray(result2);

		return;
	}
}