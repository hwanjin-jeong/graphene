package net.iponweb.disthene.reader.graphite.functions;

import com.google.common.base.Joiner;
import net.iponweb.disthene.reader.beans.TimeSeries;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.exceptions.InvalidArgumentException;
import net.iponweb.disthene.reader.exceptions.TimeSeriesNotAlignedException;
import net.iponweb.disthene.reader.graphite.PathTarget;
import net.iponweb.disthene.reader.graphite.Target;
import net.iponweb.disthene.reader.graphite.evaluation.TargetEvaluator;
import net.iponweb.disthene.reader.utils.CollectionUtils;
import net.iponweb.disthene.reader.utils.TimeSeriesUtils;

import java.util.*;

/**
 * @author Andrei Ivanov
 * @author jerome89
 */
public class SumSeriesWithWildcardsFunction extends DistheneFunction {


    public SumSeriesWithWildcardsFunction(String text) {
        super(text, "sumSeriesWithWildcards");
    }

    @Override
    public List<TimeSeries> evaluate(TargetEvaluator evaluator) throws EvaluationException {
        List<TimeSeries> processedArguments = new ArrayList<>(evaluator.eval((Target) arguments.get(0)));

        if (processedArguments.size() == 0) return new ArrayList<>();

        if (!TimeSeriesUtils.checkAlignment(processedArguments)) {
            throw new TimeSeriesNotAlignedException();
        }

        int[] positions = new int[arguments.size() - 1];
        for (int i = 1; i < arguments.size(); i++) {
            positions[i - 1] = ((Double) arguments.get(i)).intValue();
        }

        // put series into buckets according to position
        Map<String, List<TimeSeries>> buckets = new HashMap<>();

        for (TimeSeries ts : processedArguments) {
            String bucketName = getBucketName(ts.getName(), positions);
            if (!buckets.containsKey(bucketName)) buckets.put(bucketName, new ArrayList<TimeSeries>());
            buckets.get(bucketName).add(ts);
        }

        // build new time series now
        long from = processedArguments.get(0).getFrom();
        long to = processedArguments.get(0).getTo();
        int step = processedArguments.get(0).getStep();
        int length = processedArguments.get(0).getValues().length;

        List<TimeSeries> resultTimeSeries = new ArrayList<>();

        for (Map.Entry<String, List<TimeSeries>> bucket : buckets.entrySet()) {
            TimeSeries timeSeries = new TimeSeries(bucket.getKey(), from, to, step);
            Double[] values = new Double[length];

            for (int i = 0; i < length; i++) {
                List<Double> points = new ArrayList<>();
                for (TimeSeries ts : bucket.getValue()) {
                    points.add(ts.getValues()[i]);
                }
                values[i] = CollectionUtils.sum(points);
            }

            timeSeries.setValues(values);
            timeSeries.setName(bucket.getKey());
            resultTimeSeries.add(timeSeries);
        }

        return resultTimeSeries;
    }

    private String getBucketName(String name, int[] positions) {
        String[] split = name.split("\\.");
        for (int position : positions) {
            if (position < split.length) {
                split[position] = null;
            }
        }
        return Joiner.on(".").skipNulls().join(split);
    }

    @Override
    public void checkArguments() throws InvalidArgumentException {
        check(arguments.size() >= 2,
            "sumSeriesWithWildcards: number of arguments is " + arguments.size() + ". Must be at least two.");

        Optional<Object> argSeries = Optional.of(arguments.get(0));
        check(argSeries.orElse(null) instanceof Target,
            "sumSeriesWithWildcards: argument is " + getClassName(argSeries.orElse(null)) + ". Must be series");

        for (int i = 1; i < arguments.size(); i++) {
            Optional<Object> argNode = Optional.ofNullable(arguments.get(i));
            check(argNode.orElse(null) instanceof Double,
                "sumSeriesWithWildcards: argument " + i + " is " + getClassName(argNode.orElse(null)) + ". Must be a number");
        }
    }
}
