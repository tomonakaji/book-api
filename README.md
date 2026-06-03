# book-api

## 概要
コーディングテスト用のリポジトリ。
https://quo-digital.hatenablog.com/entry/2024/03/22/143542

コードはmainブランチに直pushで管理。

## 前提

- Java 17

## 環境変数

アプリ起動前に、DB接続情報を環境変数として設定する。

```bash
export BOOK_API_DB_HOST=localhost
export BOOK_API_DB_PORT=5432
export BOOK_API_DB_NAME=<省略>
export BOOK_API_DB_USER=<省略>
export BOOK_API_DB_PASSWORD=<省略>
```

これらの環境変数は、以下の設定で共通利用する。

- `compose.yaml`
- Spring Boot の datasource / Flyway
- jOOQ のコード生成

## PostgreSQLの起動

```bash
docker compose up -d
docker compose ps
```

## アプリケーションの起動

SpringBoot起動時にFlywayのmigrationが実行される。

```bash
./gradlew bootRun
```

## jOOQコード生成

PostgreSQLが起動済みで、スキーマ作成後に実行すること。

```bash
./gradlew jooqCodegen
```

生成先は `build/generated-src/jooq` です。

## API実行例

### 著者登録

```bash
curl -i -X POST http://localhost:8080/v1/authors \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Suzuki Taro",
    "birthDate": "1990-06-19"
  }'
```

### 著者更新

```bash
curl -i -X PUT http://localhost:8080/v1/authors/1 \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Tanaka Jiro",
    "birthDate": "1991-07-20"
  }'
```

### 書籍登録

```bash
curl -i -X POST http://localhost:8080/v1/books \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "Momotaro",
    "price": 800,
    "authorIds": [1, 2],
    "publicationStatus": "PUBLISHED"
  }'
```

### 書籍更新

```bash
curl -i -X PUT http://localhost:8080/v1/books/1 \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "Kaguyahime",
    "price": 900,
    "authorIds": [3],
    "publicationStatus": "PUBLISHED"
  }'
```

### 著者に紐づく書籍一覧取得

```bash
curl -i http://localhost:8080/v1/authors/1/books
```
