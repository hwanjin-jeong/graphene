package net.iponweb.disthene.reader.graphite.functions;

import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.exceptions.InvalidArgumentException;
import net.iponweb.disthene.reader.exceptions.MultipleDivisorsException;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author Andrei Ivanov
 * @author jerome89
 */
public class DivideSeriesFunction extends DistheneFunction {

    public DivideSeriesFunction(String text) {
        super(text, "divideSeries");
    }

    private final static Logger logger = Logger.getLogger(DivideSeriesFunction.class);

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        List<TimeSeries> dividends = new ArrayList<>(evaluator.eval((Target) arguments.get(0)));

        if (dividends.size() == 0) return Collections.emptyList();

        List<TimeSeries> divisor = evaluator.eval((Target) arguments.get(1));
        if (divisor.size() == 0) return Collections.emptyList();
        if (divisor.size() != 1) throw new MultipleDivisorsException();

        return compute(dividends, divisor.get(0));
    }

    private List<TimeSeries> compute(List<TimeSeries> dividends, TimeSeries divisor) {
        for (TimeSeries ts : dividends) {
            for (int i = 0; i < divisor.getValues().length; i++) {
                if (null == divisor.getValues()[i] || null == ts.getValues()[i] || divisor.getValues()[i] == 0) {
                    ts.getValues()[i] = null;
                } else {
                    ts.getValues()[i] = ts.getValues()[i] / divisor.getValues()[i];
                }
            }
            ts.setName(getText());
        }
        return dividends;
    }

    @Override
    public List<TimeSeries> computeDirectly(List<TimeSeries> seriesListToCompute) {
        if (seriesListToCompute.size() != 2) {
            logger.warn("divideSeries: computeDirectly method should provide length 2 list of TimeSeries");
            return Collections.emptyList();
        }

        return compute(Collections.singletonList(seriesListToCompute.get(0)), seriesListToCompute.get(1));
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        check(arguments.size() == 2,
            "divideSeries: number of arguments is " + arguments.size() + ". Must be two.");

        for (Object arg : arguments) {
            Optional<Object> argSeries = Optional.ofNullable(arg);
            check(argSeries.orElse(null) instanceof Target,
                "divideSeries: argument is " + getClassName(argSeries.orElse(null)) + ". Must be series");
        }
    }
}
