# akkawithkamon
Run mvn package && java -javaagent:aspectjweaver.1.8.10.jar -jar target/kamonakka-1.0-SNAPSHOT.jar


Then using docker run
docker run -v /etc/localtime:/etc/localtime:ro -P -p 80:80 -p 2003:2003 -p 8125:8125/udp -p 8126:8126 -p 8083:8083 -p 8086:8086 -p 8084:8084 --name kamon-grafana-dashboard muuki88/grafana_graphite:latest
