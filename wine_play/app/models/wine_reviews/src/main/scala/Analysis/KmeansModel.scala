package models.wine_reviews.src.main.scala.Analysis

import models.wine_reviews.src.main.scala.Data.Wine.{getSpark, getWineDF}
import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.ml.evaluation.ClusteringEvaluator
import org.apache.spark.ml.feature.FeatureHasher
import org.apache.spark.sql.DataFrame


object KmeansModel extends App {

  /* Get SparkSession and DataFrame */
  val spark = getSpark
  val df = getWineDF

  // Feature Harsher - for the String-valued data, desides 'description'
  val hasher = new FeatureHasher()
    .setInputCols("country", "price", "province", "region_1", "title", "variety")
    .setOutputCol("features")

  val featurized = hasher.transform(df)
  println("After Harsher: ")
  featurized.show()
//  featurized.take(10).foreach(println)



//   Train a k-means model
  val kmeans = new KMeans().setK(12).setSeed(1L).setFeaturesCol("features")
  val model = kmeans.fit(featurized)

  val predictions: DataFrame = model.transform(featurized)

  // Evaluate clustering by computing Within Set Sum of Squared Errors.
  val evaluator = new ClusteringEvaluator()

  val silhouette = evaluator.evaluate(predictions)

  // Shows the result.
//  println("Cluster Centers: ")
//  model.clusterCenters.take(10).foreach(println)

  model.save("kmeans.model")
  println("kmeans model saved")


  val loadModel = ClusteringEvaluator.load("kmeans.model")

  // Save to JSON file
  predictions.coalesce(1).write.json("csvdata/kmJSON")


}
