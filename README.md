# TsumTsum

LINE：ディズニー ツムツムのハート交換用自動送信プログラム

# Features

同分送信できます（たぶん）

# Requirement

必要なランタイム・ライブラリ

* Windows10 or lator
* jdk 8
* maven
* sikulixapi 1.1.3

# Installation

sikulixapi 1.1.3 は sikulixsetup-1.1.3 を実行しオプション指定することで生成する。  
※念のため事前に入手して生成したJARを内包している。
```powershell
New-Item -ItemType Directory -Force lib
Push-Location lib
curl.exe -LO "https://launchpad.net/sikuli/obsolete/1.1.3/+download/sikulixsetup-1.1.3.jar"
java -jar sikulixsetup-1.1.3.jar options 2 4.1 notest
Move-Item -Force sikulixapi.jar sikulixapiwin.jar
Pop-Location
```

次に、Mavenで以下のコマンドを実行することでビルドする。

```powershell
mvn clean install
```

# Usage

Mavenでの実行方法

```powershell
mvn exec:java -Dexec.mainClass=club.u_1.tsum.application.bs5.App
```

もしくは生成されたJarを実行することでも動作する。

```powershell
java -jar target/tsum2-1.0-SNAPSHOT-jar-with-dependencies.jar
```

# Note

現状は sikulixapi 1.1.3 でしか動作しない。
sikulixapi 2.0.5 で動作させる準備はできているが、sikulixapi 2.0.5 が実行時エラーとなり動作しないためである。

# Author

* 作成者 Fujii Yuichi
* 所属 proengineer.org
* E-mail fujii.yuichi@outlook.jp

# License

The source code is licensed [MIT](LICENSE). The website content is licensed CC BY 4.0, see [LICENSE](LICENSE).
