package com.mayhew3.drafttower.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayhew3.drafttower.shared.PlayerColumn;
import com.mayhew3.drafttower.shared.Position;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.GeneralRegressionModel;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.EvaluationException;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.GeneralRegressionModelEvaluator;
import org.jpmml.evaluator.ModelEvaluator;
import org.jpmml.model.JAXBUtil;

import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;
import java.util.*;

/**
 * Prediction model which makes predictions using spooky magic.
 */
@Singleton
public class PredictionModelImpl implements PredictionModel {

  private final Map<Position, List<ModelEvaluator<GeneralRegressionModel>>> allModels;

  @Inject
  public PredictionModelImpl() throws JAXBException {
    allModels = new HashMap<>();
    for (Position position : Position.values()) {
      if (position == Position.DH || position == Position.RS) {
        continue;
      }
      ArrayList<ModelEvaluator<GeneralRegressionModel>> positionModels = new ArrayList<>();
      allModels.put(position, positionModels);
      for (int rank = 1; rank <= ((position == Position.OF || position == Position.P) ? 5 : 3); rank++) {
        PMML modelContents = JAXBUtil.unmarshalPMML(new StreamSource(
            getClass().getResourceAsStream("/com/mayhew3/drafttower/models/" + position.getShortName() + rank + ".pmml")));
        positionModels.add(new GeneralRegressionModelEvaluator(modelContents));
      }
    }
  }

  @Override
  public float getPrediction(Position position, int rank, int pickNum, Integer... numFilled) {
    ModelEvaluator<GeneralRegressionModel> model = allModels.get(position).get(rank);
    Map<FieldName, FieldValue> args = new LinkedHashMap<>();

    try {
      FieldName pickNumField = FieldName.create("startPick");
      args.put(pickNumField, model.prepare(pickNumField, pickNum));

      for (int i = 0; i < numFilled.length; i++) {
        FieldName numFilledField = FieldName.create(
            "has" + (numFilled.length == 1 ? "" : (i + 1)) + position.getShortName());
        args.put(numFilledField, model.prepare(numFilledField, numFilled[i]));
      }

      Map<FieldName, ?> results = model.evaluate(args);
      return ((Double) results.get(model.getTargetField())).floatValue();
    } catch (EvaluationException e) {
      e.printStackTrace();
    }
    return 0;
  }

  @Override
  public PlayerColumn getSortCol() {
    return PlayerColumn.PTS;
  }

  @Override
  public boolean getSortColAscending() {
    return false;
  }
}