package orig2011.v6;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


public class ReversiScoreView implements PropertyChangeListener {


	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName()=="Turn") {
			if (evt.getSource().getClass() == ReversiModel.class) {
				System.out.println("Bong! White: " + ((ReversiModel)evt.getSource()).getWhiteScore()
						+ "\tBlack: " + ((ReversiModel)evt.getSource()).getWhiteScore());
				System.out.println("Turn: " +  ((ReversiModel)evt.getSource()).getTurnColor());
			}
		}

	}

}
