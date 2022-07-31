# sound

音を鳴らす環境を作る

## 必要なもの

* sbt

## 動かし方

`sbt run` ではうまく動きません。jarファイルを作って実行します。

起動するといきなり音が鳴るので注意。ハウリングにも注意。

* `$ sbt assembly`
* `$ java -jar target/scala-3.1.0/sound-assembly-0.1.0-SNAPSHOT.jar`
