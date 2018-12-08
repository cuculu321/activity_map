# pd_activity
金沢・野々市市の避難所を表示するAndroidアプリ

## 使用したデータ
- [金沢市オープンデータカタログ](https://www4.city.kanazawa.lg.jp/11010/opendata/catalog.html)

csvはセルに改行データが含まれていたりいなかったりするので、Pythonで整形してから使いましょう。

## 機能
- 金沢・野々市市の避難所を表示します。
- GPSを使う場合、自分の周辺の避難所ピンはオレンジ色で表記されます。
- トグルをオンにした状態で避難所ピンをタップすると、GoogleMapで経路が表示されます。

## 動作画面
![動作画面](https://i.imgur.com/sgWzgiU.png)
