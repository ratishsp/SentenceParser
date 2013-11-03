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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;

/**
 * @author rpuduppully
 *
 */
public class SentenceParser {

	private static final String RARE = "_RARE_";
	private static final String COMMA = "@";
	private Map<String, Integer> wordCount = new HashMap<String, Integer>();
	private static final String INPUT_FILE = "D:/project/study/nlp/h2.2/assignment/parse_train.dat";
	private static final String OUTPUT_FILE =  "D:/project/study/nlp/h2.2/assignment/parse_train_out.dat";
	private static final String COUNT_FILE = "D:/project/study/nlp/h2.2/assignment/parse_train.counts.out";
	private static final String DEV_DAT = "D:/project/study/nlp/h2.2/assignment/parse_dev.dat";
	private static final String DEV_OUT = "D:/project/study/nlp/h2.2/assignment/parse_dev.out";
	private static final String TEST_DAT = "D:/project/study/nlp/h2.2/assignment/parse_test.dat";
	private static final String TEST_OUT = "D:/project/study/nlp/h2.2/assignment/parse_test.p2.out";
	private static final String SPACE = " ";
	private Map<String, BigDecimal> qXw = new HashMap<String, BigDecimal>();
	private Map<String, BigDecimal> qXY1Y2 = new HashMap<String, BigDecimal>();
	private Map<String, Integer> X = new HashMap<String, Integer>();
	private Map<String,Integer> Xw = new HashMap<String, Integer>();
	private Map<String,Integer> XY1Y2 = new HashMap<String, Integer>();
	
	private Map<String, List<Rule>>wX = new HashMap<String,List<Rule>>();
	private Map<String, List<Rule>>qwX = new HashMap<String,List<Rule>>();
	private Map<String, List<Rule>> Y1Y2X = new HashMap<String,List<Rule>>();
	private Map<String, List<Rule>> qY1Y2X = new HashMap<String,List<Rule>>();
	
	private Map<String, PiValue> pi = new LinkedHashMap<String, PiValue>();
	private Map<String, Set<String>> piPossibleNonTerminals = new LinkedHashMap<String, Set<String>>();
	private Map<String, List<PiValue>> piPossibleValues = new LinkedHashMap<String, List<PiValue>>();
	
	public static void main(String[] args) {
		JSONArray jsonArray = new JSONArray("[\"hi\",[\"me\",\"here\"]];");
		System.out.println(jsonArray.get(0));
		System.out.println(jsonArray.get(1));
		System.out.println(jsonArray.get(0) instanceof JSONArray);
		System.out.println(jsonArray.toString());
		
		SentenceParser parser = new SentenceParser();
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
			
			
			parse();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void parse() throws IOException {
		System.out.println("start "+new Date());
		long startTime = System.currentTimeMillis();
		String inputLine;
//					BufferedReader fileReader2 = new BufferedReader(new FileReader(TEST_DAT));
//					BufferedWriter fileWriter = new BufferedWriter(new FileWriter(TEST_OUT));
					BufferedReader fileReader2 = new BufferedReader(new FileReader(DEV_DAT));
					BufferedWriter fileWriter = new BufferedWriter(new FileWriter(DEV_OUT));
					while((inputLine = fileReader2.readLine()) != null){
//					inputLine = "What are geckos ?";
//					inputLine = "How many miles is it from London , England to Plymouth , England ?";
						piPossibleNonTerminals = new LinkedHashMap<String, Set<String>>();
						piPossibleValues = new LinkedHashMap<String, List<PiValue>>();
						pi = new LinkedHashMap<String, PiValue>();
						
						String [] args = inputLine.split(SPACE);
						int n = args.length;
						for(int i=0;  i<n; i++){
							String terminal = args[i];
							
							List<Rule> rules = qwX.get(terminal);
							if(rules == null){
								rules = qwX.get(RARE);
		//						terminal = RARE;
							}
							for(Rule rule: rules){
								String keyPositions = (i+1)+COMMA +( i+1);
								pi.put(keyPositions +COMMA+ rule.getX(), new PiValue(rule.getProb().floatValue(),rule.getX()+COMMA + terminal, i +1));
							
								setIntoPossibleNonTerminals(rule, keyPositions);						
							}
							
						}
						
						
						for(int l=1; l<= n-1; l++){
							for(int i=1; i<= n-l; i++ ){
								int j = i+l;
								
								for(int s=i; s<= j-1; s++){
//									System.out.println("i "+i + " j "+ j + " s "+s);
									Set<String> iTos = piPossibleNonTerminals.get(i+COMMA + s);
									Set<String> splus1Toj = piPossibleNonTerminals.get(s+1+COMMA + j);
									
									for(String iTosKey: iTos){
										for(String splus1TojKey:  splus1Toj){
											List<Rule> rules = qY1Y2X.get(iTosKey + COMMA + splus1TojKey);
											
											if(rules == null){
												continue;
											}
											for(Rule rule: rules){
												PiValue piValue = new PiValue();
												
												piValue.setProb(rule.getProb().floatValue() * pi.get(i + COMMA+ s+COMMA+iTosKey).getProb() * pi.get((s+1)+COMMA+j + COMMA + splus1TojKey).getProb());
												String x = rule.getX();
												piValue.setArgMaxRule(x+COMMA+iTosKey + COMMA + splus1TojKey);
												piValue.setArgMaxSplit(s);
//												System.out.println("pi possible value  "+piValue);
												setIntoPossibleValues(i, j, piValue, x);
												
												setIntoPossibleNonTerminals(rule, i+COMMA+j);
											}
										}
									}
								}
								
								Set<String> iToj = piPossibleNonTerminals.get(i + COMMA +j);
								if(iToj == null){
									piPossibleNonTerminals.put(i+COMMA+j, new HashSet<String>());
									continue;
								}
//								System.out.println("compute pi "+new Date());
								for(String iTojKey: iToj){
									String piKey = i + COMMA + j + COMMA+ iTojKey;
//									System.out.println("start comparison "+new Date());
//									System.out.println("piPossibleValues.get(piKey).size "+piPossibleValues.get(piKey).size());
									Collections.sort(piPossibleValues.get(piKey), new PiValueComparator());
//									System.out.println("end comparison "+new Date());
									pi.put(piKey , piPossibleValues.get(piKey).get(0));
//									System.out.println("actual pi "+piKey +" "+ piPossibleValues.get(piKey).get(0));
								}
								
//								System.out.println("computed pi "+new Date());
							}
						}
//					System.out.println("pi "+pi);
					
					Set<String> iToj = piPossibleNonTerminals.get(1 + COMMA +n);
					List<PiValue> values = new ArrayList<PiValue>();
					for(String iTojKey: iToj){
						values.add(pi.get(1+COMMA+n+COMMA+iTojKey));
					}
					Collections.sort(values, new PiValueComparator());
		
//					System.out.println("root "+values.get(0));
		//			String argMaxRule = values.get(0).getArgMaxRule();
		//			String[] nonterminalKeys = argMaxRule.split(COMMA);
		//			int split = values.get(0).getArgMaxSplit();
		//			
		//			pi.get(1+COMMA+split+COMMA+nonterminalKeys[1]);
		//			JSONArray output = new JSONArray();
		//			output.put(0, nonterminalKeys[0]);
					
					
		//			output.p
					JSONArray output = bp(1,n, values.get(0).getArgMaxRule().split(COMMA)[0]);
					System.out.println("output "+output);
//					System.out.println("piPossibleNonTerminals "+piPossibleNonTerminals);
	
					fileWriter.write(output.toString());
					fileWriter.newLine();
					}
					fileReader2.close();
					fileWriter.close();
	
					long endTime = System.currentTimeMillis();
					System.out.println("end "+new Date());
					System.out.println("time taken "+ (endTime-startTime)/1000);
	}

	private void setIntoPossibleValues(int i, int j, PiValue piValue, String x) {
		String keypositions = i+COMMA+j+COMMA+x;
		List<PiValue> piValues = piPossibleValues.get(keypositions);
		if(!piPossibleValues.containsKey(keypositions)){
			piValues = new ArrayList<PiValue>();
			piPossibleValues.put(keypositions, piValues);
		}
		piValues.add(piValue);
	}
	
	private JSONArray bp(int i, int j, String x){
		PiValue piValue = pi.get(i+COMMA+j+COMMA+x);
		
		String [] children = piValue.getArgMaxRule().split(COMMA);
		int s =piValue.getArgMaxSplit();
		JSONArray array = new JSONArray();
		
		if(children.length ==2){
			array.put(0,children[0]);
			array.put(1,children[1]);
		}else if(children.length ==3){
			array.put(0,children[0]);
			array.put(1,bp(i,s,children[1]));
			array.put(2,bp(s+1,j,children[2]));
		}else{
			throw new IllegalArgumentException("invalid input "+piValue.getArgMaxRule());
		}
		
		return array;
	}

	private void setIntoPossibleNonTerminals(Rule rule, String keyPositions) {
		Set<String> possibleNonTerminals = piPossibleNonTerminals.get(keyPositions);
		if(!piPossibleNonTerminals.containsKey(keyPositions)){
			possibleNonTerminals = new HashSet<String>();
			piPossibleNonTerminals.put(keyPositions, possibleNonTerminals);
		}
		possibleNonTerminals.add(rule.getX());
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
						element.put(1, RARE);
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

class PiValue{
	private Float prob;
	private String argMaxRule;
	private Integer argMaxSplit;
		
	public PiValue() {
		// TODO Auto-generated constructor stub
	}
	public PiValue(Float prob, String argMaxRule, Integer argMaxSplit) {
		super();
		this.prob = prob;
		this.argMaxRule = argMaxRule;
		this.argMaxSplit = argMaxSplit;
	}

	public void setArgMaxRule(String argMaxRule) {
		this.argMaxRule = argMaxRule;
	}
	
	public void setArgMaxSplit(Integer argMaxSplit) {
		this.argMaxSplit = argMaxSplit;
	}
	
	public void setProb(Float prob) {
		this.prob = prob;
	}
	
	public String getArgMaxRule() {
		return argMaxRule;
	}
	
	public Integer getArgMaxSplit() {
		return argMaxSplit;
	}
	
	public Float getProb() {
		return prob;
	}

	@Override
	public String toString() {
		return "PiValue [prob=" + prob + ", argMaxRule=" + argMaxRule
				+ ", argMaxSplit=" + argMaxSplit + "]";
	}
	
	
}
class PiValueComparator implements Comparator<PiValue>{
	public int compare(PiValue o1, PiValue o2) {
		return o2.getProb().compareTo(o1.getProb()); 
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
