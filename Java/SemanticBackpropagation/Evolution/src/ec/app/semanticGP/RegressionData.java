package ec.app.semanticGP;

import ec.gp.GPData;

public class RegressionData extends GPData {

	public double x;
	public double y;
	public int testCaseNumber = 0;

	@Override
	public void copyTo(final GPData gpd) {
		RegressionData second = (RegressionData) gpd;
		second.x = this.x;
		second.y = this.y;
		//second.testCaseNumber = this.testCaseNumber;
	}

}
