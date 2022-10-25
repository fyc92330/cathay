echo '========= 建立pom.xml並移除build.gradle ========='

gradle clean build publish

cp build/publications/customLibrary/pom-default.xml ./pom.xml

mvn install:install-file -DgroupId=com.example -DartifactId=demo -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar -Dfile=./build/libs/demo-0.0.1-SNAPSHOT.jar

mvn dependency:get -DgroupId=com.example -DartifactId=demo -Dversion=0.0.1-SNAPSHOT

mvn clean package

rm build.gradle gradlew gradlew.bat

echo '========= 執行完成 ========='