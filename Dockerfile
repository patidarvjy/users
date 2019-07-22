FROM openjdk:8
ADD build/libs/users-*.jar users.jar
CMD java -jar users.jar