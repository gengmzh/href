#!/bin/sh

echo 'starting crawler...'
JAVA='java'
JOB_JAR=/root/href/href-crawler/href-crawler-0.1.0-jar-with-dependencies.jar
MAIN_CLASS=cn.seddat.href.crawler.CrawlerScheduler
LOG_FILE=/root/href/href-crawler/logs/crawler.`date +"%Y%m%d"`.log
echo 'jar '$JOB_JAR
echo 'main class '$MAIN_CLASS
echo 'log '$LOG_FILE

nohup $JAVA -cp $JOB_JAR $MAIN_CLASS >>$LOG_FILE 2>&1 &

echo 'crawler started'
exit 0
