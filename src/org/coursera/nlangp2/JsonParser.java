/**
 * 
 */
package org.coursera.nlangp2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;

/**
 * @author rpuduppully
 *
 */
public class JsonParser {

	private static final String COMMA = ",";
	private Map<String, Integer> wordCount = new HashMap<String, Integer>();
	private static final String INPUT_FILE = "D:/project/study/nlp/h2.2/assignment/parse_train.dat";
	private static final String OUTPUT_FILE =  "D:/project/study/nlp/h2.2/assignment/parse_train_out.dat";
	private static final String COUNT_FILE = "D:/project/study/nlp/h2.2/assignment/parse_train.counts.out";
	private static final String DEV_DAT = "D:/project/study/nlp/h2.2/assignment/parse_dev.dat";
	private Map<String, BigDecimal> qXw = new HashMap<String, BigDecimal>();
	private Map<String, BigDecimal> qXY1Y2 = new HashMap<String, BigDecimal>();
	private Map<String, Integer> X = new HashMap<String, Integer>();
	private Map<String,Integer> Xw = new HashMap<String, Integer>();
	private Map<String,Integer> XY1Y2 = new HashMap<String, Integer>();
	
	private Map<String, List<Rule>>wX = new HashMap<String,List<Rule>>();
	private Map<String, List<Rule>>qwX = new HashMap<String,List<Rule>>();
	private Map<String, List<Rule>> Y1Y2X = new HashMap<String,List<Rule>>();
	private Map<String, List<Rule>> qY1Y2X = new HashMap<String,List<Rule>>();
	
	
	public static void main(String[] args) {
		JSONArray jsonArray = new JSONArray("[\"hi\",[\"me\",\"here\"]];");
		System.out.println(jsonArray.get(0));
		System.out.println(jsonArray.get(1));
		System.out.println(jsonArray.get(0) instanceof JSONArray);
		System.out.println(jsonArray.toString());
		
		JsonParser parser = new JsonParser();
		parser.execute();
		
		parser.computeRuleParameters();
		
		

		
//		parser.parseArray(new JSONArray("[s, [np, there], [s, [vp,[verb, is], [noun, asbestos] ],[., .]]]"));
//		System.out.println(new JSONArray("[s, [np, there], [s, [vp,[verb, is], [noun, asbestos] ],[., .]]]").toString());
		
	
	}

	private void computeRuleParameters() {
		try {
			String inputLine = null;
			BufferedReader fileReader = new BufferedReader(new FileReader(COUNT_FILE));
			while((inputLine = fileReader.readLine()) != null){
				if(inputLine.indexOf("NONTERMINAL") != -1){
					String [] args = inputLine.split(" ");
					X.put(args[2], Integer.parseInt(args[0]));
				}else if(inputLine.indexOf("UNARYRULE") != -1){
					String [] args = inputLine.split(" ");
					String count = args[0];
					String terminal = args[3];
					String nonterminal = args[2];
					Xw.put(nonterminal+COMMA+terminal, Integer.parseInt(count));
					
					List<Rule> rules = setRules(wX,count, terminal, nonterminal);
					
					wX.put(terminal, rules);
				}else if(inputLine.indexOf("BINARYRULE") != -1){
					String [] args = inputLine.split(" ");
					String x = args[2];
					String y1 = args[3];
					String y2 = args[4];
					String count = args[0];
					XY1Y2.put(x + COMMA + y1+ COMMA + y2, Integer.parseInt(count));
					
					Y1Y2X.put(y1+COMMA+y2, setRules(Y1Y2X, count, y1+COMMA+y2, x));
				}
			}
			
			Set<Map.Entry<String, Integer>> xwEntrySet = Xw.entrySet();
			for(Map.Entry<String, Integer> xwEntry: xwEntrySet){
				qXw.put(xwEntry.getKey(),new BigDecimal( (float)xwEntry.getValue()/ X.get(xwEntry.getKey().split(COMMA)[0])));
			}

			Set<Map.Entry<String, Integer>> xY1Y2EntrySet = XY1Y2.entrySet();
			for(Map.Entry<String, Integer> xY1Y2Entry: xY1Y2EntrySet){
				qXY1Y2.put(xY1Y2Entry.getKey(), new BigDecimal((float)xY1Y2Entry.getValue()/ X.get(xY1Y2Entry.getKey().split(COMMA)[0])));
			}
			
//			Set<Map.Entry<String, BigDecimal>> qXwEntrySet = qXw.entrySet();
//			for(Map.Entry<String, BigDecimal> qXwEntry: qXwEntrySet){
//				if(qwX.containsKey(qXwEntry.getKey().split(COMMA)[1])){
//					
//				}
//				qwX.put(qXwEntry.getKey().split(COMMA)[1], value)
//			}
			
			setRuleProb(wX, qwX);
			setRuleProb(Y1Y2X, qY1Y2X);
			
			System.out.println("wX "+wX);
			System.out.println("qXW "+qXw);
			System.out.println("qwx What"+qwX.get("What"));
			System.out.println("WHNP+PRON "+qXw.get("WHNP+PRON,What"));
			System.out.println("PRON "+qXw.get("PRON,What"));
			System.out.println("DET"+qXw.get("DET,What"));
			System.out.println("size Xw "+qXw.size());
			System.out.println("size  XY1Y2 "+qXY1Y2.size());
			System.out.println("qXY1Y2 "+qXY1Y2);
			fileReader.close();
			
			
			BufferedReader fileReader2 = new BufferedReader(new FileReader(DEV_DAT));
			while((inputLine = fileReader2.readLine()) != null){
				
			}
			fileReader2.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void setRuleProb(Map<String, List<Rule>>inputMap, Map<String, List<Rule>>outputMap) {
		for(Map.Entry<String, List<Rule>>wxEntry: inputMap.entrySet()){
			List<Rule> rules = wxEntry.getValue();
			List<Rule> rulesProb = new ArrayList<Rule>();
			for(Rule rule: rules){
				Rule ruleProb = new Rule();
				ruleProb.setProb(new BigDecimal((float)rule.getProb().intValue()/X.get(rule.getX())));
				ruleProb.setX(rule.getX());
				rulesProb.add(ruleProb);
			}
			outputMap.put(wxEntry.getKey(), rulesProb);
		}
	}

	private List<Rule> setRules(Map<String, List<Rule>> inputMap, String count, String terminal,
			String nonterminal) {
		List<Rule> rules = inputMap.get(terminal);
		if(!inputMap.containsKey(terminal)){
			rules = new ArrayList<Rule>();
		}
		Rule rule = new Rule();
		rule.setProb( new BigDecimal(Integer.parseInt(count)));
		rule.setX(nonterminal);
		rules.add(rule);
		return rules;
	}

	private void execute() {
		try {
			String inputLine = null;
			BufferedReader fileReader = new BufferedReader(new FileReader(INPUT_FILE));
			while((inputLine = fileReader.readLine()) != null){
				JSONArray jsonArray = new JSONArray(inputLine);
				parseArray(jsonArray);
			}
			fileReader.close();
			
			BufferedReader fileReader2 = new BufferedReader(new FileReader(INPUT_FILE));
			BufferedWriter fileWriter = new BufferedWriter(new FileWriter(OUTPUT_FILE));
			while((inputLine = fileReader2.readLine()) != null){
				JSONArray jsonArray = new JSONArray(inputLine);
				replaceRare(jsonArray);
				fileWriter.write(jsonArray.toString());
				fileWriter.newLine();
			}
			fileReader2.close();		
			fileWriter.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e){
			
		}
	}

	private void replaceRare(JSONArray input) {
		for(int i=0; i < input.length(); i++){
			if(input.get(i) instanceof JSONArray){
				JSONArray element = input.getJSONArray(i);
				if(element.length() == 3){
					replaceRare(element);
				}else if(element.length() ==2){
					if(wordCount.get(element.getString(1)) <5){
						element.put(1, "_RARE_");
					}
				}
			}
		}
	}

	private void parseArray(JSONArray input) {
		for(int i=0; i < input.length(); i++){
			if(input.get(i) instanceof JSONArray){
				JSONArray element = input.getJSONArray(i);
				if(element.length() == 3){
					parseArray(element);
				}else if(element.length() == 2){
					setOrIncrement(wordCount, element.getString(1));
				}else{
					throw new IllegalArgumentException("lengths 2 and 3 are acceptable for arrays");
				}
			}
		}
	}
	
	private static void setOrIncrement(Map<String, Integer> countMap,
			String key) {
		if(countMap.containsKey(key)){
			countMap.put(key, countMap.get(key)+1);
		}else{
			countMap.put(key, 1);
		}
	}
}


class Rule{
	private String X;
	private BigDecimal prob;
	
	public void setProb(BigDecimal prob) {
		this.prob = prob;
	}
	
	public void setX(String x) {
		X = x;
	}
	
	public BigDecimal getProb() {
		return prob;
	}
	
	public String getX() {
		return X;
	}

	@Override
	public String toString() {
		return "Rule [X=" + X + ", prob=" + prob + "]";
	}
	
	
}
