
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner ;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/* Main class for calculating EOD positions*/
public class EODCalculator {
	
	public static final String INPUT_TRANSACTION_FILE = "1537277231233_Input_Transactions.txt";
	public static final String INPUT_POSITIONS_FILE = "Input_StartOfDay_Positions.txt";
	public static final String OUTPUT_POSITIONS_FILE ="Expected_EndOfDay_Positions.txt";
	private HashMap<String,List<Transaction>> transactionMap;
	private List<StartOfDayPositions> startPositions;
	
	public EODCalculator(){
		this.transactionMap = InputOutputHandler.readInputTransactions(INPUT_TRANSACTION_FILE);
		this.startPositions = InputOutputHandler.readStartOfDayPositions(INPUT_POSITIONS_FILE);
	}
	
	protected List<EODPositions> calculateEODPositions(){
		List<EODPositions> eodPositions = new ArrayList<EODPositions>();
		for(StartOfDayPositions position: startPositions){
			double startPositionQuantity , endPositionQuantity;
			startPositionQuantity = endPositionQuantity = position.getQuantity();
			String instrument = position.getInstrument();
			char accountType = position.getAccountType();
			List<Transaction> transactions = transactionMap.getOrDefault(instrument, null);
			if(transactions == null){
				System.out.println("No transactions present for instrument :" + instrument);
				
			}
			else{
				for(Transaction tx : transactions){
					double transactionQuantity = tx.getTransactionQuantity();
					char type = tx.getTransactionType();
					switch(type){
						case 'B':
							switch(accountType){
								case 'E':
									endPositionQuantity = endPositionQuantity + transactionQuantity;
									break;
								case 'I':
									endPositionQuantity = endPositionQuantity - transactionQuantity;
									break;
								default:
									System.out.println("AccountType not supported");
									System.out.println(accountType);
								}
							break;
						case 'S':
							switch(accountType){
								case 'E':
									endPositionQuantity = endPositionQuantity - transactionQuantity;
									break;
								case 'I':
									endPositionQuantity = endPositionQuantity + transactionQuantity;
									break;
								default:
									System.out.println("AccountType not supported");
									System.out.println(accountType);
								}
							break;
						default:
							System.out.println("TransactionType not supported");
							System.out.println(type);
						}
				
				}
			}
			position.setQuantity(endPositionQuantity);
			EODPositions eodPosition = new EODPositions(position,(endPositionQuantity - startPositionQuantity)); 
			eodPositions.add(eodPosition);
		}
		return eodPositions;
	}
	public static void main(String[] args) {
		/* Calculate the EOD positions*/
		
		EODCalculator calculator = new EODCalculator();
		System.out.println("Calculating the End Of Day positions ....");
		List<EODPositions> eodPositions = calculator.calculateEODPositions();
		
		System.out.println("Printing the results of End Of Day positions ....");
		String content = "Instrument,Account,AccountType,Quantity,Delta\r\n";
		for(EODPositions positions : eodPositions){
			content = content + positions.toString() + "\r\n";
			System.out.println(positions);
		}
		
		/*Exporting the results */
		System.out.println("Exporting results to the file" + OUTPUT_POSITIONS_FILE);
		InputOutputHandler.exportEndOfDayPositions(OUTPUT_POSITIONS_FILE, content);
		
		
		EODPositions maxNetVolume = Collections.max(eodPositions , new NetVolumeComparator());
		EODPositions minNetVolume = Collections.min(eodPositions , new NetVolumeComparator());
		
		System.out.println("The max Net Transaction Volume is for instrument:" + maxNetVolume.getEodPosition().getInstrument() + " and net Volume:" + Math.abs(maxNetVolume.getDelta()));
		System.out.println("The min Net Transaction Volume is for instrument:" + minNetVolume.getEodPosition().getInstrument() + " and net Volume:" + Math.abs(minNetVolume.getDelta()));
	}

}

/*------------------------------------Transaction Class -------------------------*/
class Transaction {
	
	private int transactionId;
	private String instrument;
	private char transactionType;
	private double transactionQuantity;
	
	public Transaction(){
		
	}
	public Transaction (int transactionId, String instrument, char transactionType, double transactionQuantity){
		this.transactionId = transactionId;
		this.instrument = instrument;
		this.transactionType = transactionType;
		this.transactionQuantity = transactionQuantity;
	}
	
	@JsonProperty("TransactionId")
	public int getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(int id) {
        this.transactionId = id;
    }
    
    @JsonProperty("TransactionType")
    public char getTransactionType() {
        return transactionType;
    }
    public void setTransactionType(char transactionType) {
        this.transactionType = transactionType;
    }
    
    @JsonProperty("TransactionQuantity")
    public double getTransactionQuantity() {
        return transactionQuantity;
    }
    public void setTransactionQuantity(double quantity) {
        this.transactionQuantity = quantity;
    }
    
    @JsonProperty("Instrument")
    public String getInstrument() {
        return instrument;
    }
    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }
    @Override
	public String toString(){
      return "Transaction [ Id: "+transactionId+", type: "+ transactionType+ " ]";
   }
	
	@Override
	public boolean equals(Object transaction) {
		if(this == transaction){
			return true;
		}
		
		if(!(transaction instanceof Transaction)){
			return false;
		}
		return this.getInstrument().equals(((Transaction) transaction).getInstrument());
	}
	
}

/*------------------------------------StartOfDayPositions Class -------------------------*/
class StartOfDayPositions {
	private String instrument;
	private int account;
	private char accountType;
	private double quantity;
	
	public StartOfDayPositions(String instrument, int account, char accountType, double quantity) {
		this.instrument = instrument;
		this.account = account;
		this.accountType = accountType;
		this.quantity = quantity;
	}
	public String getInstrument() {
		return instrument;
	}
	public void setInstrument(String instrument) {
		this.instrument = instrument;
	}
	public int getAccount() {
		return account;
	}
	public void setAccount(int account) {
		this.account = account;
	}
	public char getAccountType() {
		return accountType;
	}
	public void setAccountType(char accountType) {
		this.accountType = accountType;
	}
	public double getQuantity() {
		return quantity;
	}
	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}
	@Override
	public String toString() {
		return "StartOfDayPositions [instrument=" + instrument + ", accountType=" + accountType + "]";
	}
}

/*------------------------------------InputOutputHandler Class -------------------------*/
/* InputOutputHandler has 3 functions - to read from transaction File/StartDay positions File, export EODPositions */
class InputOutputHandler{
	
	protected static HashMap<String,List<Transaction>> readInputTransactions(String input_file){
		Transaction[] transactions = null;
		HashMap<String,List<Transaction>> transactionMap = new HashMap<String,List<Transaction>>();
		try {
			System.out.println("Reading the file text for Input Transactions");
			String entireFileText = new Scanner(new File(input_file)).useDelimiter("\\A").next();
			ObjectMapper mapper = new ObjectMapper();
			transactions = mapper.readValue(entireFileText, Transaction[].class);
			
			for(Transaction transaction:transactions){
				System.out.println(transaction);
				String key = transaction.getInstrument();
				List<Transaction> transList = transactionMap.getOrDefault(key,null);
				if(transList != null){
					transList.add(transaction);
					transactionMap.replace(key, transList);
				}
				else{
					transList = new ArrayList<Transaction>();
					transList.add(transaction);
					transactionMap.put(key,transList);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
			
		}
		System.out.println("Done...Reading the file text for Input Transactions");
		return transactionMap;
		
	}
	
	protected static List<StartOfDayPositions> readStartOfDayPositions(String input_file){
		System.out.println("Reading the file text for start Positions");
		BufferedReader br = null;
		List<StartOfDayPositions> startPositions = new ArrayList<StartOfDayPositions>();
		try {
			br = new BufferedReader(new FileReader(new File(input_file)));
			String st = br.readLine();
			
			while ((st = br.readLine()) != null) {
				String positions[] = st.split(",");
				StartOfDayPositions position = new StartOfDayPositions(positions[0],Integer.parseInt(positions[1]),positions[2].charAt(0),Double.parseDouble(positions[3]));
				startPositions.add(position);
				System.out.println(position);
					
				}
		} catch ( NumberFormatException | IOException e) {
			
			e.printStackTrace();
		}
		finally{
			try {
				br.close();
			} catch (IOException e) {
				System.out.println("Problem in closing the Buffered Reader!!");
			}
		}
		System.out.println("Done... reading from positions file ");
		return startPositions;
		
	}
	
	protected static void exportEndOfDayPositions(String output_file, String contents){
		BufferedWriter br = null;
		try {
			br = new BufferedWriter(new FileWriter(new File(output_file)));
			br.write(contents);
			System.out.println("Done... File export at location:" + output_file);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		finally{
			try {
				br.close();
			} catch (IOException e) {
				System.out.println("Problem in closing the Buffered writer!!");
			}
		}
	}
	
}

/*------------------------------------EODPositions Class -------------------------*/
/* EOD Positions class having End Of Day positions */
class EODPositions{
	private StartOfDayPositions eodPosition;
	private double delta;
	
	public EODPositions(StartOfDayPositions eodPosition, double delta) {
		this.eodPosition = eodPosition;
		this.delta = delta;
	}

	public StartOfDayPositions getEodPosition() {
		return eodPosition;
	}

	public void setEodPosition(StartOfDayPositions eodPosition) {
		this.eodPosition = eodPosition;
	}

	public double getDelta() {
		return delta;
	}

	public void setDelta(double delta) {
		this.delta = delta;
	}
	
	@Override
	public String toString() {
		return eodPosition.getInstrument() + ","+  eodPosition.getAccount() + "," + eodPosition.getAccountType() + ","+ Math.round(eodPosition.getQuantity()) +
				 "," + Math.round(delta);
	}
}

/*------------------------------------NetVolumeComparator Class -------------------------*/
/* To compare the EOD Positions*/
class NetVolumeComparator implements Comparator<EODPositions>{

	@Override
	public int compare(EODPositions pos1, EODPositions pos2) {
		
		if (Math.abs(pos1.getDelta()) > Math.abs(pos2.getDelta()))
            return 1; 
        if (Math.abs(pos1.getDelta()) == Math.abs(pos2.getDelta()))
            return 0;
        return -1;
	}
	
}
