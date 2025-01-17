package com.graphene.writer.input.graphite

import com.graphene.writer.input.GrapheneMetric
import com.graphene.writer.input.MetricConverter
import java.util.Collections
import net.iponweb.disthene.reader.utils.MetricRule

class GraphiteMetricConverter : MetricConverter<GraphiteMetric> {

  override fun convert(metric: GraphiteMetric): GrapheneMetric {
    return GrapheneMetric(
      Collections.emptyMap(),
      convertTags(metric),
      metric.value,
      metric.timestamp
    )
  }

  private fun convertTags(metric: GraphiteMetric): MutableMap<String, String> {
    val separatedMetricKey = metric.key.split(DELIMITER)

    val tags = mutableMapOf<String, String>()
    for ((index, key) in separatedMetricKey.withIndex()) {
      tags["$index"] = MetricRule.generate(key)
    }
    return tags
  }

  companion object {
    private const val DELIMITER = "."
  }
}
