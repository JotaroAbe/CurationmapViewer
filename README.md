# キュレーションマップ自動生成システム
作成中。以下の内容は変更される可能性があります。
## キュレーションマップとは
各観点に分割されたまとめ文書の断片と詳細文章をノードとし、まとめ文書の分割された個々の観点からその観点に対する詳細文章に対してリンクを持ったネットワーク構造を図示したものを指す。まとめ文書から詳細文書へとわかりやすく提示することによって、利用者がよりそのクエリの内容を理解しやすいようにすることを目指している。
## システム概要
- 入出力
    - 入力：クエリ  
    ex.)双葉杏、ドラゴンボールファイターズ、横浜DeNAベイスターズ
    - 出力：キュレーションマップ
- UI
    - Webブラウザ上に入力フォームを設置
    - 処理結果をブラウザ上に出力
- 処理概要
    1. Google Custom Search Engineを用いて文書を収集
    2. 文書ごとに解析(形態素等)、テキスト断片への分割
    3. テキスト断片と文書間のリンク生成
    4. テキスト断片、リンクの併合
    5. まとめ文書推定計算(HITS)
    6. リンク先を文書内テキスト断片への変更
- 内部データ
    - キュレーションマップはJSONで表記
- データベース
    - 過去のクエリと出力の保持(API制限対策、同処理複数回実行の回避)
## Jsonデータ
### CurationMap
|key|type|content|
|---|---|---|
|query|string|入力されたクエリ|
|documents|array|Documentの配列|
### Document
|key|type|content|
|---|---|---|
|url|string|Data Source|
|docNum|number|Document Number|
|hub|number|Hub値|
|auth|number|Authority値|
|fragments|array|Fragmentの配列|
|uuid|string|UUID|
### Fragment
|key|type|content|
|---|---|---|
|text|string|本文|
|links|array|Linkの配列|
|uuid|string|UUID|
### Link
|key|type|content|
|---|---|---|
|destDocNum|number|リンク先のDocument Number|
|uuid|string|リンク先のUUID|


## 使用言語・フレームワーク・ライブラリなど
- サーバ
    - Scala
    - Json
    - [Play Framework](https://www.playframework.com/)
    - [MongoDB](https://www.mongodb.com/)
    - [Morphia](https://mongodb.github.io/morphia/)
    - [jsoup](https://jsoup.org/)
    - [FelisCatusZero](https://github.com/ktr-skmt/FelisCatusZero-multilingual)
- クライアント
    - HTML5
    - TypeScript
    - [Jquery](https://jquery.com/)
    - CSS
    - Json
    - [D3.js](https://d3js.org/)
## めもらんだむ
- サーバは必須機能完成。データベースとかはまだ
- 表示部は特にもっといい方法があるかも
- 研究中のシステムのため、具体的な処理内容を変更する可能性あり
