import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class EODCalculatorTest {

	@Before
	public void setUp() throws Exception {
		/*nothing much */
	}

	@After
	public void tearDown() throws Exception {
		/*nothing much */
	}

	@Test
	public void calculateEODPositionsTest() {
		EODCalculator testCalculator = new EODCalculator();
		try {
			List<EODPositions> eodPositions = testCalculator.calculateEODPositions();
			EODPositions maxNetVolume = Collections.max(eodPositions , new NetVolumeComparator());
			EODPositions minNetVolume = Collections.min(eodPositions , new NetVolumeComparator());
			assertEquals(maxNetVolume.getEodPosition().getInstrument(),"AMZN");
			assertEquals(minNetVolume.getEodPosition().getInstrument(),"NFLX");
			
		}
		catch(Exception e){
			System.out.println("Failed while running calcualtor");
			assert false;
		}
		
	}

}
